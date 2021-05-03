package com.github.urm8.isortconnect.settings

import com.github.urm8.isortconnect.listeners.PyProjectParser
import com.jetbrains.django.util.VirtualFileUtil
import javax.swing.JComponent

@Suppress("DialogTitleCapitalization")
class Configurable : com.intellij.openapi.options.Configurable {

    private lateinit var _component: Component
    override fun isModified(): Boolean {
        val settings = IsortConnectService.instance
        return settings.url != _component.isortdURI ||
                settings.triggerOnSave != _component.triggerOnSave ||
                settings.pyprojectToml != _component.pyprojectToml ||
                settings.optimizeImports != _component.optimizeImports ||
                settings.showNotifications != _component.showNotifications ||
                settings.useCompression != _component.useCompression
    }

    override fun getDisplayName(): String {
        return "IsortConnect"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return _component.getPreferredFocusedComponent()
    }

    override fun apply() {
        val state = IsortConnectService.instance
        state.url = _component.isortdURI
        state.triggerOnSave = _component.triggerOnSave
        state.pyprojectToml = _component.pyprojectToml
        state.optimizeImports = _component.optimizeImports
        state.showNotifications = _component.showNotifications
        state.useCompression = _component.useCompression

        VirtualFileUtil.findFile(state.pyprojectToml)?.run {
            state.pyprojectConf = PyProjectParser.parse(this)
        }
    }

    override fun reset() {
        val settings = IsortConnectService.instance
        _component.isortdURI = settings.url
        _component.triggerOnSave = settings.triggerOnSave
        _component.pyprojectToml = settings.pyprojectToml
        _component.optimizeImports = settings.optimizeImports
        _component.showNotifications = settings.showNotifications
        _component.useCompression = settings.useCompression
    }

    override fun createComponent(): JComponent {
        _component = Component()
        return _component.panel
    }
}
