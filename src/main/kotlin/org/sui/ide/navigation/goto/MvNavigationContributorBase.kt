package org.sui.ide.navigation.goto

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.GotoClassContributor
import com.intellij.navigation.NavigationItem
import org.sui.lang.core.psi.MvQualNamedElement

abstract class MvNavigationContributorBase: ChooseByNameContributorEx,
                                            GotoClassContributor {

    override fun getQualifiedName(item: NavigationItem): String? =
        (item as? MvQualNamedElement)?.qualName?.editorText()

    override fun getQualifiedNameSeparator(): String = "::"
}