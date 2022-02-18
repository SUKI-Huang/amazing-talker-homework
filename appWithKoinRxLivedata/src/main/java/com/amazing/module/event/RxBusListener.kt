package com.amazing.module.event

interface RxBusListener {
    fun onNetworkAvailableChanged(isAvailable: Boolean)
    fun onTimezoneChanged()
}
