# API Documentation

This document describes the API endpoints that your server must implement to work with the Android Share Button app.

## Overview

The app communicates with your server using two HTTP endpoints:

1. **Configuration Endpoint (GET)**: Provides initial setup configuration
2. **Share Endpoint (POST)**: Receives shared content from the app

## Base Requirements

- Your server must be accessible from the Android device
- Endpoints should support JSON content type
- HTTPS is recommended for production (HTTP works for development)
- CORS headers may be needed depending on your setup

## Endpoints

### 1. Configuration Endpoint

Provides the app with configuration information during initial setup.

#### Request

```
GET /api/config
```

**Headers:**
- None required

**Body:**
- None

#### Response

**Status Code:** `200 OK`

**Content-Type:** `application/json`

**Body:**
```json
{
  "name": "string",
  "icon": "string (URL)",
  "endpoint": "string (URL)"
}
```

**Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | No | Display name for the share target. Shown in Android's share menu. Default: "Custom Share" |
| `icon` | string | No | URL to an icon image (PNG/JPEG). Displayed in the app's configuration screen. |
| `endpoint` | string | Yes | The URL where shared content will be POSTed. Can be the same as the config URL or different. |

#### Example Response

```json
{
  "name": "My Personal Reading List",
  "icon": "https://example.com/images/icon.png",
  "endpoint": "https://example.com/api/share"
}
```

#### Error Responses

**Invalid JSON:**
```
Status: 400 Bad Request
```

**Server Error:**
```
Status: 500 Internal Server Error
```

### 2. Share Endpoint

Receives shared content from the Android app.

#### Request

```
POST /api/share
```

**Headers:**
- `Content-Type: application/json`

**Body:**
```json
{
  "content": "string",
  "type": "string",
  "timestamp": number
}
```

**Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `content` | string | Yes | The shared content. Could be a URL, text, or other content from the sharing app. |
| `type` | string | Yes | Content type. Currently always "text". Future versions may support "image", "file", etc. |
| `timestamp` | number | Yes | Unix timestamp in milliseconds when the content was shared. |

#### Example Request

```json
{
  "content": "https://example.com/interesting-article",
  "type": "text",
  "timestamp": 1699876543210
}
```

#### Response

**Status Code:** `200 OK` (or `201 Created`)

**Content-Type:** `application/json`

**Body:**
```json
{
  "success": boolean,
  "message": "string",
  "id": "string or number (optional)"
}
```

**Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `success` | boolean | Yes | Whether the share was processed successfully |
| `message` | string | No | Human-readable success message |
| `id` | string/number | No | Optional identifier for the shared item |

#### Example Success Response

```json
{
  "success": true,
  "message": "Content added to your list",
  "id": "abc123"
}
```

#### Error Responses

**Missing Content:**
```json
Status: 400 Bad Request
{
  "success": false,
  "error": "Content is required"
}
```

**Server Error:**
```json
Status: 500 Internal Server Error
{
  "success": false,
  "error": "Failed to process share"
}
```

## Implementation Examples

### Node.js (Express)

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
  
  if (!content) {
    return res.status(400).json({
      success: false,
      error: "Content is required"
    });
  }
  
  // Process the shared content
  console.log('Received share:', content);
  
  res.json({
    success: true,
    message: "Share received",
    id: Date.now()
  });
});

app.listen(3000);
```

### Python (Flask)

```python
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/api/config', methods=['GET'])
def get_config():
    return jsonify({
        'name': 'My Share List',
        'icon': 'https://example.com/icon.png',
        'endpoint': 'https://example.com/api/share'
    })

@app.route('/api/share', methods=['POST'])
def receive_share():
    data = request.get_json()
    content = data.get('content')
    
    if not content:
        return jsonify({
            'success': False,
            'error': 'Content is required'
        }), 400
    
    # Process the shared content
    print(f'Received share: {content}')
    
    return jsonify({
        'success': True,
        'message': 'Share received',
        'id': int(time.time())
    })

if __name__ == '__main__':
    app.run(port=3000)
```

### PHP

```php
<?php
header('Content-Type: application/json');

$request_method = $_SERVER['REQUEST_METHOD'];
$request_uri = $_SERVER['REQUEST_URI'];

// Configuration endpoint
if ($request_method === 'GET' && strpos($request_uri, '/api/config') !== false) {
    echo json_encode([
        'name' => 'My Share List',
        'icon' => 'https://example.com/icon.png',
        'endpoint' => 'https://example.com/api/share'
    ]);
    exit;
}

// Share endpoint
if ($request_method === 'POST' && strpos($request_uri, '/api/share') !== false) {
    $data = json_decode(file_get_contents('php://input'), true);
    
    if (empty($data['content'])) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'error' => 'Content is required'
        ]);
        exit;
    }
    
    // Process the shared content
    error_log('Received share: ' . $data['content']);
    
    echo json_encode([
        'success' => true,
        'message' => 'Share received',
        'id' => time()
    ]);
    exit;
}

http_response_code(404);
echo json_encode(['error' => 'Not found']);
?>
```

## Security Considerations

### Authentication

For production use, implement authentication on your endpoints:

**API Key Method:**
```javascript
app.use((req, res, next) => {
  const apiKey = req.headers['x-api-key'];
  if (apiKey !== process.env.API_KEY) {
    return res.status(401).json({ error: 'Unauthorized' });
  }
  next();
});
```

**Bearer Token Method:**
```javascript
app.use((req, res, next) => {
  const token = req.headers['authorization']?.replace('Bearer ', '');
  if (!verifyToken(token)) {
    return res.status(401).json({ error: 'Unauthorized' });
  }
  next();
});
```

### HTTPS

Always use HTTPS in production to encrypt data in transit:

```javascript
const https = require('https');
const fs = require('fs');

const options = {
  key: fs.readFileSync('private-key.pem'),
  cert: fs.readFileSync('certificate.pem')
};

https.createServer(options, app).listen(443);
```

### Rate Limiting

Implement rate limiting to prevent abuse:

```javascript
const rateLimit = require('express-rate-limit');

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100 // limit each IP to 100 requests per windowMs
});

app.use('/api/', limiter);
```

### Input Validation

Always validate and sanitize input:

```javascript
app.post('/api/share', (req, res) => {
  const { content, type, timestamp } = req.body;
  
  // Validate content
  if (typeof content !== 'string' || content.length > 10000) {
    return res.status(400).json({
      success: false,
      error: 'Invalid content'
    });
  }
  
  // Validate type
  if (!['text', 'url'].includes(type)) {
    return res.status(400).json({
      success: false,
      error: 'Invalid type'
    });
  }
  
  // Process share...
});
```

## Testing

### Using curl

**Test Configuration Endpoint:**
```bash
curl -X GET http://localhost:3000/api/config
```

**Test Share Endpoint:**
```bash
curl -X POST http://localhost:3000/api/share \
  -H "Content-Type: application/json" \
  -d '{
    "content": "https://example.com/test",
    "type": "text",
    "timestamp": 1699876543210
  }'
```

### Using Postman

1. Create a GET request to your config endpoint
2. Verify the JSON response
3. Create a POST request to your share endpoint
4. Set Content-Type header to `application/json`
5. Add JSON body with required fields
6. Send and verify response

## Webhooks and Integrations

You can extend your share endpoint to integrate with other services:

### Send to Slack

```javascript
const axios = require('axios');

app.post('/api/share', async (req, res) => {
  const { content } = req.body;
  
  // Send to Slack
  await axios.post(process.env.SLACK_WEBHOOK_URL, {
    text: `New share: ${content}`
  });
  
  res.json({ success: true });
});
```

### Save to Database

```javascript
const { MongoClient } = require('mongodb');

app.post('/api/share', async (req, res) => {
  const { content, type, timestamp } = req.body;
  
  // Save to MongoDB
  const collection = db.collection('shares');
  const result = await collection.insertOne({
    content,
    type,
    timestamp,
    createdAt: new Date()
  });
  
  res.json({
    success: true,
    id: result.insertedId
  });
});
```

### Send Email Notification

```javascript
const nodemailer = require('nodemailer');

app.post('/api/share', async (req, res) => {
  const { content } = req.body;
  
  // Send email
  await transporter.sendMail({
    to: process.env.EMAIL_TO,
    subject: 'New Share',
    text: `You received a new share: ${content}`
  });
  
  res.json({ success: true });
});
```

## Troubleshooting

### CORS Issues

If you encounter CORS errors, add CORS headers:

```javascript
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  next();
});
```

### Request Timeout

Increase timeout values if processing takes time:

```javascript
const server = app.listen(3000);
server.timeout = 60000; // 60 seconds
```

### Large Content

Handle large content appropriately:

```javascript
app.use(express.json({ limit: '10mb' }));
```

## Support

For questions or issues with the API specification, please open an issue on GitHub.
