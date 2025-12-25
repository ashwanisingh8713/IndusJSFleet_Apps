import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.indusjs.fleet.App
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewController
import platform.UIKit.setStatusBarStyle

fun MainViewController(): UIViewController = ComposeUIViewController { 
    App(onThemeChanged = { ThemeChanged(it) })
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