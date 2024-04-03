package org.sui.lang

import com.intellij.lang.Language

object MoveLanguage : Language("Move") {
    override fun isCaseSensitive() = true
    override fun getDisplayName() = "Move"
}
