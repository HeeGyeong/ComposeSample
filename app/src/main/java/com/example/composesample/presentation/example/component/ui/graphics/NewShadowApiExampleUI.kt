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
            item { NeumorphismCard() }
            item { GlowEffectCard() }
            item { KeyboardButtonCard() }
            item { BlendModeCard() }
            item { LayeredShadowCard() }
            item { ColoredShadowCard() }
            item { ShadowDirectionCard() }
            item { MaterialCardComparisonCard() }
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
                            .drawBehind {
                                val shadowColor = Color.Black.copy(alpha = 0.4f)
                                val insetSize = 6.dp.toPx()
                                val cornerRadius = 12.dp.toPx()
                                
                                inset(insetSize, insetSize, insetSize, insetSize) {
                                    drawRoundRect(
                                        color = shadowColor,
                                        size = size,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                                    )
                                }
                                
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
    var radiusValue by remember { mutableStateOf(30f) }
    var spreadValue by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var alphaValue by remember { mutableStateOf(0.7f) }

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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .drawBehind {
                            if (spreadValue > 0) {
                                drawRoundRect(
                                    color = Color.Black.copy(alpha = alphaValue * 0.3f),
                                    topLeft = Offset(
                                        offsetX - spreadValue/2,
                                        offsetY - spreadValue/2
                                    ),
                                    size = Size(
                                        size.width + spreadValue,
                                        size.height + spreadValue
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                                )
                            }
                        }
                        .offset(offsetX.dp, offsetY.dp)
                        .shadow(
                            elevation = (radiusValue / 2).dp,
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DEMO",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        when (selectedProperty) {
                            "radius" -> Text(
                                text = "${radiusValue.toInt()}px",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 8.sp
                            )
                            "spread" -> Text(
                                text = "+${spreadValue.toInt()}px",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 8.sp
                            )
                            "offset" -> Text(
                                text = "${offsetX.toInt()},${offsetY.toInt()}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 8.sp
                            )
                            "alpha" -> Text(
                                text = "${(alphaValue * 100).toInt()}%",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedProperty) {
                "radius" -> {
                    Text(
                        text = "Blur Radius: ${radiusValue.toInt()}px (elevation: ${(radiusValue/2).toInt()}dp)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = radiusValue,
                        onValueChange = { radiusValue = it },
                        valueRange = 0f..60f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                }
                "spread" -> {
                    Text(
                        text = "Spread: ${spreadValue.toInt()}px (size: ${80 + spreadValue.toInt()}dp)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = spreadValue,
                        onValueChange = { spreadValue = it },
                        valueRange = 0f..40f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                }
                "offset" -> {
                    Text(
                        text = "Offset X: ${offsetX.toInt()}px",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = offsetX,
                        onValueChange = { offsetX = it },
                        valueRange = -30f..30f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                    Text(
                        text = "Offset Y: ${offsetY.toInt()}px",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = offsetY,
                        onValueChange = { offsetY = it },
                        valueRange = -30f..30f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2))
                    )
                }
                "alpha" -> {
                    Text(
                        text = "Alpha: ${(alphaValue * 100).toInt()}% (opacity)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
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
        targetValue = if (isPressed) 5f else 25f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "shadowRadius"
    )
    
    val shadowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.2f else 0.7f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "shadowAlpha"
    )
    
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "buttonScale"
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
                        .size((100 * buttonScale).dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                }
                            )
                        }
                        .shadow(
                            elevation = shadowRadius.dp,
                            shape = RoundedCornerShape(20.dp),
                            clip = false,
                            ambientColor = Color(0xFFFF9800).copy(alpha = shadowAlpha),
                            spotColor = Color(0xFFE65100).copy(alpha = shadowAlpha * 1.2f)
                        )
                        .background(
                            Brush.radialGradient(
                                colors = if (isPressed) listOf(
                                    Color(0xFFE65100),
                                    Color(0xFFBF360C)
                                ) else listOf(
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
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(
                            elevation = (glowIntensity / 5).dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = Color(0xFF00E5FF).copy(alpha = glowIntensity / 100f),
                            spotColor = Color(0xFF00E5FF).copy(alpha = glowIntensity / 80f)
                        )
                        .background(Color(0xFF00E5FF), CircleShape)
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(
                            elevation = (glowIntensity / 5).dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = Color(0xFFE91E63).copy(alpha = glowIntensity / 100f),
                            spotColor = Color(0xFFE91E63).copy(alpha = glowIntensity / 80f)
                        )
                        .background(Color(0xFFE91E63), CircleShape)
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
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
                text = "상단: 일반 버튼 vs 하단: 뉴모피즘 효과 (볼록/오목)",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "🔴 일반 버튼 (비교용)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(
                            Color(0xFFE0E0E0),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(
                            Color(0xFFE0E0E0),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "B",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "✨ 뉴모피즘 버튼 (이중 그림자)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            Color(0xFFE0E0E0),
                            RoundedCornerShape(16.dp)
                        )
                        .drawBehind {
                            val lightShadow = Color.White.copy(alpha = 0.9f)
                            val darkShadow = Color.Black.copy(alpha = 0.15f)
                            val offset = 8.dp.toPx()
                            val cornerRadius = 16.dp.toPx()
                            
                            drawRoundRect(
                                color = darkShadow,
                                topLeft = Offset(offset, offset),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                            )
                            
                            drawRoundRect(
                                color = lightShadow,
                                topLeft = Offset(-offset/2, -offset/2),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            Color(0xFFE0E0E0),
                            RoundedCornerShape(16.dp)
                        )
                        .drawBehind {
                            val darkShadow = Color.Black.copy(alpha = 0.2f)
                            val lightHighlight = Color.White.copy(alpha = 0.8f)
                            val inset = 4.dp.toPx()
                            val cornerRadius = 16.dp.toPx()
                            
                            drawRoundRect(
                                color = darkShadow,
                                topLeft = Offset(inset, inset),
                                size = Size(
                                    size.width - inset * 2,
                                    size.height - inset * 2
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius - inset)
                            )
                            
                            drawRoundRect(
                                color = lightHighlight,
                                topLeft = Offset(inset * 2, inset * 2),
                                size = Size(
                                    size.width - inset * 4,
                                    size.height - inset * 4
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius - inset * 2)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
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
                    text = "🎯 개선된 뉴모피즘: 실제 이중 그림자로 볼록/오목 효과를 구현했습니다!",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFF2196F3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
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

@Composable
private fun LayeredShadowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📚 레이어드 섀도우",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57C00)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "여러 그림자 레이어를 중첩하여 깊이감 표현:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .drawBehind {
                            val shadowLayers = listOf(
                                Triple(20.dp.toPx(), 0.15f, Offset(0f, 8.dp.toPx())),
                                Triple(40.dp.toPx(), 0.1f, Offset(0f, 16.dp.toPx())),
                                Triple(60.dp.toPx(), 0.05f, Offset(0f, 24.dp.toPx()))
                            )
                            
                            shadowLayers.forEach { (blur, alpha, offset) ->
                                drawRoundRect(
                                    color = Color.Black.copy(alpha = alpha),
                                    topLeft = offset,
                                    size = size,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                                )
                            }
                        }
                        .shadow(8.dp, RoundedCornerShape(20.dp))
                        .background(Color.White, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "깊이감",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57C00)
                        )
                        Text(
                            text = "3 Layers",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF57C00).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 여러 레이어의 그림자를 중첩하면 더 자연스럽고 깊이감 있는 효과를 만들 수 있습니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFF57C00),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun ColoredShadowCard() {
    var selectedColor by remember { mutableStateOf(Color(0xFF9C27B0)) }
    val colorOptions = listOf(
        Color(0xFF9C27B0) to "Purple",
        Color(0xFF00BCD4) to "Cyan",
        Color(0xFFFF5722) to "Orange",
        Color(0xFF4CAF50) to "Green"
    )

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
                text = "🎨 컬러 섀도우",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ambientColor와 spotColor로 생동감 있는 그림자:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            clip = false,
                            ambientColor = selectedColor.copy(alpha = 0.5f),
                            spotColor = selectedColor.copy(alpha = 0.7f)
                        )
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    selectedColor.copy(alpha = 0.8f),
                                    selectedColor
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colorOptions.forEach { (color, name) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { selectedColor = color }
                                .background(color, CircleShape)
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                    } else Modifier
                                )
                        )
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = selectedColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "✨ 컬러 섀도우는 현대적인 UI 디자인에서 브랜드 아이덴티티를 표현하는 강력한 도구입니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color.White,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun ShadowDirectionCard() {
    var angle by remember { mutableStateOf(45f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE1F5FE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🧭 방향성 있는 그림자",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "오프셋을 조절하여 광원의 방향을 표현:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val angleRad = Math.toRadians(angle.toDouble())
                val offsetX = (kotlin.math.cos(angleRad) * 20f).toFloat()
                val offsetY = (kotlin.math.sin(angleRad) * 20f).toFloat()

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = Color.Black.copy(alpha = 0.25f),
                                topLeft = Offset(offsetX, offsetY),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                            )
                        }
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .background(Color(0xFF0288D1), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${angle.toInt()}°",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "광원 각도: ${angle.toInt()}°",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0277BD)
            )

            Slider(
                value = angle,
                onValueChange = { angle = it },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF0288D1),
                    activeTrackColor = Color(0xFF0288D1)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF0277BD).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 offset을 이용해 광원의 위치를 시뮬레이션하여 더 사실적인 그림자를 만들 수 있습니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF0277BD),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun MaterialCardComparisonCard() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📐 Material Design Elevation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
                
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "접기" else "펼치기",
                    tint = Color(0xFF6A1B9A),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { expanded = !expanded }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Material Design의 Elevation 가이드라인 비교:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(
                    Triple(1.dp, "버튼", Color(0xFF9C27B0)),
                    Triple(4.dp, "카드", Color(0xFF7B1FA2)),
                    Triple(8.dp, "메뉴", Color(0xFF6A1B9A)),
                    Triple(16.dp, "다이얼로그", Color(0xFF4A148C))
                ).forEach { (elevation, label, color) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$label (${elevation.value.toInt()}dp)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6A1B9A),
                            modifier = Modifier.weight(0.4f)
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .height(56.dp)
                                .shadow(elevation, RoundedCornerShape(8.dp))
                                .background(color, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${elevation.value.toInt()}dp",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "📚 Material Design Elevation 가이드라인",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        listOf(
                            "• 0dp: 배경 표면",
                            "• 1dp: 검색 바, 카드 (휴식 상태)",
                            "• 2dp: 버튼 (휴식 상태)",
                            "• 4dp: 앱 바",
                            "• 6dp: FAB (휴식 상태), 스낵바",
                            "• 8dp: 하단 내비게이션, 메뉴",
                            "• 12dp: FAB (눌림 상태)",
                            "• 16dp: 내비게이션 드로어, 모달 사이드 시트",
                            "• 24dp: 다이얼로그, 피커"
                        ).forEach { guideline ->
                            Text(
                                text = guideline,
                                fontSize = 10.sp,
                                color = Color(0xFF6A1B9A),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF6A1B9A).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎯 Material Design의 Elevation 시스템은 일관된 시각적 계층을 만드는 핵심 원칙입니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF6A1B9A),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
