package com.example.composesample.presentation.example.component.data.sse

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

/**
 * SSE 예제의 메인 Composable 함수
 * 위키피디아의 실시간 업데이트를 SSE를 통해 수신하고 표시
 *
 * 같은 스트림을 두 구현으로 받을 수 있다 — okhttp-eventsource(콜백)와 Ktor SSE(Flow).
 * 구현마다 첫 이벤트 필드·스레드 수·종료 방식을 재서 비교 카드에 남긴다. 참고 자료는 같은 폴더 exampleGuide.kt.
 *
 * @param onBackEvent 뒤로가기 버튼 클릭 시 호출될 콜백
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SSEExampleUI(onBackEvent: () -> Unit) {
    val sseViewModel: SSEViewModel = koinViewModel()
    val uiState = sseViewModel.uiState.collectAsStateWithLifecycle().value
    val selectedImpl by sseViewModel.selectedImpl.collectAsStateWithLifecycle()
    val stats by sseViewModel.stats.collectAsStateWithLifecycle()
    val clickCount = remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {
        MainHeader(
            title = "SSE Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ImplSelector(
                    selected = selectedImpl,
                    enabled = !uiState.isConnected,
                    onSelect = sseViewModel::selectImpl
                )
            }

            item {
                Button(
                    onClick = {
                        if (!uiState.isConnected) {
                            clickCount.intValue += 1
                            sseViewModel.incrementCycleCount()
                            when (selectedImpl) {
                                SSEViewModel.SSEImpl.EVENTSOURCE ->
                                    sseViewModel.startSSEConnection(clickCount.intValue.toString())
                                SSEViewModel.SSEImpl.KTOR ->
                                    sseViewModel.startKtorConnection(clickCount.intValue.toString())
                            }
                        } else {
                            sseViewModel.closeSSEConnection()
                        }
                    }
                ) {
                    Text(
                        if (uiState.isConnected) {
                            "SSE 연결 종료"
                        } else {
                            "SSE 연결 — ${selectedImpl.label}\n(위키피디아 실시간 업데이트 보기)"
                        },
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                ComparisonCard(stats = stats, ktorDefaults = sseViewModel.ktorDefaults)
                Spacer(modifier = Modifier.height(12.dp))
            }

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

/** 구현 선택 — 연결 중에는 바꿀 수 없다 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImplSelector(
    selected: SSEViewModel.SSEImpl,
    enabled: Boolean,
    onSelect: (SSEViewModel.SSEImpl) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SSEViewModel.SSEImpl.entries.forEach { impl ->
            FilterChip(
                selected = selected == impl,
                onClick = { onSelect(impl) },
                enabled = enabled,
                label = { Text(impl.label) }
            )
        }
    }
}

/**
 * 두 구현을 같은 스트림에 붙여 잰 값. 스레드 수는 이름으로 묶었다(연결 전 → 3번째 이벤트 때 → 종료 1초 뒤).
 * 첫 이벤트까지의 시간은 네트워크 상태에 따라 달라지는 참고값이다.
 */
@Composable
private fun ComparisonCard(stats: Map<SSEViewModel.SSEImpl, SSEViewModel.RunStats>, ktorDefaults: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("콜백 vs Flow — 같은 스트림, 두 구현", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            MonoText(
                "okhttp-eventsource: BackgroundEventHandler.onMessage(...) 콜백 → 끝낼 때 close()\n" +
                    "Ktor: client.sse(url) { incoming.take(10).collect { ... } } → 블록이 끝나거나 Job 취소로 종료"
            )
            Spacer(modifier = Modifier.height(8.dp))
            SSEViewModel.SSEImpl.entries.forEach { impl ->
                val run = stats[impl]
                Text(impl.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (run == null) {
                    MonoText("아직 연결하지 않았다")
                } else {
                    MonoText("수신 ${run.events}개 · 첫 이벤트까지 ${run.firstEventMs?.let { "${it}ms" } ?: "-"}")
                    MonoText("첫 이벤트: ${run.firstEventFields ?: "-"}")
                    MonoText("스레드 연결 전: ${run.threadsBefore ?: "-"}")
                    MonoText("스레드 수신 중: ${run.threadsDuring ?: "-"}")
                    MonoText("스레드 종료 1초 뒤: ${run.threadsAfter ?: "-"}")
                    MonoText("종료: ${run.endedBy ?: "-"} · 종료 요청 뒤 도착한 이벤트 ${run.afterStopEvents}개")
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text("재연결 설정", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            MonoText("okhttp-eventsource: RetryDelayStrategy.defaultStrategy().initialDelay(3초) — 끊기면 다시 붙는다")
            MonoText("Ktor 기본값: $ktorDefaults")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "콜백 쪽은 종료를 정한 뒤에도 close() 가 끝날 때까지 받은 이벤트가 계속 오므로 앱이 직접 걸러야 한다" +
                    "(실측 0~15개, 실행마다 다름). Ktor 는 take·Job 취소로 끝나 그 뒤 처리가 0개다. 화면을 나가면 Ktor 는 " +
                    "viewModelScope 취소로 저절로 멈추고, eventsource 는 onCleared() 에서 close() 해야 멈춘다. " +
                    "Ktor 는 기본으로 재연결하지 않는다(maxReconnectionAttempts=0).",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
private fun MonoText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 1.dp)
    )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = "수집된 첫 글자들: $chars",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}