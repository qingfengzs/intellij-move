package org.move.cli.settings

import org.move.cli.runConfigurations.aptos.AptosCliExecutor
import org.move.cli.runConfigurations.sui.SuiCliExecutor
import org.move.openapiext.PluginPathManager
import org.move.stdext.toPathOrNull

sealed class SuiExec {
    abstract val execPath: String

    object Bundled: SuiExec() {
        override val execPath: String
            get() = PluginPathManager.bundledAptosCli ?: ""
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
    }
}
