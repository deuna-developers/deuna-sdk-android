package com.deuna.sdkexample.ui.screens.main.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun Inputs(
    orderToken: String,
    userToken: String,
    onOrderTokenChange: (String) -> Unit,
    onUserTokenChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = orderToken,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            shape = RoundedCornerShape(12.dp),
            label = {
                Text("Order Token")
            },
            onValueChange = onOrderTokenChange
        )

        Box(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = userToken,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            shape = RoundedCornerShape(12.dp),
            label = {
                Text("User Token")
            },
            onValueChange = onUserTokenChange
        )
    }
}