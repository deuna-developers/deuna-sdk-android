package com.deuna.explore.presentation.screens.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deuna.explore.domain.DraftConfig
import com.deuna.explore.domain.ExploreEnvironment
import com.deuna.explore.presentation.ExploreViewModel

@Composable
fun ConfigurationDrawer(
    viewModel: ExploreViewModel,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val draft = state.draftConfig
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ExploreColors.screenBackground)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DrawerHeader(onClose = onClose)

            EnvironmentSection(
                selected = draft.environment,
                onSelect = { env -> viewModel.updateDraftConfig { it.copy(environment = env) } },
            )

            KeysSection(
                publicKey = draft.publicKey,
                privateKey = draft.privateKey,
                keyErrorMessage = state.keyErrorMessage,
                onPublicKeyChange = { viewModel.updateDraftConfig { c -> c.copy(publicKey = it) } },
                onPrivateKeyChange = { viewModel.updateDraftConfig { c -> c.copy(privateKey = it) } },
            )

            TokensSection(
                orderToken = draft.orderToken,
                userToken = draft.userToken,
                onOrderTokenChange = { viewModel.updateDraftConfig { c -> c.copy(orderToken = it) } },
                onUserTokenChange = { viewModel.updateDraftConfig { c -> c.copy(userToken = it) } },
            )

            WidgetTypeSection(
                selected = draft.selectedWidget,
                onSelect = { viewModel.updateDraftConfig { c -> c.copy(selectedWidget = it) } },
            )

            OptionsSection(
                hidePayButton = draft.hidePayButton,
                enableSplitPayment = draft.enableSplitPayment,
                presentationMode = draft.presentationMode,
                onHidePayButtonChange = { viewModel.updateDraftConfig { c -> c.copy(hidePayButton = it) } },
                onEnableSplitPaymentChange = { viewModel.updateDraftConfig { c -> c.copy(enableSplitPayment = it) } },
                onPresentationModeChange = { viewModel.updateDraftConfig { c -> c.copy(presentationMode = it) } },
            )

            UserInfoSection(
                firstName = draft.userInfoFirstName,
                lastName = draft.userInfoLastName,
                email = draft.userInfoEmail,
                onFirstNameChange = { viewModel.updateDraftConfig { c -> c.copy(userInfoFirstName = it) } },
                onLastNameChange = { viewModel.updateDraftConfig { c -> c.copy(userInfoLastName = it) } },
                onEmailChange = { viewModel.updateDraftConfig { c -> c.copy(userInfoEmail = it) } },
            )

            FraudSection(
                fraudProvidersJson = draft.fraudProvidersJson,
                fraudId = draft.fraudId,
                fraudIdStatusMessage = state.fraudIdStatusMessage,
                isGeneratingFraudId = state.isGeneratingFraudId,
                isApplyingConfiguration = state.isApplyingConfiguration,
                onFraudProvidersJsonChange = { viewModel.updateDraftConfig { c -> c.copy(fraudProvidersJson = it) } },
                onFraudIdChange = { viewModel.updateDraftConfig { c -> c.copy(fraudId = it) } },
                onGenerateFraudId = { viewModel.generateFraudId(context) },
            )
        }

        DrawerFooter(
            isApplyingConfiguration = state.isApplyingConfiguration,
            onCancel = {
                viewModel.discardDraftChanges()
                onClose()
            },
            onApply = {
                viewModel.applyConfiguration { onClose() }
            },
        )
    }
}

@Composable
private fun DrawerHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Configuration",
            fontSize = 32.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Gray,
            )
        }
    }
}

@Composable
private fun EnvironmentSection(
    selected: ExploreEnvironment,
    onSelect: (ExploreEnvironment) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Environment")
        SegmentedPillSelector(
            items = ExploreEnvironment.entries,
            selected = selected,
            labelOf = { it.title },
            onSelect = onSelect,
        )
    }
}

@Composable
private fun DrawerFooter(
    isApplyingConfiguration: Boolean,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    Column {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onCancel,
                enabled = !isApplyingConfiguration,
            ) {
                Text("Cancelar")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onApply,
                enabled = !isApplyingConfiguration,
                colors = ButtonDefaults.buttonColors(containerColor = ExploreColors.brandBlue),
            ) {
                Text(if (isApplyingConfiguration) "Applying..." else "Explorar")
            }
        }
    }
}
