package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.settings.IsortConnectService
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlFileType
import org.toml.lang.psi.TomlTable

class PyProjectParser(private val tomlFile: VirtualFile) : AsyncFileListener.ChangeApplier {

    override fun afterVfsChange() {
        val settings = IsortConnectService.instance
        settings.pyprojectConf = parse(tomlFile)
    }

    companion object {
        const val TIMEOUT = 500

        @JvmStatic
        fun parse(tomlFile: VirtualFile): Map<String, String> {
            val dataContext = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(TIMEOUT)
            val project = dataContext?.getData(CommonDataKeys.PROJECT) ?: return mapOf()

            val cfg = mutableMapOf<String, String>()
            val psi = PsiManager.getInstance(project).findFile(tomlFile)
            if (psi != null && psi.fileType == TomlFileType && psi is TomlFile) {
                for (
                    entry in psi.findChildrenByClass(TomlTable::class.java)
                        .filter { elem -> elem.header.textMatches("[tool.isort]") }
                        .flatMap { elem -> elem.entries }
                        .filterNot { elem -> elem.value?.text.isNullOrBlank() }
                ) {
                    cfg[entry.key.text] = entry.value!!.text
                }
            }
            return cfg
        }
    }
}
