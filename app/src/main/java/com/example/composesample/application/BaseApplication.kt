package com.example.composesample.application

import android.app.Application
import com.example.composesample.module.apiModule
import com.example.composesample.module.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BaseApplication)

            modules(apiModule)
            modules(viewModelModule)
        }
    }
}