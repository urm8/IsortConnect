package com.github.urm8.isortconnect.settings

import javax.swing.JComponent

class Configurable : com.intellij.openapi.options.Configurable {
    lateinit var _component: Component
    override fun isModified(): Boolean {
        val settings = AppState.instance
        return settings.url != _component.url || settings.triggerOnSave != _component.triggerOnSave
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
    }

    override fun reset() {
        val settings = AppState.instance
        _component.url = settings.url
        _component.triggerOnSave = settings.triggerOnSave
    }

    override fun createComponent(): JComponent? {
        _component = Component()
        return _component.panel
    }

}