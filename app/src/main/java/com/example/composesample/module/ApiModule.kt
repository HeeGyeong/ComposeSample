package com.example.composesample.module

import android.util.Log
import com.example.composesample.api.ApiClient
import com.example.composesample.api.ApiInterface
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.BuildConfig
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val apiModule: Module = module {

    single<ApiInterface> { get<Retrofit>().create(ApiInterface::class.java) }

    // Retrofit setting
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(ApiClient.BASE_URL)
            .client(get())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(get<GsonConverterFactory>())
            .build()
    }

    single<GsonConverterFactory> { GsonConverterFactory.create() }

    single {
        OkHttpClient.Builder()
            .run {
                addInterceptor(get<Interceptor>()) // 하단에 선언한 Intercepter 를 주입

                connectTimeout(60, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
                addInterceptor(get<HttpLoggingInterceptor>())
                build()
            }
    }

    single {
        Interceptor { chain ->
            with(chain) {
                val newRequest = request().newBuilder()
                    .addHeader("X-Naver-Client-Id", "33chRuAiqlSn5hn8tIme")
                    .addHeader("X-Naver-Client-Secret", "fyfwt9PCUN")
                    .build()
                proceed(newRequest)
            }
        }
    }

    single {
        HttpLoggingInterceptor { message ->
            Log.d("ApiLogger", "$message ")
        }
            .let {
                if (BuildConfig.DEBUG) {
                    it.setLevel(HttpLoggingInterceptor.Level.BODY)
                } else {
                    it.setLevel(HttpLoggingInterceptor.Level.NONE)
                }
            }
    }
}