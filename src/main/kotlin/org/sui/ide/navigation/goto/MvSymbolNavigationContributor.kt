package org.sui.ide.navigation.goto

import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import org.sui.lang.core.psi.MvNamedElement
import org.sui.openapiext.allMoveFiles


class MvSymbolNavigationContributor: MvNavigationContributorBase() {
    override fun processNames(
        processor: Processor<in String>,
        scope: GlobalSearchScope,
        filter: IdFilter?
    ) {
        // get all names
        val project = scope.project ?: return
        val visitor = object: MvNamedElementsVisitor() {
            override fun processNamedElement(element: MvNamedElement) {
                val elementName = element.name ?: return
                processor.process(elementName)
            }
        }
        project.allMoveFiles().map { it.accept(visitor) }
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters
    ) {
        val project = parameters.project
        val visitor = object: MvNamedElementsVisitor() {
            override fun processNamedElement(element: MvNamedElement) {
                val elementName = element.name ?: return
                if (elementName == name) processor.process(element)
            }
        }
        project.allMoveFiles().map { it.accept(visitor) }
    }
}
