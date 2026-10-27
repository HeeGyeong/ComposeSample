package com.example.composesample.presentation.example.component.ui.layout.grid

import kotlin.math.roundToInt

/**
 * Grid 예제 이름 붙인 영역 카드(#161)의 순수 부분 — 영역 정의, 라이브러리와 같은 규칙으로 계산한 '기대 칸',
 * 화면에서 잰 위치를 칸으로 바꾸는 계산. 화면과 단위 테스트가 함께 쓴다.
 *
 * 기대 칸 계산은 foundation-layout 1.12.1 Grid.kt 의 resolveGridItemIndices 를 이 예제가 쓰는 경우
 * (2차원 영역 + 1×1 자동 배치 · GridFlow.Row)로 좁혀 옮긴 것이다. 화면은 이 값과 실측 값을 나란히 보여 준다.
 */

/** 영역 id — enum 이라 equals/hashCode 가 안정적이다(문서 요구) */
enum class DashArea { Header, Side, Body, Footer, Badge }

/** 1부터 세는 칸과 span */
data class GridCell(val row: Int, val column: Int, val rowSpan: Int = 1, val columnSpan: Int = 1) {
    val label: String
        get() = "${row}행 ${column}열" + if (rowSpan > 1 || columnSpan > 1) " (${rowSpan}×${columnSpan})" else ""
}

/** 레이아웃 하나 — 같은 자식에게 config(트랙 수와 영역)만 바꿔 준다. 영역 칸은 음수(끝에서부터)를 그대로 둔다 */
data class DashLayout(val name: String, val columns: Int, val rows: Int, val areas: Map<DashArea, GridCell>)

/** 가로형 — 사이드바가 왼쪽 세로로, Footer 는 row = -1(마지막 행) */
val wideDashLayout = DashLayout(
    name = "가로형",
    columns = 3,
    rows = 4,
    areas = mapOf(
        DashArea.Header to GridCell(1, 1, columnSpan = 3),
        DashArea.Side to GridCell(2, 1, rowSpan = 3),
        DashArea.Body to GridCell(2, 2, rowSpan = 2, columnSpan = 2),
        DashArea.Footer to GridCell(-1, 2, columnSpan = 2),
    )
)

/** 세로형 — 같은 네 영역을 2열로 쌓는다 */
val narrowDashLayout = DashLayout(
    name = "세로형",
    columns = 2,
    rows = 5,
    areas = mapOf(
        DashArea.Header to GridCell(1, 1, columnSpan = 2),
        DashArea.Body to GridCell(2, 1, rowSpan = 2, columnSpan = 2),
        DashArea.Side to GridCell(4, 1, columnSpan = 2),
        DashArea.Footer to GridCell(-1, 1, columnSpan = 2),
    )
)

/** 겹치는 영역 함정용 — Body 의 오른쪽 아래 한 칸 */
fun DashLayout.badgeCell(): GridCell = resolve(areas.getValue(DashArea.Body)).let {
    GridCell(it.row + it.rowSpan - 1, it.column + it.columnSpan - 1)
}

/**
 * 라이브러리(resolveToZeroBasedIndex)와 같은 해석을 1부터 세는 값으로 — 양수는 그대로, 0 은 지정 안 함(null),
 * 음수는 끝에서부터(-1 = 마지막 선언 트랙). 선언 트랙보다 앞을 가리키는 음수도 null(자동 배치)이다.
 */
fun resolveIndex(index: Int, trackCount: Int): Int? = when {
    index > 0 -> index
    index == 0 -> null
    else -> (trackCount + index + 1).takeIf { it >= 1 }
}

/** 영역 칸의 음수를 풀어 1부터로 */
fun DashLayout.resolve(cell: GridCell): GridCell = cell.copy(
    row = resolveIndex(cell.row, rows) ?: 0,
    column = resolveIndex(cell.column, columns) ?: 0
)

/** 영역 정의 점검 — 겹침도 빈칸도 오류가 아니라서 정의를 바꿀 때 직접 봐야 한다 */
data class Coverage(val overlapped: List<GridCell>, val empty: List<GridCell>)

fun DashLayout.coverage(): Coverage {
    val count = mutableMapOf<Pair<Int, Int>, Int>()
    areas.values.map { resolve(it) }.forEach { c ->
        for (r in c.row until c.row + c.rowSpan) for (col in c.column until c.column + c.columnSpan) {
            count[r to col] = (count[r to col] ?: 0) + 1
        }
    }
    val all = (1..rows).flatMap { r -> (1..columns).map { c -> r to c } }
    return Coverage(
        overlapped = all.filter { (count[it] ?: 0) > 1 }.map { GridCell(it.first, it.second) },
        empty = all.filter { (count[it] ?: 0) == 0 }.map { GridCell(it.first, it.second) }
    )
}

/** 선언 순서대로 놓을 자식 — area 가 null 이면 gridItem 없이 자동 배치 */
data class AreaChild(val name: String, val area: Any?)

/**
 * 기대 칸 — 선언 순서대로 돌며 정의된 영역은 그 칸에(이미 차 있어도 그대로 — 겹침 허용),
 * 그 밖(자동 배치 · 정의 안 된 영역 id)은 커서부터 행 우선으로 첫 빈칸에. 열을 넘으면 다음 행, 선언 행을 넘으면 암시 행.
 * 반환은 놓는 순서(행 → 열, 같으면 선언 순서) — Grid 는 이 순서로 place 하므로 뒤에 놓인 것이 위에 그려진다.
 */
fun DashLayout.expectedPlacements(children: List<AreaChild>, extraAreas: Map<Any, GridCell> = emptyMap()): List<Pair<AreaChild, GridCell>> {
    val defined: Map<Any, GridCell> = areas.mapKeys { it.key as Any } + extraAreas
    val occupied = mutableSetOf<Pair<Int, Int>>()
    var cursorRow = 1
    var cursorCol = 1
    val placed = children.map { child ->
        val area = child.area?.let { defined[it] }
        val cell = if (area != null) {
            resolve(area)
        } else {
            var r = cursorRow
            var c = cursorCol
            while (true) {
                if (c > columns) {
                    c = 1
                    r++
                    continue
                }
                if ((r to c) !in occupied) break
                c++
            }
            cursorRow = r
            cursorCol = c + 1
            GridCell(r, c)
        }
        for (r in cell.row until cell.row + cell.rowSpan) for (c in cell.column until cell.column + cell.columnSpan) occupied += r to c
        child to cell
    }
    return placed.sortedWith(compareBy({ it.second.row }, { it.second.column }))
}

/** 다른 자식이 이 칸을 모두 덮고, 그 자식이 나중에 놓이면(위에 그려지면) 가려진다 */
fun hiddenBy(order: List<Pair<AreaChild, GridCell>>, index: Int): String? {
    val (_, cell) = order[index]
    return order.drop(index + 1).firstOrNull { (_, other) ->
        other.row <= cell.row && other.column <= cell.column &&
            other.row + other.rowSpan >= cell.row + cell.rowSpan &&
            other.column + other.columnSpan >= cell.column + cell.columnSpan
    }?.first?.name
}

/**
 * 화면에서 잰 위치(px)를 칸으로 — 열은 모두 같은 fr, 행은 모두 같은 고정 높이일 때(암시 행도 같은 높이의 자식으로 채운다).
 * 위치는 Grid 안쪽(패딩 뒤) 기준이다.
 */
fun measuredCell(x: Float, y: Float, width: Float, height: Float, columnWidth: Float, rowHeight: Float, gap: Float): GridCell =
    GridCell(
        row = (y / (rowHeight + gap)).roundToInt() + 1,
        column = (x / (columnWidth + gap)).roundToInt() + 1,
        rowSpan = ((height + gap) / (rowHeight + gap)).roundToInt(),
        columnSpan = ((width + gap) / (columnWidth + gap)).roundToInt()
    )

/** 열 n 개가 같은 fr 일 때 한 열의 폭 */
fun equalColumnWidth(contentWidth: Float, columns: Int, gap: Float): Float = (contentWidth - gap * (columns - 1)) / columns
