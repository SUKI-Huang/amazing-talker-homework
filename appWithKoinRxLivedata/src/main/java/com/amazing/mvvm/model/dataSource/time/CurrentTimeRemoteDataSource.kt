package com.amazing.mvvm.model.dataSource.time

import com.amazing.base.BaseRemoteDataSource
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class CurrentTimeRemoteDataSource : BaseRemoteDataSource<Date>() {

    fun load() {
        callOnStart()
        apiInterfaceManager.timeApi.get(constant.getApiCurrentTime())
            .map {
                // simulate the response time. you guys can see how I handle the loading state on view
                Thread.sleep(500)

                // if network is unavailable, there's no way to check the real time so use local time as workaround, or throw exception
                val date = it.headers().getDate("date") ?: Date(System.currentTimeMillis())
                return@map date
            }
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { this.setSubscription(it) }
            .subscribeBy(
                onNext = {
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
