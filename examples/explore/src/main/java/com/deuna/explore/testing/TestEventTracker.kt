package com.deuna.explore.testing

import java.util.concurrent.CopyOnWriteArrayList

data class TrackedEvent(
    val type: String,
    val data: Any? = null,
    val timestampMs: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
)

/**
 * Thread-safe event bus that captures callbacks and bridge events dispatched
 * by DEUNA widgets during integration test execution.
 */
object TestEventTracker {
    private val _events = CopyOnWriteArrayList<TrackedEvent>()

    val events: List<TrackedEvent>
        get() = _events

    fun recordEvent(type: String, data: Any? = null, isError: Boolean = false) {
        _events.add(TrackedEvent(type = type, data = data, isError = isError))
    }

    fun recordError(type: String, error: Any?) {
        recordEvent(type = type, data = error, isError = true)
    }

    fun recordSuccess(type: String, data: Any?) {
        recordEvent(type = type, data = data, isError = false)
    }

    fun hasEvent(vararg types: String): Boolean {
        return _events.any { it.type in types }
    }

    fun findFirst(vararg types: String): TrackedEvent? {
        return _events.firstOrNull { it.type in types }
    }

    fun getErrors(): List<TrackedEvent> {
        return _events.filter { it.isError }
    }

    fun dumpEvents(): String {
        if (_events.isEmpty()) return "[No events recorded]"
        return _events.joinToString(separator = "\n") { event ->
            val tag = if (event.isError) "❌ ERROR" else "ℹ️ EVENT"
            "  [$tag] ${event.type} -> ${event.data ?: "{}"}"
        }
    }

    fun clear() {
        _events.clear()
    }
}
