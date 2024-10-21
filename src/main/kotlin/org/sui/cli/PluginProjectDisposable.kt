package org.sui.cli

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class PluginApplicationDisposable: Disposable {
    override fun dispose() {
    }

    override fun toString(): String = "SUI_PLUGIN_DISPOSABLE"
}