package com.example.composesample.presentation.example.component.ui.graphics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

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
            item { GlowEffectCard() }
            item { NeumorphismCard() }
            item { KeyboardButtonCard() }
            item { BlendModeCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✨ New Shadow API for Compose 1.9.0",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "최신 Compose 1.9.0 버전으로 업데이트되었습니다! 새로운 그림자 API와 향상된 성능을 경험해보세요.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureChip("dropShadow", Color(0xFF4CAF50))
                FeatureChip("innerShadow", Color(0xFF2196F3))
                FeatureChip("세밀한 제어", Color(0xFFFF9800))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = "💡 업데이트 완료! Kotlin 2.1.0 + Compose 1.9.0 환경에서 최신 API를 활용한 그림자 효과",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String, color: Color) {
    Surface(
        modifier = Modifier,
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
private fun BasicShadowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E8),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 기본 Shadow API 사용법",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "dropShadow와 innerShadow의 기본 사용법을 확인해보세요:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // dropShadow 예제
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "dropShadow",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF388E3C)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(8.dp)
                            // 향상된 shadow API (Compose 1.9.0)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(12.dp),
                                clip = false,
                                ambientColor = Color(0xFF4CAF50).copy(alpha = 0.3f),
                                spotColor = Color(0xFF2E7D32).copy(alpha = 0.5f)
                            )
                            .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Heart",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // innerShadow 예제
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "innerShadow",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF388E3C)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(8.dp)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                            // 향상된 innerShadow 효과
                            .drawBehind {
                                val shadowColor = Color.Black.copy(alpha = 0.4f)
                                val insetSize = 6.dp.toPx()
                                val cornerRadius = 12.dp.toPx()
                                
                                // Inner shadow 효과
                                inset(insetSize, insetSize, insetSize, insetSize) {
                                    drawRoundRect(
                                        color = shadowColor,
                                        size = size,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                                    )
                                }
                                
                                // 하이라이트 효과
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.2f),
                                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                    size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Heart",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "✨ 업데이트 완료! Compose 1.9.0의 향상된 shadow API 사용:\n• ambientColor와 spotColor 지원\n• 더 사실적인 그림자 효과",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C)
                )
            }
        }
    }
}

@Composable
private fun ShadowPropertiesCard() {
    var selectedProperty by remember { mutableStateOf("radius") }
    var radiusValue by remember { mutableStateOf(20f) }
    var spreadValue by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var alphaValue by remember { mutableStateOf(0.5f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎛️ Shadow Properties 체험",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "각 속성을 조절하여 그림자 효과를 확인해보세요:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 속성 선택 탭
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("radius", "spread", "offset", "alpha").forEach { property ->
                    Button(
                        onClick = { selectedProperty = property },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedProperty == property)
                                Color(0xFF1976D2) else Color(0xFF1976D2).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = property,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 그림자 미리보기
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        // 실제 그림자 효과 적용
                        .shadow(
                            elevation = (radiusValue / 4).dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = alphaValue * 0.6f),
                            spotColor = Color.Black.copy(alpha = alphaValue * 0.8f)
                        )
                        .background(
                            Color(0xFF2196F3),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DEMO",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 슬라이더 컨트롤
            when (selectedProperty) {
                "radius" -> {
                    Text("Blur Radius: ${radiusValue.toInt()}px", fontSize = 12.sp)
                    Slider(
                        value = radiusValue,
                        onValueChange = { radiusValue = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                }
                "spread" -> {
                    Text("Spread: ${spreadValue.toInt()}px", fontSize = 12.sp)
                    Slider(
                        value = spreadValue,
                        onValueChange = { spreadValue = it },
                        valueRange = 0f..50f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                }
                "offset" -> {
                    Text("Offset X: ${offsetX.toInt()}px", fontSize = 12.sp)
                    Slider(
                        value = offsetX,
                        onValueChange = { offsetX = it },
                        valueRange = -50f..50f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                    Text("Offset Y: ${offsetY.toInt()}px", fontSize = 12.sp)
                    Slider(
                        value = offsetY,
                        onValueChange = { offsetY = it },
                        valueRange = -50f..50f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                }
                "alpha" -> {
                    Text("Alpha: ${(alphaValue * 100).toInt()}%", fontSize = 12.sp)
                    Slider(
                        value = alphaValue,
                        onValueChange = { alphaValue = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
            Text(
                text = "💡 Compose 1.9.0의 향상된 shadow API로 더 세밀하고 사실적인 그림자 효과를 구현할 수 있습니다!",
                modifier = Modifier.padding(8.dp),
                fontSize = 11.sp,
                color = Color(0xFF1976D2),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            }
        }
    }
}

@Composable
private fun InteractiveShadowCard() {
    var isPressed by remember { mutableStateOf(false) }
    
    val shadowRadius by animateFloatAsState(
        targetValue = if (isPressed) 10f else 30f,
        animationSpec = spring()
    )
    
    val shadowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.3f else 0.6f,
        animationSpec = spring()
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎮 인터랙티브 그림자",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "터치 상호작용으로 애니메이션되는 그림자 효과:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                }
                            )
                        }
                        // 실제 애니메이션 그림자 효과
                        .shadow(
                            elevation = (shadowRadius / 3).dp,
                            shape = RoundedCornerShape(20.dp),
                            clip = false,
                            ambientColor = Color(0xFFFF9800).copy(alpha = shadowAlpha * 0.6f),
                            spotColor = Color(0xFFE65100).copy(alpha = shadowAlpha * 0.8f)
                        )
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF9800),
                                    Color(0xFFE65100)
                                )
                            ),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "TOUCH",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Shadow Radius: ${shadowRadius.toInt()}px",
                    fontSize = 12.sp,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "Alpha: ${(shadowAlpha * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE65100).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎯 animateFloatAsState를 사용하여 그림자 속성을 부드럽게 애니메이션할 수 있습니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun GlowEffectCard() {
    var glowIntensity by remember { mutableStateOf(50f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✨ 글로우 효과",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "밝은 색상을 사용하여 글로우 효과를 만들어보세요:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 청록색 글로우
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        // 실제 글로우 효과
                        .shadow(
                            elevation = (glowIntensity / 5).dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = Color(0xFF00E5FF).copy(alpha = glowIntensity / 100f),
                            spotColor = Color(0xFF00E5FF).copy(alpha = glowIntensity / 80f)
                        )
                        .background(Color(0xFF00E5FF), CircleShape)
                )

                // 핑크색 글로우
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        // 실제 글로우 효과
                        .shadow(
                            elevation = (glowIntensity / 5).dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = Color(0xFFE91E63).copy(alpha = glowIntensity / 100f),
                            spotColor = Color(0xFFE91E63).copy(alpha = glowIntensity / 80f)
                        )
                        .background(Color(0xFFE91E63), CircleShape)
                )

                // 초록색 글로우
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        // 실제 글로우 효과
                        .shadow(
                            elevation = (glowIntensity / 5).dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = Color(0xFF4CAF50).copy(alpha = glowIntensity / 100f),
                            spotColor = Color(0xFF4CAF50).copy(alpha = glowIntensity / 80f)
                        )
                        .background(Color(0xFF4CAF50), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "글로우 강도: ${glowIntensity.toInt()}%",
                fontSize = 12.sp,
                color = Color.White
            )
            
            Slider(
                value = glowIntensity,
                onValueChange = { glowIntensity = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00E5FF),
                    activeTrackColor = Color(0xFF00E5FF)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF00E5FF).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 밝은 색상과 큰 radius를 사용하여 네온사인 같은 글로우 효과를 만들 수 있습니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF00E5FF),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun NeumorphismCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎨 뉴모피즘 디자인",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "dropShadow와 innerShadow를 조합하여 뉴모피즘 효과:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 볼록한 버튼
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        // 실제 뉴모피즘 효과 (볼록)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            clip = false,
                            ambientColor = Color.White.copy(alpha = 0.8f),
                            spotColor = Color.Gray.copy(alpha = 0.3f)
                        )
                        .background(
                            Color(0xFFE0E0E0),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 오목한 버튼
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color(0xFFE0E0E0),
                            RoundedCornerShape(20.dp)
                        )
                        // 실제 뉴모피즘 효과 (inset)
                        .drawBehind {
                            val shadowColor = Color.Gray.copy(alpha = 0.4f)
                            val highlightColor = Color.White.copy(alpha = 0.6f)
                            val insetSize = 4.dp.toPx()
                            val cornerRadius = 20.dp.toPx()
                            
                            // Inner shadow
                            inset(insetSize, insetSize, insetSize, insetSize) {
                                drawRoundRect(
                                    color = shadowColor,
                                    size = size,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                                )
                            }
                            
                            // Inner highlight
                            drawRoundRect(
                                color = highlightColor,
                                topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Down",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF757575).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎯 뉴모피즘은 밝은 그림자(위/왼쪽)와 어두운 그림자(아래/오른쪽)를 조합하여 만듭니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun KeyboardButtonCard() {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E8),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⌨️ 3D 키보드 버튼",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "여러 그림자를 조합하여 현실적인 3D 버튼 효과:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 50.dp)
                        .clickable { isPressed = !isPressed }
                        // 실제 3D 키보드 버튼 효과
                        .shadow(
                            elevation = if (isPressed) 2.dp else 6.dp,
                            shape = RoundedCornerShape(8.dp),
                            clip = false,
                            ambientColor = if (isPressed) Color.Gray.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f),
                            spotColor = if (isPressed) Color.Gray.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f)
                        )
                        .background(
                            if (isPressed) Color(0xFFE0E0E0) else Color.White,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isPressed) "눌림 상태" else "기본 상태",
                fontSize = 12.sp,
                color = Color(0xFF388E3C),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 실제로는 상단에 하이라이트(흰색), 하단에 드롭 섀도우(검정)를 추가하여 3D 효과를 만듭니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun BlendModeCard() {
    var selectedBlendMode by remember { mutableStateOf("Normal") }
    val blendModes = listOf("Normal", "Overlay", "Multiply", "Screen")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎭 블렌딩 모드",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "다양한 블렌딩 모드로 현실적인 그림자 효과:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 블렌딩 모드 선택
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                blendModes.forEach { mode ->
                    Button(
                        onClick = { selectedBlendMode = mode },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedBlendMode == mode)
                                Color(0xFFD32F2F) else Color(0xFFD32F2F).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = mode,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 블렌딩 효과 미리보기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 파란 배경
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF2196F3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // 시뮬레이션된 블렌딩된 그림자
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                when (selectedBlendMode) {
                                    "Overlay" -> Color(0xFF1565C0)
                                    "Multiply" -> Color(0xFF0D47A1)
                                    "Screen" -> Color(0xFF42A5F5)
                                    else -> Color.Black.copy(alpha = 0.3f)
                                },
                                CircleShape
                            )
                    )
                }

                // 초록 배경
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                when (selectedBlendMode) {
                                    "Overlay" -> Color(0xFF388E3C)
                                    "Multiply" -> Color(0xFF2E7D32)
                                    "Screen" -> Color(0xFF66BB6A)
                                    else -> Color.Black.copy(alpha = 0.3f)
                                },
                                CircleShape
                            )
                    )
                }

                // 빨간 배경
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFF44336), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                when (selectedBlendMode) {
                                    "Overlay" -> Color(0xFFD32F2F)
                                    "Multiply" -> Color(0xFFB71C1C)
                                    "Screen" -> Color(0xFFEF5350)
                                    else -> Color.Black.copy(alpha = 0.3f)
                                },
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "현재 블렌딩 모드: $selectedBlendMode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F)
                    )
                    
                    Text(
                        text = when (selectedBlendMode) {
                            "Overlay" -> "배경색과 혼합되어 더 깊은 그림자"
                            "Multiply" -> "배경색을 어둡게 만드는 그림자"
                            "Screen" -> "밝게 만드는 하이라이트 효과"
                            else -> "기본 블렌딩 (검정 그림자)"
                        },
                        fontSize = 10.sp,
                        color = Color(0xFFD32F2F).copy(alpha = 0.7f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}
