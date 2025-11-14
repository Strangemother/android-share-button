# QR Code Setup

The app supports automatic configuration via QR codes. Users can scan a QR code with their camera app, and the talofa.me app will automatically open and configure itself.

## Supported URL Formats

The app supports two deep link formats:

### 1. Custom Scheme (Recommended for local use)
```
talofa://setup?url=YOUR_CONFIG_URL&key=YOUR_API_KEY
```

### 2. HTTPS Scheme (Better for production)
```
https://talofa.me/setup?url=YOUR_CONFIG_URL&key=YOUR_API_KEY
```

## Parameters

| Parameter | Required | Description |
|-----------|----------|-------------|
| `url` | Yes | Your API configuration endpoint URL (can be with or without protocol) |
| `key` | No | Optional API key for authentication |

## Examples

### Basic Setup (No API Key)
```
talofa://setup?url=https://api.example.com/config
```

### Setup with API Key
```
talofa://setup?url=https://api.example.com/config&key=my-secret-key
```

### Local Development (HTTP)
```
talofa://setup?url=192.168.1.100:3000/api/config
```

### Setup without Protocol (Will try HTTPS first, then HTTP)
```
talofa://setup?url=talofa.me/api/config
```

## Creating QR Codes

### Online QR Code Generators
1. Go to any QR code generator (e.g., qr-code-generator.com, qr.io, or goqr.me)
2. Select "URL" or "Text" type
3. Paste your deep link URL
4. Generate and download the QR code

### Command Line (using qrencode)
```bash
# Install qrencode
sudo apt-get install qrencode  # Linux
brew install qrencode          # macOS

# Generate QR code
qrencode -o setup-qr.png "talofa://setup?url=https://api.example.com/config&key=your-api-key"
```

### Python Script
```python
import qrcode

url = "talofa://setup?url=https://api.example.com/config&key=your-api-key"
qr = qrcode.make(url)
qr.save("setup-qr.png")
```

### Node.js Script
```javascript
const QRCode = require('qrcode');

const url = 'talofa://setup?url=https://api.example.com/config&key=your-api-key';
QRCode.toFile('setup-qr.png', url, (err) => {
  if (err) throw err;
  console.log('QR code saved!');
});
```

## How It Works

1. User scans the QR code with their phone's camera app
2. Android recognizes the `talofa://` scheme or `https://talofa.me/setup` URL
3. The system opens the talofa.me app
4. The app extracts the `url` and `key` parameters
5. The configuration fields are auto-filled
6. Setup is automatically triggered
7. User sees success/error message

## Security Considerations

### For Production
- Use HTTPS URLs only in your QR codes
- Rotate API keys regularly
- Consider using one-time setup tokens instead of permanent API keys
- Host QR codes securely (don't print API keys on public posters)

### For Development
- HTTP is acceptable for local network testing
- Use IP-based URLs for local development

## Best Practices

1. **Test your QR codes** before distributing them
2. **Add a label** near the QR code explaining what it does
3. **Provide fallback instructions** for manual setup
4. **Version your QR codes** if you have multiple environments (dev, staging, prod)
5. **Consider dynamic QR codes** that redirect to a URL you control (allows updating without reprinting)

## Server-Side Implementation

Your server can provide a QR code endpoint that generates setup QR codes:

```python
# Python/Flask example
from flask import send_file
import qrcode
import io

@app.route('/setup-qr')
def setup_qr():
    # Generate deep link
    config_url = request.host_url + 'api/config'
    api_key = generate_temporary_key()  # Optional: generate one-time key
    
    deep_link = f"talofa://setup?url={config_url}&key={api_key}"
    
    # Generate QR code
    qr = qrcode.make(deep_link)
    buf = io.BytesIO()
    qr.save(buf)
    buf.seek(0)
    
    return send_file(buf, mimetype='image/png')
```

```javascript
// Node.js/Express example
const QRCode = require('qrcode');

app.get('/setup-qr', async (req, res) => {
  const configUrl = `${req.protocol}://${req.get('host')}/api/config`;
  const apiKey = generateTemporaryKey(); // Optional
  
  const deepLink = `talofa://setup?url=${configUrl}&key=${apiKey}`;
  
  try {
    const qrCodeBuffer = await QRCode.toBuffer(deepLink);
    res.type('png').send(qrCodeBuffer);
  } catch (err) {
    res.status(500).send('Error generating QR code');
  }
});
```

## Troubleshooting

### QR code doesn't open the app
- Verify the app is installed
- Check the deep link URL format
- Try using the `talofa://` scheme instead of `https://talofa.me`
- Ensure URL parameters are properly URL-encoded

### App opens but doesn't configure
- Check the server logs to see if the request arrived
- Verify the config URL is accessible from the phone
- Test the URL manually in the app first

### Android says "No apps can perform this action"
- The app may not be properly installed
- The deep link scheme might not be recognized
- Reinstall the app and try again
