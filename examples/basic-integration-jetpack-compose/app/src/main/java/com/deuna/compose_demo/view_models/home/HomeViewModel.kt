package com.deuna.compose_demo.view_models.home

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.deuna.maven.*

const val ERROR_TAG = "❌ DeunaSDK"
const val DEBUG_TAG = "👀 DeunaSDK"

/**
 * ViewModel for the Home screen, responsible for handling user interactions and data manipulation.
 * @param deunaSDK The DeunaSDK instance used for payment and card saving operations.
 */
class HomeViewModel(val deunaSDK: DeunaSDK) : ViewModel() {

    // State variables for order token and user token
    val orderToken = mutableStateOf("")
    val userToken = mutableStateOf("")

    val userTokenValue: String?
        get() {
            return userToken.value.ifEmpty { null }
        }
}

