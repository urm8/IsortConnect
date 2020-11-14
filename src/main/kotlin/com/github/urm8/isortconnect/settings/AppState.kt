package com.github.urm8.isortconnect.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.Nullable

@State(
    name = "com.github.urm8.isortconnect.settings.AppState",
    storages = [Storage("iSortConnect.xml")]
)
data class AppState(
        var url: String = DEFAULT_URL,
        var triggerOnSave: Boolean = DEFAULT_TRIGGER_ON_SAVE,
        var pyprojectToml: String = "",
        var pyprojectConf: Map<String, String>? = null
) :
    PersistentStateComponent<AppState> {
    @Nullable
    override fun getState(): AppState = this

    override fun loadState(p0: AppState) {
        XmlSerializerUtil.copyBean(p0, this)
    }

    companion object {
        val instance: AppState = ServiceManager.getService(AppState::class.java)
        const val DEFAULT_URL = "localhost:47393"
        const val DEFAULT_TRIGGER_ON_SAVE = true
    }
}
