package org.sui.lang.core.psi.ext

import org.sui.lang.core.psi.MvStruct
import org.sui.lang.core.psi.MvStructPat

val MvStructPat.patFieldNames: List<String>
    get() =
        fieldPatList.map { it.referenceName }

val MvStructPat.structItem: MvStruct? get() = this.path.reference?.resolveFollowingAliases() as? MvStruct
