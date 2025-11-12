"""
Example API Server for Android Share Button

This is a simple Python/Flask server that implements the required
endpoints for the Android Share Button app.

Installation:
    pip install flask

Usage:
    python example-server.py

The server will run on http://0.0.0.0:3000
"""

from flask import Flask, request, jsonify
from datetime import datetime
import os

app = Flask(__name__)
PORT = int(os.environ.get('PORT', 3000))

# In-memory storage for shared items (use a database in production)
shared_items = []


@app.route('/api/config', methods=['GET'])
def get_config():
    """
    Configuration Endpoint (GET)
    
    Returns the configuration for the share button.
    The Android app calls this endpoint during setup.
    """
    config = {
        'name': 'My Personal List',
        'icon': 'https://via.placeholder.com/64/6200EE/FFFFFF?text=Share',
        'endpoint': f'http://localhost:{PORT}/api/share'
    }
    
    print(f'Configuration requested: {config}')
    return jsonify(config)


@app.route('/api/share', methods=['POST'])
def receive_share():
    """
    Share Endpoint (POST)
    
    Receives shared content from the Android app.
    This is where you implement your custom logic.
    """
    data = request.get_json()
    
    content = data.get('content')
    content_type = data.get('type')
    timestamp = data.get('timestamp')
    
    if not content:
        return jsonify({
            'success': False,
            'error': 'Content is required'
        }), 400
    
    # Store the shared item
    item = {
        'id': len(shared_items) + 1,
        'content': content,
        'type': content_type,
        'timestamp': timestamp,
        'receivedAt': datetime.now().isoformat()
    }
    
    shared_items.append(item)
    
    print(f'Received share: {item}')
    
    # Here you could:
    # - Save to a database
    # - Send a notification
    # - Add to a task list
    # - Forward to another service
    # - etc.
    
    return jsonify({
        'success': True,
        'id': item['id'],
        'message': 'Content received successfully'
    })


@app.route('/api/shares', methods=['GET'])
def list_shares():
    """
    List Endpoint (GET) - Optional
    
    Returns all shared items (for demonstration purposes)
    """
    return jsonify({
        'total': len(shared_items),
        'items': shared_items
    })


@app.route('/health', methods=['GET'])
def health_check():
    """Health Check Endpoint"""
    return jsonify({
        'status': 'ok',
        'timestamp': datetime.now().isoformat()
    })


@app.before_request
def log_request():
    """Log all requests"""
    print(f"{datetime.now().isoformat()} - {request.method} {request.path}")


if __name__ == '__main__':
    print('=' * 60)
    print('Android Share Button - Example API Server (Python)')
    print('=' * 60)
    print(f'Server running on http://0.0.0.0:{PORT}')
    print('')
    print('Available endpoints:')
    print(f'  GET  http://localhost:{PORT}/api/config  - Configuration')
    print(f'  POST http://localhost:{PORT}/api/share   - Receive shares')
    print(f'  GET  http://localhost:{PORT}/api/shares  - View all shares')
    print(f'  GET  http://localhost:{PORT}/health      - Health check')
    print('')
    print('To use with the Android app:')
    print('  1. Make sure your device can reach this server')
    print('  2. Use your computer\'s IP address instead of localhost')
    print(f'  3. Enter: http://YOUR_IP:{PORT}/api/config in the app')
    print('=' * 60)
    print('')
    print('Note: Debug mode is disabled for security.')
    print('Set FLASK_DEBUG=1 environment variable to enable debugging.')
    
    # Debug mode disabled for security - enable only in development if needed
    debug_mode = os.environ.get('FLASK_DEBUG', '0') == '1'
    app.run(host='0.0.0.0', port=PORT, debug=debug_mode)
