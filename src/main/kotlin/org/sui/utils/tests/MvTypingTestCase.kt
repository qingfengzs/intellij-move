package org.sui.utils.tests

import org.intellij.lang.annotations.Language
import org.sui.lang.MoveFileType

abstract class MvTypingTestCase : MvTestBase() {
    protected fun doTest(c: Char = '\n') = checkByFile {
        myFixture.type(c)
    }

    protected fun doTest(@Language("Sui Move") before: String, type: Char, @Language("Sui Move") after: String) {
        val beforeText = replaceCaretMarker(before)
        val afterText = replaceCaretMarker(after)

        myFixture.configureByText(MoveFileType, beforeText)
        myFixture.type(type)
        myFixture.checkResult(afterText)
    }

    protected fun doTestByText(
        @Language("Sui Move") before: String,
        @Language("Sui Move") after: String,
        c: Char = '\n'
    ) =
        checkByText(before.trimIndent(), after.trimIndent()) {
            myFixture.type(c)
        }

}
