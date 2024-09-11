package com.example.demoandroid

import android.content.Intent
import android.util.Log
import com.deuna.maven.element.domain.ElementsError
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.ElementsWidget
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.example.demoandroid.screens.SaveCardSuccessfulActivity
import org.json.JSONObject

/**
 * Show the widget that processes a payment using Click to Pay.
 */
fun MainActivity.clickToPay() {
    deunaSdk.initElements(
        context = this,
        userInfo = UserInfo(
            // required for click_to_pay
            firstName = "Darwin",
            lastName = "Morocho",
            email = "dmorocho+3@deuna.com",
        ),
        types = listOf(
            mapOf(
                "name" to ElementsWidget.CLICK_TO_PAY
            )
        ),
        callbacks = ElementsCallbacks().apply {
            onSuccess = { data ->
                deunaSdk.close()
                Intent(context, SaveCardSuccessfulActivity::class.java).apply {
                    putExtra(
                        SaveCardSuccessfulActivity.ARGUMENTS_DATA,
                        JSONObject(
                            mapOf(
                                "title" to "Click To Pay enrollment successful",
                                "savedCardData"  to (data["metadata"] as Json)["createdCard"] as Json
                            )
                        ).toString()
                    )
                    startActivity(this)
                }
            }
            onEventDispatch = { type, data ->
                Log.d(DEBUG_TAG, "onEventDispatch ${type.name}: $data")
            }
            onError = { error ->
                Log.e(ERROR_TAG, error.type.message)
                Log.e(ERROR_TAG, error.metadata?.code ?: "")
                Log.e(ERROR_TAG, error.metadata?.message ?: "")

                if (error.type == ElementsError.Type.INITIALIZATION_FAILED) {
                    deunaSdk.close()
                }
            }
            onClosed = {
                Log.d(DEBUG_TAG, "Widget was closed")
            }
        },
    )
}