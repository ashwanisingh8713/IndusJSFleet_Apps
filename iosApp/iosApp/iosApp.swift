import SwiftUI
import SharedUI
import FirebaseCore
import FirebaseCrashlytics
import OSLog

/// AppDelegate for Firebase initialization
class AppDelegate: NSObject, UIApplicationDelegate {

    private static let log = Logger(subsystem: "com.indusjs.fleet.iosApp", category: "Firebase")

    /// Firebase Installations requires `API_KEY` to be exactly 39 characters (real key from Firebase Console).
    /// The repo ships a placeholder plist for CI/clones; skip configure so the app does not crash on launch.
    private static func shouldConfigureFirebase() -> Bool {
        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
              let dict = NSDictionary(contentsOfFile: path) as? [String: Any],
              let apiKey = dict["API_KEY"] as? String else {
            log.warning("GoogleService-Info.plist missing or has no API_KEY — Firebase disabled.")
            return false
        }
        guard apiKey.count == 39 else {
            log.warning("Firebase disabled: API_KEY must be 39 characters (replace GoogleService-Info.plist from Firebase Console). Current length=\(apiKey.count).")
            return false
        }
        if apiKey.contains("PLACEHOLDER") {
            log.warning("Firebase disabled: placeholder API_KEY in GoogleService-Info.plist.")
            return false
        }
        return true
    }

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        guard Self.shouldConfigureFirebase() else {
            return true
        }

        FirebaseApp.configure()

        #if DEBUG
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(false)
        #else
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(true)
        #endif

        return true
    }
}

@main
struct ComposeApp: App {
    // Use AppDelegate for Firebase initialization
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea(.all)
        }
    }
}

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Updates will be handled by Compose
    }
}
