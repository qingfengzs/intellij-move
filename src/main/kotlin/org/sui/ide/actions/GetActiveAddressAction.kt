package org.sui.ide.actions

import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import org.sui.cli.runConfigurations.SuiCommandLine
import org.sui.cli.settings.suiExecPath
import org.sui.ide.notifications.MvNotifications

@Suppress("DEPRECATION")
class GetActiveAddressAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return

        ApplicationManager.getApplication().executeOnPooledThread {
            val suiCommandLine = SuiCommandLine("client active-address");
            if (project.suiExecPath == null) return@executeOnPooledThread

            val commandLine = suiCommandLine.toGeneralCommandLine(project.suiExecPath!!)
            val processOutput: ProcessOutput = ExecUtil.execAndGetOutput(commandLine)

            val exitCode = processOutput.exitCode
            val output = processOutput.stdout + processOutput.stderr
            val notificationType = if (exitCode == 0) NotificationType.INFORMATION else NotificationType.ERROR
            MvNotifications.pluginNotifications().createNotification(
                "Active Address",
                output, notificationType
            ).notify(project)
        }
    }
}