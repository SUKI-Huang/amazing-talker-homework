package com.amazing.module.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object FlowBus {
    private val flowPublisher = MutableSharedFlow<Any>()
    private val mainCoroutineScope = CoroutineScope(Dispatchers.IO)

    fun publish(event: FlowBusEvent) {
        mainCoroutineScope.launch { flowPublisher.emit(event) }
    }

    fun initListener(flowBusListener: FlowBusListener, lifecycleOwner: LifecycleOwner) {
        flowPublisher.asSharedFlow().filterIsInstance<TimezoneChangedEvent>().onEach { flowBusListener.onTimezoneChanged() }.launchIn(lifecycleOwner.lifecycleScope)
        flowPublisher.asSharedFlow().filterIsInstance<NetworkChangedEvent>().onEach { flowBusListener.onNetworkAvailableChanged(it.isAvailable) }.launchIn(lifecycleOwner.lifecycleScope)
    }
}
