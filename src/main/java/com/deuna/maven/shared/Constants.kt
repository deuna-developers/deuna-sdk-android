package com.deuna.maven.shared

import com.deuna.maven.element.domain.ElementsError

enum class ErrorCodes {
    INITIALIZATION_ERROR,
    UNKNOWN_ERROR
}

object ErrorMessages {
    const val UNKNOWN = "Unknown error"
}

object ElementsTypeKey {
    const val NAME = "name"
}

object ElementsWidget {
    const val VAULT = "vault"
    const val CLICK_TO_PAY = "click_to_pay"
}

object OnEmbedEvents {
    const val APM_CLOSED = "apmClosed"
}

object QueryParameters {
    const val MODE = "mode"
    const val WIDGET = "widget"
    const val XPROPS_B64 = "xpropsB64"
    const val PUBLIC_API_KEY = "publicApiKey"
    const val USER_TOKEN = "userToken"
    const val CSS_FILE = "cssFile"
    const val STYLE_FILE = "styleFile"
    const val PAYMENT_METHODS = "paymentMethods"
    const val CHECKOUT_MODULES = "checkoutModules"
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
    const val EMAIL = "email"
    const val LANGUAGE = "language"
}

object PaymentsErrorMessages {
    const val ORDER_TOKEN_MUST_NOT_BE_EMPTY = "OrderToken must not be empty."
    const val PAYMENT_LINK_COULD_NOT_BE_GENERATED = "Payment link could not be generated."
    const val NO_INTERNET_CONNECTION = "No internet connection available."
    const val ORDER_COULD_NOT_BE_RETRIEVED = "Order could not be retrieved."
}

object ElementsErrorMessages {
    const val PAYMENT_LINK_COULD_NOT_BE_GENERATED = "Vault link could not be generated."

    const val INVALID_USER_INFO =
        "Invalid instance of UserInfo: check the firstName, lastName and email fields."

    const val MISSING_USER_INFO =
        "userInfo must be passed when the types parameter contains click_to_pay."

    const val MISSING_USER_TOKEN_OR_USER_INFO = "userToken or userInfo must be passed."
}

class PaymentWidgetErrors {
    companion object {
        val noInternetConnection = PaymentsError(
            type = PaymentsError.Type.NO_INTERNET_CONNECTION
        )

        val invalidOrderToken = PaymentsError(
            type = PaymentsError.Type.INVALID_ORDER_TOKEN,
            metadata = PaymentsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = PaymentsErrorMessages.ORDER_TOKEN_MUST_NOT_BE_EMPTY
            )
        )

        val linkCouldNotBeGenerated = PaymentsError(
            type = PaymentsError.Type.INITIALIZATION_FAILED,
            metadata = PaymentsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = PaymentsErrorMessages.PAYMENT_LINK_COULD_NOT_BE_GENERATED
            )
        )

        val initializationFailed = PaymentsError(
            type = PaymentsError.Type.INITIALIZATION_FAILED,
        )
    }
}


class ElementsErrors {
    companion object {
        val noInternetConnection = ElementsError(
            type = ElementsError.Type.NO_INTERNET_CONNECTION
        )

        val invalidUserInfo = ElementsError(
            type = ElementsError.Type.INITIALIZATION_FAILED,
            metadata = ElementsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = ElementsErrorMessages.INVALID_USER_INFO
            )
        )

        val missingUserInfo = ElementsError(
            type = ElementsError.Type.INITIALIZATION_FAILED,
            metadata = ElementsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = ElementsErrorMessages.MISSING_USER_INFO
            )
        )

        val missingUserTokenOrUserInfo = ElementsError(
            type = ElementsError.Type.INITIALIZATION_FAILED,
            metadata = ElementsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = ElementsErrorMessages.MISSING_USER_TOKEN_OR_USER_INFO
            )
        )

        val initializationFailed = ElementsError(
            type = ElementsError.Type.INITIALIZATION_FAILED
        )
    }
}