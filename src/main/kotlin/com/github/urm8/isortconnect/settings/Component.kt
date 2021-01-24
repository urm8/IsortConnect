package com.github.urm8.isortconnect.settings

import com.github.urm8.isortconnect.dialogs.PingDialog
import com.github.urm8.isortconnect.service.SorterService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class Component {
    fun getPreferredFocusedComponent(): JComponent = urlTextField
    private val urlTextField = JBTextField()
    private val triggerOnSaveButton = JBCheckBox("Trigger on save ?")
    private val pyprojectTomlTextField = JBTextField()
    private val loadPyProjectTomlButton = TextFieldWithBrowseButton(pyprojectTomlTextField) {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("toml")
        val file = FileChooser.chooseFile(descriptor, null, null) ?: return@TextFieldWithBrowseButton
        if (file.name == "pyproject.toml") {
            pyprojectTomlTextField.text = file.path
        }
    }

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

    var pyprojectToml: String
        get() = pyprojectTomlTextField.text
        set(value) {
            pyprojectTomlTextField.text = value
        }

    private val checkBtn = JButton("Check connection").apply {
        this.addActionListener {
            GlobalScope.launch(Dispatchers.IO) {
                val isReachable = SorterService.ping()
                ApplicationManager.getApplication().invokeLater(
                    {
                        PingDialog(isReachable).showAndGet()
                    },
                    ModalityState.stateForComponent(rootPane)
                )
            }
        }
    }

    var topInset = 0
    val panel = FormBuilder.createFormBuilder()
        .addLabeledComponent(JBLabel("Server Url"), urlTextField, ++topInset, true)
        .addLabeledComponent(JBLabel("Trigger On Save"), triggerOnSaveButton, ++topInset, false)
        .addLabeledComponent(JBLabel("Check connection"), checkBtn, ++topInset, false)
        .addLabeledComponent(JBLabel("pyproject.toml"), loadPyProjectTomlButton, ++topInset, false)
        .addComponentFillVertically(JPanel(), 0)
        .panel
}
