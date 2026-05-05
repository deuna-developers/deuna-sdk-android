package com.deuna.maven.shared

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

enum class HttpMethod { GET, POST, PUT, PATCH, DELETE }

object DeunaHttpClient {

    private val client = OkHttpClient()

    fun request(
        method: HttpMethod,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: JSONObject? = null,
    ): JSONObject {
        val requestBody = when (method) {
            HttpMethod.GET, HttpMethod.DELETE -> null
            else -> (body?.toString() ?: "").toRequestBody("application/json".toMediaType())
        }

        val request = Request.Builder()
            .url(url)
            .apply { headers.forEach { (k, v) -> header(k, v) } }
            .method(method.name, requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty response (HTTP ${response.code})")

        DeunaLogs.info("[http] ${method.name} ${request.url} → ${response.code}\n${JSONObject(responseBody).toString(2)}")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }

        return JSONObject(responseBody)
    }

    fun get(url: String, headers: Map<String, String> = emptyMap()) =
        request(HttpMethod.GET, url, headers)

    fun post(url: String, headers: Map<String, String> = emptyMap(), body: JSONObject? = null) =
        request(HttpMethod.POST, url, headers, body)

    fun put(url: String, headers: Map<String, String> = emptyMap(), body: JSONObject? = null) =
        request(HttpMethod.PUT, url, headers, body)

    fun patch(url: String, headers: Map<String, String> = emptyMap(), body: JSONObject? = null) =
        request(HttpMethod.PATCH, url, headers, body)

    fun delete(url: String, headers: Map<String, String> = emptyMap()) =
        request(HttpMethod.DELETE, url, headers)
}
