package com.indusjs.logger

/**
 * WasmJS [LogFileManager]: persists logs in IndexedDB via JS interop.
 * Database: `IndusJS`, ObjectStore: `FleetLogs`.
 *
 * WasmJS uses typed JS interop rather than raw `js()` strings.
 */
internal actual class LogFileManager actual constructor(
    context: PlatformContext,
    sessionTimestamp: String
) {
    private companion object {
        const val MAX_ENTRY_CHARS = 2 * 1024 * 1024
        const val FILE_PREFIX = "ijs_fleet_"
    }

    private val baseKey: String = "$FILE_PREFIX$sessionTimestamp"
    private var rolloverIndex = 0
    private var currentKey: String = "$baseKey.txt"
    private var buffer = StringBuilder()
    private var bufferSize = 0

    init {
        requestPersistence()
        ensureDb()
    }

    actual fun write(entry: String) {
        if (bufferSize + entry.length > MAX_ENTRY_CHARS) {
            flush()
            rollover()
        }
        buffer.append(entry)
        bufferSize += entry.length
        flush()
    }

    actual fun close() {
        flush()
    }

    private fun rollover() {
        rolloverIndex++
        currentKey = "${baseKey}_$rolloverIndex.txt"
        buffer = StringBuilder()
        bufferSize = 0
    }

    private fun requestPersistence() {
        jsRequestPersistence()
    }

    private fun ensureDb() {
        jsEnsureDb()
    }

    private fun flush() {
        jsFlush(currentKey, buffer.toString())
    }
}

// WasmJS external JS interop functions
private fun jsRequestPersistence() {
    js("""
        if (typeof navigator !== 'undefined' && navigator.storage && navigator.storage.persist) {
            navigator.storage.persist();
        }
    """)
}

private fun jsEnsureDb() {
    js("""
        (function() {
            var request = indexedDB.open("IndusJS", 1);
            request.onupgradeneeded = function(event) {
                var db = event.target.result;
                if (!db.objectStoreNames.contains("FleetLogs")) {
                    db.createObjectStore("FleetLogs");
                }
            };
        })()
    """)
}

private fun jsFlush(key: String, value: String) {
    js("""
        (function() {
            var request = indexedDB.open("IndusJS", 1);
            request.onsuccess = function(event) {
                var db = event.target.result;
                var tx = db.transaction("FleetLogs", "readwrite");
                var store = tx.objectStore("FleetLogs");
                store.put(value, key);
            };
        })()
    """)
}

