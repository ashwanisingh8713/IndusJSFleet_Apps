package com.indusjs.fleet.core.i18n

import platform.Foundation.NSUserDefaults

/**
 * iOS: persist the preferred language under `AppleLanguages`. There is no in-process system-locale
 * setter, so the live re-localization within a session is driven by [ProvideAppLanguage]'s `key`
 * recompose + anything routed through `LocalAppLanguage`; the system string resolution fully picks
 * up the new language on the next app launch.
 */
actual object AppLocaleController {
    actual fun apply(languageCode: String) {
        val defaults = NSUserDefaults.standardUserDefaults
        defaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
        defaults.synchronize()
    }
}
