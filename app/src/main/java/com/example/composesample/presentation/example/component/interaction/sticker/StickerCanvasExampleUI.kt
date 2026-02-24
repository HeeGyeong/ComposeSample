package com.example.composesample.presentation.example.component.interaction.sticker

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * Sticker Canvas Example UI
 *
 * 스티커 캔버스 앱에서 사용되는 제스처 시스템, 스프링 물리, 필오프 애니메이션,
 * 다이컷 렌더링, Z-ordering을 구현하는 예제입니다.
 */

data class StickerItem(
    val id: Int,
    val emoji: String,
    val label: String,
    val bgColor: Color,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var rotation: Float = 0f,
    var pinchScale: Float = 1f,
    var zIndex: Float = 0f,
    var isDragging: Boolean = false,
    var doubleTapZoomed: Boolean = false,
)

private val availableStickers = listOf(
    "🔥" to Color(0xFFFF6B35),
    "⭐" to Color(0xFFFFD700),
    "💜" to Color(0xFF9C27B0),
    "🎯" to Color(0xFFE53935),
    "🚀" to Color(0xFF1976D2),
    "🎨" to Color(0xFF00BCD4),
    "🌈" to Color(0xFF4CAF50),
    "💎" to Color(0xFF3F51B5),
    "🎵" to Color(0xFFFF9800),
    "🦋" to Color(0xFF7B1FA2),
    "🍀" to Color(0xFF2E7D32),
    "🌸" to Color(0xFFE91E63),
)

@Composable
fun StickerCanvasExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedExample by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabItem("캔버스", selectedExample == 0, { selectedExample = 0 }, Modifier.weight(1f))
            TabItem("제스처", selectedExample == 1, { selectedExample = 1 }, Modifier.weight(1f))
            TabItem("물리", selectedExample == 2, { selectedExample = 2 }, Modifier.weight(1f))
            TabItem("Die-Cut", selectedExample == 3, { selectedExample = 3 }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedExample) {
            0 -> FullCanvasDemo()
            1 -> GestureDemo()
            2 -> SpringPhysicsDemo()
            3 -> DieCutDemo()
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "Sticker Canvas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Gestures, Physics & Die-Cut Rendering",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1976D2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

@Composable
private fun FullCanvasDemo() {
    val stickers = remember { mutableStateListOf<StickerItem>() }
    var nextId by remember { mutableIntStateOf(0) }
    var zIndexCounter by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "스티커 캔버스",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "아래에서 스티커를 추가하고, 드래그·핀치·회전·탭·더블 탭으로 조작하세요.",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            StickerTray(
                onStickerSelected = { emoji, color ->
                    zIndexCounter++
                    val cx = if (canvasSize.width > 0) (canvasSize.width / 2f) else 200f
                    val cy = if (canvasSize.height > 0) (canvasSize.height / 2f) else 300f
                    val randomOffX = (-60..60).random().toFloat()
                    val randomOffY = (-60..60).random().toFloat()
                    val randomRot = (-15..15).random().toFloat()
                    stickers.add(
                        StickerItem(
                            id = nextId++,
                            emoji = emoji,
                            label = emoji,
                            bgColor = color,
                            offsetX = cx + randomOffX,
                            offsetY = cy + randomOffY,
                            rotation = randomRot,
                            zIndex = zIndexCounter,
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFECEFF1))
                    .border(2.dp, Color(0xFFB0BEC5), RoundedCornerShape(16.dp))
                    .onSizeChanged { canvasSize = it }
            ) {
                if (stickers.isEmpty()) {
                    Text(
                        text = "스티커를 추가해보세요!",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp,
                        color = Color(0xFF90A4AE)
                    )
                }

                stickers.forEachIndexed { index, sticker ->
                    DraggableSticker(
                        sticker = sticker,
                        onOffsetChange = { dx, dy ->
                            stickers[index] = sticker.copy(
                                offsetX = (sticker.offsetX + dx).coerceIn(
                                    0f,
                                    canvasSize.width.toFloat()
                                ),
                                offsetY = (sticker.offsetY + dy).coerceIn(
                                    0f,
                                    canvasSize.height.toFloat()
                                ),
                            )
                        },
                        onScaleRotateChange = { scaleDelta, rotDelta ->
                            stickers[index] = sticker.copy(
                                pinchScale = (sticker.pinchScale * scaleDelta).coerceIn(0.5f, 3f),
                                rotation = sticker.rotation + rotDelta,
                            )
                        },
                        onTap = {
                            zIndexCounter++
                            stickers[index] = sticker.copy(zIndex = zIndexCounter)
                        },
                        onDoubleTap = {
                            stickers[index] = sticker.copy(
                                doubleTapZoomed = !sticker.doubleTapZoomed
                            )
                        },
                        onDragStart = {
                            stickers[index] = sticker.copy(isDragging = true)
                        },
                        onDragEnd = {
                            stickers[index] = sticker.copy(isDragging = false)
                        },
                    )
                }
            }
        }

        if (stickers.isNotEmpty()) {
            FloatingActionButton(
                onClick = { stickers.clear() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear all")
            }
        }
    }
}

@Composable
private fun StickerTray(onStickerSelected: (String, Color) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "스티커 트레이",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF616161)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableStickers) { (emoji, color) ->
                    Card(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onStickerSelected(emoji, color) },
                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableSticker(
    sticker: StickerItem,
    onOffsetChange: (Float, Float) -> Unit,
    onScaleRotateChange: (Float, Float) -> Unit,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (sticker.isDragging) 16.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevation"
    )
    val animatedScale by animateFloatAsState(
        targetValue = when {
            sticker.isDragging -> 1.1f
            sticker.doubleTapZoomed -> 2f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "peelScale"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (sticker.isDragging) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "peelAlpha"
    )

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (sticker.offsetX - 40).roundToInt(),
                    (sticker.offsetY - 40).roundToInt()
                )
            }
            .zIndex(sticker.zIndex)
            .graphicsLayer {
                scaleX = sticker.pinchScale * animatedScale
                scaleY = sticker.pinchScale * animatedScale
                rotationZ = sticker.rotation
                alpha = animatedAlpha
            }
            .shadow(animatedElevation, RoundedCornerShape(14.dp))
            .pointerInput(sticker.id) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { onDoubleTap() },
                )
            }
            .pointerInput(sticker.id) {
                detectTransformGestures(
                    onGesture = { _, pan, zoom, rotation ->
                        if (pan.x != 0f || pan.y != 0f) {
                            onDragStart()
                        }
                        onOffsetChange(pan.x, pan.y)
                        onScaleRotateChange(zoom, rotation)
                    }
                )
            }
            .pointerInput(sticker.id) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.all { !it.pressed }) {
                            onDragEnd()
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.White, RoundedCornerShape(14.dp))
                .padding(4.dp)
                .background(sticker.bgColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = sticker.emoji,
                fontSize = 36.sp,
            )
        }
    }
}

@Composable
private fun GestureDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "제스처 시스템",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "각 제스처의 개별 동작을 확인합니다. detectTransformGestures로 " +
                                "드래그·핀치·회전을 동시에 처리하고, detectTapGestures로 탭·더블 탭을 처리합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item { DragOnlyDemo() }
        item { PinchZoomDemo() }
        item { RotateDemo() }
        item { TapDoubleTapDemo() }
    }
}

@Composable
private fun DragOnlyDemo() {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    GestureDemoCard(
        title = "1️⃣ 드래그 (한 손가락)",
        description = "detectTransformGestures의 pan 파라미터로 위치를 이동합니다."
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFECEFF1)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(64.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(4.dp)
                    .background(Color(0xFFFF6B35).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, _, _ ->
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("🔥", fontSize = 28.sp)
            }
        }
    }
}

@Composable
private fun PinchZoomDemo() {
    var scale by remember { mutableFloatStateOf(1f) }

    GestureDemoCard(
        title = "2️⃣ 핀치 줌 (두 손가락)",
        description = "zoom 파라미터로 0.5x~3.0x 범위 내 크기를 조절합니다."
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFECEFF1))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(4.dp)
                    .background(Color(0xFFFFD700).copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⭐", fontSize = 28.sp)
            }

            Text(
                text = "Scale: ${String.format("%.2f", scale)}x",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }
    }
}

@Composable
private fun RotateDemo() {
    var rotation by remember { mutableFloatStateOf(0f) }

    GestureDemoCard(
        title = "3️⃣ 회전 (두 손가락)",
        description = "rotation 파라미터로 graphicsLayer의 rotationZ를 제어합니다."
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFECEFF1))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, _, rot ->
                        rotation += rot
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer { rotationZ = rotation }
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(4.dp)
                    .background(Color(0xFF9C27B0).copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("💜", fontSize = 28.sp)
            }

            Text(
                text = "Rotation: ${rotation.toInt()}°",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }
    }
}

@Composable
private fun TapDoubleTapDemo() {
    var tapCount by remember { mutableIntStateOf(0) }
    var doubleTapZoomed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (doubleTapZoomed) 2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "doubleTapScale"
    )

    GestureDemoCard(
        title = "4️⃣ 탭 & 더블 탭",
        description = "탭: zIndex 증가로 최상위 배치. 더블 탭: 바운시 스프링으로 2x 줌 토글."
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFECEFF1)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(4.dp)
                    .background(Color(0xFF1976D2).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { tapCount++ },
                            onDoubleTap = { doubleTapZoomed = !doubleTapZoomed },
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", fontSize = 28.sp)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tap count: $tapCount  |  Zoomed: $doubleTapZoomed",
                    fontSize = 12.sp,
                    color = Color(0xFF616161)
                )
            }
        }
    }
}

@Composable
private fun GestureDemoCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SpringPhysicsDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Spring 물리 & 필오프 애니메이션",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "스티커를 잡으면 들어올리는 듯한 필오프 효과가 적용됩니다.\n" +
                                "그림자 크기 증가 + 스케일 증가 + 알파 감소로 구현합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item { PeelOffDemo() }
        item { SpringComparisonDemo() }
    }
}

@Composable
private fun PeelOffDemo() {
    var isDragging by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val animatedElevation by animateDpAsState(
        targetValue = if (isDragging) 20.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "peelElev"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isDragging) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "peelScale"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "peelAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "필오프 효과 (Peel-Off)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "스티커를 드래그하면 들어올리는 효과가 적용됩니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFECEFF1)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            alpha = animatedAlpha
                        }
                        .shadow(animatedElevation, RoundedCornerShape(14.dp))
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(5.dp)
                        .background(
                            Color(0xFFFF6B35).copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp)
                        )
                        .size(80.dp)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, _, _ ->
                                isDragging = true
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.all { !it.pressed }) {
                                        isDragging = false
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 40.sp)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isDragging) "잡는 중 (Peel-Off)" else "놓음 (Resting)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDragging) Color(0xFFE53935) else Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Shadow: ${animatedElevation}  Scale: ${
                            String.format(
                                "%.2f",
                                animatedScale
                            )
                        }  Alpha: ${String.format("%.2f", animatedAlpha)}",
                        fontSize = 10.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpringComparisonDemo() {
    var trigger by remember { mutableStateOf(false) }

    val noBounce by animateFloatAsState(
        targetValue = if (trigger) 1.5f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "noBounce"
    )
    val lowBounce by animateFloatAsState(
        targetValue = if (trigger) 1.5f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "lowBounce"
    )
    val medBounce by animateFloatAsState(
        targetValue = if (trigger) 1.5f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "medBounce"
    )
    val highBounce by animateFloatAsState(
        targetValue = if (trigger) 1.5f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "highBounce"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spring DampingRatio 비교",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Button(
                    onClick = { trigger = !trigger },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("토글", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpringBall("No\nBounce", noBounce, Color(0xFF90A4AE))
                SpringBall("Low\nBouncy", lowBounce, Color(0xFF4CAF50))
                SpringBall("Medium\nBouncy", medBounce, Color(0xFFFF9800))
                SpringBall("High\nBouncy", highBounce, Color(0xFFE53935))
            }
        }
    }
}

@Composable
private fun SpringBall(label: String, scale: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(4.dp, CircleShape)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%.1f", scale),
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

@Composable
private fun DieCutDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Die-Cut 렌더링 & Z-Ordering",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "실제 비닐 스티커처럼 흰색 테두리(die-cut border)와 그림자를 적용합니다.\n" +
                                "Z-ordering으로 스티커 간 겹침 순서를 제어합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item { DieCutStylesCard() }
        item { ZOrderingCard() }
    }
}

@Composable
private fun DieCutStylesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Die-Cut 스타일 비교",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Color(0xFFFFEB3B).copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("테두리 없음", fontSize = 10.sp, color = Color(0xFF757575))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(4.dp)
                        .background(
                            Color(0xFFFFEB3B).copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Die-Cut", fontSize = 10.sp, color = Color(0xFF757575))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(12.dp, RoundedCornerShape(14.dp))
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(5.dp)
                        .background(
                            Color(0xFFFFEB3B).copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Die-Cut\n+ 강한 그림자",
                    fontSize = 10.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "구현 방법",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242)
        )
        Spacer(modifier = Modifier.height(4.dp))

        val steps = listOf(
            "1. shadow()로 드롭 섀도우 적용",
            "2. 흰색 배경 + RoundedCornerShape로 Die-Cut 테두리",
            "3. 내부 padding으로 흰색 테두리 두께 조절",
            "4. 콘텐츠 배경으로 스티커 색상 적용",
        )
        steps.forEach { step ->
            Text(
                text = step,
                fontSize = 12.sp,
                color = Color(0xFF616161),
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun ZOrderingCard() {
    val stickers = remember {
        mutableStateListOf(
            Triple("🔥", Color(0xFFFF6B35), 0f),
            Triple("⭐", Color(0xFFFFD700), 1f),
            Triple("💜", Color(0xFF9C27B0), 2f),
            Triple("🚀", Color(0xFF1976D2), 3f),
        )
    }
    var nextZ by remember { mutableFloatStateOf(4f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Z-Ordering (탭하여 최상위로)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "스티커를 탭하면 zIndex가 증가하여 최상위에 배치됩니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFECEFF1)),
                contentAlignment = Alignment.Center
            ) {
                stickers.forEachIndexed { index, (emoji, color, z) ->
                    val xOff = (index - 1.5f) * 40f
                    val yOff = (index - 1.5f) * 15f

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(xOff.roundToInt(), yOff.roundToInt()) }
                            .zIndex(z)
                            .size(72.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp))
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .padding(4.dp)
                            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .clickable {
                                stickers[index] = Triple(emoji, color, nextZ)
                                nextZ++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 24.sp)
                            Text(
                                "z: ${z.toInt()}",
                                fontSize = 9.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StickerCanvasExampleUIPreview() {
    StickerCanvasExampleUI(onBackEvent = {})
}
