# API Flow Documentation

## Overview

The talofa.me Android app acts as a share target that forwards shared content to a configurable API endpoint. The flow involves three main operations: **Setup**, **Share**, and **Group Selection** (optional).

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
  User-Agent: talofa.me/1.0.1 (Android)
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

When content is shared through the app, there are two possible flows depending on server response:

### Flow A: Direct Share (No Groups)

#### 1. Content Extraction
The app extracts from the Android share intent:
- `EXTRA_TEXT`: The main shared text/URL
- `EXTRA_TITLE`: Optional title
- `EXTRA_SUBJECT`: Optional subject

#### 2. Share Request (POST)

**Request:**
```
POST [POST_ENDPOINT]
Headers:
  User-Agent: talofa.me/1.0.1 (Android)
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

#### 3. Success Response (HTTP 200)

**Response:**
```json
{
  "success": true
}
```

Result: Share complete, activity closes.

---

### Flow B: Share with Group Selection

#### 1. Initial Share Request (Same as Flow A)

**Request:**
```
POST [POST_ENDPOINT]
Headers:
  User-Agent: talofa.me/1.0.1 (Android)
  X-Delivery-Key: [stored_delivery_key]
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

#### 2. Group Selection Required Response (HTTP 202 Accepted)

**Response:**
```json
{
  "share_id": "abc123xyz789",
  "groups": [
    {
      "id": "work",
      "name": "Work",
      "icon": "https://example.com/work-icon.png",
      "description": "Work related items"
    },
    {
      "id": "personal",
      "name": "Personal",
      "icon": "https://example.com/personal-icon.png",
      "description": "Personal stuff"
    },
    {
      "id": "family",
      "name": "Family"
    }
  ]
}
```

**Required fields:**
- `share_id`: Unique identifier for the created share (sent back in step 4)
- `groups`: Array of group objects
  - `id`: Unique group identifier (required)
  - `name`: Display name shown to user (required)
  - `icon`: URL to group icon (optional, not currently displayed)
  - `description`: Subtitle text (optional, not currently displayed)

#### 3. User Interaction
- Bottom sheet slides up from bottom
- User sees list of groups
- User taps to select a group OR dismisses sheet
- If dismissed: Activity closes, share is created but not assigned to group
- If selected: Continue to step 4

#### 4. Group Selection Request (POST)

**Request:**
```
POST [POST_ENDPOINT]
Headers:
  User-Agent: talofa.me/1.0.1 (Android)
  X-Delivery-Key: [stored_delivery_key]
  Content-Type: application/json

Body:
{
  "share_id": "abc123xyz789",
  "group_id": "work"
}
```

**Notes:**
- This is a lightweight request with only `share_id` and `group_id`
- The content is NOT re-sent - server should lookup by `share_id`
- This allows server to associate the already-created share with the selected group

#### 5. Success Response (HTTP 200)

**Response:**
```json
{
  "success": true
}
```

Result: Share assigned to group, activity closes.

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

### Setup Flow
```
[User] → [Configure App]
           ↓
       [GET /api/config + X-API-Key]
           ↓
       [Server Returns Config + delivery_key]
           ↓
       [Store Locally]
```

### Direct Share Flow (No Groups)
```
[User] → [Share Content]
           ↓
       [POST /endpoint + Content]
           ↓
       [Server: HTTP 200]
           ↓
       [Success Toast + Close]
```

### Share with Group Selection Flow
```
[User] → [Share Content]
           ↓
       [POST /endpoint + Content]
           ↓
       [Server: HTTP 202 + share_id + groups]
           ↓
       [Show Bottom Sheet with Groups]
           ↓
   ┌─────────────────────┐
   │                     │
   ↓                     ↓
[User Selects]    [User Dismisses]
   ↓                     ↓
[POST share_id      [Close Activity]
 + group_id]        (share created
   ↓                 but unassigned)
[Server: HTTP 200]
   ↓
[Success Toast + Close]
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

The share endpoint should handle two types of requests:
1. Initial share with content (decides whether to request group selection)
2. Group selection for existing share (just `share_id` and `group_id`)

```python
@app.route('/api/share', methods=['POST'])
def receive_share():
    delivery_key = request.headers.get('X-Delivery-Key')
    
    # Validate delivery key
    if not validate_delivery_key(delivery_key):
        return {'error': 'Invalid delivery key'}, 401
    
    data = request.json
    
    # Check if this is a group selection request
    if 'share_id' in data and 'group_id' in data:
        # Group selection for existing share
        share_id = data['share_id']
        group_id = data['group_id']
        
        # Update the share with the selected group
        update_share_group(share_id, group_id)
        
        return {'success': True}, 200
    
    # Initial share request
    text = data.get('text')
    title = data.get('title')
    subject = data.get('subject')
    timestamp = data.get('timestamp')
    
    # Create the share and get its ID
    share_id = create_share(delivery_key, text, title, subject, timestamp)
    
    # Decide if group selection is needed
    # This can be based on any logic: user settings, time of day, content analysis, etc.
    if should_request_group_selection(delivery_key):
        # Get available groups for this user
        groups = get_user_groups(delivery_key)
        
        return {
            'share_id': share_id,
            'groups': [
                {
                    'id': g.id,
                    'name': g.name,
                    'icon': g.icon_url,
                    'description': g.description
                }
                for g in groups
            ]
        }, 202  # 202 Accepted
    
    # No group selection needed, complete immediately
    return {'success': True}, 200


def should_request_group_selection(delivery_key):
    """
    Determine if group selection should be requested.
    Examples of logic:
    - User has groups configured
    - User preference is enabled
    - Time-based rules
    - Content analysis suggests categorization
    """
    user = get_user_by_delivery_key(delivery_key)
    return user.has_groups() and user.group_selection_enabled
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
