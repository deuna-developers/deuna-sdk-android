package com.deuna.maven.widgets.configuration

/**
 * Configuration for the auto-resize behavior of an embedded widget.
 *
 * Pass an instance of this class to the widget configuration to enable automatic
 * resizing to the WebView content height. The widget must be placed inside a
 * scrollable container (e.g., a Compose Column with verticalScroll).
 *
 * @param initialHeightDp Optional initial height in dp shown while the page loads.
 */
data class AutoResizeConfig(
    val initialHeightDp: Int? = null,
)
