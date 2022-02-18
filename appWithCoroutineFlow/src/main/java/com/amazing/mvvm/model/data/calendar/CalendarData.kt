package com.amazing.mvvm.model.data.calendar

import java.text.SimpleDateFormat
import java.util.*

class CalendarData(
    val sunday: List<Period>,
    val monday: List<Period>,
    val tuesday: List<Period>,
    val wednesday: List<Period>,
    val thursday: List<Period>,
    val friday: List<Period>,
    val saturday: List<Period>
) {
    // if the schedule period state not only available and booked, consider to change `isAvailable` to enum
    class Period(val isAvailable: Boolean, val startAt: Date, val endAt: Date)
}

// calendar data extensions
fun CalendarData.isSundayAvailable(currentTime: Date): Boolean = sunday.any { it.isAvailable && (it.startAt.time > currentTime.time) }
fun CalendarData.isMondayAvailable(currentTime: Date): Boolean = monday.any { it.isAvailable && (it.startAt.time > currentTime.time) }
fun CalendarData.isTuesdayAvailable(currentTime: Date): Boolean = tuesday.any { it.isAvailable && (it.startAt.time > currentTime.time) }
fun CalendarData.isWednesdayAvailable(currentTime: Date): Boolean = wednesday.any { it.isAvailable && (it.startAt.time > currentTime.time) }
fun CalendarData.isThursdayAvailable(currentTime: Date): Boolean = thursday.any { it.isAvailable && (it.startAt.time > currentTime.time) }
fun CalendarData.isFridayAvailable(currentTime: Date): Boolean = friday.any { it.isAvailable && (it.startAt.time > currentTime.time) }
fun CalendarData.isSaturdayAvailable(currentTime: Date): Boolean = saturday.any { it.isAvailable && (it.startAt.time > currentTime.time) }

// calendar period extensions
fun CalendarData.Period.isDisplayAvailable(currentTime: Date): Boolean = this.isAvailable && (this.startAt.time > currentTime.time)
val CalendarData.Period.displayTime: String
    get() {
        // consider declare `dateFormat` as global variable to reduce cpu and memory usage (if access frequently)
        val dateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return dateFormat.format(this.startAt)
    }
