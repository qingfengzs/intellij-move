/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.move.ide.newProject

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.platform.GeneratorPeerImpl
import com.intellij.ui.dsl.builder.panel
import org.move.cli.defaultProjectSettings
import org.move.cli.settings.SuiExec
import org.move.cli.settings.SuiSettingsPanel
import org.move.cli.settings.isValidExecutable
import javax.swing.JComponent

class SuiProjectGeneratorPeer(val parentDisposable: Disposable) : GeneratorPeerImpl<SuiProjectConfig>() {

    private val suiSettingsPanel =
        SuiSettingsPanel(showDefaultProjectSettingsLink = false) { checkValid?.run() }

    init {
        Disposer.register(parentDisposable, suiSettingsPanel)
    }

//    private val aptosInitCheckBox = JBCheckBox("Run 'aptos init'", false)
//    private val aptosSettingsPanel = AptosSettingsPanel(aptosInitCheckBox.selected)

    private var checkValid: Runnable? = null

    override fun getSettings(): SuiProjectConfig {
        return SuiProjectConfig(
            panelData = suiSettingsPanel.panelData,
//            aptosInitEnabled = aptosInitCheckBox.isSelected,
//            initData = aptosSettingsPanel.data
        )
    }

    override fun getComponent(myLocationField: TextFieldWithBrowseButton, checkValid: Runnable): JComponent {
        this.checkValid = checkValid
        return super.getComponent(myLocationField, checkValid)
    }

    override fun getComponent(): JComponent {
        val panel = panel {
//            group {}
            suiSettingsPanel.attachTo(this)
        }
        val defaultSuiPath = defaultProjectSettings()?.state?.suiPath
        suiSettingsPanel.suiExec = SuiExec.fromSettingsFormat(defaultSuiPath)
        return panel
    }

    override fun validate(): ValidationInfo? {
        val suiPath = this.suiSettingsPanel.suiExec.pathOrNull()
        if (suiPath == null || !suiPath.isValidExecutable()) {
            return ValidationInfo("Invalid path to Sui executable")
        }

//        if (aptosInitEnabled()) {
//            return aptosSettingsPanel.validate()
//        }
        return null
    }

//    private fun aptosInitEnabled(): Boolean {
//        return aptosInitCheckBox.isSelected
//    }
}
