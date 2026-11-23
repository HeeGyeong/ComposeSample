package com.example.composesample.presentation.example.component.ui.text

import android.content.ClipData
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLengthReject
import androidx.compose.foundation.text.input.maxLengthTrim
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val MAX_LENGTH = 10

@Composable
fun TextFieldMaxLengthExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "TextField Max Length 버그", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ProblemConceptCard() }
            item { BuggyExampleCard() }
            item { CorrectExampleCard() }
            item { RejectVsTrimCard() }
            item { CodeComparisonCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ProblemConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "문제 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Compose의 새로운 BasicTextField(TextFieldState API)에서 " +
                        "`InputTransformation.maxLengthReject(N)`(1.12 까지 이름은 `maxLength(N)` — 1.13 에서 이름만 바뀌고 동작은 같다)을 " +
                        "적용하면 사용자 키보드 입력은 제한되지만, " +
                        "코드에서 `state.edit { append(...) }` 같은 방식으로 직접 상태를 변경하면 " +
                        "해당 제한이 전혀 적용되지 않습니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "핵심 원인",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100),
                    modifier = Modifier.weight(0.3f)
                )
                Text(
                    text = "InputTransformation은 IME/키보드의 입력 파이프라인에만 연결되어 있어, " +
                            "프로그래매틱 수정 경로에는 호출되지 않음",
                    fontSize = 11.sp,
                    color = Color(0xFF424242),
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}

@Composable
private fun BuggyExampleCard() {
    val state = rememberTextFieldState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "❌ 버그 재현: InputTransformation만 사용",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "최대 $MAX_LENGTH 자. 직접 키보드로 입력하면 제한되지만, 아래 버튼으로 " +
                        "프로그래매틱 변경 시 제한이 무시됩니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            TextFieldBox(state = state, inputTransformation = InputTransformation.maxLengthReject(MAX_LENGTH))

            Spacer(modifier = Modifier.height(8.dp))
            LengthIndicator(current = state.text.length, isOver = state.text.length > MAX_LENGTH)

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        state.edit {
                            replace(0, length, "This is a long text that exceeds limit")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(text = "긴 텍스트 주입", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { state.edit { replace(0, length, "") } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text(text = "Clear", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CorrectExampleCard() {
    val state = rememberTextFieldState()

    // LaunchedEffect + snapshotFlow: 상태가 변경될 때마다 길이를 관찰하여
    // 키보드 입력이든 프로그래매틱 변경이든 모두 강제로 자름
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .collectLatest { current ->
                if (current.length > MAX_LENGTH) {
                    // 초과분을 빈 문자열로 교체하여 제거
                    state.edit {
                        replace(MAX_LENGTH, length, "")
                    }
                }
            }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✅ 올바른 해결: LaunchedEffect + snapshotFlow",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "snapshotFlow로 상태를 관찰하여 입력 경로와 무관하게 길이를 강제합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            TextFieldBox(state = state, inputTransformation = null)

            Spacer(modifier = Modifier.height(8.dp))
            LengthIndicator(current = state.text.length, isOver = state.text.length > MAX_LENGTH)

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        state.edit {
                            replace(0, length, "This is a long text that exceeds limit")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text(text = "긴 텍스트 주입", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { state.edit { replace(0, length, "") } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text(text = "Clear", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 1.13 에서 maxLength 가 둘로 나뉜 것 — Reject(넘치면 그 입력 전체를 되돌림, 옛 maxLength 와 같은 클래스) /
 * Trim(넘치는 만큼만 잘라 넣음). 한 글자씩 칠 때는 둘 다 MAX_LENGTH 에서 멈추고, 여러 글자가 한 번에 들어오는 붙여넣기에서 갈린다.
 */
@Composable
private fun RejectVsTrimCard() {
    val rejectState = rememberTextFieldState()
    val trimState = rememberTextFieldState()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1.13: maxLengthReject vs maxLengthTrim",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "한 글자씩 칠 때는 둘 다 $MAX_LENGTH 자에서 멈춘다. 갈리는 곳은 여러 글자가 한 번에 들어오는 붙여넣기 — " +
                        "'8자 채우기' → '12345' 복사 → 각 칸을 길게 눌러 붙여넣기: Reject 는 붙여넣기 전체를 되돌려 8자 그대로, " +
                        "Trim 은 앞의 2자만 넣어 $MAX_LENGTH 자.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 준비용 프로그래매틱 입력(8 < 10 이라 우회 여부와 무관)
                        rejectState.setTextAndPlaceCursorAtEnd("abcdefgh")
                        trimState.setTextAndPlaceCursorAtEnd("abcdefgh")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(text = "8자 채우기", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(ClipData.newPlainText("maxLength", "12345").toClipEntry())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text(text = "'12345' 복사", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "maxLengthReject($MAX_LENGTH)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
            Spacer(modifier = Modifier.height(4.dp))
            TextFieldBox(state = rejectState, inputTransformation = InputTransformation.maxLengthReject(MAX_LENGTH))
            Spacer(modifier = Modifier.height(4.dp))
            LengthIndicator(current = rejectState.text.length, isOver = rejectState.text.length > MAX_LENGTH)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "maxLengthTrim($MAX_LENGTH)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
            Spacer(modifier = Modifier.height(4.dp))
            TextFieldBox(state = trimState, inputTransformation = InputTransformation.maxLengthTrim(MAX_LENGTH))
            Spacer(modifier = Modifier.height(4.dp))
            LengthIndicator(current = trimState.text.length, isOver = trimState.text.length > MAX_LENGTH)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "둘 다 InputTransformation 이라 위 '긴 텍스트 주입'처럼 state.edit {} 로 바꾸면 그대로 넘친다 — " +
                        "프로그래매틱 수정까지 막으려면 여전히 snapshotFlow 방식.",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun TextFieldBox(
    state: TextFieldState,
    inputTransformation: InputTransformation?
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
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF212121)),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LengthIndicator(current: Int, isOver: Boolean) {
    val color = if (isOver) Color(0xFFD32F2F) else Color(0xFF388E3C)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$current / $MAX_LENGTH",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        if (isOver) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "⚠ 제한 초과!",
                fontSize = 11.sp,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CodeComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "코드 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                title = "❌ 잘못된 방법",
                code = "val state = rememberTextFieldState()\n" +
                        "\n" +
                        "BasicTextField(\n" +
                        "    state = state,\n" +
                        "    inputTransformation =\n" +
                        "        InputTransformation.maxLengthReject(10)\n" +
                        "        // 1.12 까지: maxLength(10)\n" +
                        ")\n" +
                        "\n" +
                        "// 프로그래매틱 수정은 제한 우회!\n" +
                        "state.edit {\n" +
                        "    replace(0, length, \"20자 이상 텍스트\")\n" +
                        "}",
                borderColor = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                title = "✅ 올바른 방법",
                code = "val state = rememberTextFieldState()\n" +
                        "\n" +
                        "LaunchedEffect(state) {\n" +
                        "    snapshotFlow { state.text.toString() }\n" +
                        "        .collectLatest { current ->\n" +
                        "            if (current.length > MAX) {\n" +
                        "                state.edit {\n" +
                        "                    replace(MAX, length, \"\")\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "}",
                borderColor = Color(0xFF388E3C)
            )
        }
    }
}

@Composable
private fun CodeBlock(title: String, code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = borderColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
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
                "InputTransformation은 IME/키보드 경로에만 호출됨 — state.edit {} 같은 프로그래매틱 수정은 통과되지 않음",
                "1.13: maxLength(N) 은 deprecated — 같은 동작은 maxLengthReject(N)(넘치면 그 입력 전체를 되돌림), 새로 생긴 maxLengthTrim(N) 은 넘치는 만큼만 잘라 넣음. 둘 다 InputTransformation 이라 위 우회는 그대로",
                "단일 진실 원천(TextFieldState)을 스냅샷으로 관찰(snapshotFlow)하면 입력 경로와 무관하게 불변식(invariant)을 강제할 수 있음",
                "collectLatest로 빠른 연속 변경 시 이전 작업을 취소 — 불필요한 재진입 방지",
                "외부 제약(길이, 패턴, 포맷 등) 강제 로직은 UI Transformation이 아닌 상태 관찰 레이어에 두는 것이 안전함"
            )
            bullets.forEach { bullet ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
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
