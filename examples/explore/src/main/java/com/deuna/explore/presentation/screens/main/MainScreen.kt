package com.deuna.explore.presentation.screens.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deuna.explore.domain.ExplorePresentationMode
import com.deuna.explore.presentation.ExploreViewModel
import com.deuna.explore.presentation.screens.drawer.ConfigurationDrawer

@Composable
fun MainScreen(viewModel: ExploreViewModel) {
    val state by viewModel.uiState.collectAsState()
    var isDrawerOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = state.appliedConfig.merchantName.ifEmpty { "SDK Tester" },
                showRefresh = state.appliedConfig.presentationMode == ExplorePresentationMode.EMBEDDED
                    && state.isShowingEmbeddedScreen,
                onOpenDrawer = {
                    viewModel.openDrawer()
                    isDrawerOpen = true
                },
                onRefresh = { viewModel.refreshEmbedded() },
            )
            HorizontalDivider()
            if (state.latestVersion != null) {
                UpdateBanner(
                    latestVersion = state.latestVersion!!,
                    onDownload = {
                        val url = "https://github.com/${viewModel.githubRepo}/releases"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                )
            }
            ModeContent(viewModel = viewModel)
        }

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it },
        ) {
            ConfigurationDrawer(
                viewModel = viewModel,
                onClose = { isDrawerOpen = false },
            )
        }
    }
}

@Composable
private fun UpdateBanner(latestVersion: String, onDownload: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B2B6E))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "New version available: v$latestVersion",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier.clickable { onDownload() },
        ) {
            Text(
                text = "Download",
                color = Color(0xFF1B2B6E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}
