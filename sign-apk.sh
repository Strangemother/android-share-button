#!/bin/bash

# Quick Sign Script - Signs the release APK with a debug key for testing

set -e

UNSIGNED_APK="app/build/outputs/apk/release/share-button-v1.0.0-release-unsigned.apk"
SIGNED_APK="app/build/outputs/apk/release/share-button-v1.0.0-release-signed.apk"
DEBUG_KEYSTORE="$HOME/.android/debug.keystore"

echo "=========================================="
echo "Quick Sign Release APK"
echo "=========================================="
echo ""

# Check if unsigned APK exists
if [ ! -f "$UNSIGNED_APK" ]; then
    echo "Error: Unsigned APK not found at $UNSIGNED_APK"
    echo "Run ./build-release.sh first"
    exit 1
fi

# Check if debug keystore exists
if [ ! -f "$DEBUG_KEYSTORE" ]; then
    echo "Creating debug keystore..."
    keytool -genkey -v -keystore "$DEBUG_KEYSTORE" \
            -storepass android -alias androiddebugkey \
            -keypass android -keyalg RSA -keysize 2048 \
            -validity 10000 \
            -dname "CN=Android Debug,O=Android,C=US"
fi

echo "Signing APK with debug key..."
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
          -keystore "$DEBUG_KEYSTORE" \
          -storepass android -keypass android \
          "$UNSIGNED_APK" androiddebugkey

echo ""
echo "Optimizing with zipalign..."

# Find zipalign
ZIPALIGN=$(find $ANDROID_HOME/build-tools -name zipalign 2>/dev/null | head -1)

if [ -z "$ZIPALIGN" ]; then
    echo "Warning: zipalign not found, skipping optimization"
    cp "$UNSIGNED_APK" "$SIGNED_APK"
else
    $ZIPALIGN -f -v 4 "$UNSIGNED_APK" "$SIGNED_APK"
fi

echo ""
echo "=========================================="
echo "✅ APK Signed Successfully!"
echo "=========================================="
echo ""
echo "Signed APK: $SIGNED_APK"
ls -lh "$SIGNED_APK"
echo ""
echo "You can now install this APK on your device"
