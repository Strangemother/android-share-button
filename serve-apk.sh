#!/bin/bash

# Serve APK over HTTP for easy phone download

set -e

PORT=8000

echo "Starting HTTP server on port $PORT..."
echo "Press Ctrl+C to stop"
echo ""

# Start Python HTTP server
cd app/build/outputs/apk/debug
python3 -m http.server $PORT
