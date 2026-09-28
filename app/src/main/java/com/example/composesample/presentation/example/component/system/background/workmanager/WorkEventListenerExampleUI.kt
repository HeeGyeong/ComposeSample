package com.example.composesample.presentation.example.component.system.background.workmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

/**
 * work 2.12 의 `ExecutionEventListener` / `ScheduleEventListener` 예제.
 *
 * 리스너는 앱 전역(Configuration)에만 등록할 수 있어 화면이 직접 달 수 없다 —
 * 등록은 BaseApplication, 수집은 [WorkEventRecorder], 이 화면은 구독만 한다.
 */
@Composable
fun WorkEventListenerExampleUI(onBackEvent: () -> Unit) {
    val viewModel: WorkEventListenerViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val events = viewModel.events.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "WorkManager 이벤트 리스너",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { ScenarioCard(viewModel, uiState) }
            item { TimelineCard(events, viewModel) }
            item { BlockedCard(viewModel, uiState) }
            item { ComparisonCard() }
        }
    }
}

// ==================== 1. 두 리스너 ====================

@Composable
private fun OverviewCard() {
    EventCard(title = "1. 두 리스너가 보는 축이 다르다") {
        BodyText(
            "work 2.12 는 작업 수명주기를 관찰하는 리스너 두 개를 추가했다. 둘 다 Configuration 에만 등록할 수 있고 " +
                "앱 전역에 하나씩만 존재한다 — 화면 단위로 붙였다 떼는 API 가 아니다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("리스너", "콜백", isHeader = true)
        TableRow("ScheduleEventListener", "onEnqueued · onUpdated · onUnblocked · onCancelled · onPrerequisiteFailed")
        TableRow("ExecutionEventListener", "onStarted · onStopped · onFinished · onException")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "스케줄 축은 \"큐에서 무슨 일이 있었나\", 실행 축은 \"실제로 돌았나\"를 답한다. " +
                "둘을 겹쳐 봐야 '큐에는 들어갔는데 실행이 안 된' 구간이 설명된다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "// Configuration.Provider (BaseApplication)\n" +
                "@OptIn(ExperimentalEventsApi::class)\n" +
                "Configuration.Builder()\n" +
                "    .setExecutionEventListener(DemoExecutionEventListener())\n" +
                "    .setScheduleEventListener(DemoScheduleEventListener())\n" +
                "    .build()"
        )
        CaptionText(
            "Execution 쪽 4개는 전부 abstract 라 모두 구현해야 하고, Schedule 쪽 5개는 기본 구현이 있어 " +
                "필요한 것만 재정의해도 된다(javap 로 확인). 모든 콜백이 suspend 다."
        )
    }
}

// ==================== 2. 시나리오 ====================

@Composable
private fun ScenarioCard(
    viewModel: WorkEventListenerViewModel,
    uiState: WorkEventListenerViewModel.UiState
) {
    EventCard(title = "2. 언제 어떤 콜백이 오는가") {
        BodyText("시나리오를 실행하면 아래 타임라인이 채워진다. 실행하는 동안 잠시 기다려야 한다.")
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WorkEventListenerViewModel.Scenario.entries.forEach { scenario ->
                ActionButton(
                    label = scenario.label,
                    enabled = uiState.running == null
                ) { viewModel.run(scenario) }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        WorkEventListenerViewModel.Scenario.entries.forEach {
            BulletText("${it.label} — ${it.description}")
        }

        uiState.running?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${it.label} 실행 중...",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
        }
    }
}

// ==================== 3. 타임라인 ====================

@Composable
private fun TimelineCard(
    events: List<WorkEventEntry>,
    viewModel: WorkEventListenerViewModel
) {
    EventCard(title = "3. 이벤트 타임라인") {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = "수신 ${events.size}건",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                modifier = Modifier.weight(1f)
            )
            ActionButton(label = "지우기", enabled = events.isNotEmpty(), tone = ButtonTone.NEUTRAL) {
                viewModel.clearEvents()
            }
        }

        if (events.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            CaptionText("아직 수신한 이벤트가 없다. 위에서 시나리오를 실행할 것.")
            return@EventCard
        }

        Spacer(modifier = Modifier.height(8.dp))
        events.forEach { EventRow(it) }
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "ms 는 시나리오 시작 기준 경과 시간. 스레드 이름이 함께 찍히는데, 콜백이 메인 스레드가 아니라 " +
                "WorkManager 의 실행기 스레드에서 온다는 것을 확인할 수 있다."
        )
    }
}

@Composable
private fun EventRow(entry: WorkEventEntry) {
    val color = when (entry.source) {
        WorkEventSource.SCHEDULE -> Color(0xFF6A1B9A)
        WorkEventSource.EXECUTION -> Color(0xFF1565C0)
        WorkEventSource.LEGACY_HANDLER -> Color(0xFFEF6C00)
    }

    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row {
            Text(
                text = "%5dms".format(entry.elapsedMillis),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.width(60.dp)
            )
            Text(
                text = entry.source.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.width(80.dp)
            )
            Text(
                text = entry.callback,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }
        Text(
            text = "        ${entry.workName} · ${entry.state}" +
                if (entry.detail.isNotEmpty()) " · ${entry.detail}" else "",
            fontSize = 11.sp,
            color = Color(0xFF616161)
        )
        Text(
            text = "        thread=${entry.threadName}",
            fontSize = 10.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}

// ==================== 4. 제약 대기 ====================

@Composable
private fun BlockedCard(
    viewModel: WorkEventListenerViewModel,
    uiState: WorkEventListenerViewModel.UiState
) {
    EventCard(title = "4. 큐에는 있는데 실행되지 않는 상태") {
        BodyText(
            "충족되지 않는 제약(충전 중)을 단 작업을 넣으면 ENQUEUED 에서 멈춘다. " +
                "스케줄 축에는 onEnqueued 가 찍히지만 실행 축은 아무것도 주지 않는다 — " +
                "실행 리스너만 보고 있으면 \"작업이 사라진\" 것처럼 보이는 구간이다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(label = "제약 걸린 작업 넣기", enabled = !uiState.blockedQueued) {
                viewModel.enqueueBlocked()
            }
            ActionButton(
                label = "취소",
                enabled = uiState.blockedQueued,
                tone = ButtonTone.NEUTRAL
            ) { viewModel.cancelBlocked() }
        }
        CaptionText("취소하면 onCancelled 가 온다. 충전기를 꽂으면 제약이 풀려 실행되는 것도 볼 수 있다.")
    }
}

// ==================== 5. 2.11 핸들러와 대조 ====================

@Composable
private fun ComparisonCard() {
    EventCard(title = "5. 2.11 의 Consumer 예외 핸들러와 무엇이 다른가") {
        BodyText(
            "예외를 받는 경로는 2.11 에도 있었다(setWorkerExecutionExceptionHandler). " +
                "④ 시나리오를 실행하면 같은 실패가 두 경로로 들어오는 것을 타임라인에서 나란히 볼 수 있다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("축", "2.11 Consumer / 2.12 onException", isHeader = true)
        TableRow("호출 모델", "Consumer.accept(동기) / suspend (WorkManager 가 완료를 기다린다)")
        TableRow("전달 정보", "WorkerExceptionInfo(워커 클래스·params) / Throwable + WorkInfo(상태·태그)")
        TableRow("범위", "예외 전용 / 수명주기 전체의 한 콜백")
        TableRow("호출 순서", "Consumer 가 먼저, onException 이 나중 (실기기 실측: 637ms → 719ms)")
        Spacer(modifier = Modifier.height(8.dp))
        BulletText(
            "suspend 라서 리스너 안에서 곧바로 중단 함수를 호출할 수 있다 — 로그 전송 같은 작업을 " +
                "별도 스코프로 던지지 않아도 된다. 대신 리스너가 느리면 그만큼 붙잡는다."
        )
        BulletText(
            "2.11 핸들러는 2.12 에서도 유지되고 deprecated 도 아니다(javap 확인). 둘 중 하나를 고르는 관계가 아니라 " +
                "예외만 필요하면 Consumer, 수명주기 전체가 필요하면 리스너다."
        )
        CaptionText("참고 자료와 개념 정리는 같은 폴더 exampleGuide.kt 참고.")
    }
}

// ==================== 공통 UI ====================

private enum class ButtonTone { PRIMARY, NEUTRAL }

@Composable
private fun ActionButton(
    label: String,
    enabled: Boolean,
    tone: ButtonTone = ButtonTone.PRIMARY,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (tone == ButtonTone.PRIMARY) Color(0xFF37474F) else Color(0xFF90A4AE),
            disabledContainerColor = Color(0xFFCFD8DC)
        )
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun EventCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

@Composable
private fun BulletText(text: String) {
    Text(
        text = "· $text",
        fontSize = 12.sp,
        color = Color(0xFF616161),
        lineHeight = 18.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun TableRow(first: String, second: String, isHeader: Boolean = false) {
    val weight = if (isHeader) FontWeight.Bold else FontWeight.Normal
    val color = if (isHeader) Color(0xFF212121) else Color(0xFF424242)

    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = first,
            fontSize = 11.sp,
            fontWeight = weight,
            fontFamily = FontFamily.Monospace,
            color = color,
            modifier = Modifier.width(120.dp)
        )
        Text(text = second, fontSize = 11.sp, fontWeight = weight, color = color, lineHeight = 16.sp)
    }
}
