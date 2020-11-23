package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.service.SorterService
import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.jetbrains.python.PythonFileType

class PyFileListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val dataContext = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(TIMEOUT)
        val project = dataContext?.getData(PROJECT)
        if (project != null) {
            val index = ProjectRootManager.getInstance(project).fileIndex
            val service = project.service<IsortConnectService>()
            if (service.state.triggerOnSave) {
                for (
                    file in events
                        .mapNotNull { event -> event.file }
                        .filter { file -> file.fileType != PythonFileType.INSTANCE || !index.isInSource(file) }
                ) {
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
        const val TIMEOUT = 2000
    }
}
