package com.amazing.application

import android.app.Application
import com.amazing.module.di.DependencyInjection
import com.amazing.module.event.SystemEventCenter

class AmazingApplication : Application() {

    companion object {
        lateinit var instance: AmazingApplication
        lateinit var dependencyInjection: DependencyInjection
    }

    override fun onCreate() {
        super.onCreate()
        init()
        initSystemEventCenter()
    }

    private fun init() {
        instance = this
        dependencyInjection = DependencyInjection(this)
    }

    private fun initSystemEventCenter() {
        SystemEventCenter(this).initialize()
    }
}
