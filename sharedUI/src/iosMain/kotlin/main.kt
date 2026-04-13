import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.indusjs.fleet.App
import com.indusjs.logger.IjsLogger
import com.indusjs.logger.PlatformContext
import com.ijs.subscription.presentation.platform.createIosRazorpayLauncher
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewController
import platform.UIKit.setStatusBarStyle

private val iosRazorpayLauncher = createIosRazorpayLauncher()

fun MainViewController(): UIViewController {
    // Initialize file logger before Compose starts
    IjsLogger.init(PlatformContext())

    return ComposeUIViewController {
        App(
            onThemeChanged = { ThemeChanged(it) },
            razorpayLauncher = iosRazorpayLauncher
        )
    }
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
    LaunchedEffect(isDark) {
        // Light content (white icons) for dark backgrounds, dark content for light backgrounds
        UIApplication.sharedApplication.setStatusBarStyle(
            if (isDark) UIStatusBarStyleLightContent else UIStatusBarStyleDarkContent
        )
    }
}