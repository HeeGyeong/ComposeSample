package com.example.composesample.presentation.example.component.ui.visibility

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathHitTester
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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

/**
 * 커스텀 Visibility + PathHitTester 예제
 * PathHitTester 참고: https://www.romainguy.dev/posts/2025/arbitrary-shape-tap-detection/
 *
 * PathHitTester는 임의의 Path 내부에 특정 좌표가 포함되는지 확인하는 API
 * - 별 모양 Path를 탭하면 PathHitTester로 hit test 후 색상 변경
 */
@Composable
private fun CustomVisibilityModifierCard() {
    var selectedVisibility by remember { mutableIntStateOf(0) }
    val visibilities = listOf("Visible", "Invisible", "Gone")

    // PathHitTester 관련 상태 - 각 Box별 탭 상태
    var box1Tapped by remember { mutableStateOf(false) }
    var box2Tapped by remember { mutableStateOf(false) }
    var box3Tapped by remember { mutableStateOf(false) }
    var tapResult by remember { mutableStateOf("별 모양을 탭해보세요!") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎨 커스텀 Modifier.visible() + PathHitTester",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "View 시스템과 유사한 API + 별 탭 시 색상 변경",
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

            // 데모 영역 - Canvas로 별 모양 Box 그리기
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(1.dp, Color(0xFF7B1FA2).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedVisibility) {
                            detectTapGestures { offset ->
                                val starSize = 35f
                                val boxHeight = 50f
                                val boxPadding = 12f
                                val starCenterX = size.width - boxPadding - starSize - 8f

                                // Box 1 별 Path
                                val star1Path = createStarPath(
                                    centerX = starCenterX,
                                    centerY = boxPadding + boxHeight / 2,
                                    outerRadius = starSize,
                                    innerRadius = starSize * 0.5f
                                )
                                val star1HitTester = PathHitTester(star1Path)

                                // Box 2 별 Path (Visible일 때만)
                                val star2Path = createStarPath(
                                    centerX = starCenterX,
                                    centerY = boxPadding + boxHeight + 8f + boxHeight / 2,
                                    outerRadius = starSize,
                                    innerRadius = starSize * 0.5f
                                )
                                val star2HitTester = PathHitTester(star2Path)

                                // Box 3 별 Path - Gone일 때는 위치가 다름
                                val box3Y = when (Visibility.entries[selectedVisibility]) {
                                    Visibility.Gone -> boxPadding + boxHeight + 8f + boxHeight / 2
                                    else -> boxPadding + (boxHeight + 8f) * 2 + boxHeight / 2
                                }
                                val star3Path = createStarPath(
                                    centerX = starCenterX,
                                    centerY = box3Y,
                                    outerRadius = starSize,
                                    innerRadius = starSize * 0.5f
                                )
                                val star3HitTester = PathHitTester(star3Path)

                                // Hit test
                                when {
                                    offset in star1HitTester -> {
                                        box1Tapped = !box1Tapped
                                        tapResult = if (box1Tapped) "⭐ Box 1 별 활성화!" else "Box 1 별 비활성화"
                                    }
                                    selectedVisibility != 2 && offset in star2HitTester -> {
                                        // Gone이 아닐 때만 Box 2 hit test
                                        box2Tapped = !box2Tapped
                                        tapResult = if (box2Tapped) "⭐ Box 2 별 활성화!" else "Box 2 별 비활성화"
                                    }
                                    offset in star3HitTester -> {
                                        box3Tapped = !box3Tapped
                                        tapResult = if (box3Tapped) "⭐ Box 3 별 활성화!" else "Box 3 별 비활성화"
                                    }
                                    else -> {
                                        tapResult = "별 외부를 탭했습니다"
                                    }
                                }
                            }
                        }
                ) {
                    val starSize = 35f
                    val boxHeight = 50f
                    val boxPadding = 12f
                    val boxWidth = size.width - boxPadding * 2
                    val starCenterX = size.width - boxPadding - starSize - 8f

                    // Box 1
                    drawRoundRect(
                        color = if (box1Tapped) Color(0xFFBBDEFB) else Color(0xFFE3F2FD),
                        topLeft = Offset(boxPadding, boxPadding),
                        size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                    )
                    val star1Path = createStarPath(starCenterX, boxPadding + boxHeight / 2, starSize, starSize * 0.5f)
                    drawPath(star1Path, if (box1Tapped) Color(0xFFFFD700) else Color(0xFFFFC107), style = Fill)
                    drawPath(star1Path, Color(0xFFFF8F00), style = Stroke(width = 2f))

                    // Box 2 (Visibility에 따라)
                    val box2Y = boxPadding + boxHeight + 8f
                    when (Visibility.entries[selectedVisibility]) {
                        Visibility.Visible -> {
                            drawRoundRect(
                                color = if (box2Tapped) Color(0xFFD1C4E9) else Color(0xFFE1BEE7),
                                topLeft = Offset(boxPadding, box2Y),
                                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                            )
                            val star2Path = createStarPath(starCenterX, box2Y + boxHeight / 2, starSize, starSize * 0.5f)
                            drawPath(star2Path, if (box2Tapped) Color(0xFFFFD700) else Color(0xFFFFC107), style = Fill)
                            drawPath(star2Path, Color(0xFFFF8F00), style = Stroke(width = 2f))
                        }
                        Visibility.Invisible -> {
                            // alpha 0f - 공간은 차지하지만 보이지 않음 (테두리만 표시)
                            drawRoundRect(
                                color = Color(0xFFE1BEE7).copy(alpha = 0.2f),
                                topLeft = Offset(boxPadding, box2Y),
                                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
                                style = Stroke(width = 1f)
                            )
                        }
                        Visibility.Gone -> {
                            // 아무것도 그리지 않음 - 공간도 없음
                        }
                    }

                    // Box 3 - Gone일 때는 위로 올라감
                    val box3Y = when (Visibility.entries[selectedVisibility]) {
                        Visibility.Gone -> box2Y
                        else -> boxPadding + (boxHeight + 8f) * 2
                    }
                    drawRoundRect(
                        color = if (box3Tapped) Color(0xFFC8E6C9) else Color(0xFFE8F5E9),
                        topLeft = Offset(boxPadding, box3Y),
                        size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                    )
                    val star3Path = createStarPath(starCenterX, box3Y + boxHeight / 2, starSize, starSize * 0.5f)
                    drawPath(star3Path, if (box3Tapped) Color(0xFFFFD700) else Color(0xFFFFC107), style = Fill)
                    drawPath(star3Path, Color(0xFFFF8F00), style = Stroke(width = 2f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 결과 표시
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEDE7F6)
            ) {
                Text(
                    text = tapResult,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7B1FA2),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (Visibility.entries[selectedVisibility]) {
                    Visibility.Visible -> "✅ Visible: 보임 + 공간 차지"
                    Visibility.Invisible -> "👻 Invisible: 안 보임 + 공간 차지 (점선 표시)"
                    Visibility.Gone -> "❌ Gone: 안 보임 + 공간 없음 (Box 3이 위로 이동)"
                },
                fontSize = 11.sp,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "PathHitTester로 별 모양 내부만 정확히 탭 감지",
                fontSize = 10.sp,
                color = Color.Gray
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

/**
 * 별 모양 Path 생성
 */
private fun createStarPath(
    centerX: Float,
    centerY: Float,
    outerRadius: Float,
    innerRadius: Float,
    points: Int = 5
): Path {
    val path = Path()
    val angleStep = Math.PI / points

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - Math.PI / 2
        val x = centerX + (radius * kotlin.math.cos(angle)).toFloat()
        val y = centerY + (radius * kotlin.math.sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

