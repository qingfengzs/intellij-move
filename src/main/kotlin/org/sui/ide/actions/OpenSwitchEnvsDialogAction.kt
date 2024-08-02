package org.sui.ide.actions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import org.sui.cli.runConfigurations.SuiCommandLine
import org.sui.cli.settings.suiExecPath
import org.sui.ide.dialog.EnvDialog
import org.sui.utils.StringUtils

class OpenSwitchEnvsDialogAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return

        val task = Runnable {
            val suiCommandLine = SuiCommandLine("client envs", listOf("--json"))
            if (project.suiExecPath == null) return@Runnable

            val commandLine = suiCommandLine.toGeneralCommandLine(project.suiExecPath!!)
            val processOutput: ProcessOutput = ExecUtil.execAndGetOutput(commandLine)
            val outputJson = processOutput.stdout + processOutput.stderr

            val cleanJsonString = StringUtils.cleanJsonListString(outputJson)

            val gson = Gson()
            val type = object : TypeToken<List<Any>>() {}.type
            val data = gson.fromJson<List<Any>>(cleanJsonString, type)
            val networks = gson.fromJson<List<NetEnv>>(gson.toJson(data[0]), object : TypeToken<List<NetEnv>>() {}.type)
            val activeNetwork = data[1] as String
            val networkList = Envs(activeNetwork, networks)
            ApplicationManager.getApplication().invokeLater {
                EnvDialog(networkList, project).show()
            }
        }
        ProgressManager.getInstance().runProcessWithProgressSynchronously(task, "Processing", true, project)
    }

    data class Envs(
        val activeNetwork: String,
        val networks: List<NetEnv>
    )

    data class NetEnv(
        val alias: String,
        val rpc: String,
        val ws: String?
    )
}