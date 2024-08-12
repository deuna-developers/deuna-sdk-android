package com.deuna.maven.shared

import android.net.Uri

class Utils {
    companion object {
        /**
         * Build an URL with a set of query parameters
         */
        fun buildUrl(baseUrl: String, queryParams: Map<String, String>): String {
            val uriBuilder = Uri.parse(baseUrl).buildUpon()

            // Append query parameters
            for ((key, value) in queryParams) {
                uriBuilder.appendQueryParameter(key, value)
            }

            // Build the final URL
            return uriBuilder.build().toString()
        }
    }
}