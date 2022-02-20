package com.amazing.mvvm.viewModel.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.amazing.base.BaseViewModel
import com.amazing.base.toLiveData
import com.amazing.module.network.component.RequestState
import com.amazing.mvvm.model.data.calendar.CalendarData
import com.amazing.mvvm.model.repository.schedule.TeacherScheduleRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.inject

class CalendarViewModel : BaseViewModel() {

    private val teacherScheduleRepository: TeacherScheduleRepository by inject()
    private var teacherName: String? = null
    private var calendarData: CalendarData? = null
    val teacherScheduleRequestStateLiveData: LiveData<RequestState<CalendarData>> by lazy { teacherScheduleRepository.teacherScheduleRequestState.toLiveData(compositeDisposable) }
    val isPreviousWeekAvailableStateLiveData: LiveData<Boolean> = Transformations.map(teacherScheduleRequestStateLiveData) { (it as? RequestState.OnSuccess<CalendarData>)?.data?.previousCalendarStartAt != null }

    init {
        initCurrentDateTime()
    }

    private fun initCurrentDateTime() {
        addDisposable(
            teacherScheduleRepository.teacherScheduleRequestState
                .filter { it is RequestState.OnSuccess<CalendarData> }
                .map { (it as RequestState.OnSuccess<CalendarData>).data }
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { this.calendarData = it },
                    onError = { /* it will crash while onError not set and occur exception at sametime*/ }
                )
        )
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
