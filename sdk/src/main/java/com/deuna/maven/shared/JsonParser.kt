@file:Suppress("UNCHECKED_CAST")

package com.deuna.maven.shared

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Convert a JSONObject to a Map<String, Any>
 */
@Throws(JSONException::class)
fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keysItr: Iterator<String> = this.keys()
    while (keysItr.hasNext()) {
        val key = keysItr.next()
        var value: Any = this.get(key)
        when (value) {
            is JSONArray -> value = value.toList()
            is JSONObject -> value = value.toMap()
        }
        map[key] = value
    }
    return map
}

/**
 * Convert a JSONArray to a List<JSON>
 */
@Throws(JSONException::class)
fun JSONArray.toList(): List<Any> {
    val list = mutableListOf<Any>()
    for (i in 0 until this.length()) {
        var value: Any = this[i]
        when (value) {
            is JSONArray -> value = value.toList()
            is JSONObject -> value = value.toMap()
        }
        list.add(value)
    }
    return list
}

/**
 * Convert a List<Json> to a base64 string
 */
fun List<Json>.toBase64(): String {
    val jsonArray = JSONArray(this)
    val jsonString = jsonArray.toString()
    return encodeBase64(jsonString.toByteArray(Charsets.UTF_8))
}

/**
 * Convert a Map<String,Any> to a JSONObject instance
 */
fun Json.toJSONObject(): JSONObject {
    val jsonObject = JSONObject()
    for ((key, value) in this) {
        when (value) {
            is Map<*, *> -> jsonObject.put(key, (value as Map<String, Any>).toJSONObject())
            is List<*> -> jsonObject.put(key, JSONArray(value))
            else -> jsonObject.put(key, value)
        }
    }
    return jsonObject
}

/**
 * Convert a Map<String,Any> to a base64 string
 */
fun Json.toBase64(): String {
    val json = this.toJSONObject()
    val jsonString = json.toString()
    return encodeBase64(jsonString.toByteArray(Charsets.UTF_8))
}

/**
 * Convert a byteArray to a base64 string
 */
private fun encodeBase64(bytes: ByteArray): String {
    val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    // Base64 characters table
    val result = StringBuilder()
    var i = 0
    while (i < bytes.size) {
        val byteChunk = (bytes[i].toInt() and 0xff) shl 16 or
                (if (i + 1 < bytes.size) (bytes[i + 1].toInt() and 0xff) shl 8 else 0) or
                (if (i + 2 < bytes.size) (bytes[i + 2].toInt() and 0xff) else 0)

        result.append(base64Chars[(byteChunk shr 18) and 0x3f])
        result.append(base64Chars[(byteChunk shr 12) and 0x3f])
        result.append(if (i + 1 < bytes.size) base64Chars[(byteChunk shr 6) and 0x3f] else '=')
        result.append(if (i + 2 < bytes.size) base64Chars[byteChunk and 0x3f] else '=')

        i += 3
    }

    return result.toString()
}