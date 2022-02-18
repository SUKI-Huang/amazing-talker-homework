package com.amazing.mvvm.model.dataSource.schedule

import com.amazing.base.BaseRemoteDataSource
import com.amazing.extensions.gmt
import com.amazing.extensions.plus
import com.amazing.mvvm.model.data.calendar.CalendarData
import com.amazing.mvvm.model.data.schedule.ScheduleData
import kotlinx.coroutines.delay
import java.util.*
import java.util.concurrent.TimeUnit

class TeacherScheduleRemoteDataSource : BaseRemoteDataSource<CalendarData>() {

    fun load(teacherName: String, _startAt: Date) {
        callOnStart()
        if (teacherName.isEmpty()) {
            callOnError(RuntimeException("teacher name can not be empty!"))
            return
        }
        request(
            request = {
                // simulate the response time. you can see how I handle the loading state on view
                delay(500)
                val scheduleData = apiInterfaceManager.scheduleApi.getSchedule(constant.getApiTeacherSchedule(teacherName, _startAt.gmt))
                val calendarData = processScheduleDate(scheduleData)
                return@request calendarData
            },
            onSuccess = {
                // do something you want to do before call on success, like trigger event center or something else
                callOnSuccess(it)
            },
            onError = {
                // do something you want to do on error, like sending error analytics event to firebase analytics or other third-party services
                callOnError(it)
            }
        )
    }

    // process complex date data in background thread to get better ui rendering performance
    // process date data here to prevent modified ui logic in the future, and made the ui component easier to understand
    private fun processScheduleDate(scheduleData: ScheduleData): CalendarData {

        val sunday = arrayListOf<CalendarData.Period>()
        val monday = arrayListOf<CalendarData.Period>()
        val tuesday = arrayListOf<CalendarData.Period>()
        val wednesday = arrayListOf<CalendarData.Period>()
        val thursday = arrayListOf<CalendarData.Period>()
        val friday = arrayListOf<CalendarData.Period>()
        val saturday = arrayListOf<CalendarData.Period>()

        fun addCalendarPeriod(calendarPeriod: CalendarData.Period) {
            val calendar = Calendar.getInstance().apply { this.time = calendarPeriod.startAt }
            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> sunday.add(calendarPeriod)
                Calendar.MONDAY -> monday.add(calendarPeriod)
                Calendar.TUESDAY -> tuesday.add(calendarPeriod)
                Calendar.WEDNESDAY -> wednesday.add(calendarPeriod)
                Calendar.THURSDAY -> thursday.add(calendarPeriod)
                Calendar.FRIDAY -> friday.add(calendarPeriod)
                Calendar.SATURDAY -> saturday.add(calendarPeriod)
            }
        }

        scheduleData.available?.flatMap {
            // check data is valid
            val startAt = it?.start ?: throw RuntimeException("the 'start' in the available list it null!")
            val endAt = it.end ?: throw RuntimeException("the 'end' in the available list it null!")
            getCalendarPeriod(startAt, endAt, true)
        }?.onEach { addCalendarPeriod(it) }

        scheduleData.booked?.flatMap {
            // check data is valid
            val startAt = it?.start ?: throw RuntimeException("the 'start' in the booked list it null!")
            val endAt = it.end ?: throw RuntimeException("the 'end' in the booked list it null!")
            getCalendarPeriod(startAt, endAt, false)
        }?.onEach { addCalendarPeriod(it) }

        sunday.sortBy { it.startAt }
        monday.sortBy { it.startAt }
        tuesday.sortBy { it.startAt }
        wednesday.sortBy { it.startAt }
        thursday.sortBy { it.startAt }
        friday.sortBy { it.startAt }
        saturday.sortBy { it.startAt }

        return CalendarData(sunday, monday, tuesday, wednesday, thursday, friday, saturday)
    }

    private fun getCalendarPeriod(startAt: Date, endAt: Date, isAvailable: Boolean): List<CalendarData.Period> {
        val interval = 30L // 30 min
        val calendarPeriodList = arrayListOf<CalendarData.Period>()
        var currentStartAt = startAt
        while (currentStartAt.time < endAt.time) {
            val nextStartAt = currentStartAt.plus(interval, TimeUnit.MINUTES)
            val calendarPeriod = CalendarData.Period(isAvailable, currentStartAt, nextStartAt)
            calendarPeriodList.add(calendarPeriod)
            currentStartAt = nextStartAt
        }
        return calendarPeriodList
    }
}
