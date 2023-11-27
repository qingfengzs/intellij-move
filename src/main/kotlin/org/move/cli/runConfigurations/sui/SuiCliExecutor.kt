package org.move.cli.runConfigurations.sui

import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.isDirectory
import org.move.cli.Consts
import org.move.cli.settings.isValidExecutable
import org.move.cli.settings.suiExec
import org.move.openapiext.*
import org.move.openapiext.common.isUnitTestMode
import org.move.stdext.RsResult
import org.move.stdext.executableName
import org.move.stdext.isExecutableFile
import org.move.stdext.unwrapOrElse
import java.nio.file.Path
import java.nio.file.Paths

class SuiCliExecutor(val location: Path) {

    fun newEnv(
        project: Project,
        owner: Disposable,
        alias: String,
        rpc:String,
    ): MvProcessResult<ProcessOutput> {
        if (!isUnitTestMode) {
            checkIsBackgroundThread()
        }
        val commandLine = SuiCommandLine(
            "client new-env ",
            arguments = listOf(
                "--alias", alias,
                "--rpc",rpc
            ),
            workingDirectory = project.root
        )
        return commandLine.toGeneralCommandLine(this).execute(owner)
    }

    fun switch(
        project: Project,
        owner: Disposable,
        network: String,
    ): MvProcessResult<ProcessOutput> {
        if (!isUnitTestMode) {
            checkIsBackgroundThread()
        }
        val commandLine = SuiCommandLine(
            "client switch",
            arguments = listOf(
                "--env", network,
            ),
            workingDirectory = project.root
        )
        return commandLine.toGeneralCommandLine(this).execute(owner)
    }

    fun simpleCommand(
        project: Project,
        subCommand:String,
        args: List<String>,
        onComplete: (ProcessOutput?) -> Unit
    ) {
        val suiCommandLine = SuiCommandLine(
            subCommand,
            arguments = args,
            workingDirectory = project.root
        )
        val commandLine = suiCommandLine.toGeneralCommandLine(this)
        commandLine.executeAsync(onComplete)

    }

    fun moveNew(
        project: Project,
        parentDisposable: Disposable,
        rootDirectory: VirtualFile,
        packageName: String,
    ): MvProcessResult<VirtualFile> {
        if (!isUnitTestMode) {
            checkIsBackgroundThread()
        }

        val commandLine = SuiCommandLine(
            "move",
            listOf(
                "new",
                packageName,
            ),
            workingDirectory = project.root
        )
        commandLine.toGeneralCommandLine(this)
            .execute(parentDisposable)
            .unwrapOrElse { return RsResult.Err(it) }
        fullyRefreshDirectory(rootDirectory)

        val manifest =
            checkNotNull(rootDirectory.findChild(Consts.MANIFEST_FILE)) { "Can't find the manifest file" }
        return RsResult.Ok(manifest)
    }

    fun version(): String? {
        if (!isUnitTestMode) {
            checkIsBackgroundThread()
        }
        if (!location.isValidExecutable()) return null

        val commandLine = SuiCommandLine(
            null,
            listOf("--version"),
            workingDirectory = null,
        )
        val lines = commandLine.toGeneralCommandLine(this).execute()?.stdoutLines.orEmpty()
        return if (lines.isNotEmpty()) return lines.joinToString("\n") else null
    }

    companion object {
//        fun fromProject(project: Project): AptosCliExecutor? = project.aptosPath?.let { AptosCliExecutor(it) }
        fun fromProject(project: Project): SuiCliExecutor? = project.suiExec.toExecutor()

        data class GeneratedFilesHolder(val manifest: VirtualFile)

        fun suggestPath(): String? {
            for (path in homePathCandidates()) {
                when {
                    path.isDirectory() -> {
                        val candidate = path.resolveExisting(executableName("sui")) ?: continue
                        if (candidate.isExecutableFile())
                            return candidate.toAbsolutePath().toString()
                    }

                    path.isExecutableFile() && path.fileName.toString() == executableName("suis") -> {
                        if (path.isExecutableFile())
                            return path.toAbsolutePath().toString()
                    }
                }
            }
            return null
        }

        private fun homePathCandidates(): Sequence<Path> {
            val pathVariable = System.getenv("PATH") ?: return emptySequence()
            val pathSeparator = System.getProperty("path.separator")

            return pathVariable.split(pathSeparator).asSequence().map { Paths.get(it) }
        }
    }

}