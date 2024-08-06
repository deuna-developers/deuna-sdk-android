![](https://d-una-one.s3.us-east-2.amazonaws.com/gestionado_por_d-una.png)
# DeunaSDK Documentation
[![License](https://img.shields.io/github/license/deuna-developers/deuna-sdk-ios?style=flat-square)](https://github.com/deuna-developers/deuna-sdk-io/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios-blue?style=flat-square)](https://github.com/deuna-developers/deuna-sdk-ios#)

## Introduction

DeunaSDK is a Android-based SDK designed to facilitate integration with the DEUNA. This SDK provides a seamless way to initialize payments, handle success, error, and close actions, and manage configurations.

Get started with our [📚 integration guides](https://docs.deuna.com/docs/integraciones-del-android-sdk) and [example projects](https://github.com/DUNA-E-Commmerce/deuna-sdk-android/tree/master/examples/basic-integration)



## Installation

### Gradle

You can install DeunaSDK using by adding the following dependency to your `build.gradle` file:

  ```
    implementation("com.deuna.maven:deunasdk:2.1.1")
  ```

## Usage


### Initialization

To use the SDK you need to create one instance of `DeunaSDK`. There are 2 ways that you can create an instance of `DeunaSDK`:

1. Registing a singleton to use the same instance in any part of your code

    ```kotlin
    DeunaSDK.initialize(
        environment = Environment.SANDBOX, // Environment.PRODUCTION , etc
        publicApiKey = "YOUR_PUBLIC_API_KEY"
    )
    ```
    Now you can use the same instance of DeunaSDK using `DeunaSDK.shared`

    ```kotlin
    DeunaSDK.shared.initCheckout(...)
    ```

2. Instantiation

    ```kotlin

    class MyClass {
        private lateinit val deunaSDK: DeunaSDK
    
        init {
            deunaSDK = DeunaSDK(
                environment = Environment.SANDBOX,
                publicApiKey = "YOUR_PUBLIC_API_KEY"
            )
        }

        fun buy(){
            deunaSdk.initCheckout(...)
        }
    }

    ```

### Launch the Checkout

To launch the checkout process you must use the `initCheckout` function. It sets up the WebView, checks for internet connectivity, and loads the payment link.

**Parameters:**
-   **orderToken**: The token representing the order.
-   **callbacks**: An instance of the `CheckoutCallbacks` class, which contains closures that will be called on success, error, or when the WebView is closed.
-   **closeEvents**: A set of `CheckoutEvent` values specifying when to automatically close the checkout.

    > NOTE: By default, the WebView modal is only closed when the user presses the close button. You can use the `closeEvents` parameter to close the WebView without having to call the `closeCheckout` function.

    ```kotlin
    class MyClass: AppCompatActivity() {
        val deunaSDK: DeunaSDK ....

        fun buy(orderToken:String){
            val callbacks =  CheckoutCallbacks().apply {
                onSuccess = { response ->
                   deunaSDK.closeCheckout(...)
                   // show the success view
                }
                onError = { error ->
                    // your logic
                   deunaSDK.closeCheckout(...)
                }
                onCanceled = {
                    // called when the payment process was canceled by user
                    // Calling closeCheckout(...) is unnecessary here.
                }
                eventListener = { type, response ->
                    when(type){
                        ...
                    }
                }
                onClosed = {
                    // DEUNA widget was closed
                }
            }

            deunaSDK.initCheckout(
                context = this,
                orderToken = orderToken,
                callbacks = callbacks
            )
        }
    }
    ```


### Launch the VAULT WIDGET

To launch the vault widget you must use the `initElements` function. It sets up the WebView, checks for internet connectivity, and loads the elements link.

**Parameters:**
-   **userToken**: The token representing the user.
-   **callbacks**: An instance of the `ElementsCallbacks` class, which contains closures that will be called on success, error, or when the WebView is closed.
-   **closeEvents**: A set of `ElementsEvent` values specifying when to automatically close the checkout.

    > NOTE: By default, the WebView modal is only closed when the user presses the close button. You can use the `closeEvents` parameter to close the WebView without having to call the `closeElements` function.

    ```kotlin
    class MyClass: AppCompatActivity() {
        val deunaSDK: DeunaSDK ....

        fun saveCard(userToken:String){
            val callbacks =  ElementsCallbacks().apply {
                onSuccess = { response ->
                   deunaSDK.closeElements(...)
                   // show the success view
                }
                onError = { error ->
                    // your logic
                   deunaSDK.closeElements(...)
                }
                onCanceled = {
                    // called when the elements process was canceled by user
                    // Calling closeElements(...) is unnecessary here.
                }
                eventListener = { type, response ->
                    when(type){
                        ...
                    }
                }
                onClosed = {
                    // the elements view was closed
                }
            }

            deunaSDK.initElements(
                context = this,
                orderToken = userToken,
                callbacks = callbacks
            )
        }
    }
    ```

### Logging
To enable or disable logging:
```kotlin
DeunaLogs.isEnabled = false // or true
```


### Network Reachability
The SDK automatically checks for network availability before initializing the checkout process.



## FAQs
* ### How to get an **order token** ?
    To generate an order token, refer to our API documentation on our [API Referece](https://docs.deuna.com/reference/order_token)

* ### How to get an **user token** ?
    You'll need a registered user in DEUNA. Follow the instructions for ["User Registration"](https://docs.deuna.com/reference/users-register) in our API reference.

    Once you have a registered user, you can obtain an access token through a two-step process:

    **Request an OTP code**: Use our API for "[Requesting an OTP Code](https://docs.deuna.com/reference/request-otp)" via email.

    **Login with OTP code:** Use the retrieved code to "[Log in with OTP](https://docs.deuna.com/reference/login-with-otp)" and get an access token for your user.




## CHANGELOG
Check all changes [here](https://github.com/DUNA-E-Commmerce/deuna-sdk-android/blob/master/CHANGELOG.md)

## Author
DUENA Inc.

## License
DEUNA's SDKs and Libraries are available under the MIT license. See the LICENSE file for more info.
