package org.sui.lang.core.completion.providers

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.sui.lang.core.completion.MvCompletionContext
import org.sui.lang.core.psi.MvPatBinding
import org.sui.lang.core.psi.MvLetStmt
import org.sui.lang.core.psi.containingModule
import org.sui.lang.core.psi.ext.isMsl
import org.sui.lang.core.psiElement
import org.sui.lang.core.resolve.collectCompletionVariants
import org.sui.lang.core.resolve.ref.Namespace
import org.sui.lang.core.resolve2.processItemDeclarations
import org.sui.lang.core.withParent

object StructPatCompletionProvider: MvCompletionProvider() {
    override val elementPattern: ElementPattern<out PsiElement>
        get() =
            PlatformPatterns.psiElement()
                .withParent<MvPatBinding>()
                .withSuperParent(2, psiElement<MvLetStmt>())

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val bindingPat = parameters.position.parent as MvPatBinding
        val module = bindingPat.containingModule ?: return
        val completionCtx = MvCompletionContext(bindingPat, bindingPat.isMsl())

        collectCompletionVariants(result, completionCtx) {
            processItemDeclarations(module, setOf(Namespace.TYPE), it)
        }
    }
}
