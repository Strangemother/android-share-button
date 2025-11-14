# Configuration Guide

This document explains how to customize the WebView app's default configuration.

## Single-Source Configuration

All configuration is managed through a **single file**: `app.properties`

## Configuration Values

The app has two main configuration values:

- **DEFAULT_SCHEME**: The custom URL scheme for deep links (default: `wv7f2a9c`)
- **DEFAULT_URL**: The default website to load (default: `https://talofa.me`)

## How to Edit

**Edit one file only:**
- File: `webview-app/app.properties`

```properties
DEFAULT_SCHEME=wv7f2a9c
DEFAULT_URL=https://talofa.me
```

That's it! The build system automatically injects these values into:
- Kotlin code (via `BuildConfig.DEFAULT_SCHEME` and `BuildConfig.DEFAULT_URL`)
- Android manifest (deep link intent filters)

## Generating Random Schemes

To generate a new random 8-character scheme identifier:

```bash
cd webview-app
./generate-scheme.sh
```

This will output a unique scheme like `wv9k3m2x`. Copy this value into `app.properties`.

## Why Change These?

- **DEFAULT_SCHEME**: Each deployment should use a unique scheme to avoid conflicts with other apps
- **DEFAULT_URL**: Point to your own web application instead of talofa.me

## After Changing

After modifying `app.properties`:

1. Rebuild: `./gradlew :webview-app:clean :webview-app:assembleRelease`
2. The new scheme will be: `<your-scheme>://setup?url=<website>`
3. Generate new QR codes with the updated scheme

    tolafoTolafo://setup?url=https://polypointjs.com&scheme=polypointWebview
    talofaTalofa://setup?url=https://polypointjs.com&scheme=polypointWebview
    
## Technical Details

The build configuration reads `app.properties` and:
- Generates `BuildConfig` constants accessible in Kotlin code
- Replaces `${defaultScheme}` placeholders in `AndroidManifest.xml`
- All changes are compile-time, no runtime overhead
