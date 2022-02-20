package com.amazing.mvvm.model.repository.schedule

import com.amazing.module.network.component.RequestState
import com.amazing.module.network.component.callOnError
import com.amazing.module.network.component.callOnStart
import com.amazing.module.network.component.callOnSuccess
import com.amazing.mvvm.model.data.calendar.CalendarData
import com.amazing.mvvm.model.dataSource.schedule.TeacherScheduleRemoteDataSource
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

class TeacherScheduleRepository {

    private val teacherScheduleRemoteDataSource by lazy {
        TeacherScheduleRemoteDataSource().apply {
            setOnStartListener { teacherScheduleRequestState.callOnStart() }
            setOnSuccessListener { teacherScheduleRequestState.callOnSuccess(it) }
            setOnErrorListener { teacherScheduleRequestState.callOnError(it) }
        }
    }

    val teacherScheduleRequestState by lazy<PublishSubject<RequestState<CalendarData>>> { PublishSubject.create() }

    fun loadTeacherSchedule(teacherName: String, startAt: Date? = null) = teacherScheduleRemoteDataSource.load(teacherName, startAt)
}
