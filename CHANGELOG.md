## [2.4.0-beta.1]
- Added support for the new payment widget. Check the implementation [here](https://docs.deuna.com/docs/integracion-payment-widget-android).

* **BREAKING CHANGES:**
* `OrderResponse` was replaced by `Map<String,Any>`.
* `ElementsResponse` was replaced by `Map<String,Any>`].
* `CheckoutError` was replaced by `PaymentsError`.

## [2.1.1]
- Set compileSdk to 32.

## [2.0.3]
- Fixed sandbox domains.

## [2.0.2]
- Updated dependency version to `2.0.2`.

## [2.0.1]
- Fixed bad elements URL on production.

## [2.0.0]

### Changed

* **Renamed classes:**
  * `DeUnaSdk` to `DeunaSDK`
  * `Callbacks` to `CheckoutCallbacks`
  * `OrderResponse` to `CheckoutResponse`
  * `CheckoutEvents` to `CheckoutEvent`
* **Changed parameter order in event listener handlers:**
  * **Before:**
      ```kotlin
      Callbacks().apply {
          eventListener = { response, type ->
              
          }
      }
      
      ElementCallbacks().apply {
          eventListener = { response, type ->
              
          }
      }
      ```
  * **Now:**
      ```kotlin
      CheckoutCallbacks().apply {
          eventListener = { type, response ->
              
          }
      }
      
      ElementCallbacks().apply {
          eventListener = { type, response ->
              
          }
      }
      ```