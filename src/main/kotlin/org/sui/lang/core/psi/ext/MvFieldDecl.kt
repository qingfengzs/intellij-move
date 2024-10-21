package org.sui.lang.core.psi.ext

import org.sui.lang.core.psi.MvNamedElement
import org.sui.lang.core.psi.MvType

val MvFieldDecl.owner: MvFieldsOwner? get() = ancestorStrict()

interface MvFieldDecl: MvDocAndAttributeOwner, MvNamedElement {
    val type: MvType?
}