package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 카탈로그 화면에서 5가지 모션 패턴이 같은 [duration] / [easing] 값을 공유하도록
 * 끌어올린 상태. 슬라이더로 값을 바꾸면 전체 섹션이 즉시 새 스펙으로 반응한다.
 */
private data class ShowcaseSpec(
    val durationMs: Int,
    val easing: Easing,
    val easingLabel: String
)

@Composable
fun AnimationsShowcaseExampleUI(onBackEvent: () -> Unit) {
    // 카탈로그 공통 입력: 모든 섹션이 이 값을 그대로 받아 같은 톤으로 재생.
    var durationMs by remember { mutableStateOf(600f) }
    var easingIndex by remember { mutableStateOf(0) }
    val (easing, easingLabel) = remember(easingIndex) { easingOptions[easingIndex] }
    val spec = ShowcaseSpec(durationMs.toInt(), easing, easingLabel)

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
                text = "Compose Animations Showcase",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ShowcaseInfoCard(
                    title = "공통 슬라이더로 5가지 패턴 동시 비교",
                    description = "한 화면에서 같은 duration/easing 을 사용해 다양한 Compose 모션 API를 나란히 재생합니다.\n" +
                            "• Section A: animateXxxAsState (Dp / Color / scale·alpha)\n" +
                            "• Section B: AnimatedVisibility + Crossfade\n" +
                            "• Section C: AnimatedContent(SizeTransform) + updateTransition\n" +
                            "• Section D: rememberInfiniteTransition + Drag spring 복귀\n\n" +
                            "기존 SpringTweenSnap 예제가 'AnimationSpec 자체'에 초점을 맞췄다면, 이 예제는 'API 간 어떤 모션이 어울리는가'를 본다.",
                    bgColor = Color(0xFFE8F5E9)
                )
            }

            item { ShowcaseControls(durationMs, { durationMs = it }, easingIndex, { easingIndex = it }) }

            item { HorizontalDivider() }
            item { ShowcaseSectionHeader("A. animateXxxAsState (Dp / Color / scale·alpha)") }
            item { SectionAAnimateAsState(spec) }
            item {
                ShowcaseInfoCard(
                    title = "animateXxxAsState",
                    description = "타깃 값이 바뀔 때마다 자동으로 보간되는 가장 흔한 패턴.\n" +
                            "• Dp/Float/Color/Offset 별 전용 헬퍼가 존재\n" +
                            "• AnimationSpec(tween/spring/keyframes) 으로 톤 결정\n" +
                            "• label 인자는 Layout Inspector / Recomposition 추적용",
                    bgColor = Color(0xFFFFF8E1)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ShowcaseControls(
    durationMs: Float,
    onDurationChange: (Float) -> Unit,
    easingIndex: Int,
    onEasingChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("공통 duration: ${durationMs.toInt()}ms", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Slider(
                value = durationMs,
                onValueChange = onDurationChange,
                valueRange = 100f..2000f,
                steps = 18
            )
            Text("easing 선택", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                easingOptions.forEachIndexed { idx, (_, label) ->
                    val selected = idx == easingIndex
                    Button(
                        onClick = { onEasingChange(idx) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Color(0xFF1976D2) else Color(0xFFBBDEFB)
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Color.White else Color.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionAAnimateAsState(spec: ShowcaseSpec) {
    var toggled by remember { mutableStateOf(false) }
    val animationSpec = tween<Dp>(durationMillis = spec.durationMs, easing = spec.easing)
    val floatSpec = tween<Float>(durationMillis = spec.durationMs, easing = spec.easing)
    val colorSpec = tween<Color>(durationMillis = spec.durationMs, easing = spec.easing)

    val offset by animateDpAsState(
        targetValue = if (toggled) 120.dp else 0.dp,
        animationSpec = animationSpec,
        label = "A_offset"
    )
    val tint by animateColorAsState(
        targetValue = if (toggled) Color(0xFFE91E63) else Color(0xFF1976D2),
        animationSpec = colorSpec,
        label = "A_color"
    )
    val scale by animateFloatAsState(
        targetValue = if (toggled) 1.6f else 1.0f,
        animationSpec = floatSpec,
        label = "A_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (toggled) 0.4f else 1.0f,
        animationSpec = floatSpec,
        label = "A_alpha"
    )

    ShowcaseDemoCard(
        title = "Section A — animate*AsState",
        toggled = toggled,
        onToggle = { toggled = !toggled }
    ) {
        AnimatedBall("Dp offset", offset, Color(0xFF4CAF50))
        ColorBox("Color tint", tint)
        ScaleAlphaBox("scale × alpha", scale = scale, alpha = alpha, color = Color(0xFF9C27B0))
    }
}

@Composable
private fun ShowcaseDemoCard(
    title: String,
    toggled: Boolean,
    onToggle: () -> Unit,
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
                    Text(
                        text = if (toggled) "← 되돌리기" else "→ 재생",
                        color = Color.White,
                        fontSize = 12.sp
                    )
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
        Box(modifier = Modifier
            .weight(1f)
            .height(24.dp)) {
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
private fun ColorBox(label: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.weight(0.7f))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

@Composable
private fun ScaleAlphaBox(label: String, scale: Float, alpha: Float, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.weight(0.7f))
        Box(modifier = Modifier
            .weight(1f)
            .height(36.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun ShowcaseSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun ShowcaseInfoCard(title: String, description: String, bgColor: Color) {
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

/**
 * 카탈로그 전반에서 사용하는 easing 옵션. 슬라이더 토글로 즉시 교체된다.
 */
private val easingOptions: List<Pair<Easing, String>> = listOf(
    FastOutSlowInEasing to "FastOutSlowIn",
    LinearOutSlowInEasing to "LinearOutSlowIn",
    FastOutLinearInEasing to "FastOutLinearIn",
    LinearEasing to "Linear"
)
