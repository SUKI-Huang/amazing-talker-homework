package com.amazing.module.event

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.amazing.extensions.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SystemEventCenter(private val application: Application) {
    val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    val mainCoroutineScope = CoroutineScope(Dispatchers.Main)
    private var isInitialized = false
    private val eventReceiver = EventReceiver()
    private val networkChannel = Channel<Boolean>()

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
        ioCoroutineScope.launch {
            networkChannel.receiveAsFlow()
                .debounce(500)
                .collect { isNetworkAvailable -> mainCoroutineScope.launch { FlowBus.publish(NetworkChangedEvent(isNetworkAvailable)) } }
        }
    }

    inner class EventReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.WIFI_STATE_CHANGED_ACTION -> ioCoroutineScope.launch { networkChannel.send(isNetworkAvailable) }
                Intent.ACTION_TIMEZONE_CHANGED -> mainCoroutineScope.launch { FlowBus.publish(TimezoneChangedEvent()) }
            }
        }
    }
}
