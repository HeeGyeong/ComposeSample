package com.example.composesample.presentation.example.component.ui.visibility

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun VisibilityExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Visibility in Compose",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConditionalCompositionCard() }
            item { AlphaModifierCard() }
            item { AnimatedVisibilityCard() }
            item { CustomVisibilityModifierCard() }
        }
    }
}

@Composable
private fun ConditionalCompositionCard() {
    var isVisible by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🚫 조건부 컴포지션 (GONE)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "if 조건문으로 Composable 제거",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 토글 스위치
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "isVisible: $isVisible",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )

                Switch(
                    checked = isVisible,
                    onCheckedChange = { isVisible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFD32F2F)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 데모 영역
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD32F2F).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DemoBox("Box 1", Color(0xFFE3F2FD))

                    // 조건부 컴포지션 - GONE 효과
                    if (isVisible) {
                        DemoBox("Box 2 (조건부)", Color(0xFFFFCDD2))
                    }

                    DemoBox("Box 3", Color(0xFFE8F5E9))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isVisible) "✅ Box 2가 컴포지션에 포함됨" else "❌ Box 2가 컴포지션에서 제거됨 (공간 없음)",
                fontSize = 11.sp,
                color = Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
private fun AlphaModifierCard() {
    var isVisible by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "👻 Alpha Modifier (INVISIBLE)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Modifier.alpha(0f)로 투명하게 (공간 유지)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 토글 스위치
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "alpha: ${if (isVisible) "1f" else "0f"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )

                Switch(
                    checked = isVisible,
                    onCheckedChange = { isVisible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFE65100)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 데모 영역
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE65100).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DemoBox("Box 1", Color(0xFFE3F2FD))

                    // Alpha modifier - INVISIBLE 효과
                    Box(
                        modifier = Modifier.alpha(if (isVisible) 1f else 0f)
                    ) {
                        DemoBox("Box 2 (alpha)", Color(0xFFFFE0B2))
                    }

                    DemoBox("Box 3", Color(0xFFE8F5E9))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isVisible) "✅ Box 2가 보임 (alpha: 1f)" else "👻 Box 2가 투명 (alpha: 0f) - 공간은 유지됨!",
                fontSize = 11.sp,
                color = Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun AnimatedVisibilityCard() {
    var isVisible by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✨ AnimatedVisibility",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "애니메이션과 함께 표시/숨김",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 토글 스위치
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "visible: $isVisible",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Switch(
                    checked = isVisible,
                    onCheckedChange = { isVisible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF2E7D32)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 데모 영역
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DemoBox("Box 1", Color(0xFFE3F2FD))

                    // AnimatedVisibility
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                    ) {
                        DemoBox("Box 2 (Animated)", Color(0xFFC8E6C9))
                    }

                    DemoBox("Box 3", Color(0xFFE8F5E9))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "fadeIn + expandVertically / fadeOut + shrinkVertically",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun CustomVisibilityModifierCard() {
    var selectedVisibility by remember { mutableIntStateOf(0) }
    val visibilities = listOf("Visible", "Invisible", "Gone")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎨 커스텀 Modifier.visible()",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "View 시스템과 유사한 API 제공",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visibility 선택
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                visibilities.forEachIndexed { index, label ->
                    VisibilityOptionButton(
                        label = label,
                        isSelected = selectedVisibility == index,
                        onClick = { selectedVisibility = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 데모 영역
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF7B1FA2).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DemoBox("Box 1", Color(0xFFE3F2FD))

                    // 커스텀 Visibility
                    when (Visibility.entries[selectedVisibility]) {
                        Visibility.Visible -> {
                            DemoBox("Box 2 (Visible)", Color(0xFFE1BEE7))
                        }
                        Visibility.Invisible -> {
                            Box(modifier = Modifier.alpha(0f)) {
                                DemoBox("Box 2 (Invisible)", Color(0xFFE1BEE7))
                            }
                        }
                        Visibility.Gone -> {
                            // 아무것도 렌더링하지 않음
                        }
                    }

                    DemoBox("Box 3", Color(0xFFE8F5E9))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (Visibility.entries[selectedVisibility]) {
                    Visibility.Visible -> "✅ Visible: 보임 + 공간 차지"
                    Visibility.Invisible -> "👻 Invisible: 안 보임 + 공간 차지"
                    Visibility.Gone -> "❌ Gone: 안 보임 + 공간 없음"
                },
                fontSize = 11.sp,
                color = Color(0xFF7B1FA2)
            )
        }
    }
}

@Composable
private fun DemoBox(text: String, color: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (text.contains("Invisible") || text.contains("alpha"))
                    Icons.Filled.Close
                else
                    Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF666666),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun VisibilityOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF7B1FA2) else Color(0xFFE1BEE7),
        animationSpec = tween(200),
        label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF7B1FA2),
        animationSpec = tween(200),
        label = "contentColor"
    )

    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

// 커스텀 Visibility enum
enum class Visibility {
    Visible,
    Invisible,
    Gone
}

