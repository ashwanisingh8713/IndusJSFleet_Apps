plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.metro).apply(false)
    alias(libs.plugins.room).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.buildConfig).apply(false)
    // Firebase & Google Services
    alias(libs.plugins.google.services).apply(false)
    alias(libs.plugins.firebase.crashlytics).apply(false)
}

/**
 * Installs CocoaPods dependencies for the Swift iOS host app.
 * Run once after clone (or when Podfile.lock changes) before opening
 * `iosApp/iosApp.xcworkspace` in Xcode — otherwise the project references
 * missing `Pods/Target Support Files/...` xcconfigs and the build fails.
 */
tasks.register<Exec>("iosPodInstall") {
    group = "ios"
    description = "Runs `pod install` in iosApp/ (CocoaPods; required for Xcode build)."
    workingDir = rootProject.file("iosApp")
    commandLine("pod", "install")
}
