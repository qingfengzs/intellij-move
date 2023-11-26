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
import java.util.regex.Matcher
import javax.swing.*
import javax.swing.table.DefaultTableModel

class AddressDialog(var data: List<String>) : DialogWrapper(true) {
    init {
        init()
        title = "Click Address To Switch"
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()

        // 创建表格模型并填充数据
        val tableModel = DefaultTableModel(arrayOf("Address"), 0)
        data.forEach { address ->
            tableModel.addRow(arrayOf(address))
        }
        val table = JTable(tableModel)

        // 调整列宽
        val columnModel = table.columnModel
        columnModel.getColumn(0).preferredWidth = 500

        // 添加点击事件监听器
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val row = table.rowAtPoint(e.point)
                if (row >= 0) {
                    executeCommand(tableModel.getValueAt(row, 0).toString())
                }
                dispose() // 关闭对话框
            }
        })

        val scrollPane = JScrollPane(table)
        scrollPane.preferredSize = Dimension(550, 150) // 调整滚动面板大小
        panel.add(scrollPane)
        return panel
    }

    // 移除标准按钮
    override fun createActions(): Array<Action> {
        return arrayOf()
    }

    private fun executeCommand(address: String) {
        // 获取project 对象
        val project = ProjectManager.getInstance().defaultProject

        val onProcessComplete: (ProcessOutput?) -> Unit = { output ->
            if (output != null && output.exitCode == 0) {
                println("Process executed successfully with output: ${output.stdout}")
                showNotification(address)
            } else {
                println("Process failed with error: ${output?.stderr}")
            }
        }
        project.suiExec.toExecutor()
            ?.simpleCommand(project, "client", listOf("switch", "--address", address), onProcessComplete)
    }

    private fun showNotification(message: String) {
        // 显示通知
        Notifications.Bus.notify(Notification("Move Language", "Switch Address", message, NotificationType.INFORMATION))
    }
}