package com.example.composesample.di

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.gson.*
import org.koin.dsl.module
import android.util.Log
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import org.koin.android.BuildConfig

// 네트워크 설정 상수
private object NetworkConstants {
    const val CONNECT_TIMEOUT = 60_000
    const val SOCKET_TIMEOUT = 60_000
    const val BASE_URL = "https://jsonplaceholder.typicode.com"
}

val ktorModule = module {
    single {
        HttpClient(Android) {

            defaultRequest {
                url(NetworkConstants.BASE_URL)
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

            /**
             * Header 설정
             *
             * DefaultRequest를 사용.
             */
            install(DefaultRequest) {
                header("X-Naver-Client-Id", "33chRuAiqlSn5hn8tIme")
                header("X-Naver-Client-Secret", "fyfwt9PCUN")
            }
            
            // HTTP 클라이언트 엔진 설정
            engine {
                connectTimeout = NetworkConstants.CONNECT_TIMEOUT
                socketTimeout = NetworkConstants.SOCKET_TIMEOUT
            }
        }
    }
} 