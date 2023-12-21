package org.sui.ide.navigation

import com.intellij.codeInsight.hint.DeclarationRangeHandler
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.startOffset
import org.sui.lang.core.psi.MvFunction
import org.sui.lang.core.psi.MvModule
import org.sui.lang.core.psi.ext.endOffset
import org.sui.lang.core.psi.ext.getPrevNonCommentSibling

class ModuleDeclarationRangeHandler : DeclarationRangeHandler<MvModule> {
    override fun getDeclarationRange(container: MvModule): TextRange {
        val startOffset = container.firstNonCommentChild.startOffset
        val endOffset = (container.moduleBlock?.getPrevNonCommentSibling() ?: container).endOffset
        return TextRange(startOffset, endOffset)
    }
}

class FunctionDeclarationRangeHandler : DeclarationRangeHandler<MvFunction> {
    override fun getDeclarationRange(container: MvFunction): TextRange {
        val startOffset = container.firstNonCommentChild.startOffset
        val endOffset = (container.codeBlock?.getPrevNonCommentSibling() ?: container).endOffset
        return TextRange(startOffset, endOffset)
    }
}

private val PsiElement.firstNonCommentChild: PsiElement get() = this.children.first { it !is PsiComment }