package org.sui.utils.tests.completion

import org.intellij.lang.annotations.Language
import org.sui.utils.tests.MvLightTestBase

abstract class CompletionTestCase : MvLightTestBase() {
    lateinit var completionFixture: MvCompletionTestFixture

    override fun setUp() {
        super.setUp()
        completionFixture = MvCompletionTestFixture(myFixture)
        completionFixture.setUp()
    }

    override fun tearDown() {
        completionFixture.tearDown()
        super.tearDown()
    }

    protected fun doFirstCompletion(
        @Language("Sui Move") before: String,
        @Language("Sui Move") after: String
    ) = completionFixture.doFirstCompletion(before, after)

    protected fun doSingleCompletion(
        @Language("Sui Move") before: String,
        @Language("Sui Move") after: String
    ) = completionFixture.doSingleCompletion(before, after)

    protected fun checkContainsCompletion(
        variant: String,
        @Language("Sui Move") code: String
    ) = completionFixture.checkContainsCompletion(code, variant)

    protected fun checkContainsCompletion(
        variants: List<String>,
        @Language("Sui Move") code: String
    ) = completionFixture.checkContainsCompletion(code, variants)

    protected fun checkCompletion(
        lookupString: String,
        @Language("Sui Move") before: String,
        @Language("Sui Move") after: String,
        completionChar: Char = '\n',
    ) = completionFixture.checkCompletion(lookupString, before, after, completionChar)

    protected fun checkNotContainsCompletion(
        variant: String,
        @Language("Sui Move") code: String
    ) = completionFixture.checkNotContainsCompletion(code, variant)

    protected fun checkNoCompletion(@Language("Sui Move") code: String) = completionFixture.checkNoCompletion(code)

}
