package com.deuna.maven.web_views.base

import com.deuna.maven.web_views.DeunaWebViewActivity


inline fun <reified T : Enum<T>> DeunaWebViewActivity.parseCloseEvents(closeEventAsListString: List<String>): Set<T> {
    // Use `T` as the generic type for the enum
    return closeEventAsListString.mapNotNull { stringValue ->
        try {
            // Use `enumValueOf<T>` to get the enum value of type `T`
            enumValueOf<T>(stringValue)
        } catch (e: IllegalArgumentException) {
            null // Ignore invalid enum constant names
        }
    }.toSet()
}
