package org.sui.cli.runConfigurations.aptos.cmd

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.panel
import org.sui.openapiext.fullWidthCell
import org.sui.utils.ui.WorkingDirectoryField
import java.nio.file.Path
import javax.swing.JComponent

class AptosCommandConfigurationEditor : SettingsEditor<AptosCommandConfiguration>() {

    private val commandTextField = ExpandableTextField()
    private val envVarsField = EnvironmentVariablesComponent()
    val workingDirectoryField = WorkingDirectoryField()

    private val workingDirectory: Path? get() = workingDirectoryField.toPath()

    override fun resetEditorFrom(configuration: AptosCommandConfiguration) {
        commandTextField.text = configuration.command
        workingDirectoryField.component.text = configuration.workingDirectory?.toString().orEmpty()
        envVarsField.envData = configuration.environmentVariables
    }

    override fun applyEditorTo(configuration: AptosCommandConfiguration) {
        configuration.command = commandTextField.text
        configuration.workingDirectory = this.workingDirectory
        configuration.environmentVariables = envVarsField.envData
    }

    override fun createEditor(): JComponent {
        return panel {
            row("&Command:") {
                fullWidthCell(commandTextField)
//                    .columns(COLUMNS_LARGE)
                    .resizableColumn()
            }
            row(envVarsField.label) {
                fullWidthCell(envVarsField)
            }
            row(workingDirectoryField.label) {
                fullWidthCell(workingDirectoryField)
                    .resizableColumn()
            }
        }
    }
}
