package org.sui.lang.core.resolve.ref

import org.sui.lang.core.psi.MvItemSpecRef
import org.sui.lang.core.psi.MvNamedElement
import org.sui.lang.core.psi.ext.itemSpec
import org.sui.lang.core.psi.ext.module
import org.sui.lang.core.psi.ext.mslSpecifiableItems

class MvItemSpecRefReferenceImpl(element: MvItemSpecRef): MvPolyVariantReferenceCached<MvItemSpecRef>(element) {

    override fun multiResolveInner(): List<MvNamedElement> {
        val module = element.itemSpec.module ?: return emptyList()
        val referenceName = element.referenceName
        return module.mslSpecifiableItems
            .filter { it.name == referenceName }
    }

}
