package org.move.ide.actions

import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import org.move.cli.settings.suiExec
import org.move.ide.dialog.AddressDialog
import org.move.ide.dialog.EnvDialog
import java.util.regex.Pattern

class OpenSwitchEnvsDialogAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return

        val onProcessComplete: (ProcessOutput?) -> Unit = { output ->
            if (output != null && output.exitCode == 0) {
                println("Process executed successfully with output: ${output.stdout}")
                val envs = output.stdout
                val regex = """(?<=^|\n)[^\n]+?""".toRegex()
                val envList = regex.findAll(envs).map { it.value }.toSet().toList()
                ApplicationManager.getApplication().invokeLater {
                    EnvDialog(envList).show()
                }
            } else {
                println("Process failed with error: ${output?.stderr}")
            }
        }
        project.suiExec.toExecutor()?.simpleCommand(project, "client", listOf("envs"), onProcessComplete)
    }

}
