package com.amazing.module.network.component

import kotlinx.coroutines.flow.MutableStateFlow

sealed class RequestState<T> {

    companion object {
        fun <T> callOnStart(): RequestState<T> = OnStart()
        fun <T> callOnSuccess(data: T): RequestState<T> = OnSuccess(data)
        fun <T> callOnFailed(throwable: Throwable): RequestState<T> = OnError(throwable)
    }

    class OnStart<T> : RequestState<T>()
    data class OnSuccess<T>(var data: T) : RequestState<T>()
    data class OnError<T>(val throwable: Throwable) : RequestState<T>()
}

fun <T> MutableStateFlow<RequestState<T>?>.callOnError(throwable: Throwable) {
    value = RequestState.callOnFailed(throwable)
}

fun <T> MutableStateFlow<RequestState<T>?>.callOnSuccess(t: T) {
    value = RequestState.callOnSuccess(t)
}

fun <T> MutableStateFlow<RequestState<T>?>.callOnStart() {
    value = RequestState.callOnStart()
}
