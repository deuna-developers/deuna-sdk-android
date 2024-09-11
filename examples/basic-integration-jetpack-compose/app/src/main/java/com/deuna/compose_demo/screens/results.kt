package com.deuna.compose_demo.screens

import com.deuna.maven.element.domain.ElementsError
import com.deuna.maven.shared.Json
import com.deuna.maven.shared.PaymentsError


/**
 * Sealed classes for representing the results of checkout and element saving processes.
 */

sealed class PaymentWidgetResult {

    /**
     * Indicates successful completion of the payment process.
     *
     * @property order The CheckoutResponse.Dat object containing details about the completed payment
     */
    data class Success(val order: Json) : PaymentWidgetResult()

    /**
     * Indicates an error occurred during the payment process.
     *
     */
    data class Error(val error: PaymentsError) : PaymentWidgetResult()

    /**
     * Indicates the payment process was cancelled by the user.
     */
    data object Canceled : PaymentWidgetResult()
}


/**
 * Sealed classes for representing the results of checkout and element saving processes.
 */

sealed class CheckoutResult {

    /**
     * Indicates successful completion of the checkout process.
     *
     * @property order The CheckoutResponse object containing details about the completed checkout.
     */
    data class Success(val order: Json) : CheckoutResult()

    /**
     * Indicates an error occurred during the checkout process.
     *
     * @property error The PaymentsError object detailing the error encountered.
     */
    data class Error(val error: PaymentsError) : CheckoutResult()

    /**
     * Indicates the checkout process was cancelled by the user.
     */
    data object Canceled : CheckoutResult()
}


sealed class ElementsResult {

    /**
     * Indicates successful completion of the element saving process (e.g., saving card information).
     *
     * @property savedCard The elements response object containing details about the saved elements.
     */
    data class Success(val savedCard: Json) : ElementsResult()

    /**
     * Indicates an error occurred during the element saving process.
     *
     * @property error The ElementsErrorMessage object detailing the error encountered.
     */
    data class Error(val error: ElementsError) : ElementsResult()

    /**
     * Indicates the element saving process was cancelled by the user.
     */
    data object Canceled : ElementsResult()
}