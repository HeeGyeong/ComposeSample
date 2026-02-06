package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Dial Component Example UI
 *
 * Canvas를 사용하여 다양한 Dial(원형 슬라이더) 컴포넌트를 직접 구현하는 예제입니다.
 * (원본 블로그의 ChromaDial 라이브러리 개념을 Canvas로 재현합니다.)
 */
@Composable
fun DialComponentExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { BasicDialCard() }
            item { RangeDialCard() }
            item { SnappingDialCard() }
            item { MultiTurnDialCard() }
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
                    text = "Dial Component",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Canvas로 구현하는 원형 다이얼",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

// ==================== 1. Basic Dial ====================
@Composable
private fun BasicDialCard() {
    var degree by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "1. Basic Dial",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "0°~360° 범위의 기본 다이얼. 썸을 드래그하여 값을 변경합니다.",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Dial(
                degree = degree,
                onDegreeChanged = { degree = it },
                trackColor = Color(0xFFE0E0E0),
                activeTrackColor = Color(0xFF1976D2),
                thumbColor = Color(0xFF1976D2),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${degree.roundToInt()}°",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}

// ==================== 2. Range Dial ====================

@Composable
private fun RangeDialCard() {
    var degree by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "2. Range Dial (225°~270°)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "startDegrees=225, sweepDegrees=270으로 3/4 원형 다이얼.",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Dial(
                degree = degree,
                onDegreeChanged = { degree = it },
                startDegrees = 225f,
                sweepDegrees = 270f,
                trackColor = Color(0xFFE0E0E0),
                activeTrackColor = Color(0xFF4CAF50),
                thumbColor = Color(0xFF4CAF50),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            val percentage = (degree / 270f * 100).roundToInt().coerceIn(0, 100)
            Text(
                text = "$percentage%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

// ==================== 3. Snapping Dial ====================

@Composable
private fun SnappingDialCard() {
    var degree by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "3. Snapping Dial (30° interval)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "30도마다 스냅 포인트가 있는 다이얼. 시계처럼 정확한 단위로 설정 가능합니다.",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Dial(
                degree = degree,
                onDegreeChanged = { degree = it },
                interval = 30f,
                showTickMarks = true,
                trackColor = Color(0xFFE0E0E0),
                activeTrackColor = Color(0xFFFF9800),
                thumbColor = Color(0xFFFF9800),
                tickColor = Color(0xFFBDBDBD),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            val step = (degree / 30f).roundToInt()
            Text(
                text = "Step $step (${degree.roundToInt()}°)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
        }
    }
}

// ==================== 4. Multi-Turn Dial ====================

@Composable
private fun MultiTurnDialCard() {
    var degree by remember { mutableFloatStateOf(0f) }
    var turns by remember { mutableIntStateOf(0) }
    var prevDegree by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "4. Multi-Turn Dial",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "360도를 넘어 여러 바퀴 회전 가능한 다이얼. 회전 수가 표시됩니다.",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Dial(
                degree = degree,
                onDegreeChanged = { newDegree ->
                    // 회전 수 감지 (0→360 또는 360→0 경계 통과 시)
                    val delta = newDegree - prevDegree
                    if (delta < -180f) {
                        turns = (turns + 1).coerceAtMost(3)
                    } else if (delta > 180f) {
                        turns = (turns - 1).coerceAtLeast(0)
                    }
                    prevDegree = newDegree
                    degree = newDegree
                },
                trackColor = Color(0xFFE0E0E0),
                activeTrackColor = Color(0xFF9C27B0),
                thumbColor = Color(0xFF9C27B0),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            val totalDegree = turns * 360 + degree.roundToInt()
            Text(
                text = "${turns}바퀴 + ${degree.roundToInt()}° = ${totalDegree}°",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // 바퀴 수 인디케이터
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < turns
                    Canvas(modifier = Modifier.size(16.dp)) {
                        if (isFilled) {
                            drawCircle(color = Color(0xFF9C27B0))
                        } else {
                            drawCircle(
                                color = Color(0xFF9C27B0),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "/ 4바퀴",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

// ==================== Dial Composable ====================

/**
 * Canvas 기반 Dial 컴포넌트.
 *
 * 원본 블로그의 ChromaDial 라이브러리 개념을 Canvas로 재현합니다.
 *
 * @param degree 현재 각도 값 (0~sweepDegrees)
 * @param onDegreeChanged 각도 변경 시 호출되는 콜백
 * @param startDegrees 시작 각도 (0 = 12시 방향)
 * @param sweepDegrees 커버하는 각도 범위 (기본 360)
 * @param interval 스냅 간격 (0 = 스무스)
 * @param showTickMarks 눈금 표시 여부
 * @param trackColor 비활성 트랙 색상
 * @param activeTrackColor 활성 트랙 색상
 * @param thumbColor 썸 색상
 * @param tickColor 눈금 색상
 * @param modifier Modifier
 */
@Composable
private fun Dial(
    degree: Float,
    onDegreeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    startDegrees: Float = 0f,
    sweepDegrees: Float = 360f,
    interval: Float = 0f,
    showTickMarks: Boolean = false,
    trackColor: Color = Color(0xFFE0E0E0),
    activeTrackColor: Color = Color(0xFF1976D2),
    thumbColor: Color = Color(0xFF1976D2),
    tickColor: Color = Color(0xFFBDBDBD),
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .pointerInput(startDegrees, sweepDegrees, interval) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // 터치 좌표 → 각도 계산 (12시 방향 기준)
                    val touchAngle = calculateAngle(
                        centerX = centerX,
                        centerY = centerY,
                        touchX = change.position.x,
                        touchY = change.position.y
                    )

                    // startDegrees 기준 상대 각도 계산
                    var relativeDegree = (touchAngle - startDegrees + 360f) % 360f

                    // sweepDegrees 범위 내로 제한
                    if (relativeDegree > sweepDegrees) {
                        // 범위 밖이면 가장 가까운 끝으로 스냅
                        val distToStart = relativeDegree
                        val distToEnd = 360f - relativeDegree + sweepDegrees
                        relativeDegree = if (distToStart < distToEnd) 0f else sweepDegrees
                    }

                    // interval 스냅
                    val snappedDegree = if (interval > 0f) {
                        val snapped =
                            (relativeDegree / interval).roundToInt() * interval
                        snapped.coerceIn(0f, sweepDegrees)
                    } else {
                        relativeDegree
                    }

                    onDegreeChanged(snappedDegree)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.06f
            val thumbRadius = size.minDimension * 0.055f
            val padding = thumbRadius + strokeWidth / 2f + 4f
            val dialSize = Size(
                size.width - padding * 2,
                size.height - padding * 2
            )
            val topLeft = Offset(padding, padding)

            // Canvas의 startAngle은 3시 방향이 0도이므로 보정
            // 블로그의 0도 = 12시 방향 → Canvas에서 -90도
            val canvasStartAngle = startDegrees - 90f

            // 배경 트랙
            drawArc(
                color = trackColor,
                startAngle = canvasStartAngle,
                sweepAngle = sweepDegrees,
                useCenter = false,
                topLeft = topLeft,
                size = dialSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 활성 트랙
            if (degree > 0f) {
                drawArc(
                    color = activeTrackColor,
                    startAngle = canvasStartAngle,
                    sweepAngle = degree,
                    useCenter = false,
                    topLeft = topLeft,
                    size = dialSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 눈금 표시
            if (showTickMarks && interval > 0f) {
                drawTickMarks(
                    startDegrees = startDegrees,
                    sweepDegrees = sweepDegrees,
                    interval = interval,
                    tickColor = tickColor,
                    center = center,
                    radius = dialSize.minDimension / 2f,
                    strokeWidth = strokeWidth
                )
            }

            // 썸 위치 계산
            val thumbAngleRad =
                Math.toRadians((startDegrees + degree - 90f).toDouble())
            val thumbX =
                center.x + (dialSize.width / 2f) * cos(thumbAngleRad).toFloat()
            val thumbY =
                center.y + (dialSize.height / 2f) * sin(thumbAngleRad).toFloat()

            // 썸 그림자
            drawCircle(
                color = Color.Black.copy(alpha = 0.15f),
                radius = thumbRadius + 2f,
                center = Offset(thumbX + 1f, thumbY + 2f)
            )

            // 썸
            drawCircle(
                color = thumbColor,
                radius = thumbRadius,
                center = Offset(thumbX, thumbY)
            )

            // 썸 내부 흰색 원
            drawCircle(
                color = Color.White,
                radius = thumbRadius * 0.45f,
                center = Offset(thumbX, thumbY)
            )

            // 중앙 값 표시
            drawCenterLabel(
                textMeasurer = textMeasurer,
                degree = degree,
                sweepDegrees = sweepDegrees,
                activeColor = activeTrackColor,
                center = center
            )
        }
    }
}

/**
 * 터치 좌표로부터 12시 방향 기준 각도를 계산합니다.
 */
private fun calculateAngle(
    centerX: Float,
    centerY: Float,
    touchX: Float,
    touchY: Float
): Float {
    val angle = Math.toDegrees(
        atan2(
            (touchY - centerY).toDouble(),
            (touchX - centerX).toDouble()
        )
    ).toFloat()
    // 3시 방향(0도) → 12시 방향(0도)으로 변환: +90도 보정
    return (angle + 90f + 360f) % 360f
}

/**
 * 눈금선(tick marks)을 그립니다.
 */
private fun DrawScope.drawTickMarks(
    startDegrees: Float,
    sweepDegrees: Float,
    interval: Float,
    tickColor: Color,
    center: Offset,
    radius: Float,
    strokeWidth: Float
) {
    val tickCount = (sweepDegrees / interval).roundToInt()
    val tickInnerRadius = radius - strokeWidth * 0.8f
    val tickOuterRadius = radius + strokeWidth * 0.8f

    for (i in 0..tickCount) {
        val tickDegree = startDegrees + i * interval
        val tickAngleRad = Math.toRadians((tickDegree - 90f).toDouble())

        val innerX = center.x + tickInnerRadius * cos(tickAngleRad).toFloat()
        val innerY = center.y + tickInnerRadius * sin(tickAngleRad).toFloat()
        val outerX = center.x + tickOuterRadius * cos(tickAngleRad).toFloat()
        val outerY = center.y + tickOuterRadius * sin(tickAngleRad).toFloat()

        drawLine(
            color = tickColor,
            start = Offset(innerX, innerY),
            end = Offset(outerX, outerY),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 다이얼 중앙에 현재 값을 표시합니다.
 */
private fun DrawScope.drawCenterLabel(
    textMeasurer: TextMeasurer,
    degree: Float,
    sweepDegrees: Float,
    activeColor: Color,
    center: Offset
) {
    // 퍼센트 표시
    val percentage = (degree / sweepDegrees * 100).roundToInt()
    val text = "$percentage%"
    val textStyle = TextStyle(
        fontSize = (size.minDimension * 0.08f).sp,
        fontWeight = FontWeight.Bold,
        color = activeColor,
        textAlign = TextAlign.Center
    )

    val textLayoutResult = textMeasurer.measure(text = text, style = textStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = text,
        topLeft = Offset(
            x = center.x - textLayoutResult.size.width / 2f,
            y = center.y - textLayoutResult.size.height / 2f
        ),
        style = textStyle
    )
}

@Preview(showBackground = true)
@Composable
fun DialComponentExampleUIPreview() {
    DialComponentExampleUI(onBackEvent = {})
}
