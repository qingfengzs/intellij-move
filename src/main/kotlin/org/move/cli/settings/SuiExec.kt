package org.move.cli.settings

import com.intellij.openapi.project.ProjectManager
import org.move.cli.defaultMoveSettings
import org.move.cli.runConfigurations.sui.SuiCliExecutor
import org.move.openapiext.PluginPathManager
import org.move.stdext.toPathOrNull
import java.nio.file.Path

sealed class SuiExec {
    abstract val execPath: String

    object Bundled: SuiExec() {
        override val execPath: String
            get() = PluginPathManager.bundledSuiCli ?: ""
    }

    data class LocalPath(override val execPath: String): SuiExec()

    fun pathOrNull() = this.execPath.toPathOrNull()

    fun toExecutor(): SuiCliExecutor? =
        execPath.toPathOrNull()?.let { SuiCliExecutor(it) }

    fun pathToSettingsFormat(): String? =
        when (this) {
            is LocalPath -> this.execPath
            is Bundled -> null
        }

    companion object {
        fun fromSettingsFormat(suiPath: String?): SuiExec =
            when (suiPath) {
                null -> Bundled
                else -> LocalPath(suiPath)
            }

        fun getVersion(suiExecPath: Path?): String? {
            return suiExecPath?.let {
                val version = SuiCliExecutor(it).version()
                val regex = Regex("sui\\s+(\\d+\\.\\d+\\.\\d+(-[a-f0-9]+)?)")
                val matchResult = version?.let { it1 -> regex.find(it1) }
                var state = ProjectManager.getInstance().defaultProject.moveSettings.state
                if (matchResult != null) {
                    // update default setting
                    val defaultMoveSettings = ProjectManager.getInstance().defaultMoveSettings
                    defaultMoveSettings?.modify {
                        it.suiPath = suiExecPath.toString()
                        it.isValidExec = true
                    }
                    state = state.also {
                        it.suiPath = suiExecPath.toString()
                        it.isValidExec = true
                    }
                    val matchResultValue = matchResult.value
                    println("match result:$matchResultValue")
                    return@let version
                } else {
                    state = state.also { it.isValidExec = false }
                    return@let ""
                }
            }
        }

    }
}
