package org.move.ide.dialog

import com.intellij.execution.process.ProcessOutput
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogWrapper
import org.move.cli.settings.suiExec
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel

class EnvDialog(var data: List<String>) : DialogWrapper(true) {
    init {
        init()
        title = "Click Env To Switch"
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()

        val tableModel = DefaultTableModel(arrayOf("Envs"), 0)
        data.forEach { env ->
            tableModel.addRow(arrayOf(env))
        }
        val table = JTable(tableModel)

        val columnModel = table.columnModel
        columnModel.getColumn(0).preferredWidth = 500

        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val row = table.rowAtPoint(e.point)
                if (row >= 0) {
                    executeCommand(tableModel.getValueAt(row, 0).toString())
                }
                dispose()
            }
        })

        val scrollPane = JScrollPane(table)
        scrollPane.preferredSize = Dimension(550, 150)
        panel.add(scrollPane)
        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf()
    }

    private fun executeCommand(env: String) {
        val project = ProjectManager.getInstance().defaultProject

        val onProcessComplete: (ProcessOutput?) -> Unit = { output ->
            if (output != null && output.exitCode == 0) {
                println("Process executed successfully with output: ${output.stdout}")
                showNotification(env)
            } else {
                println("Process failed with error: ${output?.stderr}")
            }
        }
        project.suiExec.toExecutor()
            ?.simpleCommand(project, "client", listOf("switch", "--env", env), onProcessComplete)
    }

    private fun showNotification(message: String) {
        Notifications.Bus.notify(Notification("Move Language", "Switch Env", message, NotificationType.INFORMATION))
    }
}