package com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay

// Static CompositionLocal (전체 리컴포지션)
val LocalStaticCounter = staticCompositionLocalOf { 0 }

// Dynamic CompositionLocal (부분 리컴포지션)
val LocalDynamicCounter = compositionLocalOf { 0 }

@Composable
fun StaticDynamicCompositionLocalExampleUI(
    onBackEvent: () -> Unit
) {
    var staticCounter by remember { mutableStateOf(0) }
    var dynamicCounter by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Static vs Dynamic CompositionLocal",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { ControlPanel(staticCounter, dynamicCounter, { staticCounter = it }, { dynamicCounter = it }) }
            item {
                CompositionLocalProvider(LocalStaticCounter provides staticCounter) {
                    StaticCompositionLocalDemo()
                }
            }
            item {
                CompositionLocalProvider(LocalDynamicCounter provides dynamicCounter) {
                    DynamicCompositionLocalDemo()
                }
            }
            item { ComparisonCard() }
            item { DecisionGuideCard() }
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
                text = "🎯 Static vs Dynamic CompositionLocal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CompositionLocal의 두 가지 타입은 '변경 빈도'가 아닌 '리컴포지션 전파 방식'에서 차이가 있습니다.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureChip("Static: 전체", Color(0xFFD32F2F))
                FeatureChip("Dynamic: 부분", Color(0xFF4CAF50))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = "💡 아래 버튼으로 값을 변경하며 리컴포지션 차이를 확인하세요",
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
private fun ControlPanel(
    staticCounter: Int,
    dynamicCounter: Int,
    onStaticChange: (Int) -> Unit,
    onDynamicChange: (Int) -> Unit
) {
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
                text = "🎮 제어 패널",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Static Counter Control
            Text(
                text = "Static Counter: $staticCounter",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onStaticChange(staticCounter + 1) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Static +1", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { onStaticChange(0) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Counter Control
            Text(
                text = "Dynamic Counter: $dynamicCounter",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDynamicChange(dynamicCounter + 1) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dynamic +1", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { onDynamicChange(0) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF81C784)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE65100).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "⚠️ Static 증가 시: 아래 모든 컴포넌트 리컴포지션\n✅ Dynamic 증가 시: 값을 읽는 컴포넌트만 리컴포지션",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun StaticCompositionLocalDemo() {
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
                text = "🔴 staticCompositionLocalOf",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "값 변경 시 Provider 이하 전체 리컴포지션",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StaticReaderComponent(label = "Reader A", modifier = Modifier.weight(1f))
                StaticNonReaderComponent(label = "Non-Reader B", modifier = Modifier.weight(1f))
                StaticReaderComponent(label = "Reader C", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "⚠️ 리컴포지션 동작:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 값을 읽는 A, C뿐만 아니라\n• 값을 읽지 않는 B도 리컴포지션\n• 전체 하위 트리 무효화",
                        fontSize = 11.sp,
                        color = Color(0xFFD32F2F),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StaticReaderComponent(label: String, modifier: Modifier = Modifier) {
    val counter = LocalStaticCounter.current
    var recomposeCount by remember { mutableStateOf(0) }

    LaunchedEffect(counter) {
        recomposeCount++
    }

    RecompositionIndicator(
        label = label,
        value = "📖 $counter",
        recomposeCount = recomposeCount,
        color = Color(0xFFD32F2F),
        reads = true,
        modifier = modifier
    )
}

@Composable
private fun StaticNonReaderComponent(label: String, modifier: Modifier = Modifier) {
    var recomposeCount by remember { mutableStateOf(0) }

    // LocalStaticCounter를 읽지 않음
    LaunchedEffect(Unit) {
        recomposeCount++
    }

    // 하지만 Static이므로 값 변경 시 리컴포지션됨
    val currentComposition = currentRecomposeScope
    DisposableEffect(currentComposition) {
        recomposeCount++
        onDispose { }
    }

    RecompositionIndicator(
        label = label,
        value = "🚫 -",
        recomposeCount = recomposeCount,
        color = Color(0xFF9E9E9E),
        reads = false,
        modifier = modifier
    )
}

@Composable
private fun DynamicCompositionLocalDemo() {
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
                text = "🟢 compositionLocalOf",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "값 변경 시 실제로 읽는 컴포저블만 리컴포지션",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DynamicReaderComponent(label = "Reader A", modifier = Modifier.weight(1f))
                DynamicNonReaderComponent(label = "Non-Reader B", modifier = Modifier.weight(1f))
                DynamicReaderComponent(label = "Reader C", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "✅ 리컴포지션 동작:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 값을 읽는 A, C만 리컴포지션\n• 값을 읽지 않는 B는 리컴포지션 안 함\n• 부분 무효화로 성능 최적화",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicReaderComponent(label: String, modifier: Modifier = Modifier) {
    val counter = LocalDynamicCounter.current
    var recomposeCount by remember { mutableStateOf(0) }

    LaunchedEffect(counter) {
        recomposeCount++
    }

    RecompositionIndicator(
        label = label,
        value = "📖 $counter",
        recomposeCount = recomposeCount,
        color = Color(0xFF4CAF50),
        reads = true,
        modifier = modifier
    )
}

@Composable
private fun DynamicNonReaderComponent(label: String, modifier: Modifier = Modifier) {
    var recomposeCount by remember { mutableStateOf(0) }

    // LocalDynamicCounter를 읽지 않음
    LaunchedEffect(Unit) {
        recomposeCount++
    }

    RecompositionIndicator(
        label = label,
        value = "🚫 -",
        recomposeCount = recomposeCount,
        color = Color(0xFF9E9E9E),
        reads = false,
        modifier = modifier
    )
}

@Composable
private fun RecompositionIndicator(
    label: String,
    value: String,
    recomposeCount: Int,
    color: Color,
    reads: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (recomposeCount > 0) 1.1f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, color, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (reads) color else Color(0xFF9E9E9E),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "리컴포지션",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${recomposeCount}회",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ComparisonCard() {
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
                text = "⚖️ 특징 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComparisonItem(
                title = "리컴포지션 범위",
                staticText = "전체 하위 트리",
                dynamicText = "읽는 컴포저블만"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonItem(
                title = "초기 성능",
                staticText = "빠름 (추적 없음)",
                dynamicText = "약간 느림 (추적)"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonItem(
                title = "변경 시 성능",
                staticText = "느림 (전체 무효화)",
                dynamicText = "빠름 (부분 무효화)"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonItem(
                title = "메모리 사용",
                staticText = "적음",
                dynamicText = "약간 많음"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 핵심: '변경 빈도'가 아닌 '리컴포지션 패턴'이 선택 기준입니다",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ComparisonItem(title: String, staticText: String, dynamicText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.width(100.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1976D2)
        )

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFD32F2F).copy(alpha = 0.1f)
        ) {
            Text(
                text = staticText,
                modifier = Modifier.padding(8.dp),
                fontSize = 11.sp,
                color = Color(0xFFD32F2F),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ) {
            Text(
                text = dynamicText,
                modifier = Modifier.padding(8.dp),
                fontSize = 11.sp,
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DecisionGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF1F8E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 선택 가이드",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            DecisionGuideItem(
                icon = "🔴",
                title = "Static을 선택하세요",
                points = listOf(
                    "거의 변경되지 않음 (앱당 1-2회)",
                    "대부분의 하위가 값을 사용",
                    "전체 UI 새로고침이 자연스러움",
                    "예: Theme, Context, Configuration"
                ),
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            DecisionGuideItem(
                icon = "🟢",
                title = "Dynamic을 선택하세요",
                points = listOf(
                    "자주 변경됨 (사용자 인터랙션)",
                    "일부만 값을 사용",
                    "부분 업데이트가 성능에 중요",
                    "예: User state, UI state, Alpha"
                ),
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Tip",
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "확신이 없다면 Dynamic(compositionLocalOf) 사용을 권장합니다",
                        fontSize = 12.sp,
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DecisionGuideItem(icon: String, title: String, points: List<String>, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(2.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        points.forEach { point ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .padding(top = 6.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = point,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
