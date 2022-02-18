package com.amazing.mvvm.viewModel.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazing.extensions.getCalendarStartAt
import com.amazing.extensions.plus
import com.amazing.module.network.component.RequestState
import com.amazing.mvvm.model.data.calendar.CalendarData
import com.amazing.mvvm.model.repository.schedule.TeacherScheduleRepository
import com.amazing.mvvm.model.repository.time.CurrentTimeRepository
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.TimeUnit

class CalendarViewModel : ViewModel() {

    private val currentTimeRepository by lazy { CurrentTimeRepository() }
    private val teacherScheduleRepository by lazy { TeacherScheduleRepository() }

    private val _teacherNameState = MutableStateFlow<String?>(null)
    private val _currentDateTimeState = MutableStateFlow<Date?>(null)
    private val _currentCalendarStartAtState = MutableStateFlow<Date?>(null)
    private val _previousCalendarStartAtState = MutableStateFlow<Date?>(null)
    private val _nextCalendarStartAtState = MutableStateFlow<Date?>(null)
    private var defaultCalendarStartAt: Date? = null

    // change it from MutableStateFlow to StateFlow, prevent state modified from view layer
    // make sure the state changed because of network request from remote data source
    private val currentTimeRequestState: StateFlow<RequestState<Date>?> by lazy { currentTimeRepository.currentTimeRequestState }
    val teacherScheduleRequestState: StateFlow<RequestState<CalendarData>?> by lazy { teacherScheduleRepository.teacherScheduleRequestState }

    private val _isPreviousAvailableState = MutableStateFlow(false)
    val isPreviousWeekAvailableState: StateFlow<Boolean> = _isPreviousAvailableState
    val currentCalendarStartAtState = _currentCalendarStartAtState

    init {
        initState()
    }

    private fun initState() {
        // when teacher name changed, reload currentDateTime
        _teacherNameState.filterNotNull().onEach {
            val currentDateTime = _currentDateTimeState.value
            if (currentDateTime == null) loadCurrentTime() else loadTeacherSchedule(currentDateTime)
        }.launchIn(viewModelScope)

        // current calendar startAt changed, then update page state and load schedule
        _currentCalendarStartAtState.filterNotNull().onEach {
            _previousCalendarStartAtState.value = it.plus(-7, TimeUnit.DAYS)
            _nextCalendarStartAtState.value = it.plus(7, TimeUnit.DAYS)
            _isPreviousAvailableState.value = _previousCalendarStartAtState.value!!.time >= defaultCalendarStartAt!!.time
            loadTeacherSchedule(_currentCalendarStartAtState.value!!)
        }.launchIn(viewModelScope)

        // when current time changed load teacher schedule
        currentTimeRequestState.filterIsInstance<RequestState.OnSuccess<Date>>().onEach {
            val currentDateTime = it.data
            val currentCalendarStartAt = currentDateTime.getCalendarStartAt()
            defaultCalendarStartAt = currentCalendarStartAt
            _currentDateTimeState.value = currentDateTime
            _currentCalendarStartAtState.value = currentCalendarStartAt
        }.launchIn(viewModelScope)
    }

    private fun loadCurrentTime() = currentTimeRepository.loadCurrentTime()

    private fun loadTeacherSchedule(currentCalendarStartAt: Date) {
        val teacherName = _teacherNameState.value ?: return
        teacherScheduleRepository.loadTeacherSchedule(teacherName, currentCalendarStartAt)
    }

    fun setTeacher(teacherName: String) {
        // prevent abuse, just return or throw exception
        if (teacherName.isEmpty()) return
        _teacherNameState.value = teacherName
    }

    fun goNextWeek() {
        val nextCalendarStartAt = _nextCalendarStartAtState.value ?: return
        _currentCalendarStartAtState.value = nextCalendarStartAt
    }

    fun goPreviousWeek() {
        val previousCalendarStartAt = _previousCalendarStartAtState.value ?: return
        val defaultCalendarStartAt = defaultCalendarStartAt ?: return
        val canGoPrevious = previousCalendarStartAt.time >= defaultCalendarStartAt.time
        if (!canGoPrevious) return
        _currentCalendarStartAtState.value = previousCalendarStartAt
    }

    fun retry() {
        if (_teacherNameState.value == null) return
        val currentDateTime = _currentCalendarStartAtState.value ?: return
        loadTeacherSchedule(currentDateTime)
    }

    fun getCurrentDateTime() = _currentDateTimeState.value!!
}
