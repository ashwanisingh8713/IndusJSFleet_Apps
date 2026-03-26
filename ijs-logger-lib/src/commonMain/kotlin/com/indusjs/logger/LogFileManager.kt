package com.indusjs.logger

/**
 * Manages log file creation, rotation, and writing per platform.
 *
 * Rules:
 * - New file per app launch (never appends to prior session).
 * - 2 MB max per file; rolls over with `_1`, `_2` suffix.
 * - Old logs retained; no auto-deletion.
 *
 * | Platform | Storage Location                                         |
 * |----------|----------------------------------------------------------|
 * | Android  | `sdcard/IndusJS/Fleet/Log/`                               |
 * | iOS      | `<AppDocumentsDirectory>/IndusJS/Fleet/Log/`              |
 * | Web      | IndexedDB → database: `IndusJS`, store: `FleetLogs`      |
 */
internal expect class LogFileManager(context: PlatformContext, sessionTimestamp: String) {
    fun write(entry: String)
    fun close()
}

