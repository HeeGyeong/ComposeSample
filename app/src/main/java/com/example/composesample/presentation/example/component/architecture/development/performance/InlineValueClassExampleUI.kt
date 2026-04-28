package com.example.composesample.presentation.example.component.architecture.development.performance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

inline fun <T> measureInline(label: String, block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsed = System.currentTimeMillis() - start
    return result to elapsed
}

fun <T> measureNonInline(label: String, block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsed = System.currentTimeMillis() - start
    return result to elapsed
}

inline fun <T> List<T>.customFilterInline(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (predicate(item)) {
            result.add(item)
        }
    }
    return result
}

fun <T> List<T>.customFilterNonInline(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (predicate(item)) {
            result.add(item)
        }
    }
    return result
}

@Composable
fun InlineValueClassExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Inline Functions",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { InlineExplanationCard() }
            item { CodeComparisonCard() }
            item { PerformanceBenchmarkCard() }
        }
    }
}

@Composable
private fun InlineExplanationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Inline Functions란?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "inline 키워드를 붙이면:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    BulletPoint("함수 본문이 호출 지점에 복사됨 (인라인 전개)")
                    BulletPoint("람다 파라미터의 객체 할당이 제거됨")
                    BulletPoint("함수 호출 오버헤드가 사라짐")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color(0xFF1976D2).copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "언제 사용하나요?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UseCaseChip("고차 함수", Color(0xFF4CAF50))
                UseCaseChip("람다를 받는 함수", Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF9C4)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "큰 함수에는 사용하지 마세요. 코드 크기가 증가합니다.",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color(0xFF1976D2), CircleShape)
                .padding(top = 6.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun UseCaseChip(text: String, color: Color) {
    Surface(
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
private fun CodeComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📝 코드 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "같은 기능, 다른 구현 방식",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeComparisonSection(
                title = "Inline Function",
                code = """
inline fun <T> measureInline(
    label: String, 
    block: () -> T
): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsed = System.currentTimeMillis() - start
    return result to elapsed
}
                """.trimIndent(),
                explanation = "• 호출 시점에 함수 본문이 복사됨\n• 람다 객체 생성 없음\n• 함수 호출 스택 없음",
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(12.dp))

            CodeComparisonSection(
                title = "Non-Inline Function",
                code = """
fun <T> measureNonInline(
    label: String, 
    block: () -> T
): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsed = System.currentTimeMillis() - start
    return result to elapsed
}
                """.trimIndent(),
                explanation = "• 일반적인 함수 호출\n• 람다가 객체로 생성됨\n• 함수 호출 오버헤드 존재",
                color = Color(0xFFFF9800)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF9C27B0).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📊 실제 차이",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "호출 횟수가 많을수록 Inline의 성능 이점이 커집니다.\n아래 벤치마크에서 직접 확인해보세요!",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeComparisonSection(
    title: String,
    code: String,
    explanation: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (title.contains("Inline")) "최적화" else "일반",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF263238)
            ) {
                Text(
                    text = code,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF80CBC4),
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = explanation,
                fontSize = 11.sp,
                color = Color(0xFF666666),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PerformanceBenchmarkCard() {
    var iterations by remember { mutableStateOf(10000) }
    var isRunning by remember { mutableStateOf(false) }
    var inlineTime by remember { mutableStateOf(0L) }
    var nonInlineTime by remember { mutableStateOf(0L) }
    var inlineFilterTime by remember { mutableStateOf(0L) }
    var nonInlineFilterTime by remember { mutableStateOf(0L) }

    val scale by animateFloatAsState(
        targetValue = if (isRunning) 1.02f else 1f,
        animationSpec = spring()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "성능 벤치마크",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F51B5)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "실제 성능 차이를 측정해보세요",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "반복 횟수 선택",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1000 to "1K", 10000 to "10K", 100000 to "100K", 1000000 to "1M").forEach { (count, label) ->
                    Button(
                        onClick = { iterations = count },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (iterations == count)
                                Color(0xFF3F51B5) else Color(0xFFC5CAE9)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isRunning
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (iterations == count) Color.White else Color(0xFF3F51B5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isRunning = true

                    val (_, inline1) = measureInline("test") {
                        repeat(iterations) { it * 2 }
                    }
                    inlineTime = inline1

                    val (_, nonInline1) = measureNonInline("test") {
                        repeat(iterations) { it * 2 }
                    }
                    nonInlineTime = nonInline1

                    val testList = (1..1000).toList()
                    val (_, inline2) = measureInline("filter") {
                        repeat(iterations / 100) {
                            testList.customFilterInline { it % 2 == 0 }
                        }
                    }
                    inlineFilterTime = inline2

                    val (_, nonInline2) = measureNonInline("filter") {
                        repeat(iterations / 100) {
                            testList.customFilterNonInline { it % 2 == 0 }
                        }
                    }
                    nonInlineFilterTime = nonInline2

                    isRunning = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isRunning
            ) {
                if (isRunning) {
                    Text("측정 중...", color = Color.White, fontSize = 14.sp)
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "벤치마크 시작",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = inlineTime > 0 || nonInlineTime > 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "테스트 1: 간단한 연산 반복",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BenchmarkResult(
                        label = "Inline",
                        time = inlineTime,
                        color = Color(0xFF4CAF50),
                        isFaster = inlineTime < nonInlineTime && nonInlineTime > 0
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BenchmarkResult(
                        label = "Non-Inline",
                        time = nonInlineTime,
                        color = Color(0xFFFF5722),
                        isFaster = nonInlineTime < inlineTime && inlineTime > 0
                    )

                    if (inlineTime > 0 && nonInlineTime > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PerformanceComparison(inlineTime, nonInlineTime, "간단한 연산")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF3F51B5).copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "테스트 2: 람다를 받는 고차 함수",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BenchmarkResult(
                        label = "Inline (람다 최적화)",
                        time = inlineFilterTime,
                        color = Color(0xFF4CAF50),
                        isFaster = inlineFilterTime < nonInlineFilterTime && nonInlineFilterTime > 0
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BenchmarkResult(
                        label = "Non-Inline (람다 객체 생성)",
                        time = nonInlineFilterTime,
                        color = Color(0xFFFF5722),
                        isFaster = nonInlineFilterTime < inlineFilterTime && inlineFilterTime > 0
                    )

                    if (inlineFilterTime > 0 && nonInlineFilterTime > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PerformanceComparison(inlineFilterTime, nonInlineFilterTime, "람다 함수")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF3F51B5).copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📊 분석 결과",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F51B5)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• 람다를 받는 함수에서 Inline의 성능 이점이 더 큽니다\n• 호출 횟수가 많을수록 차이가 명확해집니다\n• 실제 성능은 JVM 최적화에 따라 달라질 수 있습니다",
                                fontSize = 11.sp,
                                color = Color(0xFF666666),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkResult(
    label: String,
    time: Long,
    color: Color,
    isFaster: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = if (isFaster)
            androidx.compose.foundation.BorderStroke(2.dp, color)
        else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isFaster) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isFaster) FontWeight.Bold else FontWeight.Medium,
                    color = color
                )
            }
            Text(
                text = "${time}ms",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}

@Composable
private fun PerformanceComparison(inlineTime: Long, nonInlineTime: Long, context: String) {
    val improvement = if (nonInlineTime > inlineTime) {
        ((nonInlineTime - inlineTime).toFloat() / nonInlineTime * 100).toInt()
    } else {
        -((inlineTime - nonInlineTime).toFloat() / inlineTime * 100).toInt()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (improvement > 0)
            Color(0xFF4CAF50).copy(alpha = 0.1f)
        else
            Color(0xFFFF5722).copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (improvement > 0)
                        "⚡ Inline이 더 빠름"
                    else
                        "⚠️ Non-inline이 더 빠름",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (improvement > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
                Text(
                    text = "${kotlin.math.abs(improvement)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (improvement > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$context 실행 시간 차이: ${kotlin.math.abs(nonInlineTime - inlineTime)}ms",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
