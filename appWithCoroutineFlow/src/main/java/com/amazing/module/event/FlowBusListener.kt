package com.amazing.module.event

interface FlowBusListener {
    fun onNetworkAvailableChanged(isAvailable: Boolean)
    fun onTimezoneChanged()
}
