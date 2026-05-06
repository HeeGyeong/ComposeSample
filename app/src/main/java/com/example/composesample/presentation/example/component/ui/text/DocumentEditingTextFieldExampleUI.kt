package com.example.composesample.presentation.example.component.ui.text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
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
                text = "단순 입력을 넘어 문서 편집기 수준 패턴 4가지: " +
                        "Undo/Redo, Selection 조작, AnnotatedString 미리보기, 멀티 커서 시뮬레이션. " +
                        "모두 단일 진실 원천(TextFieldState) 위에서 동작합니다.",
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
                "AnnotatedString 미리보기는 입력 평문 유지 + snapshotFlow 관찰로 분리. OutputTransformation을 쓰면 입력 자체를 변환 가능",
                "멀티 커서 시뮬레이션은 오프셋을 뒤에서부터 삽입해야 인덱스가 보존됨 (앞부터 삽입 시 시프트로 깨짐)"
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
