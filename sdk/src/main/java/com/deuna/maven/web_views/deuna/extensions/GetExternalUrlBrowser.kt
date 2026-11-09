package com.deuna.maven.web_views.deuna.extensions

import com.deuna.maven.shared.DOMAINS_MUST_BE_OPENED_IN_CUSTOM_TAB
import com.deuna.maven.web_views.ExternalUrlBrowser
import com.deuna.maven.web_views.deuna.DeunaWidget
import java.net.URL

/**
 * Check if the url must be opened in custom tabs
 * @param url - url to check
 * @return ExternalUrlBrowser
 */
fun DeunaWidget.getExternalUrlBrowser(url: String): ExternalUrlBrowser {
    val host = URL(url).host
    for (domain in DOMAINS_MUST_BE_OPENED_IN_CUSTOM_TAB) {
        if (host.contains(domain)) {
            return ExternalUrlBrowser.CUSTOM_TABS
        }
    }
    return ExternalUrlBrowser.WEB_VIEW
}