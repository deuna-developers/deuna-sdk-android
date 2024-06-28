# Example app using  the DEUNA SDK with jetpack compose

## Run locally

1. Open the example project `deuna-sdk-android/examples/basic-integration-jetpack-compose` in Android Studio.
2. Wait for Android Studio to download the project dependencies and index the project.
3. Go to the MainActivity.kt file and locate the following code snippet. Replace `YOUR_PUBLIC_API_KEY` with your actual public API key and choose the appropriate environment (e.g., `SANDBOX` or `PRODUCTION`).
    ```kotlin
    DeunaSDK(
        environment = Environment.SANDBOX,
        publicApiKey = "YOUR_PUBLIC_API_KEY"
    )
    ```
4. Run the project.