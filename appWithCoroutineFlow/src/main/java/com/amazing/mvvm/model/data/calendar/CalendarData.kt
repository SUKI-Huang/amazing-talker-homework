package com.amazing.mvvm.model.data.calendar

import java.text.SimpleDateFormat
import java.util.*

class CalendarData(
    val previousCalendarStartAt: Date?,
    val currentCalendarStartAt: Date,
    val nextCalendarStartAt: Date,
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
fun CalendarData.isSundayAvailable(): Boolean = sunday.any { it.isAvailable }
fun CalendarData.isMondayAvailable(): Boolean = monday.any { it.isAvailable }
fun CalendarData.isTuesdayAvailable(): Boolean = tuesday.any { it.isAvailable }
fun CalendarData.isWednesdayAvailable(): Boolean = wednesday.any { it.isAvailable }
fun CalendarData.isThursdayAvailable(): Boolean = thursday.any { it.isAvailable }
fun CalendarData.isFridayAvailable(): Boolean = friday.any { it.isAvailable }
fun CalendarData.isSaturdayAvailable(): Boolean = saturday.any { it.isAvailable }

// calendar period extensions
val CalendarData.Period.displayTime: String
    get() {
        // consider declare `dateFormat` as global variable to reduce cpu and memory usage (if access frequently)
        val dateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return dateFormat.format(this.startAt)
    }
