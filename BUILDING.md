# Building talofa.me

## Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 34 (Android 14)
- JDK 17 or later
- Gradle 8.2+

## Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/Strangemother/android-share-button.git
   cd android-share-button
   ```

2. Build from command line:
   ```bash
   ./gradlew assembleDebug
   ```

   Or open in Android Studio and build there.

3. Find the APK at: `app/build/outputs/apk/debug/talofa-v1.0.1-debug.apk`

## Installing on Device

### Via Command Line
```bash
./gradlew installDebug
```

### Via APK Sideloading
1. Enable "Install from unknown sources" in your Android settings
2. Transfer the APK to your device
3. Open and install it

### Serving APK Over HTTP
For easy phone installation:
```bash
./serve-apk.sh
```
Then visit `http://[your-ip]:8000` from your phone's browser.

## Release Build

For production builds:

1. Create a keystore:
   ```bash
   keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias talofa
   ```

2. Configure signing in `app/build.gradle.kts` (see comments in file)

3. Build release:
   ```bash
   ./gradlew assembleRelease
   ```

## Dependencies

- AndroidX Core KTX
- AndroidX AppCompat
- Material Components for Android
- ConstraintLayout
- Kotlin Coroutines
- OkHttp (HTTP client)

## Architecture

- **MainActivity**: Configuration screen
- **ShareReceiverActivity**: Handles share intents
- **GroupSelectionBottomSheet**: Optional group selection UI
- **ConfigManager**: Persistent storage (SharedPreferences)
- **ApiClient**: HTTP communication (OkHttp)

## Technical Documentation

See `/docs/dev/` for detailed API flow and implementation details.
