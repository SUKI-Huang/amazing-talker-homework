package com.amazing.module.event

class NetworkChangedEvent(val isAvailable: Boolean) : FlowBusEvent()
class TimezoneChangedEvent : FlowBusEvent()
open class FlowBusEvent
