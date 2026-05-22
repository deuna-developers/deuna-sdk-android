![](https://d-una-one.s3.us-east-2.amazonaws.com/gestionado_por_d-una.png)
# DeunaSDK Documentation
[![License](https://img.shields.io/github/license/deuna-developers/deuna-sdk-ios?style=flat-square)](https://github.com/deuna-developers/deuna-sdk-io/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios-blue?style=flat-square)](https://github.com/deuna-developers/deuna-sdk-ios#)

## Introduction

DeunaSDK is a Android-based SDK designed to facilitate integration with the DEUNA. This SDK provides a seamless way to initialize payments, handle success, error, and close actions, and manage configurations.

Get started with our [📚 integration guides](https://docs.deuna.com/docs/integracion-android-sdk) and [example projects](https://github.com/deuna-developers/deuna-sdk-android/tree/main/examples)



## FAQs
* ### How to get an **order token** ?
    To generate an order token, refer to our API documentation on our [API Referece](https://docs.deuna.com/reference/order_token)

* ### How to get an **user token** ?
    You'll need a registered user in DEUNA. Follow the instructions for ["User Registration"](https://docs.deuna.com/reference/users-register) in our API reference.

    Once you have a registered user, you can obtain an access token through a two-step process:

    **Request an OTP code**: Use our API for "[Requesting an OTP Code](https://docs.deuna.com/reference/request-otp)" via email.

    **Login with OTP code:** Use the retrieved code to "[Log in with OTP](https://docs.deuna.com/reference/login-with-otp)" and get an access token for your user.

## Author
DUENA Inc.

## License
DEUNA's SDKs and Libraries are available under the MIT license. See the LICENSE file for more info.
