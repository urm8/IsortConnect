package com.github.urm8.isortconnect.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import java.time.Duration

class RunImportSort : AnAction() {
    private val _host = "localhost"
    private val _port = 47393
    private val uri: URI = URI.create("http://${_host}:${_port}")
    private val _client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
    private val _app: Application = ApplicationManager.getApplication()
    private val _log = Logger.getInstance(javaClass)

    override fun actionPerformed(e: AnActionEvent) {

        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val document = editor.document
        val contents = document.text
        if (contents.isBlank()) {
            _log.warn("Empty file, exiting")
            return
        }
        val request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(contents, Charset.forName("utf-8")))
                .header("X-PROFILE", "black")
                .timeout(Duration.ofSeconds(5))
                .build()
        try {
            _client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply {
                if (it.statusCode() != 200) {
                    _log.warn("Got non 200 status code ${it.statusCode()}: ${it.body()}")
                    return@thenApply
                }
                val sortedContents: String = it.body()!!
                if (contents.compareTo(sortedContents) != 0 && document.isWritable) {
                    WriteCommandAction.runWriteCommandAction(project) { document.setText(sortedContents) }
                }

            }
        } catch (e: Exception) {
            _log.error("failed to get response from sorter", e)
            return
        }
    }

}
