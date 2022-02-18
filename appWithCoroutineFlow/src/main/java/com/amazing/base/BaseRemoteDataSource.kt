package com.amazing.base

import com.amazing.application.AmazingApplication
import kotlinx.coroutines.*

open class BaseRemoteDataSource<T> {
    private var onStartListener: (() -> Unit)? = null
    private var onSuccessListener: ((T) -> Unit)? = null
    private var onErrorListener: ((Throwable) -> Unit)? = null

    private var ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    private var mainCoroutineScope = CoroutineScope(Dispatchers.Main)
    var isLoading: Boolean = false
        private set

    val retrofit by lazy { AmazingApplication.dependencyInjection.retrofit }
    val apiInterfaceManager by lazy { AmazingApplication.dependencyInjection.apiInterfaceManager }
    val constant by lazy { AmazingApplication.dependencyInjection.constant }

    // allow cancel from outside (if needed)
    fun cancel() {
        if (ioCoroutineScope.isActive) ioCoroutineScope.cancel()
        if (mainCoroutineScope.isActive) mainCoroutineScope.cancel()
        ioCoroutineScope = CoroutineScope(Dispatchers.IO)
        mainCoroutineScope = CoroutineScope(Dispatchers.Main)
    }

    protected fun callOnStart() {
        if (isLoading) cancel()
        isLoading = true
        onStartListener?.invoke()
    }

    fun callOnSuccess(t: T) {
        isLoading = false
        onSuccessListener?.invoke(t)
        cancel()
    }

    protected fun callOnError(throwable: Throwable) {
        isLoading = false
        onErrorListener?.invoke(throwable)
        cancel()
    }

    // you can choose the thread mode when onSuccess or onError by using onSuccessDispatcher and onErrorDispatcher
    // the default thread is Main thread , because most scenario we will pass the result to ViewModel and change UI state in Main Thread
    // but if your remote data source is only process data or update the local database, your can choose it on background Thread
    fun <T> request(
        request: suspend () -> T,
        onSuccess: (value: T) -> Unit,
        onError: (throwable: Throwable) -> Unit,
        onSuccessDispatcher: CoroutineDispatcher = Dispatchers.Main,
        onErrorDispatcher: CoroutineDispatcher = Dispatchers.Main,
    ) {
        ioCoroutineScope.launch {
            try {
                val value = request.invoke()
                when (onSuccessDispatcher) {
                    Dispatchers.Main -> mainCoroutineScope.launch { onSuccess.invoke(value) }
                    Dispatchers.IO -> ioCoroutineScope.launch { onSuccess.invoke(value) }
                }
            } catch (e: Exception) {
                when (onErrorDispatcher) {
                    Dispatchers.Main -> mainCoroutineScope.launch { onError.invoke(e) }
                    Dispatchers.IO -> ioCoroutineScope.launch { onError.invoke(e) }
                }
            }
        }
    }

    fun setOnStartListener(listener: (() -> Unit)?) {
        onStartListener = listener
    }

    fun setOnSuccessListener(listener: ((T) -> Unit)?) {
        onSuccessListener = listener
    }

    fun setOnErrorListener(listener: ((Throwable) -> Unit)?) {
        onErrorListener = listener
    }
}
