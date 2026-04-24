package com.deuna.maven.internal.modal

import com.deuna.maven.web_views.deuna.DeunaWidget

internal interface DeunaModalHost {
    val deunaWidget: DeunaWidget
    fun dismiss()
}
