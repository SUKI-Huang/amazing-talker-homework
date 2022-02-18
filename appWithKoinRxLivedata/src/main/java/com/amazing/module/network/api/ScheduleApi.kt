package com.amazing.module.network.api

import com.amazing.mvvm.model.data.schedule.ScheduleData
import io.reactivex.rxjava3.core.Flowable
import retrofit2.http.GET
import retrofit2.http.Url

interface ScheduleApi {

    @GET
    fun getSchedule(
        @Url url: String
    ): Flowable<ScheduleData>
}
