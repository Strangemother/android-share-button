# Production Deployment Guide

## Overview

This guide covers deploying the Android Share Button app to production.

## Production-Ready Features

✅ **Code Optimization**
- ProGuard enabled for code shrinking and obfuscation
- Resource shrinking enabled to reduce APK size
- Debug logging removed in release builds

✅ **Security**
- Network security config enforces HTTPS (localhost allowed for testing)
- Cleartext traffic disabled for production
- Backup rules configured
- Sensitive data excluded from backups

✅ **Build Variants**
- **Debug**: `com.sharebutton.app.debug` - For development and testing
- **Release**: `com.sharebutton.app` - For production distribution

✅ **Proper Versioning**
- Version code: 1
- Version name: 1.0.0
- Archives named: `share-button-v1.0.0`

## Building for Production

### 1. Debug Build (Testing)

```bash
./build.sh
```

This creates a debug APK at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Release Build (Production)

```bash
./build-release.sh
```

This creates an unsigned release APK with optimizations enabled.

## Signing the Release APK

For production distribution, you **must** sign the APK:

### Step 1: Create a Keystore (One Time)

```bash
keytool -genkey -v -keystore release-key.jks \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -alias share-button
```

**⚠️ IMPORTANT**: 
- Store `release-key.jks` securely
- Never commit it to version control
- Remember your passwords!

### Step 2: Sign the APK

```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
          -keystore release-key.jks \
          app/build/outputs/apk/release/app-release-unsigned.apk \
          share-button
```

### Step 3: Optimize with zipalign

```bash
$ANDROID_HOME/build-tools/34.0.0/zipalign -v 4 \
    app/build/outputs/apk/release/app-release-unsigned.apk \
    app-release-signed.apk
```

## Automated Signing (Recommended)

Add this to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "share-button"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other settings
        }
    }
}
```

Then set environment variables:

```bash
export KEYSTORE_PASSWORD="your_store_password"
export KEY_PASSWORD="your_key_password"
./gradlew assembleRelease
```

## Distribution

### Google Play Store

1. Sign the APK as described above
2. Create an App Bundle (recommended):
   ```bash
   ./gradlew bundleRelease
   ```
3. Upload to Google Play Console
4. Follow the store listing requirements

### Direct Distribution

1. Build and sign the release APK
2. Upload to your server
3. Users must enable "Install from Unknown Sources"
4. Provide the download link

### Self-Hosting

Use the included `serve-apk.sh` script for quick distribution:

```bash
./serve-apk.sh
```

## Version Management

When releasing new versions:

1. Update version in `app/build.gradle.kts`:
   ```kotlin
   defaultConfig {
       versionCode = 2  // Increment for each release
       versionName = "1.1.0"  // Semantic versioning
   }
   ```

2. Create a git tag:
   ```bash
   git tag -a v1.1.0 -m "Version 1.1.0"
   git push origin v1.1.0
   ```

## Security Checklist

Before production release:

- [ ] Release APK is signed with your keystore
- [ ] Keystore is backed up securely
- [ ] `usesCleartextTraffic` is set to `false` (or uses network security config)
- [ ] ProGuard rules are tested
- [ ] No API keys or secrets in the code
- [ ] Test on multiple Android versions (7.0 - 14+)
- [ ] Test with HTTPS endpoints only

## Testing Production Build

1. Install the signed release APK on a test device
2. Test all functionality with production API endpoints
3. Verify ProGuard hasn't broken any features
4. Check APK size (should be smaller than debug)
5. Test on devices running Android 7.0 (minSdk)

## APK Size Optimization

Expected sizes:
- Debug APK: ~13-15 MB
- Release APK (with ProGuard): ~8-10 MB

To further reduce size:
- Enable `isShrinkResources = true` (already done)
- Remove unused dependencies
- Use vector drawables instead of PNGs
- Consider using App Bundle format

## Troubleshooting

### "App not installed" error
- Ensure APK is properly signed
- Check that package name matches (no `.debug` suffix)
- Uninstall any previous versions

### Network security errors
- Verify your API uses HTTPS
- Check `network_security_config.xml` if using custom certificates

### ProGuard issues
- Check `app/build/outputs/mapping/release/` for obfuscation mapping
- Add necessary `-keep` rules in `proguard-rules.pro`

## Support

For issues or questions, check:
- `README.md` - General app information
- `SETUP.md` - Development setup
- `API.md` - API documentation
