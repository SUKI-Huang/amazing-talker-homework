package com.amazing.mvvm.model.repository.time

import com.amazing.module.network.component.RequestState
import com.amazing.module.network.component.callOnError
import com.amazing.module.network.component.callOnStart
import com.amazing.module.network.component.callOnSuccess
import com.amazing.mvvm.model.dataSource.time.CurrentTimeRemoteDataSource
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

class CurrentTimeRepository {

    private val currentTimeRemoteDataSource by lazy {
        CurrentTimeRemoteDataSource().apply {
            setOnStartListener { currentTimeRequestState.callOnStart() }
            setOnSuccessListener { currentTimeRequestState.callOnSuccess(it) }
            setOnErrorListener { currentTimeRequestState.callOnError(it) }
        }
    }

    val currentTimeRequestState by lazy<PublishSubject<RequestState<Date>>> { PublishSubject.create() }

    fun loadCurrentDateTime() = currentTimeRemoteDataSource.load()
}
