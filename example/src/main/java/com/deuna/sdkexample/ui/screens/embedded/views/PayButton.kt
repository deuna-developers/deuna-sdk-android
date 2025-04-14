package com.deuna.sdkexample.ui.screens.embedded.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun PayButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C29C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            active = true,
            activeContent = { Icons.Default.Lock },
            inactiveContent = { Icons.Default.Lock },
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Pay", color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}