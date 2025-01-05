package com.example.composesample.di

import com.example.composesample.util.ConstValue.Companion.SSEWikiURL
import com.example.composesample.util.NetworkInterceptor
import com.example.composesample.util.NetworkStatusLiveData
import com.example.composesample.util.NetworkUtil
import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.EventSource
import org.koin.core.module.Module
import org.koin.dsl.module
import java.net.URI
import java.util.concurrent.TimeUnit

val networkModule: Module = module {
    single { NetworkUtil(get()) }
    single { NetworkInterceptor(get()) }
    single { NetworkStatusLiveData(get()) }

    single { (eventHandler: EventHandler) ->
        EventSource.Builder(
            eventHandler,
            URI.create(SSEWikiURL)
        )
            .reconnectTime(3, TimeUnit.SECONDS)
            .build()
    }
}