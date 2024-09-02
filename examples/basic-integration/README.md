## Run locally

1. Open the example project `deuna-sdk-android/examples/basic-integration` in Android Studio.
2. Wait for Android Studio to download the project dependencies and index the project.
3. Navigate to the `MainActivity.kt` file and locate the following code snippet. Replace `YOUR_API_KEY` with your actual public API key and choose the appropriate environment (e.g., `SANDBOX` or `PRODUCTION`).

    ```kotlin
    private val deunaSdk = DeunaSDK(
      environment = Environment.SANDBOX, // use the appropriate environment
      publicApiKey = "YOUR_PUBLIC_API_KEY", // use your public API key
    );
    ```
4. Run the project.