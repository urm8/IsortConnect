package com.github.urm8.isortconnect.service

import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
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
        sortImports(document)
    }

    fun sort(vf: @NotNull VirtualFile) {
        FileDocumentManager.getInstance().getDocument(vf)?.run {
            sort(this)
        }
    }

    private fun sortImports(document: @NotNull Document) {
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

    companion object {
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
