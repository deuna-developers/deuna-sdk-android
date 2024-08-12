package com.deuna.maven.payment_widget.domain

enum class PaymentWidgetEvent(val value: String) {
    onBinDetected("onBinDetected"),
    onInstallmentSelected("onInstallmentSelected"),
    refetchOrder("refetchOrder"),
    purchase("purchase"),
    purchaseError("purchaseError"),
    paymentMethods3dsInitiated("paymentMethods3dsInitiated"),
    linkClose("linkClose"),
}