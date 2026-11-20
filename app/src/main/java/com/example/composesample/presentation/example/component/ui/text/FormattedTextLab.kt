package com.example.composesample.presentation.example.component.ui.text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextFieldTextStyles
import androidx.compose.runtime.State
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * 서식 편집(Formatted Text) 실험 — foundation 1.12.1 의 TextFieldBuffer 서식 API 위에서
 * 화면과 단위 테스트가 같은 함수를 쓴다(결과 표는 지금 기기의 라이브러리가 직접 계산한다).
 */

/** 툴바 · Saver 가 함께 쓰는 서식 종류. */
internal enum class FormatKind(val label: String, val style: SpanStyle) {
    Bold("굵게", SpanStyle(fontWeight = FontWeight.Bold)),
    Italic("기울임", SpanStyle(fontStyle = FontStyle.Italic)),
    Underline("밑줄", SpanStyle(textDecoration = TextDecoration.Underline));

    fun matches(style: SpanStyle): Boolean = when (this) {
        Bold -> style.fontWeight == FontWeight.Bold
        Italic -> style.fontStyle == FontStyle.Italic
        Underline -> style.textDecoration == TextDecoration.Underline
    }

    companion object {
        fun of(style: SpanStyle): FormatKind? = entries.firstOrNull { it.matches(style) }
    }
}

/** 화면 표시용 정책 이름. */
internal val expandPolicies: List<Pair<String, ExpandPolicy>> = listOf(
    "InsideOnly" to ExpandPolicy.InsideOnly,
    "AtStart" to ExpandPolicy.AtStart,
    "AtEnd" to ExpandPolicy.AtEnd,
    "AtBoth" to ExpandPolicy.AtBoth
)

/**
 * '굵게[0,5) 기울임[7,9)' 형태 — 상태 밖에서 읽는 읽기 전용 조회(TextFieldState.textStyles).
 * 라이브러리는 붙인 순서대로 돌려주므로(뒤에서부터 붙이면 뒤가 먼저) 표시는 위치 순으로 정렬한다.
 */
internal fun describeStyles(styles: TextFieldTextStyles, length: Int): String {
    val ranges = styles.getSpanStyles(TextRange(0, length)).sortedWith(compareBy({ it.start }, { it.end }))
    if (ranges.isEmpty()) return "없음"
    return ranges.joinToString(" ") { r ->
        val name = FormatKind.of(r.item)?.label ?: "기타"
        "$name[${r.start},${r.end})"
    }
}

/** 선택 범위가 이 서식으로 빈틈없이 덮여 있는지. */
internal fun isCovered(styles: TextFieldTextStyles, selection: TextRange, kind: FormatKind): Boolean {
    if (selection.collapsed) {
        // 커서는 '그 위치를 감싸는 서식'으로 판정한다(앞 경계 포함 · 뒤 경계 제외 조회)
        return styles.getSpanStyles(selection).any { kind.matches(it.item) }
    }
    val hits = styles.getSpanStyles(selection).filter { kind.matches(it.item) }
        .map { it.start to it.end }.sortedBy { it.first }
    var reach = selection.min
    for ((s, e) in hits) {
        if (s > reach) return false
        if (e > reach) reach = e
        if (reach >= selection.max) return true
    }
    return reach >= selection.max
}

/**
 * 툴바 토글: 선택이 다 덮여 있으면 그 부분만 걷어내고(양쪽 남는 조각은 원래 정책으로 다시 붙임),
 * 아니면 선택 전체에 새 범위를 붙인다. 반환값 = 붙였으면 true.
 */
internal fun TextFieldBuffer.toggleFormat(kind: FormatKind, policy: ExpandPolicy): Boolean {
    if (selection.collapsed) return false
    val sel = TextRange(selection.min, selection.max)
    val hits = getSpanStyles(sel).filter { kind.matches(it.spanStyle) }
    val ranges = hits.map { it.textRange.min to it.textRange.max }.sortedBy { it.first }
    var reach = sel.min
    var covered = false
    for ((s, e) in ranges) {
        if (s > reach) break
        if (e > reach) reach = e
        if (reach >= sel.max) { covered = true; break }
    }
    if (!covered) {
        addStyle(kind.style, sel, policy)
        return true
    }
    for (hit in hits) {
        val r = hit.textRange
        val keep = hit.expandPolicy
        removeStyle(hit)
        if (r.min < sel.min) addStyle(kind.style, TextRange(r.min, sel.min), keep)
        if (r.max > sel.max) addStyle(kind.style, TextRange(sel.max, r.max), keep)
    }
    return false
}

// ==================== ExpandPolicy 표 ====================

internal data class ExpandRow(val policy: String, val atStart: String, val inside: String, val atEnd: String)

/** "abBOLDcd" 의 BOLD[2,6) 에 정책을 걸고 앞 경계(2) · 안(4) · 뒤 경계(6)에 'x' 를 넣은 결과. */
internal fun expandPolicyRows(): List<ExpandRow> = expandPolicies.map { (name, policy) ->
    val cells = listOf(2, 4, 6).map { at ->
        val state = TextFieldState("abBOLDcd")
        state.edit { addStyle(FormatKind.Bold.style, TextRange(2, 6), policy) }
        state.edit { replace(at, at, "x") }
        val r = state.textStyles.getSpanStyles(TextRange(0, state.text.length)).firstOrNull()
        when {
            r == null -> "서식 없음"
            at >= r.start && at < r.end -> "포함 [${r.start},${r.end})"
            else -> "제외 [${r.start},${r.end})"
        }
    }
    ExpandRow(name, cells[0], cells[1], cells[2])
}

// ==================== 되돌리기와 서식 ====================

internal data class UndoRow(val scenario: String, val afterEdit: String, val afterUndo: String, val afterRedo: String)

@OptIn(ExperimentalFoundationApi::class)
private fun snapshotOf(state: TextFieldState): String =
    "'${state.text}' · ${describeStyles(state.textStyles, state.text.length)}"

@OptIn(ExperimentalFoundationApi::class)
private fun undoRow(scenario: String, initial: String, setup: TextFieldBuffer.() -> Unit, edit: TextFieldBuffer.() -> Unit): UndoRow {
    val state = TextFieldState(initial)
    state.edit(setup)
    // 준비 단계(미리 붙여 둔 서식)는 되돌리기 대상에서 뺀다
    state.undoState.clearHistory()
    state.edit(edit)
    val after = snapshotOf(state)
    state.undoState.undo()
    val undone = snapshotOf(state)
    state.undoState.redo()
    val redone = snapshotOf(state)
    return UndoRow(scenario, after, undone, redone)
}

/** 같은 기기 라이브러리로 다섯 경우를 실제로 돌린 결과. */
internal fun undoScenarioRows(): List<UndoRow> {
    val bold = FormatKind.Bold.style
    return listOf(
        undoRow("① 서식만 붙이기", "hello world", {}, { addStyle(bold, TextRange(0, 5), ExpandPolicy.AtEnd) }),
        undoRow("② 서식 글자 일부 지우기", "hello world", { addStyle(bold, TextRange(0, 5), ExpandPolicy.AtEnd) }, { replace(1, 3, "") }),
        undoRow("③ 서식 글자 전부 지우기", "hello world", { addStyle(bold, TextRange(0, 5), ExpandPolicy.AtEnd) }, { replace(0, 5, "") }),
        undoRow("④ 끝 경계에 입력(AtEnd)", "hello world", { addStyle(bold, TextRange(0, 5), ExpandPolicy.AtEnd) }, { replace(5, 5, "!") }),
        undoRow("⑤ 글자 + 서식 한 번에", "abc", {}, { replace(3, 3, "DEF"); addStyle(bold, TextRange(3, 6), ExpandPolicy.AtEnd) })
    )
}

// ==================== 입력 변환 · 출력 변환 ====================

/** 입력 중 `**글자**` 를 만나면 별표를 지우고 그 글자에 굵게를 붙인다 — 상태에 남는다. */
internal object MarkdownBoldInput : InputTransformation {
    // 정규식은 한 번만 컴파일한다(입력마다 새로 만들면 비용이 입력 수에 비례)
    private val pattern = Regex("""\*\*([^*\n]+)\*\*""")

    override fun TextFieldBuffer.transformInput() {
        // 뒤에서부터 바꿔야 앞쪽 일치의 위치가 밀리지 않는다
        val matches = pattern.findAll(asCharSequence()).toList().asReversed()
        for (m in matches) {
            val content = m.groupValues[1]
            val start = m.range.first
            replace(start, m.range.last + 1, content)
            // 마크다운처럼 굵게 뒤에 이어 친 글자는 굵게가 아니도록 InsideOnly
            addStyle(FormatKind.Bold.style, TextRange(start, start + content.length), ExpandPolicy.InsideOnly)
        }
    }
}

/** 검색어를 노란 배경으로 칠한다 — 그리기용일 뿐 상태(textStyles)에는 남지 않는다. */
internal class SearchHighlightOutput(private val query: State<String>) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val q = query.value
        if (q.isEmpty()) return
        val text = asCharSequence().toString()
        var i = text.indexOf(q, ignoreCase = true)
        while (i >= 0) {
            addStyle(HIGHLIGHT, i, i + q.length)
            i = text.indexOf(q, i + q.length, ignoreCase = true)
        }
    }

    private companion object {
        val HIGHLIGHT = SpanStyle(background = Color(0xFFFFEB3B), color = Color.Black)
    }
}

// ==================== 저장 · 복원 ====================

/**
 * 서식까지 저장하는 Saver — 기본 TextFieldState.Saver 는 1.12.1 까지 텍스트 · 선택 · undo 이력만 저장했다.
 * Compose 1.13.0 부터 기본 Saver 가 서식(TextStyleBuffer — ExpandPolicy 포함)까지 저장해, 이것은 1.12 대조용으로 남긴다.
 * 저장 형식: [텍스트, 선택 시작, 선택 끝, (종류 번호, 시작, 끝) 반복]. ExpandPolicy 는 textStyles 로
 * 읽을 수 없어 AtEnd 로 되살리고, 복원하며 붙인 서식이 undo 이력에 남지 않게 이력을 비운다.
 */
@OptIn(ExperimentalFoundationApi::class)
internal object FormattedTextStateSaver : Saver<TextFieldState, Any> {
    override fun SaverScope.save(value: TextFieldState): Any {
        val out = arrayListOf<Any>(value.text.toString(), value.selection.start, value.selection.end)
        value.textStyles.getSpanStyles(TextRange(0, value.text.length)).forEach { r ->
            val kind = FormatKind.of(r.item) ?: return@forEach
            out += kind.ordinal
            out += r.start
            out += r.end
        }
        return out
    }

    override fun restore(value: Any): TextFieldState? {
        val list = value as? List<*> ?: return null
        val text = list.getOrNull(0) as? String ?: return null
        val selStart = list.getOrNull(1) as? Int ?: 0
        val selEnd = list.getOrNull(2) as? Int ?: 0
        val state = TextFieldState(text, TextRange(selStart, selEnd))
        val rest = list.drop(3).filterIsInstance<Int>()
        if (rest.isNotEmpty()) {
            state.edit {
                rest.chunked(3).filter { it.size == 3 }.forEach { (k, s, e) ->
                    val kind = FormatKind.entries.getOrNull(k) ?: return@forEach
                    if (s in 0..length && e in s..length && s < e) {
                        addStyle(kind.style, TextRange(s, e), ExpandPolicy.AtEnd)
                    }
                }
            }
            state.undoState.clearHistory()
        }
        return state
    }
}

internal data class SaverReport(
    val label: String,
    val savedItems: Int,
    val restoredText: String,
    val restoredSelection: String,
    val restoredStyles: String,
    val canUndoAfterRestore: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
private fun roundTrip(label: String, saver: Saver<TextFieldState, Any>): SaverReport {
    val state = TextFieldState("hello world", TextRange(3, 7))
    state.edit { addStyle(FormatKind.Bold.style, TextRange(0, 5), ExpandPolicy.AtEnd) }
    val saved = with(saver) { SaverScope { true }.save(state) }
    val restored = saved?.let { saver.restore(it) }
    return SaverReport(
        label = label,
        savedItems = (saved as? List<*>)?.size ?: -1,
        restoredText = restored?.text?.toString() ?: "(복원 실패)",
        restoredSelection = restored?.selection?.let { "${it.start},${it.end}" } ?: "-",
        restoredStyles = restored?.let { describeStyles(it.textStyles, it.text.length) } ?: "-",
        canUndoAfterRestore = restored?.undoState?.canUndo ?: false
    )
}

/** 굵게[0,5) 를 붙인 'hello world'(선택 3,7)를 두 Saver 로 저장 → 복원한 결과. */
internal fun saverReports(): List<SaverReport> = listOf(
    roundTrip("기본 TextFieldState.Saver", TextFieldState.Saver),
    roundTrip("직접 만든 Saver", FormattedTextStateSaver)
)

/** 측정 로그 · 화면이 함께 쓰는 판정 줄. */
internal fun formattedTextLines(): List<String> = buildList {
    expandPolicyRows().forEach { add("expand ${it.policy} | ${it.atStart} | ${it.inside} | ${it.atEnd}") }
    undoScenarioRows().forEach { add("undo ${it.scenario} | ${it.afterEdit} | ${it.afterUndo} | ${it.afterRedo}") }
    saverReports().forEach {
        add("saver ${it.label} | 항목 ${it.savedItems} | '${it.restoredText}' | 선택 ${it.restoredSelection} | ${it.restoredStyles} | canUndo=${it.canUndoAfterRestore}")
    }
}
