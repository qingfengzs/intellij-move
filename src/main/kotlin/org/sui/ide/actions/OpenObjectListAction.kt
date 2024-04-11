package org.sui.ide.actions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import org.sui.ide.dialog.ObjectDialog

class OpenObjectListAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val commandLine = GeneralCommandLine("sui", "client", "objects", "--json")
        val processOutput: ProcessOutput = ExecUtil.execAndGetOutput(commandLine)

        val addressesJson = processOutput.stdout + processOutput.stderr
        val extractData = extractData(addressesJson)
        ApplicationManager.getApplication().invokeLater {
            ObjectDialog(extractData).show()
        }
    }

    data class SuiObject(
        val objectId: String,
        val version: String,
        val digest: String,
        val type: String
    )

    fun extractData(s: String): List<SuiObject> {
        val gson = Gson()
        val type = object : TypeToken<List<Map<String, SuiObject>>>() {}.type
        val jsonList: List<Map<String, SuiObject>> = gson.fromJson(s, type)

        return jsonList.mapNotNull { it["data"] }
    }
}

