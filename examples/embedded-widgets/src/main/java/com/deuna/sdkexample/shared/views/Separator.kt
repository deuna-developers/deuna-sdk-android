package com.deuna.sdkexample.shared.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Separator(height:Dp = 10.dp) {
    Box(modifier = Modifier.height(height))
}