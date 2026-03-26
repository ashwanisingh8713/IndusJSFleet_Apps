package com.indusjs.logger

/**
 * WasmJS crash handler: installs `window.onerror` and
 * `window.onunhandledrejection` via JS interop.
 *
 * **Design note:** Crash entries are written to `console.error` rather than
 * IndexedDB because WasmJS `js()` blocks cannot call back into Kotlin code
 * (no `dynamic` dispatch like Kotlin/JS). During a crash the IndexedDB
 * transaction may not complete anyway. Console output is captured by most
 * browser dev-tools and error-monitoring services.
 *
 * Does NOT suppress existing crash reporters — previous handlers are
 * preserved and delegated to after logging.
 */
internal actual class CrashHandler actual constructor(
    private val fileManager: LogFileManager
) {
    actual fun install() {
        jsInstallCrashHandlers()
    }
}

private fun jsInstallCrashHandlers() {
    js("""
        (function() {
            var prevOnError = window.onerror;
            var prevOnRejection = window.onunhandledrejection;

            window.onerror = function(message, source, lineno, colno, error) {
                try {
                    var entry = "[CRASH] [ThreadID:1 | ThreadName:main] [UncaughtException]\n" +
                        "Message: " + message + "\nSource: " + source + ":" + lineno + ":" + colno + "\n";
                    if (error && error.stack) { entry += error.stack + "\n"; }
                    console.error("[IjsLogger]", entry);
                } catch(e) {}
                if (prevOnError) return prevOnError.apply(this, arguments);
            };

            window.onunhandledrejection = function(event) {
                try {
                    var reason = event.reason;
                    var entry = "[CRASH] [ThreadID:1 | ThreadName:main] [UnhandledPromiseRejection]\n" +
                        "Reason: " + reason + "\n";
                    if (reason && reason.stack) { entry += reason.stack + "\n"; }
                    console.error("[IjsLogger]", entry);
                } catch(e) {}
                if (prevOnRejection) return prevOnRejection.apply(this, arguments);
            };
        })()
    """)
}

