package org.sui.cli.runConfigurations.suimove.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import org.sui.cli.moveProjectsService
import org.sui.cli.runConfigurations.suimove.FunctionCallConfigurationBase
import org.sui.cli.runConfigurations.suimove.FunctionCallConfigurationEditor

class BuildCommandConfiguration(
    project: Project,
    factory: ConfigurationFactory
) : FunctionCallConfigurationBase(project, factory, BuildCommandConfigurationHandler()) {

    override fun getConfigurationEditor(): FunctionCallConfigurationEditor<BuildCommandConfiguration> {
        val moveProject = project.moveProjectsService.allProjects.first()
        return FunctionCallConfigurationEditor(
            BuildCommandConfigurationHandler(),
            moveProject,
        )
    }
}
