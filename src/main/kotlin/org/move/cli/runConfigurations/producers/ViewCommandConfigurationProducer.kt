package org.move.cli.runConfigurations.producers

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.psi.PsiElement
import org.move.cli.runConfigurations.sui.SuiConfigurationType
import org.move.cli.runConfigurations.sui.view.ViewCommandConfiguration
import org.move.cli.runConfigurations.sui.view.ViewCommandConfigurationFactory
import org.move.cli.runConfigurations.sui.view.ViewCommandConfigurationHandler

class ViewCommandConfigurationProducer : FunctionCallConfigurationProducerBase<ViewCommandConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        ViewCommandConfigurationFactory(SuiConfigurationType.getInstance())

    override fun configFromLocation(location: PsiElement) =
        ViewCommandConfigurationHandler().configurationFromLocation(location)
}
