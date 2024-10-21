package org.sui.lang.core.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import org.sui.cli.settings.moveSettings
import org.sui.lang.MvElementTypes.*
import org.sui.lang.core.*
import org.sui.lang.core.MvPsiPattern.afterAnySibling
import org.sui.lang.core.MvPsiPattern.anySpecStart
import org.sui.lang.core.MvPsiPattern.codeStatementPattern
import org.sui.lang.core.MvPsiPattern.function
import org.sui.lang.core.MvPsiPattern.identifierStatementBeginningPattern
import org.sui.lang.core.MvPsiPattern.itemSpecStmt
import org.sui.lang.core.MvPsiPattern.module
import org.sui.lang.core.MvPsiPattern.moduleSpecBlock
import org.sui.lang.core.MvPsiPattern.script
import org.sui.lang.core.MvPsiPattern.toplevel
import org.sui.lang.core.MvPsiPattern.typeParameter
import org.sui.lang.core.completion.providers.FunctionModifierCompletionProvider
import org.sui.lang.core.completion.providers.KeywordCompletionProvider

class KeywordCompletionContributor: CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            toplevel().and(identifierStatementBeginningPattern()),
            KeywordCompletionProvider("address", "module", "script", "spec")
        )
        extend(
            CompletionType.BASIC,
            script().and(identifierStatementBeginningPattern()),
            KeywordCompletionProvider(
                "public",
                "fun",
                "const",
                "use"
            )
        )
        extend(
            CompletionType.BASIC,
            module().and(identifierStatementBeginningPattern()),
            KeywordCompletionProvider(
                *VISIBILITY_MODIFIERS,
                *CONTEXT_FUNCTION_MODIFIERS,
                "native",
                "fun",
                "struct",
                "const",
                "use",
                "spec",
                "friend",
                "enum",
                "type",
                "match",
                "mut"
            )
        )
        extend(
            CompletionType.BASIC,
            moduleSpecBlock().and(identifierStatementBeginningPattern()),
            KeywordCompletionProvider(
                "use",
                "spec",
            )
        )
        extend(
            CompletionType.BASIC,
            function().with(afterAnySibling(FUNCTION_MODIFIERS)),
            FunctionModifierCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            function().with(afterAnySibling(FUNCTION_MODIFIERS)),
            KeywordCompletionProvider("fun")
        )
//        extend(
//            CompletionType.BASIC,
//            function().with(MvPsiPattern.AfterSibling(NATIVE)),
//            FunctionModifierCompletionProvider()
//        )
//        extend(
//            CompletionType.BASIC,
//            module().and(identifierStatementBeginningPattern("native")),
//            KeywordCompletionProvider(*VISIBILITY_MODIFIERS, "fun", "entry")
//        )
        extend(
            CompletionType.BASIC,
            codeStatementPattern().and(identifierStatementBeginningPattern()),
            KeywordCompletionProvider(
                "let",
                "loop",
                "while",
                "continue",
                "break",
                "if",
                "else",
                "abort",
                "return",
                "for",
                "match",
            )
        )
        extend(
            CompletionType.BASIC,
            itemSpecStmt().and(identifierStatementBeginningPattern()),
            KeywordCompletionProvider(
                "pragma",
                "let",
                "use",
                "include",
                "apply",
                "requires",
                "ensures",
                "invariant",
                "modifies",
                "aborts_if",
                "aborts_with",
                "assume",
                "assert",
            )
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                psiElement()
                    .with(MvPsiPattern.AfterSibling(FUNCTION_PARAMETER_LIST)),
                psiElement()
                    .with(MvPsiPattern.AfterAnySibling(TYPES))
            ),
            KeywordCompletionProvider("acquires")
        )
        extend(
            CompletionType.BASIC,
            typeParameter(),
            KeywordCompletionProvider("phantom")
        )
        extend(
            CompletionType.BASIC,
            anySpecStart(),
            KeywordCompletionProvider("module", "fun", "schema")
        )
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(
            parameters,
            CommonCompletionContributor.withSorter(parameters, result)
        )
    }
}

private val VISIBILITY_MODIFIERS = arrayOf(
    "public",
//    "public(script)",
    "public(friend)",
    "public(package)"
)

private val CONTEXT_FUNCTION_MODIFIERS = arrayOf("entry", "inline")

