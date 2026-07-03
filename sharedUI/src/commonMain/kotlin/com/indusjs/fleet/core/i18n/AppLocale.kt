package com.indusjs.fleet.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import com.indusjs.uicomponents.i18n.AppLanguage
import com.indusjs.uicomponents.i18n.LocalAppLanguage
import com.russhwolf.settings.Settings

/**
 * Forces the platform default locale so `compose-resources` (`stringResource`) resolves against the
 * chosen language. compose-resources reads the platform system locale via `Locale.current`, and its
 * resource-environment is internal — so the supported lever is the platform default locale plus a
 * [key]-driven recomposition (see [ProvideAppLanguage]).
 *
 * Platform reality: Android honors this live (LocaleList.setDefault drives Locale.current). iOS/Web
 * have no in-process system-locale setter, so there the choice is persisted and fully applied on the
 * next launch; the live recompose still updates everything routed through [LocalAppLanguage].
 */
expect object AppLocaleController {
    fun apply(languageCode: String)
}

/**
 * Persists the selected app language across restarts on every platform via the SHARED
 * multiplatform-settings [Settings] instance (Android SharedPreferences / iOS NSUserDefaults /
 * Web localStorage). Reuse the app's single Settings instance — never create a second.
 */
class LanguageManager(private val settings: Settings) {
    fun saved(): AppLanguage = AppLanguage.fromCode(settings.getStringOrNull(KEY))

    /** True once the user has EXPLICITLY chosen a language (via the first-launch picker or Profile).
     *  [saved] falls back to a default when unset; this distinguishes "never chosen" for the f6a
     *  first-launch picker, which shows once and never again after a choice is persisted. */
    fun hasChosen(): Boolean = settings.getStringOrNull(KEY) != null

    fun persist(language: AppLanguage) {
        settings.putString(KEY, language.code)
    }

    companion object {
        const val KEY = "app_language"
    }
}

/**
 * Applies [language] to the platform and provides [LocalAppLanguage], keyed on the language so the
 * whole subtree recomposes and every `stringResource` re-resolves when the language changes.
 */
@Composable
fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) {
    // Apply synchronously (before children compose) so the very first frame is in the right locale.
    AppLocaleController.apply(language.code)
    key(language) {
        CompositionLocalProvider(LocalAppLanguage provides language, content)
    }
}
