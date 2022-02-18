package com.amazing.mvvm.model.dataSource.time

import com.amazing.base.BaseRemoteDataSource
import kotlinx.coroutines.delay
import java.util.*

class CurrentTimeRemoteDataSource : BaseRemoteDataSource<Date>() {

    fun load() {
        callOnStart()
        request(
            request = {
                // simulate the response time. you guys can see how I handle the loading state on view
                delay(500)

                // in order to prevent user change local time to access earlier schedule, get the real time from server
                // but I don't have server for this homework, so fetch the real time from google is a workaround
                val currentTimeResponse = apiInterfaceManager.timeApi.get(constant.getApiCurrentTime())
                // if network is unavailable, there's no way to check the real time so use local time as workaround, or throw exception
                val date = currentTimeResponse.headers().getDate("date") ?: Date(System.currentTimeMillis())
                return@request date
            },
            onSuccess = {
                // do something you want to do before call on success, like trigger event center or something else
                callOnSuccess(it)
            },
            onError = {
                // do something you want to do on error, like sending error analytics event to firebase analytics or other third-party services
                // callOnError(it)

                // make sure the current time always have value
                callOnSuccess(Date(System.currentTimeMillis()))
            }
        )
    }
}
