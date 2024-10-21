package org.sui.cli

import com.intellij.openapi.externalSystem.model.ProjectSystemId
import org.sui.stdext.exists
import java.nio.file.Path

//object MvProjectLayout {
//    val sourcesDirs = arrayOf("sources", "examples", "scripts")
//    const val testsDir = "tests"
//    const val buildDir = "build"
//
//    fun dirs(root: Path): List<Path> {
//        val names = listOf(*sourcesDirs, testsDir, buildDir)
//        return names.map { root.resolve(it) }.filter { it.exists() }
//    }
//}

object Consts {
    const val MANIFEST_FILE = "Move.toml"
    const val ADDR_PLACEHOLDER = "_"
    const val MOVE_COMPILER_V2_ENV = "MOVE_LANGUAGE_V2"

    val PROJECT_SYSTEM_ID = ProjectSystemId("Sui Move")
}
