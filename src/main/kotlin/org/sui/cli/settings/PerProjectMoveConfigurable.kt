package org.sui.cli.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.EditorNotifications
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import org.sui.cli.defaultMoveSettings

class PerProjectMoveConfigurable(val project: Project) : BoundConfigurable("Sui Move Language"),
                                                         SearchableConfigurable {

    override fun getId(): String = "org.sui.lang.settings"

    private val settingsState: MoveProjectSettingsService.State = project.moveSettings.state

    private val suiSettingsPanel = SuiSettingsPanel(showDefaultProjectSettingsLink = !project.isDefault)

    override fun createPanel(): DialogPanel {
        return panel {
            suiSettingsPanel.attachTo(this)
            group {
                row {
                    checkBox("Auto-fold specs in opened files")
                        .bindSelected(settingsState::foldSpecs)
                }
//                row {
//                    checkBox("Disable telemetry for new Run Configurations")
//                        .bindSelected(settingsState::disableTelemetry)
//                }
//                row {
//                    checkBox("Enable debug mode")
//                        .bindSelected(settingsState::debugMode)
//                    comment(
//                        "Enables some explicit crashes in the plugin code. Useful for the error reporting."
//                    )
//                }
//                row {
//                    checkBox("Skip fetching latest git dependencies for tests")
//                        .bindSelected(settingsState::skipFetchLatestGitDeps)
//                    comment(
//                        "Adds --skip-fetch-latest-git-deps to the test runs."
//                    )
//                }
            }
        }
    }

    override fun disposeUIResources() {
        super<BoundConfigurable>.disposeUIResources()
        Disposer.dispose(suiSettingsPanel)
    }

    override fun reset() {
        super<BoundConfigurable>.reset()
        suiSettingsPanel.panelData =
            SuiSettingsPanel.PanelData(SuiExec.fromSettingsFormat(settingsState.suiPath))
    }

    override fun isModified(): Boolean {
        if (super<BoundConfigurable>.isModified()) return true
        val panelData = suiSettingsPanel.panelData
        return panelData.suiExec.pathToSettingsFormat() != settingsState.suiPath
    }

    /**
     * This method is called when user clicks "Apply" button in settings dialog.
     * It is also called on "OK" button click, but only if [isModified] returns true.
     */
    override fun apply() {
        super.apply()
        if (isModified()) {
            val newSettingsState = settingsState.copy()
            // update project settings
            newSettingsState.suiPath = suiSettingsPanel.panelData.suiExec.pathToSettingsFormat()
            newSettingsState.isValidExec = suiSettingsPanel.getVersionLable() != VersionLabel.INVALID_VERSION
            project.moveSettings.updateStateWithoutNotification(newSettingsState)
            // update default project settings
            val defaultSetting = ProjectManager.getInstance().defaultMoveSettings
            defaultSetting?.updateStateWithoutNotification(newSettingsState)
            // update notifications
            EditorNotifications.getInstance(project).updateAllNotifications()
        }
    }
}
