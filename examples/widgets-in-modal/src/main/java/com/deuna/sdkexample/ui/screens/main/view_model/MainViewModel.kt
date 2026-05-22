package com.deuna.sdkexample.ui.screens.main.view_model

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.deuna.maven.*
import com.deuna.sdkexample.ui.screens.main.WidgetToShow

const val ERROR_TAG = "❌ DeunaSDK"
const val DEBUG_TAG = "👀 DeunaSDK"

/**
 * ViewModel for the Home screen, responsible for handling user interactions and data manipulation.
 * @param deunaSDK The DeunaSDK instance used for payment and card saving operations.
 */
class MainViewModel(
    val deunaSDK: DeunaSDK,
    val orderToken: String,
    val userToken: String,
) : ViewModel()

