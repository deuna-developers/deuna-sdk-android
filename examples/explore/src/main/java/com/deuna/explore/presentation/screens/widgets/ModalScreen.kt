package com.deuna.explore.presentation.screens.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.deuna.explore.data.ProductCatalog
import com.deuna.explore.domain.ApmOption
import com.deuna.explore.domain.ExploreProduct
import com.deuna.explore.presentation.ExploreViewModel

private val NavyBlue = Color(0xFF1B2B6E)
private val MediumBlue = Color(0xFF2563EB)
private val LightBlue = Color(0xFF3B82F6)
private val AccentBlue = Color(0xFF147AE8)

@Composable
fun ModalScreen(viewModel: ExploreViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val products = state.products
    val selectedIds = state.selectedProductIds
    val useManual = state.useManualOrderTokenFlow
    val generatedOrderToken = state.generatedOrderToken
    var showApmDialog by remember { mutableStateOf(false) }

    if (showApmDialog) {
        ApmPickerDialog(
            options = state.apmOptions,
            isLoading = state.isLoadingApms,
            onSelect = { apm ->
                showApmDialog = false
                viewModel.showFormularios(context, apm)
            },
            onDismiss = { showApmDialog = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (useManual) {
            ManualCheckoutPreview(products = products, onClear = { viewModel.clearGeneratedOrder() })
        } else if (generatedOrderToken != null) {
            OrderCreatedCard(
                orderToken = generatedOrderToken,
                selectedProducts = products.filter { it.id in selectedIds },
                onClear = { viewModel.clearGeneratedOrder() },
            )
        } else {
            ProductCatalogSection(
                products = products,
                selectedIds = selectedIds,
                onToggle = { viewModel.toggleProductSelection(it) },
            )
        }

        if (!state.modalStatusMessage.isNullOrEmpty()) {
            Text(
                text = state.modalStatusMessage!!,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { viewModel.showModalWidget(context) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLaunchingModalWidget,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            ) {
                Text(
                    text = if (state.isLaunchingModalWidget) "Preparing..." else "Show Widget",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }

            Button(
                onClick = { viewModel.showWallets() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLaunchingWallets,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MediumBlue),
            ) {
                Text(
                    text = if (state.isLaunchingWallets) "Preparing..." else "Wallets",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }

            Button(
                onClick = {
                    viewModel.loadApmOptions()
                    showApmDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLaunchingFormularios,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlue),
            ) {
                Text(
                    text = if (state.isLaunchingFormularios) "Preparando..." else "Formularios",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun ProductCatalogSection(
    products: List<ExploreProduct>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    val selectedProducts = products.filter { it.id in selectedIds }
    val selectedTotal = selectedProducts.sumOf { it.priceInCents }
    val fractionDigits = products.firstOrNull()?.fractionDigits ?: 2
    val symbol = products.firstOrNull()?.currencySymbol ?: "$"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Available Products", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            products.chunked(2).forEach { rowProducts ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowProducts.forEach { product ->
                        ProductCard(
                            product = product,
                            isSelected = product.id in selectedIds,
                            onToggle = { onToggle(product.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowProducts.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        CartSummaryCard(
            selectedCount = selectedIds.size,
            subtotalCents = selectedTotal,
            fractionDigits = fractionDigits,
            symbol = symbol,
        )
    }
}

@Composable
private fun ProductCard(
    product: ExploreProduct,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(1.5.dp, AccentBlue.copy(alpha = 0.6f))
        else null,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(product.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface),
                )
                // Price chip — top start
                Text(
                    text = ProductCatalog.formatPrice(product.priceInCents, product.fractionDigits, product.currencySymbol),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                )
                // Added icon chip — top end
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color(0xFF16A34A), RoundedCornerShape(6.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Added",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)

            if (isSelected) {
                OutlinedButton(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue),
                ) {
                    Text("Remove", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    Text("Add", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CartSummaryCard(
    selectedCount: Int,
    subtotalCents: Int,
    fractionDigits: Int,
    symbol: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Cart ($selectedCount)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "$selectedCount Items Selected",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Row {
                Text("Subtotal", fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text(ProductCatalog.formatPrice(subtotalCents, fractionDigits, symbol), fontSize = 13.sp)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(
                    ProductCatalog.formatPrice(subtotalCents, fractionDigits, symbol),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentBlue,
                )
            }
        }
    }
}

@Composable
private fun OrderCreatedCard(
    orderToken: String,
    selectedProducts: List<ExploreProduct>,
    onClear: () -> Unit,
) {
    val total = selectedProducts.sumOf { it.priceInCents }
    val fractionDigits = selectedProducts.firstOrNull()?.fractionDigits ?: 2
    val symbol = selectedProducts.firstOrNull()?.currencySymbol ?: "$"
    val shortToken = if (orderToken.length > 20) orderToken.take(20) + "…" else orderToken

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentBlue.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Order Created", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear order", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row {
                Text("Token", fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text(shortToken, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Row {
                Text("Items", fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("${selectedProducts.size}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(ProductCatalog.formatPrice(total, fractionDigits, symbol), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentBlue)
            }
        }
    }
}

@Composable
private fun ManualCheckoutPreview(products: List<ExploreProduct>, onClear: () -> Unit) {
    val total = products.sumOf { it.priceInCents }
    val fractionDigits = products.firstOrNull()?.fractionDigits ?: 2
    val symbol = products.firstOrNull()?.currencySymbol ?: "$"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Checkout", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear order", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Row {
                Text("Items", fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("${products.size}", fontSize = 13.sp)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(ProductCatalog.formatPrice(total, fractionDigits, symbol), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentBlue)
            }
        }
    }
}
