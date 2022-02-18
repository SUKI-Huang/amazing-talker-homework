package com.amazing.module.event

class NetworkChangedEvent(val isAvailable: Boolean) : RxBusEvent()
class TimezoneChangedEvent : RxBusEvent()
open class RxBusEvent
