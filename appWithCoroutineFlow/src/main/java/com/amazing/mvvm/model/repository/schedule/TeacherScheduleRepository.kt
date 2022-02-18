package com.amazing.mvvm.model.repository.schedule

import com.amazing.module.network.component.RequestState
import com.amazing.module.network.component.callOnError
import com.amazing.module.network.component.callOnStart
import com.amazing.module.network.component.callOnSuccess
import com.amazing.mvvm.model.data.calendar.CalendarData
import com.amazing.mvvm.model.dataSource.schedule.TeacherScheduleRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class TeacherScheduleRepository {

    private val teacherScheduleRemoteDataSource by lazy {
        TeacherScheduleRemoteDataSource().apply {
            setOnStartListener { teacherScheduleRequestState.callOnStart() }
            setOnSuccessListener { teacherScheduleRequestState.callOnSuccess(it) }
            setOnErrorListener { teacherScheduleRequestState.callOnError(it) }
        }
    }

    // flow is different from livedata, it need a default value, so make the default value as null, and the RequestState here is nullable
    val teacherScheduleRequestState by lazy { MutableStateFlow<RequestState<CalendarData>?>(null) }

    fun loadTeacherSchedule(teacherName: String, startAt: Date) = teacherScheduleRemoteDataSource.load(teacherName, startAt)
}
