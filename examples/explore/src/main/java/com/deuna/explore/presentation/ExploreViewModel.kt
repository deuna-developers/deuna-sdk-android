package com.deuna.explore.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.deuna.explore.R
import com.deuna.explore.data.ApmRepository
import com.deuna.explore.data.ConfigStorage
import com.deuna.explore.data.MerchantService
import com.deuna.explore.data.OrderTokenService
import com.deuna.explore.data.ProductCatalog
import com.deuna.explore.data.UpdateChecker
import com.deuna.explore.domain.*
import com.deuna.maven.DeunaSDK
import com.deuna.maven.generateFraudId
import com.deuna.maven.initCheckout
import com.deuna.maven.initElements
import com.deuna.maven.initNextAction
import com.deuna.maven.initPaymentWidget
import com.deuna.maven.initVoucherWidget
import com.deuna.maven.shared.CheckoutCallbacks
import com.deuna.maven.shared.ElementsCallbacks
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.widgets.configuration.AutoResizeConfig
import com.deuna.maven.widgets.configuration.CheckoutWidgetConfiguration
import com.deuna.maven.widgets.configuration.DeunaWidgetConfiguration
import com.deuna.maven.widgets.configuration.ElementsWidgetConfiguration
import com.deuna.maven.widgets.configuration.NextActionWidgetConfiguration
import com.deuna.maven.widgets.configuration.PaymentWidgetConfiguration
import com.deuna.maven.widgets.configuration.VoucherWidgetConfiguration
import com.deuna.maven.widgets.next_action.NextActionCallbacks
import com.deuna.maven.widgets.payment_widget.PaymentWidgetCallbacks
import com.deuna.maven.widgets.voucher.VoucherCallbacks
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

// ─── Navigation events (one-shot, like iOS navigationPath.append) ───────────

sealed class NavigationEvent {
    data class PaymentSuccess(val orderJson: String) : NavigationEvent()
    data class CardSavedSuccess(val cardJson: String) : NavigationEvent()
    data class OpenWallets(val orderToken: String?) : NavigationEvent()
    object OpenAutoResize : NavigationEvent()
}

// ─── UI State ────────────────────────────────────────────────────────────────

data class ExploreUiState(
    val appliedConfig: IntegrationConfig = IntegrationConfig.default,
    val draftConfig: DraftConfig = DraftConfig.from(IntegrationConfig.default),
    val products: List<ExploreProduct> = emptyList(),
    val selectedProductIds: Set<String> = emptySet(),
    val embeddedWidgetConfig: DeunaWidgetConfiguration? = null,
    val isShowingEmbeddedScreen: Boolean = false,
    val isApplyingConfiguration: Boolean = false,
    val isLaunchingModalWidget: Boolean = false,
    val isLaunchingWallets: Boolean = false,
    val keyErrorMessage: String? = null,
    val modalStatusMessage: String? = null,
    val fraudIdStatusMessage: String? = null,
    val isGeneratingFraudId: Boolean = false,
    val useManualOrderTokenFlow: Boolean = false,
    val apmOptions: List<ApmOption> = emptyList(),
    val isLoadingApms: Boolean = false,
    val isLaunchingFormularios: Boolean = false,
    val generatedOrderToken: String? = null,
    val latestVersion: String? = null,
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class ExploreViewModel(
    private val configStorage: ConfigStorage,
    private val merchantService: MerchantService = MerchantService(),
    private val orderTokenService: OrderTokenService = OrderTokenService(),
    val githubRepo: String = "",
    private val appVersion: String = "",
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    var deunaSDK: DeunaSDK
        private set

    init {
        val savedConfig = configStorage.load(IntegrationConfig.default)
        deunaSDK = DeunaSDK(
            environment = savedConfig.environment.sdkEnvironment,
            publicApiKey = savedConfig.publicKey.ifEmpty { "YOUR_PUBLIC_API_KEY" },
        )
        deunaSDK.applyCustomWebViewSettings { settings ->
            if (WebViewFeature.isFeatureSupported(WebViewFeature.PAYMENT_REQUEST)) {
                WebSettingsCompat.setPaymentRequestEnabled(settings, true)
            }
        }
        _uiState.update { state ->
            state.copy(
                appliedConfig = savedConfig,
                draftConfig = DraftConfig.from(savedConfig),
                products = ProductCatalog.buildProducts(savedConfig.merchantCurrencyCode),
                useManualOrderTokenFlow = savedConfig.orderToken.isNotBlank(),
            )
        }

        viewModelScope.launch {
            val latest = runCatching {
                UpdateChecker.getLatestVersion(githubRepo)
            }.getOrNull()
            if (latest != null && latest != appVersion) {
                _uiState.update { it.copy(latestVersion = latest) }
            }
        }
    }

    // ─── Drawer actions ──────────────────────────────────────────────────────

    fun openDrawer() {
        _uiState.update { it.copy(draftConfig = DraftConfig.from(it.appliedConfig)) }
    }

    fun discardDraftChanges() {
        _uiState.update {
            it.copy(
                draftConfig = DraftConfig.from(it.appliedConfig),
                keyErrorMessage = null,
                modalStatusMessage = null,
                fraudIdStatusMessage = null,
            )
        }
    }

    fun updateDraftConfig(update: (DraftConfig) -> DraftConfig) {
        _uiState.update { it.copy(draftConfig = update(it.draftConfig)) }
    }

    fun applyConfiguration(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    keyErrorMessage = null,
                    modalStatusMessage = null,
                    fraudIdStatusMessage = null,
                    isApplyingConfiguration = true,
                )
            }

            val draft = _uiState.value.draftConfig
            val publicKey = draft.publicKey.trim()

            if (publicKey.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isApplyingConfiguration = false,
                        keyErrorMessage = "Public API Key is required.",
                    )
                }
                return@launch
            }

            val fraudProvidersJson = draft.fraudProvidersJson.trim()
            if (fraudProvidersJson.isNotEmpty()) {
                try {
                    JSONObject(fraudProvidersJson)
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isApplyingConfiguration = false,
                            keyErrorMessage = "Invalid fraud providers JSON.",
                        )
                    }
                    return@launch
                }
            }

            var newConfig = draft.toIntegrationConfig()
            val privateKey = newConfig.privateKey.trim()
            val hasManualOrderToken = newConfig.orderToken.isNotBlank()
            val useManual = hasManualOrderToken

            if (privateKey.isNotEmpty()) {
                val shouldTokenize = (newConfig.presentationMode == ExplorePresentationMode.EMBEDDED
                    || newConfig.presentationMode == ExplorePresentationMode.AUTO_RESIZE)
                    && !hasManualOrderToken

                try {
                    if (shouldTokenize) {
                        val products = ProductCatalog.buildProducts(newConfig.merchantCurrencyCode)
                        val tokenResult = orderTokenService.createOrderToken(newConfig.environment, privateKey, products)
                        newConfig = newConfig.copy(
                            orderToken = tokenResult.orderToken,
                            merchantName = tokenResult.merchantProfile.name,
                            merchantCountryCode = tokenResult.merchantProfile.countryCode,
                            merchantCurrencyCode = tokenResult.merchantProfile.currencyCode,
                        )
                    } else {
                        val profile = merchantService.loadMerchantProfile(newConfig.environment, privateKey)
                        newConfig = newConfig.copy(
                            merchantName = profile.name,
                            merchantCountryCode = profile.countryCode,
                            merchantCurrencyCode = profile.currencyCode,
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isApplyingConfiguration = false,
                            keyErrorMessage = e.message ?: "Failed to validate private key.",
                        )
                    }
                    return@launch
                }
            }

            deunaSDK = DeunaSDK(
                environment = newConfig.environment.sdkEnvironment,
                publicApiKey = publicKey,
            )

            configStorage.save(newConfig)

            _uiState.update {
                it.copy(
                    appliedConfig = newConfig,
                    draftConfig = DraftConfig.from(newConfig),
                    products = ProductCatalog.buildProducts(newConfig.merchantCurrencyCode),
                    selectedProductIds = emptySet(),
                    embeddedWidgetConfig = null,
                    isShowingEmbeddedScreen = false,
                    useManualOrderTokenFlow = useManual,
                    isApplyingConfiguration = false,
                )
            }

            onSuccess()
        }
    }

    // ─── Fraud ID generation ─────────────────────────────────────────────────

    fun generateFraudId(context: Context) {
        val draft = _uiState.value.draftConfig
        val publicKey = draft.publicKey.trim()
        if (publicKey.isEmpty()) {
            _uiState.update { it.copy(fraudIdStatusMessage = "Public API Key is required.") }
            return
        }

        val fraudJson = draft.fraudProvidersJson.trim()
        val params: Json = try {
            if (fraudJson.isEmpty()) emptyMap()
            else {
                val jObj = JSONObject(fraudJson)
                jObj.keys().asSequence().associate { key ->
                    val inner = jObj.getJSONObject(key)
                    key to inner.keys().asSequence().associate { k -> k to inner.get(k) }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(fraudIdStatusMessage = "Invalid fraud providers JSON.") }
            return
        }

        _uiState.update {
            it.copy(isGeneratingFraudId = true, fraudIdStatusMessage = null)
        }

        val tempSdk = DeunaSDK(
            environment = draft.environment.sdkEnvironment,
            publicApiKey = publicKey,
        )
        tempSdk.generateFraudId(context = context, params = params) { generatedId ->
            val fraudId = generatedId ?: ""
            val statusMsg = if (generatedId != null) "FraudId generated." else "Failed to generate FraudId."
            _uiState.update { state ->
                state.copy(
                    draftConfig = state.draftConfig.copy(fraudId = fraudId),
                    fraudIdStatusMessage = statusMsg,
                    isGeneratingFraudId = false,
                )
            }
        }
    }

    // ─── Widget launch ───────────────────────────────────────────────────────

    fun showModalWidget(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLaunchingModalWidget = true, modalStatusMessage = null)
            }

            val state = _uiState.value
            val config = state.appliedConfig

            val orderToken = resolveOrderToken(config, state)
            if (orderToken == null) {
                _uiState.update { it.copy(isLaunchingModalWidget = false) }
                return@launch
            }

            when (config.presentationMode) {
                ExplorePresentationMode.MODAL -> {
                    launchModalWidget(context, config, orderToken)
                    _uiState.update { it.copy(isLaunchingModalWidget = false) }
                }

                ExplorePresentationMode.EMBEDDED -> {
                    val widgetConfig = buildEmbeddedWidgetConfig(config, orderToken)
                    _uiState.update {
                        it.copy(
                            embeddedWidgetConfig = widgetConfig,
                            isShowingEmbeddedScreen = true,
                            isLaunchingModalWidget = false,
                        )
                    }
                }

                ExplorePresentationMode.AUTO_RESIZE -> {
                    val widgetConfig = buildEmbeddedWidgetConfig(config, orderToken, autoResizeConfig = AutoResizeConfig(initialHeightDp = 100))
                    _uiState.update {
                        it.copy(
                            embeddedWidgetConfig = widgetConfig,
                            isShowingEmbeddedScreen = false,
                            isLaunchingModalWidget = false,
                        )
                    }
                    _navigationEvents.send(NavigationEvent.OpenAutoResize)
                }
            }
        }
    }

    fun refreshEmbedded() {
        val state = _uiState.value
        if (state.appliedConfig.presentationMode != ExplorePresentationMode.EMBEDDED) return
        if (!state.isShowingEmbeddedScreen) return

        deunaSDK = DeunaSDK(
            environment = state.appliedConfig.environment.sdkEnvironment,
            publicApiKey = state.appliedConfig.publicKey,
        )

        _uiState.update { it.copy(embeddedWidgetConfig = null) }

        viewModelScope.launch {
            kotlinx.coroutines.delay(50)
            val config = _uiState.value.appliedConfig
            val token = config.orderToken
            if (token.isNotBlank()) {
                val widgetConfig = buildEmbeddedWidgetConfig(config, token)
                _uiState.update { it.copy(embeddedWidgetConfig = widgetConfig) }
            }
        }
    }

    fun refreshAutoResize() {
        val state = _uiState.value
        if (state.appliedConfig.presentationMode != ExplorePresentationMode.AUTO_RESIZE) return

        deunaSDK = DeunaSDK(
            environment = state.appliedConfig.environment.sdkEnvironment,
            publicApiKey = state.appliedConfig.publicKey,
        )

        _uiState.update { it.copy(embeddedWidgetConfig = null) }

        viewModelScope.launch {
            kotlinx.coroutines.delay(50)
            val config = _uiState.value.appliedConfig
            val token = config.orderToken
            if (token.isNotBlank()) {
                val widgetConfig = buildEmbeddedWidgetConfig(config, token, autoResizeConfig = AutoResizeConfig(initialHeightDp = 100))
                _uiState.update { it.copy(embeddedWidgetConfig = widgetConfig) }
            }
        }
    }

    fun submitEmbedded() {
        // Submit is called via DeunaWidget.submit() directly from the screen
    }

    fun showWallets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLaunchingWallets = true, modalStatusMessage = null) }

            val state = _uiState.value
            val config = state.appliedConfig

            val orderToken: String? = if (config.orderToken.isNotBlank()) {
                config.orderToken
            } else if (!state.useManualOrderTokenFlow) {
                if (state.selectedProductIds.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLaunchingWallets = false,
                            modalStatusMessage = "Select at least one product to continue.",
                        )
                    }
                    return@launch
                }
                val privateKey = config.privateKey.trim()
                if (privateKey.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLaunchingWallets = false,
                            modalStatusMessage = "Private Key is required to tokenize products.",
                        )
                    }
                    return@launch
                }
                try {
                    val selectedProducts =
                        state.products.filter { it.id in state.selectedProductIds }
                    val result = orderTokenService.createOrderToken(config.environment, privateKey, selectedProducts)
                    val updatedConfig = config.copy(
                        orderToken = result.orderToken,
                        merchantName = result.merchantProfile.name,
                        merchantCountryCode = result.merchantProfile.countryCode,
                        merchantCurrencyCode = result.merchantProfile.currencyCode,
                    )
                    configStorage.save(updatedConfig)
                    _uiState.update { it.copy(appliedConfig = updatedConfig, draftConfig = DraftConfig.from(updatedConfig)) }
                    result.orderToken
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLaunchingWallets = false,
                            modalStatusMessage = e.message ?: "Failed to create order token.",
                        )
                    }
                    return@launch
                }
            } else {
                null
            }

            _uiState.update { it.copy(isLaunchingWallets = false) }
            _navigationEvents.send(NavigationEvent.OpenWallets(orderToken))
        }
    }

    fun loadApmOptions() {
        if (_uiState.value.isLoadingApms) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApms = true) }
            val options = try {
                ApmRepository.fetchApmOptions()
            } catch (e: Exception) {
                emptyList()
            }
            _uiState.update { it.copy(apmOptions = options, isLoadingApms = false) }
        }
    }

    fun showFormularios(context: Context, apm: ApmOption) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLaunchingFormularios = true, modalStatusMessage = null) }

            val state = _uiState.value
            val config = state.appliedConfig

            val orderToken = resolveOrderToken(config, state)
            if (orderToken == null) {
                _uiState.update { it.copy(isLaunchingFormularios = false) }
                return@launch
            }

            val paymentMethods: List<com.deuna.maven.shared.Json> = listOf(
                mapOf("paymentMethod" to apm.paymentMethod, "processors" to listOf(apm.processor))
            )

            when (config.presentationMode) {
                ExplorePresentationMode.MODAL -> {
                    deunaSDK.initPaymentWidget(
                        context = context,
                        orderToken = orderToken,
                        userToken = tokenOrNull(config.userToken),
                        paymentMethods = paymentMethods,
                        behavior = splitPaymentBehavior(config.enableSplitPayment),
                        fraudCredentials = parseFraudCredentials(config.fraudProvidersJson),
                        callbacks = makePaymentCallbacks(),
                    )
                    _uiState.update { it.copy(isLaunchingFormularios = false) }
                }

                ExplorePresentationMode.EMBEDDED -> {
                    val widgetConfig = PaymentWidgetConfiguration(
                        sdkInstance = deunaSDK,
                        orderToken = orderToken,
                        userToken = tokenOrNull(config.userToken),
                        hidePayButton = config.hidePayButton,
                        paymentMethods = paymentMethods,
                        behavior = splitPaymentBehavior(config.enableSplitPayment),
                        fraudCredentials = parseFraudCredentials(config.fraudProvidersJson),
                        callbacks = makePaymentCallbacks(),
                    )
                    _uiState.update {
                        it.copy(
                            embeddedWidgetConfig = widgetConfig,
                            isShowingEmbeddedScreen = true,
                            isLaunchingFormularios = false,
                        )
                    }
                }

                ExplorePresentationMode.AUTO_RESIZE -> {
                    val widgetConfig = PaymentWidgetConfiguration(
                        sdkInstance = deunaSDK,
                        orderToken = orderToken,
                        userToken = tokenOrNull(config.userToken),
                        hidePayButton = config.hidePayButton,
                        paymentMethods = paymentMethods,
                        behavior = splitPaymentBehavior(config.enableSplitPayment),
                        fraudCredentials = parseFraudCredentials(config.fraudProvidersJson),
                        callbacks = makePaymentCallbacks(),
                        autoResizeConfig = AutoResizeConfig(initialHeightDp = 100),
                    )
                    _uiState.update {
                        it.copy(
                            embeddedWidgetConfig = widgetConfig,
                            isShowingEmbeddedScreen = false,
                            isLaunchingFormularios = false,
                        )
                    }
                    _navigationEvents.send(NavigationEvent.OpenAutoResize)
                }
            }
        }
    }

    fun toggleProductSelection(productId: String) {
        _uiState.update { state ->
            val updated = state.selectedProductIds.toMutableSet()
            if (productId in updated) updated.remove(productId) else updated.add(productId)
            state.copy(selectedProductIds = updated)
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private suspend fun resolveOrderToken(
        config: IntegrationConfig,
        state: ExploreUiState,
    ): String? {
        if (config.orderToken.isNotBlank()) return config.orderToken

        if (state.useManualOrderTokenFlow) {
            _uiState.update { it.copy(modalStatusMessage = "Order token is required.") }
            return null
        }

        if (state.selectedProductIds.isEmpty()) {
            _uiState.update { it.copy(modalStatusMessage = "Select at least one product to continue.") }
            return null
        }

        val privateKey = config.privateKey.trim()
        if (privateKey.isEmpty()) {
            _uiState.update { it.copy(modalStatusMessage = "Private Key is required to tokenize products.") }
            return null
        }

        return try {
            val selectedProducts = state.products.filter { it.id in state.selectedProductIds }
            val result = orderTokenService.createOrderToken(config.environment, privateKey, selectedProducts)
            val updatedConfig = config.copy(
                orderToken = result.orderToken,
                merchantName = result.merchantProfile.name,
                merchantCountryCode = result.merchantProfile.countryCode,
                merchantCurrencyCode = result.merchantProfile.currencyCode,
            )
            configStorage.save(updatedConfig)
            _uiState.update {
                it.copy(
                    appliedConfig = updatedConfig,
                    draftConfig = DraftConfig.from(updatedConfig),
                    generatedOrderToken = result.orderToken,
                )
            }
            result.orderToken
        } catch (e: Exception) {
            _uiState.update { it.copy(modalStatusMessage = e.message ?: "Failed to create order token.") }
            null
        }
    }

    fun clearGeneratedOrder() {
        val state = _uiState.value
        val updatedConfig = state.appliedConfig.copy(orderToken = "")
        configStorage.save(updatedConfig)
        _uiState.update {
            it.copy(
                appliedConfig = updatedConfig,
                draftConfig = DraftConfig.from(updatedConfig),
                generatedOrderToken = null,
                useManualOrderTokenFlow = false,
                selectedProductIds = emptySet(),
                modalStatusMessage = null,
            )
        }
    }

    private fun tokenOrNull(value: String): String? =
        value.trim().ifEmpty { null }

    private fun splitPaymentBehavior(enabled: Boolean): Json? {
        if (!enabled) return null
        return mapOf(
            "paymentMethods" to mapOf(
                "creditCard" to mapOf(
                    "splitPayments" to mapOf("maxCards" to 2),
                    "flow" to "purchase",
                )
            )
        )
    }

    private fun fallbackUserInfo(config: IntegrationConfig): UserInfo? {
        if (tokenOrNull(config.userToken) != null) return null
        val email = config.userInfoEmail.trim()
        if (email.isEmpty()) return null
        val firstName = config.userInfoFirstName.trim().ifEmpty { null }
        val lastName = config.userInfoLastName.trim().ifEmpty { null }
        return UserInfo(firstName = firstName, lastName = lastName, email = email)
    }

    private fun buildEmbeddedWidgetConfig(
        config: IntegrationConfig,
        orderToken: String,
        autoResizeConfig: AutoResizeConfig? = null,
    ): DeunaWidgetConfiguration {
        return when (config.selectedWidget) {
            ExploreWidget.PAYMENT_WIDGET -> PaymentWidgetConfiguration(
                sdkInstance = deunaSDK,
                orderToken = orderToken,
                userToken = tokenOrNull(config.userToken),
                hidePayButton = config.hidePayButton,
                behavior = splitPaymentBehavior(config.enableSplitPayment),
                fraudCredentials = parseFraudCredentials(config.fraudProvidersJson),
                callbacks = makePaymentCallbacks(),
                autoResizeConfig = autoResizeConfig,
            )
            ExploreWidget.CHECKOUT_WIDGET -> CheckoutWidgetConfiguration(
                sdkInstance = deunaSDK,
                orderToken = orderToken,
                userToken = tokenOrNull(config.userToken),
                hidePayButton = config.hidePayButton,
                callbacks = makeCheckoutCallbacks(),
                autoResizeConfig = autoResizeConfig,
            )
            ExploreWidget.VAULT_WIDGET -> ElementsWidgetConfiguration(
                sdkInstance = deunaSDK,
                userToken = tokenOrNull(config.userToken),
                orderToken = tokenOrNull(orderToken),
                hidePayButton = config.hidePayButton,
                behavior = splitPaymentBehavior(config.enableSplitPayment),
                userInfo = fallbackUserInfo(config),
                fraudCredentials = parseFraudCredentials(config.fraudProvidersJson),
                callbacks = makeElementsCallbacks(),
                autoResizeConfig = autoResizeConfig,
            )
            ExploreWidget.NEXT_ACTION_WIDGET -> NextActionWidgetConfiguration(
                sdkInstance = deunaSDK,
                orderToken = orderToken,
                hidePayButton = config.hidePayButton,
                callbacks = makeNextActionCallbacks(),
                autoResizeConfig = autoResizeConfig,
            )
            ExploreWidget.VOUCHER_WIDGET -> VoucherWidgetConfiguration(
                sdkInstance = deunaSDK,
                orderToken = orderToken,
                hidePayButton = config.hidePayButton,
                callbacks = makeVoucherCallbacks(),
                autoResizeConfig = autoResizeConfig,
            )
            ExploreWidget.CLICK_TO_PAY_WIDGET -> ElementsWidgetConfiguration(
                sdkInstance = deunaSDK,
                userToken = tokenOrNull(config.userToken),
                orderToken = tokenOrNull(orderToken),
                hidePayButton = config.hidePayButton,
                userInfo = fallbackUserInfo(config),
                types = listOf(mapOf("name" to "clickToPay")),
                callbacks = makeElementsCallbacks(),
                autoResizeConfig = autoResizeConfig,
            )
        }
    }

    private fun launchModalWidget(context: Context, config: IntegrationConfig, orderToken: String) {
        when (config.selectedWidget) {
            ExploreWidget.PAYMENT_WIDGET -> deunaSDK.initPaymentWidget(
                context = context,
                orderToken = orderToken,
                userToken = tokenOrNull(config.userToken),
                behavior = splitPaymentBehavior(config.enableSplitPayment),
                fraudCredentials = parseFraudCredentials(config.fraudProvidersJson),
                callbacks = makePaymentCallbacks(),
            )
            ExploreWidget.CHECKOUT_WIDGET -> deunaSDK.initCheckout(
                context = context,
                orderToken = orderToken,
                userToken = tokenOrNull(config.userToken),
                callbacks = makeCheckoutCallbacks(),
            )
            ExploreWidget.VAULT_WIDGET -> deunaSDK.initElements(
                context = context,
                userToken = tokenOrNull(config.userToken),
                orderToken = tokenOrNull(orderToken),
                behavior = splitPaymentBehavior(config.enableSplitPayment),
                userInfo = fallbackUserInfo(config),
                fraudCredentials = parseFraudCredentials(config.fraudProvidersJson),
                callbacks = makeElementsCallbacks(),
            )
            ExploreWidget.NEXT_ACTION_WIDGET -> deunaSDK.initNextAction(
                context = context,
                orderToken = orderToken,
                callbacks = makeNextActionCallbacks(),
            )
            ExploreWidget.VOUCHER_WIDGET -> deunaSDK.initVoucherWidget(
                context = context,
                orderToken = orderToken,
                callbacks = makeVoucherCallbacks(),
            )
            ExploreWidget.CLICK_TO_PAY_WIDGET -> deunaSDK.initElements(
                context = context,
                userToken = tokenOrNull(config.userToken),
                orderToken = tokenOrNull(orderToken),
                userInfo = fallbackUserInfo(config),
                types = listOf(mapOf("name" to "clickToPay")),
                callbacks = makeElementsCallbacks(),
            )
        }
    }

    // ─── Callback factories ───────────────────────────────────────────────────

    private fun makePaymentCallbacks() = PaymentWidgetCallbacks().apply {
        onSuccess = { data -> routePaymentSuccess(data) }
        onError = { error -> android.util.Log.e("Explore", "Payment error: ${error.metadata?.message}") }
        onClosed = { android.util.Log.d("Explore", "Widget closed") }
        onEventDispatch = { event, data -> android.util.Log.d("Explore", "PaymentWidget event=${event} data=$data") }
    }

    private fun makeCheckoutCallbacks() = CheckoutCallbacks().apply {
        onSuccess = { data -> routePaymentSuccess(data) }
        onError = { error -> android.util.Log.e("Explore", "Checkout error: ${error.metadata?.message}") }
        onClosed = { android.util.Log.d("Explore", "Widget closed") }
        onEventDispatch = { event, data -> android.util.Log.d("Explore", "Checkout event=${event} data=$data") }
    }

    private fun makeElementsCallbacks() = ElementsCallbacks().apply {
        onSuccess = { data ->
            @Suppress("UNCHECKED_CAST")
            val cardData = ((data["metadata"] as? Json)?.get("createdCard") as? Json) ?: data
            routeCardSavedSuccess(cardData)
        }
        onError = { error -> android.util.Log.e("Explore", "Elements error: ${error.metadata?.message}") }
        onClosed = { android.util.Log.d("Explore", "Widget closed") }
        onEventDispatch = { event, data -> android.util.Log.d("Explore", "Elements event=${event} data=$data") }
    }

    private fun makeNextActionCallbacks() = NextActionCallbacks().apply {
        onSuccess = { data -> routePaymentSuccess(data) }
        onError = { error -> android.util.Log.e("Explore", "NextAction error: ${error.metadata?.message}") }
        onEventDispatch = { event, data -> android.util.Log.d("Explore", "NextAction event=${event} data=$data") }
    }

    private fun makeVoucherCallbacks() = VoucherCallbacks().apply {
        onSuccess = { data -> routePaymentSuccess(data) }
        onError = { error -> android.util.Log.e("Explore", "Voucher error: ${error.metadata?.message}") }
        onEventDispatch = { event, data -> android.util.Log.d("Explore", "Voucher event=${event} data=$data") }
    }

    private fun routePaymentSuccess(data: Json) {
        deunaSDK.close {
            val json = try { org.json.JSONObject(data).toString() } catch (e: Exception) { data.toString() }
            viewModelScope.launch {
                _uiState.update { it.copy(isShowingEmbeddedScreen = false, embeddedWidgetConfig = null) }
                _navigationEvents.send(NavigationEvent.PaymentSuccess(json))
            }
        }
    }

    private fun routeCardSavedSuccess(data: Json) {
       deunaSDK.close {
           val json = try { org.json.JSONObject(data).toString() } catch (e: Exception) { data.toString() }
           viewModelScope.launch {
               _uiState.update { it.copy(isShowingEmbeddedScreen = false, embeddedWidgetConfig = null) }
               _navigationEvents.send(NavigationEvent.CardSavedSuccess(json))
           }
       }
    }

    private fun parseFraudCredentials(fraudProvidersJson: String): Json? {
        val trimmed = fraudProvidersJson.trim()
        if (trimmed.isEmpty()) return null
        return try {
            val jObj = JSONObject(trimmed)
            jObj.keys().asSequence().associate { key ->
                val inner = jObj.getJSONObject(key)
                key to inner.keys().asSequence().associate { k -> k to inner.get(k) }
            }
        } catch (e: Exception) {
            null
        }
    }

    // ─── Factory ─────────────────────────────────────────────────────────────

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val githubRepo = context.getString(R.string.github_repo)
            val appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
            } catch (e: Exception) { "1.0" }
            @Suppress("UNCHECKED_CAST")
            return ExploreViewModel(
                configStorage = ConfigStorage(context.applicationContext),
                githubRepo = githubRepo,
                appVersion = appVersion,
            ) as T
        }
    }
}
