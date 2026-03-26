package com.indusjs.logger

/**
 * Installs platform-specific uncaught exception / crash handlers
 * that write crash info to the log before delegating to the default handler.
 *
 * | Platform | Mechanism                                                  |
 * |----------|------------------------------------------------------------|
 * | Android  | `Thread.UncaughtExceptionHandler` — write then delegate    |
 * | iOS      | `NSSetUncaughtExceptionHandler` — write then re-raise      |
 * | Web      | `window.onerror` + `window.onunhandledrejection`           |
 *
 * Must NOT suppress existing crash reporters (Crashlytics, Sentry, etc.).
 */
internal expect class CrashHandler(fileManager: LogFileManager) {
    fun install()
}

