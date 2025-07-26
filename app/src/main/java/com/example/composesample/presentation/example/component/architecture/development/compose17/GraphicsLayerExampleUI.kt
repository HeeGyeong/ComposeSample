package com.example.composesample.presentation.example.component.architecture.development.compose17

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.getTextStyle

/**
 * GraphicsLayer Example
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GraphicsLayerExampleUI(onBackButtonClick: () -> Unit) {
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
                        text = "GraphicsLayer 향상된 기능",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = getTextStyle(20)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "✨ Compose 1.7.6 GraphicsLayer 향상 예정 기능",
                style = getTextStyle(18),
                color = Color.Blue
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            BlendModeExample()

            Spacer(modifier = Modifier.height(24.dp))

            ColorFilterExample()

            Spacer(modifier = Modifier.height(24.dp))

            CombinedEffectsExample()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun BlendModeExample() {
    var selectedBlendMode by remember { mutableStateOf(BlendMode.SrcOver) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎨 BlendMode 효과 (Compose 1.8+)",
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "다양한 BlendMode를 선택하여 실제 효과를 확인해보세요!",
                style = getTextStyle(12),
                color = Color.Blue
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // BlendMode 선택 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BlendModeButton("Normal", BlendMode.SrcOver) { selectedBlendMode = it }
                BlendModeButton("Multiply", BlendMode.Multiply) { selectedBlendMode = it }
                BlendModeButton("Screen", BlendMode.Screen) { selectedBlendMode = it }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // BlendMode 적용된 박스들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 배경
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Red, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "배경",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        style = getTextStyle(12)
                    )
                }
                
                Text("+", style = getTextStyle(20))
                
                // 전경 (BlendMode 적용)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(
                            compositingStrategy = CompositingStrategy.Offscreen
                        )
                        .background(Color.Blue, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "전경",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        style = getTextStyle(12)
                    )
                }
                
                Text("=", style = getTextStyle(20))
                
                // 결과 (겹쳐진 상태)
                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red, RoundedCornerShape(8.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                compositingStrategy = CompositingStrategy.Offscreen
                            )
                            .background(Color.Blue.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    )
                    Text(
                        text = "결과",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        style = getTextStyle(12)
                    )
                }
            }
        }
    }
}

@Composable
private fun BlendModeButton(
    text: String,
    blendMode: BlendMode,
    onClick: (BlendMode) -> Unit
) {
    Button(
        onClick = { onClick(blendMode) },
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text, style = getTextStyle(12))
    }
}

@Composable
private fun ColorFilterExample() {
    var tintStrength by remember { mutableStateOf(0.5f) }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🌈 ColorFilter 효과 (시뮬레이션)",
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ColorFilter가 활성화되었습니다. 색상과 강도를 조절해보세요!",
                style = getTextStyle(12),
                color = Color.Green
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 색상 선택 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColorButton("빨강", Color.Red) { selectedColor = it }
                ColorButton("초록", Color.Green) { selectedColor = it }
                ColorButton("파랑", Color.Blue) { selectedColor = it }
                ColorButton("노랑", Color.Yellow) { selectedColor = it }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 강도 조절 슬라이더
            Text(
                text = "색상 강도: ${(tintStrength * 100).toInt()}%",
                style = getTextStyle(14)
            )
            
            Slider(
                value = tintStrength,
                onValueChange = { tintStrength = it },
                valueRange = 0f..1f
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 원본과 효과 적용된 결과 비교
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Color.Gray,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = "원본",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White,
                            style = getTextStyle(14)
                        )
                    }
                    Text("원본", style = getTextStyle(12))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    color = selectedColor,
                                    alpha = tintStrength
                                )
                            }
                            .background(
                                Color.Gray,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = "효과",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White,
                            style = getTextStyle(14)
                        )
                    }
                    Text("ColorFilter 적용", style = getTextStyle(12))
                }
            }
        }
    }
}

@Composable
private fun ColorButton(
    text: String,
    color: Color,
    onClick: (Color) -> Unit
) {
    Button(
        onClick = { onClick(color) },
        modifier = Modifier.padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.padding(2.dp))
        Text(text, style = getTextStyle(10))
    }
}

@Composable
private fun CombinedEffectsExample() {
    var isAnimating by remember { mutableStateOf(false) }
    
    val animatedRotation by animateFloatAsState(
        targetValue = if (isAnimating) 360f else 0f,
        animationSpec = tween(2000)
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isAnimating) 1.5f else 1f,
        animationSpec = tween(1000)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎭 조합 효과 (회전 + 크기 + Animation)",
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // 배경 레이어
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Magenta)
                )
                
                // 효과가 적용된 레이어
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            rotationZ = animatedRotation,
                            scaleX = animatedScale,
                            scaleY = animatedScale
                        )
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                color = Color.Cyan,
                                alpha = 0.7f
                            )
                        }
                        .background(Color.Yellow)
                ) {
                    Text(
                        text = "MAGIC!",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black,
                        style = getTextStyle(16)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = { isAnimating = !isAnimating }
            ) {
                Text(
                    text = if (isAnimating) "애니메이션 중지" else "애니메이션 시작",
                    style = getTextStyle(14)
                )
            }
        }
    }
} 