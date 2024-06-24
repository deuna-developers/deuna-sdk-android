package com.deuna.maven.shared

enum class CheckoutErrorType(val message: String) {
  NO_INTERNET_CONNECTION("No internet connection available"),
  INVALID_ORDER_TOKEN("Invalid order token"),
  CHECKOUT_INITIALIZATION_FAILED("Failed to initialize the checkout"),
  ORDER_NOT_FOUND("Order not found"),
  PAYMENT_ERROR("An error occurred while processing payment"),
  UNKNOWN_ERROR("An unknown error occurred"),
  USER_ERROR("An error occurred related to the user authentication"),
}


enum class ElementsErrorType(val message: String) {
  NO_INTERNET_CONNECTION("No internet connection available"),
  INVALID_USER_TOKEN("Invalid user token"),
  UNKNOWN_ERROR("An unknown error occurred"),
  USER_ERROR("An error occurred related to the user authentication"),
  VAULT_SAVE_ERROR("Vault save error")
}