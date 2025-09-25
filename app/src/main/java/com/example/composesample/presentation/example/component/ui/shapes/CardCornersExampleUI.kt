package com.example.composesample.presentation.example.component.ui.shapes

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun CardCornersExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Card Corners Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { BasicCornerTypesCard() }
            item { MixedCornersCard() }
            item { InteractiveCornerCard() }
            item { RealWorldExamplesCard() }
        }
    }
}

@Composable
private fun BasicCornerTypesCard() {
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
                text = "🔧 기본 Corner 타입들",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "4가지 기본 Corner 타입의 시각적 차이를 확인해보세요:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 첫 번째 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CornerTypeExample(
                    title = "Convex\n(Rounded)",
                    description = "둥근 모서리",
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                CornerTypeExample(
                    title = "Sharp\n(90°)",
                    description = "직각 모서리",
                    shape = RectangleShape,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 두 번째 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CornerTypeExample(
                    title = "Cut\n(Diagonal)",
                    description = "잘린 모서리",
                    shape = CutCornerShape(16.dp),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )

                // Concave는 시뮬레이션 (실제 구현 없이 시각적 표현)
                ConcaveCornerExample(
                    title = "Concave\n(Inward)",
                    description = "오목한 모서리",
                    color = Color(0xFFE91E63),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CornerTypeExample(
    title: String,
    description: String,
    shape: androidx.compose.ui.graphics.Shape,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color, shape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Star",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.Center
        )

        Text(
            text = description,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConcaveCornerExample(
    title: String,
    description: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Concave 효과 시뮬레이션 (실제로는 복잡한 Path 연산 필요)
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // 기본 배경
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(color, RectangleShape)
            )

            // 모서리에 원형으로 잘라내는 효과 시뮬레이션
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .offset((-28).dp, (-28).dp)
                    .background(Color.White, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .offset(28.dp, (-28).dp)
                    .background(Color.White, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .offset(28.dp, 28.dp)
                    .background(Color.White, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .offset((-28).dp, 28.dp)
                    .background(Color.White, CircleShape)
            )

            // 중앙 아이콘
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Star",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.Center
        )

        Text(
            text = description,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MixedCornersCard() {
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
                text = "🎭 혼합 Corner 스타일",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "하나의 카드에서 서로 다른 모서리 스타일을 조합한 예시들:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 실제 혼합 스타일 예시들
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 상단만 둥근 모서리
                MixedCornerExampleCard(
                    title = "정보 카드 스타일",
                    description = "상단: 둥근 모서리, 하단: 직각 모서리",
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    color = Color(0xFF4CAF50)
                )

                // 대각선 스타일
                MixedCornerExampleCard(
                    title = "대각선 스타일",
                    description = "좌상단과 우하단만 둥근 모서리",
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 20.dp
                    ),
                    color = Color(0xFFFF9800)
                )

                // 잘린 모서리 조합
                MixedCornerExampleCard(
                    title = "잘린 모서리 조합",
                    description = "상단: 둥근, 하단: 잘린 모서리",
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp
                    ),
                    color = Color(0xFFE91E63),
                    bottomShape = CutCornerShape(
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun MixedCornerExampleCard(
    title: String,
    description: String,
    shape: androidx.compose.ui.graphics.Shape,
    color: Color,
    bottomShape: androidx.compose.ui.graphics.Shape? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 실제 혼합 모서리 카드
        if (bottomShape != null) {
            // 상단과 하단이 다른 Shape인 경우 (Column으로 분리)
            Column(
                modifier = Modifier.size(width = 80.dp, height = 60.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    shape = shape,
                    backgroundColor = color,
                    elevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    shape = bottomShape,
                    backgroundColor = color.copy(alpha = 0.8f),
                    elevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            // 단일 Shape인 경우
            Card(
                modifier = Modifier.size(width = 80.dp, height = 60.dp),
                shape = shape,
                backgroundColor = color,
                elevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Star",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun InteractiveCornerCard() {
    // 각 모서리별 설정 상태
    var topStartSize by remember { mutableStateOf(16f) }
    var topEndSize by remember { mutableStateOf(16f) }
    var bottomStartSize by remember { mutableStateOf(16f) }
    var bottomEndSize by remember { mutableStateOf(16f) }
    
    var topStartType by remember { mutableStateOf("Rounded") }
    var topEndType by remember { mutableStateOf("Rounded") }
    var bottomStartType by remember { mutableStateOf("Rounded") }
    var bottomEndType by remember { mutableStateOf("Rounded") }

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
                text = "🎮 인터랙티브 Corner 에디터",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "각 모서리를 개별적으로 제어해보세요:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 프리뷰 카드 (상단에 배치)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // 실제 각 모서리별 설정을 반영한 Shape 생성
                val dynamicShape = RoundedCornerShape(
                    topStart = if (topStartType == "Sharp") 0.dp else topStartSize.dp,
                    topEnd = if (topEndType == "Sharp") 0.dp else topEndSize.dp,
                    bottomStart = if (bottomStartType == "Sharp") 0.dp else bottomStartSize.dp,
                    bottomEnd = if (bottomEndType == "Sharp") 0.dp else bottomEndSize.dp
                )

                Card(
                    modifier = Modifier.size(100.dp),
                    shape = dynamicShape,
                    backgroundColor = Color(0xFFE65100),
                    elevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "DEMO",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 각 모서리별 컨트롤
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CornerControl(
                    label = "좌상단 (TopStart)",
                    size = topStartSize,
                    type = topStartType,
                    onSizeChange = { topStartSize = it },
                    onTypeChange = { topStartType = it }
                )
                
                CornerControl(
                    label = "우상단 (TopEnd)",
                    size = topEndSize,
                    type = topEndType,
                    onSizeChange = { topEndSize = it },
                    onTypeChange = { topEndType = it }
                )
                
                CornerControl(
                    label = "좌하단 (BottomStart)",
                    size = bottomStartSize,
                    type = bottomStartType,
                    onSizeChange = { bottomStartSize = it },
                    onTypeChange = { bottomStartType = it }
                )
                
                CornerControl(
                    label = "우하단 (BottomEnd)",
                    size = bottomEndSize,
                    type = bottomEndType,
                    onSizeChange = { bottomEndSize = it },
                    onTypeChange = { bottomEndType = it }
                )
            }
        }
    }
}

@Composable
private fun CornerControl(
    label: String,
    size: Float,
    type: String,
    onSizeChange: (Float) -> Unit,
    onTypeChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE65100).copy(alpha = 0.05f),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE65100)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 타입 선택
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Rounded", "Sharp").forEach { typeOption ->
                    Button(
                        onClick = { onTypeChange(typeOption) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (type == typeOption)
                                Color(0xFFE65100) else Color(0xFFE65100).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = typeOption,
                            fontSize = 9.sp,
                            color = if (type == typeOption) Color.White else Color(0xFFE65100)
                        )
                    }
                }
            }
            
            // 크기 조절 (Sharp가 아닐 때만)
            if (type != "Sharp") {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${size.toInt()}dp",
                        fontSize = 10.sp,
                        color = Color(0xFFE65100),
                        modifier = Modifier.width(30.dp)
                    )
                    
                    Slider(
                        value = size,
                        onValueChange = onSizeChange,
                        valueRange = 0f..32f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE65100),
                            activeTrackColor = Color(0xFFE65100)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun RealWorldExamplesCard() {
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
                text = "🌟 실제 활용 사례",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "다양한 UI 패턴에서 Custom Corner를 활용한 예시들:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RealWorldExample(
                    title = "프로필 카드",
                    description = "사용자 정보를 담는 개성 있는 카드",
                    icon = Icons.Filled.AccountCircle,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 4.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    ),
                    color = Color(0xFF2196F3)
                )

                RealWorldExample(
                    title = "알림 패널",
                    description = "중요도에 따른 시각적 차별화",
                    icon = Icons.Filled.Notifications,
                    shape = CutCornerShape(
                        topStart = 0.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 0.dp
                    ),
                    color = Color(0xFFFF9800)
                )

                RealWorldExample(
                    title = "액션 버튼",
                    description = "동적이고 모던한 버튼 디자인",
                    icon = Icons.Filled.Settings,
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        bottomEnd = 24.dp
                    ),
                    color = Color(0xFF4CAF50)
                )

                RealWorldExample(
                    title = "정보 카드",
                    description = "콘텐츠 유형별 브랜딩",
                    icon = Icons.Filled.Info,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    ),
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
private fun RealWorldExample(
    title: String,
    description: String,
    icon: ImageVector,
    shape: androidx.compose.ui.graphics.Shape,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(60.dp),
            shape = shape,
            backgroundColor = color,
            elevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}