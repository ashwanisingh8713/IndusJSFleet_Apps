package com.indusjs.fleet.web

import com.ijs.subscription.presentation.platform.RazorpayLauncher
import com.ijs.subscription.presentation.platform.createWebRazorpayLauncher

actual fun platformRazorpayLauncher(): RazorpayLauncher = createWebRazorpayLauncher()
