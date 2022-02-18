package com.amazing.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.koin.core.component.KoinComponent

open class BaseViewModel : ViewModel(), KoinComponent {

    val compositeDisposable = CompositeDisposable()

    protected fun addDisposable(disposable: Disposable) = compositeDisposable.add(disposable)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

fun <T> PublishSubject<T>.toLiveData(compositeDisposable: CompositeDisposable): MutableLiveData<T> {
    val data = MutableLiveData<T>()
    compositeDisposable.add(this.subscribe({ t: T -> data.value = t }, {}))
    return data
}
