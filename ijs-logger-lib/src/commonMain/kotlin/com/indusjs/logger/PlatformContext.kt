package com.indusjs.logger

/**
 * Platform-specific context required for logger initialization.
 * - Android: wrapper around android.content.Context
 * - iOS: marker class (no context needed)
 * - Web: marker class (no context needed)
 */
expect class PlatformContext

