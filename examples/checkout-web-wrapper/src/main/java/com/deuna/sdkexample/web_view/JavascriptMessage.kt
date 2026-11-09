package com.deuna.sdkexample.web_view

typealias Json = Map<String, Any>

sealed class JavascriptMessage {
    data class OnSuccess(val payload: Json, val widgetType: String) : JavascriptMessage()
    data class OnEventDispatch(val payload: Json, val widgetType: String) : JavascriptMessage()
    data class OnError(val payload: Json, val widgetType: String) : JavascriptMessage()
}