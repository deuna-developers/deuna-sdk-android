package com.deuna.compose_demo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deuna.compose_demo.LocalNavController
import com.deuna.compose_demo.Navigator


@Composable
fun ClickToPaySuccessfulScreen() {
    val navController = LocalNavController.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Click To Pay successful")
            Box(modifier = Modifier.height(20.dp))
            ElevatedButton(
                onClick = {
                    // pop the current screen
                    navController.popBackStack()
                },
            ) {
                Text(text = "Go back!")
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    Navigator {
        ClickToPaySuccessfulScreen()
    }
}