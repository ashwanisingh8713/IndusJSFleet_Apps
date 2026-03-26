package com.indusjs.logger

internal actual fun currentThreadId(): Long = android.os.Process.myTid().toLong()

internal actual fun currentThreadName(): String = Thread.currentThread().name

