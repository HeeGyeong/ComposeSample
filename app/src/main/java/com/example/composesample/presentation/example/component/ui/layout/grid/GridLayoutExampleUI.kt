package com.example.composesample.presentation.example.component.ui.layout.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.columns
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * Compose Grid API Example
 *
 * Compose 1.11 의 실험 API 인 Grid(non-lazy 2D 트랙 레이아웃)를 시연한다.
 * 이름 붙인 영역(area() / gridItem(area))은 Compose 1.12+ 신규라 이 예제 범위 밖이다.
 */
@Composable
fun GridLayoutExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "Compose Grid API (2D 레이아웃)", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GridConceptCard() }
            item { GridTrackSizeCard() }
            item { GridFlexAndGapCard() }
            item { GridPlacementCard() }
            item { GridFlowCard() }
            item { GridVsLazyGridCard() }
            item { GridSummaryCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun GridConceptCard() {
    ExampleCard(title = "Grid API 개요", titleColor = Color(0xFF1976D2)) {
        Text(
            text = "CSS Grid 에서 착안한 2차원 레이아웃입니다. 열(column)과 행(row) 트랙을 먼저 선언하고 " +
                    "자식을 그 격자에 배치합니다. 스크롤 컨테이너가 아니라 일반 레이아웃이라 자식이 전부 컴포즈됩니다.",
            fontSize = 14.sp,
            color = Color(0xFF424242),
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 프로젝트 내 다른 레이아웃 예제와 축이 어떻게 다른지 정리
        val comparisons = listOf(
            "Grid" to "트랙 기반 2D 배치, non-lazy",
            "LazyStaggeredGrid" to "스크롤 격자, 화면 밖은 미컴포즈",
            "FlowRow" to "1차원 흐름 + 자동 줄바꿈",
            "CustomLayout" to "MeasurePolicy 를 직접 구현"
        )
        comparisons.forEach { (api, desc) ->
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
                    modifier = Modifier.width(140.dp)
                )
                Text(text = desc, fontSize = 12.sp, color = Color(0xFF424242))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock(
            code = """
                @OptIn(ExperimentalGridApi::class)
                Grid(
                    config = {
                        column(72.dp)      // 고정
                        column(1.fr)       // 남은 공간 분배
                        row(GridTrackSize.Auto)
                        gap(8.dp)
                    }
                ) {
                    Box(Modifier.gridItem(row = 0, column = 0))
                }
            """.trimIndent(),
            borderColor = Color(0xFF1976D2)
        )
    }
}

// ==================== 2. 트랙 크기 6종 ====================

@OptIn(ExperimentalGridApi::class)
@Composable
private fun GridTrackSizeCard() {
    // 컨테이너 폭을 줄여 가며 각 트랙이 어떻게 반응하는지 관측하기 위한 비율
    var widthFraction by remember { mutableFloatStateOf(1f) }

    ExampleCard(title = "트랙 크기 6종 + minmax", titleColor = Color(0xFF388E3C)) {
        Text(
            text = "Grid 는 ① 고정 크기(Fixed) → ② 콘텐츠 기반(Auto/MinContent/MaxContent)·비율(Percentage) 순으로 " +
                    "먼저 자리를 떼어 주고, ③ 그러고도 남은 공간만 Flex(fr) 트랙이 나눠 갖습니다. " +
                    "아래 슬라이더로 컨테이너 폭을 줄이면 fr 트랙부터 먼저 줄어드는 것을 볼 수 있습니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledSlider(
            label = "컨테이너 폭",
            value = "${(widthFraction * 100).toInt()}%",
            sliderValue = widthFraction,
            onValueChange = { widthFraction = it },
            valueRange = 0.4f..1f
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Fixed / Percentage / Flex / Auto",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Grid(
            config = {
                column(GridTrackSize.Fixed(64.dp))      // 항상 64dp
                column(GridTrackSize.Percentage(0.25f)) // 컨테이너 폭의 25%
                column(GridTrackSize.Flex(1.fr))        // 남은 공간 분배
                column(GridTrackSize.Auto)              // 콘텐츠 크기에 맞춤
                row(GridTrackSize.Auto)
                gap(6.dp)
            },
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            TrackCell(label = "64.dp", color = Color(0xFF43A047))
            TrackCell(label = "25%", color = Color(0xFF1E88E5))
            TrackCell(label = "1.fr", color = Color(0xFFF4511E))
            TrackCell(label = "Auto", color = Color(0xFF8E24AA))
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "MinContent / MaxContent / minmax",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "세 트랙에 같은 문장을 넣었습니다. MinContent 는 줄바꿈을 최대한 해서 '가장 긴 단어' 폭까지 줄이고, " +
                    "MaxContent 는 줄바꿈 없이 한 줄로 펼친 폭을 요구합니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Grid(
            config = {
                column(GridTrackSize.MinContent)
                column(GridTrackSize.MaxContent)
                column(minmax(40.dp, 1.fr)) // 최소 40dp 보장 + 남으면 fr 로 확장
                row(GridTrackSize.Auto)
                gap(6.dp)
            },
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            ContentCell(text = "Compose Grid Track", color = Color(0xFF00897B))
            ContentCell(text = "Compose Grid Track", color = Color(0xFF5E35B1))
            ContentCell(text = "Compose Grid Track", color = Color(0xFFD81B60))
        }
    }
}

// ==================== 3. fr 비율 & gap ====================

@OptIn(ExperimentalGridApi::class)
@Composable
private fun GridFlexAndGapCard() {
    var firstWeight by remember { mutableIntStateOf(1) }
    var secondWeight by remember { mutableIntStateOf(2) }
    var columnGapDp by remember { mutableIntStateOf(8) }
    var rowGapDp by remember { mutableIntStateOf(8) }

    ExampleCard(title = "fr 비율과 gap", titleColor = Color(0xFFF57C00)) {
        Text(
            text = "fr 은 Row 의 weight 와 같은 역할이지만 '트랙'에 붙는다는 점이 다릅니다. " +
                    "gap 은 남은 공간을 계산하기 전에 먼저 빠지므로, 간격을 키우면 fr 트랙이 그만큼 줄어듭니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LabeledSlider(
            label = "1번 트랙",
            value = "${firstWeight}fr",
            sliderValue = firstWeight.toFloat(),
            onValueChange = { firstWeight = it.toInt() },
            valueRange = 1f..4f,
            steps = 2
        )
        LabeledSlider(
            label = "2번 트랙",
            value = "${secondWeight}fr",
            sliderValue = secondWeight.toFloat(),
            onValueChange = { secondWeight = it.toInt() },
            valueRange = 1f..4f,
            steps = 2
        )
        LabeledSlider(
            label = "columnGap",
            value = "${columnGapDp}dp",
            sliderValue = columnGapDp.toFloat(),
            onValueChange = { columnGapDp = it.toInt() },
            valueRange = 0f..32f
        )
        LabeledSlider(
            label = "rowGap",
            value = "${rowGapDp}dp",
            sliderValue = rowGapDp.toFloat(),
            onValueChange = { rowGapDp = it.toInt() },
            valueRange = 0f..32f
        )

        Spacer(modifier = Modifier.height(10.dp))
        Grid(
            config = {
                column(firstWeight.fr)
                column(secondWeight.fr)
                column(1.fr)
                row(GridTrackSize.Auto)
                row(GridTrackSize.Auto)
                columnGap(columnGapDp.dp)
                rowGap(rowGapDp.dp)
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFE0B2), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            TrackCell(label = "${firstWeight}fr", color = Color(0xFFEF6C00))
            TrackCell(label = "${secondWeight}fr", color = Color(0xFFFB8C00))
            TrackCell(label = "1fr", color = Color(0xFFFFA726))
            TrackCell(label = "${firstWeight}fr", color = Color(0xFFEF6C00))
            TrackCell(label = "${secondWeight}fr", color = Color(0xFFFB8C00))
            TrackCell(label = "1fr", color = Color(0xFFFFA726))
        }
    }
}

// ==================== 4. 배치(gridItem) ====================

@OptIn(ExperimentalGridApi::class)
@Composable
private fun GridPlacementCard() {
    var overlap by remember { mutableStateOf(false) }

    ExampleCard(title = "자동 배치 vs 명시 배치(gridItem)", titleColor = Color(0xFF7B1FA2)) {
        Text(
            text = "Modifier 없이 두면 배치 커서가 순서대로 채웁니다. Modifier.gridItem 으로 좌표와 span 을 직접 " +
                    "지정하면 대시보드처럼 특정 셀을 넓게 쓸 수 있습니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Grid(
            config = {
                // 복수형 columns(vararg) 로 트랙을 한 번에 선언할 수도 있다
                columns(
                    GridTrackSize.Flex(1.fr),
                    GridTrackSize.Flex(1.fr),
                    GridTrackSize.Flex(1.fr)
                )
                row(44.dp)
                row(56.dp)
                row(56.dp)
                gap(8.dp)
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE1BEE7), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            // 헤더: 0행을 3칸 모두 차지
            PlacementCell(
                label = "Header (0행, 3칸)",
                color = Color(0xFF6A1B9A),
                modifier = Modifier.gridItem(row = 0, column = 0, columnSpan = 3)
            )
            // 사이드바: IntRange 오버로드 — gridItem(rows = 1..2, columns = 0..0)
            PlacementCell(
                label = "Side\n(1..2행)",
                color = Color(0xFF8E24AA),
                modifier = Modifier.gridItem(rows = 1..2, columns = 0..0)
            )
            // 본문 A: 1행의 1~2열
            PlacementCell(
                label = "A (1행, 2칸)",
                color = Color(0xFFAB47BC),
                modifier = Modifier.gridItem(row = 1, column = 1, columnSpan = 2)
            )
            // 본문 B: 겹침 토글에 따라 헤더 자리로 이동
            PlacementCell(
                label = if (overlap) "B (겹침!)" else "B",
                color = if (overlap) Color(0xFFE53935) else Color(0xFFBA68C8),
                modifier = if (overlap) {
                    Modifier.gridItem(row = 0, column = 0, columnSpan = 2)
                } else {
                    Modifier.gridItem(row = 2, column = 1)
                }
            )
            PlacementCell(
                label = "C",
                color = Color(0xFFCE93D8),
                modifier = Modifier.gridItem(row = 2, column = 2)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        ToggleRow(
            label = "B 를 헤더 좌표로 이동(겹침 재현)",
            checked = overlap,
            onCheckedChange = { overlap = it }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "함정: 명시 배치는 충돌을 막아 주지 않습니다. 같은 좌표를 두 아이템이 요구하면 오류 없이 그대로 " +
                    "포개져 나중에 배치된 쪽이 위에 그려집니다. 좌표를 직접 관리하는 순간 겹침 검증은 개발자 몫이 됩니다.",
            fontSize = 12.sp,
            color = if (overlap) Color(0xFFC62828) else Color(0xFF616161),
            lineHeight = 17.sp
        )
    }
}

// ==================== 5. GridFlow ====================

@OptIn(ExperimentalGridApi::class)
@Composable
private fun GridFlowCard() {
    var rowFlow by remember { mutableStateOf(true) }

    ExampleCard(title = "GridFlow — 자동 배치 방향", titleColor = Color(0xFF00838F)) {
        Text(
            text = "좌표를 지정하지 않은 아이템을 채우는 커서의 진행 방향입니다. Row 는 한 행을 다 채우고 다음 행으로, " +
                    "Column 은 한 열을 다 채우고 다음 열로 넘어갑니다. 아래 숫자 순서를 비교해 보세요.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        ToggleRow(
            label = if (rowFlow) "GridFlow.Row (행 우선)" else "GridFlow.Column (열 우선)",
            checked = rowFlow,
            onCheckedChange = { rowFlow = it }
        )
        Spacer(modifier = Modifier.height(10.dp))

        Grid(
            config = {
                column(1.fr)
                column(1.fr)
                column(1.fr)
                row(40.dp)
                row(40.dp)
                row(40.dp)
                flow = if (rowFlow) GridFlow.Row else GridFlow.Column
                gap(6.dp)
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFB2EBF2), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            repeat(9) { index ->
                PlacementCell(
                    label = "${index + 1}",
                    color = Color(0xFF00838F).copy(alpha = 0.45f + index * 0.06f)
                )
            }
        }
    }
}

// ==================== 6. Grid 는 lazy 가 아니다 ====================

private const val CELL_COUNT = 60

@OptIn(ExperimentalGridApi::class)
@Composable
private fun GridVsLazyGridCard() {
    // 살아있는 자식 컴포지션 수 — DisposableEffect 진입 시 +1, 폐기 시 -1
    var gridAlive by remember { mutableIntStateOf(0) }
    var lazyAlive by remember { mutableIntStateOf(0) }

    ExampleCard(title = "Grid 는 lazy 가 아니다 (실측)", titleColor = Color(0xFFC62828)) {
        Text(
            text = "같은 ${CELL_COUNT}개 셀을 Grid 와 LazyVerticalGrid 에 각각 넣고, 살아있는 자식 컴포지션 수를 " +
                    "셉니다. 두 컨테이너 모두 높이 160dp 로 스크롤되지만 결과는 다릅니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Grid (verticalScroll) — 살아있는 셀 $gridAlive / $CELL_COUNT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC62828)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                .verticalScroll(rememberScrollState())
                .padding(6.dp)
        ) {
            Grid(
                config = {
                    repeat(6) { column(1.fr) }
                    gap(4.dp)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(CELL_COUNT) { index ->
                    CountedCell(
                        index = index,
                        color = Color(0xFFEF5350),
                        onAliveChange = { gridAlive += it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "LazyVerticalGrid — 살아있는 셀 $lazyAlive / $CELL_COUNT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(CELL_COUNT) { index ->
                CountedCell(
                    index = index,
                    color = Color(0xFF66BB6A),
                    onAliveChange = { lazyAlive += it }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Grid 쪽은 스크롤과 무관하게 ${CELL_COUNT}개가 계속 살아 있고, LazyVerticalGrid 는 화면에 보이는 " +
                    "만큼만 유지하다 스크롤에 따라 증감합니다. 판별 기준은 단순합니다 — 셀 개수가 데이터 양에 비례해 " +
                    "늘어난다면 Grid 가 아니라 Lazy 계열을 써야 합니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
    }
}

// ==================== 7. 정리 ====================

@Composable
private fun GridSummaryCard() {
    ExampleCard(title = "정리", titleColor = Color(0xFF37474F)) {
        val points = listOf(
            "선언 순서" to "트랙을 먼저 선언(column/row)하고 자식을 배치한다",
            "크기 해석" to "Fixed·콘텐츠·Percentage 가 먼저, 남은 공간을 fr 이 분배",
            "gap" to "남은 공간 계산 전에 먼저 차감된다",
            "배치" to "gridItem(row, column, span) 또는 IntRange 오버로드",
            "겹침" to "좌표 충돌은 오류가 아니라 포개짐 — 검증은 개발자 몫",
            "non-lazy" to "모든 자식이 컴포즈되므로 대량 데이터에는 부적합"
        )
        points.forEach { (title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F),
                    modifier = Modifier.width(72.dp)
                )
                Text(text = desc, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "이 예제의 범위 밖: 이름 붙인 영역(area() / gridItem(area))은 Compose 1.12+ 에서 추가된 API라 " +
                    "현재 버전(1.11.1)에는 없습니다. 지금은 좌표와 span 으로만 배치합니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            code = """
                // 대시보드 배치 — 좌표 + span
                Grid(config = {
                    columns(Flex(1.fr), Flex(1.fr), Flex(1.fr))
                    row(44.dp); row(56.dp); row(56.dp)
                    gap(8.dp)
                }) {
                    Header(Modifier.gridItem(row = 0, column = 0, columnSpan = 3))
                    Side(Modifier.gridItem(rows = 1..2, columns = 0..0))
                    Body(Modifier.gridItem(row = 1, column = 1, columnSpan = 2))
                }
            """.trimIndent(),
            borderColor = Color(0xFF37474F)
        )
    }
}

// ==================== 공용 컴포넌트 ====================

@Composable
private fun ExampleCard(
    title: String,
    titleColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

/**
 * 트랙 크기를 눈으로 재기 위한 셀. 트랙 폭에 그대로 채워진다.
 */
@Composable
private fun TrackCell(label: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(color, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

/**
 * MinContent/MaxContent 비교용 셀 — 트랙이 콘텐츠 크기를 어떻게 요구하는지 보이기 위해
 * 고정 폭 없이 텍스트만 담는다.
 */
@Composable
private fun ContentCell(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(6.dp))
            .padding(6.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = Color.White, lineHeight = 15.sp)
    }
}

@Composable
private fun PlacementCell(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            lineHeight = 14.sp
        )
    }
}

/**
 * 컴포지션이 살아있는 동안만 카운트되는 셀.
 * DisposableEffect 진입에서 +1, onDispose 에서 -1 하므로 화면 밖으로 나가 폐기되면 즉시 줄어든다.
 */
@Composable
private fun CountedCell(index: Int, color: Color, onAliveChange: (Int) -> Unit) {
    DisposableEffect(Unit) {
        onAliveChange(1)
        onDispose { onAliveChange(-1) }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(color, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "${index + 1}", fontSize = 9.sp, color = Color.White)
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: String,
    sliderValue: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF424242),
            modifier = Modifier.width(84.dp)
        )
        Slider(
            value = sliderValue,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            modifier = Modifier.width(48.dp)
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFE0E0E0),
            lineHeight = 16.sp
        )
    }
}
