package com.example.composesample.presentation.example.component.ui.style

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleState
import androidx.compose.foundation.style.StyleStateKey
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.fillWidth
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.selected
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * Foundation Style API Example
 *
 * Compose 1.11 의 실험 API `androidx.compose.foundation.style` 를 시연한다.
 *
 * 이 API 의 핵심은 "디자인 토큰을 CompositionLocal 로 전파하는 것"이 아니라
 * **컴포넌트 하나의 상태별 스타일을 선언적으로 기술하는 것**이다 —
 * CSS 의 `:hover` / `:active` / `:focus` / `:checked` 에 대응하는 구조로,
 * `Modifier.styleable(state, style)` 한 줄이 상태에 따라 스스로 다시 그린다.
 *
 * 구성
 * - 1~6 카드: 실제 `androidx.compose.foundation.style` API (styleable / Style{} /
 *   상태 변형 / animate / 커스텀 StyleStateKey / 리컴포지션 대조)
 * - 7 카드: 디자인 토큰 전파 패턴 — **별개 주제**. 이전 버전의 이 예제가
 *   Style API 라고 잘못 설명하던 부분이며, 자체 구현 패턴으로 서술을 정정해 유지한다.
 */
@Composable
fun FoundationStyleApiExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Foundation Style API (Compose 1.11)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { RealApiOverviewCard() }
            item { StyleableLiveCard() }
            item { AppDrivenStateCard() }
            item { AnimateTransitionCard() }
            item { CustomStateKeyCard() }
            item { RecompositionCompareCard() }
            item { DesignTokenPatternCard() }
            item { SummaryCard() }
        }
    }
}

// ==================== 1. 실제 API 개요 ====================

@Composable
private fun RealApiOverviewCard() {
    ExampleCard(title = "Foundation Style API 란?", titleColor = Color(0xFF6750A4)) {
        Text(
            text = "androidx.compose.foundation.style 는 한 컴포넌트의 상태별 모양을 하나의 Style 객체에 " +
                    "모아 선언하고, Modifier.styleable(state, style) 로 붙이는 API 입니다. " +
                    "상태가 바뀌면 Modifier.Node 가 스타일을 다시 해석해 그리므로, " +
                    "if (isPressed) 같은 분기를 컴포지션에 두지 않아도 됩니다.",
            fontSize = 14.sp,
            color = Color(0xFF424242),
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        val surfaces = listOf(
            "Modifier.styleable" to "state + style 을 노드에 연결하는 진입점",
            "Style { }" to "StyleScope 리시버 람다. 배경·테두리·패딩·변형·텍스트 속성 선언",
            "상태 변형" to "pressed / hovered / focused / checked / selected / disabled",
            "animate(spec, style)" to "그 블록의 진입·이탈을 애니메이션으로 전이",
            "StyleStateKey<T>" to "앱이 정의하는 커스텀 상태 키",
            "rememberUpdatedStyleState" to "InteractionSource → StyleState 로 연결"
        )
        surfaces.forEach { (api, desc) ->
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4),
                    modifier = Modifier.width(150.dp)
                )
                Text(text = desc, fontSize = 12.sp, color = Color(0xFF424242))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock(
            code = """
                @OptIn(ExperimentalFoundationStyleApi::class)
                val interaction = remember { MutableInteractionSource() }
                val state = rememberUpdatedStyleState(interaction)
                val style = remember {
                    Style {
                        fillWidth()
                        contentPadding(16.dp)          // 내부 여백 (CSS padding)
                        background(Color(0xFF6750A4))
                        shape(RoundedCornerShape(12.dp))
                        pressed(Style { background(Color(0xFFD32F2F)) })
                    }
                }

                Box(
                    Modifier
                        .clickable(interaction, indication = null) { }
                        .styleable(state, style)
                )
            """.trimIndent(),
            borderColor = Color(0xFF6750A4)
        )

        Spacer(modifier = Modifier.height(10.dp))
        NoticeBox(
            text = "서술 정정: 이 예제의 이전 버전은 Style API 를 '디자인 토큰을 단일 CompositionLocal 로 " +
                    "전파하는 패턴'이라고 설명했습니다. 그건 실제 API 와 다른 주제라서 아래 7번 카드로 분리하고 " +
                    "'앱 자체 토큰 전파 패턴'으로 서술을 고쳤습니다.",
            color = Color(0xFFB71C1C)
        )
    }
}

// ==================== 2. styleable 실동작 (포인터 상태) ====================

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
private fun StyleableLiveCard() {
    val interaction = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interaction)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Style 은 값 객체다. 매 컴포지션마다 새로 만들면 노드가 매번 다시 해석하므로 remember 로 유지한다.
    val boxStyle = remember {
        Style {
            fillWidth()
            contentPadding(20.dp)
            background(Color(0xFF6750A4))
            shape(RoundedCornerShape(14.dp))
            border(2.dp, Color(0xFF4A3880))

            // 눌린 동안
            pressed(
                Style {
                    background(Color(0xFFD32F2F))
                    scale(0.97f)
                }
            )
            // 마우스/스타일러스 호버 (터치 전용 기기에서는 발생하지 않음)
            hovered(Style { background(Color(0xFF7E57C2)) })
            // 포커스 (아래 버튼으로 요청)
            focused(Style { border(4.dp, Color(0xFFFFC107)) })
        }
    }

    ExampleCard(title = "styleable + Style{} 실동작", titleColor = Color(0xFF6750A4)) {
        Text(
            text = "아래 상자를 길게 눌러 보세요. 눌린 동안 배경색과 크기가 바뀝니다. " +
                    "포인터 상태(pressed/hovered/focused)는 clickable 에 넘긴 InteractionSource 를 " +
                    "styleable 노드가 직접 수집해 반영합니다.",
            fontSize = 13.sp,
            color = Color(0xFF616161),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .focusRequester(focusRequester)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = {}
                )
                .styleable(styleState, boxStyle),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "누르고 있어 보세요",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { focusRequester.requestFocus() }) {
                Text(text = "포커스 주기", fontSize = 12.sp)
            }
            OutlinedButton(onClick = { focusManager.clearFocus() }) {
                Text(text = "포커스 해제", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        StateBadgeRow(state = styleState)

        Spacer(modifier = Modifier.height(10.dp))
        TipBox(
            text = "위 배지는 상태를 컴포지션에서 읽으므로 이 카드가 리컴포지션됩니다. " +
                    "styleable 자체는 컴포지션을 깨우지 않습니다 — 6번 카드에서 그 차이를 셉니다."
        )
    }
}

/** StyleState 의 predefined 상태를 그대로 노출하는 배지 줄. */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
private fun StateBadgeRow(state: StyleState) {
    val badges = listOf(
        "pressed" to state.isPressed,
        "hovered" to state.isHovered,
        "focused" to state.isFocused,
        "enabled" to state.isEnabled,
        "selected" to state.isSelected,
        "checked" to state.isChecked
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        badges.forEach { (label, active) ->
            Box(
                modifier = Modifier
                    .background(
                        if (active) Color(0xFF2E7D32) else Color(0xFFE0E0E0),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color.White else Color(0xFF757575)
                )
            }
        }
    }
}

// ==================== 3. 앱이 제어하는 상태 + 선언 순서 함정 ====================

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
private fun AppDrivenStateCard() {
    var isChecked by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }
    var isEnabled by remember { mutableStateOf(true) }
    var checkedDeclaredLast by remember { mutableStateOf(true) }

    val interaction = remember { MutableInteractionSource() }
    // 블록은 컴포지션마다 실행되므로 앱 상태를 StyleState 로 밀어 넣는 통로가 된다.
    val styleState = rememberUpdatedStyleState(interaction) {
        it.isChecked = isChecked
        it.isSelected = isSelected
        it.isEnabled = isEnabled
    }

    // 같은 속성(background)을 두 상태가 동시에 요구할 때, 나중에 선언된 쪽이 이긴다.
    val checkedLastStyle = remember {
        Style {
            fillWidth()
            contentPadding(20.dp)
            background(Color(0xFF455A64))
            shape(RoundedCornerShape(14.dp))
            selected(Style { background(Color(0xFFEF6C00)) })
            checked(Style { background(Color(0xFF1565C0)) })
            disabled(Style { alpha(0.35f) })
        }
    }
    val selectedLastStyle = remember {
        Style {
            fillWidth()
            contentPadding(20.dp)
            background(Color(0xFF455A64))
            shape(RoundedCornerShape(14.dp))
            checked(Style { background(Color(0xFF1565C0)) })
            selected(Style { background(Color(0xFFEF6C00)) })
            disabled(Style { alpha(0.35f) })
        }
    }

    ExampleCard(title = "앱이 제어하는 상태 + 선언 순서 = 우선순위", titleColor = Color(0xFF00695C)) {
        Text(
            text = "checked / selected / enabled 는 InteractionSource 가 아니라 앱이 정합니다. " +
                    "rememberUpdatedStyleState 의 블록에서 MutableStyleState 에 대입하면 됩니다.",
            fontSize = 13.sp,
            color = Color(0xFF616161),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        ToggleRow(label = "isChecked (파랑)", value = isChecked) { isChecked = it }
        ToggleRow(label = "isSelected (주황)", value = isSelected) { isSelected = it }
        ToggleRow(label = "isEnabled", value = isEnabled) { isEnabled = it }
        ToggleRow(
            label = if (checkedDeclaredLast) "선언 순서: selected → checked" else "선언 순서: checked → selected",
            value = checkedDeclaredLast
        ) { checkedDeclaredLast = it }

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = {}
                )
                .styleable(
                    styleState,
                    if (checkedDeclaredLast) checkedLastStyle else selectedLastStyle
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isChecked && isSelected) {
                    if (checkedDeclaredLast) "둘 다 켜짐 → checked 승 (파랑)" else "둘 다 켜짐 → selected 승 (주황)"
                } else {
                    "checked / selected 를 켜 보세요"
                },
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        WarningBox(
            text = "함정: 상태 변형은 CSS 처럼 특이도(specificity)로 겨루지 않습니다. " +
                    "조건이 맞는 블록을 선언된 순서대로 그 자리에서 적용하므로 같은 속성은 " +
                    "마지막에 선언한 쪽이 최종값입니다. 두 상태를 모두 켠 채 위 순서 스위치를 뒤집어 보세요."
        )

        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            code = """
                val state = rememberUpdatedStyleState(interaction) {
                    it.isChecked = isChecked      // 블록은 @Composable — 리시버가 아니라 it 로 받는다
                    it.isSelected = isSelected
                    it.isEnabled = isEnabled
                }

                Style {
                    background(Gray)
                    selected(Style { background(Orange) })
                    checked(Style { background(Blue) })   // 나중 선언 → 둘 다 켜지면 이쪽이 최종
                    disabled(Style { alpha(0.35f) })      // isEnabled == false 일 때
                }
            """.trimIndent(),
            borderColor = Color(0xFF00695C)
        )
    }
}

// ==================== 4. animate() 선언적 전이 ====================

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
private fun AnimateTransitionCard() {
    var toggled by remember { mutableStateOf(false) }

    val animatedInteraction = remember { MutableInteractionSource() }
    val instantInteraction = remember { MutableInteractionSource() }
    val animatedState = rememberUpdatedStyleState(animatedInteraction) { it.isChecked = toggled }
    val instantState = rememberUpdatedStyleState(instantInteraction) { it.isChecked = toggled }

    val animatedStyle = remember {
        Style {
            fillWidth()
            contentPadding(18.dp)
            background(Color(0xFF546E7A))
            shape(RoundedCornerShape(12.dp))
            // 이 블록 안의 상태 전환은 spec 을 따라 보간된다
            animate(
                tween(700),
                Style {
                    checked(
                        Style {
                            background(Color(0xFF2E7D32))
                            scale(1.05f)
                        }
                    )
                }
            )
        }
    }
    val instantStyle = remember {
        Style {
            fillWidth()
            contentPadding(18.dp)
            background(Color(0xFF546E7A))
            shape(RoundedCornerShape(12.dp))
            checked(
                Style {
                    background(Color(0xFF2E7D32))
                    scale(1.05f)
                }
            )
        }
    }

    ExampleCard(title = "animate(spec, style) — 선언적 전이", titleColor = Color(0xFF1B5E20)) {
        Text(
            text = "같은 상태 전환을 두 상자에 동시에 겁니다. 위쪽은 animate 블록 안에 있어 700ms 로 " +
                    "보간되고, 아래쪽은 즉시 스냅됩니다. animateColorAsState 를 따로 붙이지 않아도 " +
                    "스타일 선언 안에서 전이가 끝납니다.",
            fontSize = 13.sp,
            color = Color(0xFF616161),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        ToggleRow(label = "isChecked", value = toggled) { toggled = it }
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.styleable(animatedState, animatedStyle),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "animate(tween(700))",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier.styleable(instantState, instantStyle),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "전이 없음 (즉시 반영)",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        TipBox(
            text = "animate(enterSpec, exitSpec, style) 오버로드로 진입/이탈 스펙을 따로 줄 수 있습니다. " +
                    "인자를 하나만 주면 양쪽에 같은 스펙이 쓰입니다."
        )
    }
}

// ==================== 5. 커스텀 StyleStateKey ====================

/** 앱이 정의하는 상태 키. 기본값은 생성자에 넣는다. */
@OptIn(ExperimentalFoundationStyleApi::class)
private val UrgentKey = StyleStateKey(false)

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
private fun CustomStateKeyCard() {
    var urgent by remember { mutableStateOf(false) }

    val interaction = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interaction) { it.set(UrgentKey, urgent) }

    val style = remember {
        Style {
            fillWidth()
            contentPadding(18.dp)
            background(Color(0xFF37474F))
            shape(RoundedCornerShape(12.dp))
            // predefined 상태가 아닌 앱 정의 상태 — 판정 람다를 직접 넘긴다
            state(UrgentKey, Style {
                background(Color(0xFFC62828))
                border(3.dp, Color(0xFFFFD54F))
            }) { key, state -> state.get(key) }
        }
    }

    ExampleCard(title = "커스텀 StyleStateKey", titleColor = Color(0xFFB71C1C)) {
        Text(
            text = "pressed/checked 같은 미리 정의된 상태로 부족하면 StyleStateKey<T> 를 만들어 " +
                    "앱 고유의 상태축을 추가할 수 있습니다. state(key, style) { key, state -> 판정 } 형태로 " +
                    "언제 적용할지도 직접 정합니다.",
            fontSize = 13.sp,
            color = Color(0xFF616161),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        ToggleRow(label = "urgent", value = urgent) { urgent = it }
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.styleable(styleState, style),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (urgent) "URGENT" else "평상시",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock(
            code = """
                private val UrgentKey = StyleStateKey(false)   // 기본값 false

                val state = rememberUpdatedStyleState(interaction) {
                    it.set(UrgentKey, urgent)
                }

                Style {
                    background(Gray)
                    state(UrgentKey, Style { background(Red) }) { key, state ->
                        state.get(key)          // 판정 람다
                    }
                }
            """.trimIndent(),
            borderColor = Color(0xFFB71C1C)
        )
    }
}

// ==================== 6. 리컴포지션 대조 ====================

@Composable
private fun RecompositionCompareCard() {
    val styleableCounter = remember { CompositionCounter() }
    val chainCounter = remember { CompositionCounter() }

    ExampleCard(title = "styleable vs 조건부 Modifier 체이닝 (실측)", titleColor = Color(0xFF4527A0)) {
        Text(
            text = "두 상자는 눌렀을 때 같은 모양이 되지만 상태를 읽는 위치가 다릅니다. " +
                    "왼쪽은 styleable 노드가 상태를 읽고, 오른쪽은 collectIsPressedAsState() 로 " +
                    "컴포지션에서 읽습니다. 각각을 눌러 보며 카운터가 어떻게 움직이는지 직접 확인해 보세요.",
            fontSize = 13.sp,
            color = Color(0xFF616161),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        StyleableProbe(counter = styleableCounter)
        Spacer(modifier = Modifier.height(10.dp))
        ModifierChainProbe(counter = chainCounter)

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "styleable 컴포지션 ${styleableCounter.count}회 / 조건부 체이닝 ${chainCounter.count}회",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4527A0)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(onClick = {
            styleableCounter.reset()
            chainCounter.reset()
        }) {
            Text(text = "카운터 초기화", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "styleable 은 상태를 Modifier.Node 안에서 읽으므로 누르는 동안에도 컴포지션이 깨지지 않습니다. " +
                    "반면 collectIsPressedAsState() 는 State<Boolean> 을 컴포지션에서 읽어 Modifier 를 새로 " +
                    "만들기 때문에 누를 때마다 그 컴포저블이 다시 컴포즈됩니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
    }
}

/** styleable 방식 — 상태를 노드가 읽는다. */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
private fun StyleableProbe(counter: CompositionCounter) {
    val interaction = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interaction)
    val style = remember {
        Style {
            fillWidth()
            contentPadding(16.dp)
            background(Color(0xFF5E35B1))
            shape(RoundedCornerShape(10.dp))
            pressed(Style { background(Color(0xFFD32F2F)) })
        }
    }

    // 컴포지션이 성공한 뒤에 세야 컴포지션 도중 상태 쓰기가 되지 않는다
    SideEffect { counter.increment() }

    Box(
        modifier = Modifier
            .clickable(interactionSource = interaction, indication = null, onClick = {})
            .styleable(styleState, style),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "styleable (노드에서 읽음)",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** 기존 방식 — 상태를 컴포지션에서 읽어 Modifier 를 조건부로 만든다. */
@Composable
private fun ModifierChainProbe(counter: CompositionCounter) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()

    SideEffect { counter.increment() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = {})
            .background(
                if (isPressed) Color(0xFFD32F2F) else Color(0xFF5E35B1),
                RoundedCornerShape(10.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "조건부 체이닝 (컴포지션에서 읽음)",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== 7. 디자인 토큰 전파 패턴 (별개 주제) ====================

@Composable
private fun DesignTokenPatternCard() {
    var selectedPreset by remember { mutableStateOf(TokenPreset.Light) }
    val currentTokens = remember(selectedPreset) { selectedPreset.toTokens() }

    ExampleCard(title = "부록: 디자인 토큰 전파 패턴", titleColor = Color(0xFF37474F)) {
        NoticeBox(
            text = "이 카드는 Foundation Style API 가 아닙니다. 앱 전역 토큰을 하나의 Immutable 객체 + " +
                    "단일 CompositionLocal 로 묶는 별개의 패턴이며, 이전 버전의 이 예제가 Style API 라고 " +
                    "잘못 설명하던 내용입니다. 두 주제는 층위가 달라 서로를 대체하지 않습니다 — " +
                    "토큰 전파는 앱 전역, Style API 는 컴포넌트 단위입니다.",
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TokenPreset.entries.forEach { preset ->
                val isSelected = preset == selectedPreset
                OutlinedButton(onClick = { selectedPreset = preset }) {
                    Text(
                        text = preset.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF6750A4) else Color(0xFF757575)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        LegacyTokenSection(preset = selectedPreset)

        Spacer(modifier = Modifier.height(12.dp))
        CompositionLocalProvider(LocalAppTokens provides currentTokens) {
            SingleLocalTokenSection()
        }

        Spacer(modifier = Modifier.height(12.dp))
        // 부분 오버라이드: copy() 로 spacing 만 줄인 자식 트리
        val denseTokens = remember(currentTokens) {
            currentTokens.copy(spacing = AppSpacing(xs = 2.dp, sm = 4.dp, md = 8.dp, lg = 12.dp))
        }
        Text(
            text = "copy() 부분 오버라이드 — spacing 만 교체, 나머지는 상속",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(8.dp))
        CompositionLocalProvider(LocalAppTokens provides currentTokens) {
            TokenSwatch(label = "기본 spacing.md = ${currentTokens.spacing.md}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        CompositionLocalProvider(LocalAppTokens provides denseTokens) {
            TokenSwatch(label = "dense spacing.md = ${denseTokens.spacing.md}")
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "MaterialTheme.colorScheme.primary = ${MaterialTheme.colorScheme.primary} " +
                    "(Material 토큰은 그대로 병행 사용)",
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun LegacyTokenSection(preset: TokenPreset) {
    // Legacy: 토큰 종류별로 Provider 를 따로 쌓는다 — Preset 별 매핑도 수동
    val accent = when (preset) {
        TokenPreset.Light -> Color(0xFF7D5260)
        TokenPreset.Dark -> Color(0xFFEFB8C8)
        TokenPreset.Brand -> Color(0xFFE29578)
    }
    val radius = when (preset) {
        TokenPreset.Brand -> 4.dp
        else -> 12.dp
    }

    CompositionLocalProvider(
        LocalLegacyAccentColor provides accent,
        LocalLegacyCornerRadius provides radius
    ) {
        Column {
            Text(
                text = "A. 토큰마다 Local 을 따로 두는 방식",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E24AA)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LegacySwatch()
        }
    }
}

@Composable
private fun LegacySwatch() {
    val accent = LocalLegacyAccentColor.current
    val radius = LocalLegacyCornerRadius.current
    val caption = LocalLegacyCaptionStyle.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(accent, RoundedCornerShape(radius))
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "Accent / Radius / Caption — Local 3개 조회",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "토큰 개수만큼 Local + Provider 가 필요",
                color = Color.White.copy(alpha = 0.9f),
                style = caption
            )
        }
    }
}

@Composable
private fun SingleLocalTokenSection() {
    val tokens = LocalAppTokens.current

    Column {
        Text(
            text = "B. 토큰을 하나로 묶어 Local 1개로 전파",
            style = tokens.typography.title,
            color = tokens.colors.primary
        )
        Spacer(modifier = Modifier.height(tokens.spacing.xs))
        Text(
            text = "LocalAppTokens.current 하나로 typography·colors·shapes·spacing 전부 접근. " +
                    "Preset 변경 시 객체만 교체하면 된다.",
            style = tokens.typography.body,
            color = tokens.colors.onSurface
        )
        Spacer(modifier = Modifier.height(tokens.spacing.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(tokens.colors.primary, tokens.shapes.large)
                .border(2.dp, tokens.colors.accent, tokens.shapes.large)
                .padding(tokens.spacing.md),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "primary + accent + shapes.large + spacing.md",
                    style = tokens.typography.body,
                    color = tokens.colors.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "토큰 4종 — 같은 객체에서 일관 조회",
                    style = tokens.typography.caption,
                    color = tokens.colors.onPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun TokenSwatch(label: String) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.colors.primary, tokens.shapes.medium)
            .padding(tokens.spacing.md)
    ) {
        Text(
            text = label,
            style = tokens.typography.body,
            color = tokens.colors.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== 8. 정리 ====================

@Composable
private fun SummaryCard() {
    ExampleCard(title = "정리", titleColor = Color(0xFF311B92)) {
        val rows = listOf(
            "상태 반영 위치" to "조건부 체이닝: 컴포지션 / styleable: Modifier.Node",
            "상태 소스" to "InteractionSource 를 styleable 노드가 직접 수집",
            "앱 상태 주입" to "rememberUpdatedStyleState 의 블록에서 MutableStyleState 에 대입",
            "우선순위" to "특이도 없음 — 선언 순서대로 적용, 같은 속성은 마지막 선언이 승",
            "전이" to "animate(spec, style) 로 블록 단위 진입·이탈 보간",
            "확장" to "StyleStateKey<T> + state(key, style) { 판정 } 으로 커스텀 상태축",
            "합성" to "Style(a, b) 또는 a then b 로 스타일 객체를 값처럼 조합",
            "주의" to "Style 은 remember 로 유지 / styleable(state) 단독 호출은 deprecated"
        )
        rows.forEach { (k, v) ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "• $k",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF311B92),
                    modifier = Modifier.weight(0.34f)
                )
                Text(
                    text = v,
                    fontSize = 12.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(0.66f)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        WarningBox(
            text = "전부 @OptIn(ExperimentalFoundationStyleApi::class) 가 필요한 실험 API 입니다. " +
                    "1.11 기준 시그니처이며 안정화 전까지 바뀔 수 있습니다."
        )
    }
}

// ==================== 토큰 전파 패턴용 모델 (앱 자체 구현) ====================

@Immutable
data class AppTypography(
    val title: TextStyle,
    val body: TextStyle,
    val caption: TextStyle
)

@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val surface: Color,
    val onSurface: Color,
    val accent: Color
)

@Immutable
data class AppShapes(
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape
)

@Immutable
data class AppSpacing(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp
)

/**
 * 디자인 토큰 묶음.
 *
 * 주의: Compose 의 `androidx.compose.foundation.style.Style` 과는 무관한 앱 자체 타입이다.
 * 혼동을 막기 위해 이름에서 "Style" 을 뺐다.
 */
@Immutable
data class AppTokens(
    val typography: AppTypography,
    val colors: AppColors,
    val shapes: AppShapes,
    val spacing: AppSpacing
)

// staticCompositionLocalOf: 값이 거의 바뀌지 않을 때 권장 (전체 트리 invalidate)
val LocalAppTokens = staticCompositionLocalOf<AppTokens> {
    error("LocalAppTokens 이 제공되지 않음 — Provider 로 감싸야 합니다.")
}

enum class TokenPreset(val label: String) {
    Light("Light"),
    Dark("Dark"),
    Brand("Brand");

    fun toTokens(): AppTokens = when (this) {
        Light -> lightTokens
        Dark -> darkTokens
        Brand -> brandTokens
    }
}

private val baseTypography = AppTypography(
    title = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    body = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    caption = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

private val baseShapes = AppShapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

private val baseSpacing = AppSpacing(xs = 4.dp, sm = 8.dp, md = 16.dp, lg = 24.dp)

private val lightTokens = AppTokens(
    typography = baseTypography,
    colors = AppColors(
        primary = Color(0xFF6750A4),
        onPrimary = Color.White,
        surface = Color(0xFFF7F2FA),
        onSurface = Color(0xFF1C1B1F),
        accent = Color(0xFF7D5260)
    ),
    shapes = baseShapes,
    spacing = baseSpacing
)

private val darkTokens = AppTokens(
    typography = baseTypography,
    colors = AppColors(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        accent = Color(0xFFEFB8C8)
    ),
    shapes = baseShapes,
    spacing = baseSpacing
)

private val brandTokens = AppTokens(
    typography = baseTypography.copy(
        title = baseTypography.title.copy(fontFamily = FontFamily.Serif),
        body = baseTypography.body.copy(fontFamily = FontFamily.Serif)
    ),
    colors = AppColors(
        primary = Color(0xFF006D77),
        onPrimary = Color.White,
        surface = Color(0xFFEDF6F9),
        onSurface = Color(0xFF003B47),
        accent = Color(0xFFE29578)
    ),
    shapes = AppShapes(
        small = RoundedCornerShape(2.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp)
    ),
    spacing = baseSpacing
)

// Legacy 비교용: 토큰을 개별 CompositionLocal 로 분리
private val LocalLegacyAccentColor = compositionLocalOf { Color(0xFF7D5260) }
private val LocalLegacyCornerRadius = compositionLocalOf { 12.dp }
private val LocalLegacyCaptionStyle = compositionLocalOf {
    TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
}

// ==================== 공통 UI 헬퍼 ====================

/**
 * 리컴포지션 횟수 카운터.
 *
 * @Stable 을 붙여야 이 객체를 파라미터로 받는 자식이 skippable 로 남는다.
 * 안 붙이면 카운터가 오를 때마다 자식이 재컴포즈돼 무한 루프가 된다.
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

@Composable
private fun ToggleRow(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f)
        )
        Switch(checked = value, onCheckedChange = onValueChange)
    }
}

@Composable
private fun NoticeBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = color, lineHeight = 17.sp)
    }
}

@Composable
private fun WarningBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color(0xFFE65100), lineHeight = 17.sp)
    }
}

@Composable
private fun TipBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color(0xFF2E7D32), lineHeight = 17.sp)
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
