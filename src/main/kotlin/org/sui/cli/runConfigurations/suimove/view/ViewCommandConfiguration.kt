package org.sui.cli.runConfigurations.suimove.view

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import org.sui.cli.moveProjectsService
import org.sui.cli.runConfigurations.suimove.FunctionCallConfigurationBase
import org.sui.cli.runConfigurations.suimove.FunctionCallConfigurationEditor

class ViewCommandConfiguration(
    project: Project,
    factory: ConfigurationFactory
) : FunctionCallConfigurationBase(project, factory, ViewCommandConfigurationHandler()) {
    override fun getConfigurationEditor(): FunctionCallConfigurationEditor<ViewCommandConfiguration> {
        val moveProject = project.moveProjectsService.allProjects.first()
        val editor = FunctionCallConfigurationEditor<ViewCommandConfiguration>(
            ViewCommandConfigurationHandler(),
            moveProject,
        )
        return editor
    }
}
