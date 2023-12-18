![](https://d-una-one.s3.us-east-2.amazonaws.com/gestionado_por_d-una.png)
# DeunaSDK Documentation
[![License](https://img.shields.io/github/license/deuna-developers/deuna-sdk-ios?style=flat-square)](https://github.com/deuna-developers/deuna-sdk-io/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios-blue?style=flat-square)](https://github.com/deuna-developers/deuna-sdk-ios#)

## Introduction

DeunaSDK is a Android-based SDK designed to facilitate integration with the DEUNA. This SDK provides a seamless way to initialize payments, handle success, error, and close actions, and manage configurations.

Get started with our [📚 integration guides](https://docs.deuna.com/docs/integraciones-del-android-sdk) and [example projects](#examples)



## Installation

### Gradle

You can install DeunaSDK using Swift Package Manager by adding the following dependency to your `build.gradle` file:

    implementation("com.deuna.maven:deunasdk:0.2.6")

### Examples

- [Prebuilt UI](examples) (Recommended)
    - This example demonstrates how to build a payment flow using [`PaymentWidget`](https://docs.deuna.com/docs/widget-payments-and-fraud)

## Usage


### Configuration

Before using the SDK, you need to configure it with your API key, order token, user token, environment, element type, and close button configuration.

     DeUnaSdk.config(
            apiKey = "8560b86594b9af09165e765bc6b57d9797c9535479274f5de3f83f92c5fc3c70584080fde4e875f3782c3285e4be6996c25b4f77fac99e2ae4e6992b0778",
            orderToken = "2b754211-3eb1-4e2e-832e-56dca6419f08",
            environment = Environment.DEVELOPMENT,
            elementType = ElementType.EXAMPLE, // Optional
            closeOnEvents = arrayOf(CheckoutEvents.CHANGE_ADDRESS), // Optional
            userToken = "" // Optional
        )

### Initializing Checkout

This method initializes the checkout process. It sets up the WebView, checks for internet connectivity, and loads the payment link.

**Parameters:**
-   **view**: The view that will contain the WebView.
          val initPayment = DeUnaSdk.initCheckout(view)
          initPayment.onSuccess = { order ->
            val orderSuccessResponse: OrderSuccessResponse = order
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(
                context,
                "Success Orden id: ${orderSuccessResponse.order?.order_id}",
                duration
            )
            toast.show()
          }

          initPayment.onClose = {

          }

          initPayment.onError = { order, errorMessage ->
            val orderErrorResponse: OrderErrorResponse? = order
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(
                context,
                "Error Orden id: ${orderErrorResponse?.order?.order_id}",
                duration
            )
            toast.show()
          }



## Classes & Enums

### CloseButtonConfig

This class allows you to customize the appearance and position of the close button on the WebView.

## DeunaSDKError

`OrderErrorResponse` is an enumeration that represents the possible errors that can occur during the SDK's operation. Each case in this enumeration provides a specific type of error, making it easier to handle and provide feedback to the user or developer.

## Author
DUENA Inc.

## License
DEUNA's SDKs and Libraries are available under the MIT license. See the LICENSE file for more info.
