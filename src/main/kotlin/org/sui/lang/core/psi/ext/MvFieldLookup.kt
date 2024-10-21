package org.sui.lang.core.psi.ext

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.sui.lang.core.psi.*
import org.sui.lang.core.resolve.*
import org.sui.lang.core.resolve.ref.MvPolyVariantReference
import org.sui.lang.core.resolve.ref.MvPolyVariantReferenceBase
import org.sui.lang.core.resolve.ref.NONE
import org.sui.lang.core.resolve2.ref.FieldResolveVariant
import org.sui.lang.core.types.infer.inference
import org.sui.lang.core.types.ty.TyAdt
import org.sui.stdext.wrapWithList

fun processNamedFieldVariants(
    element: MvMethodOrField,
    receiverTy: TyAdt,
    msl: Boolean,
    processor: RsResolveProcessor
): Boolean {
    val receiverItem = receiverTy.item
    if (!isFieldsAccessible(element, receiverItem, msl)) return false

    return when (receiverItem) {
        is MvStruct -> processor.processAll(NONE, receiverItem.namedFields)
        is MvEnum -> {
            val visitedFields = mutableSetOf<String>()
            for (variant in receiverItem.variants) {
                val visitedVariantFields = mutableSetOf<String>()
                for (namedField in variant.namedFields) {
                    val fieldName = namedField.name
                    if (fieldName in visitedFields) continue
                    if (processor.process(NONE, namedField)) return true
                    // collect all names for the variant
                    visitedVariantFields.add(fieldName)
                }
                // add variant fields to the global fields list to skip them in the next variants
                visitedFields.addAll(visitedVariantFields)
            }
            false
        }
        else -> error("unreachable")
    }
}

// todo: change into VisibilityFilter
fun isFieldsAccessible(
    element: MvElement,
    item: MvStructOrEnumItemElement,
    msl: Boolean
): Boolean {
    if (!msl) {
        // cannot resolve field if not in the same module as struct definition
        val dotExprModule = element.namespaceModule ?: return false
        if (item.containingModule != dotExprModule) return false
    }
    return true
}

class MvFieldLookupReferenceImpl(
    element: MvFieldLookup
): MvPolyVariantReferenceBase<MvFieldLookup>(element) {

    override fun multiResolve(): List<MvNamedElement> {
        val msl = element.isMsl()
        val receiverExpr = element.receiverExpr
        val inference = receiverExpr.inference(msl) ?: return emptyList()
        return inference.getResolvedField(element).wrapWithList()
    }
}

abstract class MvFieldLookupMixin(node: ASTNode): MvElementImpl(node),
                                                  MvFieldLookup {

    override val referenceNameElement: PsiElement get() = (identifier ?: integerLiteral)!!

    override fun getReference(): MvPolyVariantReference = MvFieldLookupReferenceImpl(this)
}
