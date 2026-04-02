package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpringTweenSnapExampleUI(onBackEvent: () -> Unit) {
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
                text = "Spring / Tween / Snap 애니메이션",
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
                    title = "AnimationSpec 비교",
                    description = "Compose 낮은 레벨 애니메이션 스펙:\n" +
                            "• spring: 물리 기반, 자연스러운 바운스 (지속 시간 없음)\n" +
                            "• tween: 시간 기반, 이징 커브로 속도 제어\n" +
                            "• snap: 즉시 완료 (애니메이션 없음)\n" +
                            "• keyframes: 구간별 중간값·이징을 직접 지정",
                    bgColor = Color(0xFFE8F5E9)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("1. spring()") }
            item { SpringDemo() }
            item {
                InfoCard(
                    title = "spring 파라미터",
                    description = "dampingRatio: 바운스 강도\n" +
                            "  HIGH_BOUNCY(0.2) > MEDIUM_BOUNCY(0.5) > LOW_BOUNCY(0.75) > NO_BOUNCY(1.0)\n" +
                            "stiffness: 이동 속도 (HIGH_BOUNCY > MEDIUM > LOW > VERY_LOW)\n" +
                            "visibilityThreshold: 목표와 이 값 차이 이하면 완료로 간주\n" +
                            "특징: 지속 시간 파라미터 없음 — 물리 법칙으로 자동 결정",
                    bgColor = Color(0xFFFFF8E1)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("2. tween()") }
            item { TweenDemo() }
            item {
                InfoCard(
                    title = "tween 파라미터",
                    description = "durationMillis: 애니메이션 지속 시간 (기본 300ms)\n" +
                            "delayMillis: 시작 전 대기 시간\n" +
                            "easing: FastOutSlowIn(기본) / LinearOutSlowIn / LinearEasing 등\n" +
                            "    CubicBezierEasing(x1, y1, x2, y2)로 커스텀 곡선 가능",
                    bgColor = Color(0xFFE3F2FD)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("3. snap()") }
            item { SnapDemo() }
            item {
                InfoCard(
                    title = "snap 파라미터",
                    description = "delayMillis: 지정 시간 대기 후 즉시 완료 (기본 0)\n" +
                            "애니메이션 없이 즉시 목표값으로 점프\n" +
                            "활용: 토글 전환 초기화, 상태 리셋, 애니메이션 비활성화 분기",
                    bgColor = Color(0xFFFCE4EC)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("4. keyframes()") }
            item { KeyframesDemo() }
            item {
                InfoCard(
                    title = "keyframes 패턴",
                    description = "durationMillis: 전체 시간\n" +
                            "value at timeMs: 특정 시점의 중간값 지정\n" +
                            "value at timeMs using easing: 이전 구간 이징 지정\n" +
                            "예: 0.dp at 0 → 240.dp at 300(오버슈팅) → 200.dp at 600",
                    bgColor = Color(0xFFEDE7F6)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SpringDemo() {
    var toggled by remember { mutableStateOf(false) }

    val offsetBouncy by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "spring_high_bouncy"
    )
    val offsetMedium by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "spring_medium_bouncy"
    )
    val offsetNoBounce by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "spring_no_bouncy"
    )

    AnimationDemoCard(
        title = "Spring Demo",
        onToggle = { toggled = !toggled },
        toggled = toggled
    ) {
        AnimatedBall("High Bouncy (0.2)", offsetBouncy, Color(0xFF4CAF50))
        AnimatedBall("Medium Bouncy (0.5)", offsetMedium, Color(0xFF2196F3))
        AnimatedBall("No Bouncy (1.0)", offsetNoBounce, Color(0xFFF44336))
    }
}

@Composable
private fun TweenDemo() {
    var toggled by remember { mutableStateOf(false) }

    val offsetFast by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "tween_300ms"
    )
    val offsetSlow by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = tween(durationMillis = 1200, easing = LinearEasing),
        label = "tween_1200ms_linear"
    )
    val offsetDelay by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = tween(durationMillis = 500, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "tween_delay"
    )

    AnimationDemoCard(
        title = "Tween Demo",
        onToggle = { toggled = !toggled },
        toggled = toggled
    ) {
        AnimatedBall("300ms FastOutSlowIn", offsetFast, Color(0xFF9C27B0))
        AnimatedBall("1200ms Linear", offsetSlow, Color(0xFFFF9800))
        AnimatedBall("500ms + 400ms 딜레이", offsetDelay, Color(0xFF607D8B))
    }
}

@Composable
private fun SnapDemo() {
    var toggled by remember { mutableStateOf(false) }

    val offsetSnap by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = snap(),
        label = "snap"
    )
    val offsetSnapDelay by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = snap(delayMillis = 600),
        label = "snap_delay"
    )
    val offsetTween by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = tween(400),
        label = "tween_compare"
    )

    AnimationDemoCard(
        title = "Snap Demo",
        onToggle = { toggled = !toggled },
        toggled = toggled
    ) {
        AnimatedBall("snap() — 즉시", offsetSnap, Color(0xFFE91E63))
        AnimatedBall("snap(600ms 딜레이)", offsetSnapDelay, Color(0xFF00BCD4))
        AnimatedBall("tween(400ms) 비교", offsetTween, Color(0xFF8BC34A))
    }
}

@Composable
private fun KeyframesDemo() {
    var toggled by remember { mutableStateOf(false) }

    // 오버슈팅 효과: 목표를 지나친 후 되돌아옴
    val offsetOvershoot by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = keyframes {
            durationMillis = 600
            0.dp at 0
            150.dp at 300  // 오버슈팅
            120.dp at 600
        },
        label = "keyframes_overshoot"
    )
    // 정지 구간: 중간에 잠시 멈춘 후 다시 이동
    val offsetPause by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = keyframes {
            durationMillis = 900
            0.dp at 0
            60.dp at 200
            60.dp at 550  // 정지 구간 (200ms~550ms)
            120.dp at 800
        },
        label = "keyframes_pause"
    )
    val offsetSpring by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "spring_compare"
    )

    AnimationDemoCard(
        title = "Keyframes Demo",
        onToggle = { toggled = !toggled },
        toggled = toggled
    ) {
        AnimatedBall("오버슈팅 후 정착", offsetOvershoot, Color(0xFF795548))
        AnimatedBall("중간 정지 구간", offsetPause, Color(0xFF009688))
        AnimatedBall("spring 비교", offsetSpring, Color(0xFF3F51B5))
    }
}

@Composable
private fun AnimationDemoCard(
    title: String,
    onToggle: () -> Unit,
    toggled: Boolean,
    content: @Composable () -> Unit
) {
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
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (toggled) Color(0xFF757575) else Color(0xFF1976D2)
                    )
                ) {
                    Text(if (toggled) "← 되돌리기" else "→ 이동", color = Color.White, fontSize = 12.sp)
                }
            }
            content()
        }
    }
}

@Composable
private fun AnimatedBall(label: String, offset: Dp, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(0.7f)
        )
        Box(modifier = Modifier.weight(1f).height(24.dp)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = offset)
                    .clip(CircleShape)
                    .background(color)
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
