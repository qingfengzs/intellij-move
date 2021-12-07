package org.move.ide.typing

import com.intellij.codeInsight.highlighting.BraceMatchingUtil
import com.intellij.openapi.editor.ex.EditorEx
import org.intellij.lang.annotations.Language
import org.move.lang.MoveFileType
import org.move.utils.tests.MoveTestBase
import org.move.utils.tests.replaceCaretMarker

class BraceMatcherTest : MoveTestBase() {
    fun `test don't pair parenthesis before identifier`() = doTest(
        "script { fun main() { let _ = /*caret*/typing }}",
        '(',
        "script { fun main() { let _ = (/*caret*/typing }}"
    )

    fun `test pair parens before semicolon`() = doTest(
        "script { fun main() { let _ = /*caret*/; }}",
        '(',
        "script { fun main() { let _ = (/*caret*/); }}"
    )

    fun `test pair parens before brace`() = doTest(
        "script { fun main/*caret*/ {}}",
        '(',
        "script { fun main(/*caret*/) {}}"
    )

    fun `test add second pair of angle brackets`() = doTest(
        "script { fun main<NFT/*caret*/>() {}}",
        '<',
        "script { fun main<NFT</*caret*/>>() {}}",
    )

    fun `test dont pair braces inside identifier`() = doTest(
        "script { fun main<NF/*caret*/T>() {}}",
        '<',
        "script { fun main<NF</*caret*/T>() {}}",
    )

    fun `test add pair of angle brackets for struct field`() = doTest(
        "module M { struct Col { a: Option/*caret*/ } }",
        '<',
        "module M { struct Col { a: Option</*caret*/> } }",
    )

    fun `test match parens`() = doMatch("script { fun main/*caret*/(x: u8) {}}", ")")

    fun `test match angle brackets`() = doMatch("script { fun main/*caret*/<T>(x: u8) {}}", ">")

    fun `test no match`() {
        noMatch("script { fun main() { let a = 4 /*caret*/< 5 && 2 > 1; } }")
        noMatch("script { fun main() { let a = 4 /*caret*/<< 5 || 2 >> 1; } }")
        noMatch("script { fun main() { let a = 4 /*caret*/< 5; let b = { 1 >> 2 }; } }")
    }

    private fun noMatch(@Language("Move") source: String) {
        val sourceText = replaceCaretMarker(source)

        myFixture.configureByText(MoveFileType, sourceText)
        val editorHighlighter = (myFixture.editor as EditorEx).highlighter
        val iterator = editorHighlighter.createIterator(myFixture.editor.caretModel.offset)
        val matched = BraceMatchingUtil.matchBrace(myFixture.editor.document.charsSequence,
            myFixture.file.fileType,
            iterator,
            true)
        check(!matched)
    }

    private fun doMatch(@Language("Move") source: String, coBrace: String) {
        val sourceText = replaceCaretMarker(source)

        myFixture.configureByText(MoveFileType, sourceText)
        val expected = sourceText.replace("<caret>", "").lastIndexOf(coBrace)
        check(BraceMatchingUtil.getMatchedBraceOffset(myFixture.editor, true, myFixture.file) == expected)
    }

    private fun doTest(@Language("Move") before: String, type: Char, @Language("Move") after: String) {
        val beforeText = replaceCaretMarker(before)
        val afterText = replaceCaretMarker(after)

        myFixture.configureByText(MoveFileType, beforeText)
        myFixture.type(type)
        myFixture.checkResult(afterText)
    }
}