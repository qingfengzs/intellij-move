package org.sui.lang.core.psi.ext

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import org.sui.ide.MoveIcons
import org.sui.lang.core.psi.MvElementImpl
import org.sui.lang.core.psi.MvTupleFieldDecl
import javax.swing.Icon

val MvTupleFieldDecl.position: Int?
    get() = owner?.positionalFields?.withIndex()?.firstOrNull { it.value === this }?.index

abstract class MvTupleFieldDeclMixin(node: ASTNode): MvElementImpl(node), MvTupleFieldDecl {

    override fun getIcon(flags: Int): Icon? = MoveIcons.FIELD

    override fun getName(): String? = position?.toString()

    override fun setName(name: String): PsiElement {
        throw IncorrectOperationException("Cannot rename fake named element")
    }
}