package com.amazing.module.di

import android.content.Context
import com.amazing.constant.Constant
import com.amazing.module.network.component.ApiInterfaceManager
import com.amazing.module.network.component.NetworkModule

// not implemented DI feature in this project (Reasons on Readme.md file)
// temporary put variable here
class DependencyInjection(context: Context) {
    val retrofit by lazy { NetworkModule.defaultRetrofit(context) }
    val apiInterfaceManager by lazy { ApiInterfaceManager(retrofit) }
    val constant by lazy { Constant() }
}
