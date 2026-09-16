package com.example.composesample.presentation.example.component.system.background.workmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.composesample.presentation.MainHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 워커 예외 핸들러 예제 (work-runtime 2.11.0+)
 *
 * - WorkManager 는 워커가 **터져도 앱을 죽이지 않는다.** 로그만 남기고 작업을 FAILED 로 닫아 버리기
 *   때문에, 크래시 리포팅 도구에도 아무것도 올라가지 않는 사각지대가 생긴다.
 *   `Configuration.Builder` 의 `setWorkerExecutionExceptionHandler` / `setWorkerInitializationExceptionHandler`
 *   가 그 사각지대를 여는 콜백이고, 인자로 [androidx.work.WorkerExceptionInfo](workerClassName·workerParameters·throwable)
 *   가 온다.
 * - 화면은 **실패 4종을 직접 넣어 보고** 무엇이 핸들러를 깨우는지 대조한다 —
 *   ① 실행 중 throw ② Result.failure() ③ 생성자 throw ④ 생성자 시그니처 불일치.
 *   WorkInfo 상태는 넷 다 FAILED 라서, 상태만으로는 구분되지 않는다는 것이 이 예제의 출발점이다.
 * - 전달되는 Throwable 의 모양도 경로마다 다르다. 기본 팩토리가 `Constructor.newInstance` 를 쓰므로
 *   ③은 **InvocationTargetException 으로 감싸져** 오고(원인은 cause), ④는 `getDeclaredConstructor`
 *   단계에서 나므로 **NoSuchMethodException 이 그대로** 온다. work-runtime 2.11.2 의 `WorkerFactory`
 *   바이트코드에서 확인한 경로다.
 * - 핸들러는 앱 전역 설정이라 [com.example.composesample.application.BaseApplication] 이
 *   `Configuration.Provider` 를 구현하고, 매니페스트에서 기본 초기화를 제거한다. 워커와 수집기는
 *   같은 폴더의 WorkerExceptionDemoWorkers.kt 에 있다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 참고.
 */

// ==================== 시나리오 정의 ====================

/** 화면이 넣어 보는 실패 4종. id 는 태그로 달아 WorkInfo 를 시나리오별로 골라낸다. */
private enum class DemoScenario(
    val id: String,
    val label: String,
    val buttonText: String,
    val expectedHandler: String,
    val expectedThrowable: String
) {
    ExecutionThrow(
        id = "execution_throw",
        label = "① 실행 중 throw",
        buttonText = "예외 던지기",
        expectedHandler = "Execution 발화",
        expectedThrowable = "IllegalStateException"
    ),
    ExecutionFailure(
        id = "execution_failure",
        label = "② Result.failure()",
        buttonText = "실패 결과 반환",
        expectedHandler = "발화 없음",
        expectedThrowable = "-"
    ),
    InitThrow(
        id = "init_throw",
        label = "③ 생성자 throw",
        buttonText = "생성자에서 던지기",
        expectedHandler = "Initialization 발화",
        expectedThrowable = "InvocationTargetException → cause"
    ),
    InitSignature(
        id = "init_signature",
        label = "④ 생성자 시그니처 불일치",
        buttonText = "생성자 없는 워커",
        expectedHandler = "Initialization 발화",
        expectedThrowable = "NoSuchMethodException"
    )
}

/** 시나리오에 맞는 워커를 한 번만 실행하는 요청으로 만들어 넣는다. */
private fun enqueueScenario(workManager: WorkManager, scenario: DemoScenario) {
    val builder = when (scenario) {
        DemoScenario.ExecutionThrow -> OneTimeWorkRequestBuilder<ThrowingDemoWorker>()
        DemoScenario.ExecutionFailure -> OneTimeWorkRequestBuilder<FailureResultDemoWorker>()
        DemoScenario.InitThrow -> OneTimeWorkRequestBuilder<BrokenInitDemoWorker>()
        DemoScenario.InitSignature -> OneTimeWorkRequestBuilder<MissingConstructorDemoWorker>()
    }

    val request = builder
        // 핸들러는 workerParameters 를 통째로 받으므로, 여기 넣은 inputData 가 리포트에 그대로 보인다
        .setInputData(workDataOf(WorkerExceptionScenarioKey to scenario.label))
        .addTag(WorkerExceptionDemoTag)
        .addTag(scenario.id)
        .build()

    workManager.enqueue(request)
}

// ==================== 화면 ====================

@Composable
fun WorkerExceptionHandlerExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val workManager = remember(context) { WorkManager.getInstance(context) }

    // 핸들러가 남긴 기록. 화면을 닫아도 계속 쌓인다(앱 전역 수집기).
    val reports by WorkerExceptionReporter.reports.collectAsStateWithLifecycle()
    val workInfos by workManager
        .getWorkInfosByTagFlow(WorkerExceptionDemoTag)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Worker 예외 핸들러",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { RegistrationCard() }
            item {
                ExecutionCard(
                    onEnqueue = { scenario -> enqueueScenario(workManager, scenario) }
                )
            }
            item {
                InitializationCard(
                    onEnqueue = { scenario -> enqueueScenario(workManager, scenario) }
                )
            }
            item { ComparisonCard(workInfos = workInfos, reports = reports) }
            item { MeasurementCard() }
            item { ReportListCard(reports = reports) }
            item { PitfallCard() }
            item { SummaryCard() }
        }
    }
}

// ==================== 1. 핸들러 4종이 가리키는 실패가 다르다 ====================

@Composable
private fun ConceptCard() {
    SectionCard(title = "1. Configuration 이 받는 예외 핸들러는 4종이다") {
        BodyText(
            "워커가 던진 예외는 앱을 죽이지 않는다. WorkManager 가 잡아서 로그를 남기고 작업을 " +
                "FAILED 로 닫기 때문에, 아무 설정도 하지 않으면 크래시 리포팅에는 한 줄도 올라가지 않는다. " +
                "예외 핸들러는 그 사각지대를 여는 콜백이다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("핸들러", "언제 불리는가 / 인자", isHeader = true)
        TableRow("Initialization", "WorkManager 초기화 실패 · Consumer<Throwable>")
        TableRow("Scheduling", "작업 스케줄링 실패 · Consumer<Throwable>")
        TableRow(
            "WorkerInitialization",
            "워커 인스턴스 생성 실패 · Consumer<WorkerExceptionInfo> ← 2.11 신규"
        )
        TableRow(
            "WorkerExecution",
            "워커 실행 중 Throwable 전파 · Consumer<WorkerExceptionInfo> ← 2.11 신규"
        )
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "앞의 둘은 Throwable 하나만 받지만, 워커 관련 둘은 WorkerExceptionInfo 를 받는다. " +
                "어떤 워커가 왜 터졌는지 식별할 재료가 여기에 다 들어 있다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        TableRow("WorkerExceptionInfo", "내용", isHeader = true)
        TableRow("workerClassName", "실패한 워커의 FQCN 문자열")
        TableRow("workerParameters", "id · tags · inputData · runAttemptCount 등 실행 컨텍스트")
        TableRow("throwable", "실패를 만든 Throwable(경로에 따라 감싸여 온다)")
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "work-runtime 2.11.2 의 WorkerWrapper 바이트코드 기준 — 생성 실패는 \"Could not create Worker\", " +
                "실행 실패는 \"failed because it threw an exception/error\" 로그 직후에 호출된다. " +
                "두 경로 모두 마지막에 Resolution.Failed 를 돌려주므로 결과는 재시도가 아니라 FAILED 다."
        )
    }
}

// ==================== 2. 등록은 앱 전역 Configuration 에서 한 번 ====================

@Composable
private fun RegistrationCard() {
    SectionCard(title = "2. 등록 위치는 화면이 아니라 Application 이다") {
        BodyText(
            "핸들러는 Configuration 에만 실을 수 있고, Configuration 은 WorkManager 가 초기화될 때 " +
                "딱 한 번 읽힌다. 그래서 Application 이 Configuration.Provider 를 구현한다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "class BaseApplication : Application(), Configuration.Provider {\n" +
                "    override val workManagerConfiguration: Configuration\n" +
                "        get() = Configuration.Builder()\n" +
                "            .setWorkerInitializationExceptionHandler(Consumer { info ->\n" +
                "                // info.workerClassName / info.workerParameters / info.throwable\n" +
                "            })\n" +
                "            .setWorkerExecutionExceptionHandler(Consumer { info -> /* ... */ })\n" +
                "            .build()\n" +
                "}"
        )
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "여기서 한 단계가 더 필요하다. work-runtime 이 매니페스트에 심어 두는 androidx.startup " +
                "초기화가 먼저 돌면 기본 Configuration 으로 초기화돼 위 설정이 무시된다. 기본 초기화를 " +
                "지워야 첫 getInstance() 호출 때 Provider 가 쓰인다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "<provider\n" +
                "    android:name=\"androidx.startup.InitializationProvider\"\n" +
                "    android:authorities=\"\${applicationId}.androidx-startup\"\n" +
                "    android:exported=\"false\"\n" +
                "    tools:node=\"merge\">\n" +
                "    <meta-data\n" +
                "        android:name=\"androidx.work.WorkManagerInitializer\"\n" +
                "        android:value=\"androidx.startup\"\n" +
                "        tools:node=\"remove\" />\n" +
                "</provider>"
        )
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "이 앱의 매니페스트에 실제로 들어 있는 선언이다. 핸들러 등록 외의 설정은 전부 기본값이라 " +
                "기존 WorkManager 예제의 동작은 그대로 유지된다."
        )
    }
}

// ==================== 3. 실행 실패: throw 와 Result.failure() ====================

@Composable
private fun ExecutionCard(onEnqueue: (DemoScenario) -> Unit) {
    SectionCard(title = "3. 실행 실패 — throw 는 핸들러를 깨우고, Result.failure() 는 아니다") {
        BodyText(
            "같은 \"실패\"라도 예외를 밖으로 내보냈는지가 갈림길이다. doWork() 밖으로 나간 Throwable 만 " +
                "WorkerExecution 핸들러를 깨운다. Result.failure() 는 정상적인 종료 경로라 아무도 부르지 않는다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScenarioButton(
                scenario = DemoScenario.ExecutionThrow,
                containerColor = Color(0xFFC62828),
                onEnqueue = onEnqueue
            )
            ScenarioButton(
                scenario = DemoScenario.ExecutionFailure,
                containerColor = Color(0xFF546E7A),
                onEnqueue = onEnqueue
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            "// ① 핸들러가 불린다\n" +
                "override suspend fun doWork(): Result {\n" +
                "    throw IllegalStateException(\"...\")\n" +
                "}\n\n" +
                "// ② 핸들러가 불리지 않는다 — WorkInfo 상태는 똑같이 FAILED\n" +
                "override suspend fun doWork(): Result = Result.failure()"
        )
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "둘 다 넣어 본 뒤 아래 5번 표에서 상태와 발화 횟수를 나란히 보면, 상태만으로는 구분되지 않는다는 " +
                "것이 바로 드러난다."
        )
    }
}

// ==================== 4. 생성 실패: 감싸진 예외와 그대로 오는 예외 ====================

@Composable
private fun InitializationCard(onEnqueue: (DemoScenario) -> Unit) {
    SectionCard(title = "4. 생성 실패 — doWork() 에 들어가 보지도 못한 경우") {
        BodyText(
            "워커는 기본 팩토리가 리플렉션으로 만든다. 그 단계에서 실패하면 실행 핸들러가 아니라 " +
                "WorkerInitialization 핸들러가 불린다. 커스텀 WorkerFactory 없이 생성자에 의존성을 " +
                "추가했을 때 실제로 만나는 실패다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScenarioButton(
                scenario = DemoScenario.InitThrow,
                containerColor = Color(0xFFAD1457),
                onEnqueue = onEnqueue
            )
            ScenarioButton(
                scenario = DemoScenario.InitSignature,
                containerColor = Color(0xFF6A1B9A),
                onEnqueue = onEnqueue
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("실패 지점", "핸들러가 받는 Throwable", isHeader = true)
        TableRow("생성자 본문에서 throw", "InvocationTargetException (실제 원인은 cause)")
        TableRow("(Context, WorkerParameters) 없음", "NoSuchMethodException (감싸지지 않는다)")
        TableRow("클래스 자체를 못 찾음", "ClassNotFoundException (감싸지지 않는다)")
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "WorkerFactory.createWorkerWithDefaultFallback 은 Class.forName → getDeclaredConstructor → " +
                "newInstance 순으로 진행하고, 각 단계의 Throwable 을 로그로 남긴 뒤 그대로 다시 던진다. " +
                "newInstance 만 JDK 규약상 InvocationTargetException 으로 감싸므로 cause 를 한 번 더 봐야 한다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "실측에서 이 차이가 더 날카롭게 드러났다 — InvocationTargetException 의 message 는 null 이다. " +
                "리포팅에 throwable.message 만 실으면 ③은 빈 문자열만 남는다. 반대로 ④의 " +
                "NoSuchMethodException 은 찾던 시그니처를 메시지에 그대로 담아 준다."
        )
    }
}

@Composable
private fun ScenarioButton(
    scenario: DemoScenario,
    containerColor: Color,
    onEnqueue: (DemoScenario) -> Unit
) {
    Button(
        onClick = { onEnqueue(scenario) },
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = scenario.buttonText, fontSize = 12.sp, color = Color.White)
    }
}

// ==================== 5. 상태 대조 ====================

@Composable
private fun ComparisonCard(
    workInfos: List<WorkInfo>,
    reports: List<WorkerExceptionReport>
) {
    SectionCard(title = "5. 같은 FAILED, 다른 관측 — 시나리오별 대조") {
        BodyText(
            "왼쪽은 WorkInfo 가 보여 주는 것(상태·시도 횟수), 오른쪽은 핸들러가 남긴 것이다. " +
                "②만 핸들러 기록이 비어 있고 상태는 ①과 같다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("시나리오", "WorkInfo 상태 / 핸들러 발화", isHeader = true)
        DemoScenario.entries.forEach { scenario ->
            val info = workInfos
                .filter { workInfo -> scenario.id in workInfo.tags }
                .maxByOrNull { workInfo -> workInfo.runAttemptCount }
            val firedCount = reports.count { report -> report.scenario == scenario.label }

            TableRow(
                first = scenario.label,
                second = buildString {
                    append(info?.state?.name ?: "미실행")
                    append(" · 시도 ")
                    append(info?.runAttemptCount ?: 0)
                    append(" / ")
                    append(if (firedCount > 0) "발화 ${firedCount}건" else "발화 없음")
                }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("시나리오", "기대값", isHeader = true)
        DemoScenario.entries.forEach { scenario ->
            TableRow(
                first = scenario.label,
                second = "${scenario.expectedHandler} · ${scenario.expectedThrowable}"
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "넷 다 결과는 FAILED 다 — 예외가 전파돼도 재시도(RETRY)로 바뀌지 않는다. " +
                "재시도를 원하면 워커가 직접 Result.retry() 를 돌려줘야 하고, 핸들러는 그 결정에 관여하지 못한다. " +
                "단 시도 횟수는 갈린다 — 실행까지 간 ①②는 1로 끝나고, 생성 단계에서 죽은 ③④는 0에 머문다."
        )
    }
}

// ==================== 6. 실기기 계측 ====================

@Composable
private fun MeasurementCard() {
    SectionCard(title = "6. 실기기 계측 결과") {
        BodyText(
            "SM-A725F / Android 13 에서 work-runtime 2.11.2 로 네 시나리오를 한 번씩 넣어 측정한 값이다. " +
                "화면에서 직접 넣어도 같은 결과가 나온다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("시나리오", "핸들러 / 받은 Throwable", isHeader = true)
        TableRow("① 실행 중 throw", "Execution · IllegalStateException(메시지 그대로)")
        TableRow("② Result.failure()", "발화 없음 — 기록이 남지 않는다")
        TableRow("③ 생성자 throw", "Initialization · InvocationTargetException(message=null) → cause=IllegalArgumentException")
        TableRow("④ 시그니처 불일치", "Initialization · NoSuchMethodException(\"…<init> [class android.content.Context, class androidx.work.WorkerParameters]\")")
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("관측 항목", "측정값", isHeader = true)
        TableRow("리포트 수", "4회 실행 → 3건(②가 빠진다)")
        TableRow("WorkInfo 상태", "네 시나리오 모두 FAILED")
        TableRow("종료 후 시도 횟수", "①② = 1 / ③④ = 0 — 생성 단계에서 죽으면 올라가지 않는다")
        TableRow("핸들러가 본 runAttemptCount", "넷 다 0 — 실행 전 값이 그대로 담긴다")
        TableRow("호출 스레드", "WM.task-2 / WM.task-3 — 메인 스레드가 아니다")
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "여기서 리포팅 코드가 걸리기 쉬운 지점이 하나 확인된다 — ③의 throwable.message 는 null 이라, " +
                "메시지만 수집하면 정작 원인인 \"의존성 주입 실패\"가 통째로 사라진다. cause 까지 함께 실어야 한다."
        )
    }
}

// ==================== 7. 핸들러가 실제로 받은 것 ====================

@Composable
private fun ReportListCard(reports: List<WorkerExceptionReport>) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    SectionCard(title = "7. 핸들러가 받은 WorkerExceptionInfo (최근 ${reports.size}건)") {
        if (reports.isEmpty()) {
            BodyText("아직 기록이 없다. 3번·4번 카드의 버튼으로 실패를 넣어 보면 여기에 쌓인다.")
        } else {
            reports.forEach { report ->
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "${report.kind.firedWhen} · ${report.scenario}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ResultRow("handler", report.kind.handlerName)
                    ResultRow("worker", report.workerClassName)
                    ResultRow(
                        "throwable",
                        "${report.throwableName}: ${report.throwableMessage ?: "(메시지 없음)"}"
                    )
                    if (report.causeName != null) {
                        ResultRow(
                            "cause",
                            "${report.causeName}: ${report.causeMessage ?: "(메시지 없음)"}"
                        )
                    }
                    ResultRow("runAttempt", report.runAttemptCount.toString())
                    ResultRow("tags", report.tags.joinToString(", "))
                    ResultRow("thread", report.threadName)
                    ResultRow("time", timeFormat.format(Date(report.recordedAt)))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { WorkerExceptionReporter.clear() },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "기록 비우기", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "thread 값이 메인 스레드가 아닌 것에 주목 — 핸들러는 WorkManager 의 실행기에서 불린다. " +
                "여기서 UI 를 직접 만지면 안 되고, 이 예제처럼 StateFlow 로 넘겨야 한다."
        )
    }
}

// ==================== 8. 함정 ====================

@Composable
private fun PitfallCard() {
    SectionCard(title = "8. 함정") {
        PitfallRow(
            title = "핸들러에서 던진 예외는 조용히 사라진다",
            description = "WorkManager 가 safeAccept 로 감싸 호출하고, 예외가 나면 " +
                "\"Exception handler threw an exception\" 로그만 남긴다. 핸들러 안에서 실패하면 " +
                "리포팅이 통째로 유실되므로, 무거운 작업이나 예외 가능성이 있는 코드를 넣지 말 것."
        )
        PitfallRow(
            title = "실패를 되돌리는 수단이 아니다",
            description = "핸들러가 호출되는 시점에는 이미 Resolution.Failed 가 정해져 있다. " +
                "Result 를 바꾸거나 재시도를 유도할 수 없고, 관측 전용이다."
        )
        PitfallRow(
            title = "Result.failure() 로 감싸면 안 보인다",
            description = "doWork() 를 try-catch 로 감싸 Result.failure() 를 돌려주는 흔한 패턴은 " +
                "예외가 밖으로 나가지 않으므로 핸들러도 부르지 않는다. 기존 WorkManager 예제의 " +
                "BackGroundWorker 도 catch 후 Result.retry() 를 쓴다 — 관측하려면 예외를 다시 던지거나 " +
                "핸들러 대신 catch 블록에서 직접 리포팅해야 한다."
        )
        PitfallRow(
            title = "기본 초기화를 지우지 않으면 설정이 무시된다",
            description = "Configuration.Provider 만 구현하고 매니페스트를 그대로 두면 androidx.startup " +
                "초기화가 먼저 끝나 버려 핸들러가 등록되지 않는다. 증상이 \"콜백이 한 번도 안 불린다\"뿐이라 " +
                "원인을 찾기 어렵다."
        )
        PitfallRow(
            title = "화면과 수명이 다르다",
            description = "핸들러는 앱 전역에 등록돼 있어 이 화면을 닫아도 계속 호출된다. " +
                "이 예제의 수집기가 object 인 이유이기도 하다 — 화면이 살아 있을 때만 모으려면 " +
                "수집기 쪽에서 별도 수명 관리가 필요하다."
        )
        PitfallRow(
            title = "릴리즈 빌드의 난독화",
            description = "workerClassName 은 문자열이라 R8 이 클래스를 리네임하면 리포트도 리네임된 " +
                "이름으로 남는다(이 프로젝트는 minifyEnabled false 라 그대로 보인다)."
        )
    }
}

// ==================== 9. 정리 ====================

@Composable
private fun SummaryCard() {
    SectionCard(title = "9. 정리") {
        TableRow("질문", "답", isHeader = true)
        TableRow("언제 불리나", "예외가 워커 밖으로 나갔을 때만 — 생성 단계면 Initialization, 실행 단계면 Execution")
        TableRow("무엇을 받나", "WorkerExceptionInfo(workerClassName · workerParameters · throwable)")
        TableRow("어디에 등록하나", "Application 의 Configuration.Provider + 매니페스트 기본 초기화 제거")
        TableRow("결과가 바뀌나", "아니다. 이미 FAILED 로 정해진 뒤 불린다")
        TableRow("어디에 쓰나", "워커 실패를 크래시 리포팅/로깅 도구로 올려 사각지대를 없애는 용도")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "정리하면, 이 핸들러는 \"작업이 실패했다\"가 아니라 \"작업이 예외로 죽었다\"를 잡는 자리다. " +
                "둘을 구분해서 보고 싶을 때 WorkInfo 만으로는 부족하다는 것이 도입 이유다."
        )
    }
}

// ==================== 공통 컴포넌트 ====================

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
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
private fun TableRow(
    first: String,
    second: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = first,
            modifier = Modifier.width(128.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(88.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = description, fontSize = 11.sp, color = Color(0xFF616161), lineHeight = 16.sp)
    }
}
