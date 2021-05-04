package com.github.urm8.isortconnect.settings

import com.github.urm8.isortconnect.dialogs.PingDialog
import com.github.urm8.isortconnect.service.SorterService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBBox
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
    private val urlTextField = JBTextField().apply {
        this.emptyText.text = "e.g. localhost:47393"
    }
    private val urlLabel = JBLabel("URI of \"isortd\" service:")
    private val urlBox = JBBox.createHorizontalBox().apply {
        this.add(urlTextField)
        this.add(
            JButton("Check connection").apply {
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
        )
    }

    private val pyprojectURITextField = JBTextField()
    private val pyprojectURILabel = JBLabel("Path to \"pyproject.toml\":")
    private val loadPyprojectButton = TextFieldWithBrowseButton(pyprojectURITextField) {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("toml")
        val file = FileChooser.chooseFile(descriptor, null, null) ?: return@TextFieldWithBrowseButton
        if (file.name == "pyproject.toml") {
            pyprojectURITextField.text = file.path
        }
    }

    private val triggerOnSaveCheckBox = JBCheckBox().apply {
        this.text = "Trigger on save"
    }
    private val triggerOptimizeImportsCheckBox = JBCheckBox().apply {
        this.text = "Optimize imports before sort"
    }
    private val showNotificationsCheckBox = JBCheckBox().apply {
        this.text = "Show notifications"
    }

    var isortdURI: String
        get() = urlTextField.text
        set(value) {
            urlTextField.text = value
        }
    var pyprojectToml: String
        get() = pyprojectURITextField.text
        set(value) {
            pyprojectURITextField.text = value
        }
    var triggerOnSave: Boolean
        get() = triggerOnSaveCheckBox.isSelected
        set(value) {
            triggerOnSaveCheckBox.isSelected = value
        }

    var optimizeImports: Boolean
        get() = triggerOptimizeImportsCheckBox.isSelected
        set(value) {
            triggerOptimizeImportsCheckBox.isSelected = value
        }

    var showNotifications: Boolean
        get() = showNotificationsCheckBox.isSelected
        set(value) {
            showNotificationsCheckBox.isSelected = value
        }

    val panel: JPanel = FormBuilder.createFormBuilder().apply {
        this.addLabeledComponent(urlLabel, urlBox)
        this.addLabeledComponent(pyprojectURILabel, loadPyprojectButton)
        this.addComponent(triggerOnSaveCheckBox)
        this.addComponent(triggerOptimizeImportsCheckBox)
        this.addComponent(showNotificationsCheckBox)
        this.addComponentFillVertically(JPanel(), this.lineCount)
    }.panel
}
