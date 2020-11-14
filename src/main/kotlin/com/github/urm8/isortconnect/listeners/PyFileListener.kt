package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.service.SorterService
import com.github.urm8.isortconnect.settings.AppState
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class PyFileListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        if (AppState.instance.triggerOnSave) {
            val locator = ProjectLocator.getInstance()
            for (event in events) {
                val file = event.file
                if (file == null || file.extension != PY_EXT) {
                    continue
                }
                val project = locator.guessProjectForFile(event.file)
                if (project != null) {
                    return PyFileApplier(file, project)
                }
            }
        }
        return null
    }

    class PyFileApplier(private val vf: VirtualFile, private val proj: Project) : AsyncFileListener.ChangeApplier {
        override fun afterVfsChange() {
            super.afterVfsChange()
            val sorter = proj.service<SorterService>()
            sorter.sort(vf)
        }
    }

    companion object {
        const val PY_EXT: String = "py"
    }
}
