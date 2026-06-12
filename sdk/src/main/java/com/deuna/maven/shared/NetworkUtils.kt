package com.deuna.maven.shared

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkUtils(private val context: Context) {

    /// Checks if the device is connected to the internet.
    val hasInternet: Boolean
        get() {
            val cmg = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+
                cmg.getNetworkCapabilities(cmg.activeNetwork)?.let { networkCapabilities ->
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                }
            } else {
                return cmg.activeNetworkInfo?.isConnectedOrConnecting == true
            }
            return false
        }
}