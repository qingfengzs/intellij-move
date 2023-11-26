package org.move.cli.runConfigurations.producers

import com.intellij.psi.PsiElement
import org.move.cli.runConfigurations.sui.SuiCommandLine

data class CommandLineFromContext(
    val sourceElement: PsiElement,
    val configurationName: String,
    val commandLine: SuiCommandLine
)
