package org.move.cli.runConfigurations.sui.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import org.move.cli.moveProjects
import org.move.cli.runConfigurations.sui.FunctionCallConfigurationBase
import org.move.cli.runConfigurations.sui.FunctionCallConfigurationEditor

class TestCommandConfiguration(
    project: Project,
    factory: ConfigurationFactory
) : FunctionCallConfigurationBase(project, factory, TestCommandConfigurationHandler()) {
    override var command: String = "move test"
    override fun getConfigurationEditor(): FunctionCallConfigurationEditor<TestCommandConfiguration> {
        val moveProject = project.moveProjects.allProjects.first()
        val editor = FunctionCallConfigurationEditor<TestCommandConfiguration>(
            TestCommandConfigurationHandler(),
            moveProject,
        )
        return editor
    }
}
