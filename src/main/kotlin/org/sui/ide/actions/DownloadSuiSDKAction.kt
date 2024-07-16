package org.sui.ide.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAwareAction
import org.sui.cli.sdks.DownloadSuiSdkDialog
import org.sui.cli.sdks.DownloadSuiSdkTask
import org.sui.cli.sdks.SuiSdk
import org.sui.cli.sdks.sdksService

@Suppress("DialogTitleCapitalization")
class DownloadSuiSDKAction : DumbAwareAction("Download pre-compiled binary from GitHub") {

    var onFinish: (SuiSdk) -> Unit = { _ -> }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val sdkParametersDialog = DownloadSuiSdkDialog(e.project)
        val isOk = sdkParametersDialog.showAndGet()
        if (isOk) {
            // download Aptos SDK
            val sdkVersion = sdkParametersDialog.versionField.text

            // todo: show balloon error if no sdks dir set
            val sdksDir = sdksService().sdksDir ?: return

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
