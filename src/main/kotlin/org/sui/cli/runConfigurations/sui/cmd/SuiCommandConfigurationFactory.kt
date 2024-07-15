package org.sui.cli.runConfigurations.aptos.cmd

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import org.sui.cli.runConfigurations.sui.cmd.SuiCommandConfiguration

class SuiCommandConfigurationFactory(
    configurationType: ConfigurationType
) :
    ConfigurationFactory(configurationType) {

    override fun getId(): String = "AnyCommand"

    override fun getName(): String = "any command"

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return SuiCommandConfiguration(project, this)
    }

//    companion object {
//        fun createTemplateRunConfiguration(
//            project: Project,
//            configurationName: String,
//            save: Boolean
//        ): RunnerAndConfigurationSettings {
//            val runManager = RunManagerEx.getInstanceEx(project)
//            val runConfiguration = runManager.createConfiguration(
//                configurationName,
//                AptosCommandConfigurationFactory(AptosTransactionConfigurationType.getInstance())
//            )
//            if (save) {
//                runManager.setTemporaryConfiguration(runConfiguration)
//            }
//            return runConfiguration
//        }
//    }
}
