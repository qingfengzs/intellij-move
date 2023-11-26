package org.move.cli.runConfigurations.sui.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import org.move.cli.moveProjects
import org.move.cli.runConfigurations.sui.FunctionCallConfigurationBase
import org.move.cli.runConfigurations.sui.FunctionCallConfigurationEditor

class RunCommandConfiguration(
    project: Project,
    factory: ConfigurationFactory
) : FunctionCallConfigurationBase(project, factory, RunCommandConfigurationHandler()) {

    override fun getConfigurationEditor(): FunctionCallConfigurationEditor<RunCommandConfiguration> {
        val moveProject = project.moveProjects.allProjects.first()
        val editor = FunctionCallConfigurationEditor<RunCommandConfiguration>(
            RunCommandConfigurationHandler(),
            moveProject,
        )
        return editor
    }
}
