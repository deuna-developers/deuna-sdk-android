package com.deuna.sdkexample.ui.screens.main.view_model.extensions

import android.content.Context
import android.util.Log
import com.deuna.maven.generateFraudId
import com.deuna.sdkexample.ui.screens.main.view_model.DEBUG_TAG
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel


fun MainViewModel.generateFraudId(
    context: Context
) {
    deunaSDK.generateFraudId(
        context = context,
        params = mapOf(
            "RISKIFIED" to mapOf(
                "storeDomain" to "deuna.com"
            )
        ),
        callback = {
            Log.i(DEBUG_TAG, "fraudId: $it")
            fraudId.value = it ?: "ERROR"
        }
    )
}