package com.amazing.module.network.api

import com.amazing.mvvm.model.data.schedule.ScheduleData
import retrofit2.http.GET
import retrofit2.http.Url

interface ScheduleApi {

    @GET
    suspend fun getSchedule(
        @Url url: String
    ): ScheduleData
}
