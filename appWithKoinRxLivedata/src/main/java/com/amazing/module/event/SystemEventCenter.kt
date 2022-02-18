package com.amazing.module.event

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.amazing.extensions.isNetworkAvailable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SystemEventCenter(private val application: Application) {
    private var isInitialized = false
    private val eventReceiver = EventReceiver()
    private val networkPublishProcessor = PublishProcessor.create<Boolean>()

    fun initialize() {
        if (isInitialized) return
        isInitialized = true
        init()
        initAction()
    }

    private fun init() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        application.registerReceiver(eventReceiver, intentFilter)
    }

    private fun initAction() {
        Flowable.fromPublisher(networkPublishProcessor)
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { RxBus.publish(NetworkChangedEvent(it)) }
    }

    inner class EventReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.WIFI_STATE_CHANGED_ACTION -> networkPublishProcessor.onNext(isNetworkAvailable)
                Intent.ACTION_TIMEZONE_CHANGED -> RxBus.publish(TimezoneChangedEvent())
            }
        }
    }
}
