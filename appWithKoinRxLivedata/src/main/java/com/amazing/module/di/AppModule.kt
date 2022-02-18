package com.amazing.module.di

import com.amazing.application.AmazingApplication
import com.amazing.constant.Constant
import com.amazing.module.network.component.ApiInterfaceManager
import com.amazing.module.network.component.NetworkModule
import com.amazing.mvvm.model.repository.schedule.TeacherScheduleRepository
import com.amazing.mvvm.model.repository.time.CurrentTimeRepository
import org.koin.dsl.module

private val application = (AmazingApplication.instance)

val globalModules = module {
    single { Constant() }
    single { NetworkModule.defaultRetrofit(application) }
    single { ApiInterfaceManager(get()) }
}

val repositoryModules = module {
    factory { CurrentTimeRepository() }
    factory { TeacherScheduleRepository() }
}

val appModules = listOf(globalModules, repositoryModules)
