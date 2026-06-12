package com.deuna.maven.internal.modal

import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal object DeunaWidgetModalRegistry {
    private val registry = ConcurrentHashMap<String, DeunaWidgetConfiguration>()

    fun register(configuration: DeunaWidgetConfiguration): String {
        val id = UUID.randomUUID().toString()
        registry[id] = configuration
        return id
    }

    fun get(id: String): DeunaWidgetConfiguration? {
        return registry[id]
    }

    fun remove(id: String) {
        registry.remove(id)
    }
}
