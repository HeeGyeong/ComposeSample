package com.example.composesample.presentation.example.component.architecture.development.flow

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.composesample.util.boldMarkup
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/**
 * Flow 함정 예제 — 맞아 보이지만 의도와 다르게 도는 Flow 코드 두 가지를 숫자로 확인한다.
 * - 같은 콜드 Flow 를 두 곳에서 수집하면 업스트림이 두 번 돈다 → shareIn 으로 공유하고,
 *   replay · 재구독 설정이 늦게 온 구독자에게 무엇을 주는지까지 센다.
 * - catch 는 자기보다 위(상류)의 예외만 잡는다 → 연산자 순서 · collect 람다 · 취소/시간 초과와의 관계를 대조한다.
 * - 측정 함수는 같은 폴더의 FlowPitfallsLab.kt(JVM 단위 테스트 FlowPitfallsLabTest 와 공유).
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "Flow 함정" 섹션 참고.
 */
@Composable
fun FlowPitfallsExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Flow 함정 — 다중 수집 · catch",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { TwoCollectorsCard() }
            item { ShareSettingsCard() }
            item { CatchPositionCard() }
            item { CatchCancellationCard() }
            item { ChecklistCard() }
        }
    }
}

// ==================== 0. 개요 ====================

@Composable
private fun OverviewCard() {
    SectionCard(title = "참고 글의 함정 네 가지") {
        BodyText(
            "참고 글(exampleGuide.kt)은 '처음엔 맞아 보이는' Flow 버그 네 가지를 다룬다. " +
                "이 화면은 **기존 예제가 다루지 않던 두 가지**를 실측하고, 나머지 둘은 이미 있는 예제로 넘긴다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        TableRow("함정", "어디서 보나", isHeader = true)
        TableRow("콜드 Flow 다중 수집", "1 · 2번 카드 — 업스트림 실행 횟수와 shareIn 설정")
        TableRow("catch 의 범위", "3 · 4번 카드 — 연산자 순서, 취소 · 시간 초과")
        TableRow("conflate 값 누락", "기존 'Flow Operators' 예제의 3번 카드(conflate)")
        TableRow("stateIn 이 안 시작", "기존 'Init Case Test Example' — stateIn(WhileSubscribed) 는 구독자가 붙어야 업스트림을 돌린다")
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "결과는 시간 대신 **횟수와 받은 값**으로 센다. 같은 측정 함수를 JVM 단위 테스트(가상 시간)에서도 돌려 판정을 고정했다."
        )
    }
}

// ==================== 1. 콜드 Flow 를 두 곳에서 수집 ====================

@Composable
private fun TwoCollectorsCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<TwoCollectorResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "1. 같은 콜드 Flow 를 두 곳에서 수집") {
        BodyText(
            "observeUser() 는 flow { emit(api.fetchUser()) } — **collect 할 때마다 fetchUser() 를 다시 부르는** 콜드 Flow 다. " +
                "헤더와 프로필 두 화면이 동시에 수집할 때 API 가 몇 번 불리는지 센다. " +
                "가짜 API 는 호출마다 버전(v1, v2 …)을 올리고 한 번에 ${FETCH_LATENCY_MS}ms 걸린다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CollectMode.entries.forEach { mode ->
            Text(text = mode.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
            CodeBlock(mode.code)
            Spacer(modifier = Modifier.height(6.dp))
        }

        DemoButton(text = if (running) "실행 중…" else "두 방식 실행", color = Color(0xFF1E88E5)) {
            if (!running) {
                running = true
                scope.launch {
                    results = CollectMode.entries.map { runTwoCollectors(it) }
                    running = false
                }
            }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            results.forEach { result ->
                Text(text = result.mode.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                ResultRow("API 호출", "${result.fetchCount}번")
                ResultRow("헤더", result.header)
                ResultRow("프로필", result.profile)
                Spacer(modifier = Modifier.height(8.dp))
            }
            BodyText(
                "콜드 그대로면 요청이 화면 수만큼 나가고, 두 화면이 **서로 다른 응답**을 그릴 수 있다(같은 사용자인데 헤더와 프로필이 어긋남). " +
                    "shareIn 은 업스트림 수집 하나를 여러 구독자가 나눠 쓴다."
            )
            Spacer(modifier = Modifier.height(6.dp))
            CaptionText(
                "두 번 도는 것이 늘 버그는 아니다 — 서로 다른 검색 두 개처럼 독립된 요청이면 두 번이 맞다. " +
                    "공유는 '같은 결과를 함께 써야 할 때'만 고르고, scope 와 SharingStarted 는 원하는 수명에 맞춘다(2번 카드)."
            )
        }
    }
}

// ==================== 2. shareIn 설정 — replay · 재구독 ====================

@Composable
private fun ShareSettingsCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<ShareResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "2. shareIn 설정이 바꾸는 것 — replay · 재구독") {
        BodyText(
            "shareIn 은 **언제 업스트림을 돌리고 멈출지(SharingStarted)**와 **늦게 온 구독자에게 무엇을 줄지(replay)**를 설정으로 정한다. " +
                "같은 API 로 다섯 가지 순서를 돌려, API 호출 수와 두 번째 구독자가 ${OBSERVE_WINDOW_MS}ms 동안 받은 값을 센다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShareCase.entries.forEach { case ->
            PitfallRow(case.label, case.note)
            CodeBlock(case.code)
            Spacer(modifier = Modifier.height(6.dp))
        }

        DemoButton(text = if (running) "실행 중…" else "5가지 실행", color = Color(0xFF00897B)) {
            if (!running) {
                running = true
                scope.launch {
                    // 경우마다 API 인스턴스가 따로라 서로 영향이 없다 → 동시에 돌려 대기 시간을 줄인다.
                    results = ShareCase.entries.map { case -> async { runShareCase(case) } }.awaitAll()
                    running = false
                }
            }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            results.forEach { result ->
                Text(text = result.case.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                ResultRow("API 호출", "${result.fetchCount}번")
                ResultRow("첫 구독자", result.first)
                ResultRow("두 번째", if (result.second.isEmpty()) "받은 값 없음" else result.second.joinToString(" → "))
                Spacer(modifier = Modifier.height(8.dp))
            }
            BodyText(
                "v1 은 첫 응답, v2 는 업스트림을 다시 돌려 받은 새 응답이다. " +
                    "replay = 0 이면 이미 지나간 값은 늦게 온 구독자에게 가지 않는다. " +
                    "구독자가 0 이 되면 WhileSubscribed() 는 업스트림을 멈추지만 **replay 캐시는 기본값으로 남아**, " +
                    "다시 붙은 구독자는 옛 값(v1)을 먼저 받고 새 응답(v2)을 이어 받는다."
            )
            Spacer(modifier = Modifier.height(6.dp))
            CaptionText(
                "옛 값을 잠깐이라도 보여 주기 싫으면 replayExpirationMillis 로 멈출 때 캐시를 비우고, " +
                    "화면 회전처럼 잠깐 떠났다 돌아오는 경우의 재요청이 싫으면 stopTimeoutMillis 로 멈춤을 늦춘다. " +
                    "stopTimeout 과 재시작 시점은 기존 'Init Case Test Example' 이 직접 구독을 끊었다 붙여 보여 준다."
            )
        }
    }
}

// ==================== 3. catch 는 자기보다 위(상류)만 잡는다 ====================

@Composable
private fun CatchPositionCard() {
    val scope = rememberCoroutineScope()
    var input by remember { mutableIntStateOf(FAILING_VALUE) }
    var results by remember { mutableStateOf<List<CatchResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "3. catch 는 자기보다 위(상류)만 잡는다") {
        BodyText(
            "연산자는 위에서 아래로 이어진다. catch 는 **자기보다 위에서 난 예외만** 잡고, " +
                "아래쪽 onEach 나 collect 람다에서 난 예외는 그대로 collect 밖으로 나간다. " +
                "catch 가 불린 횟수 · collect 가 받은 값 · 밖으로 나온 예외를 센다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            FilterChip(
                selected = input == FAILING_VALUE,
                onClick = { input = FAILING_VALUE },
                label = { Text(text = "x = $FAILING_VALUE (실패)", fontSize = 12.sp) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = input == ARTICLE_VALUE,
                onClick = { input = ARTICLE_VALUE },
                label = { Text(text = "x = $ARTICLE_VALUE (원문 값)", fontSize = 12.sp) }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        CatchCase.entries.forEach { case ->
            Text(text = case.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
            CodeBlock(case.code)
            Spacer(modifier = Modifier.height(6.dp))
        }

        DemoButton(text = if (running) "실행 중…" else "x = $input 으로 3가지 실행", color = Color(0xFFEF6C00)) {
            if (!running) {
                running = true
                scope.launch {
                    results = CatchCase.entries.map { runCatchCase(it, input) }
                    running = false
                }
            }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "x = ${results.first().input} 결과",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(4.dp))
            results.forEach { result ->
                Text(text = result.case.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                ResultRow("catch 호출", "${result.handlerCalls}번")
                ResultRow("받은 값", if (result.received.isEmpty()) "없음" else result.received.joinToString())
                ResultRow("밖으로", result.escaped ?: "없음(정상 완료)")
                Spacer(modifier = Modifier.height(8.dp))
            }
            BodyText(
                "x = $FAILING_VALUE 이면 **onEach 를 catch 위로 올린 줄만** catch 가 불려 -1 을 내보낸다. " +
                    "collect 람다의 일을 지키려면 그 일을 onEach 로 옮기고 그 아래에 catch 를 둔 뒤 collect() 나 launchIn(scope) 로 끝낸다(2번째 줄과 같은 모양)."
            )
            Spacer(modifier = Modifier.height(6.dp))
            CaptionText(
                "참고 글의 예제는 flowOf(1000) 에 if (it == 1) 이라 **어느 순서에서도 예외가 나지 않는다** — x = $ARTICLE_VALUE 으로 돌리면 세 줄이 모두 같아진다. " +
                    "이 화면은 실패하는 값을 $FAILING_VALUE 로 바꿔 순서의 차이를 드러냈다."
            )
        }
    }
}

// ==================== 4. catch 와 취소 ====================

@Composable
private fun CatchCancellationCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<CancelResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "4. catch 와 취소 · 시간 초과") {
        BodyText(
            "catch 는 **수집하는 코루틴 자신이 취소된 것**이면 핸들러를 부르지 않고 다시 던진다. " +
                "그런데 같은 TimeoutCancellationException 이라도 withTimeout 이 어디에 있느냐에 따라 결과가 갈린다. " +
                "시간 초과는 ${SHORT_TIMEOUT_MS}ms, 상류 작업은 ${LONG_WORK_MS}ms 다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CancelCase.entries.forEach { case ->
            Text(text = case.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
            CodeBlock(case.code)
            Spacer(modifier = Modifier.height(6.dp))
        }

        DemoButton(text = if (running) "실행 중…" else "4가지 실행", color = Color(0xFF8E24AA)) {
            if (!running) {
                running = true
                scope.launch {
                    results = CancelCase.entries.map { runCancelCase(it) }
                    running = false
                }
            }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            results.forEach { result ->
                Text(text = result.case.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                ResultRow("catch 호출", result.handlerSaw?.let { "${result.handlerCalls}번 ($it)" } ?: "${result.handlerCalls}번")
                ResultRow("받은 값", if (result.received.isEmpty()) "없음" else result.received.joinToString())
                ResultRow("결과", result.outcome)
                Spacer(modifier = Modifier.height(8.dp))
            }
            BodyText(
                "withTimeout 이 수집을 감싸면 시간 초과는 **수집 코루틴의 취소**라 catch 를 그냥 지나간다. " +
                    "withTimeout 이 상류 안에 있으면 수집 코루틴은 멀쩡하고 상류가 예외를 던진 것이라, catch 가 보통 예외처럼 잡아 -1 을 내보낸다."
            )
            Spacer(modifier = Modifier.height(6.dp))
            CaptionText(
                "핸들러에 if (e is CancellationException) throw e 를 넣으면, 수집 코루틴의 진짜 취소는 애초에 핸들러까지 오지 않으므로 " +
                    "그 줄이 다시 던지는 것은 상류의 시간 초과 같은 예외다. 그러면 launch 는 실패가 아니라 **취소로 끝나** 부모에 아무것도 알리지 않고, " +
                    "화면은 대체 값 없이 조용히 멈춘다. 다시 던질지는 '이 시간 초과를 실패로 볼지'로 정한다."
            )
        }
    }
}

// ==================== 5. 정리 ====================

@Composable
private fun ChecklistCard() {
    SectionCard(title = "정리 — 연산자를 붙이기 전에 물을 것") {
        PitfallRow(
            "수집기를 하나 더 붙이면 업스트림이 또 도는가?",
            "콜드 Flow 는 그렇다. 같은 결과를 함께 써야 하면 shareIn · stateIn 으로 공유한다."
        )
        PitfallRow(
            "늦게 온 구독자에게 무엇을 줄 것인가?",
            "replay = 0 이면 아무것도 없다. 최신 값이 필요하면 replay = 1(상태라면 stateIn)."
        )
        PitfallRow(
            "구독자가 모두 떠났다 돌아오면?",
            "WhileSubscribed() 는 업스트림을 다시 돌리고 옛 값을 먼저 준다. stopTimeoutMillis · replayExpirationMillis 로 조정한다."
        )
        PitfallRow(
            "이 catch 는 어디까지 덮는가?",
            "자기보다 위만. 실패할 수 있는 일은 catch 위에 두고, 수집 코루틴의 취소는 catch 가 알아서 다시 던진다."
        )
    }
}

// ==================== 공용 UI ====================

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
    Text(text = text.boldMarkup(), fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text.boldMarkup(), fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

/** 코드는 줄이 길어 접히면 읽기 어렵다 → 가로 스크롤로 원문 모양을 유지한다. */
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
            modifier = Modifier.width(110.dp),
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
