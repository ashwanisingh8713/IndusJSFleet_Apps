package com.indusjs.uicomponents.i18n

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Languages the app can run in. [code] is the resource locale qualifier ("en" → values/,
 * "hi" → values-hi/). [displayName] is the language's own endonym, shown in the picker (a language
 * list conventionally renders each option in its own script regardless of the active locale).
 */
enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    HI("hi", "हिन्दी");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: EN
    }
}

/**
 * The current in-app language, provided at the App root. Read this (instead of `Locale.current`)
 * when UI needs to branch on language so it stays consistent with the displayed strings.
 */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.EN }

/**
 * Callback to change the in-app language — persists the choice and re-localizes the whole app.
 * Provided at the App root; invoked from the Profile language picker.
 */
val LocalAppLanguageController = staticCompositionLocalOf<(AppLanguage) -> Unit> { {} }

/**
 * Re-opens the first-launch language picker overlay. Provided at the App root; invoked from the
 * Sign-In screen's "भाषा / Language" link so users can change language before logging in.
 */
val LocalLanguagePickerLauncher = staticCompositionLocalOf<() -> Unit> { {} }
