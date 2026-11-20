package com.example.composesample.presentation.example.component.ui.visibility

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListDefaults
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathHitTester
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.composesample.presentation.MainHeader
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun VisibilityExampleUI(
    onBackEvent: () -> Unit
) {
    // 3번 카드의 상태 — 카드가 스크롤로 빠졌다 돌아와도 결과가 남도록 화면 루트에 둔다
    val frameLab = remember { HiddenSpinnerFrameLab() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Visibility in Compose",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // 측정 창 안의 터치(스크롤 · 물결)는 그 자체가 프레임이라 결과를 무효로 표시한다 — 이벤트는 소비하지 않는다
                .pointerInput(frameLab) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                            frameLab.onUserTouch()
                        }
                    }
                },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConditionalCompositionCard() }
            item { AlphaModifierCard() }
            item { HiddenSpinnerFrameCard(frameLab) }
            item { AnimatedVisibilityCard() }
            item { CustomVisibilityModifierCard() }
        }
    }
}

@Composable
private fun ConditionalCompositionCard() {
    var isVisible by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🚫 조건부 컴포지션 (GONE)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "if 조건문으로 Composable 제거",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 토글 스위치
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "isVisible: $isVisible",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )

                Switch(
                    checked = isVisible,
                    onCheckedChange = { isVisible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFD32F2F)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 데모 영역
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD32F2F).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DemoBox("Box 1", Color(0xFFE3F2FD))

                    // 조건부 컴포지션 - GONE 효과
                    if (isVisible) {
                        DemoBox("Box 2 (조건부)", Color(0xFFFFCDD2))
                    }

                    DemoBox("Box 3", Color(0xFFE8F5E9))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isVisible) "✅ Box 2가 컴포지션에 포함됨" else "❌ Box 2가 컴포지션에서 제거됨 (공간 없음)",
                fontSize = 11.sp,
                color = Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
private fun AlphaModifierCard() {
    var isVisible by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "👻 Alpha Modifier (INVISIBLE)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Modifier.alpha(0f)로 투명하게 (공간 유지)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 토글 스위치
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "alpha: ${if (isVisible) "1f" else "0f"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )

                Switch(
                    checked = isVisible,
                    onCheckedChange = { isVisible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFE65100)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 데모 영역
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE65100).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DemoBox("Box 1", Color(0xFFE3F2FD))

                    // Alpha modifier - INVISIBLE 효과
                    Box(
                        modifier = Modifier.alpha(if (isVisible) 1f else 0f)
                    ) {
                        DemoBox("Box 2 (alpha)", Color(0xFFFFE0B2))
                    }

                    DemoBox("Box 3", Color(0xFFE8F5E9))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isVisible) "✅ Box 2가 보임 (alpha: 1f)" else "👻 Box 2가 투명 (alpha: 0f) - 공간은 유지됨!",
                fontSize = 11.sp,
                color = Color(0xFFE65100)
            )
        }
    }
}

/**
 * 숨긴 무한 애니메이션이 만드는 프레임(#177) · 안 보이는 곳에 둔 스피너(#183)
 *
 * indeterminate CircularProgressIndicator 를 1번 묶음 네 가지(컴포지션 · 배치를 바꾸는 방법)와
 * 2번 묶음 여덟 가지(덮개 · 창 밖 · 크기 0 · Lazy 목록 위치)로 숨기고 5초 동안
 * 프레임 콜백(메인 스레드가 VSync 에 깬 횟수) · 그린 프레임(창이 보낸 프레임) · 스피너 그리기 호출을 센다.
 */
@Composable
private fun HiddenSpinnerFrameCard(lab: HiddenSpinnerFrameLab) {
    val view = LocalView.current
    val window = remember(view) { view.context.findActivity()?.window }
    val refreshHz = remember(view) { view.display?.refreshRate ?: 60f }
    val meter = remember(window) { window?.let { FrameWindowMeter(it) } }
    DisposableEffect(meter) {
        onDispose { meter?.close() }
    }
    LaunchedEffect(lab.runToken) {
        if (meter != null) lab.execute(meter, refreshHz)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⏱️ 숨긴 스피너가 만드는 프레임",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SpinnerBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "무한 애니메이션은 alpha(0f) 로 숨겨도, 가리거나 화면 밖에 둬도 멈추지 않는다 — 5초 동안 세어 본다",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 자리는 고정 — 어떤 방법으로 숨겨도 카드 높이가 바뀌지 않는다
                Box(
                    modifier = Modifier
                        .size(SpinnerSlotSize)
                        .border(1.dp, SpinnerBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    HiddenSpinnerSlot(lab)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "보이기: ${lab.visible}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpinnerBlue
                    )
                    Text(
                        text = "주사율 ${refreshHz.roundToInt()} Hz → 5초에 VSync 약 ${(refreshHz * 5).roundToInt()}번",
                        fontSize = 11.sp,
                        color = Color(0xFF546E7A)
                    )
                }
                Switch(
                    checked = lab.visible,
                    onCheckedChange = { lab.visible = it },
                    enabled = !lab.running,
                    colors = SwitchDefaults.colors(checkedThumbColor = SpinnerBlue)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CaseGroup.entries.forEachIndexed { groupIndex, group ->
                Text(
                    text = "${groupIndex + 1}. ${group.label}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HideTechnique.entries.filter { it.group == group }.forEach { technique ->
                        FilterChip(
                            selected = lab.technique == technique,
                            onClick = { lab.technique = technique },
                            enabled = !lab.running,
                            label = { Text(text = technique.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFBBDEFB))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { lab.requestCurrent() },
                    enabled = !lab.running && meter != null,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "지금 상태 재기", fontSize = 12.sp)
                }
                CaseGroup.entries.forEachIndexed { groupIndex, group ->
                    val cases = casesOf(group)
                    OutlinedButton(
                        onClick = { lab.requestGroup(group) },
                        enabled = !lab.running && meter != null,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "${groupIndex + 1}번 ${cases.size}줄 차례로 (약 ${estimatedSeconds(cases)}초)", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 세는 동안 바뀌지 않는 글자 — 카운트다운을 그리면 그 자체가 프레임이 된다
            Text(
                text = if (meter == null) "이 화면은 Activity 창을 찾지 못해 잴 수 없다" else lab.status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (lab.running) Color(0xFFD84315) else Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            HiddenSpinnerResultTable(lab)

            Spacer(modifier = Modifier.height(12.dp))

            SpinnerFact(
                title = "alpha 는 보이는 것만 바꾼다",
                body = "Modifier.alpha(0f) 는 graphicsLayer(alpha = 0f) 다. ui 1.12.1 에는 알파 0 레이어의 기록을 건너뛰는 지름길이 없고, " +
                    "스피너의 Canvas 는 애니메이션 값을 그리기 단계에서 읽는다. 값이 바뀔 때마다 그리기가 무효화되어 창이 쉬지 않고 새 프레임을 그린다."
            )
            SpinnerFact(
                title = "그리기를 멈춰도 시계는 돈다",
                body = "layout 에서 place 를 빼면 노드가 배치되지 않아 그려지지 않고, 값을 읽는 곳이 없다 → 그린 프레임 0. " +
                    "그래도 rememberInfiniteTransition 의 LaunchedEffect 는 컴포지션에 있는 한 매 프레임 withFrameNanos 를 다시 요청해 " +
                    "메인 스레드는 VSync 마다 깬다(JVM 테스트: 읽는 곳이 없어도 60틱에 60번 요청)."
            )
            SpinnerFact(
                title = "멈추려면 컴포지션에서 뺀다",
                body = "if (visible) 와 AnimatedVisibility(퇴장 애니메이션이 끝나면 내용을 뺀다)는 LaunchedEffect 를 취소해 셋 다 0 이다. " +
                    "대가는 remember 상태 — 다시 보이면 스피너는 처음부터 돈다. 자리는 고정 크기 상자로 지킨다."
            )
            SpinnerFact(
                title = "가려도 · 창 밖이어도 · 크기 0 이어도 그린다",
                body = "Compose 의 그리기 경로(NodeCoordinator.draw → 그리기 Modifier → 레이어 기록)에는 가림 · 화면 밖 · 크기 0 을 거르는 검사가 없다(ui 1.12.1). " +
                    "배치된 노드라면 불투명 형제로 덮든, 다른 창(Dialog)으로 앱 창 전체를 덮든, 100,000px 밖으로 밀든, size(0.dp) 로 줄이든 " +
                    "Canvas 는 매 프레임 다시 불리고 창도 매 프레임 새로 그린다 — 셋 다 계속. " +
                    "이 앱의 실물: 예제 화면에 덮인 목록의 검색창 커서가 5초에 그린 프레임 10 을 만들었다(#177 측정 중 발견, 지금은 고침)."
            )
            SpinnerFact(
                title = "Lazy 목록 — 컴포지션에 남은 칸은 시계를 돌린다",
                body = "화면 밖 칸은 배치되지 않아 그리기는 0 이다. 그래도 칸이 컴포지션에 남아 있으면 무한 트랜지션이 매 프레임 프레임을 요청한다. 남는 경우는 셋. " +
                    "① 멈추기 직전 마지막 측정에서 화면 밖으로 나간 칸 — LazyListMeasure 는 스크롤 delta 만큼 앞 칸을 컴포즈 · 측정해 보고 화면 밖이면 버린다. " +
                    "이번 측정에서 쓴 칸이라 재사용 풀로 가지 않고, 목록이 다시 측정할 때(다음 스크롤 등)까지 남는다. " +
                    "② 스크롤 방향 앞쪽 기본 캐시 창 안(compose 까지 끝낸다 — 1.13 은 보이는 아이템 평균 크기만큼, 뷰포트의 10~50%) " +
                    "③ 직접 준 캐시 창(LazyLayoutCacheWindow)의 지나간 쪽 안. " +
                    "같은 1.5칸을 끌어도 캐시 창이 없으면 다음 측정에서 재사용 풀로 가며 비활성화되어 셋 다 0. " +
                    "그린 프레임이 0 이라 GPU 렌더링 막대 · gfxinfo 에는 안 보이고 프레임 콜백(doFrame)에만 보인다."
            )
            SpinnerFact(
                title = "원문",
                body = "RevenueCat 페이월 버튼이 진행 표시를 알파 0 으로 숨겨 두었다 — 손대지 않은 페이월이 5초에 302~303 프레임. " +
                    "if (animatedProgressAlpha > 0f) 로 감싸자 0 (페이드아웃 동안만 컴포지션에 남는다)."
            )
            SpinnerFact(
                title = "세는 법",
                body = "프레임 콜백 = 메인 Looper 메시지 로그의 Choreographer\$FrameDisplayEventReceiver 디스패치, " +
                    "그린 프레임 = Window FrameMetrics(겨냥한 VSync 가 창 안인 것). 창의 시작 · 끝은 Handler 메시지로 정한다 — " +
                    "Compose 메인 디스패처는 코루틴을 이어 갈 때마다 프레임 콜백도 요청해서, 코루틴으로 재면 측정이 프레임 하나를 만든다. " +
                    "개발자 옵션 '애니메이터 길이 배율' 이 0 이면 무한 트랜지션이 끝 값으로 건너뛰고 멈춰 모든 줄이 0 이 된다."
            )
        }
    }
}

/** 고른 방법으로 스피너를 숨긴다 — 스피너 그리기 호출은 스피너 노드 안쪽에서 센다 */
@Composable
private fun HiddenSpinnerSlot(lab: HiddenSpinnerFrameLab) {
    val counted = Modifier.countSpinnerDraws(lab)
    when (lab.technique) {
        HideTechnique.Alpha -> CircularProgressIndicator(
            modifier = Modifier
                .alpha(if (lab.visible) 1f else 0f)
                .then(counted),
            color = SpinnerBlue
        )

        HideTechnique.NotPlaced -> CircularProgressIndicator(
            modifier = Modifier
                .placedOnlyWhen(lab.visible)
                .then(counted),
            color = SpinnerBlue
        )

        HideTechnique.IfVisible -> if (lab.visible) {
            CircularProgressIndicator(modifier = counted, color = SpinnerBlue)
        }

        HideTechnique.Animated -> AnimatedVisibility(
            visible = lab.visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(modifier = counted, color = SpinnerBlue)
        }

        // 같은 창 · 같은 부모 안의 불투명 형제 — 나중에 그려 스피너를 완전히 덮는다
        HideTechnique.Covered -> Box {
            CircularProgressIndicator(modifier = counted, color = SpinnerBlue)
            if (!lab.visible) {
                Box(modifier = Modifier.matchParentSize().background(CoverGray))
            }
        }

        // 다른 창(Dialog)이 앱 창 전체를 덮는다 — 스피너는 아래 창에 그대로 있다
        HideTechnique.DialogCovered -> {
            CircularProgressIndicator(modifier = counted, color = SpinnerBlue)
            if (!lab.visible) CoverDialog(lab)
        }

        // 배치는 하되 창 밖 좌표로 — offset 람다는 배치 단계에서 읽어 리컴포지션 없이 옮긴다
        HideTechnique.OffWindow -> CircularProgressIndicator(
            modifier = Modifier
                .offset { IntOffset(if (lab.visible) 0 else OFF_WINDOW_PX, 0) }
                .then(counted),
            color = SpinnerBlue
        )

        // 배치는 하되 크기를 0 으로
        HideTechnique.ZeroSize -> CircularProgressIndicator(
            modifier = Modifier
                .then(if (lab.visible) Modifier else Modifier.size(0.dp))
                .then(counted),
            color = SpinnerBlue
        )

        HideTechnique.LazyReleased,
        HideTechnique.LazyLastPass,
        HideTechnique.LazyAhead,
        HideTechnique.LazyKept -> key(lab.technique) { LazySpinnerStrip(lab, lab.technique) }
    }
}

/** 스피너 노드 안쪽에서 그리기 호출을 센다 — 평범한 필드라 쓰기가 프레임을 부르지 않는다 */
private fun Modifier.countSpinnerDraws(lab: HiddenSpinnerFrameLab): Modifier = drawWithContent {
    lab.onSpinnerDraw()
    drawContent()
}

/** 덮개 창 — 세는 동안에는 닫히지 않는다(닫히면 보임 상태로 바뀌어 줄이 틀린다) */
@Composable
private fun CoverDialog(lab: HiddenSpinnerFrameLab) {
    Dialog(
        onDismissRequest = { if (!lab.running) lab.visible = true },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF263238)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "다른 창(Dialog)이 앱 창을 덮고 있다\n세는 동안에는 닫히지 않는다 — 끝나면 뒤로 가기로 닫는다",
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 한 칸짜리 LazyRow — 칸 너비 = 뷰포트 너비라 한 번에 한 아이템만 보인다. 스피너는 LAZY_SPINNER_INDEX 번 칸.
 * 숨길 때는 손가락 스크롤처럼 delta 가 있는 scrollBy 로 민다(미리 가져오기 · 캐시 창은 delta 로 움직인다).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazySpinnerStrip(lab: HiddenSpinnerFrameLab, technique: HideTechnique) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = LAZY_SPINNER_INDEX)
    // 캐시 창은 Compose 1.13 부터 LazyRow 매개변수로 준다(LazyListState 로 주는 방식은 deprecated).
    // key(technique) 안이라 이 분기는 바뀌지 않는다 — 분기 안 remember 가 안전하다
    val cacheWindow = if (technique == HideTechnique.LazyKept) {
        // 뒤 · 앞 모두 한 칸 — 지나간 칸을 한 칸 너비만큼 컴포지션에 남긴다
        remember { LazyLayoutCacheWindow(ahead = SpinnerSlotSize, behind = SpinnerSlotSize) }
    } else {
        // 1.13 기본: 보이는 아이템 평균 크기로 앞쪽만(뷰포트의 10~50%), 지나간 쪽은 0
        LazyListDefaults.cacheWindow(state)
    }
    LaunchedEffect(lab.visible) {
        // 보이기 = 스피너 칸으로 건너뛰기. 건너뛴 측정은 그 사이 칸을 컴포즈하지 않는다
        state.scrollToItem(LAZY_SPINNER_INDEX)
        if (lab.visible) return@LaunchedEffect
        when (technique) {
            // 한 번의 scrollBy = 한 번의 측정 — 그 측정이 스피너 칸을 컴포즈 · 측정한 뒤 화면 밖이라 버린다
            HideTechnique.LazyLastPass -> state.scrollBy(state.layoutInfo.viewportSize.width.toFloat())
            // 두 칸 건너뛴 뒤 스피너 쪽으로 한 칸 끌어 온다 — 첫 칸이 칸 경계에 맞아 스피너가 끄는 방향 바로 앞 칸이 된다.
            // 1.13 기본 캐시 창은 앞쪽을 보이는 아이템 평균 크기(뷰포트의 10~50%)만큼만 본다 — 이 한 칸 목록에서는 반 칸이라,
            // 첫 칸이 반쯤 걸친 채 멈추면(1.5칸 끌기) 스피너가 창 밖이 되어 미리 가져오지 않는다(1.12 는 다음 칸을 늘 가져왔다)
            HideTechnique.LazyAhead -> {
                state.scrollToItem(LAZY_SPINNER_INDEX + 2)
                state.dragSlots(-1f)
            }
            // 지나간 칸 · 캐시 창 안 칸 — 같은 거리(1.5칸)를 끌고, 캐시 창이 있느냐만 다르다
            else -> state.dragSlots(1.5f)
        }
    }
    LazyRow(
        state = state,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false,
        cacheWindow = cacheWindow
    ) {
        items(LAZY_ITEM_COUNT, contentType = { if (it == LAZY_SPINNER_INDEX) "spinner" else "empty" }) { index ->
            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                if (index == LAZY_SPINNER_INDEX) {
                    CircularProgressIndicator(modifier = Modifier.countSpinnerDraws(lab), color = SpinnerBlue)
                } else {
                    Text(text = "$index", fontSize = 11.sp, color = Color(0xFF90A4AE))
                }
            }
        }
    }
}

/** 손가락으로 끌듯 칸의 1/8 씩 프레임마다 민다 — 칸을 넘은 뒤에도 측정이 몇 번 더 돌아 지나간 칸을 내보낸다 */
private suspend fun LazyListState.dragSlots(slots: Float) {
    val stepPx = layoutInfo.viewportSize.width / DRAG_STEPS_PER_SLOT.toFloat() * sign(slots)
    repeat((abs(slots) * DRAG_STEPS_PER_SLOT).roundToInt()) {
        scrollBy(stepPx)
        withFrameNanos { }
    }
}

/**
 * 크기는 그대로 재고 안쪽을 배치하지 않는다(흔히 쓰는 Modifier.visible() 모양).
 * 노드는 가장 안쪽 좌표계까지 배치돼야 '배치됨' 이 되고(InnerNodeCoordinator.onAfterPlaceAt → onNodePlaced),
 * 부모는 배치된 자식만 그린다(InnerNodeCoordinator.performDraw 의 isPlaced) — ui 1.12.1 소스 · 실측(그리기 0)
 */
private fun Modifier.placedOnlyWhen(placed: Boolean): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        if (placed) placeable.place(0, 0)
    }
}

@Composable
private fun HiddenSpinnerResultTable(lab: HiddenSpinnerFrameLab) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "방법", modifier = Modifier.weight(1.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpinnerBlue)
            FrameCounter.entries.forEach { counter ->
                Text(
                    text = counter.label,
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpinnerBlue
                )
            }
        }
        FrameCase.entries.forEach { case ->
            val result = lab.results[case]
            if (!case.visible && case == FrameCase.entries.first { !it.visible && it.technique.group == case.technique.group }) {
                Text(
                    text = "${case.technique.group.ordinal + 1}. ${case.technique.group.label}",
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF546E7A)
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(
                    text = case.label,
                    modifier = Modifier.weight(1.6f),
                    fontSize = 11.sp,
                    color = Color(0xFF212121)
                )
                FrameCounter.entries.forEach { counter ->
                    val expected = expectedRate(case, counter)
                    Text(
                        text = when {
                            result == null -> "— (${expected.label})"
                            result.matches(counter) -> "${result.count(counter)}"
                            else -> "${result.count(counter)} (예상 ${expected.label})"
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        color = when {
                            result == null -> Color(0xFF90A4AE)
                            result.matches(counter) -> Color(0xFF2E7D32)
                            else -> Color(0xFFC62828)
                        }
                    )
                }
            }
            if (result != null && (result.touched || result.droppedReports > 0)) {
                Text(
                    text = buildString {
                        if (result.touched) append("세는 중 화면을 건드림 — 다시 재기 ")
                        if (result.droppedReports > 0) append("FrameMetrics 보고 누락 ${result.droppedReports}")
                    },
                    fontSize = 10.sp,
                    color = Color(0xFFC62828)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "괄호 안은 예상 — 계속은 창 안 VSync 수의 1/4 이상, 0 은 정확히 0",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun SpinnerFact(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Text(text = body, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 18.sp)
    }
}

private val SpinnerBlue = Color(0xFF1565C0)
private val CoverGray = Color(0xFFCFD8DC)
private val SpinnerSlotSize = 56.dp

/** 창 밖 좌표(px) — 어떤 화면 폭보다도 멀다 */
private const val OFF_WINDOW_PX = 100_000

/** 한 칸 LazyRow 의 아이템 수와 스피너 칸 */
private const val LAZY_ITEM_COUNT = 8
private const val LAZY_SPINNER_INDEX = 3
private const val DRAG_STEPS_PER_SLOT = 8

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun AnimatedVisibilityCard() {
    var isVisible by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✨ AnimatedVisibility",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "애니메이션과 함께 표시/숨김",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 토글 스위치
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "visible: $isVisible",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Switch(
                    checked = isVisible,
                    onCheckedChange = { isVisible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF2E7D32)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 데모 영역
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DemoBox("Box 1", Color(0xFFE3F2FD))

                    // AnimatedVisibility
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                    ) {
                        DemoBox("Box 2 (Animated)", Color(0xFFC8E6C9))
                    }

                    DemoBox("Box 3", Color(0xFFE8F5E9))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "fadeIn + expandVertically / fadeOut + shrinkVertically",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

/**
 * 커스텀 Visibility + PathHitTester 예제
 *
 * PathHitTester는 임의의 Path 내부에 특정 좌표가 포함되는지 확인하는 API
 * - 별 모양 Path를 탭하면 PathHitTester로 hit test 후 색상 변경
 */
@Composable
private fun CustomVisibilityModifierCard() {
    var selectedVisibility by remember { mutableIntStateOf(0) }
    val visibilities = listOf("Visible", "Invisible", "Gone")

    // PathHitTester 관련 상태 - 각 Box별 탭 상태
    var box1Tapped by remember { mutableStateOf(false) }
    var box2Tapped by remember { mutableStateOf(false) }
    var box3Tapped by remember { mutableStateOf(false) }
    var tapResult by remember { mutableStateOf("별 모양을 탭해보세요!") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎨 커스텀 Modifier.visible() + PathHitTester",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "View 시스템과 유사한 API + 별 탭 시 색상 변경",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visibility 선택
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                visibilities.forEachIndexed { index, label ->
                    VisibilityOptionButton(
                        label = label,
                        isSelected = selectedVisibility == index,
                        onClick = { selectedVisibility = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 데모 영역 - Canvas로 별 모양 Box 그리기
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(1.dp, Color(0xFF7B1FA2).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedVisibility) {
                            detectTapGestures { offset ->
                                val starSize = 35f
                                val boxHeight = 50f
                                val boxPadding = 12f
                                val starCenterX = size.width - boxPadding - starSize - 8f

                                // Box 1 별 Path
                                val star1Path = createStarPath(
                                    centerX = starCenterX,
                                    centerY = boxPadding + boxHeight / 2,
                                    outerRadius = starSize,
                                    innerRadius = starSize * 0.5f
                                )
                                val star1HitTester = PathHitTester(star1Path)

                                // Box 2 별 Path (Visible일 때만)
                                val star2Path = createStarPath(
                                    centerX = starCenterX,
                                    centerY = boxPadding + boxHeight + 8f + boxHeight / 2,
                                    outerRadius = starSize,
                                    innerRadius = starSize * 0.5f
                                )
                                val star2HitTester = PathHitTester(star2Path)

                                // Box 3 별 Path - Gone일 때는 위치가 다름
                                val box3Y = when (Visibility.entries[selectedVisibility]) {
                                    Visibility.Gone -> boxPadding + boxHeight + 8f + boxHeight / 2
                                    else -> boxPadding + (boxHeight + 8f) * 2 + boxHeight / 2
                                }
                                val star3Path = createStarPath(
                                    centerX = starCenterX,
                                    centerY = box3Y,
                                    outerRadius = starSize,
                                    innerRadius = starSize * 0.5f
                                )
                                val star3HitTester = PathHitTester(star3Path)

                                // Hit test
                                when {
                                    offset in star1HitTester -> {
                                        box1Tapped = !box1Tapped
                                        tapResult = if (box1Tapped) "⭐ Box 1 별 활성화!" else "Box 1 별 비활성화"
                                    }
                                    selectedVisibility != 2 && offset in star2HitTester -> {
                                        // Gone이 아닐 때만 Box 2 hit test
                                        box2Tapped = !box2Tapped
                                        tapResult = if (box2Tapped) "⭐ Box 2 별 활성화!" else "Box 2 별 비활성화"
                                    }
                                    offset in star3HitTester -> {
                                        box3Tapped = !box3Tapped
                                        tapResult = if (box3Tapped) "⭐ Box 3 별 활성화!" else "Box 3 별 비활성화"
                                    }
                                    else -> {
                                        tapResult = "별 외부를 탭했습니다"
                                    }
                                }
                            }
                        }
                ) {
                    val starSize = 35f
                    val boxHeight = 50f
                    val boxPadding = 12f
                    val boxWidth = size.width - boxPadding * 2
                    val starCenterX = size.width - boxPadding - starSize - 8f

                    // Box 1
                    drawRoundRect(
                        color = if (box1Tapped) Color(0xFFBBDEFB) else Color(0xFFE3F2FD),
                        topLeft = Offset(boxPadding, boxPadding),
                        size = Size(boxWidth, boxHeight),
                        cornerRadius = CornerRadius(8f)
                    )
                    val star1Path = createStarPath(starCenterX, boxPadding + boxHeight / 2, starSize, starSize * 0.5f)
                    drawPath(star1Path, if (box1Tapped) Color(0xFFFFD700) else Color(0xFFFFC107), style = Fill)
                    drawPath(star1Path, Color(0xFFFF8F00), style = Stroke(width = 2f))

                    // Box 2 (Visibility에 따라)
                    val box2Y = boxPadding + boxHeight + 8f
                    when (Visibility.entries[selectedVisibility]) {
                        Visibility.Visible -> {
                            drawRoundRect(
                                color = if (box2Tapped) Color(0xFFD1C4E9) else Color(0xFFE1BEE7),
                                topLeft = Offset(boxPadding, box2Y),
                                size = Size(boxWidth, boxHeight),
                                cornerRadius = CornerRadius(8f)
                            )
                            val star2Path = createStarPath(starCenterX, box2Y + boxHeight / 2, starSize, starSize * 0.5f)
                            drawPath(star2Path, if (box2Tapped) Color(0xFFFFD700) else Color(0xFFFFC107), style = Fill)
                            drawPath(star2Path, Color(0xFFFF8F00), style = Stroke(width = 2f))
                        }
                        Visibility.Invisible -> {
                            // alpha 0f - 공간은 차지하지만 보이지 않음 (테두리만 표시)
                            drawRoundRect(
                                color = Color(0xFFE1BEE7).copy(alpha = 0.2f),
                                topLeft = Offset(boxPadding, box2Y),
                                size = Size(boxWidth, boxHeight),
                                cornerRadius = CornerRadius(8f),
                                style = Stroke(width = 1f)
                            )
                        }
                        Visibility.Gone -> {
                            // 아무것도 그리지 않음 - 공간도 없음
                        }
                    }

                    // Box 3 - Gone일 때는 위로 올라감
                    val box3Y = when (Visibility.entries[selectedVisibility]) {
                        Visibility.Gone -> box2Y
                        else -> boxPadding + (boxHeight + 8f) * 2
                    }
                    drawRoundRect(
                        color = if (box3Tapped) Color(0xFFC8E6C9) else Color(0xFFE8F5E9),
                        topLeft = Offset(boxPadding, box3Y),
                        size = Size(boxWidth, boxHeight),
                        cornerRadius = CornerRadius(8f)
                    )
                    val star3Path = createStarPath(starCenterX, box3Y + boxHeight / 2, starSize, starSize * 0.5f)
                    drawPath(star3Path, if (box3Tapped) Color(0xFFFFD700) else Color(0xFFFFC107), style = Fill)
                    drawPath(star3Path, Color(0xFFFF8F00), style = Stroke(width = 2f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 결과 표시
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEDE7F6)
            ) {
                Text(
                    text = tapResult,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7B1FA2),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (Visibility.entries[selectedVisibility]) {
                    Visibility.Visible -> "✅ Visible: 보임 + 공간 차지"
                    Visibility.Invisible -> "👻 Invisible: 안 보임 + 공간 차지 (점선 표시)"
                    Visibility.Gone -> "❌ Gone: 안 보임 + 공간 없음 (Box 3이 위로 이동)"
                },
                fontSize = 11.sp,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "PathHitTester로 별 모양 내부만 정확히 탭 감지",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DemoBox(text: String, color: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (text.contains("Invisible") || text.contains("alpha"))
                    Icons.Filled.Close
                else
                    Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF666666),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun VisibilityOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF7B1FA2) else Color(0xFFE1BEE7),
        animationSpec = tween(200),
        label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF7B1FA2),
        animationSpec = tween(200),
        label = "contentColor"
    )

    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

// 커스텀 Visibility enum
enum class Visibility {
    Visible,
    Invisible,
    Gone
}

/**
 * 별 모양 Path 생성
 */
private fun createStarPath(
    centerX: Float,
    centerY: Float,
    outerRadius: Float,
    innerRadius: Float,
    points: Int = 5
): Path {
    val path = Path()
    val angleStep = Math.PI / points

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - Math.PI / 2
        val x = centerX + (radius * kotlin.math.cos(angle)).toFloat()
        val y = centerY + (radius * kotlin.math.sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

