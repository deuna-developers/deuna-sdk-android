# Explore (Android SDK Example)

`Explore` is the official Android example app for DEUNA SDK integrations.

## What it includes

- Embedded and modal widget flows in one app.
- A side drawer to switch environment, widget type, and runtime options.
- Product catalog with cart for app-side order tokenization.
- Wallets screen (Google Pay) via `DeunaSDK.initElements`.

## Run locally

Open `deuna-sdk-android` in Android Studio and run the `:explore` module.

Make sure `settings.gradle.kts` includes:
```kotlin
include("explore")
project(":explore").projectDir = file("examples/explore")
```

## Architecture

```
domain/          — entities (ExploreWidget, IntegrationConfig, …)
data/            — ConfigStorage (SharedPreferences), ProductCatalog, MerchantService, OrderTokenService
presentation/
  ExploreViewModel  — state + actions (mirrors iOS ExploreCoordinator)
  navigation/       — NavHost with payment-success, card-saved, wallets routes
  screens/
    main/           — MainScreen, TopBar, ModeContent
    drawer/         — ConfigurationDrawer + all drawer sections
    widgets/        — EmbeddedScreen, ModalScreen
    result/         — PaymentSuccessScreen, CardSavedSuccessScreen
    wallets/        — WalletsScreen (Google Pay)
```

## Differences from iOS

| iOS | Android |
|-----|---------|
| Keychain | SharedPreferences |
| SwiftUI NavigationStack | Compose NavHost |
| ObservableObject coordinator | ViewModel + StateFlow |
| Apple Pay | Google Pay |
| Async/await | Coroutines |
