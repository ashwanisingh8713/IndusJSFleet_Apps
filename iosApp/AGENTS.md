# AGENTS.md - iosApp

## Purpose

iOS platform entry point for the IndusJS Fleet app. **Thin SwiftUI shell** that hosts the shared Compose UI from `sharedUI` module via `UIViewControllerRepresentable`. Handles iOS-specific concerns: Firebase Crashlytics initialization.

**Language:** Swift  
**Framework:** SwiftUI + Compose Multiplatform (via KMP framework)

**Razorpay (host / ATS):** [../Docs/Razorpay/iosApp_RAZORPAY_INTEGRATION.md](../Docs/Razorpay/iosApp_RAZORPAY_INTEGRATION.md) — [../Docs/Razorpay/README.md](../Docs/Razorpay/README.md).

---

## Source Tree

```
iosApp/
├── Podfile                    # CocoaPods dependencies (Firebase)
├── Podfile.lock
├── iosApp/
│   ├── iosApp.swift           # ★ App entry point (46 lines)
│   ├── Info.plist
│   ├── GoogleService-Info.plist # Firebase config
│   └── Assets.xcassets/       # App icons and assets
├── iosApp.xcodeproj/
└── iosApp.xcworkspace/        # Open this in Xcode (includes Pods)
```

---

## Entry Point: `iosApp.swift`

```swift
@main
struct ComposeApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea(.all)
        }
    }
}

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainKt.MainViewController()  // Calls into sharedUI KMP framework
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### AppDelegate
- Initializes Firebase via `FirebaseApp.configure()`
- Disables Crashlytics in DEBUG builds
- Enables Crashlytics in RELEASE builds

---

## Build

Open `iosApp.xcworkspace` in Xcode, then Build & Run (⌘R).

The `sharedUI` module is compiled as a KMP framework and linked via the Xcode project configuration.

## Dependencies

- `sharedUI` — via KMP framework (all UI and business logic)
- Firebase (via CocoaPods) — Crashlytics + Analytics

**All business logic, screens, navigation, and data flows live in `sharedUI`. This module only provides the iOS shell.**

