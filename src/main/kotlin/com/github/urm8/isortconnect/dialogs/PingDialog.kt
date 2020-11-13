package com.github.urm8.isortconnect.dialogs

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class PingDialog(private val isReachable: Boolean) : DialogWrapper(true) {
    init {
        init()
        title = "Connectivity check"
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout())
        val label = JLabel(if (isReachable) "Success!" else "Failed to connect!", SwingConstants.CENTER)
        label.font = Font(label.font.name, Font.BOLD, fontSize)
        label.preferredSize = Dimension(labelWight, labelHeight)
        panel.add(label, BorderLayout.CENTER)
        return panel
    }

    companion object {
        const val fontSize = 18
        const val labelHeight = 21
        const val labelWight = 150
    }
}
