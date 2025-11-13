# Implementation Summary

This document provides a technical overview of the Android Share Button implementation.

## Project Structure

```
android-share-button/
├── app/
│   ├── build.gradle.kts           # App-level build configuration
│   ├── proguard-rules.pro         # ProGuard rules for release builds
│   └── src/main/
│       ├── AndroidManifest.xml    # App manifest with intent filters
│       ├── java/com/sharebutton/app/
│       │   ├── MainActivity.kt              # Configuration UI
│       │   ├── ShareReceiverActivity.kt     # Share intent handler
│       │   ├── ConfigManager.kt             # Configuration storage
│       │   └── ApiClient.kt                 # HTTP client
│       └── res/
│           ├── drawable/          # Vector drawables
│           ├── layout/            # UI layouts
│           ├── mipmap-*/          # Launcher icons
│           └── values/            # Strings, colors, themes
├── build.gradle.kts               # Project-level build configuration
├── settings.gradle.kts            # Project settings
├── gradle.properties              # Gradle properties
├── gradlew                        # Gradle wrapper script
├── example-server.js              # Node.js example server
├── example-server.py              # Python example server
├── docker-compose.yml             # Docker deployment
├── package.json                   # Node.js dependencies
├── requirements.txt               # Python dependencies
├── README.md                      # Project overview
├── SETUP.md                       # Setup guide
└── API.md                         # API documentation
```

## Core Components

### 1. MainActivity.kt

**Purpose:** Configuration screen where users set up the API endpoint.

**Key Features:**
- Text input for API endpoint URL
- "Setup" button to fetch configuration from API
- Display current configuration (name, endpoint)
- Load and display icon from URL
- "Test Share" button to test the share functionality

**Implementation Details:**
- Uses View Binding for type-safe view access
- Coroutines for asynchronous API calls
- SharedPreferences for persistent storage
- OkHttp for icon image loading

**User Flow:**
1. User enters API endpoint URL
2. Taps "Setup" button
3. App fetches configuration from API
4. Configuration is saved and displayed
5. Icon is downloaded and shown (if provided)

### 2. ShareReceiverActivity.kt

**Purpose:** Handles incoming share intents from other apps.

**Key Features:**
- Receives ACTION_SEND intents
- Extracts shared text/URL and metadata
- Posts content to configured endpoint
- Shows toast notifications for feedback
- Transparent theme for seamless UX

**Implementation Details:**
- Registered in AndroidManifest with intent filters for text/plain
- Checks configuration before processing
- Builds JSON payload with content, type, timestamp
- Uses coroutines for non-blocking POST request
- Automatically finishes after posting

**Share Flow:**
1. User shares content from another app
2. Android shows share sheet with app's share target
3. User selects the custom share target
4. App receives shared content
5. Content is POSTed to endpoint
6. User sees success/error toast
7. Activity closes

### 3. ConfigManager.kt

**Purpose:** Manages persistent storage of configuration data.

**Storage:** SharedPreferences (key-value storage)

**Stored Data:**
- API URL (configuration endpoint)
- Share name (display name)
- Icon URL (optional)
- POST endpoint (where content is sent)

**Methods:**
- Getters/setters for each configuration field
- `isConfigured()`: Checks if app is set up
- `clear()`: Removes all configuration

### 4. ApiClient.kt

**Purpose:** Handles all HTTP communication.

**Dependencies:**
- OkHttp for HTTP requests
- Kotlin Coroutines for async operations
- org.json for JSON parsing

**Methods:**

**`fetchConfiguration(apiUrl: String)`**
- GET request to fetch configuration
- Returns sealed class result (Success/Error)
- Parses JSON response
- Extracts name, icon, endpoint fields

**`postSharedContent(endpoint: String, content: String, contentType: String)`**
- POST request to send shared content
- Builds JSON payload
- Returns sealed class result (Success/Error)
- 30-second timeout for all operations

## Android Manifest Configuration

### Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
Required for HTTP communication.

### Application Configuration
```xml
android:usesCleartextTraffic="true"
```
Allows HTTP traffic (for development). Remove for production.

### Intent Filters
```xml
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="text/plain" />
</intent-filter>
```
Registers the app as a share target for plain text.

## Build Configuration

### Target SDK
- Minimum SDK: 34 (Android 14)
- Target SDK: 34 (Android 14)
- Compile SDK: 34

### Dependencies
- AndroidX Core KTX: Core Android Kotlin extensions
- AndroidX AppCompat: Backward compatibility
- Material Components: Material Design UI
- ConstraintLayout: Modern layout system
- Kotlin Coroutines: Asynchronous programming
- OkHttp: HTTP client

### Build Types
- **Debug**: Development builds with debugging enabled
- **Release**: Optimized production builds

## Data Flow

### Configuration Flow
```
User enters URL
    ↓
MainActivity calls ApiClient.fetchConfiguration()
    ↓
GET request to API
    ↓
API returns JSON {name, icon, endpoint}
    ↓
ConfigManager saves configuration
    ↓
MainActivity displays configuration
    ↓
MainActivity loads icon from URL
```

### Share Flow
```
User shares from another app
    ↓
Android shows share sheet
    ↓
User selects custom share target
    ↓
ShareReceiverActivity receives intent
    ↓
Extract shared content and metadata
    ↓
Build JSON payload
    ↓
ApiClient.postSharedContent()
    ↓
POST request to endpoint
    ↓
Show success/error toast
    ↓
Activity finishes
```

## JSON Formats

### Configuration Response (GET /api/config)
```json
{
  "name": "Display name for share target",
  "icon": "https://example.com/icon.png",
  "endpoint": "https://example.com/api/share"
}
```

### Share Request (POST /api/share)
```json
{
  "content": "Shared text or URL",
  "type": "text",
  "timestamp": 1699876543210
}
```

### Share Response
```json
{
  "success": true,
  "message": "Optional message",
  "id": "Optional identifier"
}
```

## UI Components

### MainActivity Layout
- TextInputLayout with EditText for URL input
- Button for setup
- TextViews for status display
- ImageView for icon display
- Button for testing share functionality

### Themes
- **Theme.ShareButton**: Main app theme (Material Design)
- **Theme.ShareButton.Transparent**: Transparent theme for share receiver

### Resources
- **strings.xml**: All user-facing text
- **colors.xml**: Material Design color palette
- **themes.xml**: Theme definitions

## Security Considerations

### Current Implementation
- Allows cleartext (HTTP) traffic for development
- No authentication on API calls
- Configuration stored unencrypted in SharedPreferences

### Production Recommendations
1. **Use HTTPS**: Disable cleartext traffic
2. **Add Authentication**: Implement API key or OAuth
3. **Encrypt Storage**: Use EncryptedSharedPreferences
4. **Input Validation**: Validate and sanitize all inputs
5. **Rate Limiting**: Implement on server side
6. **Error Handling**: Don't expose sensitive info in errors

## Extension Points

### Supporting More Content Types

Add intent filters in AndroidManifest:
```xml
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="image/*" />
</intent-filter>
```

Update ShareReceiverActivity to handle images:
```kotlin
when (intent.type) {
    "image/*" -> handleImageShare()
    "text/plain" -> handleTextShare()
}
```

### Adding Authentication

Update ApiClient to include auth headers:
```kotlin
val request = Request.Builder()
    .url(endpoint)
    .header("Authorization", "Bearer $token")
    .post(requestBody)
    .build()
```

### Custom Share Metadata

Extend JSON payload in ApiClient:
```kotlin
val json = JSONObject().apply {
    put("content", content)
    put("type", contentType)
    put("timestamp", System.currentTimeMillis())
    put("source", getSourceApp())
    put("userId", getUserId())
}
```

## Testing Strategies

### Unit Tests
- Test ConfigManager storage/retrieval
- Test ApiClient request/response handling
- Test JSON parsing

### Integration Tests
- Test MainActivity UI interactions
- Test ShareReceiverActivity intent handling
- Test end-to-end share flow

### Manual Testing
1. Install app on device
2. Configure with test server
3. Share from various apps (Chrome, Notes, etc.)
4. Verify content reaches server
5. Test error scenarios (no network, invalid endpoint)

## Known Limitations

1. **Content Types**: Currently only supports text/plain
2. **File Size**: No limit on content size (could cause memory issues)
3. **Network**: No offline queuing (fails if no network)
4. **Auth**: No built-in authentication mechanism
5. **Icon Format**: Only supports PNG/JPEG for icons
6. **Error Reporting**: Limited error details to user

## Future Enhancements

1. **Multiple Endpoints**: Support multiple configured endpoints
2. **Content Queue**: Queue shares when offline
3. **Image Support**: Share images and files
4. **Share History**: View previously shared items
5. **Authentication**: Built-in OAuth/API key support
6. **Notifications**: Background sync with notifications
7. **Custom Fields**: User-configurable metadata fields
8. **Share Preview**: Preview before sending
9. **Batch Sharing**: Share multiple items at once
10. **Analytics**: Track share success/failure rates

## Development Notes

### Building the App
```bash
./gradlew assembleDebug    # Build debug APK
./gradlew installDebug     # Install on connected device
./gradlew assembleRelease  # Build release APK
```

### Debugging
- Use Android Studio's debugger
- Check Logcat for logs
- Use Chrome DevTools for API debugging
- Test with example servers first

### Code Style
- Kotlin coding conventions
- Material Design guidelines
- AndroidX best practices
- Coroutines for async operations

## Support and Contribution

For issues, questions, or contributions:
- GitHub Issues: Report bugs and request features
- Pull Requests: Contribute improvements
- Discussions: Ask questions and share use cases

## License

MIT License - See LICENSE file for details.
