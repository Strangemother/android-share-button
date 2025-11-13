# API Flow Documentation

## Overview

The talofa.me Android app acts as a share target that forwards shared content to a configurable API endpoint. The flow involves two main operations: **Setup** and **Share**.

## Setup Flow

When a user configures the app with an API endpoint, the following happens:

### 1. User Input
- User enters API endpoint URL (e.g., `https://talofa.me/api/share` or `http://192.168.50.10:8000`)
- User optionally enters an API key for authentication

### 2. Configuration Request (GET)

**Request:**
```
GET [API_ENDPOINT_URL]
Headers:
  User-Agent: talofa.me/1.0.0 (Android)
  X-API-Key: [user_provided_api_key]  (if provided)
```

**Expected Response:**
```json
{
  "name": "Custom Share Name",
  "icon": "https://example.com/icon.png",
  "endpoint": "https://example.com/api/share",
  "delivery_key": "unique-delivery-key-for-this-device"
}
```

### 3. Local Storage
The app stores the following locally:
- API endpoint URL
- API key (if provided)
- Share target name
- Icon URL
- Post endpoint
- **Delivery key** (received from server)

## Share Flow

When content is shared through the app:

### 1. Content Extraction
The app extracts from the Android share intent:
- `EXTRA_TEXT`: The main shared text/URL
- `EXTRA_TITLE`: Optional title
- `EXTRA_SUBJECT`: Optional subject

### 2. Share Request (POST)

**Request:**
```
POST [POST_ENDPOINT]
Headers:
  User-Agent: talofa.me/1.0.0 (Android)
  X-Delivery-Key: [stored_delivery_key]  (if exists)
  Content-Type: application/json

Body:
{
  "text": "The shared content",
  "title": "Optional title",
  "subject": "Optional subject",
  "type": "text",
  "timestamp": 1699840000000
}
```

**Notes:**
- `title` and `subject` fields are only included if present
- `timestamp` is Unix timestamp in milliseconds
- `X-Delivery-Key` header is only sent if a delivery key was received during setup

**Expected Response:**
- Success: HTTP 200-299
- Error: Any other status code (error message shown to user)

## Security Features

### Network Security
- Cleartext (HTTP) traffic is allowed to support local/self-hosted endpoints
- HTTPS is recommended for production use

### API Key
- Optional user-provided key for authentication
- Sent only during setup, not during share operations
- Stored locally in encrypted SharedPreferences

### Delivery Key
- Server-generated unique identifier
- Returned during setup and stored locally
- Sent with every share request
- Allows server to identify and authorize specific app instances

## Data Flow Diagram

```
[User] → [Configure App]
           ↓
       [GET /api/config + X-API-Key]
           ↓
       [Server Returns Config + delivery_key]
           ↓
       [Store Locally]

[User] → [Share Content]
           ↓
       [POST /endpoint + X-Delivery-Key + JSON Body]
           ↓
       [Server Processes]
           ↓
       [Success/Error Toast]
```

## Example Server Implementation

### Setup Endpoint (GET)
```python
@app.route('/api/config', methods=['GET'])
def get_config():
    api_key = request.headers.get('X-API-Key')
    
    # Validate API key if provided
    if api_key and not validate_api_key(api_key):
        return {'error': 'Invalid API key'}, 401
    
    # Generate unique delivery key
    delivery_key = generate_unique_key()
    
    return {
        'name': 'My Share Service',
        'icon': 'https://example.com/icon.png',
        'endpoint': 'https://example.com/api/share',
        'delivery_key': delivery_key
    }
```

### Share Endpoint (POST)
```python
@app.route('/api/share', methods=['POST'])
def receive_share():
    delivery_key = request.headers.get('X-Delivery-Key')
    
    # Validate delivery key
    if not validate_delivery_key(delivery_key):
        return {'error': 'Invalid delivery key'}, 401
    
    data = request.json
    text = data.get('text')
    title = data.get('title')
    subject = data.get('subject')
    timestamp = data.get('timestamp')
    
    # Process the shared content
    process_share(delivery_key, text, title, subject, timestamp)
    
    return {'success': True}
```

## User Agent Detection

The app identifies itself with:
```
User-Agent: talofa.me/1.0.0 (Android)
```

This can be used to:
- Serve different responses to the app vs web browsers
- Track app usage
- Enable app-specific features
- Return configuration from the home URL when app is detected
