package com.deuna.sdkexample.ui.screens.main

object MainScreenTestTags {
    const val WIDGET_PICKER_BUTTON = "widget_picker_button"
    const val SHOW_WIDGET_BUTTON = "show_widget_button"
    const val ORDER_TOKEN_INPUT = "order_token_input"
    const val USER_TOKEN_INPUT = "user_token_input"

    fun widgetOption(widget: WidgetToShow): String = "widget_option_${widget.name.lowercase()}"
}
