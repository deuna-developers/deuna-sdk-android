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