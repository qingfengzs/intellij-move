package org.sui.lang.core.psi.ext

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.sui.lang.core.psi.MvElementImpl
import org.sui.lang.core.psi.MvFunction
import org.sui.lang.core.psi.MvFunctionParameter
import org.sui.lang.core.psi.MvFunctionParameterList

// BindingPat has required name
val MvFunctionParameter.name: String get() = this.patBinding.name

val MvFunctionParameter.paramIndex: Int get() =
    (this.parent as MvFunctionParameterList).functionParameterList.indexOf(this)

val MvFunctionParameter.isSelfParam: Boolean get() =
    this.patBinding.name == "self" && this.paramIndex == 0

var MvFunctionParameter.resolveContext: MvFunction?
    get() = (this as MvFunctionParameterMixin).resolveContext
    set(value) {
        (this as MvFunctionParameterMixin).resolveContext = value
    }

abstract class MvFunctionParameterMixin(node: ASTNode) : MvElementImpl(node), MvFunctionParameter {
    var resolveContext: MvFunction? = null

    override fun getContext(): PsiElement? = resolveContext ?: super.getContext()
}
