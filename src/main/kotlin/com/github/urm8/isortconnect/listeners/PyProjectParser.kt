package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.settings.AppState
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import org.tomlj.Toml
import org.tomlj.TomlTable

@Suppress("UNCHECKED_CAST")
class PyProjectParser(private val tomlFile: VirtualFile) : AsyncFileListener.ChangeApplier {

    override fun beforeVfsChange() {
    }

    override fun afterVfsChange() {
        val settings = AppState.instance
        settings.pyprojectConf = parse(tomlFile)
    }

    companion object {
        @JvmStatic
        fun parse(tomlFile: VirtualFile): Map<String, String> {
            val config = Toml.parse(tomlFile.inputStream)
            val isortConfig = config["tool.isort"] as TomlTable?
            if (isortConfig?.isEmpty != false) {
                return mapOf()
            }
            return isortConfig.toMap().map { entry -> entry.key to entry.value.toString() }.toMap()
        }
    }
}