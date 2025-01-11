package com.example.composesample.presentation.example.component.test

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader

@Composable
fun UITestExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {
        MainHeader(
            title = "SSE Example",
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

        Button(onClick = {
            clickCount.value++
            text.value = if (clickCount.value % 2 == 0) "Hello" else "Clicked!"
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
        ) {
            Text("계산하기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = result,
            modifier = Modifier.testTag("resultText")
        )
    }
}