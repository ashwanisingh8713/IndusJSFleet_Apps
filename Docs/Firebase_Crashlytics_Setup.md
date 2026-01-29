# Firebase Crashlytics Setup Guide

## Overview

Firebase Crashlytics is integrated into IndusJS Fleet for crash reporting on both Android and iOS platforms.

---

## Prerequisites

### Placeholder Files Included

⚠️ **IMPORTANT**: Placeholder configuration files are included for development builds:

- `androidApp/google-services.json` - Placeholder (won't send crash reports)
- `iosApp/iosApp/GoogleService-Info.plist` - Placeholder (won't send crash reports)

**For production, replace these with real files from Firebase Console!**

### How to Get Real Files

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add your Android app:
   - Package name: `com.indusjs.fleet.androidApp`
   - Download `google-services.json`
   - Replace `androidApp/google-services.json`
4. Add your iOS app:
   - Bundle ID: `com.indusjs.fleet.iosApp` (or your actual bundle ID)
   - Download `GoogleService-Info.plist`
   - Replace `iosApp/iosApp/GoogleService-Info.plist`

---

## Android Setup

### Already Configured ✅

The following has been set up:

1. **build.gradle.kts (root)**: Firebase plugins registered
2. **androidApp/build.gradle.kts**: 
   - Google Services plugin applied
   - Firebase Crashlytics plugin applied
   - Firebase BOM dependency added
   - Crashlytics and Analytics dependencies added
   - BuildConfig enabled
3. **FleetApplication.kt**: Application class with Crashlytics initialization
4. **AndroidManifest.xml**: Application class registered

### Manual Step Required

Place `google-services.json` in the `androidApp/` directory:

```
IndusJSFleet/
├── androidApp/
│   ├── google-services.json  <-- Place here
│   ├── build.gradle.kts
│   └── src/
```

---

## iOS Setup

### Step 1: Install Pods

```bash
cd iosApp
pod install
```

If you don't have CocoaPods installed:
```bash
sudo gem install cocoapods
```

### Step 2: Add GoogleService-Info.plist

1. Download `GoogleService-Info.plist` from Firebase Console
2. Add it to your Xcode project:
   - Open `iosApp.xcworkspace` (NOT `.xcodeproj`)
   - Drag `GoogleService-Info.plist` into the `iosApp` group
   - Ensure "Copy items if needed" is checked
   - Ensure target `iosApp` is selected

### Step 3: Upload dSYM Files (For symbolicated crash reports)

Add a Run Script Phase in Xcode:
1. Open `iosApp.xcworkspace`
2. Select the `iosApp` target
3. Go to Build Phases
4. Click `+` → `New Run Script Phase`
5. Add this script:

```bash
"${PODS_ROOT}/FirebaseCrashlytics/run"
```

6. Add Input Files:
```
$(SRCROOT)/$(BUILT_PRODUCTS_DIR)/$(INFOPLIST_PATH)
```

---

## Usage

### Android

```kotlin
// In any Activity or Fragment
val app = application as FleetApplication

// Set user ID (after login)
app.setUserId("user123")

// Log custom messages
app.logMessage("User navigated to Settings")

// Record non-fatal exceptions
try {
    // Some code that might throw
} catch (e: Exception) {
    app.recordException(e)
}
```

### iOS

```swift
import FirebaseCrashlytics

// Set user ID
Crashlytics.crashlytics().setUserID("user123")

// Log custom messages
Crashlytics.crashlytics().log("User navigated to Settings")

// Record non-fatal exceptions
Crashlytics.crashlytics().record(error: error)
```

---

## Build Types

### Debug Builds
- Crashlytics collection is **disabled** by default
- No mapping file uploads
- Faster build times

### Release Builds
- Crashlytics collection is **enabled**
- Mapping files are uploaded for deobfuscation
- Native symbols are uploaded (Android)

---

## Testing Crashlytics

### Force a Test Crash

**Android:**
```kotlin
FirebaseCrashlytics.getInstance().log("Testing Crashlytics")
throw RuntimeException("Test Crash")
```

**iOS:**
```swift
fatalError("Test Crash")
```

> **Important**: Test crashes won't appear in the Firebase Console until:
> 1. The app is restarted after the crash
> 2. You wait a few minutes for data to process

---

## Troubleshooting

### Android

1. **Build fails with "google-services.json not found"**
   - Ensure `google-services.json` is in `androidApp/` directory

2. **Crashes not appearing in console**
   - Check that Crashlytics is enabled: `FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled`
   - Restart the app after a crash
   - Wait up to 15 minutes for data to appear

### iOS

1. **Pod install fails**
   - Run `pod repo update` first
   - Ensure Xcode Command Line Tools are installed

2. **Build fails with Firebase import errors**
   - Use `.xcworkspace` instead of `.xcodeproj`
   - Clean build folder: `Cmd + Shift + K`

---

## Version Reference

See `gradle/libs.versions.toml` for current versions:

```toml
firebase-bom = "33.7.0"
google-services = "4.4.2"
firebase-crashlytics-plugin = "3.0.2"
```

iOS Pods (see `iosApp/Podfile`):
- FirebaseCore: ~> 11.6
- FirebaseCrashlytics: ~> 11.6
- FirebaseAnalytics: ~> 11.6

