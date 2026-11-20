package com.example.composesample.example

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.text.TextRange
import com.example.composesample.presentation.example.component.ui.text.FormatKind
import com.example.composesample.presentation.example.component.ui.text.FormattedTextStateSaver
import com.example.composesample.presentation.example.component.ui.text.MarkdownBoldInput
import com.example.composesample.presentation.example.component.ui.text.SearchHighlightOutput
import com.example.composesample.presentation.example.component.ui.text.describeStyles
import com.example.composesample.presentation.example.component.ui.text.expandPolicyRows
import com.example.composesample.presentation.example.component.ui.text.isCovered
import com.example.composesample.presentation.example.component.ui.text.saverReports
import com.example.composesample.presentation.example.component.ui.text.toggleFormat
import com.example.composesample.presentation.example.component.ui.text.undoScenarioRows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 서식 편집 Lab — 기대값은 ExpandPolicy KDoc 규칙 · 손으로 계산한 위치 · foundation 1.12.1 실측으로 쓴다.
 * undo/saver 테스트는 1.12.1 동작을 고정한다: 상향 뒤 실패하면 화면 문구(7 · 9번 카드)를 다시 잰다.
 */
@OptIn(ExperimentalFoundationApi::class)
class FormattedTextLabTest {

    private fun styles(state: TextFieldState) = describeStyles(state.textStyles, state.text.length)

    @Test
    fun `ExpandPolicy 는 경계 입력만 갈리고 안쪽 입력은 늘 포함한다`() {
        val rows = expandPolicyRows().associateBy { it.policy }
        // BOLD[2,6) — 앞 경계(2) 에 넣으면 포함 시 [2,7) · 제외 시 [3,7), 뒤 경계(6) 는 포함 시 [2,7) · 제외 시 [2,6)
        assertEquals(listOf("제외 [3,7)", "포함 [2,7)", "제외 [2,6)"), rows.getValue("InsideOnly").cells())
        assertEquals(listOf("포함 [2,7)", "포함 [2,7)", "제외 [2,6)"), rows.getValue("AtStart").cells())
        assertEquals(listOf("제외 [3,7)", "포함 [2,7)", "포함 [2,7)"), rows.getValue("AtEnd").cells())
        assertEquals(listOf("포함 [2,7)", "포함 [2,7)", "포함 [2,7)"), rows.getValue("AtBoth").cells())
    }

    private fun com.example.composesample.presentation.example.component.ui.text.ExpandRow.cells() =
        listOf(atStart, inside, atEnd)

    @Test
    fun `정책 없는 addStyle 은 AtEnd 처럼 뒤 경계 입력을 포함한다`() {
        val state = TextFieldState("abcd")
        state.edit { addStyle(FormatKind.Bold.style, 1, 3) }
        state.edit { replace(3, 3, "Z") }
        assertEquals("굵게[1,4)", styles(state))
    }

    @Test
    fun `토글은 덮인 선택에서 그 부분만 걷어내고 양쪽 조각을 남긴다`() {
        val state = TextFieldState("0123456789", TextRange(3, 6))
        state.edit { addStyle(FormatKind.Bold.style, TextRange(0, 10), ExpandPolicy.AtEnd) }
        assertTrue(isCovered(state.textStyles, state.selection, FormatKind.Bold))
        var added = true
        state.edit { added = toggleFormat(FormatKind.Bold, ExpandPolicy.AtEnd) }
        assertFalse(added)
        assertEquals("굵게[0,3) 굵게[6,10)", styles(state))
        assertFalse(isCovered(state.textStyles, state.selection, FormatKind.Bold))
    }

    @Test
    fun `빈틈이 있으면 덮이지 않은 것으로 보고 선택 전체에 붙인다`() {
        val state = TextFieldState("0123456789", TextRange(0, 8))
        state.edit {
            addStyle(FormatKind.Bold.style, TextRange(0, 3), ExpandPolicy.AtEnd)
            addStyle(FormatKind.Bold.style, TextRange(5, 8), ExpandPolicy.AtEnd)
        }
        assertFalse(isCovered(state.textStyles, state.selection, FormatKind.Bold))
        var added = false
        state.edit { added = toggleFormat(FormatKind.Bold, ExpandPolicy.AtEnd) }
        assertTrue(added)
        assertTrue(isCovered(state.textStyles, state.selection, FormatKind.Bold))
        // 다른 종류는 영향 없음
        assertFalse(isCovered(state.textStyles, state.selection, FormatKind.Italic))
    }

    @Test
    fun `커서 위치 판정은 앞 경계를 포함하고 뒤 경계를 뺀다`() {
        val state = TextFieldState("0123456789")
        state.edit { addStyle(FormatKind.Italic.style, TextRange(2, 5), ExpandPolicy.AtEnd) }
        assertTrue(isCovered(state.textStyles, TextRange(2), FormatKind.Italic))
        assertTrue(isCovered(state.textStyles, TextRange(4), FormatKind.Italic))
        assertFalse(isCovered(state.textStyles, TextRange(5), FormatKind.Italic))
    }

    @Test
    fun `1_12_1 실측 — 살아 있는 서식은 undo redo 가 범위를 되돌리고 사라진 서식은 다시 만들지 못한다`() {
        val rows = undoScenarioRows().associateBy { it.scenario.take(1) }
        val add = rows.getValue("①")
        assertEquals("'hello world' · 굵게[0,5)", add.afterEdit)
        assertEquals("'hello world' · 없음", add.afterUndo)
        assertEquals("'hello world' · 없음", add.afterRedo)

        val partial = rows.getValue("②")
        assertEquals("'hlo world' · 굵게[0,3)", partial.afterEdit)
        assertEquals("'hello world' · 굵게[0,5)", partial.afterUndo)
        assertEquals("'hlo world' · 굵게[0,3)", partial.afterRedo)

        val all = rows.getValue("③")
        assertEquals("' world' · 없음", all.afterEdit)
        assertEquals("'hello world' · 없음", all.afterUndo)

        val typed = rows.getValue("④")
        assertEquals("'hello! world' · 굵게[0,6)", typed.afterEdit)
        assertEquals("'hello world' · 굵게[0,5)", typed.afterUndo)
        assertEquals("'hello! world' · 굵게[0,6)", typed.afterRedo)

        val together = rows.getValue("⑤")
        assertEquals("'abcDEF' · 굵게[3,6)", together.afterEdit)
        assertEquals("'abc' · 없음", together.afterUndo)
        assertEquals("'abcDEF' · 없음", together.afterRedo)
    }

    @Test
    fun `1_13_0 실측 — 기본 Saver 도 서식을 지킨다(1_12_1 은 버렸다) · 직접 만든 Saver 도 지킨다`() {
        val (builtin, custom) = saverReports()
        assertEquals("hello world", builtin.restoredText)
        assertEquals("3,7", builtin.restoredSelection)
        // 1.12.1 은 "없음" 이었다 — 1.13.0 의 Saver 는 [텍스트, 선택 시작, 선택 끝, undo, 서식] 5항목
        assertEquals("굵게[0,5)", builtin.restoredStyles)
        assertEquals(5, builtin.savedItems)
        // 기본 Saver 는 undo 이력도 저장해서 복원 뒤에도 되돌릴 것이 남아 있다
        assertTrue(builtin.canUndoAfterRestore)

        assertEquals("hello world", custom.restoredText)
        assertEquals("3,7", custom.restoredSelection)
        assertEquals("굵게[0,5)", custom.restoredStyles)
        assertFalse(custom.canUndoAfterRestore)
        // 텍스트 · 선택 2 + (종류, 시작, 끝) 1개
        assertEquals(6, custom.savedItems)
    }

    @Test
    fun `1_13_0 기본 Saver 는 ExpandPolicy 까지 되살리고 직접 만든 Saver 는 AtEnd 로 되살린다`() {
        fun roundTripInsideOnly(saver: Saver<TextFieldState, Any>): String {
            val state = TextFieldState("hello world")
            state.edit { addStyle(FormatKind.Bold.style, TextRange(0, 5), ExpandPolicy.InsideOnly) }
            val saved = with(saver) { SaverScope { true }.save(state) }!!
            val restored = saver.restore(saved)!!
            // 굵게 끝 경계에 한 글자 — InsideOnly 면 늘지 않고 AtEnd 면 는다
            restored.edit { replace(5, 5, "Z") }
            return styles(restored)
        }
        assertEquals("굵게[0,5)", roundTripInsideOnly(TextFieldState.Saver))
        assertEquals("굵게[0,6)", roundTripInsideOnly(FormattedTextStateSaver))
    }

    @Test
    fun `직접 만든 Saver 는 깨진 값을 건너뛴다`() {
        assertNull(FormattedTextStateSaver.restore("not a list"))
        val restored = FormattedTextStateSaver.restore(listOf("abc", 1, 2, 0, 0, 2, 9, 1, 3, 1, 0, 5))
        // (0,0,2) 굵게 OK · (9,1,3) 없는 종류 · (1,0,5) 범위 밖 → 건너뜀
        assertEquals("abc", restored?.text?.toString())
        assertEquals("굵게[0,2)", restored?.let { styles(it) })
        val saved = with(FormattedTextStateSaver) { SaverScope { true }.save(restored!!) }
        assertEquals(listOf("abc", 1, 2, 0, 0, 2), saved)
    }

    @Test
    fun `마크다운 입력 변환은 별표를 지우고 굵게를 붙이며 뒤에 친 글자는 굵게가 아니다`() {
        val state = TextFieldState("a **bc** d **e**")
        state.edit { with(MarkdownBoldInput) { transformInput() } }
        // 'a bc d e' — b=2 c=3, e=7
        assertEquals("a bc d e", state.text.toString())
        assertEquals("굵게[2,4) 굵게[7,8)", styles(state))
        state.edit { replace(4, 4, "Z") }
        assertEquals("굵게[2,4) 굵게[8,9)", styles(state))
        // 별표 하나 · 닫히지 않은 별표는 그대로
        val plain = TextFieldState("*a* **b")
        plain.edit { with(MarkdownBoldInput) { transformInput() } }
        assertEquals("*a* **b", plain.text.toString())
        assertEquals("없음", styles(plain))
    }

    @Test
    fun `검색 강조는 대소문자 없이 모든 위치를 칠한다`() {
        val query = mutableStateOf("compose")
        val highlight = SearchHighlightOutput(query)
        val state = TextFieldState("Compose x compose y COMPOSE")
        state.edit { with(highlight) { transformOutput() } }
        assertEquals(listOf(0 to 7, 10 to 17, 20 to 27), state.textStyles.getSpanStyles(TextRange(0, state.text.length)).map { it.start to it.end })
        query.value = ""
        val empty = TextFieldState("Compose")
        empty.edit { with(highlight) { transformOutput() } }
        assertEquals("없음", styles(empty))
    }

    @Test
    fun `서식 종류는 SpanStyle 로 되찾는다`() {
        FormatKind.entries.forEach { assertEquals(it, FormatKind.of(it.style)) }
        assertNull(FormatKind.of(androidx.compose.ui.text.SpanStyle()))
    }
}
