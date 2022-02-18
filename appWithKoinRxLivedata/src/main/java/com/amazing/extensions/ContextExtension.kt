package com.amazing.extensions

import android.content.Context
import android.content.pm.ApplicationInfo

// https://stackoverflow.com/questions/7085644/how-to-check-if-apk-is-signed-or-debug-build
// after assemble to aar BuildConfig.Debug will always false, so use the code down below and replace BuildConfig.Debug
// so if your are making a library project do not use `BuildConfig.Debug`
val Context.isDebugMode: Boolean
    get() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
