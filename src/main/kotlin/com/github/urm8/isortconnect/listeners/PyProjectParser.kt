package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlTable

@Suppress("UNCHECKED_CAST")
class PyProjectParser(private val tomlFile: VirtualFile) : AsyncFileListener.ChangeApplier {

    override fun afterVfsChange() {
        val settings = IsortConnectService.instance
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
            val cfg = isortConfig.toMap().map { entry ->
                entry.key to if (entry.value is TomlArray) {
                    (entry.value as TomlArray).toList().joinToString(",") { elem -> elem.toString() }
                } else {
                    entry.value.toString()
                }
            }.toMap()
            return cfg
        }
    }
}
