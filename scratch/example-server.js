/**
 * Example API Server for Android Share Button
 * 
 * This is a simple Node.js/Express server that implements the required
 * endpoints for the Android Share Button app.
 * 
 * Installation:
 *   npm install express body-parser
 * 
 * Usage:
 *   node example-server.js
 * 
 * The server will run on http://localhost:3000
 */

const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(bodyParser.json());
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
  next();
});

// In-memory storage for shared items (use a database in production)
const sharedItems = [];

/**
 * Configuration Endpoint (GET)
 * 
 * Returns the configuration for the share button.
 * The Android app calls this endpoint during setup.
 */
app.get('/api/config', (req, res) => {
  const config = {
    name: "My Personal List",
    icon: "https://via.placeholder.com/64/6200EE/FFFFFF?text=Share",
    endpoint: `http://localhost:${PORT}/api/share`
  };
  
  console.log('Configuration requested:', config);
  res.json(config);
});

/**
 * Share Endpoint (POST)
 * 
 * Receives shared content from the Android app.
 * This is where you implement your custom logic.
 */
app.post('/api/share', (req, res) => {
  const { content, type, timestamp } = req.body;
  
  if (!content) {
    return res.status(400).json({ 
      success: false, 
      error: 'Content is required' 
    });
  }
  
  // Store the shared item
  const item = {
    id: sharedItems.length + 1,
    content,
    type,
    timestamp,
    receivedAt: new Date().toISOString()
  };
  
  sharedItems.push(item);
  
  console.log('Received share:', item);
  
  // Here you could:
  // - Save to a database
  // - Send a notification
  // - Add to a task list
  // - Forward to another service
  // - etc.
  
  res.json({ 
    success: true,
    id: item.id,
    message: 'Content received successfully'
  });
});

/**
 * List Endpoint (GET) - Optional
 * 
 * Returns all shared items (for demonstration purposes)
 */
app.get('/api/shares', (req, res) => {
  res.json({
    total: sharedItems.length,
    items: sharedItems
  });
});

/**
 * Health Check Endpoint
 */
app.get('/health', (req, res) => {
  res.json({ 
    status: 'ok',
    timestamp: new Date().toISOString()
  });
});

// Start the server
app.listen(PORT, () => {
  console.log('='.repeat(60));
  console.log('Android Share Button - Example API Server');
  console.log('='.repeat(60));
  console.log(`Server running on http://localhost:${PORT}`);
  console.log('');
  console.log('Available endpoints:');
  console.log(`  GET  http://localhost:${PORT}/api/config  - Configuration`);
  console.log(`  POST http://localhost:${PORT}/api/share   - Receive shares`);
  console.log(`  GET  http://localhost:${PORT}/api/shares  - View all shares`);
  console.log(`  GET  http://localhost:${PORT}/health      - Health check`);
  console.log('');
  console.log('To use with the Android app:');
  console.log(`  1. Make sure your device can reach this server`);
  console.log(`  2. Use your computer's IP address instead of localhost`);
  console.log(`  3. Enter: http://YOUR_IP:${PORT}/api/config in the app`);
  console.log('='.repeat(60));
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM signal received: closing HTTP server');
  server.close(() => {
    console.log('HTTP server closed');
  });
});
