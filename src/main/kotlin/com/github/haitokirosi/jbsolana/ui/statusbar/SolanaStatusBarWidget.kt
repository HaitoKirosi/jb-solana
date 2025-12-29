package com.github.haitokirosi.jbsolana.ui.statusbar

import com.github.haitokirosi.jbsolana.services.SolanaCliService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.Timer

class SolanaStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = SolanaStatusBarWidget.ID
    override fun getDisplayName(): String = "Solana Configuration"
    override fun isAvailable(project: Project): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = SolanaStatusBarWidget(project)
    override fun disposeWidget(widget: StatusBarWidget) = widget.dispose()
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

class SolanaStatusBarWidget(private val project: Project) : CustomStatusBarWidget {

    companion object {
        const val ID = "SolanaStatusBarWidget"
        private const val REFRESH_INTERVAL_MS = 10000
        private const val PREFIX = "Solana: "
        private const val LOADING_STATE = "Refreshing..."
    }

    private val label = JBLabel(PREFIX + LOADING_STATE)
    private val timer = Timer(REFRESH_INTERVAL_MS) { fetchClusterStatus() }

    init {
        label.border = JBUI.Borders.empty(0, 2)
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                updateLabel(LOADING_STATE)
                fetchClusterStatus()
            }
        })

        fetchClusterStatus()
        timer.start()
    }

    private fun fetchClusterStatus() {
        ApplicationManager.getApplication().executeOnPooledThread {
            val cluster = SolanaCliService.getInstance(project).getActiveCluster()
            ApplicationManager.getApplication().invokeLater {
                updateLabel(cluster)
            }
        }
    }

    private fun updateLabel(status: String) {
        label.text = "$PREFIX$status"
        label.toolTipText = "Current Cluster: $status"
    }

    override fun ID(): String = ID

    override fun getComponent(): JComponent = label

    override fun install(statusBar: StatusBar) {}

    override fun dispose() {
        timer.stop()
    }
}
