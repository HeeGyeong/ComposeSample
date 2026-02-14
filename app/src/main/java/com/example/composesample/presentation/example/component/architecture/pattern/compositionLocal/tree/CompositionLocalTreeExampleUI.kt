package com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.tree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * CompositionLocal Tree Visualization Example UI
 *
 * Composition Tree에서 CompositionLocal이 어떻게 동작하는지 시각적으로 보여줍니다.
 */

// === 예제용 CompositionLocal 정의 ===
private val LocalThemeColor = compositionLocalOf { Color(0xFF1976D2) }
private val LocalUserName = compositionLocalOf { "Default User" }
private val LocalCounter = compositionLocalOf { 0 }

@Composable
fun CompositionLocalTreeExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedExample by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabItem("Tree", selectedExample == 0, { selectedExample = 0 }, Modifier.weight(1f))
            TabItem("Lookup", selectedExample == 1, { selectedExample = 1 }, Modifier.weight(1f))
            TabItem("Shadow", selectedExample == 2, { selectedExample = 2 }, Modifier.weight(1f))
            TabItem("Recomp", selectedExample == 3, { selectedExample = 3 }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedExample) {
            0 -> TreeStructureDemo()
            1 -> LookupWalkDemo()
            2 -> ShadowingDemo()
            3 -> RecompositionScopeDemo()
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "CompositionLocal Tree",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Composition 트리에서의 데이터 흐름 시각화",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1976D2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

// ==================== 1. Tree Structure Demo ====================

/**
 * Composition Tree 구조와 CompositionLocalProvider가
 * 노드에 데이터를 부착하는 방식을 시각화합니다.
 */
@Composable
private fun TreeStructureDemo() {
    val textMeasurer = rememberTextMeasurer()
    var showProvider by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Composition Tree Structure",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Compose가 컴포저블을 실행하면 내부적으로 트리가 만들어집니다.\n" +
                                "각 노드는 선택적 'locals map'을 가질 수 있습니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showProvider = !showProvider },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showProvider) Color(0xFF4CAF50) else Color(0xFF1976D2)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (showProvider) "Provider 제거" else "Provider 추가",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            // Tree visualization
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(16.dp)
                ) {
                    drawTreeStructure(textMeasurer, showProvider)
                }
            }
        }

        item {
            // Locals map explanation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (showProvider) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (showProvider) "✅ Provider 활성" else "📋 Provider 없음",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showProvider) Color(0xFF2E7D32) else Color(0xFFF57F17)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (showProvider)
                            "Provider 노드에 { LocalTheme → Dark } 맵이 부착되었습니다.\n" +
                                    "하위 노드들은 이 값을 '상속'하는 것이 아니라, " +
                                    "필요할 때 트리를 올라가며 찾습니다."
                        else
                            "Provider가 없으면 모든 노드의 locals map이 비어있습니다.\n" +
                                    ".current 호출 시 기본값(defaultValue)을 사용합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTreeStructure(textMeasurer: TextMeasurer, showProvider: Boolean) {
    val centerX = size.width / 2f
    val nodeRadius = 28f
    val levelGap = 75f
    val startY = 40f

    // Node positions
    val appPos = Offset(centerX, startY)
    val providerPos = Offset(centerX, startY + levelGap)
    val screenPos = Offset(centerX, startY + levelGap * 2)
    val cardPos = Offset(centerX - 80f, startY + levelGap * 3)
    val textPos = Offset(centerX + 80f, startY + levelGap * 3)

    // Edges
    drawTreeEdge(appPos, providerPos, nodeRadius)
    drawTreeEdge(providerPos, screenPos, nodeRadius)
    drawTreeEdge(screenPos, cardPos, nodeRadius)
    drawTreeEdge(screenPos, textPos, nodeRadius)

    // Nodes
    drawTreeNode(textMeasurer, appPos, "App", nodeRadius, Color(0xFF546E7A))

    if (showProvider) {
        drawTreeNode(textMeasurer, providerPos, "Provider", nodeRadius + 4f, Color(0xFF4CAF50))
        // Locals map annotation
        val mapText = "{ LocalTheme → Dark }"
        val mapStyle = TextStyle(fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
        val mapLayout = textMeasurer.measure(mapText, mapStyle)
        drawText(
            textMeasurer = textMeasurer,
            text = mapText,
            topLeft = Offset(providerPos.x + nodeRadius + 12f, providerPos.y - mapLayout.size.height / 2f),
            style = mapStyle
        )
    } else {
        drawTreeNode(textMeasurer, providerPos, "Provider", nodeRadius, Color(0xFFBDBDBD))
        val emptyText = "locals: { }"
        val emptyStyle = TextStyle(fontSize = 11.sp, color = Color(0xFF9E9E9E))
        val emptyLayout = textMeasurer.measure(emptyText, emptyStyle)
        drawText(
            textMeasurer = textMeasurer,
            text = emptyText,
            topLeft = Offset(providerPos.x + nodeRadius + 12f, providerPos.y - emptyLayout.size.height / 2f),
            style = emptyStyle
        )
    }

    drawTreeNode(textMeasurer, screenPos, "Screen", nodeRadius, Color(0xFF546E7A))
    drawTreeNode(textMeasurer, cardPos, "Card", nodeRadius, Color(0xFF546E7A))
    drawTreeNode(textMeasurer, textPos, "Text", nodeRadius, Color(0xFF546E7A))

    // Empty locals annotation for children
    val childAnnotations = listOf(screenPos to "상속 아님, 룩업", cardPos to "locals: { }", textPos to "locals: { }")
    childAnnotations.forEach { (pos, label) ->
        val style = TextStyle(fontSize = 10.sp, color = Color(0xFF9E9E9E))
        val layout = textMeasurer.measure(label, style)
        drawText(
            textMeasurer = textMeasurer,
            text = label,
            topLeft = Offset(pos.x + nodeRadius + 12f, pos.y - layout.size.height / 2f),
            style = style
        )
    }
}

private fun DrawScope.drawTreeNode(
    textMeasurer: TextMeasurer,
    center: Offset,
    label: String,
    radius: Float,
    color: Color
) {
    // Shadow
    drawCircle(color = Color.Black.copy(alpha = 0.1f), radius = radius + 2f, center = Offset(center.x + 1f, center.y + 2f))
    // Node circle
    drawCircle(color = color, radius = radius, center = center)
    // Label
    val style = TextStyle(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    val layout = textMeasurer.measure(label, style)
    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f),
        style = style
    )
}

private fun DrawScope.drawTreeEdge(from: Offset, to: Offset, nodeRadius: Float) {
    val dirX = to.x - from.x
    val dirY = to.y - from.y
    val len = kotlin.math.sqrt(dirX * dirX + dirY * dirY)
    val nx = dirX / len
    val ny = dirY / len

    drawLine(
        color = Color(0xFFBDBDBD),
        start = Offset(from.x + nx * nodeRadius, from.y + ny * nodeRadius),
        end = Offset(to.x - nx * nodeRadius, to.y - ny * nodeRadius),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
}

// ==================== 2. Lookup Walk Demo ====================

/**
 * LocalTheme.current 호출 시 트리를 위로 올라가며 탐색하는
 * 룩업 메커니즘을 애니메이션으로 시각화합니다.
 */
@Composable
private fun LookupWalkDemo() {
    val textMeasurer = rememberTextMeasurer()
    var lookupStep by remember { mutableIntStateOf(-1) }
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            lookupStep = 0
            delay(800)
            lookupStep = 1
            delay(800)
            lookupStep = 2
            delay(800)
            lookupStep = 3
            delay(1200)
            lookupStep = -1
            isAnimating = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Lookup Mechanism",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "LocalTheme.current 호출 시, Compose는 현재 노드에서 " +
                                "트리를 위로 올라가며 Provider를 찾습니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { if (!isAnimating) isAnimating = true },
                        enabled = !isAnimating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "룩업 애니메이션 시작", fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(16.dp)
                ) {
                    drawLookupTree(textMeasurer, lookupStep)
                }
            }
        }

        item {
            // Step description
            val stepDescriptions = listOf(
                "1️⃣ [Text]에서 시작: \"LocalTheme가 필요해\"",
                "2️⃣ [Card] 확인 → 없음, 부모로 이동...",
                "3️⃣ [Screen(Provider)] 확인 → 찾았다! LocalTheme → Dark ✓",
                "✅ 값 반환: Dark (트리 위쪽에서 가장 먼저 찾은 값)"
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (lookupStep >= 0) Color(0xFFFFF3E0) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (lookupStep in 0..3) {
                        Text(
                            text = stepDescriptions[lookupStep],
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            lineHeight = 22.sp
                        )
                    } else {
                        Text(
                            text = "버튼을 눌러 룩업 과정을 확인하세요.",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "lookup(key, nodeIndex):\n" +
                                "  current = nodeIndex\n" +
                                "  while current != null:\n" +
                                "    if current.locals.contains(key):\n" +
                                "      return current.locals[key]\n" +
                                "    current = current.parentIndex\n" +
                                "  return key.defaultValue",
                        fontSize = 12.sp,
                        color = Color(0xFF616161),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawLookupTree(textMeasurer: TextMeasurer, lookupStep: Int) {
    val centerX = size.width / 2f
    val nodeRadius = 28f
    val levelGap = 80f
    val startY = 40f

    val appPos = Offset(centerX, startY)
    val screenPos = Offset(centerX, startY + levelGap)  // Provider here
    val cardPos = Offset(centerX, startY + levelGap * 2)
    val textPos = Offset(centerX, startY + levelGap * 3)

    // Edges
    drawTreeEdge(appPos, screenPos, nodeRadius)
    drawTreeEdge(screenPos, cardPos, nodeRadius)
    drawTreeEdge(cardPos, textPos, nodeRadius)

    // Step-dependent colors
    val appColor = if (lookupStep >= 3) Color(0xFF9E9E9E) else Color(0xFF546E7A)
    val screenColor = when {
        lookupStep == 2 || lookupStep == 3 -> Color(0xFF4CAF50) // Found!
        else -> Color(0xFF4CAF50).copy(alpha = 0.6f)
    }
    val cardColor = when {
        lookupStep == 1 -> Color(0xFFFF9800) // Checking
        else -> Color(0xFF546E7A)
    }
    val textColor = when {
        lookupStep == 0 -> Color(0xFFFF9800) // Start
        else -> Color(0xFF546E7A)
    }

    // Draw lookup arrow animation
    if (lookupStep >= 0) {
        val positions = listOf(textPos, cardPos, screenPos, appPos)
        val targetIdx = lookupStep.coerceAtMost(2) // Found at screen(index 2)
        for (i in 0 until targetIdx) {
            val from = positions[i]
            val to = positions[i + 1]
            drawLookupArrow(from, to, nodeRadius, Color(0xFFFF9800))
        }
    }

    // Nodes
    drawTreeNode(textMeasurer, appPos, "App", nodeRadius, appColor)
    drawTreeNode(textMeasurer, screenPos, "Screen", nodeRadius + 2f, screenColor)
    drawTreeNode(textMeasurer, cardPos, "Card", nodeRadius, cardColor)
    drawTreeNode(textMeasurer, textPos, "Text", nodeRadius, textColor)

    // Provider label
    val provLabel = "{ LocalTheme → Dark }"
    val provStyle = TextStyle(fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
    val provLayout = textMeasurer.measure(provLabel, provStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = provLabel,
        topLeft = Offset(screenPos.x + nodeRadius + 12f, screenPos.y - provLayout.size.height / 2f),
        style = provStyle
    )

    // Step markers
    val markers = listOf(
        textPos to "1. Start",
        cardPos to "2. 없음 ↑",
        screenPos to "3. 찾음! ✓"
    )
    markers.forEachIndexed { idx, (pos, label) ->
        if (lookupStep >= idx) {
            val style = TextStyle(
                fontSize = 10.sp,
                color = if (idx == 2) Color(0xFF2E7D32) else Color(0xFFE65100),
                fontWeight = FontWeight.Bold
            )
            val layout = textMeasurer.measure(label, style)
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(pos.x - nodeRadius - layout.size.width - 8f, pos.y - layout.size.height / 2f),
                style = style
            )
        }
    }
}

private fun DrawScope.drawLookupArrow(from: Offset, to: Offset, nodeRadius: Float, color: Color) {
    val dirX = to.x - from.x
    val dirY = to.y - from.y
    val len = kotlin.math.sqrt(dirX * dirX + dirY * dirY)
    val nx = dirX / len
    val ny = dirY / len

    val startOffset = Offset(from.x + nx * (nodeRadius + 4f), from.y + ny * (nodeRadius + 4f))
    val endOffset = Offset(to.x - nx * (nodeRadius + 4f), to.y - ny * (nodeRadius + 4f))

    drawLine(
        color = color,
        start = startOffset,
        end = endOffset,
        strokeWidth = 3f,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
    )

    // Arrowhead
    val arrowSize = 10f
    val arrowPath = Path().apply {
        moveTo(endOffset.x, endOffset.y)
        lineTo(endOffset.x - nx * arrowSize - ny * arrowSize * 0.5f, endOffset.y - ny * arrowSize + nx * arrowSize * 0.5f)
        lineTo(endOffset.x - nx * arrowSize + ny * arrowSize * 0.5f, endOffset.y - ny * arrowSize - nx * arrowSize * 0.5f)
        close()
    }
    drawPath(arrowPath, color = color)
}

// ==================== 3. Shadowing Demo ====================

/**
 * 중첩된 Provider에서 같은 키를 다시 제공할 때
 * 자식이 가까운 값을 사용하는 섀도잉을 시각화합니다.
 */
@Composable
private fun ShadowingDemo() {
    val textMeasurer = rememberTextMeasurer()
    var innerTheme by remember { mutableStateOf("Light") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Shadowing with Nested Providers",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "중첩된 Provider가 같은 키를 제공하면, 자식 노드는 " +
                                "가장 가까운 Provider의 값을 사용합니다 (섀도잉).",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { innerTheme = "Light" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (innerTheme == "Light") Color(0xFFFFB300) else Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Inner = Light",
                                color = if (innerTheme == "Light") Color.White else Color(0xFF616161)
                            )
                        }
                        Button(
                            onClick = { innerTheme = "Red" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (innerTheme == "Red") Color(0xFFF44336) else Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Inner = Red",
                                color = if (innerTheme == "Red") Color.White else Color(0xFF616161)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .padding(16.dp)
                ) {
                    drawShadowingTree(textMeasurer, innerTheme)
                }
            }
        }

        // Live demo with actual CompositionLocalProvider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "실제 동작 확인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Outer Provider: Dark (Blue)
                    CompositionLocalProvider(LocalThemeColor provides Color(0xFF1976D2)) {
                        ThemeColorBox(label = "Outer: Dark (Blue)", depth = 0)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Inner Provider: shadows with different color
                        val innerColor = if (innerTheme == "Light") Color(0xFFFFB300) else Color(0xFFF44336)
                        CompositionLocalProvider(LocalThemeColor provides innerColor) {
                            ThemeColorBox(label = "Inner: $innerTheme (Shadowed)", depth = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeColorBox(label: String, depth: Int) {
    val themeColor = LocalThemeColor.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 24).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(themeColor)
        )
        Text(
            text = "$label → LocalThemeColor.current",
            fontSize = 13.sp,
            color = Color(0xFF424242)
        )
    }
}

private fun DrawScope.drawShadowingTree(textMeasurer: TextMeasurer, innerTheme: String) {
    val centerX = size.width / 2f
    val nodeRadius = 26f
    val levelGap = 70f
    val startY = 30f
    val branchOffset = 100f

    val appPos = Offset(centerX, startY)
    val outerProvPos = Offset(centerX, startY + levelGap)
    val leftBranch = Offset(centerX - branchOffset, startY + levelGap * 2)
    val innerProvPos = Offset(centerX + branchOffset, startY + levelGap * 2)
    val leftCard = Offset(centerX - branchOffset, startY + levelGap * 3)
    val rightScreen = Offset(centerX + branchOffset, startY + levelGap * 3)
    val leftText = Offset(centerX - branchOffset, startY + levelGap * 4)
    val rightText = Offset(centerX + branchOffset, startY + levelGap * 4)

    // Edges
    drawTreeEdge(appPos, outerProvPos, nodeRadius)
    drawTreeEdge(outerProvPos, leftBranch, nodeRadius)
    drawTreeEdge(outerProvPos, innerProvPos, nodeRadius)
    drawTreeEdge(leftBranch, leftCard, nodeRadius)
    drawTreeEdge(innerProvPos, rightScreen, nodeRadius)
    drawTreeEdge(leftCard, leftText, nodeRadius)
    drawTreeEdge(rightScreen, rightText, nodeRadius)

    // Nodes
    drawTreeNode(textMeasurer, appPos, "App", nodeRadius, Color(0xFF546E7A))
    drawTreeNode(textMeasurer, outerProvPos, "Prov₁", nodeRadius + 2f, Color(0xFF1976D2))
    drawTreeNode(textMeasurer, leftBranch, "Screen", nodeRadius, Color(0xFF546E7A))

    val innerColor = if (innerTheme == "Light") Color(0xFFFFB300) else Color(0xFFF44336)
    drawTreeNode(textMeasurer, innerProvPos, "Prov₂", nodeRadius + 2f, innerColor)
    drawTreeNode(textMeasurer, leftCard, "Card", nodeRadius, Color(0xFF546E7A))
    drawTreeNode(textMeasurer, rightScreen, "Another", nodeRadius, Color(0xFF546E7A))
    drawTreeNode(textMeasurer, leftText, "Text", nodeRadius, Color(0xFF546E7A))
    drawTreeNode(textMeasurer, rightText, "Text", nodeRadius, Color(0xFF546E7A))

    // Annotations
    val darkStyle = TextStyle(fontSize = 10.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
    val darkLabel = "{ Theme → Dark }"
    val darkLayout = textMeasurer.measure(darkLabel, darkStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = darkLabel,
        topLeft = Offset(outerProvPos.x + nodeRadius + 8f, outerProvPos.y - darkLayout.size.height / 2f),
        style = darkStyle
    )

    val innerLabel = "{ Theme → $innerTheme }"
    val innerStyle = TextStyle(fontSize = 10.sp, color = innerColor, fontWeight = FontWeight.Bold)
    val innerLayout = textMeasurer.measure(innerLabel, innerStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = innerLabel,
        topLeft = Offset(innerProvPos.x + nodeRadius + 8f, innerProvPos.y - innerLayout.size.height / 2f),
        style = innerStyle
    )

    // Result annotations
    val leftResult = "= Dark"
    val leftResultStyle = TextStyle(fontSize = 10.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
    val leftResultLayout = textMeasurer.measure(leftResult, leftResultStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = leftResult,
        topLeft = Offset(leftText.x - nodeRadius - leftResultLayout.size.width - 6f, leftText.y - leftResultLayout.size.height / 2f),
        style = leftResultStyle
    )

    val rightResult = "= $innerTheme"
    val rightResultStyle = TextStyle(fontSize = 10.sp, color = innerColor, fontWeight = FontWeight.Bold)
    val rightResultLayout = textMeasurer.measure(rightResult, rightResultStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = rightResult,
        topLeft = Offset(rightText.x + nodeRadius + 6f, rightText.y - rightResultLayout.size.height / 2f),
        style = rightResultStyle
    )

    // "SHADOWED" label
    val shadowLabel = "SHADOWED ↑"
    val shadowStyle = TextStyle(fontSize = 9.sp, color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
    val shadowLayout = textMeasurer.measure(shadowLabel, shadowStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = shadowLabel,
        topLeft = Offset(rightText.x + nodeRadius + 6f, rightText.y + rightResultLayout.size.height / 2f + 2f),
        style = shadowStyle
    )
}

// ==================== 4. Recomposition Scope Demo ====================

/**
 * compositionLocalOf 사용 시, 값을 실제로 읽는 노드만
 * 리컴포지션되는 것을 시연합니다.
 */
@Composable
private fun RecompositionScopeDemo() {
    var counter by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Recomposition Scope",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "compositionLocalOf 사용 시, 실제로 .current를 읽는 컴포저블만 " +
                                "리컴포지션됩니다. 읽지 않는 컴포저블은 영향받지 않습니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { counter++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Counter + 1")
                        }
                        Text(
                            text = "Counter = $counter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }
        }

        item {
            // Live demo
            CompositionLocalProvider(LocalCounter provides counter) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CompositionLocalProvider(LocalCounter provides $counter)",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )

                        ReaderComponent(name = "Counter Display", reads = true)
                        NonReaderComponent(name = "Static Label")
                        ReaderComponent(name = "Progress Bar", reads = true)
                        NonReaderComponent(name = "Icon Button")
                    }
                }
            }
        }

        // Visual tree diagram
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "리컴포지션 범위 시각화",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Provider(LocalCounter → $counter)\n" +
                                "  │\n" +
                                "  ├─ [Counter Display]  ← 읽음 → 리컴포지션 ✅\n" +
                                "  ├─ [Static Label]     ← 안 읽음 → 스킵 ❌\n" +
                                "  ├─ [Progress Bar]     ← 읽음 → 리컴포지션 ✅\n" +
                                "  └─ [Icon Button]      ← 안 읽음 → 스킵 ❌\n\n" +
                                "  ※ 실제로 .current를 읽는 컴포저블만 리컴포지션!",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF424242),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 핵심 인사이트",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = CompositionLocalTreeGuide.GUIDE_INFO.trimIndent(),
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * LocalCounter.current를 읽는 컴포넌트.
 * 값 변경 시 리컴포지션됨.
 */
@Composable
private fun ReaderComponent(name: String, reads: Boolean) {
    val counterValue = LocalCounter.current
    var recomposeCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(counterValue) {
        recomposeCount++
    }

    val scale by animateFloatAsState(
        targetValue = if (recomposeCount > 0) 1f else 0.95f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8F5E9))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "📖 $name",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "reads .current → 값: $counterValue",
                fontSize = 12.sp,
                color = Color(0xFF4CAF50)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "리컴포지션",
                fontSize = 10.sp,
                color = Color(0xFF757575)
            )
            Text(
                text = "${recomposeCount}회",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

/**
 * LocalCounter.current를 읽지 않는 컴포넌트.
 * 값 변경 시에도 리컴포지션되지 않음.
 */
@Composable
private fun NonReaderComponent(name: String) {
    // LocalCounter.current를 호출하지 않음!
    var recomposeCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        recomposeCount++
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEEEEEE))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "🚫 $name",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF757575)
            )
            Text(
                text = ".current 호출 안 함 → 스킵",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "리컴포지션",
                fontSize = 10.sp,
                color = Color(0xFF757575)
            )
            Text(
                text = "${recomposeCount}회",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompositionLocalTreeExampleUIPreview() {
    CompositionLocalTreeExampleUI(onBackEvent = {})
}
