package com.amazing.extensions

import okhttp3.Request
import okio.Buffer

fun Request.stringifyRequestBody(): String? {
    return try {
        val copy = this.newBuilder().build()
        val buffer = Buffer()
        copy.body!!.writeTo(buffer)
        buffer.readUtf8()
    } catch (e: Exception) {
        null
    }
}
