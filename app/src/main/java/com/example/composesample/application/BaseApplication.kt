package com.example.composesample.application

import android.app.Application
import com.example.composesample.di.CleanArchitectureAddModules
import com.example.composesample.di.KoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BaseApplication)
            loadKoinModules(KoinModules)
            loadKoinModules(CleanArchitectureAddModules)
        }
    }
}