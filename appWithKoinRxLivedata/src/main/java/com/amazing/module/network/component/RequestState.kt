package com.amazing.module.network.component

import io.reactivex.rxjava3.subjects.PublishSubject

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

fun <T> PublishSubject<RequestState<T>>.callOnError(throwable: Throwable) {
    with(this) {
        onNext(RequestState.callOnFailed(throwable))
    }
}

fun <T> PublishSubject<RequestState<T>>.callOnSuccess(t: T) {
    with(this) {
        onNext(RequestState.callOnSuccess(t))
    }
}

fun <T> PublishSubject<RequestState<T>>.callOnStart() {
    this.onNext(RequestState.callOnStart())
}
