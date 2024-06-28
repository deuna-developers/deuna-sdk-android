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