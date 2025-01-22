package com.example.composesample.presentation.example.component.test

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader

/**
 * ComposeLog 로 선언된 Log는 언제 Composable 함수가 호출되고, 그려지는지 확인하기 위함.
 *
 * Layout tree 구조는 Layout Inspector를 사용하여 확인 필요.
 */
@Composable
fun UITestExampleUI(onBackEvent: () -> Unit) {
    Log.d("ComposeLog", "CREATE : UITestExampleUI is being composed")

    var previousColumnPosition by remember { mutableStateOf<Offset?>(null) }
    var previousFirstButtonPosition by remember { mutableStateOf<Offset?>(null) }
    var previousFirstNumberPosition by remember { mutableStateOf<Offset?>(null) }
    var previousSecondNumberPosition by remember { mutableStateOf<Offset?>(null) }
    var previousCalculateButtonPosition by remember { mutableStateOf<Offset?>(null) }
    var previousResultTextPosition by remember { mutableStateOf<Offset?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .onGloballyPositioned { coordinates ->
                val currentPosition = coordinates.positionInRoot()
                if (previousColumnPosition == null) {
                    previousColumnPosition = currentPosition
                } else {
                    if (previousColumnPosition != currentPosition) {
                        previousColumnPosition = currentPosition
                    } else {
                        Log.d("ComposeLog", "POSITION SET : Column positioned at: $currentPosition")
                    }
                }
            }
    ) {
        MainHeader(
            title = "UI Test Example",
            onBackIconClicked = onBackEvent
        )

        // 첫 번째 테스트 후 코드 구현
//        val text = remember { mutableStateOf("Hello") }
//
//        Button(onClick = { text.value = "Clicked!" }) {
//            Text(text.value)
//        }

        // 두 번째 테스트 후 코드 구현
        val text = remember { mutableStateOf("Hello") }
        val clickCount = remember { mutableStateOf(0) }

        // clickEvent 첫 번째 방법
        Button(
            onClick = {
                clickCount.value++
                text.value = if (clickCount.value % 2 == 0) "Hello" else "Clicked!"
            },
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    val currentPosition = coordinates.positionInRoot()
                    if (previousFirstButtonPosition == null) {
                        previousFirstButtonPosition = currentPosition
                    } else {
                        if (previousFirstButtonPosition != currentPosition) {
                            previousFirstButtonPosition = currentPosition
                        } else {
                            Log.d(
                                "ComposeLog",
                                "POSITION SET : First Button positioned at: $currentPosition"
                            )
                        }
                    }
                }
        ) {
            Text(text.value)
        }

        // clickEvent 두 번째 방법
        val onClickEvent = remember {
            {
                clickCount.value++
                text.value = if (clickCount.value % 2 == 0) "Hello" else "Clicked!"
            }
        }

        Button(onClick = onClickEvent) {
            Text(text.value)
        }

        // clickEvent 세 번째 방법
        val onParamClickEvent = remember {
            { count: Int ->
                text.value = if (count % 2 == 0) "Hello" else "Clicked!"
            }
        }

        Button(onClick = {
            clickCount.value++
            onParamClickEvent(clickCount.value)
        }) {
            Text(text.value)
        }

        // 세 번째 테스트 후 코드 구현
        var firstNumber by remember { mutableStateOf("") }
        var secondNumber by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }

        Spacer(modifier = Modifier.height(16.dp))

        // 숫자 입력 필드들
        OutlinedTextField(
            value = firstNumber,
            onValueChange = { firstNumber = it },
            label = { Text("첫 번째 숫자") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firstNumber")
                .onGloballyPositioned { coordinates ->
                    val currentPosition = coordinates.positionInRoot()
                    if (previousFirstNumberPosition == null) {
                        previousFirstNumberPosition = currentPosition
                    } else {
                        if (previousFirstNumberPosition != currentPosition) {
                            previousFirstNumberPosition = currentPosition
                        } else {
                            Log.d(
                                "ComposeLog",
                                "POSITION SET : FirstNumber TextField positioned at: $currentPosition"
                            )
                        }
                    }
                }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = secondNumber,
            onValueChange = { secondNumber = it },
            label = { Text("두 번째 숫자") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("secondNumber")
                .onGloballyPositioned { coordinates ->
                    val currentPosition = coordinates.positionInRoot()
                    if (previousSecondNumberPosition == null) {
                        previousSecondNumberPosition = currentPosition
                    } else {
                        if (previousSecondNumberPosition != currentPosition) {
                            previousSecondNumberPosition = currentPosition
                        } else {
                            Log.d(
                                "ComposeLog",
                                "POSITION SET : SecondNumber TextField positioned at: $currentPosition"
                            )
                        }
                    }
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val sum = (firstNumber.toIntOrNull() ?: 0) + (secondNumber.toIntOrNull() ?: 0)
                result = "결과: $sum"
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calculateButton")
                .onGloballyPositioned { coordinates ->
                    val currentPosition = coordinates.positionInRoot()
                    if (previousCalculateButtonPosition == null) {
                        previousCalculateButtonPosition = currentPosition
                    } else {
                        if (previousCalculateButtonPosition != currentPosition) {
                            previousCalculateButtonPosition = currentPosition
                        } else {
                            Log.d(
                                "ComposeLog",
                                "POSITION SET : Calculate Button positioned at: $currentPosition"
                            )
                        }
                    }
                }
        ) {
            Text("계산하기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = result,
            modifier = Modifier
                .testTag("resultText")
                .onGloballyPositioned { coordinates ->
                    val currentPosition = coordinates.positionInRoot()
                    if (previousResultTextPosition == null) {
                        previousResultTextPosition = currentPosition
                    } else {
                        if (previousResultTextPosition != currentPosition) {
                            previousResultTextPosition = currentPosition
                        } else {
                            Log.d(
                                "ComposeLog",
                                "POSITION SET : Result Text positioned at: $currentPosition"
                            )
                        }
                    }
                }
        )
    }
}