package com.github.urm8.isortconnect.settings

import com.github.urm8.isortconnect.actions.RunImportSort
import com.github.urm8.isortconnect.dialogs.PingDialog
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class Component {
    fun getPreferredFocusedComponent(): JComponent = urlTextField
    private val urlTextField = JBTextField()
    private val triggerOnSaveButton = JBCheckBox("Trigger on save ?")


    var url: String
        get() = urlTextField.text
        set(value) {
            urlTextField.text = value
        }
    var triggerOnSave: Boolean
        get() = triggerOnSaveButton.isSelected
        set(value) {
            triggerOnSaveButton.isSelected = value
        }

    val checkBtn = JButton("Check connection") // noqa

    init {
        checkBtn.addActionListener(ActionListener {
            val isReachable = RunImportSort.checkPing()
            PingDialog(isReachable).showAndGet()
        })
    }

    val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Server Url"), urlTextField, 1, true)
            .addLabeledComponent(JBLabel("Trigger On Save"), triggerOnSaveButton, 2, false)
            .addLabeledComponent(JBLabel("Check connection"), checkBtn, 3, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
}