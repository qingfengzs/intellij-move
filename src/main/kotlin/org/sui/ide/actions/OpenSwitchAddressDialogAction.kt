package org.sui.ide.actions

import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import org.sui.cli.settings.suiExec
import org.sui.ide.dialog.AddressDialog

class OpenSwitchAddressDialogAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return

        val onProcessComplete: (ProcessOutput?) -> Unit = { output ->
            if (output != null && output.exitCode == 0) {
                println("Process executed successfully with output: ${output.stdout}")
                val addresses = output.stdout
                val addressPattern = "0x[a-fA-F0-9]{64}".toRegex()
                val addressList = addressPattern.findAll(addresses).map { it.value }.toSet().toList()
                ApplicationManager.getApplication().invokeLater {
                    AddressDialog(addressList).show()
                }
            } else {
                println("Process failed with error: ${output?.stderr}")
            }
        }
        project.suiExec.toExecutor()?.simpleCommand(project, "client", listOf("addresses"), onProcessComplete)
    }

}
