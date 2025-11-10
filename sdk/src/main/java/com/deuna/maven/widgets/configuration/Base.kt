package com.deuna.maven.widgets.configuration

import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Json

sealed class DeunaWidgetConfiguration(
    val sdkInstance: DeunaSDK,
    val hidePayButton: Boolean,
    val fraudCredentials: Json? = null,
) {
    var onCloseByUser: (() -> Unit)? = null
    abstract val link: String
}
