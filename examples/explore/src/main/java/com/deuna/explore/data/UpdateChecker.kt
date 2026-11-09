package com.deuna.explore.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    suspend fun getLatestVersion(githubRepo: String): String? = withContext(Dispatchers.IO) {
        if (githubRepo.isBlank()) return@withContext null
        val url = URL("https://api.github.com/repos/$githubRepo/releases/latest")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        if (conn.responseCode != 200) return@withContext null
        val json = JSONObject(conn.inputStream.bufferedReader().readText())
        json.optString("tag_name").removePrefix("v").ifBlank { null }
    }
}
