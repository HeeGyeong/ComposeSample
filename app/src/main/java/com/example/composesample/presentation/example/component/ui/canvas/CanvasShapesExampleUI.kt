package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CanvasShapesExampleUI(
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
            item { BasicShapesCard() }
            item { DrawStyleCard() }
            item { GradientBrushCard() }
            item { PathDrawingCard() }
            item { AnimatedCircleCard() }
            item { RotatingSquareCard() }
            item { ProgressIndicatorCard() }
            item { WaveAnimationCard() }
            item { TransformDemoCard() }
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White,
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

            Text(
                text = "🎨 Canvas Shapes & Animations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun BasicShapesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "기본 도형 그리기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "drawCircle, drawRect, drawRoundRect, drawLine, drawArc를 사용한 기본 도형",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            ) {
                val spacing = size.width / 6

                // Circle
                drawCircle(
                    color = Color(0xFFE91E63),
                    radius = 30.dp.toPx(),
                    center = Offset(spacing * 1, size.height / 2)
                )

                // Rectangle
                drawRect(
                    color = Color(0xFF2196F3),
                    topLeft = Offset(spacing * 2 - 30.dp.toPx(), size.height / 2 - 30.dp.toPx()),
                    size = Size(60.dp.toPx(), 60.dp.toPx())
                )

                // RoundRect
                drawRoundRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(spacing * 3 - 30.dp.toPx(), size.height / 2 - 30.dp.toPx()),
                    size = Size(60.dp.toPx(), 60.dp.toPx()),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )

                // Line
                drawLine(
                    color = Color(0xFFFF9800),
                    start = Offset(spacing * 4 - 20.dp.toPx(), size.height / 2 - 30.dp.toPx()),
                    end = Offset(spacing * 4 + 20.dp.toPx(), size.height / 2 + 30.dp.toPx()),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Arc (부채꼴)
                drawArc(
                    color = Color(0xFF9C27B0),
                    startAngle = -90f,
                    sweepAngle = 270f,
                    useCenter = true,
                    topLeft = Offset(spacing * 5 - 30.dp.toPx(), size.height / 2 - 30.dp.toPx()),
                    size = Size(60.dp.toPx(), 60.dp.toPx())
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShapeLabel("Circle", Color(0xFFE91E63))
                ShapeLabel("Rect", Color(0xFF2196F3))
                ShapeLabel("Round", Color(0xFF4CAF50))
                ShapeLabel("Line", Color(0xFFFF9800))
                ShapeLabel("Arc", Color(0xFF9C27B0))
            }
        }
    }
}

@Composable
private fun ShapeLabel(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun DrawStyleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "그리기 스타일 (Fill vs Stroke)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fill은 도형을 채우고, Stroke는 테두리만 그립니다",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        drawCircle(
                            color = Color(0xFF2196F3),
                            radius = 40.dp.toPx(),
                            style = Fill
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fill", fontSize = 12.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        drawCircle(
                            color = Color(0xFF2196F3),
                            radius = 40.dp.toPx(),
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Stroke", fontSize = 12.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        drawCircle(
                            color = Color(0xFF2196F3),
                            radius = 40.dp.toPx(),
                            style = Fill
                        )
                        drawCircle(
                            color = Color(0xFFFF9800),
                            radius = 40.dp.toPx(),
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Both", fontSize = 12.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
private fun GradientBrushCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "그라디언트 (Brush)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "linearGradient, radialGradient, sweepGradient",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
                            ),
                            radius = 35.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Linear", fontSize = 11.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFEB3B), Color(0xFFFF5722)),
                                center = center,
                                radius = 35.dp.toPx()
                            ),
                            radius = 35.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Radial", fontSize = 11.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF9C27B0),
                                    Color(0xFF2196F3),
                                    Color(0xFF4CAF50),
                                    Color(0xFFFFEB3B),
                                    Color(0xFFE91E63)
                                ),
                                center = center
                            ),
                            radius = 35.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sweep", fontSize = 11.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
private fun PathDrawingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Path로 복잡한 도형 그리기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "삼각형, 별, 하트 등 자유로운 경로 그리기",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val path = Path().apply {
                            moveTo(center.x, center.y - 30.dp.toPx())
                            lineTo(center.x + 30.dp.toPx(), center.y + 20.dp.toPx())
                            lineTo(center.x - 30.dp.toPx(), center.y + 20.dp.toPx())
                            close()
                        }
                        drawPath(path = path, color = Color(0xFF4CAF50))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Triangle", fontSize = 11.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val path = Path().apply {
                            val outerRadius = 30.dp.toPx()
                            val innerRadius = 12.dp.toPx()
                            val angleStep = 36f

                            for (i in 0..9) {
                                val angle = -90 + i * angleStep
                                val radius = if (i % 2 == 0) outerRadius else innerRadius
                                val x = center.x + radius * cos(angle * PI / 180).toFloat()
                                val y = center.y + radius * sin(angle * PI / 180).toFloat()

                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                            close()
                        }
                        drawPath(path = path, color = Color(0xFFFFEB3B))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Star", fontSize = 11.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val path = Path().apply {
                            val width = 60.dp.toPx()
                            val height = 60.dp.toPx()

                            moveTo(center.x, center.y - height / 4)
                            cubicTo(
                                center.x - width / 2, center.y - height / 2,
                                center.x - width / 2, center.y + height / 4,
                                center.x, center.y + height / 2
                            )
                            cubicTo(
                                center.x + width / 2, center.y + height / 4,
                                center.x + width / 2, center.y - height / 2,
                                center.x, center.y - height / 4
                            )
                            close()
                        }
                        drawPath(path = path, color = Color(0xFFE91E63))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Heart", fontSize = 11.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
private fun AnimatedCircleCard() {
    var isAnimating by remember { mutableStateOf(false) }

    val radiusDp by animateFloatAsState(
        targetValue = if (isAnimating) 60f else 30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "radius"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "애니메이션 - Bouncy Spring",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "클릭하면 원이 튕기듯 커지고 작아집니다",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .clickable { isAnimating = !isAnimating },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = radiusDp.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { isAnimating = !isAnimating },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isAnimating) "축소" else "확대",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RotatingSquareCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "무한 회전 애니메이션",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "rotate() 변환과 infiniteTransition 사용",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    val rectSize = 80.dp.toPx()
                    rotate(degrees = rotation, pivot = center) {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
                            ),
                            topLeft = Offset(center.x - rectSize / 2, center.y - rectSize / 2),
                            size = Size(rectSize, rectSize)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicatorCard() {
    var progress by remember { mutableFloatStateOf(0.5f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "커스텀 진행 표시기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "drawArc를 활용한 원형 Progress Indicator",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = progress,
                onValueChange = { progress = it },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF4CAF50),
                    activeTrackColor = Color(0xFF4CAF50)
                )
            )
        }
    }
}

@Composable
private fun WaveAnimationCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "파도 애니메이션",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Path와 sin 함수를 활용한 웨이브 효과",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height / 2)
                    for (x in 0..size.width.toInt() step 5) {
                        val y = size.height / 2 + sin((x + phase * 2) * PI / 180) * 30.dp.toPx()
                        lineTo(x.toFloat(), y.toFloat())
                    }
                }

                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                val path2 = Path().apply {
                    moveTo(0f, size.height / 2)
                    for (x in 0..size.width.toInt() step 5) {
                        val y =
                            size.height / 2 + sin((x + phase * 2 + 90) * PI / 180) * 30.dp.toPx()
                        lineTo(x.toFloat(), y.toFloat())
                    }
                }

                drawPath(
                    path = path2,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun TransformDemoCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "transform")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Transform 데모",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "scale, rotate, translate 동시 적용",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val radius = 25.dp.toPx()
                        scale(scale = scale, pivot = center) {
                            drawCircle(
                                color = Color(0xFFE91E63),
                                radius = radius
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scale", fontSize = 11.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val radius = 25.dp.toPx()
                        val offsetX = 10f * sin(scale * PI).toFloat()
                        val offsetY = 10f * cos(scale * PI).toFloat()
                        translate(left = offsetX, top = offsetY) {
                            drawCircle(
                                color = Color(0xFF2196F3),
                                radius = radius
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Translate", fontSize = 11.sp, color = Color(0xFF666666))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val rectSize = 40.dp.toPx()
                        scale(scale = scale, pivot = center) {
                            rotate(degrees = scale * 360f, pivot = center) {
                                drawRect(
                                    color = Color(0xFF4CAF50),
                                    topLeft = Offset(
                                        center.x - rectSize / 2,
                                        center.y - rectSize / 2
                                    ),
                                    size = Size(rectSize, rectSize)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Combined", fontSize = 11.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}
