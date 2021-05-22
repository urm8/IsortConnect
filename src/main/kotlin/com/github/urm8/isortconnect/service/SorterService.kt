package com.github.urm8.isortconnect.service

import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.jetbrains.python.codeInsight.imports.PyImportOptimizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.annotations.NotNull
import java.io.IOException
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URI
import java.time.Duration

@Service
class SorterService(private val project: @NotNull Project) {

    private val logger = Logger.getInstance(SorterService::class.java)

    fun sort(document: @NotNull Document) {
        FileDocumentManager.getInstance().getFile(document)?.run {
            run(document, this)
        }
    }

    fun sort(vf: @NotNull VirtualFile) {
        FileDocumentManager.getInstance().getDocument(vf)?.run {
            run(this, vf)
        }
    }

    private fun run(document: @NotNull Document, vf: @NotNull VirtualFile) {
        val task = object : Task.Backgroundable(project, "Sorting imports...") {
            override fun run(p0: ProgressIndicator) {
                sortImports(document, vf)
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
    }

    private fun sortImports(document: @NotNull Document, vf: @NotNull VirtualFile) {
        if (document.isWritable) {
            GlobalScope.launch(Dispatchers.IO) {
                val contents = document.text
                val sortedFile = with(uri.toURL().openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    headers.entries.forEach {
                        val value = if (it.value is Collection<*>) {
                            (it.value as Collection<*>).joinToString(separator = ",") { elem -> elem.toString() }
                        } else {
                            it.value.toString()
                        }.apply { this.trim() }
                        val key = "X-${it.key.toUpperCase()}"
                        setRequestProperty(key, value)
                    }
                    val projectRootManager = ProjectRootManager.getInstance(project)
                    if (settings.optimizeImports) {
                        optimizeImports(project, document)
                    }
                    if (settings.pyprojectToml.isNotBlank()) {
                        val roots = LocalFileSystem.getInstance().findFileByPath(settings.pyprojectToml)!!
                            .parent.children.filter { virtualFile -> hasPythonModules(virtualFile) }.joinToString(",") { file -> file.path }
                        setRequestProperty("XX-SRC", roots)
                    } else if (projectRootManager.contentSourceRoots.isNotEmpty()) {
                        val srcRoots =
                            projectRootManager.contentSourceRoots.joinToString(separator = ",") { elem -> elem.path }
                        setRequestProperty("XX-SRC", srcRoots)
                    } else {
                        val roots = projectRootManager.contentRoots.flatMap { vf ->
                            vf.children.filter { child ->
                                child.isDirectory && child.findChild("__init__.py") != null || child.children.any { grandChild ->
                                    grandChild != null && grandChild.isDirectory && child.findChild(
                                        "__init__.py"
                                    ) != null
                                }
                            }
                        }.joinToString(separator = ",") { virtualFile -> virtualFile.path }
                        setRequestProperty("XX-SRC", roots)
                    }
                    setRequestProperty("XX-PATH", vf.path)
                    connectTimeout = Duration.ofSeconds(defaultTimeOutSeconds).toMillis().toInt()
                    doOutput = true
                    try {
                        val requestBodyWriter = outputStream.bufferedWriter()
                        requestBodyWriter.write(contents)
                        requestBodyWriter.flush()
                        val body = inputStream.bufferedReader().readText()
                        if (responseCode != HTTP_OK) {
                            logger.warn("Got non 200 status code $responseCode: $body")
                            return@launch
                        }
                        body
                    } catch (e: IOException) {
                        logger.warn("failed to connect", e)

                        return@launch
                    }
                }
                if (document.isWritable && document.text.compareTo(sortedFile) != -1) {
                    logger.debug("applying changes to document at: $document")
                    WriteCommandAction.runWriteCommandAction(project) { document.setText(sortedFile) }
                }
            }
        }
    }

    private fun optimizeImports(project: Project, document: @NotNull Document) {
        try {
            WriteAction.runAndWait<Exception> {
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)!!
                PyImportOptimizer.onlyRemoveUnused().processFile(psiFile).run()
            }
        } catch (e: Exception) {
            logger.warn("failed to optimize imports: %s", e)
        }
    }

    companion object {
        val excludeDirs = mutableSetOf<String>(".git")
        const val defaultTimeOutSeconds: Long = 1
        private val settings: IsortConnectService.State
            get() = IsortConnectService.instance

        val uri: URI
            get() {
                if (settings.url.isBlank()) {
                    return URI.create("http://${IsortConnectService.DEFAULT_URL}")
                }
                var url = settings.url
                if (!url.startsWith("http://") || !url.startsWith("https://")) {
                    url = "http://$url"
                }
                return URI.create(url)
            }

        val headers: Map<String, Any>
            get() {
                return settings.pyprojectConf ?: mapOf()
            }

        fun hasPythonModules(dir: VirtualFile): Boolean {
            if (!dir.isDirectory || excludeDirs.contains(dir.name)) {
                return false
            }
            var pythonFilesFound = false
            VfsUtilCore.iterateChildrenRecursively(
                dir,
                { gch -> gch.isDirectory || !gch.extension.isNullOrBlank() && gch.extension.equals("py") },
                { file: VirtualFile ->
                    if (file.isDirectory) {
                        return@iterateChildrenRecursively true
                    }
                    pythonFilesFound = true
                    false
                }
            )
            if (!pythonFilesFound) {
                excludeDirs.add(dir.name)
            }
            return pythonFilesFound
        }

        fun ping(): Boolean {
            val serverUrl = uri.resolve("/ping").toURL()
            return with(serverUrl.openConnection() as HttpURLConnection) {
                connectTimeout = Duration.ofSeconds(defaultTimeOutSeconds).toMillis().toInt()
                try {
                    responseCode == HTTP_OK && "pong" == inputStream.bufferedReader().readText()
                } catch (e: IOException) {
                    Logger.getInstance(SorterService::class.java).error("Failed to connect", e)
                    false
                }
            }
        }
    }
}
