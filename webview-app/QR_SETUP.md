# WebView App - QR Code Setup

Configure your WebView app URL via QR code or deep link.

## Deep Link Format

```
webview://setup?url=YOUR_WEBSITE_URL
```

## Examples

### Basic Setup
```
webview://setup?url=https://my-canvas-app.com
```

### Local Development
```
webview://setup?url=http://192.168.1.100:8080
```

### With Subdirectory
```
webview://setup?url=https://example.com/canvas/app
```

## Creating QR Codes

### Command Line
```bash
qrencode -o webview-setup-qr.png "webview://setup?url=https://your-site.com"
```

### Python
```python
import qrcode

url = "webview://setup?url=https://your-canvas-app.com"
qr = qrcode.make(url)
qr.save("webview-setup-qr.png")
```

### Node.js
```javascript
const QRCode = require('qrcode');

const url = 'webview://setup?url=https://your-canvas-app.com';
QRCode.toFile('webview-setup-qr.png', url);
```

## How It Works

1. User scans QR code with camera
2. Android opens WebView app
3. URL is saved to settings
4. WebView reloads with new URL
5. URL persists across app restarts

## JavaScript Interface

Your web page can query the current URL:

```javascript
// Get configured URL
const url = Android.getSiteUrl();
console.log('Running from:', url);
```

## Default URL

If no URL is configured, the app loads: `https://talofa.me`

To change the default, edit `MainActivity.kt`:
```kotlin
private const val DEFAULT_URL = "https://your-default-site.com"
```

## Testing

1. Generate QR code with your URL
2. Scan with phone camera
3. Tap notification to open app
4. App loads your site
5. URL is saved for next launch

## Reconfiguring

Just scan a new QR code to update the URL anytime!
