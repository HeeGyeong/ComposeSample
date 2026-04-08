package com.example.composesample.presentation.example.component.ui.canvas

import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * Motion Blur for a Spinning Wheel - Example UI
 *
 * Canvas로 그린 스피닝 휠에 모션 블러를 적용하는 세 가지 기법을 비교합니다:
 * 1. Ghost Frames  - 유령 프레임 겹치기
 * 2. BlurMaskFilter - Paint 레벨 블러
 * 3. RenderEffect  - API 31+ 하드웨어 가속 블러
 */

private val wheelColors = listOf(
    Color(0xFFE53935),
    Color(0xFFFF9800),
    Color(0xFFFFEB3B),
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFFFF5722),
)

@Composable
fun MotionBlurExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabItem("Ghost\nFrames", selectedTab == 0, { selectedTab = 0 }, Modifier.width(80.dp))
            TabItem("Blur\nFilter", selectedTab == 1, { selectedTab = 1 }, Modifier.width(80.dp))
            TabItem("Render\nEffect", selectedTab == 2, { selectedTab = 2 }, Modifier.width(80.dp))
            TabItem("선형\n이동", selectedTab == 3, { selectedTab = 3 }, Modifier.width(80.dp))
            TabItem("비교", selectedTab == 4, { selectedTab = 4 }, Modifier.width(70.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> GhostFramesDemo()
            1 -> BlurMaskFilterDemo()
            2 -> RenderEffectDemo()
            3 -> LinearMotionBlurDemo()
            4 -> ComparisonDemo()
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
                    text = "Motion Blur – Spinning Wheel",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Ghost Frames · BlurMaskFilter · RenderEffect",
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
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

private fun DrawScope.drawWheelSectors(
    rotationOffset: Float = 0f,
    alpha: Float = 1f
) {
    val radius = min(size.width, size.height) / 2f
    val cx = size.width / 2f
    val cy = size.height / 2f
    val sweepAngle = 360f / wheelColors.size

    wheelColors.forEachIndexed { i, color ->
        val startAngle = rotationOffset + i * sweepAngle
        withTransform({}) {
            drawArc(
                color = color.copy(alpha = alpha),
                startAngle = startAngle,
                sweepAngle = sweepAngle - 1f,
                useCenter = true,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
    }

    drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = radius * 0.12f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFF212121).copy(alpha = alpha),
        radius = radius * 0.06f,
        center = Offset(cx, cy)
    )
}


@Composable
private fun GhostFramesDemo() {
    var ghostCount by remember { mutableIntStateOf(6) }
    var ghostDelta by remember { mutableFloatStateOf(4f) }
    var speed by remember { mutableIntStateOf(2000) }

    val infiniteTransition = rememberInfiniteTransition(label = "ghostSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speed, easing = LinearEasing)
        ),
        label = "ghostRotation"
    )

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
                        text = "Ghost Frames (유령 프레임)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "동일한 휠을 회전 방향으로 여러 각도에서 반투명하게 겹쳐 그립니다.\n" +
                                "ghostCount개의 프레임을 그리고, 각 프레임의 알파를 점진적으로 줄입니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "블러 없음",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Canvas(modifier = Modifier.size(130.dp)) {
                                withTransform({ rotate(rotation, pivot = center) }) {
                                    drawWheelSectors()
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Ghost Frames",
                                fontSize = 12.sp,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Canvas(modifier = Modifier.size(130.dp)) {
                                for (i in ghostCount downTo 0) {
                                    val ghostRotation = rotation - i * ghostDelta
                                    val alpha = (1f - i.toFloat() / (ghostCount + 1)) * 0.85f
                                    withTransform({
                                        rotate(ghostRotation, pivot = center)
                                    }) {
                                        drawWheelSectors(alpha = alpha)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ControlRow(
                        label = "Ghost 수: $ghostCount",
                        value = ghostCount.toFloat(),
                        range = 2f..16f,
                        color = Color(0xFF1976D2),
                        onValueChange = { ghostCount = it.toInt() }
                    )
                    ControlRow(
                        label = "Delta: ${ghostDelta.toInt()}°",
                        value = ghostDelta,
                        range = 1f..15f,
                        color = Color(0xFF1976D2),
                        onValueChange = { ghostDelta = it }
                    )
                    ControlRow(
                        label = "속도: ${speed}ms",
                        value = speed.toFloat(),
                        range = 400f..4000f,
                        color = Color(0xFF1976D2),
                        onValueChange = { speed = it.toInt() }
                    )
                }
            }
        }

    }
}

@Composable
private fun BlurMaskFilterDemo() {
    var blurRadius by remember { mutableFloatStateOf(8f) }
    var speed by remember { mutableIntStateOf(2000) }

    val infiniteTransition = rememberInfiniteTransition(label = "blurSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speed, easing = LinearEasing)
        ),
        label = "blurRotation"
    )

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
                        text = "BlurMaskFilter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Canvas의 Paint에 BlurMaskFilter를 설정하여 블러를 적용합니다.\n" +
                                "drawWithContent와 Modifier.graphicsLayer의 조합으로 구현합니다.\n" +
                                "softwareLayer = true 설정이 필수입니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "NORMAL",
                                fontSize = 11.sp,
                                color = Color(0xFF9C27B0),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            BlurWheelCanvas(
                                rotation = rotation,
                                blurRadius = blurRadius,
                                style = BlurMaskFilter.Blur.NORMAL
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "SOLID",
                                fontSize = 11.sp,
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            BlurWheelCanvas(
                                rotation = rotation,
                                blurRadius = blurRadius,
                                style = BlurMaskFilter.Blur.SOLID
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "OUTER",
                                fontSize = 11.sp,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            BlurWheelCanvas(
                                rotation = rotation,
                                blurRadius = blurRadius,
                                style = BlurMaskFilter.Blur.OUTER
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ControlRow(
                        label = "블러 반지름: ${blurRadius.toInt()}px",
                        value = blurRadius,
                        range = 1f..30f,
                        color = Color(0xFF9C27B0),
                        onValueChange = { blurRadius = it }
                    )
                    ControlRow(
                        label = "속도: ${speed}ms",
                        value = speed.toFloat(),
                        range = 400f..4000f,
                        color = Color(0xFF9C27B0),
                        onValueChange = { speed = it.toInt() }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BlurMaskFilter.Blur 종류",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val types = listOf(
                        "NORMAL" to "내부 + 외부 모두 블러 적용",
                        "SOLID" to "내부 선명, 외부만 블러 (그림자)",
                        "INNER" to "내부만 블러, 외부는 제거",
                        "OUTER" to "외부에만 블러, 내부는 투명"
                    )
                    types.forEach { (name, desc) ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF7B1FA2),
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = "→ $desc",
                                fontSize = 11.sp,
                                color = Color(0xFF616161)
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun BlurWheelCanvas(
    rotation: Float,
    blurRadius: Float,
    style: BlurMaskFilter.Blur
) {
    val blurColor = Color(0x40000000)

    Box(
        modifier = Modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(110.dp)
                .graphicsLayer {
                    rotationZ = rotation
                    this.compositingStrategy =
                        androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                }
        ) {
            val paint = Paint()
            paint.asFrameworkPaint().apply {
                isAntiAlias = true
                maskFilter = BlurMaskFilter(blurRadius * density, style)
                color = blurColor.toArgb()
            }

            val radius = min(size.width, size.height) / 2f * 0.95f
            val cx = size.width / 2f
            val cy = size.height / 2f

            drawIntoCanvas { canvas ->
                val sweepAngle = 360f / wheelColors.size
                wheelColors.forEachIndexed { i, color ->
                    paint.asFrameworkPaint().color =
                        color.copy(alpha = 0.7f).toArgb()
                    paint.asFrameworkPaint().maskFilter =
                        BlurMaskFilter(blurRadius * density, style)
                    canvas.drawArc(
                        left = cx - radius,
                        top = cy - radius,
                        right = cx + radius,
                        bottom = cy + radius,
                        startAngle = i * sweepAngle,
                        sweepAngle = sweepAngle - 1f,
                        useCenter = true,
                        paint = paint
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .size(90.dp)
                .graphicsLayer { rotationZ = rotation }
        ) {
            drawWheelSectors()
        }
    }
}

@Composable
private fun RenderEffectDemo() {
    var blurX by remember { mutableFloatStateOf(12f) }
    var blurY by remember { mutableFloatStateOf(2f) }
    var speed by remember { mutableIntStateOf(2000) }

    val infiniteTransition = rememberInfiniteTransition(label = "renderSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speed, easing = LinearEasing)
        ),
        label = "renderRotation"
    )

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "RenderEffect (API 31+)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Text(
                                text = "✅ 지원",
                                fontSize = 11.sp,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .background(
                                        Color(0xFFE8F5E9),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        } else {
                            Text(
                                text = "❌ API ${Build.VERSION.SDK_INT} < 31",
                                fontSize = 11.sp,
                                color = Color(0xFFE53935),
                                modifier = Modifier
                                    .background(
                                        Color(0xFFFFEBEE),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "graphicsLayer의 renderEffect로 GPU 가속 블러를 적용합니다.\n" +
                                "BlurEffect(radiusX, radiusY)로 방향별 블러를 설정할 수 있습니다.\n" +
                                "회전 방향 블러를 흉내내려면 radiusX >> radiusY 설정을 사용합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("블러 없음", fontSize = 11.sp, color = Color(0xFF757575))
                            Spacer(modifier = Modifier.height(6.dp))
                            Canvas(
                                modifier = Modifier
                                    .size(120.dp)
                                    .graphicsLayer { rotationZ = rotation }
                            ) {
                                drawWheelSectors()
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "RenderEffect",
                                fontSize = 11.sp,
                                color = Color(0xFF00695C),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val blurXDp = blurX
                            val blurYDp = blurY

                            Canvas(
                                modifier = Modifier
                                    .size(120.dp)
                                    .graphicsLayer {
                                        rotationZ = rotation
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            renderEffect =
                                                androidx.compose.ui.graphics.BlurEffect(
                                                    radiusX = blurXDp * density,
                                                    radiusY = blurYDp * density,
                                                    edgeTreatment = TileMode.Decal
                                                )
                                        }
                                    }
                            ) {
                                drawWheelSectors()
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Ghost (비교)",
                                fontSize = 11.sp,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Canvas(modifier = Modifier.size(120.dp)) {
                                for (i in 6 downTo 0) {
                                    val gr = rotation - i * 4f
                                    val alpha = (1f - i / 7f) * 0.85f
                                    withTransform({ rotate(gr, pivot = center) }) {
                                        drawWheelSectors(alpha = alpha)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ControlRow(
                            label = "Radius X: ${blurX.toInt()}",
                            value = blurX,
                            range = 0f..30f,
                            color = Color(0xFF00695C),
                            onValueChange = { blurX = it }
                        )
                        ControlRow(
                            label = "Radius Y: ${blurY.toInt()}",
                            value = blurY,
                            range = 0f..30f,
                            color = Color(0xFF00695C),
                            onValueChange = { blurY = it }
                        )
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "⚠️ RenderEffect는 Android 12(API 31) 이상에서 지원됩니다.\n현재 기기: API ${Build.VERSION.SDK_INT}",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = Color(0xFFB71C1C),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    ControlRow(
                        label = "속도: ${speed}ms",
                        value = speed.toFloat(),
                        range = 400f..4000f,
                        color = Color(0xFF00695C),
                        onValueChange = { speed = it.toInt() }
                    )
                }
            }
        }

    }
}

@Composable
private fun LinearMotionBlurDemo() {
    var ghostCount by remember { mutableIntStateOf(6) }
    var ghostDelta by remember { mutableFloatStateOf(0.04f) }
    var speed by remember { mutableIntStateOf(1000) }
    var blurX by remember { mutableFloatStateOf(18f) }

    val infiniteTransition = rememberInfiniteTransition(label = "linearMotion")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "linearProgress"
    )

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
                        text = "선형 이동 모션 블러",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "직선으로 왕복하는 오브젝트에 모션 블러를 적용합니다.\n" +
                                "FastOutSlowIn easing으로 가속·감속 구간에서\n" +
                                "Ghost 퍼짐 정도가 달라지는 것을 확인할 수 있습니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("블러 없음", fontSize = 12.sp, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.height * 0.35f
                            val cx = r + progress * (size.width - r * 2)
                            drawCircle(Color(0xFF9E9E9E), r, Offset(cx, size.height / 2f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "Ghost Frames",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.height * 0.35f
                            for (i in ghostCount downTo 0) {
                                val gp = (progress - i * ghostDelta).coerceIn(0f, 1f)
                                val cx = r + gp * (size.width - r * 2)
                                val alpha = (1f - i.toFloat() / (ghostCount + 1)) * 0.85f
                                drawCircle(
                                    color = Color(0xFF1976D2).copy(alpha = alpha),
                                    radius = r,
                                    center = Offset(cx, size.height / 2f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "RenderEffect" + if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) " (API 31+)" else "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00695C)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        renderEffect = androidx.compose.ui.graphics.BlurEffect(
                                            radiusX = blurX * density,
                                            radiusY = 3f * density,
                                            edgeTreatment = TileMode.Decal
                                        )
                                    }
                                }
                        ) {
                            val r = size.height * 0.35f
                            val cx = r + progress * (size.width - r * 2)
                            drawCircle(Color(0xFF00695C), r, Offset(cx, size.height / 2f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ControlRow(
                        label = "Ghost 수: $ghostCount",
                        value = ghostCount.toFloat(),
                        range = 2f..16f,
                        color = Color(0xFF1976D2),
                        onValueChange = { ghostCount = it.toInt() }
                    )
                    ControlRow(
                        label = "Ghost 간격: ${(ghostDelta * 100).toInt()}%",
                        value = ghostDelta,
                        range = 0.01f..0.12f,
                        color = Color(0xFF1976D2),
                        onValueChange = { ghostDelta = it }
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ControlRow(
                            label = "Blur X: ${blurX.toInt()}px",
                            value = blurX,
                            range = 1f..40f,
                            color = Color(0xFF00695C),
                            onValueChange = { blurX = it }
                        )
                    }
                    ControlRow(
                        label = "속도: ${speed}ms",
                        value = speed.toFloat(),
                        range = 300f..3000f,
                        color = Color(0xFF616161),
                        onValueChange = { speed = it.toInt() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonDemo() {
    var speed by remember { mutableIntStateOf(1500) }

    val infiniteTransition = rememberInfiniteTransition(label = "compSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "compRotation"
    )

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
                        text = "모션 블러 기법 비교",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "4가지 방식을 동일한 회전 속도에서 나란히 비교합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WheelVariant("없음", Color(0xFF757575)) {                            Canvas(modifier = Modifier.size(80.dp).graphicsLayer { rotationZ = rotation }) {
                                drawWheelSectors()
                            }
                        }
                        WheelVariant("Ghost\n(x6)", Color(0xFF1976D2)) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                for (i in 6 downTo 0) {
                                    withTransform({ rotate(rotation - i * 5f, pivot = center) }) {
                                        drawWheelSectors(alpha = (1f - i / 7f) * 0.85f)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WheelVariant("Blur\nFilter", Color(0xFF9C27B0)) {
                            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                                Canvas(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .graphicsLayer {
                                            rotationZ = rotation
                                            compositingStrategy =
                                                androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                                        }
                                ) {
                                    val paint = Paint()
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    val radius = min(size.width, size.height) / 2f
                                    val sweepAngle = 360f / wheelColors.size

                                    drawIntoCanvas { canvas ->
                                        wheelColors.forEachIndexed { i, color ->
                                            paint.asFrameworkPaint().apply {
                                                this.color = color.copy(alpha = 0.9f).toArgb()
                                                maskFilter = BlurMaskFilter(6f * density, BlurMaskFilter.Blur.NORMAL)
                                                isAntiAlias = true
                                            }
                                            canvas.drawArc(
                                                cx - radius, cy - radius, cx + radius, cy + radius,
                                                i * sweepAngle, sweepAngle - 1f, true, paint
                                            )
                                        }
                                    }

                                    drawCircle(Color.White, radius * 0.12f, center)
                                    drawCircle(Color(0xFF212121), radius * 0.06f, center)
                                }
                            }
                        }

                        WheelVariant(
                            label = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Render\nEffect" else "Render\n(N/A)",
                            color = Color(0xFF00695C)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .size(80.dp)
                                    .graphicsLayer {
                                        rotationZ = rotation
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            renderEffect = androidx.compose.ui.graphics.BlurEffect(
                                                10f * density, 1f * density, TileMode.Decal
                                            )
                                        }
                                    }
                            ) {
                                drawWheelSectors()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ControlRow(
                        label = "속도: ${speed}ms / 회전",
                        value = speed.toFloat(),
                        range = 400f..4000f,
                        color = Color(0xFF616161),
                        onValueChange = { speed = it.toInt() }
                    )
                }
            }
        }

        item { ComparisonTableCard() }
    }
}

@Composable
private fun WheelVariant(
    label: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun ComparisonTableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "기법별 특징 비교",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))

            val rows = listOf(
                listOf("기법", "API", "성능", "정확도"),
                listOf("없음", "ALL", "최상", "없음"),
                listOf("Ghost Frames", "ALL", "보통", "높음 ⭐"),
                listOf("BlurMaskFilter", "ALL", "낮음", "낮음"),
                listOf("RenderEffect", "31+", "높음 ⭐", "보통"),
            )

            rows.forEachIndexed { rowIdx, cols ->
                val isHeader = rowIdx == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isHeader) Color(0xFFE3F2FD)
                            else if (rowIdx % 2 == 0) Color(0xFFFAFAFA)
                            else Color.White,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(vertical = 5.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    cols.forEachIndexed { colIdx, cell ->
                        Text(
                            text = cell,
                            fontSize = 11.sp,
                            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                            color = if (isHeader) Color(0xFF1565C0) else Color(0xFF424242),
                            modifier = Modifier.weight(
                                when (colIdx) {
                                    0 -> 2f
                                    else -> 1f
                                }
                            )
                        )
                    }
                }
                if (rowIdx < rows.size - 1) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "✅ 권장:\n" +
                            "• 정확한 방향 블러 → Ghost Frames\n" +
                            "• 최신 기기 성능 우선 → RenderEffect\n" +
                            "• 간단한 흐림 효과 → BlurMaskFilter",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF4A148C),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun ControlRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF616161)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.3f)
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MotionBlurExampleUIPreview() {
    MotionBlurExampleUI(onBackEvent = {})
}
