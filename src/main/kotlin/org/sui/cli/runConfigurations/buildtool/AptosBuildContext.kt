package org.sui.cli.runConfigurations.buildtool

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.UserDataHolderEx
import org.sui.cli.MoveProject
import org.sui.cli.runConfigurations.aptos.workingDirectory
import org.sui.cli.runConfigurations.buildtool.AptosBuildManager.showBuildNotification
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentHashMap.KeySetView
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

abstract class AptosBuildContextBase(
    val moveProject: MoveProject,
    @NlsContexts.ProgressText val progressTitle: String,
    val isTestBuild: Boolean,
    val buildId: Any,
    val parentId: Any
) {
    val project: Project get() = moveProject.project
    val workingDirectory: Path get() = moveProject.workingDirectory

    @Volatile
    var indicator: ProgressIndicator? = null

    val errors: AtomicInteger = AtomicInteger()
    val errorCodes: KeySetView<String, Boolean> = ConcurrentHashMap.newKeySet()
    val warnings: AtomicInteger = AtomicInteger()
}

class AptosBuildContext(
    moveProject: MoveProject,
    val environment: ExecutionEnvironment,
    @NlsContexts.ProgressTitle val taskName: String,
    @NlsContexts.ProgressText progressTitle: String,
    isTestBuild: Boolean,
    buildId: Any,
    parentId: Any
): AptosBuildContextBase(moveProject, progressTitle, isTestBuild, buildId, parentId) {

    @Volatile
    var processHandler: ProcessHandler? = null

    private val buildSemaphore: Semaphore = project.getUserData(BUILD_SEMAPHORE_KEY)
        ?: (project as UserDataHolderEx).putUserDataIfAbsent(BUILD_SEMAPHORE_KEY, Semaphore(1))

    val result: CompletableFuture<AptosCompileResult> = CompletableFuture()

    val started: Long = System.currentTimeMillis()

    @Volatile
    var finished: Long = started
    private val duration: Long get() = finished - started

    fun waitAndStart(): Boolean {
        indicator?.pushState()
        try {
            indicator?.text = "Waiting for the current build to finish..."
            indicator?.text2 = ""
            while (true) {
                indicator?.checkCanceled()
                try {
                    if (buildSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS)) break
                } catch (e: InterruptedException) {
                    throw ProcessCanceledException()
                }
            }
        } catch (e: ProcessCanceledException) {
            canceled()
            return false
        } finally {
            indicator?.popState()
        }
        return true
    }

    fun finished(isSuccess: Boolean) {
        val isCanceled = indicator?.isCanceled ?: false

//        environment.artifacts = artifacts.takeIf { isSuccess && !isCanceled }

        finished = System.currentTimeMillis()
        buildSemaphore.release()

        val finishMessage: String
        val finishDetails: String?

        val errors = errors.get()
        val warnings = warnings.get()

        // We report successful builds with errors or warnings correspondingly
        val messageType = if (isCanceled) {
            finishMessage = "$taskName canceled"
            finishDetails = null
            MessageType.INFO
        } else {
            val hasWarningsOrErrors = errors > 0 || warnings > 0
            finishMessage = if (isSuccess) "$taskName finished" else "$taskName failed"
            finishDetails = if (hasWarningsOrErrors) {
                val errorsString = if (errors == 1) "error" else "errors"
                val warningsString = if (warnings == 1) "warning" else "warnings"
                "$errors $errorsString and $warnings $warningsString"
            } else {
                null
            }

            when {
                !isSuccess -> MessageType.ERROR
                hasWarningsOrErrors -> MessageType.WARNING
                else -> MessageType.INFO
            }
        }

        result.complete(
            AptosCompileResult(
                succeeded = isSuccess,
                canceled = isCanceled,
                started = started,
                duration = duration,
                errors = errors,
                warnings = warnings,
                message = finishMessage
            )
        )

        showBuildNotification(project, messageType, finishMessage, finishDetails, duration)
    }

    fun canceled() {
        finished = System.currentTimeMillis()

        result.complete(
            AptosCompileResult(
                succeeded = false,
                canceled = true,
                started = started,
                duration = duration,
                errors = errors.get(),
                warnings = warnings.get(),
                message = "$taskName canceled"
            )
        )

        environment.notifyProcessNotStarted()
    }

    companion object {
        private val BUILD_SEMAPHORE_KEY: Key<Semaphore> = Key.create("BUILD_SEMAPHORE_KEY")
    }
}

data class AptosCompileResult(
    val succeeded: Boolean,
    val canceled: Boolean,
    val started: Long,
    val duration: Long = 0,
    val errors: Int = 0,
    val warnings: Int = 0,
    val message: String = ""
)
