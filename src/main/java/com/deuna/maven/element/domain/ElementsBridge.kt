package com.deuna.maven.element.domain

import com.deuna.maven.*
import com.deuna.maven.shared.*
import com.deuna.maven.web_views.ElementsActivity
import org.json.*

@Suppress("UNCHECKED_CAST")
class ElementsBridge(
    private val activity: ElementsActivity,
    private val closeEvents: Set<ElementsEvent>,
) : WebViewBridge(name = "android") {
    override fun handleEvent(message: String) {
        try {


            val json = JSONObject(message).toMap()

            val type = json["type"] as? String
            val data = json["data"] as? Json

            if (type == null || data == null) {
                return
            }

            val event = ElementsEvent.valueOf(type)
            activity.callbacks?.eventListener?.invoke(event, data)

            when (event) {

                ElementsEvent.vaultSaveSuccess, ElementsEvent.cardSuccessfullyCreated -> {
                    activity.callbacks?.onSuccess?.invoke(data)
                }

                ElementsEvent.vaultFailed, ElementsEvent.cardCreationError, ElementsEvent.vaultSaveError -> {
                    val error = ElementsError.fromJson(
                        type = ElementsError.Type.VAULT_SAVE_ERROR,
                        data = data
                    )
                    if (error != null) {
                        activity.callbacks?.onError?.invoke(error)
                    }
                }

                ElementsEvent.vaultClosed -> {
                    closeElements(activity.sdkInstanceId!!)
                    activity.callbacks?.onCanceled?.invoke()
                }

                else -> {
                    DeunaLogs.debug("ElementsBridge Unhandled event: $event")
                }
            }

            if (closeEvents.contains(event)) {
                closeElements(activity.sdkInstanceId!!)
            }
        } catch (_: IllegalArgumentException) {
        } catch (e: Exception) {
            DeunaLogs.debug("ElementsBridge JSONException: $e")
        }
    }
}