package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.toml.lang.psi.TomlFileType

class PyprojectTomlFileListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val currentSettings = IsortConnectService.instance
        val tomlFilePath = currentSettings.pyprojectToml
        val dataContext = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(2000)
        val project = dataContext?.getData(CommonDataKeys.PROJECT)
        if (!tomlFilePath.isBlank() && project != null) {
            for (file in events.mapNotNull { event -> event.file }.filter { file -> file.fileType == TomlFileType }) {
                return PyProjectParser(tomlFile = file)
            }
        }
        return null
    }
}
