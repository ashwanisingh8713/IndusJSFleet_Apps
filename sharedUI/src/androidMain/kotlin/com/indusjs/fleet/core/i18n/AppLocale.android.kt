package com.indusjs.fleet.core.i18n

import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Android: set the JVM default locale and (API 24+) the system [LocaleList] default — the latter is
 * what `androidx.compose.ui.text.intl.Locale.current` reads on API 24+, so compose-resources
 * re-resolves to the chosen language on the next ([ProvideAppLanguage]'s `key`-driven) recomposition,
 * with no Activity recreate. On API 23 the JVM default alone drives resolution.
 */
actual object AppLocaleController {
    actual fun apply(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= 24) {
            LocaleList.setDefault(LocaleList(locale))
        }
    }
}
