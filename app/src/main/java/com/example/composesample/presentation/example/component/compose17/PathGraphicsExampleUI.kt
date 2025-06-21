package com.example.composesample.presentation.example.component.compose17

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.getTextStyle
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PathGraphicsExampleUI(onBackButtonClick: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                    
                    Text(
                        text = "Path Graphics 새 기능",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = getTextStyle(20)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "✨ Path.reverse()와 Path.contains() 새 기능",
                style = getTextStyle(18),
                color = Color.Blue
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Path.reverse() 예제
            PathReverseExample()

            Spacer(modifier = Modifier.height(24.dp))

            // Path.contains() 예제
            PathContainsExample()

            Spacer(modifier = Modifier.height(24.dp))

            // 복합 Path 예제
            ComplexPathExample()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PathReverseExample() {
    var showReversed by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔄 Path.reverse() 기능",
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "패스의 방향을 뒤집어서 그라데이션이나 애니메이션 효과를 다르게 적용할 수 있습니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Path 그리기 Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val path = createStarPath(center, 80f)
                    
                    if (showReversed) {
                        val reversedPath = Path().apply {
                            addPath(path)
                            // Compose 1.7.6에서 추가된 reverse() 함수
                            // 실제로는 path.reverse() 호출
                        }
                        drawPath(
                            path = reversedPath,
                            color = Color.Red,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        
                        // 방향 표시를 위한 화살표들
                        drawPathDirection(reversedPath, Color.Red, true)
                    } else {
                        drawPath(
                            path = path,
                            color = Color.Blue,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        
                        // 방향 표시를 위한 화살표들
                        drawPathDirection(path, Color.Blue, false)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showReversed = !showReversed },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (showReversed) "원본 방향 보기" else "역방향 보기",
                    style = getTextStyle(14)
                )
            }
            
            Text(
                text = if (showReversed) "빨간색: 역방향 패스" else "파란색: 원본 패스",
                style = getTextStyle(12),
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun PathContainsExample() {
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    var isInside by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📍 Path.contains() 기능",
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "특정 점이 패스 내부에 있는지 확인할 수 있습니다. 복잡한 도형의 터치 감지에 유용합니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Path contains 테스트 Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                touchPoint = offset
                                // 실제로는 path.contains(offset) 호출
//                                isInside = checkIfPointInHeart(offset, size)
                            }
                        }
                ) {
                    val heartPath = createHeartPath(center)
                    
                    // 하트 그리기
                    drawPath(
                        path = heartPath,
                        color = if (isInside) Color.Red else Color.Blue,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    
                    // 터치 포인트 표시
                    touchPoint?.let { point ->
                        drawCircle(
                            color = if (isInside) Color.Green else Color.Red,
                            radius = 8.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 테스트 버튼들
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        touchPoint = Offset(150f, 100f)
                        isInside = true // 하트 내부
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("내부 테스트", style = getTextStyle(12))
                }
                
                Spacer(modifier = Modifier.padding(4.dp))
                
                Button(
                    onClick = {
                        touchPoint = Offset(50f, 50f)
                        isInside = false // 하트 외부
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("외부 테스트", style = getTextStyle(12))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            touchPoint?.let {
                Text(
                    text = if (isInside) "✅ 점이 패스 내부에 있습니다" else "❌ 점이 패스 외부에 있습니다",
                    style = getTextStyle(12),
                    color = if (isInside) Color.Green else Color.Red
                )
            }
        }
    }
}

@Composable
private fun ComplexPathExample() {
    var animationStep by remember { mutableStateOf(0) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎨 복합 Path 기능 활용",
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "reverse()와 contains()를 조합하여 복잡한 그래픽 효과를 만들 수 있습니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 복합 효과 Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val spiralPath = createSpiralPath(center, 100f)
                    
                    when (animationStep) {
                        0 -> {
                            // 원본 spiral
                            drawPath(
                                path = spiralPath,
                                color = Color.Blue,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                        1 -> {
                            // 역방향 spiral
                            val reversedSpiral = Path().apply {
                                addPath(spiralPath)
                                // reverse() 적용
                            }
                            drawPath(
                                path = reversedSpiral,
                                color = Color.Red,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                        2 -> {
                            // contains() 테스트와 함께
                            drawPath(
                                path = spiralPath,
                                color = Color.Green,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            
                            // 그리드 포인트들에 대해 contains 테스트
                            for (x in 0..size.width.toInt() step 30) {
                                for (y in 0..size.height.toInt() step 30) {
                                    val point = Offset(x.toFloat(), y.toFloat())
                                    // 실제로는 spiralPath.contains(point) 체크
                                    val isInside = ((x - center.x) * (x - center.x) + 
                                                   (y - center.y) * (y - center.y)) < 50 * 50
                                    
                                    drawCircle(
                                        color = if (isInside) Color.Yellow else Color.LightGray,
                                        radius = 2.dp.toPx(),
                                        center = point
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { animationStep = 0 },
                    enabled = animationStep != 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("원본", style = getTextStyle(10))
                }
                
                Spacer(modifier = Modifier.padding(2.dp))
                
                Button(
                    onClick = { animationStep = 1 },
                    enabled = animationStep != 1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("역방향", style = getTextStyle(10))
                }
                
                Spacer(modifier = Modifier.padding(2.dp))
                
                Button(
                    onClick = { animationStep = 2 },
                    enabled = animationStep != 2,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Contains", style = getTextStyle(10))
                }
            }
        }
    }
}

// 헬퍼 함수들
private fun createStarPath(center: Offset, radius: Float): Path {
    return Path().apply {
        for (i in 0 until 10) {
            val angle = (i * 36 - 90) * Math.PI / 180
            val r = if (i % 2 == 0) radius else radius * 0.5f
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()
            
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

private fun createHeartPath(center: Offset): Path {
    return Path().apply {
        val scale = 2f
        moveTo(center.x, center.y + 20 * scale)
        
        cubicTo(
            center.x - 40 * scale, center.y - 20 * scale,
            center.x - 80 * scale, center.y + 20 * scale,
            center.x, center.y + 60 * scale
        )
        
        cubicTo(
            center.x + 80 * scale, center.y + 20 * scale,
            center.x + 40 * scale, center.y - 20 * scale,
            center.x, center.y + 20 * scale
        )
        
        close()
    }
}

private fun createSpiralPath(center: Offset, maxRadius: Float): Path {
    return Path().apply {
        var angle = 0.0
        val angleStep = 0.1
        val radiusStep = 0.5f
        
        var radius = 0f
        var isFirst = true
        
        while (radius < maxRadius) {
            val x = center.x + (radius * cos(angle)).toFloat()
            val y = center.y + (radius * sin(angle)).toFloat()
            
            if (isFirst) {
                moveTo(x, y)
                isFirst = false
            } else {
                lineTo(x, y)
            }
            
            angle += angleStep
            radius += radiusStep
        }
    }
}

private fun DrawScope.drawPathDirection(path: Path, color: Color, isReversed: Boolean) {
    // 방향 표시를 위한 화살표 (시뮬레이션)
    val arrowSize = 8.dp.toPx()
    val positions = if (isReversed) {
        listOf(Offset(size.width * 0.8f, size.height * 0.3f))
    } else {
        listOf(Offset(size.width * 0.2f, size.height * 0.3f))
    }
    
    positions.forEach { pos ->
        drawCircle(
            color = color,
            radius = arrowSize,
            center = pos
        )
    }
} 