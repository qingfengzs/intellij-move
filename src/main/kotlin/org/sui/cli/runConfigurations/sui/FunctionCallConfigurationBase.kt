package org.sui.cli.runConfigurations.sui

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import org.sui.cli.MoveProject
import org.sui.cli.moveProjects

abstract class FunctionCallConfigurationBase(
    project: Project,
    factory: ConfigurationFactory,
    val configurationHandler: CommandConfigurationHandler,
) : CommandConfigurationBase(project, factory) {

    var moveProject: MoveProject?
        get() = workingDirectory?.let { project.moveProjects.findMoveProject(it) }
        set(value) {
            workingDirectory = value?.contentRootPath
        }

    fun firstRunShouldOpenEditor(): Boolean {
        val moveProject = moveProject ?: return true
        val functionCall = configurationHandler
            .parseCommand(moveProject, command).unwrapOrNull()?.second ?: return true
        return functionCall.parametersRequired()
    }
}