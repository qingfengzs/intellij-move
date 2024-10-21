package org.sui.lang.core.resolve2.ref

import com.intellij.psi.PsiElement
import org.sui.lang.core.psi.*
import org.sui.lang.core.psi.ext.isMsl
import org.sui.lang.core.resolve.ScopeEntry
import org.sui.lang.core.resolve.collectResolveVariantsAsScopeEntries
import org.sui.lang.core.resolve.ref.*
import org.sui.lang.core.resolve2.processPatBindingResolveVariants
import org.sui.lang.core.types.infer.inference
import org.sui.lang.core.types.ty.Ty
import org.sui.stdext.wrapWithList

class MvBindingPatReferenceImpl(
    element: MvPatBinding
): MvPolyVariantReferenceBase<MvPatBinding>(element) {

    override fun multiResolve(): List<MvNamedElement> =
        rawMultiResolveUsingInferenceCache() ?: rawCachedMultiResolve()
//        resolvePatBindingRaw(element, expectedType = null).map { it.element }

//    override fun multiResolveInner(): List<MvNamedElement> =
//        resolvePatBindingRaw(element, expectedType = null).map { it.element }
//        collectResolveVariants(element.referenceName) {
//            processPatBindingResolveVariants(element, false, it)
//        }

    private fun rawMultiResolveUsingInferenceCache(): List<MvNamedElement>? {
        val parent = element.parent as? MvElement ?: return null
        if (parent is MvMatchArm || parent is MvPat) {
            val msl = parent.isMsl()
            return parent.inference(msl)?.getResolvedPatBinding(element).wrapWithList()
        } else {
            return null
        }
    }

    private fun rawCachedMultiResolve(): List<MvNamedElement> =
        resolvePatBindingRaw(element, expectedType = null).map { it.element }

    override fun handleElementRename(newName: String): PsiElement {
        if (element.parent !is MvPatField) return super.handleElementRename(newName)
        val newFieldPat = element.project.psiFactory.fieldPatFull(newName, element.text)
        return element.replace(newFieldPat)
    }
}

fun resolvePatBindingRaw(binding: MvPatBinding, expectedType: Ty? = null): List<ScopeEntry> {
    val resolveVariants =
        collectResolveVariantsAsScopeEntries(binding.referenceName) {
            val filteringProcessor = filterEnumVariantsByExpectedType(expectedType, it)
            processPatBindingResolveVariants(
                binding,
                false,
                filteringProcessor
            )
            }
    return resolveVariants
}
