package com.example.composesample.presentation.example.component.coroutine

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

@Composable
fun CoroutineExampleUI(onBackEvent: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {
        MainHeader(
            title = "Coroutine Example",
            onBackIconClicked = onBackEvent
        )

        PrintCoroutineOrder(
            coroutineScope = coroutineScope
        )

        LightWeightCoroutine()

        AsyncSuspendButton(
            coroutineScope = coroutineScope
        )

        StructuredConcurrencyButton(
            coroutineScope = coroutineScope
        )

        CancellationButton()

        ContextSwitchingButton(
            coroutineScope = coroutineScope
        )

        ExceptionHandlingButton(
            coroutineScope = coroutineScope
        )

        TimeoutButton(
            coroutineScope = coroutineScope
        )

        LaunchVsAsyncButton(
            coroutineScope = coroutineScope
        )

        CoroutineContextButton(
            coroutineScope = coroutineScope
        )

        SupervisorJobButton(
            coroutineScope = coroutineScope
        )
    }
}

/**
 * coroutine Scope 실행 순서 체크.
 *
 * 별다른 작업을 하지 않으면 launch 시 Dispacther.Default에서 실행.
 * Default는 CPU 집약적인 작업에 최적화된 스레드 풀을 사용해서, 일반적으로 빠르게 실행될 수 있음.
 *
 * 하지만, 비동기적인 작업이기 때문에 in -> out 순서대로 출력을 보장할 수 없고,
 * 오히려 out -> in 순서로 출력 될 확률이 매우 높음.
 *
 * 1L 이라도 delay가 있다면 in -> out 일 확률이 비약적으로 높아짐.
 */
@Composable
fun PrintCoroutineOrder(
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        coroutineScope.launch {
            Log.d("CoroutineExample", "Coroutine Scope in Print")
        }

        Log.d("CoroutineExample", "Coroutine Scope out Test")
    }) {
        Text("Test Print")
    }
}

/**
 * 코루틴은 경량이라는 것을 확인
 *
 * launch로 별개의 코루틴 생성 후, log를 출력하도록 설정.
 * 1만개의 코루틴을 생성하는데 cpu의 25%가 소비됨.
 *
 * runBlocking이기 때문에 default Dispachter는 Main이 되어 MainThread만 사용하게 된다.
 * 따라서, Default로 지정해주고 사용하면 1만개의 코루틴을 생성한다.
 *
 * 단, 생성하고 로그를 찍는 작업만 진행하므로, 1만개를 생성하면서 이전에 작업이 끝난 thread를 다시 사용하기 때문에
 * thread의 이름와 id가 동일한 것들이 계속하여 사용된다.
 */
@Composable
fun LightWeightCoroutine() {
    val createCount = remember { mutableStateOf(0) }
    Button(onClick = {
        runBlocking {
            repeat(10000) {
                launch(Dispatchers.Default) {
                    delay(1000L)
                    Log.d(
                        "CoroutineExample",
                        "count : ${createCount.value++} : ${Thread.currentThread().name}[${Thread.currentThread().id}]"
                    )
                }
            }
        }
    }) {
        Text("Test Lightweight")
    }
}

/**
 * 비동기 함수 호출 및 suspend 함수의 동작 확인
 *
 * fetchData()를 호출하여 비동기적으로 데이터를 가져오고, 로그로 출력.
 * runBlocking을 사용하여 메인 스레드에서 블로킹 방식으로 실행하는 경우와, Dispacther.Default인 경우를 비교
 */
@Composable
fun AsyncSuspendButton(
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        Log.d("CoroutineExample", "AsyncSuspendButton runBlocking Call")
        runBlocking {
            val data = fetchData()
            Log.d("CoroutineExample", data)
        }
        Log.d("CoroutineExample", "AsyncSuspendButton runBlocking Fin")

        Log.d("CoroutineExample", "AsyncSuspendButton coroutineScope Call")
        coroutineScope.launch {
            val data = fetchData()
            Log.d("CoroutineExample", data)
        }
        Log.d("CoroutineExample", "AsyncSuspendButton coroutineScope Fin")
    }) {
        Text("Test Async Suspend")
    }
}

/**
 * 구조적 동시성의 동작 확인
 *
 * 부모 코루틴이 자식 코루틴을 포함하고, 부모가 취소되면 자식도 취소됨을 확인.
 * 500ms 후 부모 코루틴을 취소하여 자식 코루틴의 로그가 출력되지 않음을 확인.
 */
@Composable
fun StructuredConcurrencyButton(
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        coroutineScope.launch {
            val job = launch {
                launch {
                    delay(1000L)
                    Log.d("CoroutineExample", "Child coroutine")
                }
            }
            delay(500L)
            job.cancel()
            Log.d("CoroutineExample", "Parent coroutine cancelled")
        }
    }) {
        Text("Test Structured Concurrency")
    }

    Button(onClick = {
        coroutineScope.launch {
            val job = launch {
                delay(1000L)
                Log.d("CoroutineExample", "first coroutine")
            }

            launch {
                delay(1000L)
                Log.d("CoroutineExample", "seond coroutine")
            }
            delay(500L)
            job.cancel()
            Log.d("CoroutineExample", "first coroutine cancelled")
        }
    }) {
        Text("Test Structured Concurrency2")
    }
}

/**
 * 코루틴의 취소 동작 확인
 *
 * 반복 작업 중 isActive를 체크하여 코루틴이 취소되었는지 확인.
 *
 * 1300ms 후 코루틴을 취소하고, 취소된 후 로그가 출력되지 않음을 확인.
 */
@Composable
fun CancellationButton() {
    Button(onClick = {
        runBlocking {
            val job = launch {
                repeat(1000) { i ->
                    if (!isActive) return@launch
                    Log.d("CoroutineExample", "Job: $i")
                    delay(500L)
                }
            }
            delay(1300L)
            job.cancelAndJoin()
            Log.d("CoroutineExample", "Job cancelled")
        }
    }) {
        Text("Test Cancellation")
    }
}

/**
 * 코루틴의 컨텍스트 전환 확인
 *
 * 각기 다른 Dispatcher를 사용하여 코루틴을 실행하고, 스레드 이름을 로그로 출력.
 * Default, IO, Main Dispatcher의 차이를 확인.
 */
@Composable
fun ContextSwitchingButton(
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        coroutineScope.launch {
            launch(Dispatchers.Default) {
                Log.d("CoroutineExample", "Default dispatcher: ${Thread.currentThread().name}")
            }
            launch(Dispatchers.IO) {
                Log.d("CoroutineExample", "IO dispatcher: ${Thread.currentThread().name}")
            }
            launch(Dispatchers.Main) {
                Log.d("CoroutineExample", "Main dispatcher: ${Thread.currentThread().name}")
            }
        }
    }) {
        Text("Test Context Switching")
    }
}

/**
 * 코루틴 Exception 처리
 *
 * try - catch 를 통해 Exception을 처리하고, 로그로 출력.
 */
@Composable
fun ExceptionHandlingButton(
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        coroutineScope.launch {
            try {
                throw Exception("Test Exception")
            } catch (e: Exception) {
                Log.d("CoroutineExample", "Caught exception: ${e.message}")
            }
        }
    }) {
        Text("Test Exception Handling")
    }
}

/**
 * 코루틴의 Timeout 동작 확인
 *
 * withTimeout을 사용하여 500ms 후에 TimeoutException을 발생시키고, 로그로 출력.
 */
@Composable
fun TimeoutButton(
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        coroutineScope.launch {
            try {
                withTimeout(500L) {
                    repeat(1000) { i ->
                        Log.d("CoroutineExample", "Timeout Job: $i")
                        delay(100L)
                    }
                }
            } catch (e: Exception) {
                Log.d("CoroutineExample", "Timeout occurred: ${e.message}")
            }
        }
    }) {
        Text("Test Timeout")
    }
}

/**
 * launch와 async의 차이를 보여주는 코루틴 예제
 *
 * 출력 순서를 통해 비동기 통신 및 await()의 차이를 확인.
 */
@Composable
fun LaunchVsAsyncButton(
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        coroutineScope.launch {
            // launch는 결과를 반환하지 않습니다.
            val launchResult = launch {
                Log.d("CoroutineExample", "Launch result?")
                "Launch result"
            }

            Log.d("CoroutineExample", "Launch: $launchResult")

            // async는 결과를 반환합니다.
            val result = async {
                Log.d("CoroutineExample", "Async result?")
                "Async Result"
            }.await()

            Log.d("CoroutineExample", "Async: $result")
        }
    }) {
        Text("Test Launch vs Async")
    }
}

/**
 * CoroutineName을 사용하여 코루틴 이름을 지정하고, 로그로 출력.
 *
 * 코루틴 이름을 기반으로 job을 저장하고, 버튼을 통해 특정 job을 취소하도록 설정
 *
 * coroutineContext를 사용하여 특정 이름의 coroutine을 확인할 수 있음.
 */
@Composable
fun CoroutineContextButton(
    coroutineScope: CoroutineScope
) {
    val jobMap = remember { mutableMapOf<String, Job>() }

    Button(onClick = {
        val job = coroutineScope.launch(CoroutineName("ExampleCoroutine")) {
            try {
                Log.d("CoroutineExample", "Running in Coroutine: ${coroutineContext[CoroutineName]}")

                if (coroutineContext[CoroutineName]?.name == "ExampleCoroutine") {
                    Log.d("CoroutineExample", "CoroutineName : ExampleCoroutine !")
                }
                // 일부러 딜레이 추가
                delay(5000)
            } finally {
                Log.d("CoroutineExample", "ExampleCoroutine is cancelled")
            }
        }
        jobMap["ExampleCoroutine"] = job
    }) {
        Text("Start CoroutineName 1")
    }

    Button(onClick = {
        jobMap["ExampleCoroutine"]?.cancel()
    }) {
        Text("Cancel CoroutineName 1")
    }

    Button(onClick = {
        val job = coroutineScope.launch(CoroutineName("CoroutineSample")) {
            try {
                Log.d("CoroutineExample", "Running in Coroutine: ${coroutineContext[CoroutineName]}")
                // 일부러 딜레이 추가
                delay(5000)
            } finally {
                Log.d("CoroutineExample", "CoroutineSample is cancelled")
            }
        }
        jobMap["CoroutineSample"] = job
    }) {
        Text("Start CoroutineName 2")
    }

    Button(onClick = {
        jobMap["CoroutineSample"]?.cancel()
    }) {
        Text("Cancel CoroutineName 2")
    }
}

/**
 * SupervisorJob를 사용하여 코루틴의 실패를 전파하지 않도록 설정
 *
 * coroutineContext에 SupervisorJob을 추가하여 새로운 코루틴 스코프를 생성하고,
 * 이 스코프는 SupervisorJob의 특성을 상속받아 자식 코루틴의 실패가 부모에게 전파되지 않도록 함.
 */
@Composable
fun SupervisorJobButton(
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        coroutineScope.launch {
            val supervisor = SupervisorJob()
            val scope = CoroutineScope(coroutineContext + supervisor)

            scope.launch {
                try {
                    Log.d("CoroutineExample", "Child coroutine 1 started")
                    delay(1000L)
                    throw Exception("Child coroutine 1 failed")
                } catch (e: Exception) {
                    Log.d("CoroutineExample", "Caught exception in child 1: ${e.message}")
                }
            }

            scope.launch {
                try {
                    Log.d("CoroutineExample", "Child coroutine 2 started")
                    delay(2000L)
                    Log.d("CoroutineExample", "Child coroutine 2 completed")
                } catch (e: Exception) {
                    Log.d("CoroutineExample", "Caught exception in child 2: ${e.message}")
                }
            }

            delay(3000L)
            Log.d("CoroutineExample", "Parent coroutine completed")
        }
    }) {
        Text("Test SupervisorJob")
    }
}

suspend fun fetchData(): String {
    Log.d("CoroutineExample", "fetchData Call")
    delay(1000L)
    Log.d("CoroutineExample", "fetchData after delay")
    return "Data"
}
