/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.sui.cli.runConfigurations.aptos

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.NlsActions.ActionDescription
import com.intellij.openapi.util.NlsActions.ActionText
import org.sui.cli.runConfigurations.hasMoveProject
import javax.swing.Icon

abstract class RunAptosCommandActionBase(
    @ActionText text: String? = null,
    @ActionDescription description: String? = null,
    icon: Icon? = null
): DumbAwareAction(text, description, icon) {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun update(e: AnActionEvent) {
        val hasMoveProject = e.project?.hasMoveProject == true
        e.presentation.isEnabledAndVisible = hasMoveProject
    }
}
