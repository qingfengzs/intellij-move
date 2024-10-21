package org.sui.lang.core.types

import org.sui.lang.core.psi.MvEnum
import org.sui.lang.core.psi.MvSchema
import org.sui.lang.core.psi.MvStruct
import org.sui.lang.core.psi.MvTypeParameter
import org.sui.lang.core.psi.tyTypeParams
import org.sui.lang.core.psi.typeParamsToTypeParamsSubst
import org.sui.lang.core.types.ty.Ty
import org.sui.lang.core.types.ty.TyAdt
import org.sui.lang.core.types.ty.TySchema
import org.sui.lang.core.types.ty.TyTypeParameter

object MvPsiTypeImplUtil {
    fun declaredType(psi: MvTypeParameter): Ty = TyTypeParameter.named(psi)
    fun declaredType(psi: MvStruct): Ty = TyAdt.valueOf(psi)
    fun declaredType(psi: MvEnum): Ty = TyAdt.valueOf(psi)
    fun declaredType(psi: MvSchema): Ty = TySchema(psi, psi.typeParamsToTypeParamsSubst, psi.tyTypeParams)
}