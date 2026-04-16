package com.indusjs.fleet.web

import com.ijs.subscription.presentation.platform.RazorpayLauncher
import com.ijs.subscription.presentation.platform.createWasmJsRazorpayLauncher

actual fun platformRazorpayLauncher(): RazorpayLauncher = createWasmJsRazorpayLauncher()
