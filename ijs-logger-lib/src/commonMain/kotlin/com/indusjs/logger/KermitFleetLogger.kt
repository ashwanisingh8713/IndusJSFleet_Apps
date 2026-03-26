package com.indusjs.logger

import com.indusjs.fleet.core.logger.FleetLogger

/**
 * [FleetLogger] backed by the Kermit [co.touchlab.kermit.Logger] instance from [IjsLogger].
 *
 * A single instance is created via [IjsLogger.createFleetLogger] and shared
 * across the entire app. Each call site passes its own tag.
 *
 * Internal — never exposed outside `ijs-logger-lib`. Consumers depend only
 * on the [FleetLogger] interface defined in `ijs-core-lib`.
 */
internal class KermitFleetLogger : FleetLogger {

    private val logger: co.touchlab.kermit.Logger get() = IjsLogger.logger

    override fun v(tag: String, message: String, throwable: Throwable?) =
        logger.v(tag = tag, throwable = throwable) { message }

    override fun d(tag: String, message: String, throwable: Throwable?) =
        logger.d(tag = tag, throwable = throwable) { message }

    override fun i(tag: String, message: String, throwable: Throwable?) =
        logger.i(tag = tag, throwable = throwable) { message }

    override fun w(tag: String, message: String, throwable: Throwable?) =
        logger.w(tag = tag, throwable = throwable) { message }

    override fun e(tag: String, message: String, throwable: Throwable?) =
        logger.e(tag = tag, throwable = throwable) { message }
}

