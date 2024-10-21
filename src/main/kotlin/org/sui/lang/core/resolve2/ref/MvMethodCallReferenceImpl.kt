package org.sui.lang.core.resolve2.ref

import com.intellij.psi.PsiElement
import org.sui.lang.core.psi.MvElement
import org.sui.lang.core.psi.MvFunction
import org.sui.lang.core.psi.MvMethodCall
import org.sui.lang.core.psi.MvNamedElement
import org.sui.lang.core.psi.ext.isMsl
import org.sui.lang.core.resolve.ScopeEntry
import org.sui.lang.core.resolve.ref.MvPolyVariantReferenceBase
import org.sui.lang.core.resolve.ref.NAMES
import org.sui.lang.core.resolve.ref.Namespace
import org.sui.lang.core.types.infer.inference
import org.sui.stdext.wrapWithList

class MvMethodCallReferenceImpl(
    element: MvMethodCall
): MvPolyVariantReferenceBase<MvMethodCall>(element) {

    override fun multiResolve(): List<MvNamedElement> {
        val msl = element.isMsl()
        val inference = element.inference(msl) ?: return emptyList()
        return inference.getResolvedMethod(element).wrapWithList()
    }

    override fun isReferenceTo(element: PsiElement): Boolean =
        element is MvFunction && super.isReferenceTo(element)
}

interface DotExprResolveVariant : ScopeEntry {
    /** The receiver type after possible derefs performed */
//    val selfTy: Ty
    /** The number of `*` dereferences should be performed on receiver to match `selfTy` */
//    val derefCount: Int

    override val namespaces: Set<Namespace>
        get() = NAMES // Namespace does not matter in the case of dot expression

    override fun doCopyWithNs(namespaces: Set<Namespace>): ScopeEntry = this
}

data class FieldResolveVariant(
    override val name: String,
    override val element: MvNamedElement,
//    override val selfTy: Ty,
//    val derefSteps: List<Autoderef.AutoderefStep>,
//    val obligations: List<Obligation>,
): DotExprResolveVariant