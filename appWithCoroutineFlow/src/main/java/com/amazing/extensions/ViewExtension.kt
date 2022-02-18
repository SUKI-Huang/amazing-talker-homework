package com.amazing.extensions

import android.content.ContextWrapper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks

val View.activity: AppCompatActivity?
    get() {
        var context = context
        while (context is ContextWrapper) {
            if (context is AppCompatActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

fun View.setVisibility(visible: Boolean) {
    if (this.visibility == View.GONE && !visible) return
    if (this.visibility == View.VISIBLE && visible) return
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

enum class StreamType {
    None, ThrottleFirst, Debounce
}

// get the first one or the last one duration the time range, prevent user click so fast and waste cpu, memory or useless network request
fun View.onClick(coroutineScope: LifecycleCoroutineScope, duration: Long = 500L, streamType: StreamType = StreamType.ThrottleFirst, action: () -> Unit) {
    clicks()
        .let {
            when (streamType) {
                StreamType.ThrottleFirst -> {
                    if (duration > 0) it.throttleFirst(duration) else it
                }
                StreamType.Debounce -> it.debounce(duration)
                StreamType.None -> {
                    /* do nothing */ it
                }
            }
        }
        .onEach { action.invoke() }
        .launchIn(coroutineScope)
}
