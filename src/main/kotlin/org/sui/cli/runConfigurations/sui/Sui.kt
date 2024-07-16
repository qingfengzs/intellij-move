package org.sui.cli.runConfigurations.sui

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import org.sui.cli.Consts
import org.sui.cli.MoveProject
import org.sui.cli.externalLinter.ExternalLinter
import org.sui.cli.externalLinter.externalLinterSettings
import org.sui.cli.runConfigurations.SuiCommandLine
import org.sui.cli.settings.moveSettings
import org.sui.openapiext.*
import org.sui.openapiext.common.isUnitTestMode
import org.sui.stdext.RsResult
import org.sui.stdext.RsResult.Err
import org.sui.stdext.RsResult.Ok
import org.sui.stdext.buildList
import org.sui.stdext.unwrapOrElse
import java.nio.file.Path
import java.nio.file.Paths

data class Sui(val cliLocation: Path, val parentDisposable: Disposable?) : Disposable {

    init {
        if (parentDisposable != null) {
            Disposer.register(parentDisposable, this)
        }
    }

    fun init(
        project: Project,
        rootDirectory: VirtualFile,
        packageName: String
    ): RsProcessResult<VirtualFile> {
        if (!isUnitTestMode) {
            checkIsBackgroundThread()
        }
        val commandLine = SuiCommandLine(
            "move new",
            listOf(
                "--name", packageName,
//                "--assume-yes"
            ),
            workingDirectory = project.rootPath
        )
        executeCommandLine(commandLine).unwrapOrElse { return Err(it) }

        fullyRefreshDirectory(rootDirectory)

        val manifest =
            checkNotNull(rootDirectory.findChild(Consts.MANIFEST_FILE)) { "Can't find the manifest file" }
        return Ok(manifest)
    }

    fun fetchPackageDependencies(
        project: Project,
        projectDir: Path,
        skipLatest: Boolean,
        processListener: ProcessListener
    ): RsProcessResult<Unit> {
        if (project.moveSettings.fetchSuiDeps) {
            val commandLine =
                SuiCommandLine(
                    subCommand = "move build",
                    arguments = listOfNotNull(
                        "--skip-fetch-latest-git-deps".takeIf { skipLatest }
                    ),
                    workingDirectory = projectDir
                )
            // TODO: as Sui does not yet support fetching dependencies without compiling, ignore errors here,
            // TODO: still better than no call at all
            executeCommandLine(commandLine, listener = processListener)
//                .unwrapOrElse { return Err(it) }
        }
        return Ok(Unit)
    }

    fun checkProject(args: SuiCompileArgs): RsResult<ProcessOutput, RsProcessExecutionException.Start> {
//            val useClippy = args.linter == ExternalLinter.CLIPPY
//                    && !checkNeedInstallClippy(project, args.cargoProjectDirectory)
//            val checkCommand = if (useClippy) "clippy" else "check"
        val extraArguments = ParametersListUtil.parse(args.extraArguments)
        val commandLine =
            SuiCommandLine(
                "move build",
                buildList {
//                        add("--message-format=json")
                    if ("--skip-fetch-latest-git-deps" !in extraArguments) {
                        add("--skip-fetch-latest-git-deps")
                    }
//                    if (args.addCompilerV2Flags) {
//                        if ("--compiler-version" !in extraArguments) {
//                            add("--compiler-version")
//                            add("v2")
//                        }
//                        if ("--language-version" !in extraArguments) {
//                            add("--language-version")
//                            add("2.0")
//                        }
//                    }
                    addAll(ParametersListUtil.parse(args.extraArguments))
                },
                args.moveProjectDirectory,
                environmentVariables = args.envs.let {
                    val environmentMap = it.toMutableMap()
                    if (args.addCompilerV2Flags && Consts.MOVE_COMPILER_V2_ENV !in environmentMap) {
                        environmentMap[Consts.MOVE_COMPILER_V2_ENV] = "true"
                    }
                    EnvironmentVariablesData.create(environmentMap, true)
                }
            )
        return executeCommandLine(commandLine).ignoreExitCode()
    }

    fun downloadPackage(
        project: Project,
        accountAddress: String,
        packageName: String,
        outputDir: String,
        profile: String? = null,
        networkUrl: String? = null,
        connectionTimeoutSecs: Int = -1,
        nodeApiKey: String? = null,
        runner: CapturingProcessHandler.() -> ProcessOutput = {
            runProcessWithGlobalProgress(
                timeoutInMilliseconds = null
            )
        }
    ): RsProcessResult<ProcessOutput> {
        val commandLine = SuiCommandLine(
            subCommand = "move download",
            arguments = buildList {
                add("--account"); add(accountAddress)
                add("--package"); add(packageName)
                add("--bytecode")
                add("--output-dir"); add(outputDir)
                if (!profile.isNullOrBlank()) {
                    add("--profile"); add(profile)
                }
                if (!networkUrl.isNullOrBlank()) {
                    add("--url"); add(networkUrl)
                }
                if (!nodeApiKey.isNullOrBlank()) {
                    add("--node-api-key"); add(nodeApiKey)
                }
                if (connectionTimeoutSecs != -1) {
                    add("--connection-timeout-secs"); add(connectionTimeoutSecs.toString())
                }
            },
            workingDirectory = project.basePath?.let { Path.of(it) },
            environmentVariables = EnvironmentVariablesData.DEFAULT
        )
        return executeCommandLine(commandLine, runner = runner)
    }

    fun decompileDownloadedPackage(downloadedPackagePath: Path): RsProcessResult<ProcessOutput> {
        val bytecodeModulesPath =
            downloadedPackagePath.resolve("bytecode_modules").toAbsolutePath().toString()
        val commandLine = SuiCommandLine(
            subCommand = "move decompile",
            arguments = buildList {
                add("--package-path"); add(bytecodeModulesPath)
                add("--assume-yes")
            },
            workingDirectory = downloadedPackagePath
        )
        return executeCommandLine(commandLine)
    }

    fun decompileFile(
        bytecodeFilePath: String,
        outputDir: String?,
    ): RsProcessResult<ProcessOutput> {
        val fileRoot = Paths.get(bytecodeFilePath).parent
        val commandLine = SuiCommandLine(
            subCommand = "move decompile",
            arguments = buildList {
                add("--bytecode-path"); add(bytecodeFilePath)
                if (outputDir != null) {
                    add("--output-dir"); add(outputDir)
                }
                add("--assume-yes")
            },
            workingDirectory = fileRoot
        )
        // only one second is allowed to run decompiler, otherwise fails with timeout
        return executeCommandLine(commandLine)
    }

    private fun executeCommandLine(
        commandLine: SuiCommandLine,
        listener: ProcessListener? = null,
        runner: CapturingProcessHandler.() -> ProcessOutput = {
            runProcessWithGlobalProgress(
                timeoutInMilliseconds = null
            )
        }
    ): RsProcessResult<ProcessOutput> {
        return commandLine
            .toGeneralCommandLine(this.cliLocation)
            .execute(this, stdIn = null, listener = listener, runner = runner)
    }

    override fun dispose() {}
}

data class SuiCompileArgs(
    val linter: ExternalLinter,
    val moveProjectDirectory: Path,
    val extraArguments: String,
    val envs: Map<String, String>,
    val addCompilerV2Flags: Boolean,
    val skipLatestGitDeps: Boolean,
) {
    companion object {
        fun forMoveProject(moveProject: MoveProject): SuiCompileArgs {
            val linterSettings = moveProject.project.externalLinterSettings
            val moveSettings = moveProject.project.moveSettings

            val additionalArguments = linterSettings.additionalArguments
            val enviroment = linterSettings.envs
            val workingDirectory = moveProject.workingDirectory

            return SuiCompileArgs(
                linterSettings.tool,
                workingDirectory,
                additionalArguments,
                enviroment,
                addCompilerV2Flags = moveSettings.addCompilerV2CLIFlags,
                skipLatestGitDeps = moveSettings.skipFetchLatestGitDeps
            )
        }
    }
}

val MoveProject.workingDirectory: Path get() = this.currentPackage.contentRoot.pathAsPath