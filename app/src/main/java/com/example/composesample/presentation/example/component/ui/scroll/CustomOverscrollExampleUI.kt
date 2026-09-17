package com.example.composesample.presentation.example.component.ui.scroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.withoutEventHandling
import androidx.compose.foundation.withoutVisualEffect
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * Custom Overscroll Example
 *
 * 스크롤 컨테이너가 끝에 닿은 뒤 남는 delta 를 누가 처리하는지를 네 가지 경로로 대조한다.
 * - 기본: LocalOverscrollFactory 가 만들어 주는 안드로이드 기본 이펙트(글로우/스트레치)
 * - 커스텀: OverscrollEffect 를 직접 구현해 고무줄(translate)로 교체
 * - CompositionLocal: LocalOverscrollFactory 를 갈아끼워 하위 트리 전체에 적용하거나 null 로 완전히 끄기
 * - 분리: withoutVisualEffect() / withoutEventHandling() 로 "이벤트만"·"그림만" 을 서로 다른 컴포저블에 나눠 붙이기
 *
 * 어느 모드든 같은 ProbingOverscrollEffect 로 감싸서 applyToScroll/applyToFling 이 실제로
 * 어떤 값을 받고 무엇을 남기는지 화면에서 실측한다.
 */

/** 고무줄 오버스크롤의 감쇠 계수 — 남은 delta 를 그대로 쓰면 손가락보다 화면이 더 많이 움직인다. */
private const val RubberBandDamping = 0.3f

/** 이 값보다 작은 delta 는 잔떨림으로 보고 무시한다. */
private const val MinimumDelta = 0.5f

private enum class OverscrollMode(val label: String, val summary: String) {
    Default(
        label = "기본",
        summary = "LocalOverscrollFactory 가 주는 안드로이드 기본 이펙트. API 31+ 는 스트레치, 그 아래는 글로우."
    ),
    RubberBand(
        label = "고무줄(커스텀)",
        summary = "OverscrollEffect 직접 구현 — 남는 delta 를 Animatable 에 쌓고 DrawModifierNode 에서 translate 로 그린다."
    ),
    LocalFactory(
        label = "Local 팩토리",
        summary = "LocalOverscrollFactory 를 교체해 하위 트리의 모든 스크롤 컨테이너에 일괄 적용. null 을 넣으면 오버스크롤이 아예 사라진다."
    ),
    Split(
        label = "이벤트/그림 분리",
        summary = "리스트는 withoutVisualEffect() 로 이벤트만 처리하고, 헤더는 withoutEventHandling() 로 그림만 그린다."
    )
}

@Composable
fun CustomOverscrollExampleUI(onBackEvent: () -> Unit) {
    var mode by remember { mutableStateOf(OverscrollMode.Default) }
    var factoryEnabled by remember { mutableStateOf(true) }
    val probe = remember { OverscrollProbe() }

    // 모드를 바꾸면 이전 모드의 계측값이 그대로 남아 오해를 만들기 때문에 초기화한다.
    remember(mode, factoryEnabled) { probe.reset() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Custom Overscroll Example",
            onBackIconClicked = onBackEvent
        )

        OverscrollModeSelector(
            selected = mode,
            onSelect = { mode = it }
        )

        OverscrollDescriptionCard(
            mode = mode,
            factoryEnabled = factoryEnabled,
            onFactoryEnabledChange = { factoryEnabled = it }
        )

        OverscrollProbeCard(probe = probe)

        Box(modifier = Modifier.weight(1f)) {
            when (mode) {
                OverscrollMode.Default -> DefaultOverscrollDemo(probe)
                OverscrollMode.RubberBand -> RubberBandOverscrollDemo(probe)
                OverscrollMode.LocalFactory -> LocalFactoryOverscrollDemo(probe, factoryEnabled)
                OverscrollMode.Split -> SplitOverscrollDemo(probe)
            }
        }
    }
}

/**
 * applyToScroll/applyToFling 이 받은 값을 기록하는 계측 홀더.
 *
 * 오버스크롤은 "리스트가 못 먹고 남긴 delta" 로만 동작하기 때문에,
 * delta / 리스트 소비분 / 남은 분을 나란히 봐야 왜 효과가 걸리고 안 걸리는지 알 수 있다.
 */
private class OverscrollProbe {
    var applyToScrollCount by mutableIntStateOf(0)
    var userInputCount by mutableIntStateOf(0)
    var sideEffectCount by mutableIntStateOf(0)
    var applyToFlingCount by mutableIntStateOf(0)
    var lastDeltaY by mutableFloatStateOf(0f)
    var lastScrollConsumedY by mutableFloatStateOf(0f)
    var lastOverscrollDeltaY by mutableFloatStateOf(0f)
    var lastFlingVelocityY by mutableFloatStateOf(0f)
    var lastFlingRemainingY by mutableFloatStateOf(0f)
    var rubberBandOffsetPx by mutableFloatStateOf(0f)
    var inProgress by mutableStateOf(false)

    fun reset() {
        applyToScrollCount = 0
        userInputCount = 0
        sideEffectCount = 0
        applyToFlingCount = 0
        lastDeltaY = 0f
        lastScrollConsumedY = 0f
        lastOverscrollDeltaY = 0f
        lastFlingVelocityY = 0f
        lastFlingRemainingY = 0f
        rubberBandOffsetPx = 0f
        inProgress = false
    }
}

/**
 * 어떤 OverscrollEffect 든 감싸서 계측만 추가하는 데코레이터.
 *
 * node 를 위임 대상 것으로 그대로 돌려주므로 시각 효과는 원본과 동일하다.
 * 다만 같은 node 인스턴스를 두 곳에 붙이면 안 되므로, 이 래퍼를 붙였으면 원본은 따로 붙이지 않는다.
 */
private class ProbingOverscrollEffect(
    private val delegate: OverscrollEffect,
    private val probe: OverscrollProbe,
) : OverscrollEffect {

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        probe.applyToScrollCount++
        if (source == NestedScrollSource.UserInput) {
            probe.userInputCount++
        } else {
            probe.sideEffectCount++
        }
        probe.lastDeltaY = delta.y

        var scrollConsumed = Offset.Zero
        val consumed = delegate.applyToScroll(delta, source) { available ->
            performScroll(available).also { scrollConsumed = it }
        }

        probe.lastScrollConsumedY = scrollConsumed.y
        probe.lastOverscrollDeltaY = delta.y - scrollConsumed.y
        probe.inProgress = delegate.isInProgress
        return consumed
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        probe.applyToFlingCount++
        probe.lastFlingVelocityY = velocity.y

        var flingConsumed = Velocity.Zero
        delegate.applyToFling(velocity) { available ->
            performFling(available).also { flingConsumed = it }
        }

        probe.lastFlingRemainingY = velocity.y - flingConsumed.y
        probe.inProgress = delegate.isInProgress
    }

    override val isInProgress: Boolean
        get() = delegate.isInProgress

    override val node: DelegatableNode
        get() = delegate.node
}

/**
 * 고무줄 오버스크롤 — 리스트가 소비하지 못한 delta 를 Animatable 에 누적하고 그만큼 콘텐츠를 밀어서 그린다.
 *
 * 처리 순서가 핵심이다.
 * 1) 이미 늘어난 상태에서 반대 방향 입력이 오면 리스트보다 고무줄을 **먼저** 되감는다.
 *    이 단계를 빼면 고무줄이 늘어난 채로 리스트가 먼저 움직여 손가락과 화면이 어긋난다.
 * 2) 남은 delta 를 performScroll 로 리스트에 넘기고, 리스트가 못 먹은 나머지만 고무줄로 보낸다.
 * 3) 플링은 리스트 처리 후 남은 속도를 initialVelocity 로 받아 0 으로 되돌린다.
 */
private class RubberBandOverscrollEffect(
    private val scope: CoroutineScope,
    private val probe: OverscrollProbe,
) : OverscrollEffect {

    private val overscrollOffset = Animatable(0f)

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        val sameDirection = sign(delta.y) == sign(overscrollOffset.value)

        // 1) 되감기 — 늘어난 고무줄이 남아 있고 입력 방향이 반대일 때만.
        val consumedByPreScroll = if (abs(overscrollOffset.value) > MinimumDelta && !sameDirection) {
            val previousOffset = overscrollOffset.value
            val newOffset = previousOffset + delta.y
            if (sign(previousOffset) != sign(newOffset)) {
                // 부호가 뒤집혔다 = 고무줄이 0 을 지나쳤다. 0 으로 스냅하고 넘친 만큼만 소비로 보고한다.
                scope.launch { overscrollOffset.snapTo(0f) }
                Offset(x = 0f, y = delta.y + previousOffset)
            } else {
                scope.launch { overscrollOffset.snapTo(newOffset) }
                delta.copy(x = 0f)
            }
        } else {
            Offset.Zero
        }

        // 2) 리스트에 위임하고, 리스트가 못 먹은 만큼만 고무줄로.
        val leftForScroll = delta - consumedByPreScroll
        val consumedByScroll = performScroll(leftForScroll)
        val overscrollDelta = leftForScroll - consumedByScroll

        if (abs(overscrollDelta.y) > MinimumDelta && source == NestedScrollSource.UserInput) {
            scope.launch {
                overscrollOffset.snapTo(overscrollOffset.value + overscrollDelta.y * RubberBandDamping)
                probe.rubberBandOffsetPx = overscrollOffset.value
            }
        }

        probe.rubberBandOffsetPx = overscrollOffset.value
        return consumedByPreScroll + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        val consumed = performFling(velocity)
        val remaining = velocity - consumed
        overscrollOffset.animateTo(
            targetValue = 0f,
            initialVelocity = remaining.y,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
        probe.rubberBandOffsetPx = overscrollOffset.value
    }

    override val isInProgress: Boolean
        get() = overscrollOffset.value != 0f

    // Animatable.value 를 draw 안에서 읽으므로 값이 바뀌면 그리기만 다시 돈다(리컴포지션 없음).
    override val node: DelegatableNode = object : Modifier.Node(), DrawModifierNode {
        override fun ContentDrawScope.draw() {
            translate(top = overscrollOffset.value) {
                this@draw.drawContent()
            }
        }
    }
}

/**
 * LocalOverscrollFactory 에 꽂을 팩토리.
 *
 * OverscrollFactory 는 equals/hashCode 를 **추상 멤버로 강제**한다 —
 * CompositionLocal 값이 같은지 비교해 불필요한 이펙트 재생성을 막기 위해서다.
 */
private class RubberBandOverscrollFactory(
    private val scope: CoroutineScope,
    private val probe: OverscrollProbe,
) : OverscrollFactory {

    override fun createOverscrollEffect(): OverscrollEffect =
        ProbingOverscrollEffect(RubberBandOverscrollEffect(scope, probe), probe)

    override fun equals(other: Any?): Boolean =
        other is RubberBandOverscrollFactory && other.scope === scope && other.probe === probe

    override fun hashCode(): Int = 31 * scope.hashCode() + probe.hashCode()
}

@Composable
private fun DefaultOverscrollDemo(probe: OverscrollProbe) {
    // rememberOverscrollEffect() 는 LocalOverscrollFactory 가 null 이면 null 을 돌려준다.
    val defaultEffect = rememberOverscrollEffect()
    val probing = remember(defaultEffect, probe) {
        defaultEffect?.let { ProbingOverscrollEffect(it, probe) }
    }

    OverscrollSampleList(
        overscrollEffect = probing,
        hint = "끝까지 내린 뒤 더 당기면 기본 스트레치(또는 글로우)가 보인다. 위 계측판의 '리스트 소비'가 0 이 되는 순간부터 오버스크롤 delta 가 잡힌다."
    )
}

@Composable
private fun RubberBandOverscrollDemo(probe: OverscrollProbe) {
    val scope = rememberCoroutineScope()
    val effect = remember(scope, probe) {
        ProbingOverscrollEffect(RubberBandOverscrollEffect(scope, probe), probe)
    }

    OverscrollSampleList(
        overscrollEffect = effect,
        hint = "기본과 달리 콘텐츠가 통째로 밀린다. 손가락을 떼면 applyToFling 의 남은 속도로 되돌아온다."
    )
}

@Composable
private fun LocalFactoryOverscrollDemo(probe: OverscrollProbe, factoryEnabled: Boolean) {
    val scope = rememberCoroutineScope()
    val factory = remember(scope, probe) { RubberBandOverscrollFactory(scope, probe) }

    // null 을 제공하면 하위 트리의 스크롤 컨테이너가 오버스크롤 이펙트를 아예 만들지 않는다.
    CompositionLocalProvider(
        LocalOverscrollFactory provides (if (factoryEnabled) factory else null)
    ) {
        OverscrollSampleList(
            overscrollEffect = null,
            hint = if (factoryEnabled) {
                "LazyColumn 에 overscrollEffect 를 넘기지 않았는데도 고무줄이 걸린다 — CompositionLocal 로 내려온 팩토리가 만든 것이다."
            } else {
                "팩토리에 null 을 제공했다. 끝에서 더 당겨도 아무 반응이 없고 계측판의 호출 수도 늘지 않는다."
            }
        )
    }
}

@Composable
private fun SplitOverscrollDemo(probe: OverscrollProbe) {
    val scope = rememberCoroutineScope()
    val effect = remember(scope, probe) {
        ProbingOverscrollEffect(RubberBandOverscrollEffect(scope, probe), probe)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 그림만 — 이벤트는 받지 않지만 같은 이펙트의 상태를 그대로 그린다.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .overscroll(effect.withoutEventHandling())
                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "withoutEventHandling() — 그림만\n리스트를 당기면 이 헤더가 대신 움직인다",
                fontSize = 12.sp,
                color = Color(0xFF2E7D32)
            )
        }

        // 이벤트만 — 계측은 그대로 돌지만 리스트 자체는 밀리지 않는다.
        OverscrollSampleList(
            overscrollEffect = effect.withoutVisualEffect(),
            hint = "리스트는 withoutVisualEffect() 라 제자리에 있고, 위 초록 박스만 고무줄처럼 움직인다. 이벤트 처리와 시각 효과는 같은 이펙트여도 붙이는 곳을 나눌 수 있다."
        )
    }
}

@Composable
private fun OverscrollSampleList(
    overscrollEffect: OverscrollEffect?,
    hint: String,
) {
    val items = remember { (1..25).map { "항목 $it" } }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = hint,
            fontSize = 11.sp,
            color = Color(0xFF616161),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),
            overscrollEffect = overscrollEffect
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverscrollModeSelector(
    selected: OverscrollMode,
    onSelect: (OverscrollMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OverscrollMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                label = { Text(text = mode.label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFBBDEFB)
                )
            )
        }
    }
}

@Composable
private fun OverscrollDescriptionCard(
    mode: OverscrollMode,
    factoryEnabled: Boolean,
    onFactoryEnabledChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = mode.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mode.summary,
                fontSize = 11.sp,
                color = Color(0xFF1565C0)
            )

            if (mode == OverscrollMode.LocalFactory) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = factoryEnabled,
                        onCheckedChange = onFactoryEnabledChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (factoryEnabled) {
                            "LocalOverscrollFactory = 고무줄 팩토리"
                        } else {
                            "LocalOverscrollFactory = null (오버스크롤 비활성화)"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF0D47A1)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverscrollProbeCard(probe: OverscrollProbe) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "실측 — applyToScroll / applyToFling",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            ProbeRow(
                label = "applyToScroll 호출",
                value = "${probe.applyToScrollCount}회 (UserInput ${probe.userInputCount} / SideEffect ${probe.sideEffectCount})"
            )
            ProbeRow(
                label = "마지막 delta.y",
                value = "${probe.lastDeltaY.roundToInt()}px"
            )
            ProbeRow(
                label = "리스트 소비",
                value = "${probe.lastScrollConsumedY.roundToInt()}px"
            )
            ProbeRow(
                label = "오버스크롤로 남은 양",
                value = "${probe.lastOverscrollDeltaY.roundToInt()}px"
            )
            ProbeRow(
                label = "applyToFling 호출",
                value = "${probe.applyToFlingCount}회 · 속도 ${probe.lastFlingVelocityY.roundToInt()} / 남은 속도 ${probe.lastFlingRemainingY.roundToInt()}"
            )
            ProbeRow(
                label = "고무줄 오프셋",
                value = "${probe.rubberBandOffsetPx.roundToInt()}px (고무줄 계열 모드에서만)"
            )
            ProbeRow(
                label = "isInProgress",
                value = probe.inProgress.toString()
            )
        }
    }
}

@Composable
private fun ProbeRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF616161),
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomOverscrollExampleUIPreview() {
    CustomOverscrollExampleUI(onBackEvent = {})
}
