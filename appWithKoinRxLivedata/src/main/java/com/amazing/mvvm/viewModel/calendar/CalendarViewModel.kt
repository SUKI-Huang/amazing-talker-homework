package com.amazing.mvvm.viewModel.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.amazing.base.BaseViewModel
import com.amazing.base.toLiveData
import com.amazing.extensions.getCalendarStartAt
import com.amazing.extensions.plus
import com.amazing.module.network.component.RequestState
import com.amazing.mvvm.model.data.calendar.CalendarData
import com.amazing.mvvm.model.repository.schedule.TeacherScheduleRepository
import com.amazing.mvvm.model.repository.time.CurrentTimeRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.TimeUnit

class CalendarViewModel : BaseViewModel() {
    private val currentTimeRepository: CurrentTimeRepository by inject()
    private val teacherScheduleRepository: TeacherScheduleRepository by inject()
    private var previousCalendarStartAt: Date? = null
    private var nextCalendarStartAt: Date? = null
    private var defaultCalendarStartAt: Date? = null
    private var currentDateTime: Date? = null
    private var teacherName: String? = null

    private val _currentCalendarStartAtStateLiveData = MutableLiveData<Date>()
    val currentCalendarStartAtStateLiveData: LiveData<Date> = _currentCalendarStartAtStateLiveData
    val teacherScheduleRequestStateLiveData: LiveData<RequestState<CalendarData>> by lazy { teacherScheduleRepository.teacherScheduleRequestState.toLiveData(compositeDisposable) }
    val isPreviousWeekAvailableStateLiveData: LiveData<Boolean> = Transformations.map(_currentCalendarStartAtStateLiveData) { it.plus(-7, TimeUnit.DAYS).time >= defaultCalendarStartAt!!.time }

    init {
        initCurrentDateTime()
    }

    private fun initCurrentDateTime() {
        addDisposable(
            currentTimeRepository.currentTimeRequestState
                .filter { it is RequestState.OnSuccess<Date> }
                .map { it as RequestState.OnSuccess<Date> }
                .map { it.data }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        val currentDateTime = it
                        val currentCalendarStartAt = currentDateTime.getCalendarStartAt()
                        this.currentDateTime = currentDateTime
                        this.defaultCalendarStartAt = currentCalendarStartAt
                        _currentCalendarStartAtStateLiveData.postValue(currentCalendarStartAt)
                        previousCalendarStartAt = currentCalendarStartAt.plus(-7, TimeUnit.DAYS)
                        nextCalendarStartAt = currentCalendarStartAt.plus(-7, TimeUnit.DAYS)
                        if (teacherName != null) setTeacher(teacherName!!)
                    },
                    onError = { /* it will crash while onError not set and occur exception at sametime*/ }
                )
        )
    }

    fun setTeacher(teacherName: String) {
        // prevent abuse, just return or throw exception
        if (teacherName.isEmpty()) return
        this.teacherName = teacherName
        val currentDateTime = this.currentDateTime
        val defaultCalendarStartAt = this.defaultCalendarStartAt
        if (currentDateTime == null || defaultCalendarStartAt == null) {
            currentTimeRepository.loadCurrentDateTime()
        } else {
            previousCalendarStartAt = defaultCalendarStartAt.plus(-7, TimeUnit.DAYS)
            nextCalendarStartAt = defaultCalendarStartAt.plus(7, TimeUnit.DAYS)
            _currentCalendarStartAtStateLiveData.postValue(defaultCalendarStartAt)
            teacherScheduleRepository.loadTeacherSchedule(teacherName, defaultCalendarStartAt)
        }
    }

    fun goNextWeek() {
        val teacherName = this.teacherName ?: return
        val nextCalendarStartAt = nextCalendarStartAt ?: return
        this.previousCalendarStartAt = nextCalendarStartAt.plus(-7, TimeUnit.DAYS)
        this.nextCalendarStartAt = nextCalendarStartAt.plus(7, TimeUnit.DAYS)
        _currentCalendarStartAtStateLiveData.postValue(nextCalendarStartAt)
        teacherScheduleRepository.loadTeacherSchedule(teacherName, nextCalendarStartAt)
    }

    fun goPreviousWeek() {
        val teacherName = this.teacherName ?: return
        val previousCalendarStartAt = previousCalendarStartAt ?: return
        val defaultCalendarStartAt = defaultCalendarStartAt ?: return
        val canGoPrevious = previousCalendarStartAt >= defaultCalendarStartAt
        if (!canGoPrevious) return
        this.previousCalendarStartAt = previousCalendarStartAt.plus(-7, TimeUnit.DAYS)
        this.nextCalendarStartAt = previousCalendarStartAt.plus(7, TimeUnit.DAYS)
        _currentCalendarStartAtStateLiveData.postValue(previousCalendarStartAt)
        teacherScheduleRepository.loadTeacherSchedule(teacherName, previousCalendarStartAt)
    }

    fun retry() {
        val teacherName = this.teacherName ?: return
        val currentCalendarStartAt = _currentCalendarStartAtStateLiveData.value ?: return
        teacherScheduleRepository.loadTeacherSchedule(teacherName, currentCalendarStartAt)
    }

    fun getCurrentDateTime() = currentDateTime!!
}
