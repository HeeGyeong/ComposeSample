package com.example.composesample.presentation.example.component.architecture.development.language

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// 컬렉션 리터럴 동등 코드 — 새 문법 [1, 2, 3] 대신 현재 stable listOf/setOf/mapOf 로 동일 결과를 실제 실행
private val sampleNumbers = listOf(1, 2, 3, 4, 5)
private val sampleSet = setOf("kotlin", "compose", "android")
private val sampleMap = mapOf("min" to 24, "target" to 35)

// 컨텍스트 파라미터 동등 코드 — context(logger) 대신 명시적 파라미터로 동일 동작을 실제 실행
private class SampleLogger {
    val lines = mutableListOf<String>()
    fun log(message: String) {
        lines.add("[LOG] $message")
    }
}

// context(logger: SampleLogger) fun greet(...) 와 동등 — 컨텍스트를 명시적 인자로 전달
private fun greetWithExplicit(logger: SampleLogger, name: String): String {
    logger.log("greet 호출: $name")
    return "Hello, $name"
}

@Composable
fun Kotlin24FeaturesExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Kotlin 2.4 Language Features",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { CollectionLiteralsCard() }
            item { ContextParametersCard() }
            item { SyntaxComparisonCard() }
            item { CompilerFlagCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kotlin 2.4 언어 기능 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kotlin 2.4는 두 가지 주목할 만한 문법 개선을 도입합니다. " +
                        "컬렉션 리터럴(Swift 스타일 [ ] 문법)과 컨텍스트 파라미터(context(...))입니다. " +
                        "둘 다 Experimental 단계로, 사용하려면 예제 모듈 수준의 컴파일러 opt-in 이 필요합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val features = listOf(
                Triple("Collection Literals", "[1, 2, 3]", "listOf/setOf 대신 대괄호 리터럴"),
                Triple("Context Parameters", "context(logger: Logger)", "수신 컨텍스트를 암시적으로 주입")
            )
            features.forEach { (label, syntax, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.34f))
                    Text(text = syntax, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2), modifier = Modifier.weight(0.36f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.3f))
                }
            }
        }
    }
}

@Composable
private fun CollectionLiteralsCard() {
    // 새 문법은 CodeBlock 으로 설명하고, 동등한 stable 코드(listOf/setOf/mapOf)를 실제 실행해 결과를 표시
    val listResult = remember { sampleNumbers.toString() }
    val setResult = remember { sampleSet.toString() }
    val mapResult = remember { sampleMap.toString() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "① Collection Literals",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Swift처럼 대괄호로 컬렉션을 생성합니다. 기대 타입에 따라 List/Set/Map 으로 추론되며, " +
                        "커스텀 타입은 companion 의 operator fun of(...) 로 리터럴을 지원할 수 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// Kotlin 2.4 컬렉션 리터럴 (Experimental)\n" +
                        "val numbers: List<Int> = [1, 2, 3, 4, 5]\n" +
                        "val tags: Set<String> = [\"kotlin\", \"compose\", \"android\"]\n" +
                        "val sdk: Map<String, Int> = [\"min\" to 24, \"target\" to 35]\n\n" +
                        "// 기존(stable) — 본 예제가 실제 실행하는 동등 코드\n" +
                        "val numbers = listOf(1, 2, 3, 4, 5)\n" +
                        "val tags = setOf(\"kotlin\", \"compose\", \"android\")\n" +
                        "val sdk = mapOf(\"min\" to 24, \"target\" to 35)",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            ResultRow(label = "List [..]", value = listResult)
            Spacer(modifier = Modifier.height(4.dp))
            ResultRow(label = "Set [..]", value = setResult)
            Spacer(modifier = Modifier.height(4.dp))
            ResultRow(label = "Map [..]", value = mapResult)
        }
    }
}

@Composable
private fun ContextParametersCard() {
    // context(logger) 와 동등한 명시적 파라미터 코드를 실제 실행
    val logger = remember { SampleLogger() }
    val greeting = remember { greetWithExplicit(logger, "Compose") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "② Context Parameters",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "함수 시그니처에 context(logger: Logger) 를 선언하면 호출부에 로거가 컨텍스트로 존재할 때 " +
                        "암시적으로 주입됩니다. 명시적 인자 전달의 보일러플레이트를 줄이며, deprecated 된 " +
                        "context receivers 를 대체합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// Kotlin 2.4 컨텍스트 파라미터 (Experimental)\n" +
                        "context(logger: Logger)\n" +
                        "fun greet(name: String): String {\n" +
                        "    logger.log(\"greet 호출: \$name\")\n" +
                        "    return \"Hello, \$name\"\n" +
                        "}\n\n" +
                        "with(logger) { greet(\"Compose\") } // logger 암시적 주입\n\n" +
                        "// 기존(stable) — 본 예제가 실제 실행하는 동등 코드\n" +
                        "fun greet(logger: Logger, name: String): String { ... }\n" +
                        "greet(logger, \"Compose\")",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            ResultRow(label = "반환값", value = greeting)
            Spacer(modifier = Modifier.height(4.dp))
            logger.lines.forEach { line ->
                ResultRow(label = "로거 출력", value = line)
            }
        }
    }
}

@Composable
private fun SyntaxComparisonCard() {
    var selectedCase by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Before / After 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("리스트 중첩", "커스텀 타입", "컨텍스트 전파").forEachIndexed { index, label ->
                    Button(
                        onClick = { selectedCase = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCase == index) Color(0xFF1976D2) else Color(0xFFE0E0E0)
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (selectedCase == index) Color.White else Color(0xFF424242),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            when (selectedCase) {
                0 -> CodeBlock(
                    code = "// ❌ 기존: 중첩 listOf 가 장황함\n" +
                            "val matrix = listOf(\n" +
                            "    listOf(1, 2, 3),\n" +
                            "    listOf(4, 5, 6)\n" +
                            ")\n\n" +
                            "// ✅ 2.4: 대괄호 리터럴로 간결\n" +
                            "val matrix = [[1, 2, 3], [4, 5, 6]]",
                    borderColor = Color(0xFF1976D2)
                )
                1 -> CodeBlock(
                    code = "// 커스텀 타입에 리터럴 지원 부여\n" +
                            "class Vector(val data: IntArray) {\n" +
                            "    companion object {\n" +
                            "        operator fun of(vararg xs: Int) =\n" +
                            "            Vector(xs)\n" +
                            "    }\n" +
                            "}\n\n" +
                            "val v: Vector = [10, 20, 30] // of(10,20,30)",
                    borderColor = Color(0xFF1976D2)
                )
                2 -> CodeBlock(
                    code = "// ❌ 기존: logger 를 매 함수마다 전달\n" +
                            "fun save(logger: Logger, item: Item) { ... }\n" +
                            "fun load(logger: Logger, id: Int) { ... }\n\n" +
                            "// ✅ 2.4: context 로 한 번 선언, 호출부 간결\n" +
                            "context(logger: Logger) fun save(item: Item)\n" +
                            "context(logger: Logger) fun load(id: Int)\n" +
                            "with(logger) { save(item); load(1) }",
                    borderColor = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
private fun CompilerFlagCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "컴파일러 opt-in",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "두 기능 모두 Experimental 이라 명시적 활성화가 필요합니다. " +
                        "본 예제는 새 문법을 코드 블록으로만 보여주고, 실제 실행은 동등한 stable 코드로 대체합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// build.gradle(.kts) — 예제 모듈에만 국소 적용\n" +
                        "kotlin {\n" +
                        "    compilerOptions {\n" +
                        "        freeCompilerArgs.addAll(\n" +
                        "            \"-Xcollection-literals\",\n" +
                        "            \"-Xcontext-parameters\"\n" +
                        "        )\n" +
                        "    }\n" +
                        "}",
                borderColor = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚠ 언어 기능을 프로젝트 전역에 적용하면 기존 코드(예: listOf 기대 위치의 [ ] 모호성, " +
                            "context receivers 마이그레이션)와 충돌할 수 있습니다. 예제 파일 단위로만 opt-in 하세요.",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    lineHeight = 16.sp
                )
            }
        }
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
                "컬렉션 리터럴 [ ] 은 기대 타입으로 List/Set/Map 을 추론 — 중첩 구조에서 가독성 향상",
                "커스텀 타입은 companion 의 operator fun of(...) 로 리터럴을 지원할 수 있음",
                "컨텍스트 파라미터 context(x: T) 는 암시적 의존성 주입으로 인자 전달 보일러플레이트 제거",
                "context parameters 는 deprecated 된 context receivers 의 후속 — 변수명을 명시해 모호성 해소",
                "두 기능 모두 Experimental — 전역 적용 금지, 예제 파일 단위 컴파일러 opt-in 권장"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "✅ $label", fontSize = 12.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF388E3C))
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}
