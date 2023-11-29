package org.move.cli.runConfigurations.sui

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.NotNullLazyValue
import org.move.cli.runConfigurations.sui.any.AnyCommandConfigurationFactory
import org.move.cli.runConfigurations.sui.run.BuildCommandConfigurationFactory
import org.move.cli.runConfigurations.sui.run.TestCommandConfigurationFactory
import org.move.ide.MoveIcons

class SuiConfigurationType :
    ConfigurationTypeBase(
        "SuiCommandConfiguration",
        "Sui",
        "Sui command execution",
        NotNullLazyValue.createConstantValue(MoveIcons.SUI_ICON)
    ) {

    init {
        addFactory(BuildCommandConfigurationFactory(this))
        addFactory(TestCommandConfigurationFactory(this))
        addFactory(AnyCommandConfigurationFactory(this))
    }

    companion object {
        fun getInstance(): SuiConfigurationType {
            return ConfigurationTypeUtil.findConfigurationType(SuiConfigurationType::class.java)
        }
    }
}
