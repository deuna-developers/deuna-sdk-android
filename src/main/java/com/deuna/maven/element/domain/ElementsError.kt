package com.deuna.maven.element.domain

import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.ErrorCodes
import com.deuna.maven.shared.ErrorMessages
import com.deuna.maven.shared.Json

@Suppress("UNCHECKED_CAST")
data class ElementsError(
    val type: Type,
    val metadata: Metadata? = null,
    val user: Json? = null
) {
    enum class Type(val message: String) {
        NO_INTERNET_CONNECTION("No internet connection available"),
        INITIALIZATION_FAILED("Failed to initialize the widget"),
        INVALID_USER_TOKEN("Invalid user token"),
        UNKNOWN_ERROR("An unknown error occurred"),
        USER_ERROR("An error occurred related to the user authentication"),
        VAULT_SAVE_ERROR("Vault save error")
    }

    data class Metadata(val code: String, val message: String)

    companion object {
        fun fromJson(type: Type, data: Json): ElementsError? {
            val metadata = data["metadata"] as? Json
            val user: Json? = data["user"] as? Json
            if (metadata == null) {
                return null
            }

            val errorCode = metadata["errorCode"] as? String
            val errorMessage = metadata["errorMessage"] as? String


            if (errorCode == null || errorMessage == null) {
                DeunaLogs.error("Missing errorCode or errorMessage")
                DeunaLogs.warning("$metadata")
            }

            return ElementsError(
                type = type,
                metadata = Metadata(
                    code = errorCode ?: ErrorCodes.UNKNOWN_ERROR.name,
                    message = errorMessage ?: ErrorMessages.UNKNOWN.message
                ),
                user = user
            )
        }
    }
}