package com.indusjs.fleet.navigation

import androidx.compose.runtime.compositionLocalOf
import com.ijs.subscription.presentation.platform.RazorpayLauncher
import com.ijs.subscription.presentation.platform.RazorpayResult

/**
 * CompositionLocal that provides the platform-specific [RazorpayLauncher] to the
 * navigation tree. Each platform supplies a concrete implementation:
 *
 * - Android: [createAndroidRazorpayLauncher] (from screen-payment androidMain)
 * - iOS:     [createIosRazorpayLauncher]     (from screen-payment iosMain)
 * - JS:      [createWebRazorpayLauncher]     (from screen-payment jsMain)
 * - WasmJS:  [createWasmJsRazorpayLauncher]  (from screen-payment wasmJsMain)
 *
 * Usage: provide it in App() before the NavDisplay:
 * ```kotlin
 * CompositionLocalProvider(LocalRazorpayLauncher provides myPlatformLauncher) {
 *     NavDisplay(...)
 * }
 * ```
 */
val LocalRazorpayLauncher = compositionLocalOf<RazorpayLauncher> {
    // Default stub launcher — returns Cancelled so screens don't hang in Compose Preview.
    { _, onResult -> onResult(RazorpayResult.Cancelled) }
}
