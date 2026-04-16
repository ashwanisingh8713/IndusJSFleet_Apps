import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.indusjs.fleet.App
import com.indusjs.fleet.web.platformRazorpayLauncher

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport { App(razorpayLauncher = platformRazorpayLauncher()) }
