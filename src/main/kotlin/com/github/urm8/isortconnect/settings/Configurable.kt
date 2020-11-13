package com.github.urm8.isortconnect.settings

import com.github.urm8.isortconnect.listeners.PyProjectParser
import com.jetbrains.django.util.VirtualFileUtil
import javax.swing.JComponent

class Configurable : com.intellij.openapi.options.Configurable {

    private lateinit var _component: Component
    override fun isModified(): Boolean {
        val settings = AppState.instance
        return settings.url != _component.url ||
            settings.triggerOnSave != _component.triggerOnSave ||
            settings.pyprojectToml != _component.pyprojectToml
    }

    override fun getDisplayName(): String {
        return "iSortConnect"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return _component.getPreferredFocusedComponent()
    }

    override fun apply() {
        val state = AppState.instance
        state.url = _component.url
        state.triggerOnSave = _component.triggerOnSave
        state.pyprojectToml = _component.pyprojectToml
        VirtualFileUtil.findFile(state.pyprojectToml)?.run {
            state.pyprojectConf = PyProjectParser.parse(this)
        }
    }

    override fun reset() {
        val settings = AppState.instance
        _component.url = settings.url
        _component.triggerOnSave = settings.triggerOnSave
        _component.pyprojectToml = settings.pyprojectToml
    }

    override fun createComponent(): JComponent? {
        _component = Component()
        return _component.panel
    }
}
