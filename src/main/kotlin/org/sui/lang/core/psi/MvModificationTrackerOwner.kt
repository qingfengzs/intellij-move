/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.sui.lang.core.psi

import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.sui.lang.core.psi.ext.stubParent
import kotlin.reflect.KClass

class MvModificationTracker(val owner: MvModificationTrackerOwner): SimpleModificationTracker() {
}

/**
 * A PSI element that holds modification tracker for some reason.
 * This is mostly used to invalidate cached type inference results.
 */
interface MvModificationTrackerOwner: MvElement {
    val modificationTracker: MvModificationTracker

    /**
     * Increments local modification counter if needed.
     *
     * If and only if false returned,
     * [MvPsiManager.moveStructureModificationTracker]
     * will be incremented.
     *
     * @param element the changed psi element
     * @see org.rust.lang.core.psi.RsPsiManagerImpl.updateModificationCount
     */
    fun incModificationCount(element: PsiElement): Boolean
}

fun PsiElement.findModificationTrackerOwner(strict: Boolean): MvModificationTrackerOwner? {
    var element = if (strict) this.parent else this
    while (element != null && element !is PsiFile) {
        if (element is MvModule) return null
        if (element is MvModificationTrackerOwner) return element
        element = element.parent
    }
    return null
//    return findContextOfType(
//        strict,
//        MvFunction::class, MvItemSpec::class,
//    )
//    return context
}

// We have to process contexts without index access because accessing indices during PSI event processing is slow.
//private val PsiElement.contextWithoutIndexAccess: PsiElement?
//    //    get() = if (this is RsExpandedElement) {
////        RsExpandedElement.getContextImpl(this, isIndexAccessForbidden = true)
////    } else {
//    get() = stubParent
////    }

@Suppress("UNCHECKED_CAST")
private fun <T: PsiElement> PsiElement.findContextOfType(
    strict: Boolean,
    vararg classes: KClass<out T>
): T? {
    var element = if (strict) stubParent else this

    while (element != null && !classes.any { it.isInstance(element) }) {
        element = element.stubParent
    }

    return element as T?
}
