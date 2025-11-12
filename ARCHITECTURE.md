# Architecture Overview

This document provides a visual overview of the Android Share Button application architecture.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER'S DEVICE                             │
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              Android Share Button App                      │  │
│  │                                                             │  │
│  │  ┌─────────────────┐      ┌──────────────────────────┐   │  │
│  │  │  MainActivity   │      │  ShareReceiverActivity   │   │  │
│  │  │                 │      │                          │   │  │
│  │  │  • URL Input    │      │  • Receives Intents      │   │  │
│  │  │  • Setup Button │      │  • Extracts Content      │   │  │
│  │  │  • Show Config  │      │  • Shows Toasts          │   │  │
│  │  └────────┬────────┘      └───────────┬──────────────┘   │  │
│  │           │                            │                   │  │
│  │           │                            │                   │  │
│  │  ┌────────▼────────────────────────────▼──────────────┐   │  │
│  │  │           ConfigManager (Storage)                   │   │  │
│  │  │                                                      │   │  │
│  │  │  SharedPreferences:                                 │   │  │
│  │  │  • API URL                                          │   │  │
│  │  │  • Share Name                                       │   │  │
│  │  │  • Icon URL                                         │   │  │
│  │  │  • POST Endpoint                                    │   │  │
│  │  └────────┬─────────────────────────────────────────┘   │  │
│  │           │                                               │  │
│  │  ┌────────▼───────────────────────────────────────────┐  │  │
│  │  │         ApiClient (HTTP Communication)             │  │  │
│  │  │                                                     │  │  │
│  │  │  OkHttp:                                           │  │  │
│  │  │  • GET /api/config                                 │  │  │
│  │  │  • POST /api/share                                 │  │  │
│  │  └────────┬───────────────────────────────────────────┘  │  │
│  └───────────┼───────────────────────────────────────────────┘  │
│              │                                                   │
└──────────────┼───────────────────────────────────────────────────┘
               │
               │ INTERNET
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                     USER'S API SERVER                            │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  GET /api/config                                          │   │
│  │                                                            │   │
│  │  Returns:                                                  │   │
│  │  {                                                         │   │
│  │    "name": "My Share Button",                             │   │
│  │    "icon": "https://example.com/icon.png",                │   │
│  │    "endpoint": "https://example.com/api/share"            │   │
│  │  }                                                         │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  POST /api/share                                          │   │
│  │                                                            │   │
│  │  Receives:                                                 │   │
│  │  {                                                         │   │
│  │    "content": "https://example.com/article",              │   │
│  │    "type": "text",                                        │   │
│  │    "timestamp": 1699876543210                             │   │
│  │  }                                                         │   │
│  │                                                            │   │
│  │  Your Custom Logic:                                       │   │
│  │  • Save to database                                       │   │
│  │  • Send notification                                      │   │
│  │  • Forward to other services                             │   │
│  │  • Add to task list                                      │   │
│  │  • etc.                                                   │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## Configuration Flow

```
┌──────────────┐
│     User     │
└──────┬───────┘
       │
       │ 1. Opens app
       ▼
┌──────────────────┐
│   MainActivity   │
└──────┬───────────┘
       │
       │ 2. Enters API URL
       │ 3. Taps "Setup"
       ▼
┌──────────────────┐
│    ApiClient     │
└──────┬───────────┘
       │
       │ 4. GET /api/config
       ▼
┌──────────────────┐
│   API Server     │
└──────┬───────────┘
       │
       │ 5. Returns JSON config
       ▼
┌──────────────────┐
│    ApiClient     │
└──────┬───────────┘
       │
       │ 6. Parses response
       ▼
┌──────────────────┐
│  ConfigManager   │
└──────┬───────────┘
       │
       │ 7. Saves to SharedPreferences
       ▼
┌──────────────────┐
│   MainActivity   │
└──────┬───────────┘
       │
       │ 8. Displays config
       │ 9. Loads icon
       ▼
┌──────────────────┐
│     User sees    │
│  configuration   │
└──────────────────┘
```

## Share Flow

```
┌──────────────┐
│     User     │
└──────┬───────┘
       │
       │ 1. Shares from another app
       │    (Browser, Notes, etc.)
       ▼
┌──────────────────┐
│ Android System   │
└──────┬───────────┘
       │
       │ 2. Shows share sheet
       │    with available targets
       ▼
┌──────────────────┐
│     User         │
└──────┬───────────┘
       │
       │ 3. Selects custom share target
       ▼
┌──────────────────────┐
│ ShareReceiverActivity│
└──────┬───────────────┘
       │
       │ 4. Receives ACTION_SEND intent
       │ 5. Extracts content
       ▼
┌──────────────────┐
│  ConfigManager   │
└──────┬───────────┘
       │
       │ 6. Gets POST endpoint
       ▼
┌──────────────────┐
│    ApiClient     │
└──────┬───────────┘
       │
       │ 7. POST /api/share
       │    with JSON payload
       ▼
┌──────────────────┐
│   API Server     │
└──────┬───────────┘
       │
       │ 8. Processes share
       │ 9. Returns success
       ▼
┌──────────────────┐
│    ApiClient     │
└──────┬───────────┘
       │
       │ 10. Returns result
       ▼
┌──────────────────────┐
│ ShareReceiverActivity│
└──────┬───────────────┘
       │
       │ 11. Shows toast
       │ 12. Finishes
       ▼
┌──────────────────┐
│  User returns to │
│  original app    │
└──────────────────┘
```

## Component Interaction

```
┌─────────────────────────────────────────────────────────────┐
│                      User Interface Layer                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐           ┌───────────────────────┐   │
│  │  MainActivity    │           │ ShareReceiverActivity │   │
│  │                  │           │                       │   │
│  │  • Config Screen │           │  • Share Handler      │   │
│  │  • User Input    │           │  • Toast Messages     │   │
│  └────────┬─────────┘           └──────────┬────────────┘   │
│           │                                 │                │
└───────────┼─────────────────────────────────┼────────────────┘
            │                                 │
            │                                 │
┌───────────┼─────────────────────────────────┼────────────────┐
│           │       Business Logic Layer      │                │
├───────────┼─────────────────────────────────┼────────────────┤
│           │                                 │                │
│  ┌────────▼────────────────────────────────▼──────────────┐ │
│  │                  ConfigManager                          │ │
│  │                                                          │ │
│  │  • Storage Management                                   │ │
│  │  • Configuration Validation                             │ │
│  └────────┬────────────────────────────────────────────────┘ │
│           │                                                   │
│  ┌────────▼──────────────────────────────────────────────┐  │
│  │                    ApiClient                           │  │
│  │                                                         │  │
│  │  • HTTP Communication                                  │  │
│  │  • JSON Serialization/Deserialization                 │  │
│  │  • Error Handling                                     │  │
│  └────────┬──────────────────────────────────────────────┘  │
│           │                                                   │
└───────────┼───────────────────────────────────────────────────┘
            │
┌───────────┼───────────────────────────────────────────────────┐
│           │         Data & Network Layer                      │
├───────────┼───────────────────────────────────────────────────┤
│           │                                                    │
│  ┌────────▼──────────────┐      ┌───────────────────────┐   │
│  │  SharedPreferences    │      │      OkHttp           │   │
│  │                       │      │                       │   │
│  │  • Local Storage      │      │  • HTTP Client        │   │
│  │  • Key-Value Pairs    │      │  • Connection Pool    │   │
│  └───────────────────────┘      └───────────────────────┘   │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

## Data Models

```
┌─────────────────────────────────────────────────────────────┐
│                     Configuration Model                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ConfigManager (Persistent Storage)                          │
│  ├── apiUrl: String?                                         │
│  ├── shareName: String?                                      │
│  ├── iconUrl: String?                                        │
│  └── postEndpoint: String?                                   │
│                                                               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     API Response Models                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ConfigResult (sealed class)                                 │
│  ├── Success                                                 │
│  │   ├── name: String                                        │
│  │   ├── icon: String                                        │
│  │   └── endpoint: String                                    │
│  └── Error                                                   │
│      └── message: String                                     │
│                                                               │
│  ShareResult (sealed class)                                  │
│  ├── Success                                                 │
│  └── Error                                                   │
│      └── message: String                                     │
│                                                               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     Share Data Model                         │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Share Payload (JSON)                                        │
│  ├── content: String                                         │
│  ├── type: String                                            │
│  └── timestamp: Long                                         │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Threading Model

```
┌────────────────────────────────────────────────────────────┐
│                     Main Thread (UI)                        │
├────────────────────────────────────────────────────────────┤
│                                                              │
│  • MainActivity UI interactions                             │
│  • ShareReceiverActivity UI updates                         │
│  • Toast messages                                           │
│  • View updates                                             │
│                                                              │
└───────────┬────────────────────────────────────────────────┘
            │
            │ Kotlin Coroutines (lifecycleScope.launch)
            │
┌───────────▼────────────────────────────────────────────────┐
│                  Background Threads (I/O)                   │
├────────────────────────────────────────────────────────────┤
│                                                              │
│  • HTTP requests (GET /api/config)                          │
│  • HTTP requests (POST /api/share)                          │
│  • Image loading (icon download)                            │
│  • JSON parsing                                             │
│  • File I/O (SharedPreferences)                            │
│                                                              │
└───────────┬────────────────────────────────────────────────┘
            │
            │ withContext(Dispatchers.Main)
            │
┌───────────▼────────────────────────────────────────────────┐
│              Main Thread (UI Update)                        │
├────────────────────────────────────────────────────────────┤
│                                                              │
│  • Display results                                          │
│  • Show errors                                              │
│  • Update UI state                                          │
│                                                              │
└────────────────────────────────────────────────────────────┘
```

## Error Handling Flow

```
                    ┌──────────────┐
                    │  User Action │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  API Call    │
                    └──────┬───────┘
                           │
                  ┌────────▼────────┐
                  │   Try Block     │
                  └────────┬────────┘
                           │
         ┌─────────────────┴─────────────────┐
         │                                   │
    ┌────▼────┐                        ┌─────▼──────┐
    │ Success │                        │   Error    │
    └────┬────┘                        └─────┬──────┘
         │                                   │
         │                              ┌────▼────────┐
         │                              │ IOException │
         │                              │  Exception  │
         │                              └────┬────────┘
         │                                   │
         │                              ┌────▼────────┐
         │                              │   Catch     │
         │                              └────┬────────┘
         │                                   │
         └───────────┬───────────────────────┘
                     │
                ┌────▼────────┐
                │  UI Update  │
                │             │
                │ • Success   │
                │ • Error Msg │
                └─────────────┘
```

## Security Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Android App                           │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Current Security:                                           │
│  ├── INTERNET permission required                           │
│  ├── Cleartext traffic allowed (dev only)                   │
│  └── Unencrypted SharedPreferences                          │
│                                                               │
│  Recommended for Production:                                │
│  ├── HTTPS only (disable cleartext)                         │
│  ├── EncryptedSharedPreferences                             │
│  ├── Certificate pinning                                    │
│  └── ProGuard/R8 obfuscation                               │
│                                                               │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  │ HTTPS
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                      API Server                              │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Recommended Security:                                       │
│  ├── HTTPS with valid certificate                           │
│  ├── Authentication (API key, OAuth)                        │
│  ├── Rate limiting                                          │
│  ├── Input validation                                       │
│  ├── SQL injection prevention                               │
│  └── CORS configuration                                     │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Development Environment                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Android Device ←→ Computer (localhost)                     │
│  192.168.1.x       192.168.1.100:3000                       │
│                                                               │
│  • Same local network                                       │
│  • HTTP acceptable                                          │
│  • Example servers (Node.js/Python)                         │
│                                                               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  Production Environment                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Android Device ←→ Cloud Server                             │
│  (anywhere)       example.com                               │
│                                                               │
│  Options:                                                    │
│  ├── Heroku (PaaS)                                          │
│  ├── AWS Lambda (Serverless)                               │
│  ├── DigitalOcean (VPS)                                     │
│  ├── Google Cloud Run (Containers)                         │
│  └── Vercel/Netlify (Serverless Functions)                 │
│                                                               │
│  Requirements:                                               │
│  ├── HTTPS with valid certificate                           │
│  ├── Public DNS name                                        │
│  ├── Database (optional)                                    │
│  └── Monitoring/Logging                                     │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Extension Points

```
┌─────────────────────────────────────────────────────────────┐
│              Potential Extension Points                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  1. Content Types                                            │
│     ├── Images (image/*)                                     │
│     ├── Files (application/*)                               │
│     └── Multiple items (ACTION_SEND_MULTIPLE)               │
│                                                               │
│  2. Authentication                                           │
│     ├── API Keys                                             │
│     ├── OAuth 2.0                                            │
│     └── JWT tokens                                           │
│                                                               │
│  3. Storage                                                  │
│     ├── Room database (local)                               │
│     ├── Work queues (offline support)                       │
│     └── Encrypted storage                                    │
│                                                               │
│  4. UI Enhancements                                          │
│     ├── Multiple endpoints                                   │
│     ├── Share history                                        │
│     └── Share preview                                        │
│                                                               │
│  5. Advanced Features                                        │
│     ├── Background sync                                      │
│     ├── Notifications                                        │
│     ├── Analytics                                            │
│     └── Custom metadata fields                              │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

This architecture provides a solid foundation for a lightweight, extensible Android share button application that can be easily customized for various use cases.
