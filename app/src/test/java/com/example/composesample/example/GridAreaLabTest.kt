package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.layout.grid.AreaChild
import com.example.composesample.presentation.example.component.ui.layout.grid.DashArea
import com.example.composesample.presentation.example.component.ui.layout.grid.GridCell
import com.example.composesample.presentation.example.component.ui.layout.grid.badgeCell
import com.example.composesample.presentation.example.component.ui.layout.grid.coverage
import com.example.composesample.presentation.example.component.ui.layout.grid.equalColumnWidth
import com.example.composesample.presentation.example.component.ui.layout.grid.expectedPlacements
import com.example.composesample.presentation.example.component.ui.layout.grid.hiddenBy
import com.example.composesample.presentation.example.component.ui.layout.grid.measuredCell
import com.example.composesample.presentation.example.component.ui.layout.grid.narrowDashLayout
import com.example.composesample.presentation.example.component.ui.layout.grid.resolve
import com.example.composesample.presentation.example.component.ui.layout.grid.resolveIndex
import com.example.composesample.presentation.example.component.ui.layout.grid.wideDashLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Grid 이름 붙인 영역 카드(#161)의 순수 부분 검증 — 화면은 이 기대 칸을 실측 칸과 나란히 보여 준다.
 * given : 가로형·세로형 대시보드 영역 정의와 선언 순서
 * when : 인덱스 해석 · 영역 점검 · 기대 칸 계산 · 실측 위치 → 칸
 * then : 라이브러리 규칙(1부터 · 0 은 지정 안 함 · 음수는 끝에서부터 · 영역은 자리를 예약하지 않음 · 행 → 열 정렬)과 같은 결과
 */
class GridAreaLabTest {

    private val dashboard = DashArea.entries.filter { it != DashArea.Badge }.map { AreaChild(it.name, it) }

    @Test
    fun `인덱스는 1부터이고 0 은 지정 안 함 음수는 끝에서부터`() {
        assertEquals(1, resolveIndex(1, trackCount = 4))
        assertNull(resolveIndex(0, trackCount = 4))
        assertEquals(4, resolveIndex(-1, trackCount = 4))
        assertEquals(1, resolveIndex(-4, trackCount = 4))
        // 선언 트랙보다 앞을 가리키는 음수는 자동 배치
        assertNull(resolveIndex(-5, trackCount = 4))
        // 선언 트랙보다 큰 양수는 그대로(암시 트랙)
        assertEquals(7, resolveIndex(7, trackCount = 4))
    }

    @Test
    fun `두 레이아웃 모두 영역이 겹치지 않고 빈칸 없이 그리드를 채운다`() {
        listOf(wideDashLayout, narrowDashLayout).forEach { layout ->
            val coverage = layout.coverage()
            assertTrue(layout.name, coverage.overlapped.isEmpty())
            assertTrue(layout.name, coverage.empty.isEmpty())
        }
        assertEquals(GridCell(4, 2, columnSpan = 2), wideDashLayout.resolve(wideDashLayout.areas.getValue(DashArea.Footer)))
        assertEquals(GridCell(5, 1, columnSpan = 2), narrowDashLayout.resolve(narrowDashLayout.areas.getValue(DashArea.Footer)))
    }

    @Test
    fun `없는 영역 id 는 자동 배치로 떨어지고 칸이 모두 찼으면 암시 행으로 간다`() {
        // Given: 네 영역이 3×4 를 모두 채운 뒤 정의 안 된 id
        val children = dashboard + AreaChild("Promo", "Promo")

        // When
        val order = wideDashLayout.expectedPlacements(children)

        // Then
        assertEquals(GridCell(5, 1), order.single { it.first.name == "Promo" }.second)
        assertEquals("Promo", order.last().first.name)
    }

    @Test
    fun `먼저 선언된 자동 배치 자식이 칸을 가져가고 영역 자식이 그 위를 덮는다`() {
        // Given / When
        val order = wideDashLayout.expectedPlacements(listOf(AreaChild("Note", null)) + dashboard)

        // Then: 같은 시작 칸 — 안정 정렬이라 선언 순서대로, 뒤의 Header 가 위
        assertEquals(listOf("Note", "Header"), order.take(2).map { it.first.name })
        assertEquals(GridCell(1, 1), order[0].second)
        assertEquals("Header", hiddenBy(order, 0))

        // 뒤에 선언하면 빈칸(암시 행)으로 간다
        val after = wideDashLayout.expectedPlacements(dashboard + AreaChild("Note", null))
        assertEquals(GridCell(5, 1), after.single { it.first.name == "Note" }.second)
    }

    @Test
    fun `시작 칸이 다르면 선언 순서가 아니라 위치가 그리는 순서를 정한다`() {
        // Given: Badge 를 Body 보다 먼저 선언, Badge 는 Body 오른쪽 아래 칸
        val badge = wideDashLayout.badgeCell()
        val children = listOf(
            AreaChild("Header", DashArea.Header),
            AreaChild("Side", DashArea.Side),
            AreaChild("Badge", DashArea.Badge),
            AreaChild("Body", DashArea.Body),
            AreaChild("Footer", DashArea.Footer)
        )

        // When
        val order = wideDashLayout.expectedPlacements(children, mapOf(DashArea.Badge to badge))

        // Then: Body(2행 2열)가 먼저 놓이고 Badge(3행 3열)가 나중 — Badge 가 위
        assertEquals(GridCell(3, 3), badge)
        val names = order.map { it.first.name }
        assertTrue(names.indexOf("Body") < names.indexOf("Badge"))
        assertNull(hiddenBy(order, names.indexOf("Badge")))
        assertNull(hiddenBy(order, names.indexOf("Body")))
    }

    @Test
    fun `실측 위치를 칸으로 바꾼다`() {
        // 내용 폭 1000px, 3열, 간격 10px → 열 326.67px · 행 100px
        val column = equalColumnWidth(1000f, columns = 3, gap = 10f)
        assertEquals(326.67f, column, 0.01f)
        // Body: 2행 2열부터 2×2
        val body = measuredCell(x = column + 10f, y = 110f, width = column * 2 + 10f, height = 210f, columnWidth = column, rowHeight = 100f, gap = 10f)
        assertEquals(GridCell(2, 2, rowSpan = 2, columnSpan = 2), body)
        // 암시 5행 1열
        assertEquals(GridCell(5, 1), measuredCell(0f, 440f, column, 100f, column, 100f, 10f))
    }
}
