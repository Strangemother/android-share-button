#!/bin/bash

# Android Share Button - Release Build Script
# This script builds the production-ready release APK

set -e

echo "=========================================="
echo "Android Share Button - Release Build"
echo "=========================================="
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    echo -e "${RED}Error: Must run from project root directory${NC}"
    exit 1
fi

# Make gradlew executable
if [ ! -x "gradlew" ]; then
    chmod +x gradlew
fi

echo -e "${YELLOW}Building RELEASE APK...${NC}"
echo ""
echo "⚠️  Note: For production, you should sign the APK with a release keystore."
echo "   See: https://developer.android.com/studio/publish/app-signing"
echo ""

# Clean and build release
./gradlew clean assembleRelease

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo "Release Build Successful!"
    echo -e "==========================================${NC}"
    echo ""
    
    APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
    
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
        echo -e "${GREEN}APK Location:${NC} $APK_PATH"
        echo -e "${GREEN}APK Size:${NC} $APK_SIZE"
        echo ""
        echo -e "${YELLOW}⚠️  This APK is UNSIGNED and should be signed before distribution${NC}"
        echo ""
        echo "To sign the APK:"
        echo "  1. Create a keystore (one time):"
        echo "     keytool -genkey -v -keystore release-key.jks \\"
        echo "             -keyalg RSA -keysize 2048 -validity 10000 \\"
        echo "             -alias share-button"
        echo ""
        echo "  2. Sign the APK:"
        echo "     jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \\"
        echo "               -keystore release-key.jks $APK_PATH share-button"
        echo ""
        echo "  3. Optimize with zipalign:"
        echo "     zipalign -v 4 $APK_PATH app-release-signed.apk"
        echo ""
    fi
else
    echo ""
    echo -e "${RED}=========================================="
    echo "Build Failed!"
    echo -e "==========================================${NC}"
    exit 1
fi
