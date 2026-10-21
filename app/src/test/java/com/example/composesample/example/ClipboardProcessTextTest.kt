package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.platform.clipboard.ClipSnapshot
import com.example.composesample.presentation.example.component.system.platform.clipboard.DemoField
import com.example.composesample.presentation.example.component.system.platform.clipboard.ProcessTextLog
import com.example.composesample.presentation.example.component.system.platform.clipboard.ProcessTextReceipt
import com.example.composesample.presentation.example.component.system.platform.clipboard.ReadVerdict
import com.example.composesample.presentation.example.component.system.platform.clipboard.checkReceipt
import com.example.composesample.presentation.example.component.system.platform.clipboard.processText
import com.example.composesample.presentation.example.component.system.platform.clipboard.sourceOf
import com.example.composesample.presentation.example.component.system.platform.clipboard.verdictOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ClipboardProcessTextExample 의 판정 로직 검증 — 화면 기록 행이 쓰는 함수를 그대로 부른다.
 * given : 클립보드 읽기 결과 / PROCESS_TEXT 로 받은 선택 문자열·엑스트라
 * when : 읽기 판정 · 어느 필드에서 왔는지 · READONLY·결과 전달 가능 여부 확인
 * then : 거부와 빈 클립은 포커스로만 갈리고, Compose 경로는 READONLY 가 뒤집혀 결과를 받을 곳이 없다
 */
class ClipboardProcessTextTest {

    private val none = ClipSnapshot(false, false, ours = false, sensitive = false, length = 0, mimeTypes = emptyList(), ownText = null)

    @Test
    fun `같은 null 이라도 포커스가 없으면 거부, 있으면 빈 클립으로 판정한다`() {
        assertEquals(ReadVerdict.DENIED, verdictOf(focused = false, snapshot = none))
        assertEquals(ReadVerdict.EMPTY, verdictOf(focused = true, snapshot = none))
        val ours = none.copy(clipPresent = true, hasPrimaryClip = true, ours = true, length = 8, ownText = "clip-123")
        assertEquals(ReadVerdict.OURS, verdictOf(focused = true, snapshot = ours))
        assertEquals(ReadVerdict.OTHER, verdictOf(focused = true, snapshot = ours.copy(ours = false, ownText = null)))
    }

    @Test
    fun `선택 문자열로 어느 필드에서 왔는지 가른다 — 대소문자 무시, 겹치면 판별 불가`() {
        assertEquals(DemoField.COMPOSE_FIELD, sourceOf("alpha"))
        assertEquals(DemoField.VIEW_EDIT, sourceOf("CHARLIE"))
        assertEquals(DemoField.CONNECTED, sourceOf("connected echo"))
        assertNull(sourceOf("e"))
        assertNull(sourceOf("다른 앱 문장"))
    }

    @Test
    fun `결과는 영문이면 대문자, 대소문자가 없는 글자면 꺾쇠로 감싼다`() {
        assertEquals("ALPHA", processText("alpha"))
        assertEquals("「안녕」", processText("안녕"))
    }

    @Test
    fun `Compose 편집 필드에서 READONLY=true 로 오면 반대로 판정하고 결과 전달 불가로 본다`() {
        val compose = ProcessTextReceipt(1, "00:00:00", "alpha", readonlyExtra = true, callingActivity = null, referrer = "x", outcome = "")
        val check = checkReceipt(compose)
        assertEquals(DemoField.COMPOSE_FIELD, check.field)
        assertEquals(false, check.expectedReadonly)
        assertEquals(false, check.readonlyOk)
        assertFalse(check.canReturnResult)

        val view = compose.copy(text = "charlie", readonlyExtra = false, callingActivity = ".presentation.example.BlogExampleActivity")
        val viewCheck = checkReceipt(view)
        assertEquals(true, viewCheck.readonlyOk)
        assertTrue(viewCheck.canReturnResult)
    }

    @Test
    fun `기록 번호는 받을 때마다 늘고 결과 갱신은 그 한 줄에만 적용된다`() {
        ProcessTextLog.clear()
        val first = ProcessTextLog.add("alpha", true, null, "x")
        val second = ProcessTextLog.add("bravo", false, null, "x")
        assertTrue("두 번째 번호($second)가 첫 번째($first)보다 커야 한다", second > first)
        ProcessTextLog.setOutcome(second, "돌려줌")
        val rows = ProcessTextLog.receipts.value
        assertEquals(listOf("열림", "돌려줌"), rows.map { it.outcome })
        ProcessTextLog.clear()
    }
}
