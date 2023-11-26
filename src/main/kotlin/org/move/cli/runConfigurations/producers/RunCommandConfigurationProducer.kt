package org.move.cli.runConfigurations.producers

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.psi.PsiElement
import org.move.cli.runConfigurations.sui.SuiConfigurationType
import org.move.cli.runConfigurations.sui.run.RunCommandConfiguration
import org.move.cli.runConfigurations.sui.run.RunCommandConfigurationFactory
import org.move.cli.runConfigurations.sui.run.RunCommandConfigurationHandler

class RunCommandConfigurationProducer : FunctionCallConfigurationProducerBase<RunCommandConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        RunCommandConfigurationFactory(SuiConfigurationType.getInstance())

    override fun configFromLocation(location: PsiElement) =
        RunCommandConfigurationHandler().configurationFromLocation(location)
}
