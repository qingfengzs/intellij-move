package org.sui.ide.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import org.sui.cli.sdks.DownloadSuiSdkDialog
import org.sui.cli.sdks.DownloadSuiSdkTask
import org.sui.cli.sdks.SuiSdk
import org.sui.cli.sdks.sdksService
import java.io.File

@Suppress("DialogTitleCapitalization")
class DownloadSuiSDKAction : DumbAwareAction("Download pre-compiled binary from GitHub") {

    var onFinish: (SuiSdk) -> Unit = { _ -> }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val sdkParametersDialog = DownloadSuiSdkDialog(e.project)
        val isOk = sdkParametersDialog.showAndGet()
        if (isOk) {
            // download Sui SDK
            val sdkVersion = sdkParametersDialog.versionField.text

            // kotlin 判断目录是否存在，不存在则发起通知，并直接返回


//            val sdksDir = sdksService().sdksDir ?: return
            val project: Project = e.project ?: return
            val sdksDir = sdksService().sdksDir // 获取 SDKs 目录

            if (sdksDir == null || !File(sdksDir).exists()) {
                // 如果目录不存在，则发起通知
                val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Sui Move Language")
                notificationGroup.createNotification(
                    "Sui SDK Download",
                    "The SDKs directory does not exist.",
                    NotificationType.ERROR
                ).notify(project)
                return // 直接返回，不继续执行下载操作
            }
            val archive = SuiSdk(sdksDir, sdkVersion)
            ProgressManager.getInstance()
                .run(DownloadSuiSdkTask(archive, onFinish))
        }
    }

    companion object {
        fun create(onFinish: (SuiSdk) -> Unit): DownloadSuiSDKAction {
            val action = DownloadSuiSDKAction()
            action.onFinish = onFinish
            return action
        }
    }
}
