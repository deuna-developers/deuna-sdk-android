package com.deuna.explore.data

import android.content.Context
import com.deuna.explore.domain.IntegrationConfig

class ConfigStorage(context: Context) {
    private val prefs = context.getSharedPreferences("explore_config", Context.MODE_PRIVATE)
    private val CONFIG_KEY = "integration_config"

    fun load(default: IntegrationConfig): IntegrationConfig {
        val json = prefs.getString(CONFIG_KEY, null) ?: return default
        return IntegrationConfig.fromJson(json)
    }

    fun save(config: IntegrationConfig) {
        prefs.edit().putString(CONFIG_KEY, config.toJson()).apply()
    }
}
