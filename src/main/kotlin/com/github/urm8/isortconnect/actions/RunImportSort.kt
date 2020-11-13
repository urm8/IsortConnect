package com.github.urm8.isortconnect.actions

import com.github.urm8.isortconnect.service.SorterService
import com.github.urm8.isortconnect.settings.AppState
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.jetbrains.python.psi.PyFile

class RunImportSort : AnAction() {
    private val logger = com.intellij.openapi.diagnostic.Logger.getInstance(AppState::class.java)

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = e.project != null && e.getData(CommonDataKeys.PSI_FILE) is PyFile
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val sorterService = project.service<SorterService>()
        val document = editor.document
        if (document.text.isBlank()) {
            logger.warn("Empty file, exiting")
            return
        }
        sorterService.sort(document)
    }
}
