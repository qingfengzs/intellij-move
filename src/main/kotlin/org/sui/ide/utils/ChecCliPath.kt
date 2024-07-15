package org.sui.ide.utils

//class ChecCliPath {
//
//    companion object {
//        fun checkCliPath(project: Project): Boolean {
//            val path = project.suiExec.execPath.toPathOrNull()
//            if (path == null || !path.isExecutableFile()) {
//                val notification = NotificationGroupManager.getInstance()
//                    .getNotificationGroup(NOTIFACATION_GROUP)
//                    .createNotification(
//                        "The Sui Cli was not found",
//                        "Please configure the Sui Cli path first.<br><a href='openSettings'>Open Setting</a>",
//                        NotificationType.INFORMATION
//                    )
//                    .setListener { n, e ->
//                        if (e.eventType == HyperlinkEvent.EventType.ACTIVATED && e.description == "openSettings") {
//                            val dataContext = DataContext { dataId ->
//                                if (CommonDataKeys.PROJECT.`is`(dataId)) project else null
//                            }
//                            OpenSettingsAction().actionPerformed(
//                                AnActionEvent.createFromAnAction(OpenSettingsAction(), null, "MyPlugin", dataContext)
//                            )
//                        }
//                    }
//                Notifications.Bus.notify(notification, project)
//                return false
//            } else {
//                return true
//            }
//        }
//    }
//
//}