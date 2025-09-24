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

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎯 Concave 모서리는 기존 Compose에서 불가능했던 혁신적인 기능입니다!",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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

            // 혼합 스타일 예시들
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MixedCornerExample(
                    title = "정보 카드 스타일",
                    description = "상단: 둥근 모서리, 하단: 직각 모서리",
                    topStartShape = RoundedCornerShape(topStart = 16.dp),
                    topEndShape = RoundedCornerShape(topEnd = 16.dp),
                    color = Color(0xFF4CAF50)
                )

                MixedCornerExample(
                    title = "액션 버튼 스타일",
                    description = "좌상단: 둥근, 우하단: 잘린 모서리",
                    topStartShape = RoundedCornerShape(topStart = 12.dp),
                    bottomEndShape = CutCornerShape(bottomEnd = 12.dp),
                    color = Color(0xFFFF9800)
                )

                MixedCornerExample(
                    title = "독특한 디자인",
                    description = "대각선으로 다른 스타일 적용",
                    topStartShape = RoundedCornerShape(topStart = 20.dp),
                    bottomEndShape = RoundedCornerShape(bottomEnd = 20.dp),
                    topEndShape = CutCornerShape(topEnd = 8.dp),
                    bottomStartShape = CutCornerShape(bottomStart = 8.dp),
                    color = Color(0xFFE91E63)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 실제 구현에서는 cornerShape() 함수로 각 모서리를 개별 제어할 수 있습니다!",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun MixedCornerExample(
    title: String,
    description: String,
    color: Color,
    topStartShape: androidx.compose.ui.graphics.Shape = RectangleShape,
    topEndShape: androidx.compose.ui.graphics.Shape = RectangleShape,
    bottomStartShape: androidx.compose.ui.graphics.Shape = RectangleShape,
    bottomEndShape: androidx.compose.ui.graphics.Shape = RectangleShape
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 시뮬레이션된 혼합 모서리 (실제로는 더 복잡한 구현 필요)
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            // 기본 사각형
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color, RectangleShape)
            )

            // 각 모서리별 오버레이 (시뮬레이션)
            if (topStartShape is RoundedCornerShape) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset((-20).dp, (-20).dp)
                        .background(color, RoundedCornerShape(bottomEnd = 20.dp))
                )
            }

            if (topEndShape is RoundedCornerShape) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(20.dp, (-20).dp)
                        .background(color, RoundedCornerShape(bottomStart = 20.dp))
                )
            }

            if (bottomStartShape is RoundedCornerShape) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset((-20).dp, 20.dp)
                        .background(color, RoundedCornerShape(topEnd = 20.dp))
                )
            }

            if (bottomEndShape is RoundedCornerShape) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(20.dp, 20.dp)
                        .background(color, RoundedCornerShape(topStart = 20.dp))
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

@Composable
private fun InteractiveCornerCard() {
    var selectedCorner by remember { mutableStateOf("topStart") }
    var selectedType by remember { mutableStateOf("Rounded") }
    var cornerSize by remember { mutableStateOf(16f) }

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
                text = "실시간으로 모서리 스타일을 변경해보세요:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 모서리 선택
            Text(
                text = "모서리 선택:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("topStart", "topEnd", "bottomStart", "bottomEnd").forEach { corner ->
                    Button(
                        onClick = { selectedCorner = corner },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedCorner == corner)
                                Color(0xFFE65100) else Color(0xFFE65100).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (corner) {
                                "topStart" -> "좌상"
                                "topEnd" -> "우상"
                                "bottomStart" -> "좌하"
                                "bottomEnd" -> "우하"
                                else -> corner
                            },
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 타입 선택
            Text(
                text = "Corner 타입:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Rounded", "Cut", "Sharp").forEach { type ->
                    Button(
                        onClick = { selectedType = type },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedType == type)
                                Color(0xFFE65100) else Color(0xFFE65100).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = type,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 크기 조절
            if (selectedType != "Sharp") {
                Text(
                    text = "Corner 크기: ${cornerSize.toInt()}dp",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE65100)
                )

                Slider(
                    value = cornerSize,
                    onValueChange = { cornerSize = it },
                    valueRange = 4f..32f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFE65100))
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 프리뷰 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // 현재 설정에 따른 Shape 생성 (시뮬레이션)
                val currentShape = when (selectedType) {
                    "Rounded" -> RoundedCornerShape(cornerSize.dp)
                    "Cut" -> CutCornerShape(cornerSize.dp)
                    else -> RectangleShape
                }

                Card(
                    modifier = Modifier.size(100.dp),
                    shape = currentShape,
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
                                text = "DEMO",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedType,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE65100).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎯 실제 구현에서는 각 모서리를 개별적으로 제어하여 더 복잡한 조합이 가능합니다!",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFFE65100),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
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

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 각 사례는 사용자 경험과 브랜드 아이덴티티를 고려한 의도적인 디자인 선택입니다.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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