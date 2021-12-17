/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.move.ide.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.annotations.TestOnly
import org.move.openapiext.common.isUnitTestMode

abstract class MvAnnotator : Annotator {
    final override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!isUnitTestMode || javaClass in enabledAnnotators) {
            annotateInternal(element, holder)
        }
    }

    protected abstract fun annotateInternal(element: PsiElement, holder: AnnotationHolder)

    companion object {
        private val enabledAnnotators: MutableSet<Class<out MvAnnotator>> = ContainerUtil.newConcurrentSet()

        @TestOnly
        fun enableAnnotator(annotatorClass: Class<out MvAnnotator>, parentDisposable: Disposable) {
            enabledAnnotators += annotatorClass
            Disposer.register(
                parentDisposable,
                { enabledAnnotators -= annotatorClass }
            )
        }
    }
}