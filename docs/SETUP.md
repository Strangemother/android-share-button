# Setup Guide

This guide will walk you through setting up the Android Share Button app and creating your own API endpoint.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Building the Android App](#building-the-android-app)
3. [Setting Up Your API Server](#setting-up-your-api-server)
4. [Connecting the App to Your Server](#connecting-the-app-to-your-server)
5. [Testing](#testing)
6. [Troubleshooting](#troubleshooting)

## Quick Start

The fastest way to get started is to use one of the example servers provided:

### Option 1: Node.js Example Server

```bash
# Install dependencies
npm install express body-parser

# Run the server
node example-server.js
```

### Option 2: Python Example Server

```bash
# Install dependencies
pip install flask

# Run the server
python example-server.py
```

## Building the Android App

### Prerequisites

- Android Studio (recommended) OR
- Android SDK with command-line tools
- JDK 17 or later
- A physical Android device running Android 14+ (API 34+) OR an emulator

### Using Android Studio

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Strangemother/android-share-button.git
   cd android-share-button
   ```

2. **Open in Android Studio:**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Build and Install:**
   - Connect your Android device via USB (or start an emulator)
   - Enable USB debugging on your device
   - Click the "Run" button (green triangle) in Android Studio
   - Select your device from the deployment target

### Using Command Line

1. **Build the APK:**
   ```bash
   cd android-share-button
   ./gradlew assembleDebug
   ```

2. **Install on device:**
   ```bash
   # Via Gradle
   ./gradlew installDebug
   
   # Or via ADB
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Setting Up Your API Server

Your API server needs to implement two endpoints:

### 1. Configuration Endpoint (GET)

**Purpose:** Provides the app with configuration during setup.

**Response Format:**
```json
{
  "name": "My Share Button",
  "icon": "https://example.com/icon.png",
  "endpoint": "https://example.com/api/share"
}
```

**Example Implementation (Node.js):**
```javascript
app.get('/api/config', (req, res) => {
  res.json({
    name: "My Personal List",
    icon: "https://example.com/icon.png",
    endpoint: "https://example.com/api/share"
  });
});
```

**Example Implementation (Python/Flask):**
```python
@app.route('/api/config', methods=['GET'])
def get_config():
    return jsonify({
        'name': 'My Personal List',
        'icon': 'https://example.com/icon.png',
        'endpoint': 'https://example.com/api/share'
    })
```

### 2. Share Endpoint (POST)

**Purpose:** Receives shared content from the app.

**Request Format:**
```json
{
  "content": "The shared text or URL",
  "type": "text",
  "timestamp": 1699876543210
}
```

**Example Implementation (Node.js):**
```javascript
app.post('/api/share', (req, res) => {
  const { content, type, timestamp } = req.body;
  
  // Your custom logic here
  console.log('Received:', content);
  
  res.json({ 
    success: true,
    message: 'Content received'
  });
});
```

**Example Implementation (Python/Flask):**
```python
@app.route('/api/share', methods=['POST'])
def receive_share():
    data = request.get_json()
    content = data.get('content')
    
    # Your custom logic here
    print(f'Received: {content}')
    
    return jsonify({
        'success': True,
        'message': 'Content received'
    })
```

## Connecting the App to Your Server

### Local Development (Device and Computer on Same Network)

1. **Find your computer's local IP address:**

   **On Windows:**
   ```cmd
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., 192.168.1.100)

   **On macOS/Linux:**
   ```bash
   ifconfig
   # or
   ip addr show
   ```
   Look for "inet" address (e.g., 192.168.1.100)

2. **Start your API server:**
   ```bash
   node example-server.js
   # or
   python example-server.py
   ```

3. **Configure the Android app:**
   - Open the Share Button app on your device
   - Enter the configuration URL: `http://YOUR_IP:3000/api/config`
   - Example: `http://192.168.1.100:3000/api/config`
   - Tap "Setup"
   - Verify the configuration is displayed

### Production Deployment

For production use, deploy your API server to a public hosting service:

**Popular Options:**
- **Heroku**: Easy deployment, free tier available
- **AWS Lambda**: Serverless, pay per use
- **DigitalOcean**: Simple VPS hosting
- **Google Cloud Run**: Containerized deployment
- **Vercel/Netlify**: For serverless functions

**Important for Production:**
- Use HTTPS (not HTTP)
- Implement authentication if needed
- Add rate limiting
- Set up proper error handling
- Use a database for persistence

## Testing

### 1. Test Configuration

In the Share Button app:
- Tap "Setup" to fetch configuration
- Verify your share button name is displayed
- Check if the icon loads (if provided)

### 2. Test Sharing

**Using the Test Button:**
1. In the Share Button app, tap "Test Share"
2. Select your custom share target from the share menu
3. Check your server logs for the received content

**Using a Real App:**
1. Open Chrome, a notes app, or any app with sharing
2. Find content to share (URL, text, etc.)
3. Tap the Share button
4. Select your custom share target
5. Verify content arrives at your server

### 3. Verify Server Side

Check your server logs or database to confirm:
- Configuration requests are received
- Share POST requests contain correct data
- Responses are sent successfully

## Troubleshooting

### "Could not resolve host" or "Connection failed"

**Causes:**
- Device can't reach the server
- Firewall blocking the connection
- Wrong IP address or port

**Solutions:**
- Verify device and computer are on the same network
- Check firewall settings on your computer
- Try pinging your computer from the device
- Ensure the server is running

### "Setup failed: HTTP 404"

**Causes:**
- Configuration endpoint URL is incorrect
- Server is not running
- Endpoint path is wrong

**Solutions:**
- Verify the URL matches your server's configuration endpoint
- Check server logs for the request
- Test the endpoint in a browser first

### "Share failed"

**Causes:**
- POST endpoint is not accessible
- Server returned an error
- Request format is incorrect

**Solutions:**
- Check server logs for error messages
- Verify POST endpoint is configured correctly
- Test with curl: `curl -X POST http://YOUR_IP:3000/api/share -H "Content-Type: application/json" -d '{"content":"test"}'`

### Share target doesn't appear in share menu

**Causes:**
- App not properly installed
- Android intent filters not registered

**Solutions:**
- Reinstall the app
- Restart your device
- Check AndroidManifest.xml for correct intent filters

### Icon not displaying

**Causes:**
- Icon URL is not accessible
- Image format not supported
- HTTPS certificate issues

**Solutions:**
- Test icon URL in a browser
- Use a direct link to an image file
- Ensure image is PNG or JPEG
- Use HTTPS URLs with valid certificates

## Advanced Configuration

### Custom Share Types

To support additional content types, modify `AndroidManifest.xml`:

```xml
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="image/*" />
</intent-filter>
```

### Authentication

Add authentication headers in `ApiClient.kt`:

```kotlin
val request = Request.Builder()
    .url(endpoint)
    .header("Authorization", "Bearer YOUR_TOKEN")
    .post(requestBody)
    .build()
```

### Custom Data Fields

Modify the JSON payload in `ApiClient.kt` to include additional fields:

```kotlin
val json = JSONObject().apply {
    put("content", content)
    put("type", contentType)
    put("timestamp", System.currentTimeMillis())
    put("deviceId", getDeviceId())
    put("appVersion", BuildConfig.VERSION_NAME)
}
```

## Next Steps

- Customize the app icon and name
- Implement authentication in your API
- Add database persistence to your server
- Set up notifications when content is received
- Deploy your server to production
- Share your use case with the community!

## Support

For issues and questions:
- GitHub Issues: https://github.com/Strangemother/android-share-button/issues
- Documentation: Check README.md for detailed information
