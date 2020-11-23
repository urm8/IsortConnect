package com.github.urm8.isortconnect.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.Nullable

@State(
        name = "com.github.urm8.isortconnect.settings.AppState",
        storages = [Storage("iSortConnect.xml")]
)
class IsortConnectService(private val project: Project) :
        PersistentStateComponent<IsortConnectService.State> {

    @Nullable
    override fun getState(): State = instance

    override fun loadState(p0: State) {
        XmlSerializerUtil.copyBean(p0, instance)
    }

    data class State(
            var url: String = DEFAULT_URL,
            var triggerOnSave: Boolean = DEFAULT_TRIGGER_ON_SAVE,
            var pyprojectToml: String = "",
            var pyprojectConf: Map<String, String>? = null
    )

    companion object {
        val instance = State()
        const val DEFAULT_URL = "localhost:47393"
        const val DEFAULT_TRIGGER_ON_SAVE = true
    }
}
