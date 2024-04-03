package org.sui.cli

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.ProjectActivity
import org.sui.cli.runConfigurations.addDefaultBuildRunConfiguration
import org.sui.cli.settings.moveSettings
import org.sui.ide.newProject.openFile
import org.sui.ide.notifications.updateAllNotifications
import org.sui.openapiext.contentRoots
import org.sui.openapiext.suiBuildRunConfigurations
import org.sui.openapiext.suiRunConfigurations

class SuiProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        // add default build configuration
        if (project.suiBuildRunConfigurations().isEmpty()) {
            val isEmpty = project.suiRunConfigurations().isEmpty()
            project.addDefaultBuildRunConfiguration(isSelected = isEmpty)
        }
        // set default setting cli path
        val defaultProjectSettings = ProjectManager.getInstance().defaultMoveSettings
        project.moveSettings.modify {
            val suiPath = defaultProjectSettings?.state?.suiPath ?: ""
            it.suiPath = suiPath
        }
        // opens Move.toml file
        val packageRoot = project.contentRoots.firstOrNull()
        if (packageRoot != null) {
            val manifest = packageRoot.findChild(/* name = */ Consts.MANIFEST_FILE)
            if (manifest != null) {
                project.openFile(manifest)
            }
            updateAllNotifications(project)
        }
        // refresh all projects
        project.moveProjectsService.refreshAllProjects()
    }
}