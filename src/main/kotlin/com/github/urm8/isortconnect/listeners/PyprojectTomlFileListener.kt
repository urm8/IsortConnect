package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class PyprojectTomlFileListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val currentSettings = IsortConnectService.instance
        val tomlFilePath = currentSettings.pyprojectToml
        if (!tomlFilePath.isBlank()) {
            for (event in events) {
                val file = event.file
                if (file != null && file.path.contains(tomlFilePath)) {
                    return PyProjectParser(file)
                }
            }
        }
        return null
    }
}
