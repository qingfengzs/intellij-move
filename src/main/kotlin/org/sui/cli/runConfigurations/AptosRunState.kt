package org.sui.cli.runConfigurations

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.filters.Filter
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import org.sui.cli.MoveFileHyperlinkFilter

abstract class AptosRunStateBase(
    environment: ExecutionEnvironment,
    val runConfiguration: CommandConfigurationBase,
    val config: CommandConfigurationBase.CleanConfiguration.Ok
) : CommandLineState(environment) {

    val project = environment.project
    val commandLine: AptosCommandLine = AptosCommandLine(null)

    override fun startProcess(): ProcessHandler {
//        val generalCommandLine = commandLine.toGeneralCommandLine(config.aptosPath)
//        val handler = KillableColoredProcessHandler(generalCommandLine)
//        consoleBuilder.console.attachToProcess(handler)
//        ProcessTerminatedListener.attach(handler)  // shows exit code upon termination
//        return handler
    }

    protected fun createFilters(): Collection<Filter> {
        val filters = mutableListOf<Filter>()
        val wd = commandLine.workingDirectory
        if (wd != null) {
            filters.add(MoveFileHyperlinkFilter(project, wd))
        }
        return filters
    }
}

class AptosRunState(
    environment: ExecutionEnvironment,
    runConfiguration: CommandConfigurationBase,
    config: CommandConfigurationBase.CleanConfiguration.Ok
) :
    AptosRunStateBase(environment, runConfiguration, config) {

    init {
        createFilters().forEach { consoleBuilder.addFilter(it) }
    }

}
