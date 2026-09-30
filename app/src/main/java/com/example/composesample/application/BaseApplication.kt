package com.example.composesample.application

import android.app.Application
import android.util.Log
import androidx.core.util.Consumer
import androidx.work.Configuration
import androidx.work.ExperimentalEventsApi
import com.example.composesample.di.CleanArchitectureAddModules
import com.example.composesample.di.KoinModules
import com.example.composesample.presentation.example.component.system.background.workmanager.DemoExecutionEventListener
import com.example.composesample.presentation.example.component.system.background.workmanager.DemoScheduleEventListener
import com.example.composesample.presentation.example.component.system.background.workmanager.WorkEventRecorder
import com.example.composesample.presentation.example.component.system.background.workmanager.WorkerExceptionHandlerKind
import com.example.composesample.presentation.example.component.system.background.workmanager.WorkerExceptionReporter
import com.example.composesample.util.isMainProcess
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

        // Application.onCreate 는 프로세스마다 실행된다 — android:process 로 분리한 서비스
        // (MultiProcessExample 의 :remote · :isolated)가 뜰 때도 이 코드가 다시 돈다.
        // Koin 그래프는 화면이 있는 기본 프로세스에서만 쓰므로 그 밖의 프로세스에서는 만들지 않는다.
        if (!isMainProcess()) return

        startKoin {
            androidContext(this@BaseApplication)
            loadKoinModules(KoinModules)
            loadKoinModules(CleanArchitectureAddModules)
        }
    }

    // setExecutionEventListener/setScheduleEventListener 가 @ExperimentalEventsApi(level=WARNING) 라 opt-in 한다.
    // 향후 버전에서 시그니처가 바뀔 수 있다는 표시이며, 붙이지 않으면 경고만 나고 컴파일은 된다.
    @OptIn(ExperimentalEventsApi::class)
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
                    // 같은 실패를 2.12 의 onException 과 나란히 보여주기 위해 이벤트 타임라인에도 남긴다
                    WorkEventRecorder.recordLegacy(
                        workName = info.workerClassName.substringAfterLast('.'),
                        throwableName = info.throwable.javaClass.simpleName
                    )
                }
            )
            // work 2.12 신규 — 작업 수명주기를 suspend 리스너로 관찰한다(WorkEventListener 예제).
            // 리스너는 Configuration 에만 등록할 수 있고 앱 전역에 하나뿐이라 화면이 직접 달 수 없다.
            .setExecutionEventListener(DemoExecutionEventListener())
            .setScheduleEventListener(DemoScheduleEventListener())
            .build()
}
