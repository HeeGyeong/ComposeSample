package com.example.composesample.presentation.example.component.ui.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// 다크 테마 토큰 색상
private val BgDark = Color(0xFF1E1E1E)
private val FgDefault = Color(0xFFD4D4D4)
private val FgKeyword = Color(0xFFC586C0)      // 보라
private val FgType = Color(0xFF4EC9B0)         // 청록
private val FgString = Color(0xFFCE9178)       // 주황
private val FgNumber = Color(0xFFB5CEA8)       // 연두
private val FgComment = Color(0xFF6A9955)      // 녹
private val FgFunction = Color(0xFFDCDCAA)     // 노랑
private val FgAnnotation = Color(0xFF9CDCFE)   // 하늘

// 토큰 타입
private enum class TokenType { KEYWORD, TYPE, STRING, NUMBER, COMMENT, FUNCTION, ANNOTATION, DEFAULT }

private data class Token(val range: IntRange, val type: TokenType)

// 우선순위 순서대로 매칭되는 패턴 — 먼저 매칭된 영역은 이후 패턴이 침범하지 못함
private val patterns: List<Pair<Regex, TokenType>> = listOf(
    Regex("""//[^\n]*""") to TokenType.COMMENT,
    Regex("""/\*[\s\S]*?\*/""") to TokenType.COMMENT,
    Regex(""""(?:[^"\\]|\\.)*"""") to TokenType.STRING,
    Regex("""@[A-Za-z_][A-Za-z0-9_]*""") to TokenType.ANNOTATION,
    Regex("""\b(fun|val|var|class|object|interface|if|else|when|for|while|return|import|package|private|public|internal|protected|companion|data|sealed|override|suspend|inline|open|abstract|in|is|as|null|true|false|this|by)\b""") to TokenType.KEYWORD,
    Regex("""\b(Int|String|Boolean|Long|Float|Double|Unit|List|Map|Set|Array|Any|Nothing|Composable)\b""") to TokenType.TYPE,
    Regex("""\b\d+(?:\.\d+)?[fFLl]?\b""") to TokenType.NUMBER,
    Regex("""\b[a-z_][A-Za-z0-9_]*(?=\s*\()""") to TokenType.FUNCTION,
)

private fun TokenType.color(): Color = when (this) {
    TokenType.KEYWORD -> FgKeyword
    TokenType.TYPE -> FgType
    TokenType.STRING -> FgString
    TokenType.NUMBER -> FgNumber
    TokenType.COMMENT -> FgComment
    TokenType.FUNCTION -> FgFunction
    TokenType.ANNOTATION -> FgAnnotation
    TokenType.DEFAULT -> FgDefault
}

// 정규식 토크나이저 — 우선순위 패턴 순서대로 매칭, 이미 점유된 영역은 스킵
private fun tokenize(source: String): List<Token> {
    val occupied = BooleanArray(source.length)
    val tokens = mutableListOf<Token>()

    for ((regex, type) in patterns) {
        for (match in regex.findAll(source)) {
            val range = match.range
            // 점유 영역 충돌 검사
            var conflict = false
            for (i in range) {
                if (occupied[i]) { conflict = true; break }
            }
            if (conflict) continue
            for (i in range) occupied[i] = true
            tokens.add(Token(range, type))
        }
    }
    return tokens.sortedBy { it.range.first }
}

// 토큰 리스트로 AnnotatedString 빌드
private fun highlight(source: String): AnnotatedString {
    val tokens = tokenize(source)
    return AnnotatedString.Builder(source).apply {
        for (token in tokens) {
            addStyle(
                style = SpanStyle(color = token.type.color()),
                start = token.range.first,
                end = token.range.last + 1
            )
        }
    }.toAnnotatedString()
}

@Composable
fun SyntaxHighlightingExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Syntax Highlighting (간소화 데모)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { StaticHighlightCard() }
            item { LiveEditingHighlightCard() }
            item { TokenLegendCard() }
            item { LimitsCard() }
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
                text = "AnnotatedString + 정규식 토크나이저",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "외부 라이브러리 없이 Kotlin 코드를 하이라이팅하는 미니 데모. " +
                        "우선순위가 정해진 정규식 패턴을 차례로 매칭하고, 이미 점유된 인덱스는 " +
                        "후순위 패턴이 침범하지 못하도록 BooleanArray로 마스킹합니다. " +
                        "결과 토큰은 SpanStyle로 AnnotatedString에 적용됩니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
        }
    }
}

private val sampleCode = """
// Kotlin 샘플 코드
@Composable
fun Greeting(name: String, count: Int = 1) {
    val message = "Hello, ${'$'}name!"
    repeat(count) { i ->
        println("[${'$'}i] ${'$'}message")
    }
}

class User(val id: Long, val name: String) {
    fun isValid(): Boolean = id > 0L && name.isNotEmpty()
}
""".trimIndent()

@Composable
private fun StaticHighlightCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. 정적 코드 하이라이팅",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "고정된 샘플 코드를 토크나이징하여 다크 테마로 표시합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val annotated = remember { highlight(sampleCode) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = annotated,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = FgDefault,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun LiveEditingHighlightCard() {
    val state = rememberTextFieldState(initialText = "fun main() {\n    val x: Int = 42\n    println(\"x=\$x\")\n}")
    var preview by remember { mutableStateOf(highlight(state.text.toString())) }

    // snapshotFlow로 입력 변화를 관찰해 미리보기 갱신
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .collect { text -> preview = highlight(text) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. 라이브 편집 + 미리보기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "입력 영역은 평문 TextField, 미리보기 영역은 AnnotatedString으로 하이라이팅. " +
                        "snapshotFlow로 변경을 감지하여 토크나이징 결과를 갱신합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            EditorField(state = state)

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        state.edit { replace(0, length, "data class Point(val x: Int, val y: Int)") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) { Text(text = "data class", color = Color.White, fontSize = 12.sp) }
                Button(
                    onClick = {
                        state.edit { replace(0, length, "// 주석 줄\nval pi = 3.14f") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) { Text(text = "주석 + 숫자", color = Color.White, fontSize = 12.sp) }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "▼ 미리보기",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = preview,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = FgDefault,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun EditorField(state: TextFieldState) {
    BasicTextField(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        textStyle = TextStyle(
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121)
        )
    )
}

@Composable
private fun TokenLegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. 토큰 색상 범례",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val legend = listOf(
                "KEYWORD" to FgKeyword,
                "TYPE" to FgType,
                "STRING" to FgString,
                "NUMBER" to FgNumber,
                "COMMENT" to FgComment,
                "FUNCTION" to FgFunction,
                "ANNOTATION" to FgAnnotation,
                "DEFAULT" to FgDefault,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                legend.forEach { (name, color) ->
                    Text(
                        text = name,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LimitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "4. 한계와 트레이드오프",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• 정규식 기반은 컨텍스트 무지(context-free) — 중첩 문자열 보간, " +
                        "타입 인자 < >, 람다 안 식별자 의미 등은 정확히 분류 불가\n" +
                        "• 큰 파일에서는 매번 전체 토크나이징이 부담 — 변경 라인 단위 증분 토크나이징 필요\n" +
                        "• 정확한 색상화가 필요하다면 Shiki(TextMate Grammar 기반) / " +
                        "TreeSitter 같은 본격 엔진 검토\n" +
                        "• 본 데모는 키워드/타입/주석/문자열/숫자/함수호출/어노테이션 정도까지만 식별",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
        }
    }
}
