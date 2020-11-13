package com.github.urm8.isortconnect.service

import com.github.urm8.isortconnect.settings.AppState
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NotNull
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
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
        val contents = document.text
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .POST(HttpRequest.BodyPublishers.ofString(contents, Charset.forName("utf-8")))
            .headers(*headers.toTypedArray())
            .timeout(Duration.ofSeconds(5))
            .build()
        var sortedContents: String? = null

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            logger.warn("Got non 200 status code ${response.statusCode()}: ${response.body()}")
        } else {
            response.body().run {
                if (document.text.compareTo(this) != 0 && document.isWritable) {
                    logger.debug("applying changes to document at: $document")
                    WriteCommandAction.runWriteCommandAction(project) { document.setText(this) }
                }
            }
        }
    }

    companion object {
        private val settings: AppState
            get() = AppState.instance

        val uri: URI
            get() {
                if (settings.url.isBlank()) {
                    return URI.create("http://${AppState.DEFAULT_URL}")
                }
                var url = settings.url
                if (!url.startsWith("http://") || !url.startsWith("https://")) {
                    url = "http://$url"
                }
                return URI.create(url)
            }

        val headers: List<String>
            get() {
                return settings.pyprojectConf?.run {
                    this.flatMap { entry -> listOf("X-${entry.key.toUpperCase()}", entry.value) }
                } ?: listOf()
            }

        val client: HttpClient
            get() = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

        fun ping(): Boolean {
            val serverUrl = uri.resolve("/ping")
            val request = HttpRequest.newBuilder()
                .uri(serverUrl)
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build()

            return try {
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                response.statusCode() == 200 && response.body() == "pong"
            } catch (e: IOException) {
                Logger.getInstance(SorterService::class.java).error("Failed to connect", e)
                false
            }
        }
    }
}
