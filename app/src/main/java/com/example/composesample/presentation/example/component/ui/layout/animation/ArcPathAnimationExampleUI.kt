package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.ArcAnimationSpec
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.DeferredTargetAnimation
import androidx.compose.animation.core.ExperimentalAnimatableApi
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.keyframesWithSpline
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.approachLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * 2D 경로 애니메이션 예제
 *
 * 기존 SpringTweenSnap / AnimationsShowcase 예제가 "시간에 따라 값이 어떻게 변하는가(1D)"를 다뤘다면,
 * 이 예제의 축은 "두 점 사이를 어떤 모양의 경로로 지나는가(2D)"다.
 *
 * 경로는 눈으로만 비교하면 착시가 생기므로, AnimationSpec 을 TargetBasedAnimation 으로 감싸
 * 실제 좌표를 샘플링해 화면에 그대로 그린다(= 화면의 곡선이 곧 스펙의 출력이다).
 */
@Composable
fun ArcPathAnimationExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                text = "2D 경로 애니메이션 (Arc / Spline)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InfoCard(
                    title = "이 예제의 축: 값이 아니라 경로",
                    description = "spring/tween/snap 은 \"시간 → 값\"의 모양(이징)을 정한다.\n" +
                            "여기서 다루는 스펙은 값이 Offset 같은 2D일 때 \"두 점 사이를 어떤 " +
                            "궤적으로 지나는가\"를 정한다.\n\n" +
                            "화면의 곡선은 그림이 아니라 실제 스펙 출력이다 — " +
                            "TargetBasedAnimation 으로 스펙을 감싸 getValueFromNanos() 로 " +
                            "121개 지점을 샘플링해 그린 것이다.",
                    bgColor = Color(0xFFE8F5E9)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("1. 직선 vs 호 — ArcAnimationSpec") }
            item { ArcModeDemo() }
            item {
                InfoCard(
                    title = "ArcMode 3종 (실측)",
                    description = "시작 (0,0) → 목표 (200,200) 을 1000ms 로 이동시켜 좌표를 측정한 결과:\n\n" +
                            "• ArcAbove — t=0.25 에서 (72.6, 13.7)\n" +
                            "  x 가 y 보다 크다 = 가로로 먼저 벌어졌다가 세로로 합류\n" +
                            "• ArcBelow — t=0.25 에서 (13.7, 72.6)\n" +
                            "  ArcAbove 의 거울상 = 세로로 먼저 벌어진다\n" +
                            "• ArcLinear — t=0.25 에서 (47.3, 47.3)\n" +
                            "  x 와 y 가 같다 = 직선. tween 과 좌표가 완전히 동일하다.\n\n" +
                            "즉 ArcLinear 는 \"호를 쓰지 않는\" 모드이고, 위/아래는 " +
                            "직선을 기준으로 어느 쪽으로 부풀지를 고른다.",
                    bgColor = Color(0xFFFFF8E1)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("2. 경유점 통과 — keyframes vs keyframesWithSpline") }
            item { SplineDemo() }
            item {
                InfoCard(
                    title = "둘 다 경유점을 통과한다 — 차이는 꺾임",
                    description = "같은 경유점을 주고 측정하면 두 스펙 모두 t=0.5 에서 경유점 좌표를 " +
                            "정확히 통과한다. 다른 것은 그 앞뒤다.\n\n" +
                            "• keyframes: 키프레임 사이를 직선으로 잇는다 → 경유점에서 각이 진다\n" +
                            "• keyframesWithSpline: 곡선으로 잇는다 → 경유점을 부드럽게 스쳐간다\n\n" +
                            "실측(경유점 앞 t=0.25): keyframes (50, -40) 은 정확히 중간값이지만, " +
                            "spline 은 (50, -62.5) 로 더 일찍 크게 휜다. 코너를 없애려고 " +
                            "구간 안쪽을 미리 굽히기 때문이다.\n\n" +
                            "keyframesWithSpline 은 실험 API 가 아니다 — opt-in 없이 쓸 수 있다.",
                    bgColor = Color(0xFFE3F2FD)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("3. 구간별로 다른 호 — keyframes + using ArcMode") }
            item { SegmentArcDemo() }
            item {
                InfoCard(
                    title = "using 은 \"그 키프레임에서 출발하는\" 구간에 붙는다",
                    description = "keyframes 블록 안에서 `값 at 시각 using ArcMode.X` 로 구간마다 다른 " +
                            "호를 줄 수 있다. 방향이 헷갈리기 쉬운데, 실측으로 확정된 규칙은:\n\n" +
                            "  0ms 키프레임에 ArcAbove → [0ms, 500ms] 구간이 위로 휜다\n" +
                            "  500ms 키프레임에 ArcBelow → [500ms, 1000ms] 구간이 아래로 휜다\n\n" +
                            "즉 들어오는 구간이 아니라 나가는 구간에 적용된다. " +
                            "마지막 키프레임에 붙이면 뒤에 구간이 없어 아무 효과가 없다.\n\n" +
                            "이 조합도 안정 API 다 — 아래 데모 함수에는 @OptIn 이 없다. " +
                            "opt-in 이 필요한 것은 ArcAnimationSpec 쪽뿐이다.",
                    bgColor = Color(0xFFEDE7F6)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("4. 타깃 보류 — DeferredTargetAnimation") }
            item { DeferredTargetDemo() }
            item {
                InfoCard(
                    title = "pendingTarget = 정해졌지만 아직 도달하지 않은 목표",
                    description = "레이아웃이 lookahead 로 \"최종 크기\"를 먼저 계산해도, 화면은 " +
                            "거기로 즉시 점프하지 않고 애니메이션으로 접근해야 한다. " +
                            "그 간극을 들고 있는 것이 이 객체다.\n\n" +
                            "측정된 계약:\n" +
                            "• 생성 직후 — pendingTarget=null, isIdle=true\n" +
                            "• 첫 updateTarget(A) — 반환 A. 애니메이션 없이 즉시 확정되고 isIdle 은 true 유지\n" +
                            "• 이후 updateTarget(B) — 반환은 아직 A(현재값)인데 pendingTarget 은 B, isIdle=false\n\n" +
                            "⚠️ updateTarget 에 넘기는 CoroutineScope 는 MonotonicFrameClock 을 " +
                            "가지고 있어야 한다. 없으면 내부 Animatable 이 프레임을 기다리다 " +
                            "IllegalStateException 으로 죽는다. 컴포지션에서 얻은 " +
                            "rememberCoroutineScope() 는 이 조건을 만족한다.",
                    bgColor = Color(0xFFFCE4EC)
                )
            }

            item { HorizontalDivider() }
            item {
                InfoCard(
                    title = "opt-in 요구 정리 (Compose 1.11.1 기준)",
                    description = "@OptIn 이 필요한 것:\n" +
                            "• ArcAnimationSpec → ExperimentalAnimationSpecApi\n" +
                            "• DeferredTargetAnimation → ExperimentalAnimatableApi\n\n" +
                            "opt-in 없이 쓸 수 있는 것:\n" +
                            "• keyframesWithSpline { }\n" +
                            "• keyframes { } 안의 `using ArcMode.X`\n\n" +
                            "같은 ArcMode 를 쓰는데도 한쪽만 실험 API 인 이유는 게이팅이 " +
                            "ArcMode 자체가 아니라 ArcAnimationSpec 클래스에 붙어 있기 때문이다.",
                    bgColor = Color(0xFFF3E5F5)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

/** 화면에 그릴 하나의 경로 — 스펙을 미리 샘플링한 좌표 목록을 들고 있다. */
private data class PathTrack(
    val label: String,
    val color: Color,
    val samples: List<Offset>,
    val dashed: Boolean = false
)

private const val PathSampleCount = 120

/**
 * AnimationSpec 을 실제로 실행하지 않고 좌표만 뽑아낸다.
 * 시작 (0,0) → 목표 (1,1) 의 정규화 공간에서 시간 균등 간격으로 샘플링하므로,
 * 인덱스 비율이 곧 재생 진행률이 된다.
 */
private fun sampleSpec(spec: AnimationSpec<Offset>): List<Offset> {
    val animation = TargetBasedAnimation(
        animationSpec = spec,
        typeConverter = Offset.VectorConverter,
        initialValue = Offset(0f, 0f),
        targetValue = Offset(1f, 1f)
    )
    val durationNanos = animation.durationNanos
    return List(PathSampleCount + 1) { index ->
        animation.getValueFromNanos(durationNanos * index / PathSampleCount)
    }
}

@OptIn(ExperimentalAnimationSpecApi::class)
@Composable
private fun ArcModeDemo() {
    val tracks = remember {
        listOf(
            PathTrack(
                label = "tween — 직선 (대조군)",
                color = Color(0xFF9E9E9E),
                samples = sampleSpec(tween(1000)),
                dashed = true
            ),
            PathTrack(
                label = "ArcAnimationSpec(ArcAbove)",
                color = Color(0xFF1976D2),
                samples = sampleSpec(ArcAnimationSpec(mode = ArcMode.ArcAbove, durationMillis = 1000))
            ),
            PathTrack(
                label = "ArcAnimationSpec(ArcBelow)",
                color = Color(0xFFE64A19),
                samples = sampleSpec(ArcAnimationSpec(mode = ArcMode.ArcBelow, durationMillis = 1000))
            ),
            PathTrack(
                label = "ArcAnimationSpec(ArcLinear) — 직선과 겹침",
                color = Color(0xFF43A047),
                samples = sampleSpec(ArcAnimationSpec(mode = ArcMode.ArcLinear, durationMillis = 1000))
            )
        )
    }
    PathComparisonCard(title = "Arc Demo", tracks = tracks)
}

@Composable
private fun SplineDemo() {
    val tracks = remember {
        // 정규화 공간 (0,0) → (1,1) 사이에 위로 크게 벗어나는 경유점을 하나 둔다.
        val waypoint = Offset(0.5f, -0.4f)
        listOf(
            PathTrack(
                label = "keyframes — 직선 구간, 경유점에서 꺾임",
                color = Color(0xFF7B1FA2),
                samples = sampleSpec(
                    keyframes {
                        durationMillis = 1000
                        Offset(0f, 0f) at 0
                        waypoint at 500
                        Offset(1f, 1f) at 1000
                    }
                )
            ),
            PathTrack(
                label = "keyframesWithSpline — 꺾임 없음",
                color = Color(0xFF00897B),
                samples = sampleSpec(
                    keyframesWithSpline {
                        durationMillis = 1000
                        Offset(0f, 0f) at 0
                        waypoint at 500
                        Offset(1f, 1f) at 1000
                    }
                )
            )
        )
    }
    PathComparisonCard(title = "Spline Demo", tracks = tracks)
}

/**
 * 구간별 ArcMode 데모.
 * 이 함수에 @OptIn 이 없다는 사실 자체가 "keyframes 안의 using ArcMode 는 안정 API"라는 증거다.
 */
@Composable
private fun SegmentArcDemo() {
    val tracks = remember {
        listOf(
            PathTrack(
                label = "직선 (대조군)",
                color = Color(0xFF9E9E9E),
                samples = sampleSpec(tween(1000)),
                dashed = true
            ),
            PathTrack(
                label = "앞 구간 ArcAbove + 뒤 구간 ArcBelow",
                color = Color(0xFFF9A825),
                samples = sampleSpec(
                    keyframes {
                        durationMillis = 1000
                        Offset(0f, 0f) at 0 using ArcMode.ArcAbove
                        Offset(0.5f, 0.5f) at 500 using ArcMode.ArcBelow
                        Offset(1f, 1f) at 1000
                    }
                )
            )
        )
    }
    PathComparisonCard(title = "Segment Arc Demo", tracks = tracks)
}

/**
 * 경로 비교 카드 — 샘플링한 궤적을 그리고, 같은 진행률로 각 경로 위의 점을 함께 움직인다.
 * 진행률은 LinearEasing 으로 흘리므로 점의 속도 차이는 곧 각 스펙 자신의 시간 분배다.
 */
@Composable
private fun PathComparisonCard(title: String, tracks: List<PathTrack>) {
    var playing by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "path_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = { playing = !playing },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (playing) Color(0xFF757575) else Color(0xFF1976D2)
                    )
                ) {
                    Text(
                        text = if (playing) "← 되감기" else "→ 재생",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, RoundedCornerShape(6.dp))
            ) {
                drawTracks(tracks, progress)
            }

            tracks.forEach { track ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(track.color)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(track.label, fontSize = 11.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

/**
 * 모든 경로를 하나의 좌표계에 그린다.
 * 확대 배율은 가로/세로 중 작은 쪽에 맞춰 동일하게 적용한다 — 축마다 다르게 늘이면
 * 원호가 타원으로 찌그러져 "경로의 모양"이라는 비교 자체가 깨지기 때문이다.
 */
private fun DrawScope.drawTracks(tracks: List<PathTrack>, progress: Float) {
    val allPoints = tracks.flatMap { it.samples }
    if (allPoints.isEmpty()) return

    val minX = allPoints.minOf { it.x }
    val maxX = allPoints.maxOf { it.x }
    val minY = allPoints.minOf { it.y }
    val maxY = allPoints.maxOf { it.y }
    val spanX = (maxX - minX).coerceAtLeast(0.0001f)
    val spanY = (maxY - minY).coerceAtLeast(0.0001f)

    val inset = 24f
    val usableWidth = size.width - inset * 2
    val usableHeight = size.height - inset * 2
    val scale = min(usableWidth / spanX, usableHeight / spanY)
    val originX = (size.width - spanX * scale) / 2f
    val originY = (size.height - spanY * scale) / 2f

    fun map(point: Offset) = Offset(
        x = originX + (point.x - minX) * scale,
        y = originY + (point.y - minY) * scale
    )

    // 시작·목표 지점 표시
    val startPoint = map(tracks.first().samples.first())
    val endPoint = map(tracks.first().samples.last())
    drawCircle(Color(0xFFBDBDBD), radius = 5f, center = startPoint)
    drawCircle(Color(0xFF616161), radius = 5f, center = endPoint)

    tracks.forEach { track ->
        val path = Path()
        track.samples.forEachIndexed { index, point ->
            val mapped = map(point)
            if (index == 0) path.moveTo(mapped.x, mapped.y) else path.lineTo(mapped.x, mapped.y)
        }
        drawPath(
            path = path,
            color = track.color,
            style = Stroke(
                width = 3f,
                pathEffect = if (track.dashed) {
                    PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                } else {
                    null
                }
            )
        )

        // 진행률에 해당하는 지점의 공(=실제 재생 위치)
        val index = (progress * PathSampleCount).toInt().coerceIn(0, PathSampleCount)
        drawCircle(track.color, radius = 9f, center = map(track.samples[index]))
    }
}

/**
 * DeferredTargetAnimation 데모.
 * approachLayout 은 lookahead 가 계산한 "최종 크기"를 알려주고, 실제로 배치할 크기는
 * 이 애니메이션이 정한다. 목표와 현재값이 갈라져 있는 상태를 숫자로 함께 보여준다.
 */
@OptIn(ExperimentalAnimatableApi::class)
@Composable
private fun DeferredTargetDemo() {
    var expanded by remember { mutableStateOf(false) }
    val sizeAnimation = remember { DeferredTargetAnimation(IntSize.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    // 컴포지션에서 얻은 scope 라 MonotonicFrameClock 을 갖고 있다 (updateTarget 의 전제 조건)
    var pendingText by remember { mutableStateOf("pendingTarget=null / isIdle=true") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deferred Target Demo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (expanded) Color(0xFF757575) else Color(0xFF1976D2)
                    )
                ) {
                    Text(
                        text = if (expanded) "축소" else "확대",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                LookaheadScope {
                    Box(
                        modifier = Modifier
                            .size(if (expanded) 120.dp else 48.dp)
                            .approachLayout(
                                isMeasurementApproachInProgress = { lookaheadSize ->
                                    sizeAnimation.updateTarget(
                                        target = lookaheadSize,
                                        coroutineScope = coroutineScope,
                                        animationSpec = tween(durationMillis = 700)
                                    )
                                    !sizeAnimation.isIdle
                                }
                            ) { measurable, _ ->
                                // lookahead 가 알려준 최종 크기로 목표를 갱신하고,
                                // 지금 프레임에 실제로 배치할 크기는 반환값(현재값)을 쓴다.
                                val currentSize = sizeAnimation.updateTarget(
                                    target = lookaheadSize,
                                    coroutineScope = coroutineScope,
                                    animationSpec = tween(durationMillis = 700)
                                )
                                pendingText = "lookahead=${lookaheadSize.width}px · " +
                                        "현재=${currentSize.width}px · isIdle=${sizeAnimation.isIdle}"
                                val placeable = measurable.measure(
                                    Constraints.fixed(currentSize.width, currentSize.height)
                                )
                                layout(placeable.width, placeable.height) {
                                    placeable.place(0, 0)
                                }
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE64A19))
                    )
                }
            }

            Text(
                text = pendingText,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Text(
                text = "lookahead 값은 버튼을 누른 즉시 최종 크기로 바뀌지만, 현재 크기는 " +
                        "그 뒤를 따라간다. 둘이 갈라져 있는 구간이 isIdle=false 다.",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun InfoCard(title: String, description: String, bgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}
