# Android Share Button

A lightweight Android 14+ application that provides a custom share button within typically supported apps. Users can share websites, links, or text to a configured HTTP endpoint.

## Features

- **Custom Share Target**: Appears in the Android share menu alongside other sharing options
- **Configurable Endpoint**: Set up your own API endpoint to receive shared content
- **Dynamic Configuration**: Fetch share button name, icon, and endpoint from your API
- **Simple HTTP POST**: Shared content is sent as JSON to your endpoint
- **Lightweight**: Minimal dependencies, focusing on core functionality

## How It Works

1. **Install the app** on your Android 14+ device
2. **Configure the endpoint**: Enter your API URL in the app
3. **Fetch configuration**: The app calls your API to get:
   - Share button name (displayed in share menu)
   - Icon URL (optional, displayed in share menu)
   - POST endpoint (where shared content will be sent)
4. **Share content**: Use the share button in any app, select your custom share target
5. **Content delivered**: The shared content is posted to your endpoint as JSON

## API Endpoint Requirements

### Configuration Endpoint (GET)

Your API endpoint should respond to GET requests with JSON:

```json
{
  "name": "My Custom Share",
  "icon": "https://example.com/icon.png",
  "endpoint": "https://example.com/api/share"
}
```

**Fields:**
- `name` (string, optional): Display name for the share target. Default: "Custom Share"
- `icon` (string, optional): URL to an icon image (PNG, JPG). Displayed in the configuration screen.
- `endpoint` (string, required): The URL where shared content will be POSTed

### Share Endpoint (POST)

When content is shared, the app sends a POST request with JSON body:

```json
{
  "content": "The shared text or URL",
  "type": "text",
  "timestamp": 1699876543210
}
```

**Fields:**
- `content` (string): The shared content (URL, text, etc.)
- `type` (string): Content type (currently always "text")
- `timestamp` (number): Unix timestamp in milliseconds

## Building the App

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 34 (Android 14)
- JDK 17 or later
- Gradle 8.2+

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/Strangemother/android-share-button.git
   cd android-share-button
   ```

2. Open the project in Android Studio or build from command line:
   ```bash
   ./gradlew assembleDebug
   ```

3. Install on device:
   ```bash
   ./gradlew installDebug
   ```

   Or find the APK at: `app/build/outputs/apk/debug/app-debug.apk`

## Usage

### Initial Setup

1. Open the "Share Button" app
2. Enter your API endpoint URL (e.g., `https://your-server.com/api/config`)
3. Tap "Setup"
4. The app will fetch configuration from your endpoint
5. Verify the displayed name and icon

### Sharing Content

1. In any app (browser, notes, etc.), tap the Share button
2. Look for your custom share target name (e.g., "My Custom Share")
3. Select it to share the content
4. A toast message confirms successful sharing

### Testing

Use the "Test Share" button in the app to trigger the Android share sheet with test content.

## Technical Details

### Architecture

- **MainActivity**: Configuration screen where users set up the API endpoint
- **ShareReceiverActivity**: Handles incoming share intents (ACTION_SEND)
- **ConfigManager**: Manages persistent storage using SharedPreferences
- **ApiClient**: Handles all HTTP communication using OkHttp

### Permissions

- `INTERNET`: Required to communicate with your API endpoint

### Dependencies

- AndroidX Core KTX
- AndroidX AppCompat
- Material Components for Android
- ConstraintLayout
- Kotlin Coroutines
- OkHttp (HTTP client)

### Supported Share Types

Currently supports:
- Plain text (`text/plain`)
- URLs shared as text
- Text content from various apps

Future versions may support:
- Images
- Files
- Multiple items

## Security Considerations

- The app supports cleartext HTTP traffic for development (remove in production)
- Configure your endpoint to use HTTPS in production
- Implement authentication on your endpoint if needed
- The app stores configuration in SharedPreferences (unencrypted)

## Example Server Implementation

Here's a simple Node.js/Express example:

```javascript
const express = require('express');
const app = express();

app.use(express.json());

// Configuration endpoint
app.get('/api/config', (req, res) => {
  res.json({
    name: "My Share List",
    icon: "https://example.com/icon.png",
    endpoint: "https://example.com/api/share"
  });
});

// Share endpoint
app.post('/api/share', (req, res) => {
  const { content, type, timestamp } = req.body;
  console.log('Received share:', content);
  
  // Save to database, send notification, etc.
  
  res.json({ success: true });
});

app.listen(3000);
```

## Troubleshooting

**"Please configure the app first"**: Open the app and set up your API endpoint.

**"Setup failed"**: Check that your API endpoint is accessible and returns valid JSON.

**"Share failed"**: Verify your POST endpoint is working and accepts JSON.

**Icon not displaying**: Check the icon URL is accessible and is a valid image format.

## License

MIT License - see LICENSE file for details

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
