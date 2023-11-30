package com.deuna.maven.element.domain

import ElementResponse
import android.app.Activity
import android.sax.Element
import org.json.JSONObject

class ElementCallbacks {
    var onSuccess: ((ElementResponse) -> Unit)? = null
    var onError: ((ElementErrorMessage?) -> Unit)? = null
    var onClose: ((Activity) -> Unit)? = null
    var eventListener: ((ElementResponse) -> Unit)? = null
}
