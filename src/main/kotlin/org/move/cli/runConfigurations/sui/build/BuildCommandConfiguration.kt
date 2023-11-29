package org.move.cli.runConfigurations.sui.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import org.move.cli.moveProjects
import org.move.cli.runConfigurations.sui.FunctionCallConfigurationBase
import org.move.cli.runConfigurations.sui.FunctionCallConfigurationEditor

class BuildCommandConfiguration(
    project: Project,
    factory: ConfigurationFactory
) : FunctionCallConfigurationBase(project, factory, BuildCommandConfigurationHandler()) {

    override fun getConfigurationEditor(): FunctionCallConfigurationEditor<BuildCommandConfiguration> {
        val moveProject = project.moveProjects.allProjects.first()
        return FunctionCallConfigurationEditor(
            BuildCommandConfigurationHandler(),
            moveProject,
        )
    }
}
