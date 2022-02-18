package com.amazing.base

import com.amazing.constant.Constant
import com.amazing.module.network.component.ApiInterfaceManager
import io.reactivex.rxjava3.disposables.Disposable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.reactivestreams.Subscription
import retrofit2.Retrofit

open class BaseRemoteDataSource<T> : KoinComponent {

    private var subscription: Subscription? = null
    private var disposable: Disposable? = null
    private var onStartListener: (() -> Unit)? = null
    private var onSuccessListener: ((T) -> Unit)? = null
    private var onErrorListener: ((Throwable) -> Unit)? = null

    var isLoading: Boolean = false
        private set

    val retrofit: Retrofit by inject()
    val apiInterfaceManager: ApiInterfaceManager by inject()
    val constant: Constant by inject()

    fun setSubscription(subscription: Subscription): BaseRemoteDataSource<T> {
        this.subscription = subscription
        return this
    }

    // allow cancel from outside (if needed)
    fun cancel() {
        subscription?.cancel()
        subscription = null
        if (disposable != null) {
            if (!disposable!!.isDisposed) disposable!!.dispose()
            disposable = null
        }
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

    fun request(request: (() -> Disposable)) {
        this.disposable = request.invoke()
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
