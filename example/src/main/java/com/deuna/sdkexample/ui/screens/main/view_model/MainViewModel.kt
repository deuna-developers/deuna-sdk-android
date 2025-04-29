package com.deuna.sdkexample.ui.screens.main.view_model

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.deuna.maven.*

const val ERROR_TAG = "❌ DeunaSDK"
const val DEBUG_TAG = "👀 DeunaSDK"

/**
 * ViewModel for the Home screen, responsible for handling user interactions and data manipulation.
 * @param deunaSDK The DeunaSDK instance used for payment and card saving operations.
 */
class MainViewModel(val deunaSDK: DeunaSDK) : ViewModel() {

    // State variables for order token and user token
    val orderToken = mutableStateOf("")
    val userToken = mutableStateOf("")
    val fraudId = mutableStateOf("")

    val orderTokenValue: String?
        get() {
            return orderToken.value.ifEmpty { null }
        }

    val userTokenValue: String?
        get() {
            return userToken.value.ifEmpty { null }
        }
}

