package org.move.cli.runConfigurations.producers

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.psi.PsiElement
import org.move.cli.runConfigurations.sui.SuiConfigurationType
import org.move.cli.runConfigurations.sui.run.*

class TestCommandConfigurationProducer : FunctionCallConfigurationProducerBase<TestCommandConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        TestCommandConfigurationFactory(SuiConfigurationType.getInstance())

    override fun configFromLocation(location: PsiElement) =
        TestCommandConfigurationHandler().configurationFromLocation(location)
}
