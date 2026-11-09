package com.deuna.explore

import org.junit.Assert.assertEquals
import org.junit.Test

class ExploreUnitTest {
    @Test
    fun defaultConfig_hasExpectedValues() {
        val config = com.deuna.explore.domain.IntegrationConfig.default
        assertEquals(com.deuna.explore.domain.ExploreEnvironment.SANDBOX, config.environment)
        assertEquals(com.deuna.explore.domain.ExploreWidget.PAYMENT_WIDGET, config.selectedWidget)
    }
}
