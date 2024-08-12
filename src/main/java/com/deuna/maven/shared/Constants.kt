package com.deuna.maven.shared

import com.deuna.maven.element.domain.ElementsError

enum class ErrorCodes {
    INITIALIZATION_ERROR,
    UNKNOWN_ERROR
}

enum class ErrorMessages(val message: String) {
    UNKNOWN("Unknown error")
}

enum class QueryParameters(val value: String) {
    MODE("mode"),
    WIDGET("widget"),
    PUBLIC_API_KEY("publicApiKey"),
    USER_TOKEN("userToken"),
    CSS_FILE("cssFile"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    EMAIL("email")
}

enum class PaymentsErrorMessages(val message: String) {
    ORDER_TOKEN_MUST_NOT_BE_EMPTY("OrderToken must not be empty."),
    PAYMENT_LINK_COULD_NOT_BE_GENERATED("Payment link could not be generated."),
    NO_INTERNET_CONNECTION("No internet connection available."),
    ORDER_COULD_NOT_BE_RETRIEVED("Order could not be retrieved.");
}

enum class ElementsErrorMessages(val message: String) {
    PAYMENT_LINK_COULD_NOT_BE_GENERATED("Vault link could not be generated."),
    INVALID_USER_INFO("Invalid instance of UserInfo: check the firstName, lastName and email fields."),
    MISSING_USER_TOKEN_OR_USER_INFO("userToken or userInfo must be passed.")
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
                message = PaymentsErrorMessages.ORDER_TOKEN_MUST_NOT_BE_EMPTY.message
            )
        )

        val linkCouldNotBeGenerated = PaymentsError(
            type = PaymentsError.Type.INITIALIZATION_FAILED,
            metadata = PaymentsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = PaymentsErrorMessages.PAYMENT_LINK_COULD_NOT_BE_GENERATED.message
            )
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
                message = ElementsErrorMessages.INVALID_USER_INFO.message
            )
        )

        val missingUserTokenOrUserInfo = ElementsError(
            type = ElementsError.Type.INITIALIZATION_FAILED,
            metadata = ElementsError.Metadata(
                code = ErrorCodes.INITIALIZATION_ERROR.name,
                message = ElementsErrorMessages.MISSING_USER_TOKEN_OR_USER_INFO.message
            )
        )
    }
}