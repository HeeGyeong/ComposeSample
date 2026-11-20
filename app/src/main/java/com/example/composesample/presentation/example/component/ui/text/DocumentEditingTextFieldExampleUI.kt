package com.example.composesample.presentation.example.component.ui.text

import android.os.Process
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun DocumentEditingTextFieldExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Document Editing TextField",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { UndoRedoCard() }
            item { SelectionManipulationCard() }
            item { AnnotatedStringPreviewCard() }
            item { MultiCursorSimulationCard() }
            item { FormattingToolbarCard() }
            item { ExpandPolicyCard() }
            item { UndoWithStylesCard() }
            item { TransformationStylesCard() }
            item { SaveRestoreStylesCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TextFieldState 심화: 문서 편집 패턴",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "단순 입력을 넘어 문서 편집기 수준 패턴: 1~4 는 Undo/Redo, Selection 조작, " +
                        "AnnotatedString 미리보기, 멀티 커서 시뮬레이션. 5~9 는 서식(굵게 · 기울임 · 밑줄)을 " +
                        "TextFieldState 안에 두는 서식 편집 — foundation 1.12.1 의 TextFieldBuffer.addStyle · " +
                        "TrackedRange · ExpandPolicy 를 지금 버전 그대로 씁니다. 모두 단일 진실 원천(TextFieldState) 위에서 동작합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UndoRedoCard() {
    val state = rememberTextFieldState(initialText = "여기에 텍스트를 입력하거나 버튼으로 변경해 보세요.")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Undo / Redo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TextFieldState.undoState로 변경 이력 되돌리기/다시 실행. " +
                        "키보드 입력 + edit{} 프로그래매틱 변경 모두 동일한 히스토리에 누적됩니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            DocumentTextField(state = state, minLines = 3)

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { state.undoState.undo() },
                    enabled = state.undoState.canUndo,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) { Text(text = "Undo", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = { state.undoState.redo() },
                    enabled = state.undoState.canRedo,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) { Text(text = "Redo", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = {
                        state.edit { append("\n[프로그래매틱 추가]") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) { Text(text = "추가", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = { state.undoState.clearHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) { Text(text = "이력 초기화", color = Color.White, fontSize = 12.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "canUndo=${state.undoState.canUndo}  /  canRedo=${state.undoState.canRedo}",
                fontSize = 11.sp,
                color = Color(0xFF616161),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SelectionManipulationCard() {
    val state = rememberTextFieldState(initialText = "Hello Compose. 텍스트를 선택해 보세요.")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. Selection 조작",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E24AA)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "selection: TextRange를 직접 변경하여 커서 이동/범위 선택을 제어합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            DocumentTextField(state = state, minLines = 2)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "selection: ${state.selection.start} .. ${state.selection.end}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 전체 선택
                        state.edit { selection = TextRange(0, length) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                ) { Text(text = "전체 선택", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = {
                        // 커서 맨 앞으로
                        state.edit { selection = TextRange(0) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                ) { Text(text = "맨 앞으로", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = {
                        // 커서 맨 뒤로
                        state.edit { selection = TextRange(length) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                ) { Text(text = "맨 뒤로", color = Color.White, fontSize = 12.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 선택 영역을 대문자로 치환
                        val sel = state.selection
                        if (!sel.collapsed) {
                            val replaced = state.text
                                .substring(sel.start, sel.end)
                                .uppercase()
                            state.edit {
                                replace(sel.start, sel.end, replaced)
                                selection = TextRange(sel.start, sel.start + replaced.length)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) { Text(text = "선택 → 대문자", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = {
                        // 선택 영역 삭제
                        val sel = state.selection
                        if (!sel.collapsed) {
                            state.edit {
                                replace(sel.start, sel.end, "")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text(text = "선택 삭제", color = Color.White, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun AnnotatedStringPreviewCard() {
    val state = rememberTextFieldState(
        initialText = "**굵게**, *기울임*, 그리고 일반 텍스트가 섞인 마크다운 미리보기."
    )
    var preview by remember { mutableStateOf(buildAnnotated(state.text.toString())) }

    // 텍스트가 변경될 때마다 미리보기 갱신
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .collect { text -> preview = buildAnnotated(text) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. AnnotatedString 미리보기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00838F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TextFieldState.text를 snapshotFlow로 관찰하여 마크다운 토큰(**, *)을 " +
                        "AnnotatedString으로 렌더링합니다. 입력은 평문으로 유지됩니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            DocumentTextField(state = state, minLines = 3)

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "미리보기",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF616161)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = preview,
                    fontSize = 14.sp,
                    color = Color(0xFF212121),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// 매우 단순한 토크나이저: **bold**, *italic*만 처리
private fun buildAnnotated(input: String): AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var i = 0
        while (i < input.length) {
            // **굵게**
            val boldEnd = input.indexOf("**", i + 2).takeIf { input.startsWith("**", i) && it > i }
            if (boldEnd != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(input.substring(i + 2, boldEnd))
                }
                i = boldEnd + 2
                continue
            }
            // *기울임*
            val italicEnd = input.indexOf("*", i + 1).takeIf {
                input.startsWith("*", i) && !input.startsWith("**", i) && it > i
            }
            if (italicEnd != null && !input.startsWith("**", italicEnd)) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(input.substring(i + 1, italicEnd))
                }
                i = italicEnd + 1
                continue
            }
            append(input[i])
            i++
        }
    }
}

@Composable
private fun MultiCursorSimulationCard() {
    val state = rememberTextFieldState(initialText = "라인 1\n라인 2\n라인 3\n라인 4")
    val cursors = remember { mutableStateListOf<Int>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "4. 멀티 커서 시뮬레이션",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD81B60)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "각 줄 시작 위치를 가상 커서로 등록하고, 한 번에 동일 텍스트를 삽입합니다. " +
                        "Compose 기본 BasicTextField는 단일 커서만 지원하므로 오프셋을 직접 관리합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            DocumentTextField(state = state, minLines = 4)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "가상 커서: ${if (cursors.isEmpty()) "없음" else cursors.joinToString()}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 모든 줄의 시작 위치를 가상 커서로 등록
                        cursors.clear()
                        var idx = 0
                        cursors.add(0)
                        val text = state.text.toString()
                        while (idx < text.length) {
                            val nl = text.indexOf('\n', idx)
                            if (nl < 0) break
                            cursors.add(nl + 1)
                            idx = nl + 1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60))
                ) { Text(text = "줄 시작에 커서", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = {
                        if (cursors.isNotEmpty()) {
                            // 뒤에서부터 삽입해야 오프셋이 유지됨
                            val sorted = cursors.sortedDescending()
                            state.edit {
                                sorted.forEach { offset ->
                                    if (offset in 0..length) {
                                        replace(offset, offset, "▶ ")
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) { Text(text = "▶ 일괄 삽입", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = { cursors.clear() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) { Text(text = "커서 초기화", color = Color.White, fontSize = 12.sp) }
            }
        }
    }
}

// ==================== 5~9. 서식 편집(Formatted Text) ====================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FormattingToolbarCard() {
    val state = rememberTextFieldState(initialText = "서식은 이제 TextFieldState 안에 있습니다. 글자를 선택해 보세요.")
    var policyIndex by remember { mutableIntStateOf(2) }
    val policy = expandPolicies[policyIndex].second
    val styles = state.textStyles

    FormattedCard(
        title = "5. 서식 툴바 — addStyle · TrackedRange",
        color = Color(0xFF6A1B9A),
        description = "선택한 글자에 edit { addStyle(style, range, ExpandPolicy) } 로 서식을 붙입니다. 서식은 텍스트와 함께 상태에 들어 있어 " +
            "입력 · 삭제에 맞춰 범위가 저절로 움직입니다. 다 덮인 선택에서 다시 누르면 그 부분만 걷어냅니다(removeStyle 뒤 남는 조각을 원래 정책으로 다시 붙임)."
    ) {
        DocumentTextField(state = state, minLines = 2)
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormatKind.entries.forEach { kind ->
                FilterChip(
                    selected = isCovered(styles, state.selection, kind),
                    onClick = { state.edit { toggleFormat(kind, policy) } },
                    label = { Text(text = kind.label, fontSize = 12.sp) }
                )
            }
        }
        Text(text = "새 서식의 ExpandPolicy", fontSize = 11.sp, color = Color(0xFF616161))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            expandPolicies.forEachIndexed { index, (name, _) ->
                FilterChip(
                    selected = policyIndex == index,
                    onClick = { policyIndex = index },
                    label = { Text(text = name, fontSize = 11.sp) }
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton("앞 7글자 선택", Color(0xFF6A1B9A)) {
                state.edit { selection = TextRange(0, minOf(7, length)) }
            }
            SmallActionButton("전체 선택", Color(0xFF6A1B9A)) {
                state.edit { selection = TextRange(0, length) }
            }
            SmallActionButton("선택 끝에 커서", Color(0xFF6A1B9A)) {
                // 이어서 치는 글자가 서식 끝 경계에 들어간다 — 정책에 따라 범위에 들어가는지 갈린다
                state.edit { selection = TextRange(selection.max) }
            }
            SmallActionButton("Undo", Color(0xFF1976D2), enabled = state.undoState.canUndo) { state.undoState.undo() }
            SmallActionButton("Redo", Color(0xFF1976D2), enabled = state.undoState.canRedo) { state.undoState.redo() }
        }
        Spacer(modifier = Modifier.height(6.dp))
        MonoLine("선택 ${state.selection.start}..${state.selection.end} · 서식 ${describeStyles(styles, state.text.length)}")
        MonoLine("state.text 는 평문: '${state.text.take(12)}…' (서식은 state.textStyles 로만 보인다)")
    }
}

@Composable
private fun ExpandPolicyCard() {
    val rows = remember { expandPolicyRows() }
    FormattedCard(
        title = "6. ExpandPolicy — 경계에 친 글자는 누구 편인가",
        color = Color(0xFF00695C),
        description = "'abBOLDcd' 의 BOLD[2,6) 에 정책을 걸고 앞 경계(2) · 안(4) · 뒤 경계(6)에 'x' 를 넣은 결과를 이 기기의 라이브러리가 " +
            "직접 계산한 표입니다. 안쪽 입력은 늘 포함, 경계 입력만 정책에 따라 갈립니다. 정책 없는 addStyle(style, start, end) 는 AtEnd 와 같습니다."
    ) {
        rows.forEach { row ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text(
                    text = row.policy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00695C),
                    modifier = Modifier.width(88.dp)
                )
                MonoLine("앞 ${row.atStart}\n안 ${row.inside}\n뒤 ${row.atEnd}")
            }
        }
    }
}

@Composable
private fun UndoWithStylesCard() {
    val rows = remember { undoScenarioRows() }
    FormattedCard(
        title = "7. 되돌리기와 서식",
        color = Color(0xFFBF360C),
        description = "같은 편집을 undo → redo 한 결과(이 기기 라이브러리가 계산). 서식이 살아 있는 동안은 범위가 함께 되돌아가지만(②④), " +
            "사라진 서식 — 붙이기를 되돌린 것(①) · 글자를 전부 지워 길이 0 이 된 것(③) · 글자와 함께 붙인 것(⑤) — 은 undo · redo 어느 쪽도 다시 만들지 못합니다(1.12.1 실측)."
    ) {
        rows.forEach { row ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(text = row.scenario, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                MonoLine("편집 뒤 ${row.afterEdit}\nundo  ${row.afterUndo}\nredo  ${row.afterRedo}")
            }
        }
    }
}

@Composable
private fun TransformationStylesCard() {
    val markdownState = rememberTextFieldState(initialText = "별표 두 개로 감싸 입력해 보세요: ")
    val searchQuery = rememberTextFieldState(initialText = "compose")
    val query = remember { derivedStateOf { searchQuery.text.toString() } }
    val highlight = remember { SearchHighlightOutput(query) }
    val searchTarget = rememberTextFieldState(
        initialText = "Compose 의 텍스트 필드. compose 는 대소문자 없이 찾습니다."
    )

    FormattedCard(
        title = "8. 입력 변환은 서식을 남기고, 출력 변환은 그리기만",
        color = Color(0xFF283593),
        description = "InputTransformation 안에서 붙인 서식은 상태에 들어갑니다 — 입력 중 별표 두 개로 감싼 글자를 만나면 별표를 지우고 굵게(InsideOnly)를 붙입니다. " +
            "OutputTransformation 의 addStyle 은 보이는 모양만 바꿔, 노랗게 칠해져도 state.textStyles 는 비어 있습니다."
    ) {
        StyledTextField(state = markdownState, inputTransformation = MarkdownBoldInput)
        MonoLine("입력 변환 칸 textStyles: ${describeStyles(markdownState.textStyles, markdownState.text.length)}")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "검색어", fontSize = 11.sp, color = Color(0xFF616161))
        DocumentTextField(state = searchQuery, minLines = 1)
        Spacer(modifier = Modifier.height(6.dp))
        StyledTextField(state = searchTarget, outputTransformation = highlight)
        MonoLine("출력 변환 칸 textStyles: ${describeStyles(searchTarget.textStyles, searchTarget.text.length)}")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SaveRestoreStylesCard() {
    val defaultState = rememberTextFieldState(initialText = "hello world")
    val customState = rememberSaveable(saver = FormattedTextStateSaver) { TextFieldState("hello world") }
    // 재생성 증거는 저장되는 이력 문자열로 남긴다(이 카드가 만들어질 때마다 한 줄).
    // 스크롤로 다시 들어온 것과 구분하려고 pid · 액티비티 표식을 함께 적는다.
    val activityMark = Integer.toHexString(System.identityHashCode(LocalContext.current)).takeLast(4)
    var history by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val n = if (history.isEmpty()) 1 else history.count { it == '\n' } + 2
        val line = "#$n pid ${Process.myPid()} · 액티비티 @$activityMark — 기본 " +
            "${describeStyles(defaultState.textStyles, defaultState.text.length)} · " +
            "직접 ${describeStyles(customState.textStyles, customState.text.length)}"
        history = if (history.isEmpty()) line else "$history\n$line"
    }
    val reports = remember { saverReports() }

    FormattedCard(
        title = "9. 저장 · 복원 — 기본 Saver 가 서식까지 저장한다(1.13)",
        color = Color(0xFF37474F),
        description = "rememberTextFieldState 의 TextFieldState.Saver 는 1.12.1 까지 텍스트 · 선택 · undo 이력만 저장해, 재생성 · 프로세스 종료 뒤 " +
            "왼쪽 서식만 사라졌습니다. Compose 1.13.0 부터는 서식까지 저장합니다(5항목, ExpandPolicy 포함). 두 칸에 굵게를 붙인 뒤 다크 모드를 " +
            "바꾸거나(같은 프로세스 재생성) 프로세스가 죽었다 살아나도 이제 두 칸 모두 남습니다. 오른쪽은 1.12 에서 쓰던 직접 만든 Saver(정책은 AtEnd 로 되살림)."
    ) {
        Text(text = "기본 rememberTextFieldState", fontSize = 11.sp, color = Color(0xFF616161))
        StyledTextField(state = defaultState)
        MonoLine("서식 ${describeStyles(defaultState.textStyles, defaultState.text.length)}")
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "rememberSaveable(saver = FormattedTextStateSaver)", fontSize = 11.sp, color = Color(0xFF616161))
        StyledTextField(state = customState)
        MonoLine("서식 ${describeStyles(customState.textStyles, customState.text.length)}")
        Spacer(modifier = Modifier.height(8.dp))
        SmallActionButton("두 칸 모두 앞 5글자 굵게", Color(0xFF37474F)) {
            listOf(defaultState, customState).forEach { s ->
                s.edit { if (length > 0) addStyle(FormatKind.Bold.style, TextRange(0, minOf(5, length)), ExpandPolicy.AtEnd) }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        MonoLine(history)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "같은 값을 Saver 로 저장 → 복원(이 기기에서 계산)", fontSize = 11.sp, color = Color(0xFF616161))
        reports.forEach { r ->
            MonoLine("${r.label}\n  저장 항목 ${r.savedItems} · 복원 서식 ${r.restoredStyles}\n  선택 ${r.restoredSelection} · canUndo=${r.canUndoAfterRestore}")
        }
    }
}

@Composable
private fun FormattedCard(
    title: String,
    color: Color,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF757575), lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StyledTextField(
    state: TextFieldState,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        BasicTextField(
            state = state,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF212121)),
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 2),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SmallActionButton(label: String, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) { Text(text = label, color = Color.White, fontSize = 12.sp) }
}

@Composable
private fun MonoLine(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF616161),
        lineHeight = 15.sp
    )
}

@Composable
private fun DocumentTextField(state: TextFieldState, minLines: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        BasicTextField(
            state = state,
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF212121)),
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = minLines),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "TextFieldState.undoState — undo()/redo()/canUndo/canRedo로 편집 이력 제어. 키보드 입력과 edit{} 프로그래매틱 변경이 동일 히스토리에 누적",
                "selection: TextRange — collapsed=커서, !collapsed=범위 선택. edit{}에서 selection 직접 대입으로 제어",
                "edit{} 블록 안 replace(start, end, str)는 mutating buffer를 다룸 — substring은 외부에서 미리 추출",
                "AnnotatedString 미리보기는 입력 평문 유지 + snapshotFlow 관찰로 분리. 입력 자체를 바꾸는 것은 InputTransformation, 보이는 모양만 바꾸는 것은 OutputTransformation",
                "멀티 커서 시뮬레이션은 오프셋을 뒤에서부터 삽입해야 인덱스가 보존됨 (앞부터 삽입 시 시프트로 깨짐)",
                "서식은 edit{ addStyle(style, range, ExpandPolicy) } 로 상태 안에 붙고, 읽기는 state.textStyles(읽기 전용). state.text 는 서식 없는 평문",
                "범위는 입력 · 삭제에 맞춰 저절로 움직이고 길이가 0 이 되면 사라진다. 경계에 친 글자를 넣을지는 ExpandPolicy 가 정한다",
                "undo/redo 는 살아 있는 서식의 범위만 함께 되돌린다 — 사라진 서식(붙이기를 되돌린 것 · 전부 지운 것)은 다시 만들지 못한다(1.12.1 실측)",
                "기본 TextFieldState.Saver 는 1.12.1 까지 텍스트 · 선택 · undo 이력만 저장했다(재생성 뒤 서식 사라짐). 1.13.0 부터 서식 · ExpandPolicy 까지 저장한다 — 1.12 를 지원해야 할 때만 직접 만든 Saver"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(
                        text = bullet,
                        fontSize = 12.sp,
                        color = Color(0xFF424242),
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
