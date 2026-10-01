package com.example.composesample.presentation.example.component.architecture.development.test

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.component.architecture.development.test.PowerAssertViewModel.Scenario
import org.koin.androidx.compose.koinViewModel

/**
 * Power-Assert — 단언이 실패하면 조건식의 중간값을 도식으로 펼쳐 주는 Kotlin 컴파일러 플러그인
 *
 * 같은 조건식을 평범한 함수(plainCheck)와 @PowerAssert 함수(powerCheck, Kotlin 2.4.20)에 넣어 실패 메시지를 비교하고,
 * 함수가 받는 CallExplanation 의 구조와 변환 규칙을 실측한다. 참고 자료와 핵심 개념은 같은 폴더의 exampleGuide.kt 참고.
 */
@Composable
fun PowerAssertExampleUI(
    onBackEvent: () -> Unit
) {
    val viewModel: PowerAssertViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Power-Assert Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { ComparisonCard(viewModel) }
            item { ExplanationCard(viewModel) }
            item { RulesCard(viewModel) }
            item { UnitTestCard() }
            item { TakeawayCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard() {
    SectionCard(
        title = "🔎 실패 메시지에 중간값을 펼친다",
        description = "컴파일러 플러그인이 단언 함수 호출부를 바꿔, 조건식의 각 부분이 어떤 값이었는지를 실패 메시지에 도식으로 넣는다. " +
            "소스를 고치지 않고 Gradle 설정만으로 켠다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        CodeBox(
            code = "plugins { alias(libs.plugins.kotlin.power.assert) }   // Kotlin 과 같은 버전\n\n" +
                "powerAssert {\n" +
                "    functions = [\"kotlin.assert\", \"kotlin.test.assertTrue\",\n" +
                "                 \"kotlin.test.assertEquals\", \"kotlin.test.assertNull\"]\n" +
                "    // 기본 TESTS 는 이름이 정확히 \"test\" 인 컴파일만 고른다 → 안드로이드엔 하나도 안 걸림\n" +
                "    compilationFilter = { it.name in [\"debug\", \"release\", \"debugUnitTest\",\n" +
                "                          \"releaseUnitTest\", \"debugAndroidTest\"] }\n" +
                "        as PowerAssertCompilationFilter\n" +
                "}"
        )
        Spacer(modifier = Modifier.height(10.dp))
        BulletText("변환 대상 함수는 마지막 파라미터로 String 이나 () -> String 을 받아야 한다")
        BulletText("2.4.20 신규: 함수에 @PowerAssert 를 붙이면 functions 에 적지 않아도 변환되고, 함수 안에서 PowerAssert.explanation 으로 식의 값들을 구조화된 형태로 받는다(실험 API)")
        BulletText("적용된 컴파일에는 kotlin-power-assert-runtime(27KB, stdlib 만 의존)이 자동으로 붙는다")
    }
}

// ==================== 2. plain vs power ====================

@Composable
private fun ComparisonCard(viewModel: PowerAssertViewModel) {
    val comparisons by viewModel.comparisons.collectAsStateWithLifecycle()

    SectionCard(
        title = "① 같은 조건, 두 함수",
        description = "plainCheck 와 powerCheck 는 본문이 같고 @PowerAssert 유무만 다르다. 실패했을 때 무엇을 알 수 있는지 비교한다."
    ) {
        RunButton("5가지 조건 실행") { viewModel.runComparisons() }
        Spacer(modifier = Modifier.height(10.dp))
        Scenario.entries.forEach { scenario ->
            val result = comparisons[scenario]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFECEFF1)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = scenario.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
                    MonoLine(scenario.code)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "plainCheck", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    DiagramBox(result?.plain ?: "…")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "powerCheck", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    DiagramBox(result?.power ?: "…")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "도식의 값 위치는 소스 텍스트 기준이라 고정폭 글꼴에서만 줄이 맞는다(칸은 가로로 스크롤). " +
                "&& 의 오른쪽처럼 평가되지 않은 부분에는 값이 찍히지 않고, @PowerAssert.Ignore 를 붙인 message 는 도식에서 빠진 채 맨 위에 붙는다."
        )
    }
}

// ==================== 3. CallExplanation ====================

@Composable
private fun ExplanationCard(viewModel: PowerAssertViewModel) {
    val lines by viewModel.explanationLines.collectAsStateWithLifecycle()

    SectionCard(
        title = "② 문자열이 아니라 값이 온다 — CallExplanation",
        description = "@PowerAssert 함수는 PowerAssert.explanation 으로 호출 소스·인자 구간·각 부분식의 값을 객체로 받는다. " +
            "toDefaultMessage() 가 위의 도식을 그리고, 값을 직접 꺼내 자기 형식의 메시지를 만들 수도 있다."
    ) {
        RunButton("합계 비교의 설명 풀어 보기") { viewModel.inspectExplanation() }
        Spacer(modifier = Modifier.height(8.dp))
        if (lines.isEmpty()) {
            MonoLine("아직 실행하지 않았다")
        } else {
            DiagramBox(lines.joinToString("\n"))
        }
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            code = "@PowerAssert\n" +
                "fun powerCheck(condition: Boolean, @PowerAssert.Ignore message: String? = null) {\n" +
                "    if (!condition) {\n" +
                "        val explanation = PowerAssert.explanation   // 변환 안 된 호출이면 null\n" +
                "        throw CheckFailure(explanation?.toDefaultMessage() ?: …, explanation)\n" +
                "    }\n" +
                "}"
        )
    }
}

// ==================== 4. 변환 규칙 ====================

@Composable
private fun RulesCard(viewModel: PowerAssertViewModel) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()

    SectionCard(
        title = "③ 변환 규칙 실측",
        description = "호출부가 어떻게 바뀌는지를 셀 수 있는 값으로 확인한다 — 평가 횟수, toString 호출 수, 설명이 null 이 되는 경로, 앱 코드의 assert."
    ) {
        RunButton("규칙 4가지 실행") { viewModel.runRules() }
        Spacer(modifier = Modifier.height(8.dp))
        rules.forEach { rule ->
            ResultRow(title = rule.title, lines = rule.lines)
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "컴파일러는 powerCheck 옆에 powerCheck\$powerassert(…, () -> CallExplanation) 를 만들고, 변환된 호출부는 이쪽을 부른다. " +
                "설명은 람다로 넘어가 실패할 때만 만들어지고, 원래 powerCheck 안의 PowerAssert.explanation 은 null 로 컴파일된다(release 바이트코드 확인)."
        )
        Spacer(modifier = Modifier.height(6.dp))
        NoteBox(
            "debug 의 핫 리로드(HotSwan)와 함께 쓸 때 두 가지가 깨진다 — @PowerAssert 함수 본문에서 PowerAssert.explanation 이 " +
                "NotImplementedError 를 던지고, 변환된 호출부의 식이 여러 번 평가된다(incrementAndGet 3회). 그래서 이 예제의 " +
                "PowerAssertChecksKt·PowerAssertViewModel 은 hotSwanCompiler { exclude(…) } 로 핫 리로드에서 뺐다(위 값은 그 상태의 실측)."
        )
    }
}

// ==================== 5. 단위 테스트 ====================

@Composable
private fun UnitTestCard() {
    SectionCard(
        title = "④ 기존 단위 테스트에 그대로 적용",
        description = "kotlin.test 의 assertEquals·assertTrue 를 functions 에 넣으면 테스트 코드를 고치지 않고 실패 메시지만 바뀐다. " +
            "PowerAssertExampleTest 가 변환 결과를 검증한다."
    ) {
        Text(text = "적용 전", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
        DiagramBox(UNIT_TEST_BEFORE)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "적용 후", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        DiagramBox(UNIT_TEST_AFTER)
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "kotlin.assert 는 kotlin._Assertions.ENABLED 일 때만 검사한다. 로컬 JVM 테스트는 단언이 켜진 채 돌고, 기기에서는 " +
                "debug 빌드가 던지고 release 는 건너뛴다(③ 실측 — desiredAssertionStatus() 는 둘 다 false). 그래서 계측 테스트(debugAndroidTest)의 " +
                "assert 도 필터에 넣어 도식을 받는다. release 에서도 반드시 검사해야 하는 조건은 assert 가 아니라 require·check 나 @PowerAssert 함수로 쓴다."
        )
    }
}

/** 같은 단위 테스트의 실패 메시지 — 플러그인 적용 전후를 로컬 JVM 테스트에서 실측 */
private const val UNIT_TEST_BEFORE = "assertEquals(13000, prices.sum())\n→ expected:<13000> but was:<13600>\n\n" +
    "assert(items.sum() == 7)\n→ Assertion failed"

private const val UNIT_TEST_AFTER = "assertEquals(13000, prices.sum())\n" +
    "                    |      |\n" +
    "                    |      13600\n" +
    "                    [4500, 6800, 2300]\n" +
    " expected:<13000> but was:<13600>\n\n" +
    "assert(items.sum() == 7)\n" +
    "       |     |     |\n" +
    "       |     6     false\n" +
    "       [1, 2, 3]"

// ==================== 6. 정리 ====================

@Composable
private fun TakeawayCard() {
    SectionCard(
        title = "📌 정리",
        description = "실패 메시지를 손으로 쓰지 않아도 되게 해 주는 도구 — 켜는 범위와 대상 함수만 정확히 고르면 된다.",
        containerColor = Color(0xFFFFF8E1)
    ) {
        BulletText("안드로이드에서는 compilationFilter 를 직접 지정해야 한다 — 기본값은 어떤 AGP 컴파일에도 걸리지 않는다")
        BulletText("조건식은 한 번만 평가되고, 설명은 실패할 때만 만들어진다 — 성공 경로 비용은 중간값 보관 정도")
        BulletText("직접 만드는 검증 함수는 @PowerAssert 로 — 설명이 null 일 수 있다는 것(리플렉션·Java·플러그인 없는 모듈)만 대비한다")
        BulletText("kotlin.assert 는 release 에서 검사하지 않는다(debug 기기·JVM 테스트는 검사) — 꼭 지켜야 할 조건은 require·check·@PowerAssert 함수로")
        BulletText("debug 핫 리로드(HotSwan)와 함께라면 @PowerAssert 함수와 그 호출 클래스를 핫 리로드에서 뺀다")
    }
}

// ==================== 공통 UI ====================

@Composable
private fun SectionCard(
    title: String,
    description: String,
    containerColor: Color = Color(0xFFFAFAFA),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 13.sp, color = Color(0xFF616161))
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun RunButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
    ) {
        Text(label, fontSize = 13.sp)
    }
}

/** 도식은 고정폭 글꼴·줄바꿈 없이 그려야 값 표시가 제자리에 맞는다 — 넘치면 가로로 스크롤 */
@Composable
private fun DiagramBox(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF263238)
    ) {
        Text(
            text = text.trim('\n'),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp,
            softWrap = false
        )
    }
}

@Composable
private fun ResultRow(title: String, lines: List<String>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFECEFF1)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            lines.forEach { line ->
                Text(text = line, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF37474F))
            }
        }
    }
}

@Composable
private fun MonoLine(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F),
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

@Composable
private fun BulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Text(text = "•", fontSize = 12.sp, color = Color(0xFF424242))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = Color(0xFF424242))
    }
}

@Composable
private fun NoteBox(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1976D2).copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(10.dp),
            fontSize = 12.sp,
            color = Color(0xFF37474F),
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun CodeBox(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF263238)
    ) {
        Text(
            text = code,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp,
            softWrap = false
        )
    }
}
