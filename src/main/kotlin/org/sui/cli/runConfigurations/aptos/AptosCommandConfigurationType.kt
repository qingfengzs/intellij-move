package org.sui.cli.runConfigurations.aptos

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.NotNullLazyValue
import org.sui.cli.runConfigurations.aptos.cmd.AptosCommandConfigurationFactory
import org.sui.ide.MoveIcons

class AptosCommandConfigurationType:
    ConfigurationTypeBase(
        "AptosCommandConfiguration",
        "Aptos",
        "Aptos command execution",
        NotNullLazyValue.createConstantValue(MoveIcons.APTOS_LOGO)
    ) {

        init {
            addFactory(AptosCommandConfigurationFactory(this))
        }

    val factory: ConfigurationFactory get() = configurationFactories.single()

    @Suppress("CompanionObjectInExtension")
    companion object {
        fun getInstance(): AptosCommandConfigurationType {
            return ConfigurationTypeUtil.findConfigurationType(AptosCommandConfigurationType::class.java)
        }
    }
}