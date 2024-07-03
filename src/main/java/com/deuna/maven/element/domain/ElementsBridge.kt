package com.deuna.maven.element.domain

import ElementsResponse
import android.content.Context
import com.deuna.maven.*
import com.deuna.maven.shared.*
import org.json.*

 class ElementsBridge(
    private val context: Context,
    private val callbacks: ElementsCallbacks?,
    private val closeEvents: Set<ElementsEvent>,
 ) : WebViewBridge() {
    override fun handleEvent(message: String) {
        try {
            val json = JSONObject(message)
            val eventData = ElementsResponse.fromJson(json)

           callbacks?.eventListener?.invoke(eventData.type, eventData)
            when (eventData.type) {

                ElementsEvent.vaultSaveSuccess, ElementsEvent.cardSuccessfullyCreated -> {
                    callbacks?.onSuccess?.invoke((eventData))
                }

                ElementsEvent.vaultFailed, ElementsEvent.cardCreationError, ElementsEvent.vaultSaveError -> eventData.data.metadata?.let {
                    handleError(
                        eventData
                    )
                }

                ElementsEvent.vaultClosed -> {
                    closeElements()
                    callbacks?.onCanceled?.invoke()
                }

                else -> {
                    DeunaLogs.debug("ElementsBridge Unhandled event: ${eventData.type}")
                }
            }
            eventData.let {
                if (closeEvents.contains(it.type)) {
                    closeElements()
                }
            }
        } catch (e: Exception) {
            DeunaLogs.debug("ElementsBridge JSONException: $e")
        }
    }

    private fun handleError(response: ElementsResponse) {
        callbacks?.onError?.invoke(
            ElementsError(
                ElementsErrorType.VAULT_SAVE_ERROR,
                response.data.user
            )
        )
    }
}