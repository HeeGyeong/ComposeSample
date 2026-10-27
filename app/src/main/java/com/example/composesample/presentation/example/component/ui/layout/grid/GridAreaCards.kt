package com.example.composesample.presentation.example.component.ui.layout.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize

/**
 * Grid 예제의 이름 붙인 영역 카드 두 장(#161) — GridConfigurationScope.area + Modifier.gridItem(areaId).
 * 표의 '기대' 는 GridAreaLab 이 라이브러리 규칙으로 계산한 칸, '실측' 은 onPlaced 로 잰 위치를 칸으로 바꾼 값이다.
 */

private val AreaRowHeight = 44.dp
private val AreaGap = 6.dp

private val areaColors = mapOf(
    "Header" to Color(0xFF4E342E),
    "Side" to Color(0xFF6D4C41),
    "Body" to Color(0xFF8D6E63),
    "Footer" to Color(0xFFA1887F),
    "Badge" to Color(0xFFE53935),
    "Note" to Color(0xFF1E88E5),
    "Promo" to Color(0xFF43A047)
)

// ==================== 이름 붙인 영역 ====================

@Composable
internal fun GridNamedAreaCard() {
    var wide by remember { mutableStateOf(true) }
    val layout = if (wide) wideDashLayout else narrowDashLayout
    val children = DashArea.entries.filter { it != DashArea.Badge }.map { AreaChild(it.name, it) }

    ExampleCard(title = "이름 붙인 영역 — 같은 자식, config 만 바꾸기", titleColor = Color(0xFF5D4037)) {
        Text(
            text = "config 에서 area(id, row, column, rowSpan, columnSpan) 로 칸에 이름을 붙이고, 자식은 gridItem(id) 로 이름만 " +
                    "가리킵니다. 아래 네 자식의 코드는 그대로 두고 config 만 바꿔 가로형·세로형을 오갑니다. Footer 는 row = -1(마지막 행)입니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        ToggleRow(
            label = "${layout.name} — ${layout.columns}열 × ${layout.rows}행",
            checked = wide,
            onCheckedChange = { wide = it }
        )
        Spacer(modifier = Modifier.height(10.dp))
        AreaGrid(layout = layout, children = children)
        Spacer(modifier = Modifier.height(6.dp))
        val coverage = layout.coverage()
        Text(
            text = "영역 정의 점검(GridAreaLab.coverage): 겹친 칸 ${coverage.overlapped.size} · 빈칸 ${coverage.empty.size} — " +
                    "겹침도 빈칸도 오류가 아니라서 정의를 바꿀 때 이런 점검을 따로 둬야 합니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            code = """
                enum class DashArea { Header, Side, Body, Footer }

                Grid(config = {
                    repeat(3) { column(1.fr) }; repeat(4) { row(44.dp) }
                    area(Header, row = 1, column = 1, columnSpan = 3)
                    area(Side, rows = 2..4, columns = 1..1)
                    area(Body, row = 2, column = 2, rowSpan = 2, columnSpan = 2)
                    area(Footer, row = -1, column = 2, columnSpan = 2)  // 마지막 행
                }) {
                    Header(Modifier.gridItem(DashArea.Header))
                    Side(Modifier.gridItem(DashArea.Side))   // 좌표를 모른다
                    Body(Modifier.gridItem(DashArea.Body))
                    Footer(Modifier.gridItem(DashArea.Footer))
                }
            """.trimIndent(),
            borderColor = Color(0xFF5D4037)
        )
    }
}

// ==================== 이름 붙인 영역의 함정 ====================

@Composable
internal fun GridAreaTrapCard() {
    var unknownArea by remember { mutableStateOf(false) }
    var autoFirst by remember { mutableStateOf(false) }
    var badge by remember { mutableStateOf(false) }
    val layout = wideDashLayout
    val children = buildList {
        if (autoFirst) add(AreaChild("Note", null))
        add(AreaChild("Header", DashArea.Header))
        add(AreaChild("Side", DashArea.Side))
        // Badge 를 Body 보다 먼저 선언한다 — Box 라면 Body 가 위에 그려진다
        if (badge) add(AreaChild("Badge", DashArea.Badge))
        add(AreaChild("Body", DashArea.Body))
        add(AreaChild("Footer", DashArea.Footer))
        // config 에 없는 id
        if (unknownArea) add(AreaChild("Promo", "Promo"))
    }
    val extraAreas: Map<Any, GridCell> = if (badge) mapOf(DashArea.Badge to layout.badgeCell()) else emptyMap()

    ExampleCard(title = "이름 붙인 영역의 함정", titleColor = Color(0xFFBF360C)) {
        Text(
            text = "가로형 대시보드에 함정을 하나씩 켭니다. 셋 다 오류도 경고도 없이 레이아웃만 달라집니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        ToggleRow(label = "① 없는 영역 id — gridItem(\"Promo\")", checked = unknownArea, onCheckedChange = { unknownArea = it })
        Spacer(modifier = Modifier.height(6.dp))
        ToggleRow(label = "② 자동 배치 자식(Note)을 영역 자식보다 먼저 선언", checked = autoFirst, onCheckedChange = { autoFirst = it })
        Spacer(modifier = Modifier.height(6.dp))
        ToggleRow(label = "③ Body 안에 Badge 영역 — Badge 를 Body 보다 먼저 선언", checked = badge, onCheckedChange = { badge = it })
        Spacer(modifier = Modifier.height(10.dp))
        AreaGrid(layout = layout, children = children, extraAreas = extraAreas)
        Spacer(modifier = Modifier.height(8.dp))
        TrapNote(
            on = unknownArea,
            text = "① 없는 id 는 조용히 자동 배치로 떨어집니다(문서: 'silently fall back'). 칸이 모두 영역이라 선언한 4행 밖 " +
                    "암시 행(Auto 크기)이 새로 생겨 그리드가 길어집니다."
        )
        TrapNote(
            on = autoFirst,
            text = "② 영역은 자리를 예약하지 않습니다. 먼저 선언된 Note 가 비어 있던 1행 1열을 가져가고, 뒤에 온 Header 는 " +
                    "명시 배치라 그 위에 그대로 포개져 Note 를 덮습니다. Note 를 뒤에 선언하면 빈칸(암시 행)으로 갑니다."
        )
        TrapNote(
            on = badge,
            text = "③ 선언은 Badge 가 먼저인데 Badge 가 위에 보입니다. Grid 는 칸을 정한 뒤 자식을 행 → 열 순서로 정렬해서 " +
                    "놓기 때문에(1.12.1 소스), 시작 칸이 다르면 선언 순서가 아니라 위치가 그리는 순서를 정합니다. " +
                    "'나중에 선언한 것이 위' 는 시작 칸이 같을 때만입니다."
        )
        Spacer(modifier = Modifier.height(12.dp))
        OneDimensionalAreaDemo()
    }
}

@Composable
private fun TrapNote(on: Boolean, text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = if (on) Color(0xFFBF360C) else Color(0xFF9E9E9E),
        lineHeight = 17.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

/** 1D 영역 — 열을 비워 둔 area 에 열보다 많은 자식을 넣으면 다음 행이 아니라 암시 열로 늘어난다 */
@OptIn(ExperimentalGridApi::class)
@Composable
private fun OneDimensionalAreaDemo() {
    var chips by remember { mutableIntStateOf(3) }
    val widths = remember { mutableStateMapOf<Int, Float>() }
    val density = LocalDensity.current

    Text(text = "1차원 영역 — area(\"Tags\", row = 1)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBF360C))
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "열을 비워 두면(GridIndexUnspecified) 1행 안에서 빈 열을 차례로 찾습니다. 3열 그리드에 칩을 5개 넣으면 " +
                "다음 행으로 줄바꿈하지 않고 1행에 암시 열(Auto)을 늘립니다.",
        fontSize = 12.sp,
        color = Color(0xFF616161),
        lineHeight = 17.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    ToggleRow(label = "칩 ${chips}개", checked = chips == 5, onCheckedChange = { chips = if (it) 5 else 3 })
    Spacer(modifier = Modifier.height(6.dp))
    Grid(
        config = {
            repeat(3) { column(1.fr) }
            row(36.dp)
            row(36.dp)
            gap(AreaGap)
            area("Tags", row = 1)
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFCCBC), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        repeat(chips) { index ->
            key(index) {
                AreaCell(
                    label = "#${index + 1}",
                    color = Color(0xFFFF7043),
                    modifier = Modifier
                        .gridItem("Tags")
                        .fillMaxWidth()
                        .height(36.dp)
                        .onPlaced { coordinates -> widths[index] = coordinates.size.width.toFloat() }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    val measured = (0 until chips).joinToString(" · ") { index ->
        widths[index]?.let { "#${index + 1} ${with(density) { it.toDp().value.toInt() }}dp" } ?: "#${index + 1} -"
    }
    Text(text = "실측 폭: $measured — 2행은 비어 있다", fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
}

// ==================== 공용 ====================

/** 영역 Grid + 자식마다 기대 칸·실측 칸 표 */
@OptIn(ExperimentalGridApi::class)
@Composable
private fun AreaGrid(layout: DashLayout, children: List<AreaChild>, extraAreas: Map<Any, GridCell> = emptyMap()) {
    val density = LocalDensity.current
    val placed = remember { mutableStateMapOf<String, Rect>() }
    var contentWidth by remember { mutableFloatStateOf(0f) }

    Grid(
        config = {
            repeat(layout.columns) { column(1.fr) }
            repeat(layout.rows) { row(AreaRowHeight) }
            gap(AreaGap)
            (layout.areas + extraAreas).forEach { (id, cell) ->
                area(id, row = cell.row, column = cell.column, rowSpan = cell.rowSpan, columnSpan = cell.columnSpan)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD7CCC8), RoundedCornerShape(8.dp))
            .padding(6.dp)
            .onSizeChanged { contentWidth = it.width.toFloat() }
    ) {
        children.forEach { child ->
            key(child.name) {
                val place = if (child.area != null) Modifier.gridItem(child.area) else Modifier
                // 정의된 영역 자식은 영역을 채우고, 자동 배치로 떨어지는 자식은 한 칸 높이(암시 행도 같은 높이가 되게)
                val size = if (child.area != null && child.area in layout.areas.keys + extraAreas.keys) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.fillMaxWidth().height(AreaRowHeight)
                }
                AreaCell(
                    label = child.name,
                    color = areaColors[child.name] ?: Color.Gray,
                    modifier = place
                        .then(size)
                        .onPlaced { coordinates -> placed[child.name] = Rect(coordinates.positionInParent(), coordinates.size.toSize()) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    val gapPx = with(density) { AreaGap.toPx() }
    val rowPx = with(density) { AreaRowHeight.toPx() }
    val columnPx = equalColumnWidth(contentWidth, layout.columns, gapPx)
    val order = layout.expectedPlacements(children, extraAreas)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = "놓는 순서(행 → 열) · 기대 칸 · 실측 칸", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
        order.forEachIndexed { index, (child, expected) ->
            val measured = placed[child.name]?.let { measuredCell(it.left, it.top, it.width, it.height, columnPx, rowPx, gapPx) }
            val mark = when (measured) {
                null -> "-"
                expected -> "✓"
                else -> "✗"
            }
            val hidden = hiddenBy(order, index)?.let { " · $it 아래에 가려짐" }.orEmpty()
            Text(
                text = "${index + 1}. ${child.name}  기대 ${expected.label} · 실측 ${measured?.label ?: "-"} $mark$hidden",
                fontSize = 11.sp,
                color = if (mark == "✗") Color(0xFFC62828) else Color(0xFF424242),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun AreaCell(label: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier.background(color, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
