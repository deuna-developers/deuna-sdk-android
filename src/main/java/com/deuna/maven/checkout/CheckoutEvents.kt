package com.deuna.maven.checkout

enum class CheckoutEvents(val value: String) {
    purchaseRejected("purchaseRejected"),
    paymentProcessing("paymentProcessing"),
    purchaseError("purchaseError"),
    purchase("purchase"),
    apmSuccess("apmSuccess"),
    apmClickRedirect("apmClickRedirect"),
    apmFailed("apmFailed"),
    changeAddress("changeAddress"),
    paymentMethods3dsInitiated("paymentMethods3dsInitiated"),
    paymentClick("paymentClick"),
    paymentMethodsCardNumberInitiated("paymentMethodsCardNumberInitiated"),
    paymentMethodsEntered("paymentMethodsEntered"),
    linkStarted("linkStarted"),
    paymentMethodsStarted("paymentMethodsStarted"),
    adBlock("adBlock"),
    couponStarted("couponStarted"),
    linkFailed("linkFailed"),
    paymentMethodsAddCard("paymentMethodsAddCard"),
    checkoutStarted("checkoutStarted"),
    paymentMethodsCardExpirationDateInitiated("paymentMethodsCardExpirationDateInitiated"),
    paymentMethodsCardNameInitiated("paymentMethodsCardNameInitiated"),
    paymentMethodsCardSecurityCodeInitiated("paymentMethodsCardSecurityCodeInitiated"),
    paymentMethodsCardNumberEntered("paymentMethodsCardNumberEntered"),
    paymentMethodsCardExpirationDateEntered("paymentMethodsCardExpirationDateEntered"),
    paymentMethodsCardSecurityCodeEntered("paymentMethodsCardSecurityCodeEntered")

}