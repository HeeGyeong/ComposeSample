package com.example.composesample.presentation.example.component.data.sse

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

/**
 * SSE 예제의 메인 Composable 함수
 * 위키피디아의 실시간 업데이트를 SSE를 통해 수신하고 표시
 *
 * @param onBackEvent 뒤로가기 버튼 클릭 시 호출될 콜백
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SSEExampleUI(onBackEvent: () -> Unit) {
    val sseViewModel: SSEViewModel = koinViewModel()
    val uiState = sseViewModel.uiState.collectAsState().value
    val clickCount = remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {
        MainHeader(
            title = "SSE Example",
            onBackIconClicked = onBackEvent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!uiState.isConnected) {
                        clickCount.value += 1
                        sseViewModel.incrementCycleCount()
                        sseViewModel.startSSEConnection(clickCount.value.toString())
                    } else {
                        sseViewModel.closeSSEConnection()
                    }
                }
            ) {
                Text(
                    if (uiState.isConnected) {
                        "SSE 연결 종료"
                    } else {
                        "SSE 연결\n(위키피디아 실시간 업데이트 보기)"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(uiState.messageList) { message ->
                    when (message) {
                        is SSEViewModel.SSEMessage.Connected -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Gray
                        )

                        is SSEViewModel.SSEMessage.Comment -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.White
                        )

                        is SSEViewModel.SSEMessage.CollectedChars -> CollectedCharsCard(chars = message.chars)
                        is SSEViewModel.SSEMessage.Disconnected -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Cyan
                        )

                        is SSEViewModel.SSEMessage.Error -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Red
                        )
                    }
                }
            }
        }
    }
}

/**
 * 상태 메시지를 표시하는 카드 Composable
 * @param message 표시할 메시지
 * @param backgroundColor 카드의 배경색
 */
@Composable
fun StatusMessageCard(message: String, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        backgroundColor = backgroundColor
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

/**
 * 수집된 문자들을 표시하는 카드 Composable
 * @param chars 수집된 문자열
 */
@Composable
fun CollectedCharsCard(chars: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Text(
            text = "수집된 첫 글자들: $chars",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}