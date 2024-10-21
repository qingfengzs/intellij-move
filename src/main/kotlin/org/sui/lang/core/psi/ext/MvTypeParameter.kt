package org.sui.lang.core.psi.ext

import com.intellij.lang.ASTNode
import org.sui.lang.MvElementTypes
import org.sui.lang.core.psi.MvAbility
import org.sui.lang.core.psi.MvTypeParameter
import org.sui.lang.core.psi.impl.MvNameIdentifierOwnerImpl
import org.sui.lang.core.types.MvPsiTypeImplUtil
import org.sui.lang.core.types.ty.Ty
import org.sui.lang.core.types.ty.TyTypeParameter

val MvTypeParameter.isPhantom get() = hasChild(MvElementTypes.PHANTOM)

val MvTypeParameter.abilityBounds: List<MvAbility>
    get() {
        return typeParamBound?.abilityList.orEmpty()
    }

abstract class MvTypeParameterMixin(node: ASTNode) : MvNameIdentifierOwnerImpl(node),
                                                     MvTypeParameter {

    override fun declaredType(msl: Boolean): Ty = MvPsiTypeImplUtil.declaredType(this)
}