package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.settings.AppState
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class TomlFileListener : AsyncFileListener {

    override fun prepareChange(p0: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val currentSettings = AppState.instance
        val tomlFilePath = currentSettings.pyprojectToml
        if (tomlFilePath.isBlank()) {
            return null
        }
        for (event in p0) {
            if (event.file?.path?.contains(tomlFilePath) == true) {
                return TomlParser(event.file!!)
            }
        }
        return null
    }

}