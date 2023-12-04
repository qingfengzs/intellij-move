package org.sui.ide.actions

import com.intellij.execution.process.ProcessOutput
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.sui.cli.settings.suiExec
import org.sui.stdext.isExecutableFile
import org.sui.stdext.toPathOrNull

class GetActiveAddressAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return
        // check sui cli installed
        val path = project.suiExec.execPath.toPathOrNull()
        if (path == null || !path.isExecutableFile()) {
            Notifications.Bus.notify(
                Notification(
                    "Sui Move Language",
                    "No sui cli can found",
                    "Please set sui cli first.",
                    NotificationType.WARNING
                )
            )
        } else {
            val onProcessComplete: (ProcessOutput?) -> Unit = { output ->
                if (output != null && output.exitCode == 0) {
                    println("Process executed successfully with output: ${output.stdout}")
                    Notifications.Bus.notify(
                        Notification(
                            "Sui Move Language",
                            "Active address",
                            output.stdout,
                            NotificationType.INFORMATION
                        )
                    )
                } else {
                    println("Process failed with error: ${output?.stderr}")
                }
            }
            project.suiExec.toExecutor()?.simpleCommand(project, "client", listOf("active-address"), onProcessComplete)
        }
    }

}
