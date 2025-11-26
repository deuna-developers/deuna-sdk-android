package com.deuna.maven.widgets.configuration

import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import java.net.URL

sealed class DeunaWidgetConfiguration(
    val sdkInstance: DeunaSDK,
    val hidePayButton: Boolean,
    val fraudCredentials: Json? = null,
    val domain: String? = null,
) {
    var onCloseByUser: (() -> Unit)? = null
    abstract val link: String


    /**
     * Overrides the domain in the link with the provided domain.
     * If the domain contains https:// or http://, it will replace the entire link.
     * Otherwise, it will only replace the domain part of the link.
     */
    fun overrideBaseUrl(baseUrl: String, replaceWith: String): String {
        try { // extract the domain from the link
            val url = URL(baseUrl)

            // first check if replaceWith contains https:// or http://
            if (replaceWith.startsWith("http://") || replaceWith.startsWith("https://")) {
                val originalHost = if (url.port != -1) {
                    "${url.host}:${url.port}"
                } else {
                    url.host
                }

                return baseUrl.replace("${url.protocol}://$originalHost", replaceWith)
            }

            // For domain without protocol, preserve original protocol and port
            val originalHost = if (url.port != -1) {
                "${url.host}:${url.port}"
            } else {
                url.host
            }

            return baseUrl.replace(originalHost, replaceWith)
        } catch (e: Exception) {
            DeunaLogs.error(e.message ?: "")
            return baseUrl
        }
    }
}
