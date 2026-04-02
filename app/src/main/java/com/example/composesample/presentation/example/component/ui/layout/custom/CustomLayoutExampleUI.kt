package com.example.composesample.presentation.example.component.ui.layout.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CustomLayout Example 참고 자료
 * - 공식 문서: https://developer.android.com/develop/ui/compose/layouts/custom
 * - Layout composable: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/package-summary#Layout
 * - MeasurePolicy: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/MeasurePolicy
 */

@Composable
fun CustomLayoutExampleUI(onBackEvent: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackEvent.invoke() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
                Text(
                    text = "Custom Layout (MeasurePolicy)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            SectionTitle("1. 기본 세로 쌓기 레이아웃")
            Spacer(modifier = Modifier.height(8.dp))
            // 직접 구현한 Column과 동일한 동작
            SimpleVerticalLayout {
                ColorBox(color = Color(0xFFEF9A9A), label = "항목 A")
                ColorBox(color = Color(0xFFA5D6A7), label = "항목 B")
                ColorBox(color = Color(0xFF90CAF9), label = "항목 C")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SectionTitle("2. 가로 중앙 정렬 레이아웃")
            Spacer(modifier = Modifier.height(8.dp))
            // 각 자식을 부모 너비 기준 가로 중앙에 배치
            CenterHorizontalLayout {
                ColorBox(color = Color(0xFFCE93D8), label = "좁은 항목", widthFraction = 0.4f)
                ColorBox(color = Color(0xFFFFCC80), label = "넓은 항목", widthFraction = 0.8f)
                ColorBox(color = Color(0xFF80DEEA), label = "중간 항목", widthFraction = 0.6f)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SectionTitle("3. 계단식 오프셋 레이아웃")
            Spacer(modifier = Modifier.height(8.dp))
            // 각 항목을 stepOffset만큼 오른쪽으로 밀어서 배치
            StaircaseLayout(stepOffset = 24) {
                ColorBox(color = Color(0xFFFFAB91), label = "1단계")
                ColorBox(color = Color(0xFFF48FB1), label = "2단계")
                ColorBox(color = Color(0xFF80CBC4), label = "3단계")
                ColorBox(color = Color(0xFFB0BEC5), label = "4단계")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ExplanationCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 1. SimpleVerticalLayout — Layout composable + MeasurePolicy 람다 직접 구현.
 *    각 자식을 순서대로 세로로 쌓는다. (Column과 동일한 동작, 학습용)
 */
@Composable
fun SimpleVerticalLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier,
        measurePolicy = MeasurePolicy { measurables, constraints ->
            // 1) 각 자식을 측정 (세로 쌓기이므로 최대 너비 허용, 높이 제한 없음)
            val placeables: List<Placeable> = measurables.map { measurable ->
                measurable.measure(constraints.copy(minHeight = 0))
            }

            // 2) 전체 높이 = 각 자식 높이의 합
            val totalHeight = placeables.sumOf { it.height }
            val width = constraints.maxWidth

            // 3) 레이아웃 크기 결정 + 배치
            layout(width, totalHeight) {
                var yPosition = 0
                placeables.forEach { placeable ->
                    placeable.placeRelative(x = 0, y = yPosition)
                    yPosition += placeable.height
                }
            }
        }
    )
}

/**
 * 2. CenterHorizontalLayout — 각 자식을 부모 너비 기준 가로 중앙에 배치.
 *    자식이 widthFraction으로 너비를 지정하면, x 오프셋을 계산하여 중앙 정렬.
 */
@Composable
fun CenterHorizontalLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier,
        measurePolicy = MeasurePolicy { measurables, constraints ->
            val placeables: List<Placeable> = measurables.map { measurable ->
                measurable.measure(Constraints(maxWidth = constraints.maxWidth))
            }

            val totalHeight = placeables.sumOf { it.height }
            val width = constraints.maxWidth

            layout(width, totalHeight) {
                var yPosition = 0
                placeables.forEach { placeable ->
                    // 가로 중앙: (부모 너비 - 자식 너비) / 2
                    val xOffset = (width - placeable.width) / 2
                    placeable.placeRelative(x = xOffset, y = yPosition)
                    yPosition += placeable.height
                }
            }
        }
    )
}

/**
 * 3. StaircaseLayout — 각 항목을 stepOffset px씩 오른쪽으로 이동하여 계단식 배치.
 */
@Composable
fun StaircaseLayout(
    modifier: Modifier = Modifier,
    stepOffset: Int = 20,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier,
        measurePolicy = MeasurePolicy { measurables, constraints ->
            val placeables: List<Placeable> = measurables.mapIndexed { index, measurable ->
                // 각 항목의 최대 너비를 계단 오프셋만큼 줄임
                val maxWidth = (constraints.maxWidth - stepOffset * index).coerceAtLeast(0)
                measurable.measure(constraints.copy(maxWidth = maxWidth, minWidth = 0))
            }

            val totalHeight = placeables.sumOf { it.height }
            val width = constraints.maxWidth

            layout(width, totalHeight) {
                var yPosition = 0
                placeables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(x = stepOffset * index, y = yPosition)
                    yPosition += placeable.height
                }
            }
        }
    )
}

/** 예제용 색상 박스 */
@Composable
private fun ColorBox(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(48.dp)
            .background(color, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
}

/** 핵심 개념 설명 카드 */
@Composable
private fun ExplanationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Layout / MeasurePolicy 핵심 개념",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "Layout composable: 자식 측정·배치를 직접 제어",
                "MeasurePolicy: measure() → layout() 두 단계로 구성",
                "measurable.measure(constraints): 자식 크기 결정",
                "placeable.placeRelative(x, y): RTL 자동 반영 배치",
                "placeable.place(x, y): RTL 미반영 절대 배치",
                "SubcomposeLayout 없이 단순 측정·배치에 적합",
                "constraints: 부모가 허용하는 최소/최대 크기 제약"
            ).forEach { line ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "• $line", fontSize = 13.sp)
            }
        }
    }
}
