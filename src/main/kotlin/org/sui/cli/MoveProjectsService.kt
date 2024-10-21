package org.sui.cli

import com.intellij.execution.RunManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectTracker
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.project.ex.ProjectEx
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.psi.util.parents
import com.intellij.util.messages.Topic
import org.jetbrains.annotations.TestOnly
import org.sui.cli.externalSystem.MoveExternalSystemProjectAware
import org.sui.cli.settings.MvProjectSettingsServiceBase.*
import org.sui.cli.settings.MvProjectSettingsServiceBase.Companion.MOVE_SETTINGS_TOPIC
import org.sui.cli.settings.debugErrorOrFallback
import org.sui.ide.notifications.logOrShowBalloon
import org.sui.lang.core.psi.ext.elementType
import org.sui.lang.toNioPathOrNull
import org.sui.openapiext.checkReadAccessAllowed
import org.sui.openapiext.common.isUnitTestFile
import org.sui.openapiext.common.isUnitTestMode
import org.sui.openapiext.debugInProduction
import org.sui.openapiext.toVirtualFile
import org.sui.stdext.AsyncValue
import org.sui.stdext.CacheEntry
import org.sui.stdext.FileToMoveProjectCache
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.TimeUnit

val Project.moveProjectsService get() = service<MoveProjectsService>()

val Project.hasMoveProject get() = this.moveProjectsService.allProjects.isNotEmpty()

class MoveProjectsService(val project: Project): Disposable {

    var initialized = false

    init {
        registerProjectAware(project, this)
    }

    val allProjects: List<MoveProject> get() = this.projects.state

    val hasAtLeastOneValidProject: Boolean get() = this.allProjects.isNotEmpty()

    fun scheduleProjectsRefresh(reason: String?): CompletableFuture<List<MoveProject>> {
        LOG.logOrShowBalloon("Refresh Projects ($reason)")
        return modifyProjectModel {
            doRefreshProjects(project, reason)
        }
    }

    @TestOnly
    fun scheduleProjectsRefreshSync(reason: String? = null): List<MoveProject> =
        scheduleProjectsRefresh(reason).get(1, TimeUnit.MINUTES)

    private fun registerProjectAware(project: Project, disposable: Disposable) {
        // There is no sense to register `CargoExternalSystemProjectAware` for default project.
        // Moreover, it may break searchable options building.
        // Also, we don't need to register `CargoExternalSystemProjectAware` in light tests because:
        // - we check it only in heavy tests
        // - it heavily depends on service disposing which doesn't work in light tests
        if (project.isDefault || isUnitTestMode && (project as? ProjectEx)?.isLight == true) return

        val moveProjectAware = MoveExternalSystemProjectAware(project)
        val projectTracker = ExternalSystemProjectTracker.getInstance(project)
        // starts tracking of project settings files
        projectTracker.register(moveProjectAware, disposable)
        // activate auto-reload
        projectTracker.activate(moveProjectAware.projectId)

        project.messageBus.connect(disposable)
            .subscribe(MOVE_SETTINGS_TOPIC, object: MoveSettingsListener {
                override fun <T: MvProjectSettingsBase<T>> settingsChanged(e: SettingsChangedEventBase<T>) {
                    if (e.affectsMoveProjectsMetadata) {
                        val tracker = ExternalSystemProjectTracker.getInstance(project)
                        tracker.markDirty(moveProjectAware.projectId)
                        tracker.scheduleProjectRefresh()
                    }
                }
            })

        // default projectTracker cannot detect Move.toml file creation,
        // as it's not present in the `settingsFiles`
        project.messageBus.connect(disposable)
            .subscribe(VirtualFileManager.VFS_CHANGES, OnMoveTomlCreatedFileListener {
                val tracker = ExternalSystemProjectTracker.getInstance(project)
                tracker.markDirty(moveProjectAware.projectId)
                tracker.scheduleProjectRefresh()
            })
    }

    // requires ReadAccess
    fun findMoveProjectForPsiElement(psiElement: PsiElement): MoveProject? {
        // read access required for the psiElement.containingFile
        checkReadAccessAllowed()
        val file = when (psiElement) {
            is PsiDirectory -> psiElement.virtualFile
            is PsiFile -> psiElement.originalFile.virtualFile
            else -> {
                val containingFile =
                    try {
                        // WARN: it fails with types of special psi factory generated items
                        psiElement.containingFile
                    } catch (e: PsiInvalidElementAccessException) {
                        val parentsChain =
                            psiElement.parents(true).map { it.elementType }.joinToString(" -> ")
                        debugErrorOrFallback(
                            "Cannot get the containing file for the ${psiElement.javaClass.name}, " +
                                    "elementType is ${psiElement.elementType}, parents chain is $parentsChain",
                            cause = e
                        ) {
                            try {
                                psiElement.node.psi.containingFile
                            } catch (e: PsiInvalidElementAccessException) {
                                null
                            }
                        }
                    }
                // returns LightVirtualFile for non-physical files
                containingFile?.originalFile?.virtualFile
            }
        } ?: return null
        return findMoveProjectForFile(file)
    }

    fun findMoveProjectForPath(path: Path): MoveProject? {
        val file = path.toVirtualFile() ?: return null
        return findMoveProjectForFile(file)
    }

    private fun doRefreshProjects(project: Project, reason: String?): CompletableFuture<List<MoveProject>> {
        val moveProjectsFut = CompletableFuture<List<MoveProject>>()

        val syncTask = MoveProjectsSyncTask(project, this, moveProjectsFut, reason)
        project.taskQueue.run(syncTask)

        return moveProjectsFut.thenApply { updatedProjects ->
            runOnlyInNonLightProject(project) {
                setupProjectRoots(project, updatedProjects)
            }
            updatedProjects
        }
    }

    fun findMoveProjectForFile(file: VirtualFile): MoveProject? {
        val cached = this.fileToMoveProjectCache.get(file)
        if (cached is CacheEntry.Present) return cached.value

        if (file.isUnitTestFile) return project.testMoveProject

        val filePath = file.toNioPathOrNull() ?: return null

        var resProject: MoveProject? = null
        val depthSortedProjects = this.projects.state.sortedByDescending { it.contentRoot.fsDepth }
        // first go through all the root packages of the projects
        for (candidate in depthSortedProjects) {
            val candidateRoot = candidate.currentPackage.contentRoot.toNioPathOrNull() ?: continue
            if (filePath.startsWith(candidateRoot)) {
                resProject = candidate
                break
            }
        }
        // go through dependencies
        for (candidate in this.projects.state) {
            if (resProject != null) break
            for (depPackage in candidate.depPackages()) {
                val depRoot = depPackage.contentRoot.toNioPathOrNull() ?: continue
                if (filePath.startsWith(depRoot)) {
                    resProject = candidate
                    break
                }
            }
        }
        this.fileToMoveProjectCache.put(file, CacheEntry.Present(resProject))
        return resProject
    }

    /**
     * The heart of the plugin Project model. Care must be taken to ensure
     * this is thread-safe, and that refreshes are scheduled after
     * set of projects changes.
     */
    private val projects = AsyncValue<List<MoveProject>>(emptyList())

    private val fileToMoveProjectCache = FileToMoveProjectCache(this)

    /**
     * All modifications to project model except for low-level `loadState` should
     * go through this method: it makes sure that when we update various IDEA listeners.
     */
    private fun modifyProjectModel(
        modifyProjects: (List<MoveProject>) -> CompletableFuture<List<MoveProject>>
    ): CompletableFuture<List<MoveProject>> {
        val syncOnRefreshTopic = {
            project.messageBus.takeIf { !it.isDisposed }
                ?.syncPublisher(MOVE_PROJECTS_REFRESH_TOPIC)
        }

        val wrappedModifyProjects = { projects: List<MoveProject> ->
            syncOnRefreshTopic()?.onRefreshStarted()
            modifyProjects(projects)
        }
        return projects.updateAsync(wrappedModifyProjects)
            .thenApply { projects ->
//                refreshOnBuildDirChangeWatcher.setWatchedProjects(projects)
                invokeAndWaitIfNeeded {
                    runWriteAction {
                        // remove file -> moveproject associations from cache
                        fileToMoveProjectCache.clear()

                        // disable for unit-tests: in those cases roots change is done by the test framework
                        runOnlyInNonLightProject(project) {
                            ProjectRootManagerEx.getInstanceEx(project)
                                .makeRootsChange(
                                    EmptyRunnable.getInstance(),
                                    RootsChangeRescanningInfo.TOTAL_RESCAN
                                )
                        }
                        // increments structure modification counter in the subscriber
                        project.messageBus
                            .syncPublisher(MOVE_PROJECTS_TOPIC).moveProjectsUpdated(this, projects)
                        initialized = true
                    }
                }
                projects
            }
            .handle { projects, err ->
                val status = err?.toRefreshStatus() ?: MoveRefreshStatus.SUCCESS
                syncOnRefreshTopic()?.onRefreshFinished(status)
                projects
            }
    }

    private fun Throwable.toRefreshStatus(): MoveRefreshStatus {
        return when {
            this is ProcessCanceledException -> MoveRefreshStatus.CANCEL
            this is CompletionException && cause is ProcessCanceledException -> MoveRefreshStatus.CANCEL
            else -> MoveRefreshStatus.FAILURE
        }
    }

    override fun dispose() {}

    companion object {
        private val LOG = logger<MoveProjectsService>()

        val MOVE_PROJECTS_TOPIC: Topic<MoveProjectsListener> = Topic.create(
            "move projects changes",
            MoveProjectsListener::class.java
        )

        val MOVE_PROJECTS_REFRESH_TOPIC: Topic<MoveProjectsRefreshListener> = Topic(
            "Move Projects refresh",
            MoveProjectsRefreshListener::class.java
        )

    }

    fun interface MoveProjectsListener {
        fun moveProjectsUpdated(service: MoveProjectsService, projects: Collection<MoveProject>)
    }

    interface MoveProjectsRefreshListener {
        fun onRefreshStarted()
        fun onRefreshFinished(status: MoveRefreshStatus)
    }

    enum class MoveRefreshStatus {
        SUCCESS,
        FAILURE,
        CANCEL
    }
}

fun setupProjectRoots(project: Project, moveProjects: List<MoveProject>) {
    invokeAndWaitIfNeeded {
        // Initialize services that we use (probably indirectly) in write action below.
        // Otherwise, they can be initialized in write action that may lead to deadlock
        RunManager.getInstance(project)
        ProjectFileIndex.getInstance(project)

        runWriteAction {
            if (project.isDisposed) return@runWriteAction
            ProjectRootManagerEx.getInstanceEx(project).mergeRootsChangesDuring {
                for (moveProject in moveProjects) {
                    val projectRoot = moveProject.contentRoot
                    val packageModule =
                        ModuleUtilCore.findModuleForFile(projectRoot, project) ?: continue
                    ModuleRootModificationUtil.updateModel(packageModule) { rootModel ->
                        val contentEntry = rootModel.contentEntries.singleOrNull() ?: return@updateModel
                        contentEntry.addExcludeFolder(FileUtil.join(projectRoot.url, "build"))
                    }
                }
            }
        }
    }
}

inline fun runOnlyInNonLightProject(project: Project, action: () -> Unit) {
    if ((project as? ProjectEx)?.isLight != true) {
        action()
    } else {
        check(isUnitTestMode)
    }
}

val VirtualFile.fsDepth: Int get() = this.path.split(File.separator).count()

fun Logger.logProjectsRefresh(status: String, reason: String? = null) {
    var logMessage = "PROJ_REFRESH $status"
    if (reason != null) {
        logMessage += " [$reason]"
    }
    this.debugInProduction(logMessage)
}
