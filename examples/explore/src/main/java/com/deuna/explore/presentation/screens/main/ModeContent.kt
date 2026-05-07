package com.deuna.explore.presentation.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.deuna.explore.domain.ExplorePresentationMode
import com.deuna.explore.presentation.ExploreViewModel
import com.deuna.explore.presentation.screens.widgets.EmbeddedScreen
import com.deuna.explore.presentation.screens.widgets.ModalScreen

@Composable
fun ModeContent(viewModel: ExploreViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (state.appliedConfig.presentationMode == ExplorePresentationMode.EMBEDDED
        && state.isShowingEmbeddedScreen
    ) {
        EmbeddedScreen(
            widgetConfig = state.embeddedWidgetConfig,
            showPayNowButton = state.appliedConfig.hidePayButton,
        )
    } else {
        ModalScreen(
            viewModel = viewModel,
        )
    }
}
