package com.indusjs.logger

/**
 * JS [LogFileManager]: persists logs in IndexedDB.
 * Database: `IndusJS`, ObjectStore: `FleetLogs`.
 * Each session key = filename, value = accumulated log text.
 *
 * Uses Kotlin/JS `dynamic` dispatch for IndexedDB interop so that
 * Kotlin variables (key, data) are properly captured in closures.
 * Raw `js("...")` blocks CANNOT access Kotlin local variables.
 */
internal actual class LogFileManager actual constructor(
    context: PlatformContext,
    sessionTimestamp: String
) {
    private companion object {
        const val MAX_ENTRY_CHARS = 2 * 1024 * 1024 // ~2 MB in chars
        const val DB_NAME = "IndusJS"
        const val DB_VERSION = 1
        const val STORE_NAME = "FleetLogs"
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
        // Raw js() is safe here — no Kotlin variables needed
        js("""
            if (typeof navigator !== 'undefined' && navigator.storage && navigator.storage.persist) {
                navigator.storage.persist();
            }
        """)
    }

    private fun ensureDb() {
        // Use dynamic API so the onupgradeneeded callback can reference STORE_NAME
        try {
            val indexedDB: dynamic = js("indexedDB")
            val storeName = STORE_NAME
            val request = indexedDB.open(DB_NAME, DB_VERSION)
            request.onupgradeneeded = { event: dynamic ->
                val db = event.target.result
                if (!db.objectStoreNames.contains(storeName)) {
                    db.createObjectStore(storeName)
                }
                Unit
            }
        } catch (_: Throwable) {
            // IndexedDB may not be available in all environments
        }
    }

    private fun flush() {
        // Use dynamic API — Kotlin lambdas properly capture local variables via closure.
        // Raw js("...") blocks CANNOT access Kotlin locals (key/data would be undefined).
        val key = currentKey
        val data = buffer.toString()
        val storeName = STORE_NAME
        try {
            val indexedDB: dynamic = js("indexedDB")
            val request = indexedDB.open(DB_NAME, DB_VERSION)
            request.onsuccess = { event: dynamic ->
                val db = event.target.result
                val tx = db.transaction(storeName, "readwrite")
                val store = tx.objectStore(storeName)
                store.put(data, key)
                Unit
            }
        } catch (_: Throwable) {
            // Best-effort — IndexedDB may not be available
        }
    }
}

