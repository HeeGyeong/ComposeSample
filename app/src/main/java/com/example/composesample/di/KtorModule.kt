package com.example.composesample.di

import android.util.Log
import com.example.composesample.BuildConfig
import com.example.data.api.ApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.gson.gson
import org.koin.dsl.module

// 네트워크 설정 상수 (BASE_URL은 ApiClient.NEW_BASE_URL 사용)
private object NetworkConstants {
    const val CONNECT_TIMEOUT = 60_000
    const val SOCKET_TIMEOUT = 60_000
}

val ktorModule = module {
    single {
        HttpClient(OkHttp) {

            defaultRequest {
                url(ApiClient.NEW_BASE_URL)
                header("X-Naver-Client-Id", BuildConfig.NAVER_CLIENT_ID)
            }

            /**
             * Header 설정
             *
             * DefaultRequest를 사용.
             */
            install(DefaultRequest) { // defaultRequest { ... } 와 동일한 역할
                header("X-Naver-Client-Secret", BuildConfig.NAVER_CLIENT_SECRET)
            }


            /**
             * HttpLoggingInterceptor
             *
             * logger를 사용하여 네트워크 로깅 설정
             * 디버그 모드에서는 모든 로그를, 릴리즈에서는 로그를 비활성화
             */
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorLogger", message)
                    }
                }
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
            }

            /**
             * .addConverterFactory(get<GsonConverterFactory>())
             *
             * ContentNegotiation를 사용.
             * JSON 직렬화/역직렬화를 위한 Gson 설정
             */
            install(ContentNegotiation) {
                gson()
            }

            // OkHttp Engine일 때, HTTP 클라이언트 엔진 설정
            engine {
                config {
                    connectTimeout(NetworkConstants.CONNECT_TIMEOUT.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
                    readTimeout(NetworkConstants.SOCKET_TIMEOUT.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
                }
            }
        }
    }
} 