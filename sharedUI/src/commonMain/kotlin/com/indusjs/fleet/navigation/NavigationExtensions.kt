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
