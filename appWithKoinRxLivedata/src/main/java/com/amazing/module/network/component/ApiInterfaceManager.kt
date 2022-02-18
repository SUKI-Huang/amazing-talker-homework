package com.amazing.module.network.component

import com.amazing.module.network.api.ScheduleApi
import com.amazing.module.network.api.TimeApi
import retrofit2.Retrofit

class ApiInterfaceManager(private val retrofit: Retrofit) {
    val timeApi: TimeApi by lazy { retrofit.create(TimeApi::class.java) }
    val scheduleApi: ScheduleApi by lazy { retrofit.create(ScheduleApi::class.java) }
}
