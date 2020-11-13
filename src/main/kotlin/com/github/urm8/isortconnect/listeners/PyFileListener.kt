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
            for (event in events.filter { event -> event.file?.extension?.equals("py", ignoreCase = true) ?: false }) {
                val file = event.file!!
                val project = locator.guessProjectForFile(file)
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
}
