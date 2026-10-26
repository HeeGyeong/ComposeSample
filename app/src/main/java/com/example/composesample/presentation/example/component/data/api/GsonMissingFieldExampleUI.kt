package com.example.composesample.presentation.example.component.data.api

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 응답 필드 누락과 Gson — 같은 JSON 을 모양이 다른 데이터 클래스로 파싱해, Gson 이 Kotlin 기본값과 non-null 선언을
 * 건너뛰는 지점과 그 값이 나중에 어디서 터지는지를 보여준다.
 * 6~8번 카드는 같은 응답을 kotlinx.serialization(1.12.0) Json 으로 파싱해 '파싱하는 줄에서 실패하는' 쪽과 대조한다.
 * - 파싱·판정 로직은 GsonMissingField.kt(단위 테스트 GsonMissingFieldTest 와 같은 함수)
 * - kotlinx 쪽 로직은 JsonMissingField.kt(단위 테스트 JsonMissingFieldTest)
 * - 참고 자료(URL/핵심 개념)는 exampleGuide.kt 의 "GsonMissingField" 섹션 참고.
 */
@Composable
fun GsonMissingFieldExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Gson Missing Field",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item { ShapeCompareCard() }
            item { UseSiteCard() }
            item { BooleanFlagCard() }
            item { FailFastCard() }
            item { ConverterPathCard() }
            item { KotlinxJsonCard() }
            item { FailureShapeCard() }
            item { ValueClassLeakCard() }
            item { GuideCard() }
        }
    }
}

private val BrokenColor = Color(0xFFC62828)
private val SilentColor = Color(0xFFEF6C00)
private val OkColor = Color(0xFF2E7D32)
private val ValueColor = Color(0xFF37474F)
private val LoudFailColor = Color(0xFF6A1B9A) // 파싱 단계에서 경로와 함께 실패 — 응답 경계에서 처리할 수 있는 실패

private fun FieldProblem.color(): Color = when (this) {
    FieldProblem.BROKEN -> BrokenColor
    FieldProblem.SILENT -> SilentColor
    FieldProblem.NONE -> ValueColor
}

/** 번호 카드 공통 틀 — 슬롯은 Column 안에서 호출한다(자식 여럿이 겹치지 않게). */
@Composable
private fun ExampleCard(
    title: String,
    description: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

/** JSON·코드 원문을 보여주는 다크 박스. */
@Composable
private fun CodeBox(lines: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        lines.forEach { line ->
            Text(
                text = line,
                color = Color(0xFFCFD8DC),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Gson 은 생성자를 부르지 않을 수 있다", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Gson 은 인자 없는 생성자가 있을 때만 생성자를 부르고, Kotlin 은 파라미터 전부에 기본값이 있을 때만 그 생성자를 만든다. " +
                    "그렇지 않으면 Gson 은 생성자 없이(JDK Unsafe) 객체를 만들고 JSON 에 있는 키만 채운다 — Kotlin 기본값도 non-null 검사도 실행되지 않는다. " +
                    "이 프로젝트의 Retrofit(GsonConverterFactory)과 Ktor(gson()) 모두 끝은 Gson.fromJson 이다. " +
                    "6~8번 카드는 같은 응답을 kotlinx.serialization 으로 파싱해, 생성자를 거쳐 파싱하는 줄에서 실패하는 쪽과 비교한다.",
                fontSize = 12.sp,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "빨강 = 타입 계약 위반(non-null 선언에 null — 나중에 NPE) · 주황 = 예외 없이 틀린 값",
                fontSize = 11.sp,
                color = Color(0xFF555555)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 1. 같은 JSON, 모델 모양 셋
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun ShapeCompareCard() {
    var payload by rememberSaveable { mutableStateOf(MemberPayload.MISSING) }

    ExampleCard(
        title = "1. 같은 JSON, 모델 모양 셋",
        description = "응답을 고르면 같은 JSON 을 ① 일부만 기본값 ② 전부 기본값 ③ nullable 응답 모델 + 매퍼로 파싱한다. 파싱 자체는 셋 다 예외 없이 끝난다.",
        accent = Color(0xFF1E88E5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MemberPayload.entries.forEach { entry ->
                FilterChip(
                    selected = entry == payload,
                    onClick = { payload = entry },
                    label = { Text(text = entry.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFBBDEFB))
                )
            }
        }
        Text(text = payload.note, fontSize = 11.sp, color = Color(0xFF757575))
        Spacer(modifier = Modifier.height(4.dp))
        CodeBox(lines = listOf(payload.json))

        Spacer(modifier = Modifier.height(10.dp))
        ModelBlock(
            title = "① 일부만 기본값 — PartialDefaultMember",
            rows = remember(payload) { parsePartial(payload).rows(payload) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ModelBlock(
            title = "② 전부 기본값 — AllDefaultMember",
            rows = remember(payload) { parseAllDefault(payload).rows(payload) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ModelBlock(
            title = "③ 응답 모델(전부 nullable) — MemberResponse",
            rows = remember(payload) { parseResponse(payload).rows(payload) }
        )
        Spacer(modifier = Modifier.height(6.dp))
        val mapped = remember(payload) { mapResult(payload) }
        Text(
            text = mapped.fold(
                onSuccess = { "→ toMemberData() ✓ $it" },
                onFailure = { "→ toMemberData() ✗ 응답 경계에서 실패: ${it.message} — 화면은 오류 상태로" }
            ),
            fontSize = 11.sp,
            color = if (mapped.isSuccess) OkColor else SilentColor
        )
    }
}

@Composable
private fun ModelBlock(title: String, rows: List<FieldRow>) {
    val broken = rows.count { it.problem == FieldProblem.BROKEN }
    val silent = rows.count { it.problem == FieldProblem.SILENT }
    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    Text(
        text = if (broken + silent == 0) "어긋난 필드 없음" else "계약 위반 $broken · 조용히 틀림 $silent",
        fontSize = 11.sp,
        color = when {
            broken > 0 -> BrokenColor
            silent > 0 -> SilentColor
            else -> OkColor
        }
    )
    Spacer(modifier = Modifier.height(2.dp))
    rows.forEach { row ->
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${row.name}: ${row.declared}",
                modifier = Modifier.weight(1f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF616161)
            )
            Text(
                text = row.value,
                modifier = Modifier.weight(0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (row.problem == FieldProblem.NONE) FontWeight.Normal else FontWeight.Bold,
                color = row.problem.color()
            )
        }
        row.reason?.let { reason ->
            Text(text = "  ⚠ $reason", fontSize = 10.sp, color = row.problem.color())
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 2. 파싱은 성공했다. 어디서 터지나
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun UseSiteCard() {
    var results by remember { mutableStateOf<List<ProbeResult>>(emptyList()) }
    val accent = Color(0xFFE53935)

    ExampleCard(
        title = "2. 파싱은 성공했다. 어디서 터지나",
        description = "'키 누락' 응답으로 만든 ① 객체에 평범한 코드를 하나씩 실행한다. 예외는 파싱한 줄이 아니라 값을 쓰는 줄에서 난다.",
        accent = accent
    ) {
        Button(
            onClick = { results = runUseProbes() },
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text(text = "${useProbes.size}가지 사용 실행", color = Color.White)
        }
        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "예외 없음 ${results.count { it.ok }} / ${results.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ValueColor
            )
            results.forEach { result ->
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = (if (result.ok) "✓ " else "✗ ") + result.probe.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (result.ok) OkColor else BrokenColor
                    )
                    Text(text = result.probe.code, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF616161))
                    Text(text = "→ ${result.text}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ValueColor)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "toString·문자열 템플릿·equals 는 null 을 그대로 보여 줘 로그로는 멀쩡해 보인다. " +
                "hashCode·copy()·멤버 호출·순회·when·non-null 파라미터 전달에서 NPE 가 난다. " +
                "엘비스(?:)는 컴파일러가 '필요 없다'고 경고하지만 null 검사가 남아 실제로는 막아 준다. " +
                "가장 위험한 것은 마지막 줄 — 예외 없이 '비활성' 으로 처리된다.",
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 3. Boolean — 꺼짐과 모름
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun BooleanFlagCard() {
    val rows = remember { flagRows() }

    ExampleCard(
        title = "3. Boolean — 꺼짐과 모름",
        description = "is_premium 하나를 세 가지로 선언해 다섯 입력을 파싱한다. Boolean 은 '안 보냄'을 false 로 만들어 무료 사용자로 취급한다.",
        accent = Color(0xFFFB8C00)
    ) {
        FlagTableRow(input = "입력", a = "Boolean", b = "Boolean?", c = "String? + 매퍼", header = true)
        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color(0xFFE0E0E0))
        rows.forEach { row ->
            FlagTableRow(input = row.input.label, a = row.asBoolean, b = row.asNullable, c = row.asString, header = false)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Boolean? 은 누락·null 을 '모름'으로 남기지만 \"Y\" 는 못 막는다 — Gson 은 문자열을 Boolean.parseBoolean 으로 읽어 \"true\" 외에는 전부 false 다. " +
                "숫자 1 은 Boolean·Boolean? 모두 JsonSyntaxException 으로 응답 전체가 실패한다. " +
                "Y/N 플래그는 String? 으로 받아 매퍼에서 해석하고, 모르는 값은 '모름'으로 남겨 다시 조회하거나 이전 값을 쓴다.",
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun FlagTableRow(input: String, a: String, b: String, c: String, header: Boolean) {
    val weight = if (header) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = input, modifier = Modifier.weight(0.6f), fontSize = 11.sp, fontWeight = weight, fontFamily = FontFamily.Monospace)
        Text(text = a, modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = weight, fontFamily = FontFamily.Monospace, color = flagColor(a, header))
        Text(text = b, modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = weight, fontFamily = FontFamily.Monospace, color = flagColor(b, header))
        Text(text = c, modifier = Modifier.weight(1.3f), fontSize = 11.sp, fontWeight = weight, fontFamily = FontFamily.Monospace, color = flagColor(c, header))
    }
}

private fun flagColor(text: String, header: Boolean): Color = when {
    header -> Color.Black
    text.startsWith("✗") -> BrokenColor
    else -> ValueColor
}

// ──────────────────────────────────────────────────────────────────────────
// 4. disableJdkUnsafe() — 빨리 실패시키기
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun FailFastCard() {
    val rows = remember { failFastRows() }

    ExampleCard(
        title = "4. disableJdkUnsafe() — 빨리 실패시키기",
        description = "GsonBuilder().disableJdkUnsafe() 는 생성자 없는 생성을 파싱 시점의 JsonIOException 으로 바꾼다. 막는 것은 거기까지다.",
        accent = Color(0xFF8E24AA)
    ) {
        rows.forEach { row ->
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    text = "${row.model} × ${row.payload.label}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (row.problem == FieldProblem.BROKEN) BrokenColor else ValueColor
                )
                Text(text = row.result, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = row.problem.color())
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "① 은 키가 빠지든 null 이든 파싱한 줄에서 실패해 위치가 정확하다. ② 는 인자 없는 생성자가 있어 그대로 통과하고, " +
                "명시적 null 은 여전히 non-null 필드에 들어간다. 이 프로젝트 DI 는 이 옵션을 켜지 않았다(켜면 기존 모델의 런타임 동작이 바뀐다).",
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 5. 프로젝트의 실제 두 경로
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun ConverterPathCard() {
    val scope = rememberCoroutineScope()
    var rows by remember { mutableStateOf<List<ConverterRow>>(emptyList()) }
    var running by remember { mutableStateOf(false) }
    val accent = Color(0xFF43A047)

    ExampleCard(
        title = "5. Retrofit · Ktor 도 같은 결과",
        description = "같은 다섯 응답을 Gson.fromJson 직접 · Retrofit GsonConverterFactory · Ktor gson() 으로 ① 모델에 파싱해 비교한다. 응답은 네트워크 대신 인터셉터·MockEngine 이 만든다.",
        accent = accent
    ) {
        Button(
            onClick = {
                running = true
                scope.launch {
                    try {
                        rows = converterRows()
                    } finally {
                        running = false
                    }
                }
            },
            enabled = !running,
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text(text = if (running) "실행 중..." else "${MemberPayload.entries.size}개 응답 × 3경로 실행", color = Color.White)
        }
        if (rows.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "세 경로 결과 일치 ${rows.count { it.same }} / ${rows.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (rows.all { it.same }) OkColor else BrokenColor
            )
            rows.forEach { row ->
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = (if (row.same) "✓ " else "✗ ") + row.payload.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (row.same) OkColor else BrokenColor
                    )
                    if (row.same) {
                        Text(text = row.direct, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ValueColor)
                    } else {
                        Text(text = "직접: ${row.direct}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ValueColor)
                        Text(text = "Retrofit: ${row.retrofit}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ValueColor)
                        Text(text = "Ktor: ${row.ktor}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ValueColor)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 6. kotlinx.serialization — 같은 응답
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun KotlinxJsonCard() {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val case = jsonCases[index]

    ExampleCard(
        title = "6. kotlinx.serialization — 파싱하는 줄에서 실패한다",
        description = "같은 응답을 플러그인이 만든 serializer + Json 으로 ① 과 같은 모양(JsonMember)에 파싱한다. 생성자를 거치므로 누락 키에는 기본값이 들어가고, 계약을 어기는 응답은 파싱 단계에서 경로와 함께 실패한다.",
        accent = Color(0xFF00897B)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            jsonCases.forEachIndexed { i, entry ->
                FilterChip(
                    selected = i == index,
                    onClick = { index = i },
                    label = { Text(text = entry.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFB2DFDB))
                )
            }
        }
        Text(text = case.note, fontSize = 11.sp, color = Color(0xFF757575))
        Spacer(modifier = Modifier.height(4.dp))
        CodeBox(lines = listOf(case.json))

        val outcomes = remember(index) {
            listOf(
                Triple("Gson ① PartialDefaultMember", gsonOutcome(case.json), true),
                Triple("Json (기본 설정)", decodeMember(strictJson, case.json), false),
                Triple("Json { ignoreUnknownKeys; coerceInputValues }", decodeMember(lenientJson, case.json), false)
            )
        }
        outcomes.forEach { (label, outcome, isGson) ->
            OutcomeBlock(label = label, outcome = outcome, isGson = isGson)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "주황 = 파싱은 성공했지만 계약 위반(Gson) · 보라 = 파싱 단계에서 경로와 함께 실패(kotlinx). " +
                "보라는 응답 경계에서 한 번에 처리할 수 있는 실패다. coerceInputValues 는 '기본값이 있는' 프로퍼티의 null·모르는 enum 만 바꾸므로 " +
                "필수값 누락·\"Y\" 는 관대한 설정에서도 실패한다(명시적 null 에 coerceInputValues 를 켜라는 hint 가 그대로 나오는 것은 member_id 에 기본값이 없어서다). DataRules 결론(응답 모델 nullable + 매퍼)은 Json 에서도 그대로다 — Json 은 실패를 앞당길 뿐이다.",
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun OutcomeBlock(label: String, outcome: DecodeOutcome, isGson: Boolean) {
    val color = when {
        outcome.ok -> OkColor
        isGson -> SilentColor
        else -> LoudFailColor
    }
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = (if (outcome.ok) "✓ " else if (isGson) "⚠ " else "✗ ") + label,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = color
        )
        Text(text = outcome.text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = color)
        outcome.hint?.let { hint ->
            Text(text = "hint: $hint", fontSize = 10.sp, color = Color(0xFF757575))
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 7. 무엇으로 잡나 — 1.12 의 실패 모양
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun FailureShapeCard() {
    val scope = rememberCoroutineScope()
    var rows by remember { mutableStateOf<List<FailureRow>>(emptyList()) }
    var depth by remember { mutableStateOf<DepthLimit?>(null) }
    var running by remember { mutableStateOf(false) }
    val accent = Color(0xFF5E35B1)

    ExampleCard(
        title = "7. 무엇으로 잡나 — 1.12 의 실패 모양",
        description = "다섯 가지 실패를 일으켜 예외 타입·원인과, catch 절 셋(SerializationException · JsonException · Exception) 중 어디에 잡히는지 본다. 1.11.0 줄은 같은 코드를 JVM 에서 잰 값이다.",
        accent = accent
    ) {
        Button(
            onClick = {
                running = true
                scope.launch {
                    try {
                        // 2만 겹 중첩은 스택을 깊게 쓴다 — 메인 스레드 밖에서
                        val (measured, limit) = withContext(Dispatchers.Default) { failureRows() to depthLimit() }
                        rows = measured
                        depth = limit
                    } finally {
                        running = false
                    }
                }
            },
            enabled = !running,
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text(text = if (running) "실행 중..." else "다섯 가지 실패 실행", color = Color.White)
        }
        rows.forEach { row ->
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(text = row.label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ValueColor)
                Text(text = "→ ${row.thrown}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = LoudFailColor)
                row.cause?.let { cause ->
                    Text(text = "   cause $cause", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = ValueColor)
                }
                Text(
                    text = "catch: SerializationException ${mark(row.bySerialization)} · JsonException ${mark(row.byJson)} · Exception ${mark(row.byException)}",
                    fontSize = 11.sp,
                    color = if (row.byException) ValueColor else BrokenColor
                )
                Text(text = "1.11.0: ${row.before111}", fontSize = 10.sp, color = Color(0xFF757575))
            }
        }
        depth?.let { limit ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "이 기기·이 스레드(Dispatchers.Default)의 실제 경계: ${limit.maxOk} 겹까지 성공" +
                    (limit.failedAt?.let { ", $it 겹에서 ${limit.stoppedBy}" } ?: ""),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (limit.stoppedBy.startsWith("StackOverflowError")) BrokenColor else ValueColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "MissingFieldException 은 JsonException 이 아니라서 catch (e: JsonException) 를 빠져나간다 — 파싱 실패는 SerializationException 으로 잡는다. " +
                "1.11 에서는 init 검증 실패가 IllegalArgumentException 그대로 나와 그 catch 도 빠져나갔고, 깊은 중첩은 StackOverflowError(Error)였다. " +
                "1.12 의 한도 400 은 '스택이 넘치기 전에 멈춘다'는 가정이다 — Galaxy A72 실측(Dispatchers.Default, 1MB 스택): release 는 400 겹까지 읽고 401 겹에서 too nested(JIT·AOT 같음), " +
                "debug(디버깅 가능 빌드 — HotSwan 재작성을 꺼도 같음)는 230~240 겹에서 StackOverflowError 가 먼저 난다. 한도를 풀면 release 도 StackOverflowError — 예제만 Throwable 로 잡아 보인다.",
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

private fun mark(caught: Boolean): String = if (caught) "✓" else "✗"

// ──────────────────────────────────────────────────────────────────────────
// 8. value class 검증과 메시지에 남는 입력
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun ValueClassLeakCard() {
    val panRows = remember { valueClassRows() }
    val leaks = remember { leakRows() }

    ExampleCard(
        title = "8. value class 검증과 메시지에 남는 입력",
        description = "검증을 팩토리에 둔 value class 와 init 에 둔 value class 를 같은 잘못된 값으로 읽고, 실패한 응답의 예외 메시지에 무엇이 남는지 본다.",
        accent = Color(0xFFD81B60)
    ) {
        Text(text = "value class — 생성 경로", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        panRows.forEach { row ->
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Text(text = row.code, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF616161))
                Text(
                    text = "→ ${row.result}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (row.problem == FieldProblem.NONE) FontWeight.Normal else FontWeight.Bold,
                    color = row.problem.color()
                )
                if (row.problem == FieldProblem.SILENT) {
                    Text(text = "  ⚠ 팩토리가 거부한 값이 그대로 들어왔다", fontSize = 10.sp, color = SilentColor)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "예외 메시지 — amount_minor 가 null 인 응답", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        CodeBox(lines = listOf(LEAK_INPUT))
        leaks.forEach { row ->
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Text(text = row.code, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF616161))
                Text(
                    text = row.text,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (row.leaks) BrokenColor else OkColor
                )
                Text(
                    text = if (row.leaks) "  ⚠ 카드번호 원문이 남는다" else "  ✓ 입력 값 없음",
                    fontSize = 10.sp,
                    color = if (row.leaks) BrokenColor else OkColor
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "플러그인 serializer 는 value class 의 private 생성자를 직접 부른다 — 검증을 팩토리에만 두면 Gson 과 같은 구멍이고, init 에 둔 검사는 역직렬화에도 실행된다. " +
                "value class 에 붙인 serializer 는 data class 필드에도 걸린다(#144 의 Gson TypeAdapter 는 JVM 에서 String 필드라 안 걸렸다). " +
                "실패 메시지는 실패한 필드와 상관없이 입력 전체를 담는다 — 로그에는 shortMessage + path 를 남기거나 exceptionsWithDebugInfo 를 끈다.",
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun GuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "핵심 정리", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "기본값 없는 파라미터가 하나라도 있으면 Gson 은 생성자 없이 만든다 → 누락 키는 null·false·0, Kotlin 기본값은 무시",
                "전부 기본값이면 누락 키에는 기본값이 들어가지만, 명시적 null 은 non-null 필드에 그대로 들어온다",
                "모르는 enum 값(서버가 새 값을 추가한 날)·리스트 안 null 원소도 non-null 선언을 뚫는다",
                "파싱은 성공한다 — NPE 는 hashCode·copy()·멤버 호출·when 처럼 값을 쓰는 줄에서, 파서와 먼 곳에서 난다",
                "응답 모델은 전부 nullable, 앱 모델은 매퍼가 만든다 — 필수값은 requireNotNull 로 필드 이름과 함께 응답 경계에서 실패",
                "Boolean? 은 누락과 false 를 구분하지만 \"Y\" 는 못 막는다(문자열은 parseBoolean) — Y/N 은 String? 으로 받아 매퍼에서",
                "disableJdkUnsafe() 는 생성자 없는 생성만 막는다 — 명시적 null 은 매퍼가 막는다",
                "비정상 응답(누락·null·이름 변경·낯선 값)을 단위 테스트에 넣는다 — GsonMissingFieldTest · JsonMissingFieldTest",
                "kotlinx.serialization 은 생성자를 거친다 — 누락 키는 기본값, 기본값 없는 키 누락·non-null 의 null·모르는 키·타입 불일치는 파싱하는 줄에서 경로와 함께 실패",
                "coerceInputValues 는 '기본값이 있는' 프로퍼티의 null·모르는 enum 만 바꾼다 — 필수값 누락과 \"Y\" 같은 타입 불일치는 그대로 실패",
                "1.12: init 검증 실패·400 겹 넘는 중첩도 JsonDecodingException(기기 debug 빌드의 백그라운드 스레드는 그 전에 StackOverflowError). 단 MissingFieldException 은 JsonException 이 아니다 → catch 는 SerializationException 으로",
                "value class 의 private 생성자는 팩토리를 못 지킨다(init 검증은 지킨다). value class 에 붙인 serializer 는 data class 필드에도 걸린다",
                "예외 메시지에는 입력 JSON 전체가 들어간다(다른 필드의 실패라도) — 로그엔 shortMessage + path, 또는 exceptionsWithDebugInfo = false"
            ).forEach {
                Text(
                    text = "• $it",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}
