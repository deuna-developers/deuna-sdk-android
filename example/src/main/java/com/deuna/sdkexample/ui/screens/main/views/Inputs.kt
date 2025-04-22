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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun Inputs(
    orderTokenState: MutableState<String>,
    userTokenState: MutableState<String>
) {
    Column {
        OutlinedTextField(
            value = orderTokenState.value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            shape = RoundedCornerShape(12.dp),
            label = {
                Text("Order Token")
            },
            onValueChange = { orderTokenState.value = it }
        )

        Box(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = userTokenState.value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            shape = RoundedCornerShape(12.dp),
            label = {
                Text("User Token")
            },
            onValueChange = { userTokenState.value = it }
        )
    }
}