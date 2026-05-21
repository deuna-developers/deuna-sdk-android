package com.deuna.explore.presentation.screens.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deuna.explore.domain.ExplorePresentationMode
import com.deuna.explore.domain.ExploreWidget

// ─── Keys ────────────────────────────────────────────────────────────────────

@Composable
fun KeysSection(
    publicKey: String,
    privateKey: String,
    keyErrorMessage: String?,
    onPublicKeyChange: (String) -> Unit,
    onPrivateKeyChange: (String) -> Unit,
) {
    DrawerCardSection(title = "Keys") {
        FieldTitle("PUBLIC KEY")
        ClearableTextField(
            value = publicKey,
            onValueChange = onPublicKeyChange,
            placeholder = "pub_test••••••••••••",
        )
        FieldTitle("PRIVATE KEY")
        ClearableTextField(
            value = privateKey,
            onValueChange = onPrivateKeyChange,
            placeholder = "pk_test••••••••••••",
        )
        if (!keyErrorMessage.isNullOrEmpty()) {
            Text(
                text = keyErrorMessage,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

// ─── Tokens ───────────────────────────────────────────────────────────────────

@Composable
fun TokensSection(
    orderToken: String,
    userToken: String,
    onOrderTokenChange: (String) -> Unit,
    onUserTokenChange: (String) -> Unit,
) {
    DrawerCardSection(title = "Tokens") {
        FieldTitle("ORDER TOKEN (OPTIONAL)")
        ClearableTextField(
            value = orderToken,
            onValueChange = onOrderTokenChange,
            placeholder = "order token",
        )
        FieldTitle("USER TOKEN (OPTIONAL)")
        ClearableTextField(
            value = userToken,
            onValueChange = onUserTokenChange,
            placeholder = "user token",
            singleLine = false,
            minLines = 3,
            maxLines = 6,
        )
    }
}

// ─── Widget Type ──────────────────────────────────────────────────────────────

@Composable
fun WidgetTypeSection(
    selected: ExploreWidget,
    onSelect: (ExploreWidget) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Widget Type")
        DrawerCard {
            ExploreWidget.entries.forEachIndexed { index, widget ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(widget) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RadioButton(
                        selected = widget == selected,
                        onClick = { onSelect(widget) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ExploreColors.brandBlue,
                            unselectedColor = ExploreColors.labelGray,
                        ),
                    )
                    Text(
                        text = widget.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black.copy(alpha = 0.82f),
                    )
                }
                if (index < ExploreWidget.entries.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                }
            }
        }
    }
}

// ─── Options ─────────────────────────────────────────────────────────────────

@Composable
fun OptionsSection(
    hidePayButton: Boolean,
    enableSplitPayment: Boolean,
    presentationMode: ExplorePresentationMode,
    onHidePayButtonChange: (Boolean) -> Unit,
    onEnableSplitPaymentChange: (Boolean) -> Unit,
    onPresentationModeChange: (ExplorePresentationMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Options")
        DrawerCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Hide Widget Pay Button",
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.82f),
                )
                Switch(
                    checked = hidePayButton,
                    onCheckedChange = onHidePayButtonChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ExploreColors.brandBlue),
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Enable Split Payment",
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.82f),
                )
                Switch(
                    checked = enableSplitPayment,
                    onCheckedChange = onEnableSplitPaymentChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ExploreColors.brandBlue),
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Presentation Mode",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.82f),
                )
                SegmentedPillSelector(
                    items = ExplorePresentationMode.entries,
                    selected = presentationMode,
                    labelOf = { it.title },
                    onSelect = onPresentationModeChange,
                )
            }
        }
    }
}

// ─── User Info ────────────────────────────────────────────────────────────────

@Composable
fun UserInfoSection(
    firstName: String,
    lastName: String,
    email: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
) {
    DrawerCardSection(title = "User Info") {
        FieldTitle("FIRST NAME (OPTIONAL)")
        ClearableTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            placeholder = "John",
        )
        FieldTitle("LAST NAME (OPTIONAL)")
        ClearableTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            placeholder = "Doe",
        )
        FieldTitle("EMAIL")
        ClearableTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "john@example.com",
        )
    }
}

// ─── Fraud ────────────────────────────────────────────────────────────────────

@Composable
fun FraudSection(
    fraudProvidersJson: String,
    fraudId: String,
    fraudIdStatusMessage: String?,
    isGeneratingFraudId: Boolean,
    isApplyingConfiguration: Boolean,
    onFraudProvidersJsonChange: (String) -> Unit,
    onFraudIdChange: (String) -> Unit,
    onGenerateFraudId: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current

    DrawerCard {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FieldTitle("FRAUD PROVIDERS JSON")
            ClearableTextField(
                value = fraudProvidersJson,
                onValueChange = onFraudProvidersJsonChange,
                placeholder = "{\n  \"RISKIFIED\": { \"storeDomain\": \"yourstore.com\" }\n}",
                singleLine = false,
                minLines = 4,
                maxLines = 8,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                FieldTitle("FRAUD ID")
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(
                        onClick = { clipboard.setText(AnnotatedString(fraudId)) },
                        enabled = fraudId.isNotBlank(),
                    ) {
                        Text(
                            "Copy",
                            color = if (fraudId.isNotBlank()) ExploreColors.brandBlue else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    TextButton(
                        onClick = onGenerateFraudId,
                        enabled = !isGeneratingFraudId && !isApplyingConfiguration,
                    ) {
                        Text(
                            if (isGeneratingFraudId) "Generando..." else "Generar",
                            color = if (!isGeneratingFraudId) ExploreColors.brandBlue else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            ClearableTextField(
                value = fraudId,
                onValueChange = onFraudIdChange,
                placeholder = "fraud id",
            )

            if (!fraudIdStatusMessage.isNullOrEmpty()) {
                val isError = fraudIdStatusMessage.contains("failed", ignoreCase = true)
                    || fraudIdStatusMessage.contains("invalid", ignoreCase = true)
                    || fraudIdStatusMessage.contains("error", ignoreCase = true)
                Text(
                    text = fraudIdStatusMessage,
                    color = if (isError) Color.Red else Color(0xFF34C759),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
