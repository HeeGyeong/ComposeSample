package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.layout.adaptive.BackMode
import com.example.composesample.presentation.example.component.ui.layout.adaptive.DirectiveMode
import com.example.composesample.presentation.example.component.ui.layout.adaptive.articleById
import com.example.composesample.presentation.example.component.ui.layout.adaptive.paneArticles
import com.example.composesample.presentation.example.component.ui.layout.adaptive.shortPaneValue
import com.example.composesample.presentation.example.component.ui.layout.adaptive.widthClassName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 적응형 다중 페인 예제의 순수 부분 검증.
 * given : 표본 글·지시 모드·뒤로가기 모드·PaneAdaptedValue 문자열
 * when : 조회·표시 이름 변환
 * then : 화면이 쓰는 값
 */
class AdaptivePaneLabTest {

    @Test
    fun `표본 글은 id 가 겹치지 않고 id 로 찾는다`() {
        assertEquals(paneArticles.size, paneArticles.map { it.id }.toSet().size)
        assertEquals("글 2 — 내비게이션", articleById(2)?.title)
        assertNull(articleById(null))
        assertNull(articleById(99))
    }

    @Test
    fun `지시 모드는 창 크기대로일 때만 분할 수를 정하지 않는다`() {
        assertNull(DirectiveMode.WINDOW.partitions)
        assertEquals(2, DirectiveMode.FORCE_TWO.partitions)
        assertEquals(1, DirectiveMode.FORCE_ONE.partitions)
    }

    @Test
    fun `뒤로가기 모드 이름은 라이브러리 이름과 같다`() {
        assertEquals(
            listOf("PopUntilScaffoldValueChange", "PopUntilCurrentDestinationChange", "PopUntilContentChange", "PopLatest"),
            BackMode.entries.map { it.label }
        )
    }

    @Test
    fun `PaneAdaptedValue 표시는 괄호 안만`() {
        assertEquals("Expanded", shortPaneValue("PaneAdaptedValue[Expanded]"))
        assertEquals("Levitated with Center and scrim=null", shortPaneValue("PaneAdaptedValue[Levitated with Center and scrim=null]"))
        assertEquals("other", shortPaneValue("other"))
    }

    @Test
    fun `폭 클래스 하한을 이름으로`() {
        assertEquals("Compact(~600dp)", widthClassName(0))
        assertEquals("Medium(600dp~)", widthClassName(600))
        assertEquals("Expanded(840dp~)", widthClassName(840))
        assertEquals("Large(1200dp~)", widthClassName(1200))
        assertEquals("XLarge(1600dp~)", widthClassName(1600))
    }
}
