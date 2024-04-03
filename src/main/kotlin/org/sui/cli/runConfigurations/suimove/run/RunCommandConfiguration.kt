package org.sui.cli.runConfigurations.suimove.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import org.sui.cli.moveProjectsService
import org.sui.cli.runConfigurations.suimove.FunctionCallConfigurationBase
import org.sui.cli.runConfigurations.suimove.FunctionCallConfigurationEditor

class RunCommandConfiguration(
    project: Project,
    factory: ConfigurationFactory
) : FunctionCallConfigurationBase(project, factory, TestCommandConfigurationHandler()) {
    override fun getConfigurationEditor(): FunctionCallConfigurationEditor<TestCommandConfiguration> {
        val moveProject = project.moveProjectsService.allProjects.first()
        val editor = FunctionCallConfigurationEditor<TestCommandConfiguration>(
            TestCommandConfigurationHandler(),
            moveProject,
        )
        return editor
    }
}
