package com.amazing.module.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.trello.rxlifecycle4.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

object RxBus {
    private val publisher = PublishSubject.create<Any>()

    private fun <T : RxBusEvent> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

    fun <T : RxBusEvent> publish(event: T) {
        publisher.onNext(event)
    }

    fun initListener(rxBusListener: RxBusListener, lifecycleOwner: LifecycleOwner) {
        listen(NetworkChangedEvent::class.java).bindUntilEvent(lifecycleOwner, Lifecycle.Event.ON_DESTROY).subscribe { rxBusListener.onNetworkAvailableChanged(it.isAvailable) }
        listen(TimezoneChangedEvent::class.java).bindUntilEvent(lifecycleOwner, Lifecycle.Event.ON_DESTROY).subscribe { rxBusListener.onTimezoneChanged() }
    }
}
