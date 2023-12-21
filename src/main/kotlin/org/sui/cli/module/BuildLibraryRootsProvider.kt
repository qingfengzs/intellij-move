package org.sui.cli.module

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile
import org.sui.cli.MoveProject
import org.sui.cli.moveProjects
import org.sui.ide.MoveIcons
import org.sui.openapiext.contentRoots
import org.sui.openapiext.toVirtualFile
import javax.swing.Icon

class MoveLibrary(
    private val name: String,
    private val sourceRoots: Set<VirtualFile>,
    private val excludedRoots: Set<VirtualFile>,
    private val icon: Icon,
    private val version: String?
) : SyntheticLibrary(), ItemPresentation {
    override fun getSourceRoots(): Collection<VirtualFile> = sourceRoots
    override fun getExcludedRoots(): Set<VirtualFile> = excludedRoots

    override fun equals(other: Any?): Boolean = other is MoveLibrary && other.sourceRoots == sourceRoots
    override fun hashCode(): Int = sourceRoots.hashCode()

    override fun getLocationString(): String? = null

    override fun getIcon(unused: Boolean): Icon = icon

    override fun getPresentableText(): String = if (version != null) "$name $version" else name
}

class BuildLibraryRootsProvider : AdditionalLibraryRootsProvider() {
    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        return project.moveProjects
            .allProjects
            .smartFlatMap { it.ideaLibraries }
            .toMutableSet()
    }

    override fun getRootsToWatch(project: Project): List<VirtualFile> {
        return getAdditionalProjectLibraries(project).flatMap { it.sourceRoots }
    }
}

private fun <U, V> Collection<U>.smartFlatMap(transform: (U) -> Collection<V>): Collection<V> =
    when (size) {
        0 -> emptyList()
        1 -> transform(first())
        else -> this.flatMap(transform)
    }

private val MoveProject.ideaLibraries: Collection<SyntheticLibrary>
    get() {
        return this.dependencies
            .map { it.first }
            // dependency is not a child of any content root
            .filter { pkg ->
                this.project.contentRoots.all { ideRoot ->
                    !pkg.contentRoot.path.startsWith(ideRoot.path)
                }
            }
            .map {
                val sourceRoots = it.layoutPaths().mapNotNull { p -> p.toVirtualFile() }.toMutableSet()
                it.moveToml.tomlFile
                    ?.virtualFile?.let { f -> sourceRoots.add(f) }
                MoveLibrary(it.packageName, sourceRoots, emptySet(), MoveIcons.MOVE_LOGO, null)
            }

    }