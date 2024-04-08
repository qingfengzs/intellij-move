package org.sui.cli.settings.sui

import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Disposer
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.bindText
import org.sui.cli.settings.MoveProjectSettingsService
import org.sui.cli.settings.VersionLabel
import org.sui.openapiext.pathField
import org.sui.stdext.toPathOrNull

class ChooseSuiCliPanel(
    private val versionUpdateListener: (() -> Unit)? = null
) : Disposable {

    private val localPathField =
        pathField(
            FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
            this,
            "Choose Sui CLI",
            onTextChanged = { text ->
                _suiCliPath = text
                _suiCliPath.toPathOrNull()?.let { versionLabel.updateAndNotifyListeners(it) }
            })
    private val versionLabel = VersionLabel(this, versionUpdateListener)

    private lateinit var _suiCliPath: String

    fun getSuiCliPath(): String {
        return _suiCliPath
    }

    fun setSuiCliPath(path: String) {
        this._suiCliPath = path
        localPathField.text = path
        path.toPathOrNull()?.let { versionLabel.updateAndNotifyListeners(it) }
    }

    fun attachToLayout(layout: Panel): Row {
        val panel = this
        if (!panel::_suiCliPath.isInitialized) {
            val defaultProjectSettings =
                ProjectManager.getInstance().defaultProject.getService(MoveProjectSettingsService::class.java)
            panel._suiCliPath = defaultProjectSettings.state.suiPath
        }
        val resultRow = with(layout) {
            group("Sui CLI") {
                row {
                    cell(localPathField)
                        .bindText(
                            { _suiCliPath },
                            { _suiCliPath = it }
                        )
                        .onChanged {
                            localPathField.text.toPathOrNull()?.let { versionLabel.updateAndNotifyListeners(it) }
                        }
                        .align(AlignX.FILL).resizableColumn()
                }
                row("--version :") { cell(versionLabel) }
            }
        }
        _suiCliPath.toPathOrNull()?.let { versionLabel.updateAndNotifyListeners(it) }
        return resultRow
    }

    override fun dispose() {
        Disposer.dispose(localPathField)
    }
}
