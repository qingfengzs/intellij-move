package org.move.cli

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.PlatformProjectOpenProcessor
import com.intellij.projectImport.ProjectOpenProcessor
import org.move.cli.runConfigurations.addDefaultBuildRunConfiguration
import org.move.cli.runConfigurations.sui.SuiCliExecutor
import org.move.cli.settings.MoveProjectSettingsService
import org.move.cli.settings.moveSettings
import org.move.ide.MoveIcons
import org.move.ide.newProject.openFile
import org.move.ide.notifications.updateAllNotifications
import org.move.openapiext.contentRoots
import org.move.openapiext.suiBuildRunConfigurations
import org.move.openapiext.suiRunConfigurations
import javax.swing.Icon

class SuiProjectOpenProcessor : ProjectOpenProcessor() {
    override val name: String get() = "Move"
    override val icon: Icon get() = MoveIcons.MOVE_LOGO

    override fun canOpenProject(file: VirtualFile): Boolean =
        FileUtil.namesEqual(file.name, Consts.MANIFEST_FILE)
                || (file.isDirectory && file.findChild(Consts.MANIFEST_FILE) != null)

    override fun doOpenProject(
        virtualFile: VirtualFile,
        projectToClose: Project?,
        forceOpenInNewFrame: Boolean,
    ): Project? {
        val platformOpenProcessor = PlatformProjectOpenProcessor.getInstance()
        return platformOpenProcessor.doOpenProject(
            virtualFile,
            projectToClose,
            forceOpenInNewFrame
        )?.also { it ->
            StartupManager.getInstance(it).runAfterOpened {
                // create default build configuration if it doesn't exist
                if (it.suiBuildRunConfigurations().isEmpty()) {
                    val isEmpty = it.suiRunConfigurations().isEmpty()
                    it.addDefaultBuildRunConfiguration(isSelected = isEmpty)
                }
                val defaultProjectSettings = ProjectManager.getInstance().defaultMoveSettings
                it.moveSettings.modify {
                    val suiPath = defaultProjectSettings?.state?.suiPath ?: ""
                    it.suiPath = if (suiPath == "") {
                        SuiCliExecutor.suggestPath().toString()
                    } else suiPath
                }

                // opens Move.toml file
                val packageRoot = it.contentRoots.firstOrNull()
                if (packageRoot != null) {
                    val manifest = packageRoot.findChild(Consts.MANIFEST_FILE)
                    if (manifest != null) {
                        it.openFile(manifest)
                    }
                    updateAllNotifications(it)
                }
                it.moveProjects.refreshAllProjects()

            }
        }
    }
}

fun defaultProjectSettings(): MoveProjectSettingsService? = ProjectManager.getInstance().defaultMoveSettings

val ProjectManager.defaultMoveSettings: MoveProjectSettingsService?
    get() = this.defaultProject.getService(MoveProjectSettingsService::class.java)
