package com.amazing.mvvm.model.repository.time

import com.amazing.module.network.component.RequestState
import com.amazing.module.network.component.callOnError
import com.amazing.module.network.component.callOnStart
import com.amazing.module.network.component.callOnSuccess
import com.amazing.mvvm.model.dataSource.time.CurrentTimeRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class CurrentTimeRepository {

    private val currentTimeRemoteDataSource by lazy {
        CurrentTimeRemoteDataSource().apply {
            setOnStartListener { currentTimeRequestState.callOnStart() }
            setOnSuccessListener { currentTimeRequestState.callOnSuccess(it) }
            setOnErrorListener { currentTimeRequestState.callOnError(it) }
        }
    }

    val currentTimeRequestState by lazy { MutableStateFlow<RequestState<Date>?>(null) }

    fun loadCurrentTime() = currentTimeRemoteDataSource.load()
}
