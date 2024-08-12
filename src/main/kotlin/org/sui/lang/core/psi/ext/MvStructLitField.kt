package org.sui.lang.core.psi.ext

import com.intellij.lang.ASTNode
import org.sui.lang.MvElementTypes
import org.sui.lang.core.psi.*
import org.sui.lang.core.resolve.RsResolveProcessor
import org.sui.lang.core.resolve.SimpleScopeEntry
import org.sui.lang.core.resolve.collectResolveVariants
import org.sui.lang.core.resolve.ref.MvMandatoryReferenceElement
import org.sui.lang.core.resolve.ref.MvPolyVariantReference
import org.sui.lang.core.resolve.ref.MvPolyVariantReferenceCached
import org.sui.lang.core.resolve.ref.Namespace
import org.sui.lang.core.resolve2.resolveBindingForFieldShorthand

val MvStructLitField.structLitExpr: MvStructLitExpr
    get() = ancestorStrict()!!

val MvStructLitField.isShorthand: Boolean
    get() = !hasChild(MvElementTypes.COLON)

inline fun <reified T : MvElement> MvStructLitField.resolveToElement(): T? =
    reference.multiResolve().filterIsInstance<T>().singleOrNull()

fun MvStructLitField.resolveToDeclaration(): MvNamedFieldDecl? = resolveToElement()
fun MvStructLitField.resolveToBinding(): MvBindingPat? = resolveToElement()

interface MvFieldRef : MvMandatoryReferenceElement

abstract class MvStructLitFieldMixin(node: ASTNode) : MvElementImpl(node),
                                                      MvStructLitField {
    override fun getReference(): MvPolyVariantReference =
        MvFieldReferenceImpl(this, shorthand = this.isShorthand)
}

class MvFieldReferenceImpl(
    element: MvFieldRef,
    var shorthand: Boolean,
) : MvPolyVariantReferenceCached<MvFieldRef>(element) {

    override fun multiResolveInner(): List<MvNamedElement> {
        val referenceName = element.referenceName
        var variants = collectResolveVariants(referenceName) {
            processStructRefFieldResolveVariants(element, it)
        }
        if (shorthand) {
            variants += resolveBindingForFieldShorthand(element)
        }
        return variants
    }
}

fun processStructRefFieldResolveVariants(
    fieldRef: MvFieldRef,
    processor: RsResolveProcessor
): Boolean {
    val fieldsOwnerItem = fieldRef.fieldOwner ?: return false
    return fieldsOwnerItem.fields
        .any { field ->
            processor.process(SimpleScopeEntry(field.name, field, setOf(Namespace.NAME)))
        }
}

private val MvFieldRef.fieldOwner: MvFieldsOwner?
    get() {
        return when (this) {
            is MvFieldPat -> {
                this.structPat.path.reference?.resolveFollowingAliases() as? MvFieldsOwner
            }

            is MvStructLitField -> {
                this.structLitExpr.path.reference?.resolveFollowingAliases() as? MvFieldsOwner
            }

            else -> null
        }
    }



