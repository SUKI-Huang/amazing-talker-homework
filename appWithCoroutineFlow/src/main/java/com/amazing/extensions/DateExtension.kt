package com.amazing.extensions

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// if your application invoke this extension frequently, make the dateFormat as global variable for better performance and lower memory usage.
// val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

val Date.gmt: String
    get() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(this)
    }

fun Date.plus(value: Long, timeUnit: TimeUnit): Date {
    return when (timeUnit) {
        TimeUnit.NANOSECONDS -> throw RuntimeException("NANOSECONDS is unsupported unit")
        TimeUnit.MICROSECONDS -> throw RuntimeException("NANOSECONDS is unsupported unit")
        TimeUnit.MILLISECONDS -> Date(this.time + value)
        TimeUnit.SECONDS -> Date(this.time + value * 1000L)
        TimeUnit.MINUTES -> Date(this.time + value * 1000L * 60L)
        TimeUnit.HOURS -> Date(this.time + value * 1000L * 60L * 60L)
        TimeUnit.DAYS -> Date(this.time + value * 1000L * 60L * 60L * 24L)
    }
}

fun Date.getCalendarStartAt(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    return calendar.time
}

val TimeZone.displayInformation: String
    get() {
        // (GMT+08:00)
        val displayName = getDisplayName(false, TimeZone.SHORT)
        val region = getDisplayName(Locale.getDefault())
        return "$region ($displayName)"
    }
