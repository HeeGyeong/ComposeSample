package com.example.composesample.application

import android.app.Application
import android.util.Log
import androidx.core.util.Consumer
import androidx.work.Configuration
import com.example.composesample.di.CleanArchitectureAddModules
import com.example.composesample.di.KoinModules
import com.example.composesample.presentation.example.component.system.background.workmanager.WorkerExceptionHandlerKind
import com.example.composesample.presentation.example.component.system.background.workmanager.WorkerExceptionReporter
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

/**
 * WorkManager 의 워커 예외 핸들러는 **Configuration 에만 등록할 수 있고, 앱 전역에 한 번만 등록된다.**
 * 그래서 Application 이 [Configuration.Provider] 를 구현한다.
 *
 * 이 구현을 쓰려면 매니페스트에서 기본 초기화(androidx.startup 의 `WorkManagerInitializer`)를
 * 제거해야 한다 — 그래야 첫 `WorkManager.getInstance()` 호출 때 아래 설정으로 초기화된다.
 * 핸들러 등록 외의 설정은 전부 기본값이라 기존 WorkManager 예제의 동작은 그대로다.
 */
class BaseApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BaseApplication)
            loadKoinModules(KoinModules)
            loadKoinModules(CleanArchitectureAddModules)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            // 워커 인스턴스 생성 실패(팩토리/리플렉션 단계)
            .setWorkerInitializationExceptionHandler(
                Consumer { info ->
                    WorkerExceptionReporter.record(WorkerExceptionHandlerKind.INITIALIZATION, info)
                }
            )
            // 워커 실행 중 밖으로 나간 Throwable
            .setWorkerExecutionExceptionHandler(
                Consumer { info ->
                    WorkerExceptionReporter.record(WorkerExceptionHandlerKind.EXECUTION, info)
                }
            )
            .build()
}
