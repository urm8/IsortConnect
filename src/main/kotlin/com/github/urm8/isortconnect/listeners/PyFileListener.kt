package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.service.SorterService
import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.jetbrains.python.PythonFileType

class PyFileListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val filesToVisit = events.mapNotNull { event -> checkEvent(event = event) }
        if (filesToVisit.isNotEmpty()) {
            return PyFileApplier(filesToVisit)
        }
        return null
    }

    private fun checkEvent(event: VFileEvent): PyFileWithService? {
        if (isPythonFile(event) && isFileModifiedAndWritable(event)) {
            val file = event.file!!
            return locator.guessProjectForFile(file)?.run {
                val project = this
                if (project.service<IsortConnectService>().state.triggerOnSave) {
                    rootManagers.getOrPut(project, { ProjectRootManager.getInstance(this) }).fileIndex.run {
                        if (isInSource(file) || isInContent(file) || isInSourceContent(file) || isInTestSourceContent
                            (file)
                        ) {
                            return PyFileWithService(file, project.service<SorterService>())
                        }
                    }
                }
                null
            }
        }
        return null
    }

    private fun isFileModifiedAndWritable(event: VFileEvent): Boolean {
        val file = event.file!!
        return (fileDocumentManager.isFileModified(file) || (event is VFileContentChangeEvent && event.isFromRefresh)) &&
            fileDocumentManager.getDocument(file)?.isWritable == true
    }

    private fun isPythonFile(event: VFileEvent) =
        event.file?.fileType == PythonFileType.INSTANCE

    class PyFileApplier(private val toSort: Collection<PyFileWithService>) : AsyncFileListener.ChangeApplier {
        override fun beforeVfsChange() {
            super.beforeVfsChange()
            toSort.forEach { pyFile -> pyFile.service.sort(pyFile.file) }
        }
    }

    companion object {
        private val locator: ProjectLocator = ProjectLocator.getInstance()
        private val rootManagers: MutableMap<Project, ProjectRootManager> = mutableMapOf()
        private val fileDocumentManager: FileDocumentManager = FileDocumentManager.getInstance()
    }
}
