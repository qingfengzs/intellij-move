package org.sui.lang.core.psi.ext

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.sui.cli.MoveProject
import org.sui.lang.core.psi.*
import org.sui.lang.core.resolve.ref.MvPolyVariantReference
import org.sui.lang.core.resolve.ref.MvPolyVariantReferenceBase
import org.sui.lang.core.resolve2.ref.MvMethodCallReferenceImpl
import org.sui.lang.core.types.address
import org.sui.lang.core.types.infer.inference
import org.sui.lang.core.types.ty.Ty
import org.sui.lang.core.types.ty.TyAdt
import org.sui.lang.core.types.ty.TyVector
import org.sui.stdext.wrapWithList

fun Ty.itemModule(moveProject: MoveProject): MvModule? {
    val norefTy = this.derefIfNeeded()
    return when (norefTy) {
        is TyVector -> {
            moveProject
                .getModulesFromIndex("vector")
                .firstOrNull { it.is0x1Address(moveProject) }
        }
        is TyAdt -> norefTy.item.module
        else -> null
    }
}

fun MvModule.is0x1Address(moveProject: MoveProject): Boolean = this.address(moveProject)?.is0x1 ?: false

abstract class MvMethodCallMixin(node: ASTNode): MvElementImpl(node), MvMethodCall {

    override fun getReference(): MvPolyVariantReference = MvMethodCallReferenceImpl(this)
}

