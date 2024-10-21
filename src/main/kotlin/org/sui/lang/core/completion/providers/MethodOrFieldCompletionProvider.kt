package org.sui.lang.core.completion.providers

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.VisibleForTesting
import org.sui.lang.core.completion.MvCompletionContext
import org.sui.lang.core.completion.createLookupElement
import org.sui.lang.core.psi.MvFunction
import org.sui.lang.core.psi.ext.*
import org.sui.lang.core.psi.tyVarsSubst
import org.sui.lang.core.resolve.collectCompletionVariants
import org.sui.lang.core.resolve.createProcessor
import org.sui.lang.core.resolve2.processMethodResolveVariants
import org.sui.lang.core.types.infer.InferenceContext
import org.sui.lang.core.types.infer.substitute
import org.sui.lang.core.types.ty.*
import org.sui.lang.core.withParent

object MethodOrFieldCompletionProvider: MvCompletionProvider() {
    override val elementPattern: ElementPattern<out PsiElement>
        get() =
            PlatformPatterns
                .psiElement()
                .withParent<MvMethodOrField>()

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val pos = parameters.position
        val element = pos.parent as MvMethodOrField

        addMethodOrFieldVariants(element, result)
    }

    @VisibleForTesting
    fun addMethodOrFieldVariants(element: MvMethodOrField, result: CompletionResultSet) {
        val msl = element.isMsl()
        val receiverTy = element.inferReceiverTy(msl).knownOrNull() ?: return
        val expectedTy = getExpectedTypeForEnclosingPathOrDotExpr(element, msl)

        val ctx = MvCompletionContext(element, msl, expectedTy)

        val tyAdt = receiverTy.derefIfNeeded() as? TyAdt
        if (tyAdt != null) {
            collectCompletionVariants(result, ctx, subst = tyAdt.substitution) {
                processNamedFieldVariants(element, tyAdt, msl, it)
            }
        }

        processMethodResolveVariants(element, receiverTy, ctx.msl, createProcessor { e ->
            val function = e.element as? MvFunction ?: return@createProcessor
            val subst = function.tyVarsSubst
            val declaredFuncTy = function.functionTy(msl).substitute(subst) as TyFunction
            val declaredSelfTy = declaredFuncTy.paramTypes.first()
            val autoborrowedReceiverTy =
                TyReference.autoborrow(receiverTy, declaredSelfTy)
                    ?: error("unreachable, references always compatible")

            val inferenceCtx = InferenceContext(msl)
            inferenceCtx.combineTypes(declaredSelfTy, autoborrowedReceiverTy)

            result.addElement(
                createLookupElement(
                    e,
                    ctx,
                    subst = inferenceCtx.resolveTypeVarsIfPossible(subst)
                )
            )
        })
    }
}