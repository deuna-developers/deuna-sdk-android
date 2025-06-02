package com.deuna.maven.widgets.configuration

import com.deuna.maven.DeunaSDK

sealed class DeunaWidgetConfiguration(
    val sdkInstance: DeunaSDK,
    val hidePayButton: Boolean,
) {
    var onCloseByUser: (() -> Unit)? = null
    abstract val link: String
}
