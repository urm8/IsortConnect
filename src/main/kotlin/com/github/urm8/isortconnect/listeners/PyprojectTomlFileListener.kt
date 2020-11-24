package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.toml.lang.psi.TomlFileType

class PyprojectTomlFileListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val locator = ProjectLocator.getInstance()
        for (file in events.mapNotNull { event -> event.file }.filter { file -> file.fileType == TomlFileType }) {
            return locator.guessProjectForFile(file)?.run {
                val service = this.service<IsortConnectService>()
                if (service.state.pyprojectToml == file.path) {
                    PyProjectParser(tomlFile = file)
                } else {
                    null
                }
            }
        }
        return null
    }

    companion object {
        const val TIMEOUT = 500
    }
}
