package org.sui.ide.dialog

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.sui.common.NOTIFACATION_GROUP
import org.sui.ide.actions.OpenSwitchEnvsDialogAction
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.DefaultTableModel

class EnvDialog(var data: OpenSwitchEnvsDialogAction.Envs) : DialogWrapper(true) {
    init {
        init()
        title = "Click Env To Switch"
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        val tableModel = DefaultTableModel(arrayOf("alias", "rpc", "ws"), 0)
        val table = JBTable(tableModel)

        data.networks.forEach { net ->
            tableModel.addRow(arrayOf(net.alias, net.rpc, net.ws))
        }
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val button = JButton("Switch")
        button.addActionListener {
            val row = table.selectedRow
            if (row >= 0) {
                executeCommand(tableModel.getValueAt(row, 0).toString())
            }
            dispose()
        }

        val scrollPane = JBScrollPane(table)
        scrollPane.preferredSize = Dimension(550, 150)
        panel.layout = BorderLayout()
        panel.add(scrollPane, BorderLayout.NORTH)
        panel.add(button, BorderLayout.AFTER_LINE_ENDS)
        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf()
    }

    private fun executeCommand(env: String) {
        val commandLine = GeneralCommandLine("sui", "client", "switch", "--env", env)
        ExecUtil.execAndGetOutput(commandLine)
    }

    private fun showNotification(message: String) {
        Notifications.Bus.notify(Notification(NOTIFACATION_GROUP, "Switch Env", message, NotificationType.INFORMATION))
    }
}