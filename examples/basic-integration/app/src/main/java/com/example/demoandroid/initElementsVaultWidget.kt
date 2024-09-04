package com.example.demoandroid

import android.content.Intent
import android.util.Log
import com.deuna.maven.initElements
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.example.demoandroid.screens.SaveCardSuccessfulActivity
import org.json.JSONObject

/**
 * Show the vault widget that stores credit and debit cards.
 */
fun MainActivity.initElementsVaultWidget() {
    deunaSdk.initElements(
        context = this,
        userToken = userToken,
        userInfo = if (userToken == null) UserInfo(
            firstName = "Darwin",
            lastName = "Morocho",
            email = "dmorocho@deuna.com",
        ) else null,
        callbacks = ElementsCallbacks().apply {
            onSuccess = { data ->
                deunaSdk.close()
                Intent(context, SaveCardSuccessfulActivity::class.java).apply {
                    putExtra(
                        SaveCardSuccessfulActivity.ARGUMENTS_DATA,
                        JSONObject(
                            mapOf(
                                "title" to "Card saved successfully",
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
            onError = {
                Log.e(ERROR_TAG, it.type.message)
                deunaSdk.close()
            }
            onCanceled = {
                Log.d(DEBUG_TAG, "Saving card was canceled by user")
            }
            onClosed = {
                Log.d(DEBUG_TAG, "Widget was closed")
            }
        },
    )
}