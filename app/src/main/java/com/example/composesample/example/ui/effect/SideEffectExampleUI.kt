package com.example.composesample.example.ui.effect

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SideEffectExampleUI(
    onBackButtonClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // TextField Data 변경 시 마다 해당 데이터 변경하도록 설정.
    val isChanged = remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }
        }

        item {
            LaunchedEffect(key1 = Unit, block = {
                Log.d("SideEffectLog", "LaunchedEffect Unit - 생성 시 최초 한번 호출.")
            })

            LaunchedEffect(key1 = isChanged.value, block = {
                Log.d("SideEffectLog", "LaunchedEffect isChanged.value - isChanged.value 변경 시 마다 호출.")
            })

            DisposableEffect(key1 = Unit, effect = {
                Log.d("SideEffectLog", "DisposableEffect Unit - 생성 시 최초 한번 호출.")

                onDispose {
                    Log.d("SideEffectLog", "DisposableEffect onDispose - 제거 시 최초 한번 호출.")
                }
            })

            DisposableEffect(key1 = isChanged.value, effect = {
                Log.d("SideEffectLog", "DisposableEffect isChanged.value - isChanged.value[${isChanged.value}] 변경 시 마다 호출.")

                onDispose {
                    Log.d("SideEffectLog", "DisposableEffect isChanged.value onDispose - isChanged.value[${isChanged.value}] 변경 시 마다 호출.")
                }
            })

            // recomposable이 발생하지 않으면, 해당 부분도 최초 1회 호출하고 호출되지 않음.
            SideEffect {
                Log.d("SideEffectLog", "SideEffect - reComposable이 성공적으로 완료될 때 마다 호출")
            }

            // remember 관련 sideEffect Example
            UpdateRememberExample(
                isChanged = isChanged
            )

            // produceState
            var isTimer by remember { mutableStateOf(false) }

            val timer by produceState(initialValue = 0, key1 = isTimer) {
                // 해당 블럭에서 Flow 등의 작업을 처리 후 initialValue에서 설정한 타입의 데이터의 값을 갱신한다.
                var job: Job? = null
                Log.d("SideEffectLog", "produceState .. $value") // value : Int Type.
                if (isTimer) {
                    job = coroutineScope.launch {
                        while (true) {
                            delay(1000)
                            value++
                        }
                    }
                }

                // 종료 시 호출
                awaitDispose {
                    // key 값이 변경될 때 마다 이전에 호출된 produceState가 있으면 종료 후 시작한다.
                    Log.d("SideEffectLog", "produceState awaitDispose .. $value")
                    job?.cancel()
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Time $timer")
                Button(onClick = {
                    isTimer = !isTimer
                }) {
                    Text(if (isTimer) "Stop" else "Start")
                }
            }

            // derivedStateOf Example
            val derivedStateCheckDataBy by remember(timer) {
                derivedStateOf { timer > 0 && timer % 2 == 0 }
            }

            val derivedStateCheckData = remember(timer) {
                derivedStateOf { timer > 0 && timer % 2 != 0 }
            }

            Text(text = "derivedStateCheckData : $derivedStateCheckDataBy")
            Text(text = "derivedStateCheckData2 : ${derivedStateCheckData.value}")

            // snapshotFlow Example
            LaunchedEffect(key1 = isChanged, block = {
                snapshotFlow { isChanged.value } // isChanged 중 isChange의 Value가 변경되었을 때 호출.
                    .map { timer > 0 && timer % 2 != 0 } // 호출 조건
                    .distinctUntilChanged() // 중복 제거
                    .collect {
                        Log.d("SideEffectLog", "snapshotFlow $it")
                    }
            })
        }
    }
}

@Composable
fun UpdateRememberExample(
    isChanged: MutableState<Boolean>
) {
    var textState by remember { mutableStateOf("Default") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = textState,
            onValueChange = { change ->
                textState = change
                isChanged.value = isChanged.value.not()
            }
        )

        Text("textState : $textState")

        Spacer(modifier = Modifier.height(20.dp))

        Divider(
            thickness = 5.dp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        RememberUpdateTestText(textState)
    }
}

@Composable
fun RememberUpdateTestText(text: String) {
    val rememberTextBy by remember { mutableStateOf(text) }
    val rememberTextByApply by remember { mutableStateOf(text) }
        .apply {
            value = text
        }
    val rememberText = remember { mutableStateOf(text) }

    // rememberUpdatedState를 확인 해보면 rememberTextByApply 와 동일한 것을 알 수 있다.
    val rememberUpdatedText by rememberUpdatedState(text)

    Text("rememberTextBy : $rememberTextBy")
    Text("rememberText : ${rememberText.value}")
    Text("rememberUpdatedText : $rememberUpdatedText")
    Text("rememberTextByApply : $rememberTextByApply")
}