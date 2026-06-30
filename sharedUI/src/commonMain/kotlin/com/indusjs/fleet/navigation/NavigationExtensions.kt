package com.indusjs.fleet.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Navigation 3 helper extensions for NavBackStack.
 */

/**
 * Navigate and clear the back stack.
 * Used for auth flows (login, logout, session expiry).
 */
fun <T : NavKey> NavBackStack<T>.navigateAndClear(route: T) {
    clear()
    add(route)
}

/**
 * Navigate only if not already on the route (prevents duplicates).
 */
fun <T : NavKey> NavBackStack<T>.navigateSingleTop(route: T) {
    if (lastOrNull() != route) {
        add(route)
    }
}

/**
 * Pop and replace current screen with a new one.
 * Used after creating an entity to navigate to its detail.
 */
fun <T : NavKey> NavBackStack<T>.popAndNavigate(route: T) {
    removeLastOrNull()
    add(route)
}

/**
 * Switch to a top-level (bottom-nav / rail) destination: pop back to the Dashboard root, then push
 * [route] (or just land on Dashboard if [route] is Dashboard). Keeps the stack rooted at Dashboard so
 * tab switches don't grow the stack and system-back from any tab returns Home.
 */
fun NavBackStack<FleetRoute>.navigateTopLevel(route: FleetRoute) {
    val dashIndex = indexOfFirst { it is FleetRoute.Dashboard }
    if (dashIndex >= 0) {
        while (size > dashIndex + 1) removeAt(size - 1)
    } else {
        add(FleetRoute.Dashboard)
    }
    if (route !is FleetRoute.Dashboard) add(route)
}
