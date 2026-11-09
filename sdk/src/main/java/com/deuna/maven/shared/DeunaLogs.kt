package com.deuna.maven.shared

import android.util.Log

class DeunaLogs {

  companion object {
    private const val TAG = "DeunaSDK"
    var isEnabled = true

    fun debug(message: String) {
      logMessage(Log.DEBUG, message)
    }

    fun info(message: String) {
      logMessage(Log.INFO, message)
    }

    fun warning(message: String) {
      logMessage(Log.WARN, message)
    }

    fun error(message: String, throwable: Throwable? = null) {
      if (!isEnabled) return
      logMessage(Log.ERROR, message, throwable)
    }

    private fun logMessage(level: Int, message: String, throwable: Throwable? = null) {
      if (!isEnabled) return
      val formattedMessage = "[${getLogLevelString(level)}] - $message"
      when (throwable) {
        null -> Log.println(level, TAG, formattedMessage)
        else -> Log.println(level, TAG, formattedMessage + ": ${throwable.message}")
      }
    }

    private fun getLogLevelString(level: Int): String {
      return when (level) {
        Log.DEBUG -> "DEBUG"
        Log.INFO -> "INFO"
        Log.WARN -> "WARNING"
        Log.ERROR -> "ERROR"
        else -> "UNKNOWN"
      }
    }
  }


}