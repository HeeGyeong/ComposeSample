package com.example.composesample.presentation.example.component.ui.layout.flexbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun FlowRowLayoutExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "FlowRow / FlowColumn (Compose Flexbox)", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { FlowRowConceptCard() }
            item { FlowRowVsRowCard() }
            item { FlowRowArrangementCard() }
            item { FlowRowMaxItemsCard() }
            item { FlowRowWeightCard() }
            item { FlowColumnCard() }
            item { PracticalFilterChipsCard() }
        }
    }
}

@Composable
private fun FlowRowConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FlowRow / FlowColumn 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "CSS Flexbox의 flex-wrap을 Compose에서 구현한 공식 레이아웃입니다. " +
                        "아이템이 가로(FlowRow) 또는 세로(FlowColumn) 방향으로 배치되다 공간이 부족하면 자동으로 줄바꿈합니다.",
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val features = listOf(
                "Row" to "줄바꿈 없음 → 넘치면 잘림",
                "FlowRow" to "공간 부족 시 자동 줄바꿈",
                "FlowColumn" to "공간 부족 시 다음 열로 이동"
            )
            features.forEach { (api, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = api,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.width(110.dp)
                    )
                    Text(text = desc, fontSize = 12.sp, color = Color(0xFF424242))
                }
            }
        }
    }
}

@Composable
private fun FlowRowVsRowCard() {
    val tags = listOf("Android", "Compose", "Jetpack", "FlowRow", "Kotlin", "Layout", "UI", "Material3")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Row vs FlowRow 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 일반 Row: 넘침
            Text(text = "Row (넘침 처리 없음)", fontSize = 13.sp, color = Color(0xFFE53935), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row {
                    tags.forEach { tag ->
                        TagChip(label = tag, color = Color(0xFFEF9A9A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FlowRow: 자동 줄바꿈
            Text(text = "FlowRow (자동 줄바꿈)", fontSize = 13.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        TagChip(label = tag, color = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowRowArrangementCard() {
    val items = listOf("짧음", "조금더긴텍스트", "중간길이", "A", "긴텍스트항목", "B", "마지막")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "horizontalArrangement 옵션",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val arrangements = listOf(
                "Start" to Arrangement.Start,
                "Center" to Arrangement.Center,
                "SpaceBetween" to Arrangement.SpaceBetween,
                "spacedBy(8.dp)" to Arrangement.spacedBy(8.dp)
            )

            arrangements.forEach { (name, arrangement) ->
                Text(
                    text = name,
                    fontSize = 12.sp,
                    color = Color(0xFF7B1FA2),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = arrangement
                    ) {
                        items.take(4).forEach { item ->
                            TagChip(label = item, color = Color(0xFF9C27B0))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FlowRowMaxItemsCard() {
    val items = listOf("항목1", "항목2", "항목3", "항목4", "항목5", "항목6", "항목7", "항목8")
    var maxItems by remember { mutableIntStateOf(3) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "maxItemsInEachRow",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "한 행에 배치할 최대 아이템 수를 제한합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 조절 버튼
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "maxItemsInEachRow: $maxItems", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { if (maxItems > 1) maxItems-- },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEF6C00)),
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("-", color = Color.White, fontSize = 16.sp)
                }
                Button(
                    onClick = { if (maxItems < 8) maxItems++ },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEF6C00)),
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", color = Color.White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = maxItems
                ) {
                    items.forEach { item ->
                        TagChip(label = item, color = Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowRowWeightCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFF3F7FF)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Modifier.weight() in FlowRow",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "FlowRowScope는 RowScope를 상속하므로 weight()로 남은 공간을 비율 배분합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // weight 적용 예시: 고정 아이템 + weight로 나머지 채우기
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    // 고정 너비 태그
                    TagChip(label = "고정", color = Color(0xFF1976D2))
                    // weight(1f) → 남은 공간 전체 차지
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .background(Color(0xFFBBDEFB), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "weight(1f) → 나머지 공간", fontSize = 11.sp, color = Color(0xFF1565C0))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 균등 분배 예시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = 3
                ) {
                    repeat(6) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .background(
                                    if (i % 2 == 0) Color(0xFF1976D2) else Color(0xFF42A5F5),
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "1f", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "maxItemsInEachRow=3, 각 아이템 weight(1f) → 행마다 3등분",
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun FlowColumnCard() {
    val items = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FlowColumn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00695C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "세로로 아이템을 배치하다 높이가 초과되면 다음 열로 이동합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(1.dp, Color(0xFF80CBC4), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // maxItemsInEachColumn으로 열당 최대 4개
                FlowColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachColumn = 4
                ) {
                    items.forEach { item ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF009688), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "maxItemsInEachColumn=4 → 4개씩 채운 후 다음 열로 이동",
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun PracticalFilterChipsCard() {
    val allTags = listOf(
        "Android", "Compose", "Kotlin", "Material3", "Coroutines",
        "Flow", "Hilt", "Room", "Retrofit", "Ktor", "Navigation", "Testing"
    )
    var selectedTags by remember { mutableStateOf(setOf("Android", "Compose")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFFFF8E1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실용 예제: 필터 칩",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "FlowRow는 동적 크기의 태그/칩 UI에 가장 적합합니다. 탭하여 선택/해제해보세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTags.forEach { tag ->
                    val isSelected = tag in selectedTags
                    FilterChip(
                        label = tag,
                        isSelected = isSelected,
                        onClick = {
                            selectedTags = if (isSelected) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "선택됨: ${selectedTags.joinToString(", ").ifEmpty { "없음" }}",
                fontSize = 12.sp,
                color = Color(0xFFF57F17),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── 재사용 컴포넌트 ────────────────────────────────────────────────────────

@Composable
private fun TagChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color(0xFFF57F17) else Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = null,
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Text(
            text = if (isSelected) "✓ $label" else label,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color(0xFFF57F17)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FlowRowLayoutPreview() {
    FlowRowLayoutExampleUI(onBackEvent = {})
}
