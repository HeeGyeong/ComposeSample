package com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

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
            item {
                ControlPanel(
                    staticCounter,
                    dynamicCounter,
                    { staticCounter = it },
                    { dynamicCounter = it })
            }
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
            item {
                NestedProviderDemo(staticCounter, dynamicCounter)
            }
            item {
                ConditionalReadingDemo(dynamicCounter)
            }
        }
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

// 중첩된 Provider에서의 동작
val LocalNestedValue = compositionLocalOf { 0 }

@Composable
private fun NestedProviderDemo(staticCounter: Int, dynamicCounter: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF9C4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔄 중첩된 Provider",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "부모 Provider 변경 시 자식 Provider 리컴포지션 동작",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Static 중첩
            CompositionLocalProvider(LocalStaticCounter provides staticCounter) {
                Column {
                    Text(
                        text = "Static 부모 (${staticCounter})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    CompositionLocalProvider(LocalNestedValue provides dynamicCounter) {
                        NestedChild(isStatic = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic 중첩
            CompositionLocalProvider(LocalDynamicCounter provides dynamicCounter) {
                Column {
                    Text(
                        text = "Dynamic 부모 (${dynamicCounter})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    CompositionLocalProvider(LocalNestedValue provides staticCounter) {
                        NestedChild(isStatic = false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF57F17).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 Static 부모는 자식까지 전부 리컴포지션\n💡 Dynamic 부모는 읽는 자식만 리컴포지션",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFF57F17),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun NestedChild(isStatic: Boolean) {
    val parentCounter = if (isStatic) LocalStaticCounter.current else LocalDynamicCounter.current
    val nestedValue = LocalNestedValue.current
    var recomposeCount by remember { mutableStateOf(0) }

    LaunchedEffect(parentCounter, nestedValue) {
        recomposeCount++
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isStatic) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "자식: 부모=$parentCounter, 중첩=$nestedValue",
                fontSize = 11.sp,
                color = if (isStatic) Color(0xFFD32F2F) else Color(0xFF4CAF50)
            )
            Text(
                text = "리컴포지션: ${recomposeCount}회",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isStatic) Color(0xFFD32F2F) else Color(0xFF4CAF50)
            )
        }
    }
}

// 조건부 읽기
@Composable
private fun ConditionalReadingDemo(dynamicCounter: Int) {
    var showValue by remember { mutableStateOf(false) }

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
                text = "🎯 조건부 읽기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CompositionLocal을 조건부로 읽을 때의 리컴포지션",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showValue = !showValue },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (showValue) Color(0xFF0277BD) else Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (showValue) "값 표시 중" else "값 숨김 중",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CompositionLocalProvider(LocalDynamicCounter provides dynamicCounter) {
                ConditionalReader(showValue = showValue)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0277BD).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 값을 읽지 않는 동안은 counter 변경에도 리컴포지션 안 됨\n💡 값을 읽기 시작하면 그때부터 리컴포지션 발생",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF0277BD),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ConditionalReader(showValue: Boolean) {
    var recomposeCount by remember { mutableStateOf(0) }
    
    // 조건부로만 값을 읽음
    val displayText = if (showValue) {
        val counter = LocalDynamicCounter.current
        LaunchedEffect(counter) {
            recomposeCount++
        }
        "📖 Counter: $counter"
    } else {
        LaunchedEffect(Unit) {
            // showValue가 false일 때 한 번만 카운트
            if (recomposeCount == 0) recomposeCount = 1
        }
        "🚫 값 읽지 않음"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (showValue) Color(0xFF0277BD).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (showValue) Color(0xFF0277BD) else Color.Gray
            )
            Column(
                horizontalAlignment = Alignment.End
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
                    color = if (showValue) Color(0xFF0277BD) else Color.Gray
                )
            }
        }
    }
}