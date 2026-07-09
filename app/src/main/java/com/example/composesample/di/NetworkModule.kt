package com.example.composesample.di

import com.example.composesample.util.NetworkInterceptor
import com.example.composesample.util.NetworkStatusLiveData
import com.example.composesample.util.NetworkUtil
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule: Module = module {
    single { NetworkUtil(get()) }
    single { NetworkInterceptor(get()) }
    single { NetworkStatusLiveData(get()) }
}