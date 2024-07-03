package com.deuna.maven.shared

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.deuna.maven.checkout.domain.CheckoutError
import com.deuna.maven.element.domain.ElementsError

class NetworkUtils(private val context: Context) {

    companion object {

        val ELEMENTS_NO_INTERNET_ERROR = ElementsError(
            ElementsErrorType.NO_INTERNET_CONNECTION,
            null,
        )
    }

    /// Checks if the device is connected to the internet.
    val hasInternet: Boolean
        get() {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return (networkCapabilities != null) && networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        }
}