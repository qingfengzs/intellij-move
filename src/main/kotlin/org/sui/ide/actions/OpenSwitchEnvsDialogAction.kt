package org.sui.ide.actions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import org.sui.cli.settings.suiExec
import org.sui.ide.dialog.EnvDialog

class OpenSwitchEnvsDialogAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return

        val onProcessComplete: (ProcessOutput?) -> Unit = { output ->
            if (output != null && output.exitCode == 0) {
                println("Process executed successfully with output: ${output.stdout}")
                val envJSON = output.stdout
                val gson = Gson()
                val type = object : TypeToken<List<Any>>() {}.type
                val parsedList: List<Any> = gson.fromJson(envJSON, type)
                val netList =
                    gson.fromJson<List<NetEnv>>(gson.toJson(parsedList[0]), object : TypeToken<List<NetEnv>>() {}.type)
                val active = parsedList[1] as String

                val envs = Envs(active, netList)

                ApplicationManager.getApplication().invokeLater {
                    EnvDialog(envs).show()
                }
            } else {
                println("Process failed with error: ${output?.stderr}")
            }
        }
        project.suiExec.toExecutor()?.simpleCommand(project, "client", listOf("envs", "--json"), onProcessComplete)
    }

    data class Envs(
        val active: String,
        val netList: List<NetEnv>
    )

    data class NetEnv(
        val alias: String,
        val rpc: String,
        val ws: String?
    )
}
