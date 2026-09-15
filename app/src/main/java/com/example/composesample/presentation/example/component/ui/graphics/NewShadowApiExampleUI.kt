package com.example.composesample.presentation.example.component.ui.graphics

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * New Shadow API — Modifier.dropShadow / Modifier.innerShadow (Compose 1.9+)
 *
 * 그림자 값은 전부 Shadow(radius, color 또는 brush, spread, offset, alpha, blendMode) 하나로 넘긴다.
 * 모디파이어 순서가 곧 그리는 순서다: dropShadow → background(도형 뒤), background → innerShadow(배경 위).
 * 참고 자료와 핵심 개념은 같은 폴더의 exampleGuide.kt 참고.
 */
@Composable
fun NewShadowApiExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "New Shadow API Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { BasicShadowCard() }
            item { ShadowPropertiesCard() }
            item { InteractiveShadowCard() }
            item { NeumorphismCard() }
            item { LayeredShadowCard() }
            item { BlendModeCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard() {
    SectionCard(
        title = "✨ New Shadow API — dropShadow / innerShadow",
        description = "Compose 1.9 에서 추가된 두 모디파이어는 Shadow 값 하나로 radius·spread·offset·color(또는 brush)·alpha·blendMode 를 모두 받는다. " +
            "이 프로젝트가 쓰는 Compose 1.11.4 에서는 opt-in 없이 바로 쓸 수 있다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureChip("dropShadow", Color(0xFF388E3C))
            FeatureChip("innerShadow", Color(0xFF1976D2))
            FeatureChip("Shadow(...)", Color(0xFFF57C00))
        }

        Spacer(modifier = Modifier.height(12.dp))

        CodeBox(
            code = "val shape = RoundedCornerShape(16.dp)\n" +
                "Modifier\n" +
                "    .dropShadow(shape, Shadow(radius = 12.dp, color = Color.Black,\n" +
                "        offset = DpOffset(0.dp, 6.dp), alpha = 0.3f))\n" +
                "    .background(Color.White, shape)\n" +
                "    .innerShadow(shape, Shadow(radius = 8.dp, color = Color.Black, alpha = 0.2f))"
        )

        Spacer(modifier = Modifier.height(12.dp))

        NoteBox(
            "모디파이어 순서가 곧 그리는 순서다 — dropShadow 는 background 보다 앞에 두어 도형 뒤에 깔고, " +
                "innerShadow 는 background 뒤에 두어 배경 위(도형 안쪽 가장자리)에 그린다."
        )
    }
}

// ==================== 2. 기본 비교 ====================

@Composable
private fun BasicShadowCard() {
    val shape = RoundedCornerShape(16.dp)
    val boxColor = Color(0xFF66BB6A)

    SectionCard(
        title = "🎯 기본 비교 — shadow(elevation) · dropShadow · innerShadow",
        description = "같은 크기·같은 도형에 세 가지 그림자를 적용했다."
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DemoItem(label = "shadow", caption = "elevation 12dp") {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(elevation = 12.dp, shape = shape)
                        .background(boxColor, shape)
                )
            }
            DemoItem(label = "dropShadow", caption = "radius 16 · y +6") {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .dropShadow(
                            shape,
                            Shadow(radius = 16.dp, color = Color.Black, offset = DpOffset(0.dp, 6.dp), alpha = 0.35f)
                        )
                        .background(boxColor, shape)
                )
            }
            DemoItem(label = "innerShadow", caption = "radius 12") {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(boxColor, shape)
                        .innerShadow(shape, Shadow(radius = 12.dp, color = Color.Black, alpha = 0.45f))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        NoteBox(
            "shadow(elevation) 은 시스템 광원을 기준으로 그려지는 플랫폼 그림자라 높이만 정할 수 있다. " +
                "dropShadow 는 블러한 도형을 직접 그려 퍼짐(spread)·위치(offset)·색까지 조절되고, " +
                "innerShadow 는 도형 안쪽 가장자리에 그림자를 그린다."
        )
    }
}

// ==================== 3. 속성 체험 ====================

@Composable
private fun ShadowPropertiesCard() {
    var radius by remember { mutableFloatStateOf(16f) }
    var spread by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(8f) }
    var alpha by remember { mutableFloatStateOf(0.5f) }
    val shape = RoundedCornerShape(20.dp)

    SectionCard(
        title = "🎛️ Shadow 속성 체험",
        description = "Shadow(radius, spread, offset, color, alpha) 의 값을 슬라이더로 바꿔 본다. offset 으로 광원의 방향도 표현된다."
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFFF1F3F6), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .dropShadow(
                        shape,
                        Shadow(
                            radius = radius.dp,
                            color = Color(0xFF0D47A1),
                            spread = spread.dp,
                            offset = DpOffset(offsetX.dp, offsetY.dp),
                            alpha = alpha
                        )
                    )
                    .background(Color(0xFF42A5F5), shape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "DEMO", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LabeledSlider("radius", "${radius.toInt()}dp", radius, 0f..40f) { radius = it }
        LabeledSlider("spread", "${spread.toInt()}dp", spread, 0f..24f) { spread = it }
        LabeledSlider("offset x", "${offsetX.toInt()}dp", offsetX, -24f..24f) { offsetX = it }
        LabeledSlider("offset y", "${offsetY.toInt()}dp", offsetY, -24f..24f) { offsetY = it }
        LabeledSlider("alpha", "${(alpha * 100).toInt()}%", alpha, 0f..1f) { alpha = it }

        Spacer(modifier = Modifier.height(8.dp))

        NoteBox(
            "offset 을 음수로 두면 그림자가 왼쪽·위로 옮겨가 광원이 오른쪽 아래에 있는 것처럼 보인다. " +
                "shadow(elevation) 만으로는 spread·offset 을 줄 수 없어 drawBehind 로 흉내 내야 했던 값들이다."
        )
    }
}

// ==================== 4. 인터랙티브 ====================

@Composable
private fun InteractiveShadowCard() {
    var isPressed by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    // Shadow 의 값은 일반 Dp/Float 라 animate*AsState 결과를 그대로 넣으면 된다
    val dropRadius by animateDpAsState(targetValue = if (isPressed) 4.dp else 20.dp, label = "dropRadius")
    val dropOffsetY by animateDpAsState(targetValue = if (isPressed) 1.dp else 10.dp, label = "dropOffsetY")
    val innerAlpha by animateFloatAsState(targetValue = if (isPressed) 0.4f else 0f, label = "innerAlpha")
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "scale"
    )

    SectionCard(
        title = "🎮 인터랙티브 — 떠 있다가 눌려 들어가는 버튼",
        description = "누르고 있는 동안 dropShadow 는 줄어들고 innerShadow 가 나타난다."
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 180.dp, height = 64.dp)
                    .scale(scale)
                    .dropShadow(
                        shape,
                        Shadow(radius = dropRadius, color = Color.Black, offset = DpOffset(0.dp, dropOffsetY), alpha = 0.3f)
                    )
                    .background(Color(0xFFFF7043), shape)
                    .innerShadow(shape, Shadow(radius = 10.dp, color = Color.Black, alpha = innerAlpha))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPressed) "눌림" else "누르고 있기",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "dropShadow radius ${dropRadius.value.toInt()}dp · y ${dropOffsetY.value.toInt()}dp · " +
                "innerShadow alpha ${(innerAlpha * 100).toInt()}%",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// ==================== 5. 뉴모피즘 ====================

private val NeuBackground = Color(0xFFE0E5EC)
private val NeuDark = Color(0xFFA3B1C6)

@Composable
private fun NeumorphismCard() {
    var keyPressed by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(20.dp)

    SectionCard(
        title = "🎨 뉴모피즘 — dropShadow ×2 / innerShadow ×2",
        description = "배경과 같은 색의 도형에 밝은 그림자(왼쪽 위)와 어두운 그림자(오른쪽 아래)를 겹친다. 바깥에 두면 볼록, 안쪽에 두면 오목하다.",
        containerColor = NeuBackground
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DemoItem(label = "볼록", caption = "dropShadow ×2") {
                Box(modifier = Modifier.size(72.dp).then(raisedNeumorphism(shape)))
            }
            DemoItem(label = "오목", caption = "innerShadow ×2") {
                Box(modifier = Modifier.size(72.dp).then(pressedNeumorphism(shape)))
            }
            DemoItem(label = "키보드 키", caption = "탭해서 전환") {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .then(if (keyPressed) pressedNeumorphism(shape) else raisedNeumorphism(shape))
                        .clickable { keyPressed = !keyPressed },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (keyPressed) Color(0xFF7B8794) else Color(0xFF4A5568)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        NoteBox(
            "볼록과 오목을 오가는 3D 키도 같은 두 쌍의 그림자를 바꿔 끼우는 것뿐이다. " +
                "그림자 색은 배경색을 기준으로 밝게/어둡게 잡아야 자연스럽다."
        )
    }
}

/** 볼록: 밝은 그림자(좌상) + 어두운 그림자(우하)를 도형 바깥에 */
private fun raisedNeumorphism(shape: Shape): Modifier = Modifier
    .dropShadow(shape, Shadow(radius = 12.dp, color = Color.White, offset = DpOffset((-6).dp, (-6).dp), alpha = 0.9f))
    .dropShadow(shape, Shadow(radius = 12.dp, color = NeuDark, offset = DpOffset(6.dp, 6.dp), alpha = 0.8f))
    .background(NeuBackground, shape)

/** 오목: 같은 한 쌍을 도형 안쪽에 */
private fun pressedNeumorphism(shape: Shape): Modifier = Modifier
    .background(NeuBackground, shape)
    .innerShadow(shape, Shadow(radius = 8.dp, color = NeuDark, offset = DpOffset(4.dp, 4.dp), alpha = 0.9f))
    .innerShadow(shape, Shadow(radius = 8.dp, color = Color.White, offset = DpOffset((-4).dp, (-4).dp), alpha = 0.9f))

// ==================== 6. 레이어 · 그라디언트 · 글로우 ====================

@Composable
private fun LayeredShadowCard() {
    var glow by remember { mutableFloatStateOf(0.8f) }
    val shape = RoundedCornerShape(16.dp)

    SectionCard(
        title = "📚 레이어 · 그라디언트 · 글로우",
        description = "dropShadow 는 여러 번 체이닝할 수 있고, 먼저 쓴 그림자가 아래에 깔린다. brush 를 주면 그라디언트 그림자, 밝은 색에 큰 radius 를 주면 글로우가 된다."
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DemoItem(label = "레이어 3겹", caption = "넓고 옅게 → 좁고 진하게") {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .dropShadow(shape, Shadow(radius = 24.dp, color = Color.Black, offset = DpOffset(0.dp, 12.dp), alpha = 0.10f))
                        .dropShadow(shape, Shadow(radius = 8.dp, color = Color.Black, offset = DpOffset(0.dp, 4.dp), alpha = 0.15f))
                        .dropShadow(shape, Shadow(radius = 2.dp, color = Color.Black, offset = DpOffset(0.dp, 1.dp), alpha = 0.25f))
                        .background(Color.White, shape)
                )
            }
            DemoItem(label = "그라디언트", caption = "Shadow(brush = …)") {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .dropShadow(
                            shape,
                            Shadow(
                                radius = 18.dp,
                                brush = Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFFFF4081))),
                                offset = DpOffset(0.dp, 8.dp),
                                alpha = 0.9f
                            )
                        )
                        .background(Color.White, shape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFF263238), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .dropShadow(
                        CircleShape,
                        Shadow(radius = 28.dp, color = Color(0xFF00E5FF), spread = 6.dp, alpha = glow)
                    )
                    .background(Color(0xFF00E5FF), CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LabeledSlider("글로우", "${(glow * 100).toInt()}%", glow, 0f..1f) { glow = it }

        Spacer(modifier = Modifier.height(8.dp))

        NoteBox(
            "글로우는 어두운 배경 위에서 offset 없이 큰 radius·spread 를 준 밝은 색 dropShadow 다. " +
                "별도의 블러 효과나 RenderEffect 가 필요 없다."
        )
    }
}

// ==================== 7. blendMode ====================

@Composable
private fun BlendModeCard() {
    val modes = remember {
        listOf(
            "SrcOver" to BlendMode.SrcOver,
            "Multiply" to BlendMode.Multiply,
            "Screen" to BlendMode.Screen,
            "Overlay" to BlendMode.Overlay
        )
    }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val shape = RoundedCornerShape(16.dp)

    SectionCard(
        title = "🎭 blendMode — 그림자를 아래 픽셀과 섞는 방식",
        description = "Shadow 의 blendMode 는 그림자를 이미 그려진 배경과 어떻게 합칠지 정한다. 단색 배경에서는 차이가 작고, 무늬가 있는 배경 위에서 드러난다."
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            modes.forEachIndexed { index, (name, _) ->
                Button(
                    onClick = { selectedIndex = index },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedIndex == index) Color(0xFFD81B60) else Color(0xFFD81B60).copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = name, color = Color.White, fontSize = 10.sp, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFFEB3B), Color(0xFF26C6DA), Color(0xFF7E57C2))),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .dropShadow(
                        shape,
                        Shadow(
                            radius = 16.dp,
                            color = Color(0xFFD81B60),
                            spread = 8.dp,
                            offset = DpOffset(12.dp, 12.dp),
                            alpha = 1f,
                            blendMode = modes[selectedIndex].second
                        )
                    )
                    .background(Color.White, shape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = modes[selectedIndex].first,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD81B60)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        NoteBox(
            "SrcOver 는 그대로 덮고, Multiply 는 배경과 곱해 어둡게, Screen 은 밝게 섞는다. " +
                "Overlay 는 배경의 밝기에 따라 둘 사이를 오간다."
        )
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
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF616161)
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun DemoItem(
    label: String,
    caption: String,
    demo: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            demo()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF424242)
        )
        Text(
            text = caption,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(64.dp),
            fontSize = 12.sp,
            color = Color(0xFF424242)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1976D2),
                activeTrackColor = Color(0xFF1976D2)
            )
        )
        Text(
            text = valueText,
            modifier = Modifier.width(44.dp),
            fontSize = 12.sp,
            color = Color(0xFF1976D2),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun FeatureChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
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
            modifier = Modifier.padding(12.dp),
            fontSize = 11.sp,
            color = Color(0xFFECEFF1),
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}
