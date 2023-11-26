package org.move.cli.runConfigurations.sui

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.NotNullLazyValue
import org.move.cli.runConfigurations.sui.any.AnyCommandConfigurationFactory
import org.move.cli.runConfigurations.sui.run.RunCommandConfigurationFactory
import org.move.cli.runConfigurations.sui.view.ViewCommandConfigurationFactory
import org.move.ide.MoveIcons

class SuiConfigurationType :
    ConfigurationTypeBase(
        "SuiCommandConfiguration",
        "Sui",
        "Sui command execution",
        NotNullLazyValue.createConstantValue(MoveIcons.SUI_ICON)
    ) {

    init {
        addFactory(RunCommandConfigurationFactory(this))
        addFactory(ViewCommandConfigurationFactory(this))
        addFactory(AnyCommandConfigurationFactory(this))
    }

    companion object {
        fun getInstance(): SuiConfigurationType {
            return ConfigurationTypeUtil.findConfigurationType(SuiConfigurationType::class.java)
        }
    }
}
