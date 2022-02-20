package com.amazing.mvvm.viewModel.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazing.module.network.component.RequestState
import com.amazing.mvvm.model.data.calendar.CalendarData
import com.amazing.mvvm.model.repository.schedule.TeacherScheduleRepository
import kotlinx.coroutines.flow.*

class CalendarViewModel : ViewModel() {
    private val teacherScheduleRepository by lazy { TeacherScheduleRepository() }

    private var teacherName: String? = null
    private var calendarData: CalendarData? = null
    val teacherScheduleRequestState: StateFlow<RequestState<CalendarData>?> by lazy { teacherScheduleRepository.teacherScheduleRequestState }
    val isPreviousWeekAvailableState = teacherScheduleRequestState.filterIsInstance<RequestState.OnSuccess<CalendarData>>().map { it.data.previousCalendarStartAt != null }

    init {
        initState()
    }

    private fun initState() {
        teacherScheduleRequestState
            .filterIsInstance<RequestState.OnSuccess<CalendarData>>()
            .onEach { calendarData = it.data }
            .launchIn(viewModelScope)
    }

    fun setTeacher(teacherName: String) {
        // prevent abuse, just return or throw exception
        if (teacherName.isEmpty()) return
        this.teacherName = teacherName
        teacherScheduleRepository.loadTeacherSchedule(teacherName)
    }

    fun goNextWeek() {
        val calendarData = calendarData ?: return
        val teacherName = teacherName ?: return
        teacherScheduleRepository.loadTeacherSchedule(teacherName, calendarData.nextCalendarStartAt)
    }

    fun goPreviousWeek() {
        val calendarData = calendarData ?: return
        val teacherName = teacherName ?: return
        val previousCalendarStartAt = calendarData.previousCalendarStartAt ?: return
        teacherScheduleRepository.loadTeacherSchedule(teacherName, previousCalendarStartAt)
    }

    fun retry() {
        val teacherName = teacherName ?: return
        val currentCalendarStartAt = calendarData?.currentCalendarStartAt
        teacherScheduleRepository.loadTeacherSchedule(teacherName, currentCalendarStartAt)
    }
}
