# WebView App - QR Code Setup

Configure your WebView app URL via QR code or deep link with a unique custom scheme.

## Default Unique Scheme

The app uses a unique 8-character scheme by default: `wv7f2a9c://`

This prevents conflicts with other apps and provides security through obscurity.

## Deep Link Formats

### Basic Setup
```
wv7f2a9c://setup?url=YOUR_WEBSITE_URL
```

### With Custom Scheme (changes future deep links)
```
wv7f2a9c://setup?url=YOUR_WEBSITE_URL&scheme=myapp123
```

After setting a custom scheme, use your new scheme for future configurations:
```
myapp123://setup?url=ANOTHER_URL
```

## Examples

### Initial Setup with Default Scheme
```
wv7f2a9c://setup?url=https://my-canvas-app.com
```

### Setup with Custom Scheme
```
wv7f2a9c://setup?url=https://my-canvas-app.com&scheme=canvas42
```

### Use Custom Scheme Later
```
canvas42://setup?url=https://updated-app.com
```

### Local Development
```
wv7f2a9c://setup?url=http://192.168.1.100:8080
```

### Legacy Support (still works)
```
webview://setup?url=https://example.com
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

Your web page can query the current configuration:

```javascript
// Get configured URL
const url = Android.getSiteUrl();
console.log('Running from:', url);

// Get current scheme
const scheme = Android.getScheme();
console.log('Deep link scheme:', scheme + '://');

// Get full config
const config = JSON.parse(Android.getConfig());
console.log('Config:', config);
// Returns: { siteUrl, scheme, deepLink }
```

## Custom Scheme Rules

Custom schemes must be:
- 4-16 characters long
- Lowercase letters and numbers only
- No spaces or special characters

**Good:** `myapp123`, `canvas42`, `wv7f2a9c`  
**Bad:** `My-App`, `canvas_app`, `ab`

## Default Values

If no configuration is set:
- **URL:** `https://talofa.me`
- **Scheme:** `wv7f2a9c`

To change defaults, edit `MainActivity.kt`:
```kotlin
private const val DEFAULT_URL = "https://your-default-site.com"
private const val DEFAULT_SCHEME = "yourapp8"  // 8 chars
```

## Testing

1. Generate QR code with your URL
2. Scan with phone camera
3. Tap notification to open app
4. App loads your site
5. URL is saved for next launch

## Reconfiguring

Just scan a new QR code to update the URL anytime!
