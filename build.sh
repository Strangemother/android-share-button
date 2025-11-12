#!/bin/bash

# Android Share Button Build Script
# This script builds the debug APK for testing

set -e  # Exit on error

echo "=========================================="
echo "Android Share Button - Build Script"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    echo -e "${RED}Error: Must run from project root directory${NC}"
    exit 1
fi

# Make gradlew executable if it isn't already
if [ ! -x "gradlew" ]; then
    echo -e "${YELLOW}Making gradlew executable...${NC}"
    chmod +x gradlew
fi

# Clean build (optional - comment out if you want faster incremental builds)
echo -e "${YELLOW}Cleaning previous build...${NC}"
./gradlew clean

# Build the debug APK
echo ""
echo -e "${YELLOW}Building debug APK...${NC}"
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo "Build Successful!"
    echo -e "==========================================${NC}"
    echo ""
    
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
        echo -e "${GREEN}APK Location:${NC} $APK_PATH"
        echo -e "${GREEN}APK Size:${NC} $APK_SIZE"
        echo ""
        echo "To install on your phone:"
        echo "  adb install $APK_PATH"
        echo ""
    fi
else
    echo ""
    echo -e "${RED}=========================================="
    echo "Build Failed!"
    echo -e "==========================================${NC}"
    exit 1
fi
