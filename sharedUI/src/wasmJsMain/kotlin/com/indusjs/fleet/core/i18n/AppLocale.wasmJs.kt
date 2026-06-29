package com.indusjs.fleet.core.i18n

/**
 * Web (Wasm): `navigator.languages` is read-only, so there is no in-process system-locale setter.
 * The language choice is persisted (via [LanguageManager]/localStorage) and the live UI updates
 * through [ProvideAppLanguage]'s `key` recompose; a page reload picks up the system-level resolution.
 */
actual object AppLocaleController {
    actual fun apply(languageCode: String) {
        // No-op: handled by recompose + persisted preference (see KDoc).
    }
}
