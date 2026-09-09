package com.example.composesample.presentation.example.component.architecture.development.concurrency

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.CopyableThrowable
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 코루틴 스택 트레이스 복구 예제
 * - suspend 경계를 넘어 던져진 예외의 트레이스에는 **호출자 프레임이 없다**. 재개는 호출자의 자바 스택이
 *   이미 사라진 뒤에 일어나기 때문이다. 이 화면은 그 사실과 kotlinx.coroutines 의 복구 동작을 모두 실측한다.
 * - 복구 스위치(DEBUG)는 assertion 활성 여부를 따라가므로 **안드로이드에서는 기본적으로 꺼져 있다**.
 *   화면이 그 근거(desiredAssertionStatus·시스템 프로퍼티)를 직접 읽어 보여준다.
 * - 예외에 필드가 하나라도 있으면 반사 복사가 통째로 스킵된다는 규칙과, 그것을 되살리는
 *   CopyableThrowable 을 같은 호출 경로로 나란히 비교한다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "Coroutine Stack Trace Recovery" 섹션 참고.
 */

// ==================== 라이브러리가 쓰는 이름 (바이트코드로 확인한 값) ====================

/** kotlinx.coroutines 의 DEBUG_PROPERTY_NAME 과 같은 값. 라이브러리 상수는 internal 이라 직접 참조할 수 없다. */
private const val DEBUG_PROPERTY_NAME = "kotlinx.coroutines.debug"

/** kotlinx.coroutines 의 STACKTRACE_RECOVERY_PROPERTY_NAME 과 같은 값. */
private const val STACKTRACE_RECOVERY_PROPERTY_NAME = "kotlinx.coroutines.stacktrace.recovery"

/** 복구가 끼워 넣는 인공 프레임의 패키지명. 실제 출력은 `_COROUTINE._BOUNDARY._(CoroutineDebugging.kt:42)` 형태다. */
private const val ARTIFICIAL_FRAME_PACKAGE = "_COROUTINE"

/** 3번 카드에서 "호출자가 트레이스에 있는지" 판단할 때 찾는 함수 이름. */
private const val CALLER_MARKER = "runScenario"

/** 4번 카드에서 같은 판단을 할 때 찾는 함수 이름(캡처를 수행하는 함수 자신). */
private const val MANUAL_MARKER = "runManualRecovery"

// ==================== 데모용 예외 3종 ====================

/**
 * 추가 필드가 없는 예외.
 * 라이브러리는 `Throwable` 이 선언한 필드 수와 예외 클래스의 필드 수가 같을 때만 반사 복사를 시도한다.
 */
private class PlainFailure(message: String) : RuntimeException(message)

/**
 * 필드가 하나 있는 예외.
 * `(String)` 생성자가 있어도 **필드가 하나 늘었다는 이유만으로** 반사 복사가 통째로 스킵된다.
 */
private class TaggedFailure(message: String, val requestId: String) : RuntimeException(message)

/**
 * 복사 방법을 직접 제공하는 예외.
 * 필드가 있어도 `createCopy()` 가 있으면 라이브러리는 그것을 먼저 쓴다 → 필드를 유지한 채 복구된다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private class CopyableFailure(
    message: String,
    val requestId: String
) : RuntimeException(message), CopyableThrowable<CopyableFailure> {

    // 원본을 cause 로 달아 두는 것이 핵심이다. 복구된 트레이스는 "요약본",
    // cause 에 달린 원본은 "실제로 터진 자리"를 담는다.
    override fun createCopy(): CopyableFailure =
        CopyableFailure(message ?: "", requestId).also { it.initCause(this) }
}

private enum class FailureKind(val label: String, val note: String) {
    PLAIN("필드 없는 예외", "추가 필드가 없어 반사 복사가 가능하다"),
    TAGGED("필드 1개 예외", "requestId 하나 때문에 반사 복사가 스킵된다"),
    COPYABLE("CopyableThrowable", "복사 방법을 직접 제공해 필드를 유지한다")
}

// ==================== 예외를 던지는 suspend 호출 체인 ====================

/** 던진 인스턴스를 기억해 두었다가, 잡은 인스턴스와 같은 객체인지(=복사되지 않았는지) 비교한다. */
private class ThrowRecord {
    var identity: Int = 0
}

private fun failWith(kind: FailureKind, record: ThrowRecord): Nothing {
    val failure = when (kind) {
        FailureKind.PLAIN -> PlainFailure("프로필 파싱 실패")
        FailureKind.TAGGED -> TaggedFailure("프로필 파싱 실패", "req-42")
        FailureKind.COPYABLE -> CopyableFailure("프로필 파싱 실패", "req-42")
    }
    record.identity = System.identityHashCode(failure)
    throw failure
}

private suspend fun parsePayload(kind: FailureKind, record: ThrowRecord): Nothing =
    withContext<Nothing>(Dispatchers.Default) { failWith(kind, record) }

private suspend fun fetchRemote(kind: FailureKind, record: ThrowRecord): Nothing =
    withContext<Nothing>(Dispatchers.IO) { parsePayload(kind, record) }

private suspend fun loadProfile(kind: FailureKind, record: ThrowRecord): Nothing =
    fetchRemote(kind, record)

// ==================== 측정 결과 ====================

private data class ScenarioResult(
    val kind: FailureKind,
    val copied: Boolean,
    val causeAttached: Boolean,
    val fieldKept: String,
    val artificialFrames: Int,
    val totalFrames: Int,
    val callerVisible: Boolean,
    val topFrames: List<String>
)

private data class SwitchState(
    val assertionsEnabled: Boolean,
    val debugProperty: String?,
    val recoveryProperty: String?,
    val artificialFramesBefore: Int,
    val artificialFramesAfter: Int
)

private data class ManualRecoveryResult(
    val exceptionHasCaller: Boolean,
    val entryHasCaller: Boolean,
    val recoveredHasCaller: Boolean,
    val artificialFrames: Int,
    val exceptionTop: List<String>,
    val entryTop: List<String>,
    val recoveredTop: List<String>
)

// ==================== 측정 로직 ====================

/**
 * 같은 호출 체인으로 예외를 던지고, 잡은 예외가 어떤 상태인지 센다.
 * 프레임 수는 호출 체인마다 달라지므로 값을 적어 두지 않고 화면이 매번 직접 센다.
 */
private suspend fun runScenario(kind: FailureKind): ScenarioResult {
    val record = ThrowRecord()
    return try {
        loadProfile(kind, record)
    } catch (throwable: Throwable) {
        val trace = throwable.stackTrace
        ScenarioResult(
            kind = kind,
            // 복구가 동작하면 잡히는 것은 원본이 아니라 "복사본"이다.
            copied = System.identityHashCode(throwable) != record.identity,
            causeAttached = throwable.cause != null,
            fieldKept = when (throwable) {
                is TaggedFailure -> throwable.requestId
                is CopyableFailure -> throwable.requestId
                else -> "-"
            },
            artificialFrames = trace.count { it.className.startsWith(ARTIFICIAL_FRAME_PACKAGE) },
            totalFrames = trace.size,
            callerVisible = trace.any { it.toString().contains(CALLER_MARKER) },
            topFrames = trace.take(7).map { it.shorten() }
        )
    }
}

/**
 * 복구 스위치의 근거를 직접 읽는다.
 *
 * 라이브러리 판정식(바이트코드 확인):
 * `DEBUG = when(prop) { null, "auto" -> assertions; "", "on" -> true; "off" -> false }`,
 * `RECOVER_STACK_TRACES = DEBUG && systemProp("kotlinx.coroutines.stacktrace.recovery", true)`
 */
private suspend fun measureSwitch(): SwitchState {
    val before = runScenario(FailureKind.PLAIN).artificialFrames

    // 지금 프로퍼티를 켜 봐도 소용이 없다 — DEBUG 는 클래스 최초 로드 때 한 번 계산된 val 이고,
    // 코루틴은 이 화면보다 훨씬 먼저 로드됐다. 그 사실을 값으로 확인한다.
    val previous = System.getProperty(DEBUG_PROPERTY_NAME)
    val after = try {
        System.setProperty(DEBUG_PROPERTY_NAME, "on")
        runScenario(FailureKind.PLAIN).artificialFrames
    } finally {
        // 다른 예제에 전역 상태를 남기지 않도록 되돌린다.
        if (previous == null) System.clearProperty(DEBUG_PROPERTY_NAME)
        else System.setProperty(DEBUG_PROPERTY_NAME, previous)
    }

    return SwitchState(
        // 라이브러리는 internal 인 CoroutineId 로 읽는다. 같은 패키지의 공개 클래스로 같은 값을 확인한다.
        assertionsEnabled = CoroutineName::class.java.desiredAssertionStatus(),
        debugProperty = previous,
        recoveryProperty = System.getProperty(STACKTRACE_RECOVERY_PROPERTY_NAME),
        artificialFramesBefore = before,
        artificialFramesAfter = after
    )
}

/**
 * 라이브러리가 하는 일을 공개 API 만으로 직접 재현한다.
 *
 * 복구는 결국 "예외 복사 + 호출자 스택 이어 붙이기 + 경계 표시"다. 라이브러리는 호출자 스택을
 * Continuation 체인에서 만들지만, 여기서는 suspend 호출에 들어가기 전에 캡처해 대신한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun runManualRecovery(): ManualRecoveryResult {
    val record = ThrowRecord()
    // 호출자 스택은 예외가 던져진 뒤에는 만들 수 없다. 들어가기 전에 확보해 둔다.
    val entryTrace = Throwable().stackTrace
    return try {
        loadProfile(FailureKind.COPYABLE, record)
    } catch (throwable: Throwable) {
        val copy = (throwable as? CopyableThrowable<*>)?.createCopy() ?: throwable
        copy.stackTrace = throwable.stackTrace + boundaryFrame() + entryTrace

        ManualRecoveryResult(
            exceptionHasCaller = throwable.stackTrace.any { it.toString().contains(MANUAL_MARKER) },
            entryHasCaller = entryTrace.any { it.toString().contains(MANUAL_MARKER) },
            recoveredHasCaller = copy.stackTrace.any { it.toString().contains(MANUAL_MARKER) },
            artificialFrames = copy.stackTrace.count { it.className.startsWith(ARTIFICIAL_FRAME_PACKAGE) },
            exceptionTop = throwable.stackTrace.take(5).map { it.shorten() },
            entryTop = entryTrace.take(3).map { it.shorten() },
            recoveredTop = copy.stackTrace.take(9).map { it.shorten() }
        )
    }
}

/**
 * 라이브러리와 같은 규칙으로 경계 프레임을 만든다.
 * (바이트코드 확인: className = "_COROUTINE.<이름>", methodName = "_", 파일/줄은 프레임 하나에서 복사)
 */
private fun boundaryFrame(): StackTraceElement {
    val here = Throwable().stackTrace[0]
    return StackTraceElement("$ARTIFICIAL_FRAME_PACKAGE._BOUNDARY", "_", here.fileName, here.lineNumber)
}

/** 화면 폭에 맞게 패키지명을 줄인다. */
private fun StackTraceElement.shorten(): String {
    val simplified = className
        .replace("com.example.composesample.presentation.example.component.architecture.development.concurrency.", "")
        .replace("kotlinx.coroutines.", "kx.")
        .replace("kotlin.coroutines.jvm.internal.", "kjvm.")
        .replace("androidx.compose.", "compose.")
    return "$simplified.$methodName($fileName:$lineNumber)"
}

// ==================== 화면 ====================

@Composable
fun CoroutineStackTraceExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "코루틴 스택 트레이스 복구",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { WhyTruncatedCard() }
            item { SwitchCard() }
            item { ScenarioCard() }
            item { ManualRecoveryCard() }
            item { CopyableGuideCard() }
            item { PitfallCard() }
        }
    }
}

// ==================== 1. 왜 잘리는가 ====================

@Composable
private fun WhyTruncatedCard() {
    SectionCard(title = "1. suspend 경계에서 무엇이 사라지나") {
        BodyText(
            "일반 함수는 호출자가 자바 스택에 그대로 쌓여 있어서, 예외가 터지면 그 스택을 그대로 찍으면 된다. " +
                "suspend 함수는 다르다 — 중단되는 순간 호출자의 자바 프레임은 정리되고, 남는 것은 Continuation 객체뿐이다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("구분", "예외가 던져질 때 스택에 남아 있는 것", isHeader = true)
        TableRow("일반 함수", "호출자 → 피호출자가 그대로 쌓여 있다")
        TableRow("suspend", "재개시킨 스레드의 프레임 + 디스패처 기계(DispatchedTask.run 등)")
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "그래서 기본 트레이스는 \"어디서 터졌는지\"는 알려주지만 \"누가 불렀는지\"는 알려주지 못한다. " +
                "kotlinx.coroutines 의 stack trace recovery 는 예외를 복사한 뒤, Continuation 체인을 거슬러 올라가 " +
                "만든 호출자 스택을 그 뒤에 이어 붙이고 경계에 인공 프레임 하나를 끼워 넣는다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock("_COROUTINE._BOUNDARY._(CoroutineDebugging.kt:42)")
        CaptionText("복구가 켜진 JVM 에서 실제로 찍히는 인공 프레임. 이 줄 아래부터가 되살린 호출자 쪽이다.")
    }
}

// ==================== 2. 스위치 실측 ====================

@Composable
private fun SwitchCard() {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<SwitchState?>(null) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "2. 이 기기에서 복구는 켜져 있나") {
        BodyText(
            "복구 여부는 라이브러리의 DEBUG 값이 정한다. DEBUG 는 시스템 프로퍼티가 없거나 auto 이면 " +
                "**assertion 활성 여부**를 그대로 따라간다. JVM 은 -ea 로 켜지만 안드로이드 런타임은 기본이 꺼짐이다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        DemoButton(text = if (running) "측정 중…" else "스위치 상태 측정", color = Color(0xFF3949AB)) {
            if (!running) {
                running = true
                scope.launch {
                    state = measureSwitch()
                    running = false
                }
            }
        }

        state?.let { measured ->
            Spacer(modifier = Modifier.height(10.dp))
            ResultRow("assertion", if (measured.assertionsEnabled) "활성" else "비활성")
            ResultRow(DEBUG_PROPERTY_NAME.substringAfterLast('.'), measured.debugProperty ?: "미설정(null)")
            ResultRow("recovery", measured.recoveryProperty ?: "미설정(null)")
            ResultRow(
                "판정",
                if (measured.assertionsEnabled) "DEBUG=on → 복구 동작" else "DEBUG=off → 복구 미동작"
            )
            Spacer(modifier = Modifier.height(8.dp))
            ResultRow("인공 프레임", "설정 전 ${measured.artificialFramesBefore}개 → 프로퍼티 on 후 ${measured.artificialFramesAfter}개")
            Spacer(modifier = Modifier.height(8.dp))
            BodyText(
                if (measured.artificialFramesBefore == measured.artificialFramesAfter) {
                    "프로퍼티를 지금 on 으로 바꿔도 숫자가 그대로다. DEBUG 는 클래스가 처음 로드될 때 한 번 계산되는 val 이고, " +
                        "코루틴은 이 화면보다 한참 먼저 로드됐다 — **이미 늦었다**. 켜려면 코루틴 클래스가 로드되기 전에 " +
                        "(Application 진입 지점 등에서) 프로퍼티를 설정하거나, JVM 이라면 -ea 를 준다."
                } else {
                    "설정 전후 숫자가 달라졌다면 이 런타임에서는 DEBUG 가 아직 계산되지 않았던 것이다."
                }
            )
            CaptionText("측정이 끝나면 프로퍼티는 원래 값으로 되돌린다(다른 예제에 전역 상태를 남기지 않기 위해).")
        }
    }
}

// ==================== 3. 예외 3종 대조 ====================

@Composable
private fun ScenarioCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<ScenarioResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "3. 같은 경로로 세 가지 예외 던져 보기") {
        BodyText(
            "loadProfile → fetchRemote(withContext IO) → parsePayload(withContext Default) 로 두 번 디스패치한 뒤 예외를 던진다. " +
                "세 예외의 차이는 **필드 유무와 복사 방법 제공 여부**뿐이다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        FailureKind.entries.forEach { kind ->
            PitfallRow(kind.label, kind.note)
        }

        DemoButton(text = if (running) "실행 중…" else "3종 실행", color = Color(0xFF00897B)) {
            if (!running) {
                running = true
                scope.launch {
                    results = FailureKind.entries.map { runScenario(it) }
                    running = false
                }
            }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            results.forEach { result ->
                Text(
                    text = result.kind.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F)
                )
                ResultRow("복사됨", if (result.copied) "예(복사본을 잡았다)" else "아니오(원본 그대로)")
                ResultRow("cause", if (result.causeAttached) "원본이 달려 있다" else "없음")
                ResultRow("필드", result.fieldKept)
                ResultRow("인공 프레임", "${result.artificialFrames}개 / 총 ${result.totalFrames}프레임")
                ResultRow("호출자", if (result.callerVisible) "트레이스에 있다" else "사라졌다")
                CodeBlock(result.topFrames.joinToString("\n") { "at $it" })
                Spacer(modifier = Modifier.height(10.dp))
            }
            BodyText(
                "복구가 꺼진 런타임에서는 세 줄이 모두 같아진다 — 복사도, cause 도, 인공 프레임도 없다. " +
                    "차이가 드러나는 것은 복구가 켜진 런타임이다."
            )
            Spacer(modifier = Modifier.height(10.dp))
            TableRow("예외", "복구 ON(JVM, assertion 활성)에서의 결과", isHeader = true)
            TableRow("필드 없음", "복사됨 · cause 연결 · _COROUTINE 프레임 생김")
            TableRow("필드 1개", "복사 스킵 · cause 없음 · 인공 프레임 없음")
            TableRow("Copyable", "복사됨 · cause 연결 · 필드(req-42) 유지")
            CaptionText(
                "위 표는 같은 코드를 assertion 이 켜진 JVM 단위 테스트에서 돌려 확인한 결과다. " +
                    "프레임 수는 호출 체인마다 달라지므로 적어 두지 않고 화면이 직접 센다."
            )
        }
    }
}

// ==================== 4. 복구 직접 재현 ====================

@Composable
private fun ManualRecoveryCard() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<ManualRecoveryResult?>(null) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "4. 복구를 직접 만들어 보기") {
        BodyText(
            "복구가 꺼진 기기에서도 원리는 확인할 수 있다. 복구가 하는 일은 세 가지뿐이다 — " +
                "예외를 복사하고, 호출자 스택을 이어 붙이고, 그 경계에 표식을 하나 남긴다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        DemoButton(text = if (running) "실행 중…" else "직접 재현", color = Color(0xFF6D4C41)) {
            if (!running) {
                running = true
                scope.launch {
                    result = runManualRecovery()
                    running = false
                }
            }
        }

        result?.let { measured ->
            Spacer(modifier = Modifier.height(10.dp))
            ResultRow("예외 트레이스", if (measured.exceptionHasCaller) "호출자 있음" else "호출자 없음")
            CodeBlock(measured.exceptionTop.joinToString("\n") { "at $it" })
            CaptionText("던져진 자리에서 위로 올라가다 디스패처 기계에서 끝난다. 이 예외를 누가 기다리고 있었는지는 없다.")
            Spacer(modifier = Modifier.height(10.dp))
            ResultRow("진입 시점", if (measured.entryHasCaller) "호출자 있음" else "호출자 없음")
            CodeBlock(measured.entryTop.joinToString("\n") { "at $it" })
            CaptionText("suspend 호출에 들어가기 전에 캡처해 둔 스택. 라이브러리는 이것을 Continuation 체인에서 만들어 낸다.")
            Spacer(modifier = Modifier.height(10.dp))
            ResultRow("병합 결과", "인공 프레임 ${measured.artificialFrames}개 · 호출자 ${if (measured.recoveredHasCaller) "복원됨" else "없음"}")
            CodeBlock(measured.recoveredTop.joinToString("\n") { "at $it" })
            CaptionText(
                "createCopy() 로 복사본을 만들고 setStackTrace 로 이어 붙였다. 경계 프레임은 라이브러리와 같은 규칙" +
                    "(_COROUTINE._BOUNDARY / 메서드명 \"_\")으로 만들었고, 파일·줄은 이 파일의 생성 위치를 가리킨다."
            )
        }
    }
}

// ==================== 5. CopyableThrowable 구현법 ====================

@Composable
private fun CopyableGuideCard() {
    SectionCard(title = "5. 왜 CopyableThrowable 이 필요한가") {
        BodyText(
            "복구는 예외를 복사해야 성립한다(원본은 cause 로 보존된다). 라이브러리는 먼저 createCopy() 를 찾고, " +
                "없으면 생성자를 반사로 찾아 복사한다. 그런데 반사 복사에는 앞단에 조건이 하나 더 있다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "// 라이브러리 내부 판정(바이트코드 확인)\n" +
                "if (throwableFields != fieldsCountOrDefault(clazz, 0)) return null // 복사 포기\n" +
                "// Throwable 이 가진 필드 수와 다르면 = 필드를 하나라도 더 선언했으면 반사 복사를 하지 않는다"
        )
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "즉 requestId 같은 필드를 하나 붙이는 순간, 생성자 모양과 무관하게 복구 대상에서 빠진다. " +
                "도메인 예외일수록 필드를 갖기 마련이라 **정작 필요한 예외가 복구되지 않는다**. 그 구멍을 메우는 것이 CopyableThrowable 이다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "@OptIn(ExperimentalCoroutinesApi::class)\n" +
                "class ApiFailure(message: String, val requestId: String) :\n" +
                "    RuntimeException(message), CopyableThrowable<ApiFailure> {\n" +
                "    override fun createCopy(): ApiFailure =\n" +
                "        ApiFailure(message ?: \"\", requestId).also { it.initCause(this) }\n" +
                "}"
        )
        CaptionText("initCause(this) 를 빼면 복사본만 남아 \"실제로 터진 자리\"를 잃는다.")
    }
}

// ==================== 6. 함정 ====================

@Composable
private fun PitfallCard() {
    SectionCard(title = "6. 걸리는 것들") {
        PitfallRow(
            "안드로이드에서는 기본적으로 꺼져 있다",
            "DEBUG 는 assertion 활성 여부를 따라가는데 안드로이드 런타임은 assertion 이 꺼져 있다. " +
                "디버그 빌드라고 켜지는 것이 아니다 — 2번 카드가 그 값을 직접 읽어 보여준다."
        )
        PitfallRow(
            "런타임에 프로퍼티를 바꿔도 늦다",
            "DEBUG/RECOVER_STACK_TRACES 는 클래스 최초 로드 때 한 번 계산되는 val 이다. " +
                "코루틴을 한 번이라도 쓴 뒤에 System.setProperty 를 불러 봐야 아무 일도 일어나지 않는다."
        )
        PitfallRow(
            "잡은 예외가 원본이 아닐 수 있다",
            "복구가 켜지면 catch 로 들어오는 것은 복사본이다. 참조 동일성(===)이나 인스턴스 캐싱을 전제로 한 코드는 깨진다. " +
                "원본은 cause 에 있다."
        )
        PitfallRow(
            "필드를 하나만 더해도 복구에서 빠진다",
            "반사 복사는 Throwable 과 필드 수가 같을 때만 시도된다. 필드가 있는 도메인 예외는 " +
                "CopyableThrowable 을 구현해야 복구 대상이 된다."
        )
        PitfallRow(
            "_COROUTINE 프레임은 진짜 코드가 아니다",
            "`_COROUTINE._BOUNDARY._` 는 파일·줄까지 있지만 실행된 적 없는 표식이다. " +
                "크래시 리포트를 그룹핑하거나 트레이스를 파싱하는 도구가 이 줄을 실제 프레임으로 오해할 수 있다."
        )
        PitfallRow(
            "공짜가 아니다",
            "복구는 예외마다 복사 + 스택 병합을 한다. 예외를 흐름 제어에 쓰는 코드에서는 비용이 눈에 띈다. " +
                "프로덕션에 켤 것이 아니라 디버깅할 때 켜는 스위치다(원하면 recovery 프로퍼티만 따로 끌 수도 있다)."
        )
    }
}

// ==================== 공통 요소 ====================

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

/** 트레이스는 줄이 길어 접히면 읽기 어렵다 → 가로 스크롤로 원문 모양을 유지한다. */
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
            modifier = Modifier.width(84.dp),
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
            modifier = Modifier.width(84.dp),
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
        CaptionText(description)
    }
}

@Composable
private fun DemoButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
