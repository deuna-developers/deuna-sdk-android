package com.deuna.maven.shared

@Suppress("UNCHECKED_CAST")
data class PaymentsError(
    val type: Type,
    val metadata: Metadata? = null,
    val order: Json? = null
) {

    data class Metadata(val code: String, val message: String)

    enum class Type(val message: String) {
        NO_INTERNET_CONNECTION("No internet connection available"),
        INVALID_ORDER_TOKEN("Invalid order token"),
        INITIALIZATION_FAILED("Failed to initialize the widget"),
        ORDER_COULD_NOT_BE_RETRIEVED("Order could not be retrieved"),
        ORDER_NOT_FOUND("Order not found"),
        PAYMENT_ERROR("An error occurred while processing payment"),
        UNKNOWN_ERROR("An unknown error occurred"),
    }


    companion object {
        fun fromJson(type: Type, data: Json): PaymentsError? {
            val metadata = data["metadata"] as? Json
            val order: Json? = data["order"] as? Json
            if (metadata == null || order == null) {
                return null
            }

            val errorCode = metadata["errorCode"] as? String
            val errorMessage = metadata["errorMessage"] as? String


            if (errorCode == null || errorMessage == null) {
                DeunaLogs.error("Missing errorCode or errorMessage")
                DeunaLogs.warning("$metadata")
            }

            return PaymentsError(
                type = type,
                metadata = Metadata(
                    code = errorCode ?: ErrorCodes.UNKNOWN_ERROR.name,
                    message = errorMessage ?: ErrorMessages.UNKNOWN.message
                ),
                order = order
            )
        }
    }
}