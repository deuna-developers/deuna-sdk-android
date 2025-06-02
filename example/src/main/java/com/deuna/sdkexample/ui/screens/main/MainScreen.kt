package com.deuna.sdkexample.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.deuna.maven.DeunaSDK
import com.deuna.maven.shared.Environment
import com.deuna.sdkexample.shared.views.Separator
import com.deuna.sdkexample.ui.screens.main.utils.showWidgetInModal
import com.deuna.sdkexample.ui.screens.main.view_model.MainViewModel
import com.deuna.sdkexample.ui.screens.main.view_model.extensions.generateFraudId
import com.deuna.sdkexample.ui.screens.main.views.Inputs
import com.deuna.sdkexample.ui.screens.main.views.ViewModePicker


enum class ViewMode(val label: String) {
    MODAL("Modal"),
    EMBEDDED("Embedded");
}

enum class WidgetToShow(val label: String) {
    PAYMENT_WIDGET("Payment Widget"),
    NEXT_ACTION_WIDGET("Next Action Widget"),
    VOUCHER_WIDGET("Voucher Widget"),
    CHECKOUT_WIDGET("Checkout Widget"),
    VAULT_WIDGET("Vault Widget"),
    CLICK_TO_PAY_WIDGET("Click to Pay Widget"),
    ;
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    navController: NavHostController
) {
    // Retrieve the user token and order token states from the view model
    val userTokenState = viewModel.userToken
    val orderTokenState = viewModel.orderToken
    val fraudId = viewModel.fraudId

    // Retrieve the Context from the composition's LocalContext
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var selectedViewMode by remember { mutableStateOf(ViewMode.MODAL) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Inputs(
                orderTokenState = orderTokenState,
                userTokenState = userTokenState
            )
            Separator(10.dp)

            Text("Fraud ID: ${fraudId.value}")



            Separator(20.dp)
            Button(
                onClick = {
                    viewModel.generateFraudId(context)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                )
            ) {
                Text("Generate Fraud ID")
            }
            Separator(20.dp)

            ViewModePicker(
                selectedViewMode = selectedViewMode
            ) { selectedViewMode = it }

            Separator(30.dp)



            WidgetToShow.entries.forEach { widget ->
                Button(
                    onClick = {
                        when (selectedViewMode) {
                            ViewMode.MODAL -> {
                                showWidgetInModal(
                                    context = context,
                                    viewModel = viewModel,
                                    widgetToShow = widget,
                                    navController = navController
                                )
                            }

                            ViewMode.EMBEDDED -> {
                                navController.navigate("embedded/${orderTokenState.value}/${userTokenState.value}/${widget.name}")
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF)
                    )
                ) {
                    Text(widget.label)
                }
            }

        }
    }
}

@Preview(showBackground = true, name = "MyScreen Preview")
@Composable
fun MyScreenPreview() {
    MainScreen(
        viewModel = MainViewModel(
            deunaSDK = DeunaSDK(
                environment = Environment.SANDBOX,
                publicApiKey = "FAKE_API_KEY"
            )
        ),
        navController = NavHostController(LocalContext.current)
    )
}