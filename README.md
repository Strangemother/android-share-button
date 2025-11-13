# talofa.me 📱

> Note to self: "Hello Me" 🫶

My personal link stash for Android (currently) and the web. Save links and images from anywhere, store them my way. https://talofa.me 

## What is this?

talofa.me is a lightweight Android app that lets you capture and store links, screenshots, and text snippets to your own server. Think of it as your personal pocket for everything you find interesting while browsing, reading, or exploring on your phone.

**Who's it for?**
- Developers building personal knowledge bases
- Anyone who collects links for later reference
- People who want control over their saved content
- Users building presentation notes, readme lists, or research collections

**What makes it special?**
- No ads, no tracking, just your stuff
- Share to your own server (or localhost!)
- Optional grouping and categorization
- Works with any backend you build

## ✨ Features

- **Text & Links**: Share URLs, articles, quotes, anything text-based
- **Images**: Share screenshots and photos directly 📸
- **Groups**: Optionally categorize shares into groups (work, personal, etc.)
- **Self-Hosted**: Point it at your server, no middleman
- **API-First**: Build automation, CICD triggers, markdown generators, whatever you want
- **HTTP Support**: Perfect for local development and self-hosted setups

## 🚀 Quick Start

### Install

1. Download the APK from [releases](https://github.com/Strangemother/android-share-button/releases)
2. Enable "Install from unknown sources" on your Android device
3. Install the APK

Or build it yourself - see [BUILDING.md](BUILDING.md)

### Configure

1. Open talofa.me
2. Enter your server URL (e.g., `http://192.168.1.100:8000` or `https://talofa.me/api`)
3. Optionally add an API key
4. Tap Setup ✅

### Use It

Tap share in any app → Select "talofa.me" → Done! Your link/image is saved to your server.

## 🎯 Usage Ideas

**For Personal Use:**
- Build a reading list from articles you find
- Save screenshots of inspiration
- Collect receipts and documents
- Bookmark recipes while scrolling

**For Developers:**
- Auto-generate changelog entries from shared commits
- Build a personal documentation site from saved links
- Trigger CICD workflows from shared content
- Create markdown-formatted collections automatically

**For Research:**
- Organize sources by project groups
- Save quotes and references instantly
- Build bibliographies from shared papers
- Keep track of resources per topic

## 🔧 Server Setup

talofa.me needs a companion server to receive your shares. You can:
- Run the example server (coming soon)
- Build your own with any framework (Django, Flask, Express, etc.)
- Point it at a simple webhook
- Deploy to Vercel, Railway, or your preferred host

See [docs/dev/API_FLOW.md](docs/dev/API_FLOW.md) for the API specification.

## 🔮 Coming Soon

- Browser extension
- Web interface for managing saved content
- Public API for third-party integrations
- More share types (files, multiple images)

The goal: give you every way to push content into your personal space, your rules.

## 📚 Documentation

- [API Flow](docs/dev/API_FLOW.md) - How the app communicates with your server
- [Building](BUILDING.md) - Compile and customize the app
- More docs coming as features develop

## 🤝 Contributing

This is a personal tool made shareable. PRs welcome! Keep it simple, keep it focused.

## 📝 License

MIT - see [LICENSE](LICENSE)
