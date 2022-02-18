package com.amazing.extensions

import android.content.ContextWrapper
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.amazing.base.BaseActivity
import com.jakewharton.rxbinding4.view.clicks
import com.trello.rxlifecycle4.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

val View.activity: BaseActivity<*>?
    get() {
        var context = context
        while (context is ContextWrapper) {
            if (context is BaseActivity<*>) {
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

// get the first one or the last one duration the time range, prevent user click so fast and waste cpu, memory or useless network request
enum class StreamType {
    None, ThrottleFirst, Debounce
}

fun View.onClick(lifecycleOwner: LifecycleOwner, duration: Long = 500L, streamType: StreamType = StreamType.ThrottleFirst, action: () -> Unit) {
    clicks()
        .let {
            when (streamType) {
                StreamType.ThrottleFirst -> it.throttleFirst(duration, TimeUnit.MILLISECONDS)
                StreamType.Debounce -> it.debounce(duration, TimeUnit.MILLISECONDS)
                StreamType.None -> {
                    /* do nothing */ it
                }
            }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .bindUntilEvent(lifecycleOwner, Lifecycle.Event.ON_DESTROY)
        .subscribe { action.invoke() }
}
