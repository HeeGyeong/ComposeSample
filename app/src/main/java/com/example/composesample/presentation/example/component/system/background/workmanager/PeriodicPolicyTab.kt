package com.example.composesample.presentation.example.component.system.background.workmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * WorkManager 예제의 '주기 작업 재등록' 탭.
 *
 * 앱 업데이트에서 주기 작업의 사양(15분 → 30분)을 바꿔 같은 이름으로 다시 등록할 때, 정책에 따라
 * 기존 사용자가 새 사양을 받는지·즉시 한 번 더 도는지·실행 중인 작업이 끊기는지를 세 정책 나란히 실측한다.
 */
@Composable
internal fun PeriodicPolicyTab() {
    val viewModel: PeriodicPolicyViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val runs = viewModel.runs.collectAsStateWithLifecycle().value

    // 데모 주기 작업은 15·30분마다 실제로 다시 돈다 — 탭을 벗어나면(다른 탭·뒤로가기) 바로 취소한다
    DisposableEffect(Unit) {
        onDispose { viewModel.onScreenLeft() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PolicyOverviewCard() }
        item { ScenarioCard(viewModel, uiState) }
        item { ResultCard(uiState, runs) }
        item { ReadingCard() }
        item { CodeCard() }
    }
}

// ==================== 1. 정책 ====================

@Composable
private fun PolicyOverviewCard() {
    PolicyCard(title = "1. 같은 이름으로 다시 등록하면") {
        BodyText(
            "주기 작업은 보통 앱이 켜질 때 enqueueUniquePeriodicWork 로 등록한다. 앱 업데이트에서 주기를 15분 → 30분으로 " +
                "바꿨다면, 이미 15분 작업이 등록된 기존 사용자에게는 그 줄이 '같은 이름의 재등록'이 된다. " +
                "그때 무엇을 할지가 ExistingPeriodicWorkPolicy 다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        PolicyLane.entries.forEach { lane ->
            LabeledText(lane.label, lane.policy.whenExisting, laneColor(lane))
        }
        LabeledText("REPLACE", "deprecated — 동작은 CANCEL_AND_REENQUEUE 와 같다", Color(0xFF757575))
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "기존 작업이 없거나 CANCELLED 면 세 정책 모두 새로 등록한다. 위 설명은 work-runtime 2.12.0 소스" +
                "(ExistingPeriodicWorkPolicy · WorkerUpdater · EnqueueRunnable) 기준이고, 아래 시나리오가 기기에서 그대로 확인한다."
        )
    }
}

// ==================== 2. 시나리오 ====================

@Composable
private fun ScenarioCard(
    viewModel: PeriodicPolicyViewModel,
    uiState: PeriodicPolicyViewModel.UiState
) {
    PolicyCard(title = "2. 시나리오 — 세 정책을 동시에 실행") {
        BodyText(
            "정책마다 다른 고유 이름을 쓰고, 등록 순서·사양은 똑같이 준다. 워커는 0.4초 기다렸다 끝나며 " +
                "실행할 때마다 받은 사양(v1/v2)과 세대(generation)를 기록한다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodicPolicyViewModel.Scenario.entries.forEach { scenario ->
                ActionButton(label = scenario.label, enabled = uiState.running == null) { viewModel.run(scenario) }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        PeriodicPolicyViewModel.Scenario.entries.forEach { BulletText("${it.label} — ${it.description}") }

        uiState.running?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${it.label} 실행 중 · ${uiState.step}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText("데모 주기 작업은 진짜로 15·30분마다 다시 돈다 — 이 탭을 벗어나면 모두 취소한다.")
    }
}

// ==================== 3. 결과 ====================

@Composable
private fun ResultCard(uiState: PeriodicPolicyViewModel.UiState, runs: List<RunRecord>) {
    val title = uiState.shown?.let { "3. 결과 — ${it.label}" } ?: "3. 결과"
    PolicyCard(title = title) {
        if (uiState.snapshots.isEmpty()) {
            CaptionText("위에서 시나리오를 실행하면 정책별 결과가 채워진다.")
            return@PolicyCard
        }
        if (uiState.interrupted) {
            WarningText("탭을 벗어나 시나리오가 중간에 멈췄다 — 다시 실행할 것.")
        }
        PolicyLane.entries.forEach { lane ->
            LaneResultBlock(
                lane = lane,
                snapshots = uiState.snapshots.filter { it.lane == lane },
                runs = runs.filter { it.lane == lane },
                timedOut = lane in uiState.timedOut
            )
        }
        CaptionText(
            "다음 실행 기준 = 다음 실행 시각 − 주기. 주기 작업은 마지막 실행이 끝난 시각부터 다시 세므로, " +
                "그 시각이 어느 실행의 끝과 맞는지로 '주기가 이어졌는지·새로 시작했는지'를 본다."
        )
    }
}

@Composable
private fun LaneResultBlock(
    lane: PolicyLane,
    snapshots: List<LaneSnapshot>,
    runs: List<RunRecord>,
    timedOut: Boolean
) {
    val color = laneColor(lane)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = lane.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        verdictOf(lane, snapshots, runs)?.let {
            Text(
                text = it.summary(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                lineHeight = 17.sp
            )
        }
        if (timedOut) WarningText("정착을 기다리다 시간 초과 — 아래 값은 중간 상태일 수 있다")
        Spacer(modifier = Modifier.height(6.dp))
        snapshots.forEach { LabeledText(it.label, it.describe(), Color(0xFF455A64)) }
        Spacer(modifier = Modifier.height(4.dp))
        if (runs.isEmpty()) {
            CaptionText("실행 기록 없음")
        } else {
            runs.forEachIndexed { index, run -> LabeledText("실행 ${index + 1}", run.describe(), Color(0xFF6D4C41)) }
        }
    }
}

private fun LaneSnapshot.describe(): String {
    if (workId == null) return "등록된 작업 없음"
    val interval = intervalMillis?.let { "${it / 60_000}분" } ?: "?"
    val next = nextScheduleAt?.let { "다음 실행 ${formatRemaining(it - takenAt)}" } ?: "다음 실행 시각 없음(${state?.name})"
    return "id ${workId.short()} · ${state?.name} · gen $generation · 주기 $interval · 태그 ${spec?.label ?: "?"} · " +
        "$next · 이 이름의 WorkInfo ${infoCount}개"
}

private fun RunRecord.describe(): String {
    val duration = endedAt?.let { " ${it - startedAt}ms" } ?: ""
    val reason = stopReason?.let { " (stopReason $it)" } ?: ""
    return "${spec.label} 사양 · gen $generation · id ${workId.short()} · ${outcome.label}$duration$reason"
}

// ==================== 4. 읽는 법 ====================

@Composable
private fun ReadingCard() {
    PolicyCard(title = "4. 읽는 법") {
        BulletText(
            "② 신규 설치에서는 세 정책이 똑같이 30분으로 등록된다 — KEEP 을 써도 새로 설치한 기기·에뮬레이터에서는 " +
                "문제가 보이지 않는 이유다. 차이는 ① 처럼 v1 작업이 이미 있는 기존 사용자에게서만 난다."
        )
        BulletText(
            "KEEP 은 이름이 같은 작업이 대기·실행 중이면 새 요청을 통째로 버린다. 주기뿐 아니라 태그도 v1 그대로다" +
                "(입력 데이터도 — 소스상 DB 를 건드리지 않고 돌아간다). 작업을 취소하거나 앱 데이터를 지우기 전까지 새 사양이 가지 않는다."
        )
        BulletText(
            "UPDATE 는 같은 id 를 유지한 채 사양·태그만 바꾸고 generation 을 올린다. 주기 기준 시각(마지막 실행 끝)을 그대로 두므로 " +
                "업데이트 직후 추가 실행이 없다. 사양이 같아도 등록할 때마다 generation 이 오르고 다음 실행 시각은 그대로다(③: gen 0→2)."
        )
        BulletText(
            "CANCEL_AND_REENQUEUE 는 새 id 의 '첫 실행'을 만든다. 주기 작업의 첫 실행은 바로 돌므로(초기 지연·flex 가 없으면) " +
                "등록할 때마다 한 번씩 더 돈다 — 앱 시작마다 이 정책으로 등록하면 주기가 사실상 '앱을 켤 때마다'가 된다(③: id 3개·실행 3회). " +
                "이전 작업은 취소 뒤 삭제돼 같은 이름으로 조회해도 WorkInfo 가 1개뿐이다."
        )
        BulletText(
            "실행 중에 다시 등록하면(④) UPDATE 는 지금 실행을 끊지 않고 다음 실행부터 새 사양을 쓴다 — WorkInfo 는 곧바로 gen 1·30분·태그 v2 인데 " +
                "돌고 있는 워커는 gen 0·v1 사양으로 끝까지 간다. CANCEL_AND_REENQUEUE 는 실행을 끊는다(stopReason 1 = CANCELLED_BY_APP)."
        )
        BulletText(
            "데모에서는 v1 이 방금 돌아서 UPDATE 와 CANCEL_AND_REENQUEUE 의 '다음 실행까지'가 둘 다 30분 가까이로 비슷해 보인다. " +
                "차이는 기준에 있다 — v1 이 20분 전에 돌았다면 UPDATE 는 10분 뒤, CANCEL_AND_REENQUEUE 는 지금 한 번 돌고 30분 뒤다."
        )
        CaptionText("실측: Galaxy A72(Android 13) debug·release, work-runtime 2.12.0. 정책 설명은 같은 버전 소스와 대조했다.")
    }
}

// ==================== 5. 코드 ====================

@Composable
private fun CodeCard() {
    PolicyCard(title = "5. 등록 코드") {
        CodeBlock(
            "// Application.onCreate 등 앱이 켜질 때마다 도는 곳\n" +
                "val request = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)\n" +
                "    .setConstraints(constraints)\n" +
                "    .build()\n\n" +
                "WorkManager.getInstance(context).enqueueUniquePeriodicWork(\n" +
                "    \"sync\",\n" +
                "    ExistingPeriodicWorkPolicy.UPDATE, // 사양을 바꿀 수 있는 작업은 UPDATE\n" +
                "    request\n" +
                ")"
        )
        BulletText("KEEP — 사양이 절대 바뀌지 않는 작업, 또는 일부러 첫 사양을 고정하고 싶을 때.")
        BulletText("UPDATE — 기본값으로 삼을 만한 선택. 기존 사용자에게 새 사양이 가고 주기가 이어진다.")
        BulletText("CANCEL_AND_REENQUEUE — 주기를 지금부터 새로 세고 싶을 때만. 등록 즉시 한 번 돈다.")
        CaptionText("참고 자료와 개념 정리는 같은 폴더 exampleGuide.kt 참고.")
    }
}

// ==================== 공통 UI ====================

private fun laneColor(lane: PolicyLane): Color = when (lane) {
    PolicyLane.KEEP -> Color(0xFFC62828)
    PolicyLane.UPDATE -> Color(0xFF2E7D32)
    PolicyLane.CANCEL_AND_REENQUEUE -> Color(0xFF1565C0)
}

@Composable
private fun ActionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF37474F),
            disabledContainerColor = Color(0xFFCFD8DC)
        )
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun PolicyCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

/** 라벨·값을 한 덩어리로 — Row(라벨, 값)는 라벨이 길면 값 칸이 0 폭에 가까워져 글자가 세로로 쌓인다 */
@Composable
private fun LabeledText(label: String, value: String, labelColor: Color) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = labelColor)) { append(label) }
            append("  ")
            append(value)
        },
        fontSize = 11.sp,
        color = Color(0xFF424242),
        lineHeight = 16.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
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
private fun WarningText(text: String) {
    Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), lineHeight = 16.sp)
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
