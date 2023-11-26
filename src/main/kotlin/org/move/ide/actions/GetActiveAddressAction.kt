package org.move.ide.actions

import com.intellij.execution.process.ProcessOutput
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.move.cli.settings.suiExec

class GetActiveAddressAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return

        val onProcessComplete: (ProcessOutput?) -> Unit = { output ->
            if (output != null && output.exitCode == 0) {
                println("Process executed successfully with output: ${output.stdout}")
                Notifications.Bus.notify(
                    Notification(
                        "Move Language",
                        "Active address",
                        "${output?.stdout}",
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
