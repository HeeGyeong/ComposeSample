package com.example.composesample.presentation.example.component.system.platform.exitinfo

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
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.component.system.platform.powersave.ExitReason
import org.koin.androidx.compose.koinViewModel

/**
 * 프로세스 종료 사후 분석 — 일부러 ANR·크래시·스스로 종료를 일으키고, 다음 실행에서 ApplicationExitInfo 로
 * 무엇이 남았는지(이유·상태 요약·트레이스) 실험 기록과 pid 로 맞춰 본다.
 */
@Composable
fun ProcessExitAnalysisExampleUI(onBackEvent: () -> Unit) {
    val viewModel: ProcessExitAnalysisViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    // 크래시 기록 처리기는 이 화면에 있는 동안만 끼운다
    DisposableEffect(Unit) {
        viewModel.attachCrashRecorder()
        onDispose { viewModel.detachCrashRecorder() }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        MainHeader(title = "프로세스 종료 사후 분석", onBackIconClicked = onBackEvent)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard(uiState) }
            item { ExperimentCard(uiState, viewModel) }
            item { MatchCard(uiState, viewModel) }
            item { ExitListCard(uiState) }
            item { SummaryCard(uiState, viewModel) }
            item { CodeCard() }
            item { ReadingCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard(uiState: ProcessExitAnalysisViewModel.UiState) {
    ExitCard(title = "1. 죽은 이유는 다음 실행에서 읽힌다") {
        BodyText(
            "프로세스가 죽는 순간에는 앱 코드가 돌지 않는다(ANR·네이티브 크래시·시스템 종료). 시스템이 남긴 기록을 다음 실행에서 " +
                "ActivityManager.getHistoricalProcessExitReasons(API 30)로 읽는 것이 사후 분석의 출발점이다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        BulletText("이유(getReason)·상태(getStatus — 종료 코드나 시그널)·중요도·PSS/RSS·설명 문자열")
        BulletText("getTraceInputStream — ANR 이면 그 순간의 스레드 덤프, API 31+ 네이티브 크래시는 tombstone(protobuf)")
        BulletText("getProcessStateSummary — 살아 있을 때 setProcessStateSummary 로 남긴 128바이트까지의 '하던 일'")
        BulletText("기록은 시스템 전역 순환 버퍼라 밀려나거나 트레이스가 null 일 수 있다(javadoc)")
        Spacer(modifier = Modifier.height(6.dp))
        LabeledText("이 프로세스", "pid ${uiState.pid}")
        LabeledText("ApplicationExitInfo", if (uiState.supported) "사용 가능(API 30+)" else "API 30 미만 — 시스템 기록 없음, 앱 처리기 기록만")
        LabeledText(
            "메모리 부족 종료 보고",
            uiState.lowMemoryKillReportSupported?.let { "isLowMemoryKillReportSupported = $it" } ?: "API 30 미만"
        )
    }
}

// ==================== 2. 실험 ====================

@Composable
private fun ExperimentCard(uiState: ProcessExitAnalysisViewModel.UiState, viewModel: ProcessExitAnalysisViewModel) {
    ExitCard(title = "2. 실험 — 이 프로세스를 일부러 끝낸다") {
        WarningText("버튼마다 이 앱 프로세스가 끝난다. 끝나기 직전에 종류·pid(${uiState.pid})·시각을 기록하고 상태 요약에도 남긴다.")
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DangerButton("ANR 일으키기") { viewModel.triggerAnr() }
            DangerButton("크래시") { viewModel.triggerCrash() }
            DangerButton("System.exit") { viewModel.exitSelf() }
            DangerButton("killProcess") { viewModel.killSelf() }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ExperimentKind.entries.forEach { BulletText("${it.label} — ${it.how}") }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("끝난 뒤 앱을 다시 열어 이 화면으로 오면 3번 카드에 결과가 맞춰진다. 크래시를 연달아 일으키면 기기가 '앱이 계속 중지됨' 안내를 띄울 수 있다.")
    }
}

// ==================== 3. 실험 ↔ 시스템 기록 ====================

@Composable
private fun MatchCard(uiState: ProcessExitAnalysisViewModel.UiState, viewModel: ProcessExitAnalysisViewModel) {
    ExitCard(title = "3. 실험 ↔ 시스템 기록(pid 로 맞춤)") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(if (uiState.loading) "읽는 중…" else "다시 읽기") { viewModel.refresh() }
            ActionButton("기록 지우기") { viewModel.clearRecords() }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (uiState.experiments.isEmpty()) {
            CaptionText("아직 실험 기록이 없다. 2번 카드의 버튼으로 프로세스를 끝내 본다.")
            return@ExitCard
        }
        uiState.experiments.forEach { experiment ->
            val exit = matchExit(experiment, uiState.exits)
            val crash = uiState.crashes.firstOrNull { it.pid == experiment.pid }
            SubTitle("[${experiment.kind.label}] pid ${experiment.pid} · ${formatTime(experiment.triggeredAt)}")
            when {
                experiment.pid == uiState.pid -> CaptionText("이 프로세스가 남긴 기록인데 아직 살아 있다 — 끝나지 않았다.")
                isSuperseded(experiment, uiState.experiments) ->
                    CaptionText("같은 프로세스에서 뒤에 또 실험했다 — 이 실험으로는 끝나지 않았다(ANR 대화상자에서 기다리기 등).")
                exit == null -> CaptionText(
                    if (uiState.supported) "같은 pid 의 시스템 기록이 없다(버퍼에서 밀렸거나 아직 반영 전)." else "API 30 미만 — 시스템 기록 없음."
                )
                else -> {
                    LabeledText("시스템", "${ExitReason.label(exit.reason)} · status ${exit.status} · ${formatTime(exit.timestamp)}")
                    exit.description?.let { LabeledText("설명", it) }
                    LabeledText("상태 요약", exit.summary ?: "없음")
                    LabeledText("트레이스", traceLabel(exit))
                    if (exit.mainStack.isNotEmpty()) StackBlock(exit.mainStack.take(STACK_PREVIEW_LINES))
                }
            }
            crash?.let { LabeledText("앱 처리기", "${it.thread} · ${it.exception} · \"${it.message}\" · ${formatTime(it.at)}") }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private const val STACK_PREVIEW_LINES = 14
private const val APP_PACKAGE = "com.example.composesample"

private fun traceLabel(exit: ExitSnapshot): String {
    if (exit.traceBytes == null) return "없음"
    if (exit.mainStack.isEmpty()) return "${exit.traceBytes}바이트"
    val marker = ProcessExitAnalysisViewModel.ANR_MARKER
    val line = markerLine(exit.mainStack, marker)
    val appFrames = appFrameCount(exit.mainStack, APP_PACKAGE)
    return "${exit.traceBytes}바이트 · main 스레드 ${exit.mainStack.size}줄(앱 프레임 ${appFrames}줄) 중 $marker " +
        if (line != null) "${line}번째 줄" else "없음"
}

// ==================== 4. 종료 기록 전체 ====================

@Composable
private fun ExitListCard(uiState: ProcessExitAnalysisViewModel.UiState) {
    ExitCard(title = "4. 종료 기록 전체 — 최근 10건") {
        BodyText("이 앱의 모든 프로세스(:remote 등 포함) 기록이 최신 순으로 온다. 실험과 무관한 종료(업데이트·최근 앱에서 제거 등)도 섞인다.")
        Spacer(modifier = Modifier.height(6.dp))
        when {
            !uiState.supported -> CaptionText("API 30 미만 — 읽을 수 없음")
            uiState.exits.isEmpty() -> CaptionText("기록 없음")
            else -> uiState.exits.forEach {
                LabeledText(
                    "${formatTime(it.timestamp)} pid ${it.pid}",
                    "${ExitReason.label(it.reason)} · status ${it.status} · ${it.processName} · importance ${it.importance} · " +
                        "PSS ${it.pssKb}KB · RSS ${it.rssKb}KB" + (it.description?.let { d -> " · \"$d\"" } ?: "") +
                        (it.summary?.let { s -> " · 요약 \"$s\"" } ?: "") + (it.traceBytes?.let { b -> " · 트레이스 ${b}B" } ?: "")
                )
            }
        }
    }
}

// ==================== 5. 상태 요약 한도 ====================

@Composable
private fun SummaryCard(uiState: ProcessExitAnalysisViewModel.UiState, viewModel: ProcessExitAnalysisViewModel) {
    ExitCard(title = "5. 상태 요약은 128바이트까지") {
        BodyText(
            "setProcessStateSummary(byte[]) 는 '지금 어느 화면·단계인지' 같은 작은 값을 시스템에 맡겨 두고, 프로세스가 죽은 뒤 그 기록에 붙여 돌려받는다. " +
                "한도는 바이트라 한글은 글자당 3바이트(UTF-8)다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("ASCII 128바이트") { viewModel.probeSummary(MAX_STATE_SUMMARY_BYTES, korean = false) }
            ActionButton("ASCII 129바이트") { viewModel.probeSummary(MAX_STATE_SUMMARY_BYTES + 1, korean = false) }
            ActionButton("한글 129바이트") { viewModel.probeSummary(MAX_STATE_SUMMARY_BYTES + 1, korean = true) }
        }
        uiState.summaryProbe?.let {
            Spacer(modifier = Modifier.height(6.dp))
            LabeledText("결과", it)
        }
        Spacer(modifier = Modifier.height(6.dp))
        BulletText("요약은 마지막으로 쓴 값 하나만 남는다 — 위 버튼을 누른 뒤 2번 실험을 하면 실험이 다시 덮어쓴다.")
        BulletText("javadoc: 호출이 잦으면 RuntimeException 으로 막힐 수 있고, 개인 정보는 넣지 말 것. UI 상태 복원용이 아니라 분석용이다.")
    }
}

// ==================== 6. 코드 ====================

@Composable
private fun CodeCard() {
    ExitCard(title = "6. 다음 실행에서 읽는 코드") {
        CodeBlock(
            "// 살아 있을 때 — 단계가 바뀔 때만\n" +
                "activityManager.setProcessStateSummary(\"checkout|step=pay\".toByteArray())\n\n" +
                "// 다음 실행 — 마지막으로 본 기록 이후 것만 처리\n" +
                "activityManager.getHistoricalProcessExitReasons(packageName, 0, 10)\n" +
                "    .filter { it.timestamp > lastSeen }\n" +
                "    .forEach { info ->\n" +
                "        when (info.reason) {\n" +
                "            REASON_ANR -> upload(info.traceInputStream?.readBytes())\n" +
                "            REASON_CRASH, REASON_CRASH_NATIVE -> report(info)\n" +
                "            REASON_LOW_MEMORY -> trimCachesNextTime()\n" +
                "        }\n" +
                "        info.processStateSummary   // 죽기 직전 하던 일\n" +
                "    }"
        )
        CaptionText("목록은 매번 같은 기록을 다시 주므로 처리한 마지막 시각을 저장해 두고 그 뒤 것만 본다.")
    }
}

// ==================== 7. 읽는 법 ====================

@Composable
private fun ReadingCard() {
    ExitCard(title = "7. 읽는 법") {
        BulletText("실험 기록과 시스템 기록은 pid 로 맞춘다. 같은 pid 에서 뒤에 또 실험했다면 앞 실험으로는 끝나지 않은 것이다(ANR 대화상자에서 기다리기 등).")
        BulletText(
            "종료 방식마다 남는 값이 다르다 — ANR 은 ANR(status 0, 설명 'user request after error: Input dispatching timed out …'), " +
                "System.exit(0) 은 EXIT_SELF(status 0), killProcess 는 SIGNALED(status 9 = SIGKILL), 크래시는 CRASH(설명 'crash'). " +
                "넷 다 앱 화면이 떠 있던 중이라 시스템이 새 pid 로 곧바로 다시 띄웠다 — 단, 짧은 간격의 두 번째 크래시는 '계속 중지됨' 대화상자가 떠 " +
                "기록 시각이 대화상자를 닫은 시각(51초 뒤)이 됐고 앱은 다시 뜨지 않았다(에뮬레이터)."
        )
        BulletText("크래시의 시스템 기록에는 'crash' 한 단어뿐 — 예외 클래스·메시지는 앱의 기본 처리기만 안다. ANR·시그널·exit 는 앱 코드가 돌지 않아 시스템 기록에만 남는다.")
        BulletText(
            "debug(HotSwan) 빌드의 ANR 트레이스에는 앱 프레임이 한 줄도 없다(Thread.sleep 바로 위가 MethodHandle·Transformers 프레임). " +
                "release 에서는 blockMainThreadForAnr → triggerAnr → 클릭 람다 → Compose performClick 이 그대로 찍힌다 — 원인 분석은 release 트레이스로."
        )
        BulletText("ANR 대기 한도는 기기마다 다르다 — 에뮬레이터 5초, Galaxy 10초. 대화상자는 탭 뒤 각각 약 6초·17초에 떴다.")
        BulletText("상태 요약은 129바이트부터 IllegalArgumentException(\"Data size is too large\") — 한글은 42자까지다.")
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "실측: Galaxy A72(SM-A725F, API 33) debug·release · Pixel Tablet API 35 에뮬레이터 debug·release. ANR 트레이스 main 스레드 — " +
                "debug 94~95줄 중 앱 프레임 0줄, release 61~62줄 중 앱 프레임 5줄(마커 6~7번째 줄). isLowMemoryKillReportSupported 는 둘 다 true."
        )
    }
}

// ==================== 공통 UI ====================

@Composable
private fun DangerButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun ExitCard(title: String, content: @Composable () -> Unit) {
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

@Composable
private fun SubTitle(text: String) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
}

/** 라벨·값을 한 덩어리로 — Row(라벨, 값)는 라벨이 길면 값 칸이 0 폭에 가까워져 글자가 세로로 쌓인다 */
@Composable
private fun LabeledText(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF37474F))) { append(label) }
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
private fun StackBlock(lines: List<String>) {
    CodeBlock(lines.joinToString("\n"))
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
