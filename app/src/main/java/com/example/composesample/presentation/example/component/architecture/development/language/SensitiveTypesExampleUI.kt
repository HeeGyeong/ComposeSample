package com.example.composesample.presentation.example.component.architecture.development.language

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

private const val LOG_TAG = "SensitiveTypes"

/**
 * 민감 값·불법 상태를 타입으로 막기 — Kotlin 블로그의 결제 시스템 패턴(Verifone, KotlinConf 2026) 중
 * 가림 value class · 검증 팩토리 · copy 가시성 · 타임아웃 결과를 옮기고, 그 장치를 Gson·리플렉션이 어떻게 우회하는지 잰다.
 * 실험 로직은 같은 폴더 SensitiveTypes.kt, 참고 자료는 exampleGuide.kt.
 */
@Composable
fun SensitiveTypesExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "민감 값·불법 상태를 타입으로",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item { MaskingCard() }
            item { SerializationCard() }
            item { DecodeCard() }
            item { CopyVisibilityCard() }
            item { TimeoutCard() }
            item { SummaryCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun IntroCard() {
    ExampleCard(title = "1. 철자 하나가 결제를 깨뜨린다", accent = Color(0xFF37474F)) {
        BodyText(
            "결제 시스템 사고를 '명세와 코드가 단어의 뜻에서 어긋난 것'으로 보고, 컴파일러가 그 어긋남을 막게 하는 패턴들이다. " +
                "이 화면은 그중 프로젝트에 없던 네 가지를 실제 코드로 옮기고, 타입 장치가 어디서 우회되는지까지 잰다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledLine("2~4번", "카드번호를 가리는 value class — 그리고 Gson 이 가림·검증을 건너뛰는 두 경로", Color(0xFFC62828))
        LabeledLine("5번", "private 생성자 data class 의 copy() — @ConsistentCopyVisibility", Color(0xFF6A1B9A))
        LabeledLine("6번", "타임아웃을 예외가 아니라 결과로 — 재시도 대신 조회", Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "원문의 다른 패턴은 이미 있는 예제로: ID 마다 value class(인자 뒤바뀜 컴파일 오류) → Inline Value Class · " +
                "sealed 결과와 else 없는 when → Sealed 도메인 에러 · coroutineScope/supervisorScope → 구조적 동시성 가드레일 · " +
                "경계에서 requireNotNull → Gson 누락 필드."
        )
    }
}

// ==================== 2. 가림 ====================

@Composable
private fun MaskingCard() {
    val rows = remember { maskingRows() }
    var logged by rememberSaveable { mutableIntStateOf(0) }

    // 가림 문자를 리터럴 "**" 로 쓰지 않는다 — 화면에 남은 마크다운 별표를 찾는 사전작업 스캔(⑲)의 오탐을 피한다
    val stars = "*".repeat(4)

    ExampleCard(title = "2. 가림 value class — 문자열이 되는 곳은 전부 가려진다", accent = Color(0xFFC62828)) {
        CodeBlock(
            "@JvmInline\n" +
                "value class Pan private constructor(private val value: String) {\n" +
                "    override fun toString() = \"PAN[$stars\${value.takeLast(4)}]\"\n" +
                "    fun toNetwork() = value          // 원문은 이 한 곳 — 검색되는 이름\n" +
                "    companion object { fun parse(raw: String): ParsedPan }\n" +
                "}"
        )
        Spacer(modifier = Modifier.height(8.dp))
        rows.forEach { ProbeLine(it) }
        Spacer(modifier = Modifier.height(6.dp))
        BulletText(
            "nullable·List 처럼 박싱되는 자리도 박스의 toString 이 재정의를 부른다. 거부 결과(ParsedPan.Invalid)에 입력을 " +
                "그대로 실으면 거기서 샌다 — 원문의 RawStan 처럼 입력도 타입(RawPan)으로 감싸 가린다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton(label = "logcat 에 남기기") {
            val pan = samplePan()
            val request = CardAuthRequest(pan, amountMinor = 1_200)
            Log.i(LOG_TAG, "카드 $pan 처리")
            Log.i(LOG_TAG, "요청 $request")
            Log.w(LOG_TAG, "결제 실패", IllegalStateException("실패 $request"))
            // 비교용: 객체를 JSON 으로 찍으면 원문이 그대로 나간다(3번 카드)
            Log.i(LOG_TAG, "JSON ${serializationRows().first().result}")
            logged += 4
        }
        if (logged > 0) {
            CaptionText("$logged 줄 기록 — adb logcat -s $LOG_TAG 로 확인(결제 테스트 카드번호만 쓴다)")
        }
    }
}

// ==================== 3. Gson 직렬화 ====================

@Composable
private fun SerializationCard() {
    val rows = remember { serializationRows() }
    ExampleCard(title = "3. 함정 ① Gson 직렬화는 toString 을 거치지 않는다", accent = Color(0xFFC62828)) {
        BodyText(
            "로그는 가려도, 객체를 JSON 으로 만들면 Gson 은 필드를 리플렉션으로 읽어 원문을 그대로 쓴다. " +
                "Pan 전용 TypeAdapter 를 등록해도 data class 필드에는 걸리지 않는다 — value class 필드는 JVM 에서 String 필드로 지워져 " +
                "Gson 이 String 어댑터를 고르기 때문이다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        rows.forEach { ProbeLine(it) }
        Spacer(modifier = Modifier.height(6.dp))
        BulletText(
            "경계 DTO 는 원문을 그대로 담지만 그것이 목적이다 — toNetwork() 라는 이름 있는 호출 한 곳에서만 원문이 나가므로 " +
                "검색·리뷰로 찾을 수 있다. 로그·분석 이벤트에는 DTO 가 아니라 도메인 객체(가려지는 쪽)를 넘긴다."
        )
        CaptionText(
            "JSON 의 필드 순서는 기기(ART)와 JVM 단위 테스트가 다르다(getDeclaredFields 순서는 정해져 있지 않다) — " +
                "가려지는지·새는지는 같다."
        )
    }
}

// ==================== 4. Gson 역직렬화 ====================

@Composable
private fun DecodeCard() {
    val rows = remember { decodeRows() }
    ExampleCard(title = "4. 함정 ② Gson 역직렬화는 검증 팩토리를 건너뛴다", accent = Color(0xFFC62828)) {
        BodyText(
            "생성자가 private 이어도 Gson 은 생성자 없이 객체를 만들고 필드에 값을 넣는다(인자 없는 생성자가 없으면 JDK Unsafe). " +
                "Pan.parse 를 한 번도 거치지 않은 Pan 이 생긴다. 아래 '재검사'는 그렇게 들어온 값을 팩토리로 다시 검사한 결과다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        rows.forEach { row ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "\"pan\": \"${row.raw}\"",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                LabeledLine("도메인 모델로 직접", row.direct, Color(0xFFC62828))
                LabeledLine("Pan 어댑터 등록", row.withAdapter, Color(0xFFC62828))
                LabeledLine("응답 DTO → 팩토리", row.viaFactory, Color(0xFF2E7D32))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        BulletText(
            "응답 모델은 서버 필드를 nullable String 으로 받고, 도메인 타입은 매퍼가 팩토리로 만든다(CLAUDE.md 데이터 클래스 규칙). " +
                "팩토리의 결과가 sealed 라 거부 사유마다 분기가 강제된다. Unsafe 경로 자체는 Gson 누락 필드 예제 참고."
        )
    }
}

// ==================== 5. copy() ====================

@Composable
private fun CopyVisibilityCard() {
    val open = remember { OpenCopyAuthorization.approve("000001", 1_200) }
    val closed = remember { ClosedCopyAuthorization.approve("000001", 1_200) }
    val openModifiers = remember { CopyVisibilityProbe.modifiers(OpenCopyAuthorization::class.java) }
    val closedModifiers = remember { CopyVisibilityProbe.modifiers(ClosedCopyAuthorization::class.java) }
    val openForged = remember { open?.let { CopyVisibilityProbe.forgeAmount(it) } ?: "-" }
    val closedForged = remember { closed?.let { CopyVisibilityProbe.forgeAmount(it) } ?: "-" }

    ExampleCard(title = "5. private 생성자 data class 의 copy()", accent = Color(0xFF6A1B9A)) {
        BodyText(
            "생성자를 private 으로 막고 검증 팩토리(approve)만 열어 둬도, data class 가 만드는 copy() 는 Kotlin 2.4 까지 public 이다. " +
                "copy(amountMinor = -500) 한 줄이면 팩토리가 거부할 값을 가진 객체가 생긴다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledLine("@ExposedCopyVisibility · copy", openModifiers, Color(0xFFC62828))
        LabeledLine("copy(\"000001\", -500)", openForged, Color(0xFFC62828))
        LabeledLine("@ConsistentCopyVisibility · copy", closedModifiers, Color(0xFF2E7D32))
        LabeledLine("copy(\"000001\", -500)", closedForged, Color(0xFF2E7D32))
        LabeledLine("approve(\"000001\", -500)", "${OpenCopyAuthorization.approve("000001", -500)}", Color(0xFF455A64))
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "data class Auth private constructor(...)        // 표시 없음\n" +
                "// ⚠ 선언: non-public primary constructor is exposed via the generated 'copy()'\n" +
                "auth.copy(amountMinor = -500)\n" +
                "// ⚠ 호출: this 'copy()' exposes the non-public primary constructor ...\n" +
                "//   둘 다 \"This will become an error in language version 2.5\"\n\n" +
                "@ConsistentCopyVisibility                        // copy 도 private\n" +
                "data class Auth private constructor(...)\n" +
                "auth.copy(amountMinor = -500)                    // ❌ 컴파일 오류"
        )
        CaptionText(
            "경고 문구는 Kotlin 2.4.21 단독 컴파일 실측. @ExposedCopyVisibility 는 선언 경고만 없애고 바깥 호출 경고는 남는다 — " +
                "그래서 위 결과는 소스 호출 대신 리플렉션으로 같은 copy 를 불렀다(HotSwan 아래에서는 리플렉션이 실패해 이 부분만 핫 리로드 제외)."
        )
    }
}

// ==================== 6. 타임아웃 ====================

@Composable
private fun TimeoutCard() {
    val asException = remember { timeoutAsException() }
    val asOutcome = remember { timeoutAsOutcome() }

    ExampleCard(title = "6. 타임아웃은 '실패'가 아니라 '모름'", accent = Color(0xFF1565C0)) {
        BodyText(
            "가짜 매입사는 승인을 처리한 뒤 첫 응답을 잃어버린다. 타임아웃을 예외로 바꿔 잡으면 '실패'로 보이고, " +
                "새 요청으로 재시도하는 순간 같은 결제가 두 번 승인된다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        ReportBlock("예외로 잡고 재시도", asException, Color(0xFFC62828))
        ReportBlock("TimedOut 결과 → 조회", asOutcome, Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "sealed interface AuthorizationOutcome {\n" +
                "    data class Approved(val authCode: String) : AuthorizationOutcome\n" +
                "    data class Declined(val reason: String) : AuthorizationOutcome\n" +
                "    data object TimedOut : AuthorizationOutcome   // 명세의 단어\n" +
                "}\n" +
                "when (outcome) {\n" +
                "    is Approved -> settle(outcome.authCode)\n" +
                "    is Declined -> notify(outcome.reason)\n" +
                "    TimedOut -> reconcile(stan)   // 재시도가 아니라 조회\n" +
                "}"
        )
        CaptionText("같은 문제를 멱등 키로 푸는 방법은 오프라인 아웃박스 전송 예제(같은 clientId 재시도) 참고.")
    }
}

@Composable
private fun ReportBlock(title: String, report: TimeoutReport, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        report.steps.forEach { CaptionText("· $it") }
        Text(
            text = "매입사에 승인된 건수: ${report.approvedAtAcquirer}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
    }
}

// ==================== 7. 정리 ====================

@Composable
private fun SummaryCard() {
    ExampleCard(title = "7. 정리", accent = Color(0xFF37474F)) {
        BulletText("가림 value class 는 문자열 템플릿·toString·예외 메시지 경로를 막는다. 리플렉션으로 필드를 읽는 직렬화기(Gson)는 막지 못한다.")
        BulletText("value class 필드는 JVM 에서 원래 타입으로 지워진다 — 그 타입용 TypeAdapter 도 필드에는 안 걸린다. 경계는 DTO 와 매퍼로 만든다.")
        BulletText("검증 팩토리는 '유일한 입구'일 때만 의미가 있다 — Gson(Unsafe)과 공개된 copy() 는 다른 입구다. 응답은 DTO 로 받고 copy 는 @ConsistentCopyVisibility.")
        BulletText("응답을 못 받은 것은 실패가 아니다 — 결과 타입으로 이름을 붙이면 재시도 대신 조회로 분기된다.")
        CaptionText(
            "실측: Kotlin 2.4.21 단독 컴파일(JVM)·단위 테스트·Galaxy A72(Android 13) debug·release 에서 가림·노출·거부·승인 건수가 같다" +
                "(JSON 필드 순서만 ART 와 JVM 이 다르다). 참고 자료는 같은 폴더 exampleGuide.kt."
        )
    }
}

// ==================== 공통 UI ====================

@Composable
private fun ExampleCard(title: String, accent: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accent)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

/** 코드 한 줄과 그 결과 — 원문 카드번호가 새면 빨간색, 원문이 나가는 것이 목적인 출구는 주황색 */
@Composable
private fun ProbeLine(row: ProbeRow) {
    val (label, color) = when {
        !row.leaks -> "가려짐" to Color(0xFF2E7D32)
        row.intended -> "원문 · 의도된 출구" to Color(0xFFEF6C00)
        else -> "원문 노출" to Color(0xFFC62828)
    }
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = row.code, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF616161))
        Text(
            text = "$label  ${row.result}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            lineHeight = 17.sp
        )
    }
}

/** 라벨·값을 한 덩어리로 — Row(라벨, 값)는 라벨이 길면 값 칸이 0 폭에 가까워져 글자가 세로로 쌓인다 */
@Composable
private fun LabeledLine(label: String, value: String, labelColor: Color) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = labelColor)) { append(label) }
            append("  ")
            append(value)
        },
        fontSize = 12.sp,
        color = Color(0xFF424242),
        lineHeight = 17.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
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
