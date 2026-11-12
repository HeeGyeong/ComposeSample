package com.example.composesample.presentation.example.component.architecture.development.tracing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

private val ProfilerBlue = Color(0xFF1565C0)
private val LegacyOrange = Color(0xFFE65100)

@Composable
fun ProfilingManagerExampleUI(onBackEvent: () -> Unit) {
    val viewModel: ProfilingManagerViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "ProfilingManager 프로파일링",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard(viewModel.apiLevel) }
            item { LegacyTraceCard(viewModel) }
            item { StackSamplingCard(viewModel) }
            item { BurstCard(viewModel) }
            item { GlobalResultsCard(viewModel) }
            item { TriggerCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard(apiLevel: Int) {
    SectionCard(title = "프로세스 안에서 기록 vs 밖에서 기록", color = ProfilerBlue) {
        Note(
            "레거시 Debug.startMethodTracing* 은 앱 프로세스 안의 ART 추적기가 스택을 모아 앱이 파일을 쓴다. " +
                "Android 15(API 35)의 ProfilingManager 는 요청만 앱이 하고, 기록은 시스템의 Perfetto(traced_perf)가 한다. " +
                "끝나면 앱 몫으로 걸러진(redacted) 파일을 files/profiling 에 넣고 콜백으로 경로를 준다."
        )
        Code(
            "// androidx.core 1.15+ 빌더 (이 앱은 core 1.19.1 — 새 의존성 없음)\n" +
                "val request = StackSamplingRequestBuilder()\n" +
                "    .setDurationMs(4000)          // 끝나는 시점(취소 신호를 안 줘도 멈춘다)\n" +
                "    .setSamplingFrequencyHz(100)\n" +
                "    .setTag(\"cs172\")\n" +
                "    .build()\n" +
                "requestProfiling(context, request, executor) { result ->\n" +
                "    result.errorCode        // 0 이면 성공, 아니면 거절 이유\n" +
                "    result.resultFilePath   // files/profiling/profile_cs172_<시각>.perfetto-stack-sample\n" +
                "}\n" +
                "// 그 밖의 빌더: SystemTraceRequestBuilder · HeapProfileRequestBuilder · JavaHeapDumpRequestBuilder"
        )
        Note("이 기기 API $apiLevel — " + if (apiLevel >= 35) "두 방식 모두 실행할 수 있다." else "ProfilingManager 는 API 35 부터라 레거시 카드만 실행된다.")
    }
}

@Composable
private fun LegacyTraceCard(viewModel: ProfilingManagerViewModel) {
    val runs by viewModel.legacy.collectAsStateWithLifecycle()
    val running by viewModel.running.collectAsStateWithLifecycle()

    SectionCard(title = "① 레거시 — 앱 프로세스 안의 추적기", color = LegacyOrange) {
        Note(
            "같은 부하(사용자 ${LEGACY_USERS}명 JSON 을 Gson 으로 $LEGACY_PARSE_ROUNDS 번)를 세 방식으로 돌린다. 추적을 켜는 순간 이 프로세스에 " +
                "생기는 스레드와, 앱이 직접 쓰는 파일 크기를 본다. 추적은 프로세스 전체에 걸려 부하는 백그라운드 스레드에서 돌린다 — " +
                "전수 추적은 기기에 따라 수십 배 느려진다(API 30 에뮬 debug 에서 ② 의 부하 — 20,000명 × 8번 — 가 2.2초 → 143초, 64MB 버퍼가 가득 참)."
        )
        LegacyTraceMode.entries.forEach { mode ->
            Button(
                onClick = { viewModel.runLegacy(mode) },
                enabled = running == null,
                colors = ButtonDefaults.buttonColors(containerColor = LegacyOrange)
            ) {
                Text(text = if (running == mode.label) "실행 중..." else mode.label, color = Color.White, fontSize = 12.sp)
            }
        }
        if (runs.isNotEmpty()) {
            val base = runs.firstOrNull { it.mode == LegacyTraceMode.NONE }?.workloadMs
            Mono(
                runs.joinToString("\n\n") { r ->
                    val ratio = if (base != null && base > 0 && r.mode != LegacyTraceMode.NONE) " (추적 없이의 ${ratioText(r.workloadMs, base)}배)" else ""
                    "${r.mode.label}\n" +
                        "  부하 ${r.workloadMs}ms$ratio\n" +
                        "  늘어난 스레드 ${if (r.addedThreads.isEmpty()) "없음" else r.addedThreads.joinToString()}\n" +
                        (r.fileName?.let { "  파일 files/legacy_traces/$it · ${r.fileBytes}바이트" + if (r.bufferFull) " — 버퍼 가득(뒤 호출은 기록 안 됨)" else "" } ?: "  파일 없음")
                }
            )
        }
        Note(
            "부하 시간은 기기 · 빌드마다 크게 흔들려 참고만 한다 — 전수 추적은 API 35 에뮬 release(AOT)에서 12~18배, " +
                "API 30 에뮬은 debug 에서도 11~50배 느려졌고 API 35 debug 에서는 차이가 거의 없었다. " +
                "스레드 이름은 커널이 15자에서 자른 값(/proc/self/task/<tid>/comm)이다."
        )
    }
}

@Composable
private fun StackSamplingCard(viewModel: ProfilingManagerViewModel) {
    val run by viewModel.sampling.collectAsStateWithLifecycle()
    val running by viewModel.running.collectAsStateWithLifecycle()

    SectionCard(title = "② ProfilingManager — 시스템이 기록하는 스택 샘플링", color = ProfilerBlue) {
        Note(
            "${STACK_SAMPLING_DURATION_MS}ms · ${STACK_SAMPLING_FREQUENCY_HZ}Hz 로 요청하고, 0.3초 뒤 같은 부하를 메인 스레드에서 돌린다. " +
                "기록이 시작됐다는 신호는 API 에 없어 공식 가이드처럼 잠깐 기다린다. 결과 파일을 앱 안에서 다시 읽어 메인 스레드 샘플에 Gson 이 얼마나 찍혔는지 센다."
        )
        Button(
            onClick = { viewModel.requestStackSampling() },
            enabled = viewModel.supported && running == null,
            colors = ButtonDefaults.buttonColors(containerColor = ProfilerBlue)
        ) {
            Text(
                text = when {
                    !viewModel.supported -> "API 35 이상에서만 (이 기기 ${viewModel.apiLevel})"
                    running == "스택 샘플링" -> "기록 중 · 결과 기다리는 중..."
                    else -> "스택 샘플링 요청 + 메인 스레드 Gson 파싱"
                },
                color = Color.White,
                fontSize = 12.sp
            )
        }
        run?.let { r -> Mono(stackSamplingText(r)) }
        Note(
            "꺼내 보기(debug 빌드): adb exec-out run-as com.example.composesample cat files/profiling/<파일> > s.perfetto-trace → ui.perfetto.dev. " +
                "release 처럼 run-as 가 안 되는 빌드는 앱이 직접 읽거나 업로드한다(이 화면처럼)."
        )
    }
}

private fun stackSamplingText(r: StackSamplingRun): String = buildString {
    val o = r.outcome
    appendLine("결과 ${o.errorCode} ${profilingErrorLabel(o.errorCode)}")
    o.errorMessage?.let { appendLine("메시지 $it") }
    appendLine("요청 → 콜백 ${o.elapsedMs}ms · 부하 ${r.workloadMs}ms")
    appendLine("기록 중 늘어난 앱 스레드 ${if (r.addedThreads.isEmpty()) "없음" else r.addedThreads.joinToString()}")
    o.filePath?.let { appendLine("파일 ${it.substringAfterLast('/')} · ${o.fileBytes}바이트") }
    val s = r.summary ?: return@buildString
    appendLine()
    appendLine("패킷 ${s.packets} · 이 프로세스 샘플 ${s.samples}")
    s.samplesByTid.take(4).forEach { (tid, n) ->
        val label = if (tid == android.os.Process.myPid()) "메인" else (r.threadNames[tid] ?: "끝난 스레드")
        appendLine("  tid $tid ($label) $n")
    }
    appendLine("메인 스레드 ${s.mainThreadSamples} 중 Gson 프레임 포함 ${s.mainWithMarker}")
    if (s.mainWithoutJava > 0) appendLine("  Java 프레임 이름 없는 샘플 ${s.mainWithoutJava}")
    appendLine("메인 스레드 맨 안쪽 Java 프레임")
    s.topLeafFrames.forEach { (name, n) -> appendLine("  $n  ${name.shortFrame()}") }
}.trimEnd()

/** 소수 첫째 자리까지 — String.format 의 기본 로캘을 피한다 */
private fun ratioText(value: Long, base: Long): String {
    val tenths = (value * 10 + base / 2) / base
    return "${tenths / 10}.${tenths % 10}"
}

/** 패키지를 줄여 한 줄에 들어가게 — com.google.gson.stream.JsonReader.nextQuotedValue → gson…JsonReader.nextQuotedValue */
private fun String.shortFrame(): String {
    val parts = split('.')
    if (parts.size <= 3) return this
    val prefix = if (startsWith(GSON_FRAME_PREFIX)) "gson…" else parts.first() + "…"
    return prefix + parts.takeLast(2).joinToString(".")
}

@Composable
private fun BurstCard(viewModel: ProfilingManagerViewModel) {
    val burst by viewModel.burst.collectAsStateWithLifecycle()
    val running by viewModel.running.collectAsStateWithLifecycle()

    SectionCard(title = "③ 시스템이 거절하는 경우", color = ProfilerBlue) {
        Note(
            "프로파일 여부는 시스템이 정한다. 앱 한도 · 기기 전체 한도(시간 창마다 비용으로 셈)를 넘거나 이미 기록 중이면 파일 없이 오류 코드만 온다. " +
                "같은 요청을 연달아 두 번 보내 두 결과를 나란히 본다."
        )
        OutlinedButton(onClick = { viewModel.requestTwice() }, enabled = viewModel.supported && running == null) {
            Text(text = if (running == "연달아 두 번") "결과 기다리는 중..." else "2초짜리 요청을 연달아 두 번", fontSize = 12.sp)
        }
        if (burst.isNotEmpty()) {
            Mono(burst.joinToString("\n") { o ->
                "${o.tag}: ${o.errorCode} ${profilingErrorLabel(o.errorCode)} · ${o.elapsedMs}ms" +
                    (o.filePath?.let { " · ${o.fileBytes}바이트" } ?: " · 파일 없음")
            })
        }
        Code(
            "# 로컬 테스트 때만 한도를 끈다 (기기 설정)\n" +
                "adb shell device_config put profiling_testing rate_limiter.disabled true\n" +
                "# 끝나면 원래대로 — API 35 에뮬에선 delete 만으론 한도가 다시 켜지지 않았다(실측)\n" +
                "adb shell device_config put profiling_testing rate_limiter.disabled false\n" +
                "adb shell device_config delete profiling_testing rate_limiter.disabled"
        )
        Note("운영에서는 한도를 예측하지 말고, 의미 있는 순간에 요청하고 거절은 코드로 남긴다(공식 가이드). 사용자 표본 · 원격 설정으로 요청량을 조절한다.")
    }
}

@Composable
private fun GlobalResultsCard(viewModel: ProfilingManagerViewModel) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val files by viewModel.resultFiles.collectAsStateWithLifecycle()

    SectionCard(title = "④ 전역 수신기 registerForAllProfilingResults", color = ProfilerBlue) {
        Note(
            "요청마다 주는 콜백과 별개로, 이 프로세스로 오는 결과를 모두 받는 Flow. 기록 도중 앱이 죽으면 시스템이 결과를 보관했다가 " +
                "다음 실행에서 이 수신기를 등록할 때 넘겨준다(최대 7일). 받은 파일은 앱이 지우지 않으면 시스템이 5일 뒤 정리한다(최근 Profiling 모듈 기준)."
        )
        Mono(
            if (!viewModel.supported) "API 35 미만 — 수신기를 등록하지 않음"
            else if (history.isEmpty()) "아직 받은 결과 없음"
            else history.take(8).joinToString("\n") { o ->
                "${o.tag.ifEmpty { "(태그 없음)" }}: ${o.errorCode}" + (o.filePath?.let { " · ${it.substringAfterLast('/')}" } ?: "")
            }
        )
        Note("files/profiling 에 남은 결과 ${files.first}개 · ${files.second}바이트")
        OutlinedButton(onClick = { viewModel.deleteResultFiles() }, enabled = files.first > 0) {
            Text(text = "결과 파일 지우기", fontSize = 12.sp)
        }
    }
}

@Composable
private fun TriggerCard() {
    SectionCard(title = "⑤ API 36 — 시스템이 알아서 기록하는 트리거", color = Color(0xFF6A1B9A)) {
        Note(
            "Android 16 부터는 앱이 관심 있는 사건을 등록해 두면 시스템이 상시 돌던 백그라운드 트레이스에서 그 순간을 잘라 넘겨준다 " +
                "— API 36 은 앱 완전 표시(reportFullyDrawn) · ANR 두 가지, android-37 SDK 에는 콜드 스타트 · OOM · 강제 종료 등이 더 있다. " +
                "결과는 ④ 의 전역 수신기로 온다. 이 예제의 기기에는 API 36 이 없어 실행하지 않고 형태만 적는다."
        )
        Code(
            "// API 36 (android.os.ProfilingTrigger)\n" +
                "val trigger = ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_APP_FULLY_DRAWN)\n" +
                "    .setRateLimitingPeriodHours(1)\n" +
                "    .build()\n" +
                "context.getSystemService(ProfilingManager::class.java)\n" +
                "    .addProfilingTriggers(listOf(trigger))\n" +
                "// 결과의 getTriggerType() 도 API 36 — API 35 에서 부르면 NoSuchMethodError(실측)"
        )
    }
}

@Composable
private fun SummaryCard() {
    SectionCard(title = "정리", color = Color(0xFF2E7D32)) {
        Note(
            "• 레거시 추적은 앱 프로세스에 추적 스레드를 만들고 측정 대상과 같은 프로세스에서 돈다.\n" +
                "• ProfilingManager 는 기록을 시스템에 맡겨 앱 스레드가 늘지 않고, 결과는 앱 몫만 걸러 files/profiling 으로 온다.\n" +
                "• 요청은 거절될 수 있다 — 오류 코드(한도 · 이미 기록 중)를 그대로 기록하고 다시 시도는 시스템 판단에 맡긴다.\n" +
                "• 결과의 Java 프레임 이름으로 '메인 스레드에서 Gson 파싱' 같은 문제를 앱 밖 도구 없이 찾을 수 있다.\n" +
                "• API 35 타입(ProfilingResult)은 버전 분기 안에서만 다루고, 화면 상태는 앱 데이터 클래스로 옮긴다."
        )
    }
}

// ============================================================================
// 공통 UI
// ============================================================================

@Composable
private fun SectionCard(title: String, color: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            content()
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF616161), lineHeight = 16.sp)
}

@Composable
private fun Code(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF263238),
        lineHeight = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .border(1.dp, ProfilerBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    )
}

@Composable
private fun Mono(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFBBDEFB),
        lineHeight = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF212121), RoundedCornerShape(8.dp))
            .padding(10.dp)
    )
}
