package org.sui.cli.settings

import com.intellij.ui.JBColor
import javax.swing.JLabel

class VersionLabel : JLabel() {

    fun setVersion(version: String?) {
        if (version == null) {
            this.text = INVALID_VERSION
            this.foreground = JBColor.RED
        } else {
            // preformat version in case of multiline string
            this.text = version
                .split("\n")
                .joinToString("<br>", "<html>", "</html>")
            this.foreground = JBColor.foreground()
        }
    }

    companion object {
        const val INVALID_VERSION = "N/A"
    }
}
