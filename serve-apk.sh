#!/bin/bash

# Serve APK over HTTP for easy phone download

set -e

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
PORT=8080

echo "=========================================="
echo "APK Download Server"
echo "=========================================="
echo ""

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo "Error: APK not found at $APK_PATH"
    echo "Run ./build.sh first to build the APK"
    exit 1
fi

# Get the codespace URL if we're in GitHub Codespaces
if [ ! -z "$CODESPACE_NAME" ]; then
    URL="https://${CODESPACE_NAME}-${PORT}.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}/app-debug.apk"
    echo "📱 Download URL for your phone:"
    echo ""
    echo "   $URL"
    echo ""
    echo "Or scan this QR code:"
    echo ""
    # Generate QR code if qrencode is available
    if command -v qrencode &> /dev/null; then
        qrencode -t ANSIUTF8 "$URL"
    else
        echo "(Install qrencode to see QR code: apt install qrencode)"
    fi
    echo ""
else
    # Get local IP
    LOCAL_IP=$(hostname -I | awk '{print $1}')
    echo "📱 Download URL (from same network):"
    echo ""
    echo "   http://${LOCAL_IP}:${PORT}/app-debug.apk"
    echo ""
fi

echo "=========================================="
echo "Starting server on port $PORT..."
echo "Press Ctrl+C to stop"
echo "=========================================="
echo ""

# Start Python HTTP server
cd app/build/outputs/apk/debug
python3 -m http.server $PORT
