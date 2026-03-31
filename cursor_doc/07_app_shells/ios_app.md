# iosApp — iOS Entry Point

## Overview

Xcode project with SwiftUI host that wraps the Kotlin/Native SharedUI framework. Firebase integrated via CocoaPods.

## File Tree

```
iosApp/
├── Podfile                    # CocoaPods: Firebase
├── Podfile.lock
├── iosApp.xcodeproj/          # Xcode project
├── iosApp.xcworkspace/        # Workspace (with Pods)
└── iosApp/
    ├── GoogleService-Info.plist   # Firebase config
    ├── Info.plist
    ├── iosApp.swift               # SwiftUI entry + AppDelegate
    ├── Assets.xcassets/
    └── Preview Content/
```

## Bootstrap Flow

### AppDelegate
```swift
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(...) -> Bool {
        FirebaseApp.configure()
        #if DEBUG
            Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(false)
        #else
            Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(true)
        #endif
        return true
    }
}
```

### SwiftUI Entry
```swift
@main
struct ComposeApp: App {
    @UIApplicationDelegateAdaptor var delegate: AppDelegate
    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea(.all)
        }
    }
}
```

### ContentView
`UIViewControllerRepresentable` wrapping `MainKt.MainViewController()` from SharedUI framework.

### Kotlin Side (sharedUI/iosMain/main.kt)
```kotlin
fun MainViewController(): UIViewController {
    IjsLogger.init(PlatformContext())
    return ComposeUIViewController {
        App(onThemeChanged = { ThemeChanged(it) })
    }
}
```

## CocoaPods

```ruby
platform :ios, '14.0'
pod 'FirebaseCore', '~> 11.6'
pod 'FirebaseCrashlytics', '~> 11.6'
pod 'FirebaseAnalytics', '~> 11.6'
```

## Key Differences from Android

- No `build.gradle.kts` — built via Xcode
- Links `SharedUI` KMP framework (static)
- Firebase via CocoaPods (not Gradle)
- No file handling callbacks (onPickFile, etc.)
- iOS 14.0 minimum deployment target
