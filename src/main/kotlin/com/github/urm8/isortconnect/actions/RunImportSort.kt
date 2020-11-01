package com.github.urm8.isortconnect.actions

import com.github.urm8.isortconnect.settings.AppState
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.psi.PyFile
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import java.time.Duration

class RunImportSort : AnAction() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = e.project != null && e.getData(CommonDataKeys.PSI_FILE) is PyFile
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val document = editor.document
        val contents = document.text
        if (contents.isBlank()) {
            logger.warn("Empty file, exiting")
            return
        }
        val request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(contents, Charset.forName("utf-8")))
                .headers(*headers.toTypedArray())
                .timeout(Duration.ofSeconds(5))
                .build()
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply {
            if (it.statusCode() != 200) {
                logger.warn("Got non 200 status code ${it.statusCode()}: ${it.body()}")
                return@thenApply
            }
            val sortedContents: String = it.body()!!
            if (contents.compareTo(sortedContents) != 0 && document.isWritable) {
                WriteCommandAction.runWriteCommandAction(project) { document.setText(sortedContents) }
            }

        }.exceptionally {
            logger.error("Oops, something went wrong ...", it)
        }
    }


    companion object {
        val settings: AppState
            get() = AppState.instance

        val uri: URI
            get() {
                if (settings.url.isBlank()) {
                    return URI.create("http://${AppState.DEFAULT_URL}")
                }
                var url = settings.url
                if (!url.startsWith("http://") || !url.startsWith("https://")) {
                    url = "http://${url}"
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

        private val logger = Logger.getInstance(AppState::class.java)

        fun checkPing(): Boolean {
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
                logger.error("Failed to connect", e)
                false
            }
        }
    }
}
