package com.example.composesample.presentation.example.component.ui.layout.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.LocalUiMediaScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiMediaScope
import androidx.compose.ui.derivedMediaQuery
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.mediaQuery
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlin.math.roundToInt

/**
 * Compose MediaQuery API Example
 *
 * Compose 1.11 의 실험 API 인 MediaQuery(선언적 환경 질의)를 시연한다.
 * 창 크기뿐 아니라 기기 자세(posture)·포인터 정밀도·키보드 종류·시청 거리까지
 * 하나의 UiMediaScope 로 질의한다.
 *
 * 이 예제의 핵심 함정은 "활성화" 다 — LocalUiMediaScope 는 기본 제공되지 않으며
 * ComposeUiFlags.isMediaQueryIntegrationEnabled 를 setContent 이전에 켜야 한다.
 * 자세한 배경은 같은 패키지의 exampleGuide.kt 참고.
 */

/** 시뮬레이션 카드에서 참/거짓이 갈리는 기준 폭 */
private val BREAKPOINT = 600.dp

@Composable
fun MediaQueryExampleUI(onBackEvent: () -> Unit) {
    // 플래그는 setContent 이전에 정해지므로 컴포지션 도중에는 값이 바뀌지 않는다.
    @OptIn(ExperimentalComposeUiApi::class)
    val integrationEnabled = ComposeUiFlags.isMediaQueryIntegrationEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "MediaQuery API (선언적 환경 적응)", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { MediaQueryConceptCard() }
            item { MediaQueryActivationCard(integrationEnabled = integrationEnabled) }
            item { MediaQueryDeviceValuesCard(integrationEnabled = integrationEnabled) }
            item { MediaQueryVsDerivedCard() }
            item { MediaQueryBranchCard(integrationEnabled = integrationEnabled) }
            item { MediaQuerySummaryCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun MediaQueryConceptCard() {
    ExampleCard(title = "MediaQuery 개요", titleColor = Color(0xFF1976D2)) {
        Text(
            text = "CSS 미디어 쿼리에서 착안한 선언적 환경 질의입니다. \"지금 환경이 이 조건을 만족하는가\" 를 " +
                    "람다로 적고 Boolean 을 돌려받습니다. 질의 대상은 UiMediaScope 하나이고, 창 크기 외에 " +
                    "기기 자세·포인터 정밀도·키보드 종류·시청 거리·카메라/마이크 유무까지 포함합니다.",
            fontSize = 14.sp,
            color = Color(0xFF424242),
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 같은 "화면 적응" 주제를 다루는 기존 예제와 축이 어떻게 다른지 정리
        val comparisons = listOf(
            "MediaQuery" to "ui 레이어. 크기 + 자세/입력/거리까지 선언적 질의",
            "WindowSizeClass" to "material3. 폭·높이를 3단계로 분류 (Activity 필요)",
            "LocalConfiguration" to "값을 직접 읽어 if 로 분기 (명령형)"
        )
        comparisons.forEach { (api, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = api,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.width(140.dp)
                )
                Text(text = desc, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock(
            code = """
                // 조건을 "값"이 아니라 "질의"로 적는다
                val isWide = mediaQuery { windowWidth >= 600.dp }
                val isTabletop = mediaQuery { windowPosture == Posture.Tabletop }
                val needsBigTargets = mediaQuery { pointerPrecision == PointerPrecision.Coarse }
            """.trimIndent(),
            borderColor = Color(0xFF1976D2)
        )
    }
}

// ==================== 2. 활성화 함정 ====================

@Composable
private fun MediaQueryActivationCard(integrationEnabled: Boolean) {
    ExampleCard(title = "먼저 알아야 할 함정 — 기본값은 '꺼짐'", titleColor = Color(0xFFC62828)) {
        Text(
            text = "LocalUiMediaScope 는 기본값이 없습니다. 플랫폼이 이 CompositionLocal 을 제공하는 것은 " +
                    "ComposeUiFlags.isMediaQueryIntegrationEnabled 가 true 일 때뿐이고, 이 플래그는 " +
                    "기본이 false 입니다. 끈 채로 mediaQuery { } 를 호출하면 컴파일은 통과하고 " +
                    "실행 시점에 IllegalStateException 이 납니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        StatusRow(
            label = "isMediaQueryIntegrationEnabled",
            value = if (integrationEnabled) "true — 실제 값 질의 가능" else "false — 질의 시 예외",
            ok = integrationEnabled
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "직접 UiMediaScope 를 만들어 제공하는 우회로도 없습니다. 플랫폼 구현을 생성하는 " +
                    "obtainUiMediaScope() 는 Kotlin internal 이라 앱 모듈에서 호출할 수 없습니다. " +
                    "즉 실제 기기 값을 쓰려면 플래그를 켜는 것이 유일한 경로입니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            code = """
                // 플래그는 컴포지션 루트가 만들어질 때 한 번 읽힌다
                // → 반드시 setContent 호출 "이전"에 켤 것
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    ComposeUiFlags.isMediaQueryIntegrationEnabled = true
                    setContent { /* ... */ }
                }
            """.trimIndent(),
            borderColor = Color(0xFFC62828)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "setContent 이후에 켜면 플래그만 true 로 보이고 CompositionLocal 은 여전히 없는 상태가 됩니다. " +
                    "이 예제는 BlogExampleActivity.onCreate 에서 미리 켜 둔 덕분에 아래 카드가 동작합니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
    }
}

// ==================== 3. 실제 기기 값 ====================

@OptIn(ExperimentalMediaQueryApi::class)
@Composable
private fun MediaQueryDeviceValuesCard(integrationEnabled: Boolean) {
    ExampleCard(title = "실제 기기 값 (실동작)", titleColor = Color(0xFF2E7D32)) {
        if (!integrationEnabled) {
            UnavailableNotice()
            return@ExampleCard
        }

        val scope = LocalUiMediaScope.current
        Text(
            text = "플랫폼이 제공한 UiMediaScope 의 8개 속성입니다. 기기를 회전하거나 멀티윈도우로 크기를 " +
                    "바꾸거나 키보드를 열면 값이 즉시 갱신됩니다 — 모두 스냅샷 상태라 읽는 쪽이 리컴포지션됩니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        ValueRow("windowWidth", "${scope.windowWidth.value.roundToInt()}dp")
        ValueRow("windowHeight", "${scope.windowHeight.value.roundToInt()}dp")
        ValueRow("windowPosture", postureLabel(scope.windowPosture))
        ValueRow("pointerPrecision", pointerPrecisionLabel(scope.pointerPrecision))
        ValueRow("keyboardKind", keyboardKindLabel(scope.keyboardKind))
        ValueRow("viewingDistance", viewingDistanceLabel(scope.viewingDistance))
        ValueRow("hasCamera", if (scope.hasCamera) "true" else "false")
        ValueRow("hasMicrophone", if (scope.hasMicrophone) "true" else "false")

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "windowPosture 는 폴더블 힌지 상태를 androidx.window 로 읽어 옵니다. 접히지 않는 기기에서는 " +
                    "항상 Flat 이고, viewingDistance 는 TV·자동차 같은 기기 종류로 결정되어 폰에서는 Near 입니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
    }
}

// ==================== 4. mediaQuery vs derivedMediaQuery ====================

@OptIn(ExperimentalMediaQueryApi::class)
@Composable
private fun MediaQueryVsDerivedCard() {
    // 실제 창 크기는 앱이 마음대로 바꿀 수 없으므로, 두 API 의 리컴포지션 차이만
    // 관측하기 위해 폭만 슬라이더로 조작하는 시뮬레이션 UiMediaScope 를 제공한다.
    var simulatedWidth by remember { mutableFloatStateOf(360f) }
    val simulatedScope = remember { SimulatedUiMediaScope { simulatedWidth.dp } }
    val plainCounter = remember { CompositionCounter() }
    val derivedCounter = remember { CompositionCounter() }

    ExampleCard(title = "mediaQuery vs derivedMediaQuery (실측)", titleColor = Color(0xFF6A1B9A)) {
        Text(
            text = "두 API 는 같은 질의를 받지만 리컴포지션 범위가 다릅니다. 아래 슬라이더로 시뮬레이션 창 폭을 " +
                    "움직이면서 각 자식이 몇 번 리컴포지션됐는지 세어 봅니다. 기준은 ${BREAKPOINT.value.roundToInt()}dp 입니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "창 폭",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                modifier = Modifier.width(48.dp)
            )
            Slider(
                value = simulatedWidth,
                onValueChange = { simulatedWidth = it },
                valueRange = 320f..900f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${simulatedWidth.roundToInt()}dp",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.width(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        CompositionLocalProvider(LocalUiMediaScope provides simulatedScope) {
            PlainQueryProbe(counter = plainCounter)
            Spacer(modifier = Modifier.height(6.dp))
            DerivedQueryProbe(counter = derivedCounter)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "mediaQuery 리컴포지션 ${plainCounter.count}회 / derivedMediaQuery ${derivedCounter.count}회",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6A1B9A)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(onClick = {
            plainCounter.reset()
            derivedCounter.reset()
        }) {
            Text(text = "카운터 초기화", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "mediaQuery 는 질의를 그 자리에서 평가해 Boolean 을 돌려주므로, 람다가 읽은 값이 1dp만 바뀌어도 " +
                    "호출한 컴포저블이 다시 컴포즈됩니다. derivedMediaQuery 는 derivedStateOf 로 감싸 State<Boolean> 을 " +
                    "돌려주므로 결과가 실제로 뒤집힐 때만 읽는 쪽을 깨웁니다. 슬라이더를 기준선 한쪽에서만 움직여 보면 " +
                    "왼쪽 카운터만 계속 오르는 것이 보입니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            code = """
                // 값이 바뀔 때마다 리컴포지션
                val isWide: Boolean = mediaQuery { windowWidth >= 600.dp }

                // 결과가 뒤집힐 때만 리컴포지션
                val isWide: Boolean by derivedMediaQuery { windowWidth >= 600.dp }
            """.trimIndent(),
            borderColor = Color(0xFF6A1B9A)
        )
    }
}

/** mediaQuery 를 호출하는 자식 — 질의 값이 바뀔 때마다 리컴포지션된다. */
@OptIn(ExperimentalMediaQueryApi::class)
@Composable
private fun PlainQueryProbe(counter: CompositionCounter) {
    val isWide = mediaQuery { windowWidth >= BREAKPOINT }
    // 컴포지션이 성공한 뒤에 세야 컴포지션 도중 상태 쓰기가 되지 않는다
    SideEffect { counter.increment() }
    ProbeRow(
        label = "mediaQuery",
        result = isWide,
        color = Color(0xFFC62828)
    )
}

/** derivedMediaQuery 를 호출하는 자식 — 결과 Boolean 이 뒤집힐 때만 리컴포지션된다. */
@OptIn(ExperimentalMediaQueryApi::class)
@Composable
private fun DerivedQueryProbe(counter: CompositionCounter) {
    val isWide by derivedMediaQuery { windowWidth >= BREAKPOINT }
    SideEffect { counter.increment() }
    ProbeRow(
        label = "derivedMediaQuery",
        result = isWide,
        color = Color(0xFF2E7D32)
    )
}

// ==================== 5. 선언적 분기 ====================

@OptIn(ExperimentalMediaQueryApi::class)
@Composable
private fun MediaQueryBranchCard(integrationEnabled: Boolean) {
    ExampleCard(title = "선언적 레이아웃 분기 (실동작)", titleColor = Color(0xFF00838F)) {
        if (!integrationEnabled) {
            UnavailableNotice()
            return@ExampleCard
        }

        val isWide = mediaQuery { windowWidth >= BREAKPOINT }
        val isTabletop = mediaQuery { windowPosture == UiMediaScope.Posture.Tabletop }
        val isCoarsePointer = mediaQuery { pointerPrecision == UiMediaScope.PointerPrecision.Coarse }

        Text(
            text = "조건을 값에서 질의로 옮기면 분기 이유가 코드에 그대로 남습니다. 아래는 실제 기기 상태로 " +
                    "평가한 결과이며, 기기를 회전하거나 폴더블을 접으면 즉시 바뀝니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        StatusRow(label = "windowWidth >= ${BREAKPOINT.value.roundToInt()}dp", value = "$isWide", ok = isWide)
        Spacer(modifier = Modifier.height(4.dp))
        StatusRow(label = "posture == Tabletop", value = "$isTabletop", ok = isTabletop)
        Spacer(modifier = Modifier.height(4.dp))
        StatusRow(label = "pointer == Coarse", value = "$isCoarsePointer", ok = isCoarsePointer)

        Spacer(modifier = Modifier.height(12.dp))
        // 세 질의를 조합해 실제로 레이아웃을 고른다
        val layoutName = when {
            isTabletop -> "테이블탑 — 상단 콘텐츠 / 하단 컨트롤 분리"
            isWide -> "넓은 창 — 사이드바 + 본문 2단"
            else -> "좁은 창 — 단일 열"
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0F7FA), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "선택된 레이아웃: $layoutName",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006064)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "터치 기기라면 pointer == Coarse 가 true 이고, 마우스를 연결하면 Fine 으로 바뀝니다. " +
                    "\"터치니까 히트 영역을 키운다\" 같은 판단을 화면 크기와 무관하게 표현할 수 있습니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
    }
}

// ==================== 6. 정리 ====================

@Composable
private fun MediaQuerySummaryCard() {
    ExampleCard(title = "정리", titleColor = Color(0xFF37474F)) {
        val points = listOf(
            "활성화" to "isMediaQueryIntegrationEnabled 를 setContent 이전에 true 로",
            "우회 불가" to "obtainUiMediaScope() 는 internal — 직접 만들 수 없다",
            "질의 대상" to "크기 2 + 자세·포인터·키보드·시청거리 + 카메라/마이크",
            "mediaQuery" to "즉시 평가 Boolean. 값이 바뀔 때마다 리컴포지션",
            "derivedMediaQuery" to "State<Boolean>. 결과가 뒤집힐 때만 리컴포지션",
            "테스트" to "UiMediaScope 는 인터페이스 — 직접 구현해 provide 하면 대체 가능"
        )
        points.forEach { (title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F),
                    modifier = Modifier.width(96.dp)
                )
                Text(text = desc, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "이 예제의 범위 밖: MediaQuery 는 실험 API 라 @OptIn(ExperimentalMediaQueryApi::class) 이 " +
                    "필요하고, 플래그 이름과 UiMediaScope 속성 구성은 정식 출시 전에 바뀔 수 있습니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            code = """
                // 테스트/프리뷰에서 환경을 갈아끼우는 법
                class FakeScope(override val windowWidth: Dp) : UiMediaScope {
                    override val windowPosture = UiMediaScope.Posture.Tabletop
                    /* 나머지 6개 속성 ... */
                }

                CompositionLocalProvider(LocalUiMediaScope provides FakeScope(720.dp)) {
                    MyAdaptiveScreen()
                }
            """.trimIndent(),
            borderColor = Color(0xFF37474F)
        )
    }
}

// ==================== 시뮬레이션 / 측정 도구 ====================

/**
 * 폭만 외부에서 조작하는 시뮬레이션 스코프.
 *
 * windowWidth 를 getter 로 둬야 스냅샷 상태 읽기가 질의 시점에 일어난다.
 * 값으로 고정하면 슬라이더를 움직여도 질의 결과가 갱신되지 않는다.
 */
@OptIn(ExperimentalMediaQueryApi::class)
@Stable
private class SimulatedUiMediaScope(private val widthProvider: () -> Dp) : UiMediaScope {
    override val windowWidth: Dp get() = widthProvider()
    override val windowHeight: Dp = 800.dp
    override val windowPosture: UiMediaScope.Posture = UiMediaScope.Posture.Flat
    override val pointerPrecision: UiMediaScope.PointerPrecision =
        UiMediaScope.PointerPrecision.Coarse
    override val keyboardKind: UiMediaScope.KeyboardKind = UiMediaScope.KeyboardKind.Virtual
    override val viewingDistance: UiMediaScope.ViewingDistance = UiMediaScope.ViewingDistance.Near
    override val hasCamera: Boolean = true
    override val hasMicrophone: Boolean = true
}

/**
 * 리컴포지션 횟수 카운터.
 *
 * @Stable 을 붙여야 이 객체를 파라미터로 받는 자식이 skippable 로 남는다.
 * 그렇지 않으면 카운터가 오를 때마다 자식이 다시 컴포즈돼 무한 루프가 된다.
 */
@Stable
private class CompositionCounter {
    var count by mutableIntStateOf(0)
        private set

    fun increment() {
        count++
    }

    fun reset() {
        count = 0
    }
}

@OptIn(ExperimentalMediaQueryApi::class)
private fun postureLabel(posture: UiMediaScope.Posture): String = when (posture) {
    UiMediaScope.Posture.Flat -> "Flat (평평 — 접히지 않은 상태)"
    UiMediaScope.Posture.Tabletop -> "Tabletop (가로 힌지로 반 접힘)"
    UiMediaScope.Posture.Book -> "Book (세로 힌지로 반 접힘)"
    else -> posture.toString()
}

@OptIn(ExperimentalMediaQueryApi::class)
private fun pointerPrecisionLabel(precision: UiMediaScope.PointerPrecision): String =
    when (precision) {
        UiMediaScope.PointerPrecision.Fine -> "Fine (마우스·스타일러스)"
        UiMediaScope.PointerPrecision.Coarse -> "Coarse (손가락 터치)"
        UiMediaScope.PointerPrecision.Blunt -> "Blunt (TV 리모컨 등 저정밀)"
        UiMediaScope.PointerPrecision.None -> "None (포인터 없음)"
        else -> precision.toString()
    }

@OptIn(ExperimentalMediaQueryApi::class)
private fun keyboardKindLabel(kind: UiMediaScope.KeyboardKind): String = when (kind) {
    UiMediaScope.KeyboardKind.Physical -> "Physical (물리 키보드 연결)"
    UiMediaScope.KeyboardKind.Virtual -> "Virtual (소프트 키보드 표시 중)"
    UiMediaScope.KeyboardKind.None -> "None (키보드 없음)"
    else -> kind.toString()
}

@OptIn(ExperimentalMediaQueryApi::class)
private fun viewingDistanceLabel(distance: UiMediaScope.ViewingDistance): String = when (distance) {
    UiMediaScope.ViewingDistance.Near -> "Near (폰·태블릿 — 손 거리)"
    UiMediaScope.ViewingDistance.Medium -> "Medium (자동차 디스플레이 등)"
    UiMediaScope.ViewingDistance.Far -> "Far (TV — 10-foot UI)"
    else -> distance.toString()
}

// ==================== 공용 컴포넌트 ====================

@Composable
private fun ExampleCard(
    title: String,
    titleColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

/** 플래그가 꺼져 있어 실제 값을 질의할 수 없을 때 표시 */
@Composable
private fun UnavailableNotice() {
    Text(
        text = "isMediaQueryIntegrationEnabled 가 false 라 LocalUiMediaScope 가 제공되지 않습니다. " +
                "이 상태에서 mediaQuery { } 를 호출하면 IllegalStateException 이 나므로 카드를 비워 둡니다.",
        fontSize = 12.sp,
        color = Color(0xFFC62828),
        lineHeight = 17.sp
    )
}

@Composable
private fun ValueRow(name: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF37474F),
            modifier = Modifier.width(132.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (ok) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (ok) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}

@Composable
private fun ProbeRow(label: String, result: Boolean, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "isWide = $result",
            fontSize = 12.sp,
            color = Color(0xFF424242)
        )
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFE0E0E0),
            lineHeight = 16.sp
        )
    }
}
