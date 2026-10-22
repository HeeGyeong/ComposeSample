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
 * WorkManager 예제의 '주기 작업 실행 시각' 탭.
 *
 * 주기 작업의 다음 실행 시각이 어떻게 정해지는지(마지막 실행 끝 + 주기 · flex · 백오프)와,
 * setNextScheduleTimeOverride 로 그 시각을 직접 지정할 때의 규칙(최소 15분 · 한 번만 · UPDATE 와 함께)을
 * WorkInfo.nextScheduleTimeMillis 실측값과 규칙으로 계산한 기대값을 나란히 놓고 확인한다.
 */
@Composable
internal fun PeriodicScheduleTab() {
    val viewModel: PeriodicScheduleViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val runs = viewModel.runs.collectAsStateWithLifecycle().value

    // 데모 작업은 진짜 주기 작업이다 — 탭을 벗어나면(다른 탭·뒤로가기) 바로 취소한다
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
        item { RuleCard() }
        item { ScenarioCard(viewModel, uiState) }
        item { ResultCard(uiState, runs) }
        item { ReadingCard() }
        item { CodeCard() }
    }
}

// ==================== 1. 규칙 ====================

@Composable
private fun RuleCard() {
    ScheduleCard(title = "1. 다음 실행 시각은 이렇게 정해진다") {
        BodyText(
            "주기 작업은 '몇 시에'가 아니라 '마지막 실행이 끝난 뒤 얼마 뒤에'로 잡힌다. 실행이 늦게 시작하거나 오래 걸린 만큼 " +
                "다음 실행도 밀리므로, 매일 아침 7시 같은 벽시계 시각은 주기만으로 지킬 수 없다(드리프트)."
        )
        CodeBlock(
            "// work-runtime 2.12.0 WorkSpec.calculateNextRunTime(주기 작업)\n" +
                "override 있음 → 첫 실행 전: override\n" +
                "              그 뒤:     max(override, lastEnqueueTime + 15분)\n" +
                "재시도 중     → lastEnqueueTime + 백오프(지수: 30초 × 2^(시도−1))\n" +
                "첫 실행 전    → 등록 시각 + initialDelay (+ 주기 − flex, flex 가 있으면)\n" +
                "그 뒤         → lastEnqueueTime(= 마지막 실행 끝) + 주기"
        )
        BulletText("setNextScheduleTimeOverride(work 2.9+)는 다음 한 번의 시각을 직접 정한다 — 실행이 끝나면 지워지고 다시 주기대로.")
        BulletText("실행 중인 워커가 자기 요청을 updateWork 로 다시 지정하면, 실행 뒤의 '지우기'가 그 값을 지우지 않는다(오버라이드 세대가 바뀌어서).")
        BulletText("화면의 '다음 실행'은 WorkInfo.nextScheduleTimeMillis — 실제 실행은 시스템 스케줄러·도즈·제약에 따라 늦을 수 있다.")
    }
}

// ==================== 2. 시나리오 ====================

@Composable
private fun ScenarioCard(viewModel: PeriodicScheduleViewModel, uiState: PeriodicScheduleViewModel.UiState) {
    ScheduleCard(title = "2. 시나리오 — 두 줄(A·B)을 나란히") {
        BodyText(
            "모든 데모 작업은 30분 주기이고 워커는 0.3초 기다렸다 끝난다. 시나리오마다 기대값을 위 규칙으로 계산해 실측값과 " +
                "맞춰 보고, 관찰이 끝나면 데모 작업을 취소한다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodicScheduleViewModel.Scenario.entries.forEach { scenario ->
                ActionButton(label = scenario.label, enabled = uiState.running == null) { viewModel.run(scenario) }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        PeriodicScheduleViewModel.Scenario.entries.forEach { BulletText("${it.label} — ${it.description}") }
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
        CaptionText("③ 은 지정한 +20초를 실제로 기다린다. 탭을 벗어나면 진행 중인 시나리오와 데모 작업을 모두 멈춘다.")
    }
}

// ==================== 3. 결과 ====================

@Composable
private fun ResultCard(uiState: PeriodicScheduleViewModel.UiState, runs: List<ScheduleRunRecord>) {
    val title = uiState.shown?.let { "3. 결과 — ${it.label}" } ?: "3. 결과"
    ScheduleCard(title = title) {
        if (uiState.snapshots.isEmpty() && uiState.notes.isEmpty()) {
            CaptionText("위에서 시나리오를 실행하면 결과가 채워진다.")
            return@ScheduleCard
        }
        if (uiState.interrupted) WarningText("탭을 벗어나 시나리오가 중간에 멈췄다 — 다시 실행할 것.")
        if (uiState.timedOut) WarningText("실행이 끝나기를 기다리다 시간 초과 — 아래 값은 중간 상태일 수 있다.")
        uiState.notes.forEach { LabeledText("관찰", it, Color(0xFF6A1B9A)) }
        if (uiState.notes.isNotEmpty()) Spacer(modifier = Modifier.height(6.dp))
        ScheduleLane.entries.forEach { lane ->
            val mine = uiState.snapshots.filter { it.lane == lane }
            if (mine.isNotEmpty()) LaneBlock(lane, mine, runs.filter { it.lane == lane })
        }
        CaptionText("일치 = 실측과 기대의 차이 3초 이내(lastEnqueueTime 은 워커가 끝난 직후 기록돼 워커의 끝 시각과 수 ms 다르다).")
    }
}

@Composable
private fun LaneBlock(lane: ScheduleLane, snapshots: List<ScheduleSnapshot>, runs: List<ScheduleRunRecord>) {
    val color = laneColor(lane)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = lane.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        snapshots.forEach { SnapshotView(it) }
        Spacer(modifier = Modifier.height(4.dp))
        if (runs.isEmpty()) {
            CaptionText("실행 기록 없음")
        } else {
            runs.forEachIndexed { index, run -> LabeledText("실행 ${index + 1}", run.describe(), Color(0xFF6D4C41)) }
        }
    }
}

@Composable
private fun SnapshotView(snapshot: ScheduleSnapshot) {
    val next = snapshot.nextScheduleAt
    val measured = if (next == null) {
        "다음 실행 없음(${snapshot.state ?: "등록된 작업 없음"})"
    } else {
        "다음 실행 ${formatClock(next)} (${formatSigned(next - snapshot.takenAt)})"
    }
    val verdict = when {
        snapshot.expected == null && next == null -> "기대대로 없음"
        snapshot.expected == null -> "기대값 없음"
        snapshot.matches -> "일치"
        else -> "차이 ${snapshot.deltaMillis?.let(::formatSigned) ?: "?"}"
    }
    val verdictColor = when (verdict) {
        "일치", "기대대로 없음" -> Color(0xFF2E7D32)
        else -> Color(0xFFC62828)
    }
    Column(modifier = Modifier.padding(top = 6.dp)) {
        LabeledText(snapshot.label, measured, Color(0xFF455A64))
        Text(
            text = "기대 ${snapshot.expected?.let(::formatClock) ?: "—"} · ${snapshot.expectedReason} → $verdict",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = verdictColor,
            lineHeight = 16.sp
        )
        CaptionText(
            "상태 ${snapshot.state ?: "?"} · gen ${snapshot.generation ?: "?"} · 시도 ${snapshot.runAttemptCount ?: "?"} · " +
                "주기 ${snapshot.intervalMillis?.let { "${it / 60_000}분" } ?: "?"} · flex ${snapshot.flexMillis?.let { "${it / 60_000}분" } ?: "?"}"
        )
    }
}

private fun ScheduleRunRecord.describe(): String {
    val duration = endedAt?.let { " (${it - startedAt}ms)" } ?: " (실행 중)"
    val result = if (retried) "retry" else "success"
    val override = override?.let { " · 지정 ${formatClock(it)}" } ?: ""
    return "${formatClock(startedAt)} 시작$duration · ${mode.name} · gen $generation · $result$override"
}

// ==================== 4. 읽는 법 ====================

@Composable
private fun ReadingCard() {
    ScheduleCard(title = "4. 읽는 법") {
        BulletText(
            "① 주기만 쓰면 다음 실행은 '실행 끝 + 30분'이라 초·밀리초까지 실행 시각에 끌려간다. 워커가 실행 안에서 다음 슬롯을 계산해 " +
                "오버라이드로 넣으면 다음 실행이 슬롯 정각(…:00.000)으로 고정된다 — KDoc 의 '매일 아침 사용자가 깨기 전에, 밀림 없이'."
        )
        BulletText(
            "② 오버라이드에도 최소 15분이 걸린다. 첫 실행 전에는 +1분이 그대로지만, 한 번 돈 뒤에는 '실행 끝 + 15분'으로 밀린다 — " +
                "주기(30분)보다 당길 수는 있어도 15분 아래로는 안 된다."
        )
        BulletText(
            "③ 오버라이드는 다음 한 번만이다. 실행되면 지워져 '실행 끝 + 주기'로 돌아가고, 태그만 바꾼 updateWork 에는 남아 있으며, " +
                "clearNextScheduleTimeOverride 로만 지운다."
        )
        BulletText("④ 재시도에서도 오버라이드가 백오프보다 앞선다 — 첫 실행의 재시도라 15분 하한도 걸리지 않는다(커스텀 백오프).")
        BulletText("⑤ flex 가 있으면 첫 실행이 바로가 아니라 (주기 − flex) 뒤다. flex 없는 주기 작업의 첫 실행은 등록 즉시 돈다.")
        BulletText(
            "⑥ 오버라이드를 실은 요청은 UPDATE·KEEP 으로만 등록한다. CANCEL_AND_REENQUEUE(REPLACE)는 매번 새 WorkSpec 이라 " +
                "최소 간격을 지킬 수 없어 WorkManager 가 IllegalArgumentException 으로 막는다."
        )
        CaptionText(
            "실측(Pixel Tablet API 35 에뮬레이터, debug·release 동일, work-runtime 2.12.0): ① A 다음 실행 = 실행 끝 + 30분(…:46.810), " +
                "B = 슬롯 정각(…:00.000, gen 0→1) · ② B 는 +1분을 지정해도 실행 끝 + 15분 · ③ 지정 +20초에 +10~12ms 로 실행, " +
                "updateWork 마다 gen +1 · ④ 백오프 +30초 vs 지정 +3분 · ⑤ 등록 + 20분 · ⑥ 메시지 \"Next Schedule Time Override must be used " +
                "with ExistingPeriodicWorkPolicyUPDATE (preferably) or KEEP\"(띄어쓰기 빠진 원문 그대로). 기대값과의 차이는 모두 100ms 이내(재시도 백오프 83ms 가 최대)."
        )
    }
}

// ==================== 5. 코드 ====================

@Composable
private fun CodeCard() {
    ScheduleCard(title = "5. 워커가 자기 다음 실행을 정하는 코드") {
        CodeBlock(
            "override suspend fun doWork(): Result {\n" +
                "    sync()\n" +
                "    val next = nextSlot(System.currentTimeMillis())   // 예: 내일 06:50\n" +
                "    WorkManager.getInstance(applicationContext).updateWork(\n" +
                "        PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.DAYS)\n" +
                "            .setId(id)                              // 자기 자신을 갱신\n" +
                "            .setNextScheduleTimeOverride(next)\n" +
                "            .build()\n" +
                "    ).await()\n" +
                "    return Result.success()   // retry() 여도 다음 시각은 next\n" +
                "}\n\n" +
                "// 처음 등록 · 앱 시작마다 — 오버라이드와 함께라면 UPDATE(또는 KEEP)\n" +
                "workManager.enqueueUniquePeriodicWork(\"sync\", ExistingPeriodicWorkPolicy.UPDATE, request)"
        )
        BulletText("updateWork 는 실행 중인 워커를 끊지 않는다(APPLIED_FOR_NEXT_RUN). 새 요청은 원래 사양(주기·입력·제약)을 그대로 담아야 한다 — 사양 전체가 바뀐다.")
        BulletText("하루 주기라면 슬롯이 늘 '실행 끝 + 15분' 뒤라 오버라이드가 그대로 쓰인다. 이 데모는 30분 주기라 슬롯을 16분 이후로 고른다.")
    }
}

// ==================== 공통 UI ====================

private fun laneColor(lane: ScheduleLane): Color = when (lane) {
    ScheduleLane.A -> Color(0xFF6D4C41)
    ScheduleLane.B -> Color(0xFF1565C0)
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
private fun ScheduleCard(title: String, content: @Composable () -> Unit) {
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
