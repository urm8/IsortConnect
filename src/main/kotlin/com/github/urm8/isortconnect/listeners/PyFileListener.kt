package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.service.SorterService
import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.jetbrains.python.PythonFileType

class PyFileListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val locator = ProjectLocator.getInstance()
        for (
            file in events
                .mapNotNull { event -> event.file }
                .filter { file -> file.fileType == PythonFileType.INSTANCE }
        ) {
            return locator.guessProjectForFile(file)?.run {
                return if (ProjectRootManager
                    .getInstance(this)
                    .fileIndex.run {
                        this.isInSource(file) ||
                            this.isInContent(file) ||
                            this.isInSourceContent(file)
                    } &&
                    this.service<IsortConnectService>().state.triggerOnSave
                ) {
                    PyFileApplier(file, this)
                } else {
                    null
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
        const val TIMEOUT = 500
    }
}
