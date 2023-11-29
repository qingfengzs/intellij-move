package org.move.cli.runConfigurations.producers

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.psi.PsiElement
import org.move.cli.runConfigurations.sui.SuiConfigurationType
import org.move.cli.runConfigurations.sui.run.*

class BuildCommandConfigurationProducer : FunctionCallConfigurationProducerBase<BuildCommandConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        BuildCommandConfigurationFactory(SuiConfigurationType.getInstance())

    override fun configFromLocation(location: PsiElement) =
        BuildCommandConfigurationHandler().configurationFromLocation(location)
}
