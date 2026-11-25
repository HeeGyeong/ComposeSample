package com.example.composesample.presentation.example.component.ui.layout.flexbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch

@Composable
fun FlowRowLayoutExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "FlowRow / FlowColumn", onBackIconClicked = onBackEvent)

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
            // Compose FlexBox(foundation-layout 1.13 정식) — 같은 칸을 FlowRow 와 나란히(#187)
            item { FlexBoxIntroCard() }
            item { FlexBoxDefaultsCard() }
            item { FlexBoxMaxItemsCard() }
            item { FlexBoxGrowShrinkCard() }
            item { FlexBoxFillCard() }
            item { FlexBoxSummaryCard() }
        }
    }
}

@Composable
private fun FlowRowConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00)),
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("-", color = Color.White, fontSize = 16.sp)
                }
                Button(
                    onClick = { if (maxItems < 8) maxItems++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00)),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F7FF))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
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
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFFF57F17) else Color(0xFFFFF3E0)
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

// ==================== Compose FlexBox(1.13 정식) — FlowRow 와 나란히(#187) ====================

private val FxOrange = Color(0xFFE65100)
private val FxMuted = Color(0xFF757575)
private val FxBody = Color(0xFF212121)
private val FxOk = Color(0xFF2E7D32)
private val FxBad = Color(0xFFC62828)
internal val FxChip = Color(0xFFFFE0B2)
private val FxFill = Color(0xFFFFB74D)

@Composable
private fun FxCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = FxOrange, lineHeight = 22.sp)
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun FxNote(text: String, color: Color = FxMuted) {
    Text(text = text, fontSize = 12.sp, color = color, lineHeight = 18.sp)
}

@Composable
private fun FxCode(text: String) {
    Text(text = text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = FxBody, lineHeight = 15.sp)
}

/** 비교 무대 — 폭을 FLEX_DEMO_WIDTH_DP 로 고정해 기기마다 같은 조건 */
@Composable
private fun FxStage(tag: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(6.dp))
            .padding(6.dp)
            .width(FLEX_DEMO_WIDTH_DP.dp)
            .testTag(tag)
    ) { content() }
    Spacer(modifier = Modifier.height(6.dp))
}

/** 칩 — 눌리면 글자가 줄바꿈되어 좁아진다(한국어는 글자 사이에서도 줄을 바꿔 최소 고유 폭이 글자 하나) */
@Composable
internal fun FxChipBox(label: String, modifier: Modifier = Modifier, color: Color = FxChip) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 12.sp, color = FxBody)
    }
}

@Composable
private fun rememberDpOf(): (Int) -> String {
    val density = LocalDensity.current.density
    return remember(density) { { px -> oneDecimalText(px / density) } }
}

@Composable
private fun FlexBoxIntroCard() {
    FxCard("Compose FlexBox (1.13 정식) — FlowRow 의 상위 호환") {
        FxNote(
            "foundation-layout 1.13.0 의 FlexBox(modifier, config, content) 는 Row · Column · FlowRow · FlowColumn 을 한 레이아웃으로 " +
                "아우른다(opt-in 없음). 방향 · 줄바꿈 · 정렬 · 간격은 FlexBoxConfig 블록이, 칸마다 grow · shrink · basis · alignSelf · order 는 " +
                "Modifier.flex { } 가 정한다. config 블록은 컴포지션이 아니라 측정 단계에서 실행된다.",
            FxBody
        )
        Spacer(modifier = Modifier.height(6.dp))
        FxNote(
            "이름 주의: 같은 목록의 'FlexBox Layout Example' 은 이 프로젝트 util 에 직접 만든 FlexBoxLayout 으로, Compose 의 FlexBox 와 " +
                "이름만 같다. 아래 카드는 모두 폭 ${FLEX_DEMO_WIDTH_DP.toInt()}dp 무대에서 onPlaced 로 읽은 칸 자리로 비교한다."
        )
    }
}

internal val FxTags = listOf("무료 배송", "오늘 출발", "1+1 행사", "신상품", "한정 수량", "리뷰 이벤트", "카드 할인", "사은품 증정")

@Composable
private fun FlexBoxDefaultsCard() {
    val flowRec = remember { PlacementRecorder() }
    val noWrapRec = remember { PlacementRecorder() }
    val wrapRec = remember { PlacementRecorder() }
    val noWrapConfig = remember { FlexBoxConfig { gap(6.dp) } }
    val wrapConfig = remember { FlexBoxConfig { wrap(FlexWrap.Wrap); gap(6.dp) } }
    val stagePx = with(LocalDensity.current) { FLEX_DEMO_WIDTH_DP.dp.roundToPx() }

    FxCard("① FlowRow 를 FlexBox 로 그대로 바꾸면 — 기본값은 줄바꿈 없음") {
        FxNote("FlexBox 의 기본 config 는 Row 처럼 한 줄(FlexWrap.NoWrap) · shrink 1 이다. FlowRow 처럼 쓰려면 wrap(FlexWrap.Wrap) 을 준다.")
        Spacer(modifier = Modifier.height(8.dp))
        FxCode("FlowRow(spacedBy(6.dp), spacedBy(6.dp))")
        FxStage("flex-defaults-flow") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FxTags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(flowRec, i)) }
            }
        }
        FxCode("FlexBox(config = { gap(6.dp) })  // wrap 없음")
        FxStage("flex-defaults-nowrap") {
            FlexBox(config = noWrapConfig) {
                FxTags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(noWrapRec, i)) }
            }
        }
        FxCode("FlexBox(config = { wrap(FlexWrap.Wrap); gap(6.dp) })")
        FxStage("flex-defaults-wrap") {
            FlexBox(config = wrapConfig) {
                FxTags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(wrapRec, i)) }
            }
        }
        val flow = flowRec.snapshot(FxTags.size)
        val natural = flow?.map { it.width }
        if (natural == null) {
            FxNote("배치 읽는 중…")
        } else {
            listOf("FlowRow" to flow, "FlexBox 기본" to noWrapRec.snapshot(FxTags.size), "FlexBox + Wrap" to wrapRec.snapshot(FxTags.size))
                .forEach { (name, boxes) ->
                    if (boxes != null) {
                        val s = summarize(boxes, natural, stagePx)
                        val bad = s.squeezed.isNotEmpty() || s.overflow.isNotEmpty()
                        Text(
                            text = "$name — 줄 ${s.lines} · 좁아진 칸 ${s.squeezed.size} · 무대 밖 칸 ${s.overflow.size}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (bad) FxBad else FxOk
                        )
                    }
                }
            val wrapBoxes = wrapRec.snapshot(FxTags.size)
            if (wrapBoxes != null) {
                FxNote(
                    if (samePlacement(flow, wrapBoxes)) "FlexBox + Wrap 은 FlowRow 와 ${FxTags.size}칸 모두 같은 자리 · 크기(±1px)."
                    else "⚠ FlexBox + Wrap 과 FlowRow 의 자리가 다르다.",
                    if (samePlacement(flow, wrapBoxes)) FxOk else FxBad
                )
            }
            FxNote(
                "기본(NoWrap)은 칸을 basis(최대 고유 폭) × shrink 비율로 줄이되 최소 고유 폭 아래로는 못 줄인다. 한국어는 글자 사이에서도 " +
                    "줄을 바꿔 최소 고유 폭이 글자 하나라 칩이 세로로 길어지고, 그래도 넘치면 무대 밖으로 그대로 나간다(잘라내려면 clipToBounds)."
            )
        }
    }
}

/** FlowRow 를 감싼 컴포저블 — maxItems 값을 매개변수로 받아 바뀔 때마다 리컴포즈된다 */
@Composable
internal fun FlowRowMaxHost(maxItems: Int, recorder: PlacementRecorder, counter: PassCounter) {
    SideEffect { counter.flowCompositions++ }
    FlowRow(
        modifier = Modifier.countMeasures { counter.flowMeasures++ },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        maxItemsInEachRow = maxItems
    ) {
        FxTags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(recorder, i)) }
    }
}

/** FlexBox 를 감싼 컴포저블 — 값이 아니라 State 를 받아 config 블록(측정 단계)에서만 읽는다 */
@Composable
internal fun FlexBoxMaxHost(maxItems: IntState, recorder: PlacementRecorder, counter: PassCounter) {
    SideEffect { counter.flexCompositions++ }
    val config = remember(maxItems) {
        FlexBoxConfig {
            wrap(FlexWrap.Wrap)
            maxItemsInEachLine(maxItems.intValue)
            gap(6.dp)
        }
    }
    FlexBox(modifier = Modifier.countMeasures { counter.flexMeasures++ }, config = config) {
        FxTags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(recorder, i)) }
    }
}

@Composable
private fun FlexBoxMaxItemsCard() {
    val maxItems = remember { mutableIntStateOf(3) }
    val flowRec = remember { PlacementRecorder() }
    val flexRec = remember { PlacementRecorder() }
    val counter = remember { PassCounter() }
    var shown by remember { mutableStateOf<PassCounter.Snapshot?>(null) }
    var steps by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val step: (Int) -> Unit = { delta ->
        val next = (maxItems.intValue + delta).coerceIn(1, FxTags.size)
        if (next != maxItems.intValue) {
            val before = counter.snapshot()
            maxItems.intValue = next
            steps++
            // 쓰기 뒤 두 프레임 — 리컴포지션 · 측정이 끝난 뒤 센다
            scope.launch {
                withFrameNanos { }
                withFrameNanos { }
                shown = counter.snapshot() - before
            }
        }
    }

    FxCard("② maxItemsInEachLine — FlowRow 의 maxItemsInEachRow 와 같은 자리, 읽는 단계는 다르다") {
        FxNote(
            "같은 칩을 FlowRow(maxItemsInEachRow = n) 와 FlexBox(config = { wrap(Wrap); maxItemsInEachLine(n) }) 로. FlowRow 는 n 을 " +
                "매개변수로 받아 컴포지션에서 읽고, FlexBox 는 n 을 담은 State 를 config 블록(측정 단계)에서 읽는다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "n = ${maxItems.intValue}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { step(-1) }, modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp)) { Text("-") }
            Button(onClick = { step(1) }, modifier = Modifier.size(36.dp), contentPadding = PaddingValues(0.dp)) { Text("+") }
        }
        FxCode("FlowRow(maxItemsInEachRow = n)")
        FxStage("flex-max-flow") { FlowRowMaxHost(maxItems.intValue, flowRec, counter) }
        FxCode("FlexBox(config = { maxItemsInEachLine(n) })")
        FxStage("flex-max-flex") { FlexBoxMaxHost(maxItems, flexRec, counter) }
        val a = flowRec.snapshot(FxTags.size)
        val b = flexRec.snapshot(FxTags.size)
        if (a != null && b != null) {
            val same = samePlacement(a, b)
            FxNote(
                if (same) "n = ${maxItems.intValue}: ${FxTags.size}칸 모두 같은 자리 · 크기(±1px), 줄 ${linesOf(a).size}" else "⚠ n = ${maxItems.intValue}: 자리가 다르다",
                if (same) FxOk else FxBad
            )
        }
        val s = shown
        if (s == null) {
            FxNote("+ / - 를 누르면 그 한 번에 일어난 리컴포지션 · 측정을 센다.")
        } else {
            FxCode(
                "마지막 한 번(누적 단계 $steps)\n" +
                    "FlowRow 호스트  리컴포지션 ${s.flowCompositions} · 측정 ${s.flowMeasures}\n" +
                    "FlexBox 호스트  리컴포지션 ${s.flexCompositions} · 측정 ${s.flexMeasures}"
            )
            FxNote("FlexBox 쪽은 컴포지션을 건너뛰고 다시 측정만 한다 — config 블록이 측정 단계에서 State 를 읽기 때문이다.")
        }
    }
}

private val FxGrowLabels = listOf("60dp", "grow 1", "grow 2")

@Composable
private fun FlexBoxGrowShrinkCard() {
    val growRec = remember { PlacementRecorder() }
    val weightRec = remember { PlacementRecorder() }
    val shrinkRec = remember { PlacementRecorder() }
    val density = LocalDensity.current
    val dpOf = rememberDpOf()
    val stage = FLEX_DEMO_WIDTH_DP

    FxCard("③ grow · shrink · basis — 식대로 나오는지 폭으로 검산") {
        FxNote("세 칸, 간격 0, 무대 ${stage.toInt()}dp. 기대 폭은 FlexBox 문서의 배분 식(CSS flex 와 같은 식)으로 계산하고 실제 폭은 onPlaced 로 읽는다.")
        Spacer(modifier = Modifier.height(8.dp))

        FxCode("FlexBox: [width 60dp] [flex { basis(60.dp); grow(1f) }] [flex { basis(60.dp); grow(2f) }]")
        FxStage("flex-grow") {
            FlexBox {
                FxChipBox("60dp", Modifier.recordPlacement(growRec, 0).width(60.dp))
                FxChipBox("grow 1", Modifier.recordPlacement(growRec, 1).flex { basis(60.dp); grow(1f) })
                FxChipBox("grow 2", Modifier.recordPlacement(growRec, 2).flex { basis(60.dp); grow(2f) }, FxFill)
            }
        }
        WidthCheck(growRec, growWidths(listOf(60f, 60f, 60f), listOf(0f, 1f, 2f), stage, 0f), density.density, dpOf,
            "basis 60 위에 남는 ${oneDecimalText(stage - 180f)}dp 를 1:2 로 더한다")

        FxCode("FlowRow: [width 60dp] [weight(1f)] [weight(2f)]")
        FxStage("flex-weight") {
            FlowRow {
                FxChipBox("60dp", Modifier.recordPlacement(weightRec, 0).width(60.dp))
                FxChipBox("weight 1", Modifier.recordPlacement(weightRec, 1).weight(1f))
                FxChipBox("weight 2", Modifier.recordPlacement(weightRec, 2).weight(2f), FxFill)
            }
        }
        WidthCheck(weightRec, weightWidths(listOf(60f, null, null), listOf(0f, 1f, 2f), stage, 0f), density.density, dpOf,
            "weight 는 칸의 기본 크기를 보지 않고 고정 칸을 뺀 ${oneDecimalText(stage - 60f)}dp 를 1:2 로 나눈다")

        FxCode("FlexBox: 세 칸 basis(140.dp), shrink 1 · 1 · 2")
        FxStage("flex-shrink") {
            FlexBox {
                FxChipBox("shrink 1", Modifier.recordPlacement(shrinkRec, 0).flex { basis(140.dp) })
                FxChipBox("shrink 1", Modifier.recordPlacement(shrinkRec, 1).flex { basis(140.dp) })
                FxChipBox("shrink 2", Modifier.recordPlacement(shrinkRec, 2).flex { basis(140.dp); shrink(2f) }, FxFill)
            }
        }
        WidthCheck(shrinkRec, shrinkWidths(listOf(140f, 140f, 140f), listOf(1f, 1f, 2f), stage, 0f), density.density, dpOf,
            "넘치는 ${oneDecimalText(420f - stage)}dp 를 shrink × basis(140 · 140 · 280) 비율로 뺀다 — shrink 만의 비율이 아니다")
    }
}

/** 실제 폭(dp) vs 기대 폭(dp) 한 줄 */
@Composable
private fun WidthCheck(recorder: PlacementRecorder, expectedDp: List<Float>, density: Float, dpOf: (Int) -> String, why: String) {
    val boxes = recorder.snapshot(expectedDp.size)
    if (boxes == null) {
        FxNote("배치 읽는 중…")
        return
    }
    val ok = widthsMatch(boxes.map { it.width }, expectedDp.map { it * density })
    FxCode("실제 " + boxes.joinToString(" · ") { dpOf(it.width) } + "  기대 " + expectedDp.joinToString(" · ") { oneDecimalText(it) } + if (ok) "  ✓" else "  ✗")
    FxNote(why, if (ok) FxMuted else FxBad)
    Spacer(modifier = Modifier.height(8.dp))
}

private val FxFillTags = listOf("전체", "할인", "신상품", "무료 배송", "오늘 출발")
private const val FX_FILL_INDEX = 2

@Composable
private fun FlexBoxFillCard() {
    val flowRec = remember { PlacementRecorder() }
    val flexRec = remember { PlacementRecorder() }
    val wrapConfig = remember { FlexBoxConfig { wrap(FlexWrap.Wrap); gap(6.dp) } }
    val dpOf = rememberDpOf()

    FxCard("④ fillMaxWidth() 는 FlexBox 에서 grow 가 된다") {
        FxNote(
            "가운데 칩 '신상품' 에만 Modifier.fillMaxWidth(). FlexBox 는 grow 를 따로 주지 않은 칸의 주축 fill 비율을 grow 로 쓰고, 줄을 나눌 때는 " +
                "basis(Auto = 최대 고유 폭)를 본다(foundation-layout 1.13.0 createFlexItem)."
        )
        Spacer(modifier = Modifier.height(8.dp))
        FxCode("FlowRow(spacedBy(6.dp), spacedBy(6.dp))")
        FxStage("flex-fill-flow") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FxFillTags.forEachIndexed { i, t ->
                    val m = Modifier.recordPlacement(flowRec, i)
                    FxChipBox(t, if (i == FX_FILL_INDEX) m.fillMaxWidth() else m, if (i == FX_FILL_INDEX) FxFill else FxChip)
                }
            }
        }
        FxCode("FlexBox(config = { wrap(FlexWrap.Wrap); gap(6.dp) })")
        FxStage("flex-fill-flex") {
            FlexBox(config = wrapConfig) {
                FxFillTags.forEachIndexed { i, t ->
                    val m = Modifier.recordPlacement(flexRec, i)
                    FxChipBox(t, if (i == FX_FILL_INDEX) m.fillMaxWidth() else m, if (i == FX_FILL_INDEX) FxFill else FxChip)
                }
            }
        }
        listOf("FlowRow" to flowRec, "FlexBox" to flexRec).forEach { (name, rec) ->
            val boxes = rec.snapshot(FxFillTags.size)
            if (boxes != null) {
                val line = linesOf(boxes).first { FX_FILL_INDEX in it }
                FxCode("$name — '신상품' 폭 ${dpOf(boxes[FX_FILL_INDEX].width)}dp · 같은 줄 칸 ${line.size}개 · 줄 ${linesOf(boxes).size}")
            }
        }
        FxNote("FlowRow 에서 무대를 가득 채우던 칸이 FlexBox 에서는 자기 줄의 남는 공간만 채운다 — 그대로 옮기면 줄 구성이 바뀐다.")
    }
}

@Composable
private fun FlexBoxSummaryCard() {
    FxCard("정리 — FlowRow 에서 FlexBox 로") {
        FxNote(
            "• FlowRow 처럼 쓰려면 wrap(FlexWrap.Wrap) — 기본값은 한 줄 · shrink 1 이라 칸이 눌리고 넘친다(①).\n" +
                "• maxItemsInEachRow → maxItemsInEachLine, spacedBy → gap 으로 바꾸면 자리가 같았다(①②).\n" +
                "• config 블록은 측정 단계에서 돈다 — 그 안에서 State 를 읽으면 바뀔 때 리컴포지션 없이 다시 측정만 한다(②).\n" +
                "• weight 와 grow 는 다르다 — grow 는 basis 위에 남는 공간을 더하고, shrink 는 shrink × basis 비율로 줄인다(③).\n" +
                "• fillMaxWidth 는 grow 로 바뀐다 — FlowRow 에서 한 줄을 차지하던 칸이 남는 공간만 채운다(④).",
            FxBody
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FlowRowLayoutPreview() {
    FlowRowLayoutExampleUI(onBackEvent = {})
}
