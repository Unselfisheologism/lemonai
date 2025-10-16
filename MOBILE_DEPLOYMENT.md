# Mobile LemonAI Deployment Guide

This guide explains how to build and deploy the Mobile LemonAI system for both Android and iOS platforms.

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Android Deployment](#android-deployment)
4. [iOS Deployment](#ios-deployment)
5. [Puter.js Integration](#puterjs-integration)
6. [Workflow Automation](#workflow-automation)
7. [Troubleshooting](#troubleshooting)

## Overview

The Mobile LemonAI system consists of:
- Android Kotlin container with Accessibility Service integration
- iOS Swift container with Shortcuts API integration
- Puter.js for authentication and data storage
- Node.js-mobile for Android background services
- WASM implementation for MCP pieces (Gmail & Notion only)
- Simplified workflow engines for both platforms

## Prerequisites

### General Requirements
- Node.js v16+
- npm or yarn
- Git

### Android Requirements
- Android Studio Arctic Fox or newer
- Android SDK API Level 21+
- Kotlin 1.5+
- Java 8+

### iOS Requirements
- Xcode 13+
- macOS Big Sur or newer
- iOS 12+ device for testing

## Android Deployment

### 1. Setup Environment
```bash
# Clone the repository
git clone <repository-url>
cd lemonai/android

# Install dependencies
./gradlew build
```

### 2. Configure Project
1. Open the project in Android Studio
2. Update `local.properties` with your SDK path:
   ```
   sdk.dir=/path/to/android/sdk
   ```

### 3. Build APK
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### 4. Permissions
The Android app requires Accessibility Service permissions:
1. Install the APK on your device
2. Go to Settings > Accessibility
3. Enable "LemonAI Accessibility Service"

## iOS Deployment

### 1. Setup Environment
```bash
# Navigate to iOS directory
cd lemonai/ios

# Install CocoaPods dependencies (if any)
pod install
```

### 2. Open in Xcode
1. Open `LemonAI.xcworkspace` in Xcode
2. Select your development team in project settings
3. Set the bundle identifier

### 3. Build and Run
1. Select your target device
2. Click "Run" or press Cmd+R

### 4. Shortcuts Integration
The iOS app integrates with the Shortcuts app:
1. Open the Shortcuts app
2. Create a new shortcut
3. Add the LemonAI actions

## Puter.js Integration

### Authentication
Puter.js handles all authentication automatically:
- No API keys required
- Users authenticate with their Puter accounts
- Automatic token management

### Data Storage
All user data is stored in Puter's cloud:
- Key-Value store for preferences
- File system for documents
- Automatic synchronization across devices

### Implementation
The integration is handled through:
1. `puterAuthService.js` (backend)
2. `puterAuth.js` (frontend)

## Workflow Automation

### Android Workflow Engine
The Android implementation uses Accessibility Service:
1. Request accessibility permissions
2. Parse workflow JSON
3. Execute steps through accessibility APIs

### iOS Workflow Engine
The iOS implementation uses Shortcuts API:
1. Convert workflow steps to Shortcuts actions
2. Save shortcuts to user library
3. Execute through Shortcuts app

## Troubleshooting

### Android Issues
1. **Accessibility Service Not Working**
   - Ensure the service is enabled in Settings
   - Restart the device if needed
   - Check for battery optimization settings

2. **Node.js-Mobile Not Starting**
   - Verify nodejs-mobile installation
   - Check Android permissions
   - Review logcat for errors

### iOS Issues
1. **Shortcuts Not Appearing**
   - Ensure Shortcuts app has necessary permissions
   - Check iOS version compatibility
   - Restart the Shortcuts app

2. **Build Failures**
   - Clean and rebuild the project
   - Update Xcode to latest version
   - Check provisioning profiles

## Support

For additional help:
1. Check the GitHub issues
2. Contact the development team
3. Review the Puter.js documentation

---
© 2025 LemonAI Team