#!/bin/bash

# Generate a random 8-character scheme for the WebView app
# Usage: ./generate-scheme.sh

SCHEME=$(head /dev/urandom | tr -dc 'a-z0-9' | head -c 8)
echo "Generated scheme: $SCHEME"
echo ""
echo "Add this to MainActivity.kt:"
echo "private const val DEFAULT_SCHEME = \"$SCHEME\""
echo ""
echo "Add this to AndroidManifest.xml:"
echo "<data android:scheme=\"$SCHEME\" android:host=\"setup\" />"
