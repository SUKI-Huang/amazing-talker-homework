package com.amazing.application

import android.app.Application
import com.amazing.module.di.appModules
import com.amazing.module.event.SystemEventCenter
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AmazingApplication : Application() {

    companion object {
        lateinit var instance: AmazingApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDependencyInjection()
        initSystemEventCenter()
    }

    private fun initDependencyInjection() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AmazingApplication)
            modules(appModules)
        }
    }

    private fun initSystemEventCenter() {
        SystemEventCenter(this).initialize()
    }
}
