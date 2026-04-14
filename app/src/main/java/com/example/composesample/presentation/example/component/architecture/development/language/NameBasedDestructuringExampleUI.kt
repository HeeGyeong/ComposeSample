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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
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

// 예제용 data class — 같은 패키지의 User와 충돌 방지를 위해 Sample 접두사 사용
private data class SampleUser(val username: String, val email: String)

@Composable
fun NameBasedDestructuringExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Name-Based Destructuring",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { PositionBasedBugCard() }
            item { NameBasedSolutionCard() }
            item { SyntaxComparisonCard() }
            item { CompilerModesCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Name-Based Destructuring 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kotlin 2.3.20의 실험적 기능으로, 구조 분해 선언 시 위치(componentN) 대신 " +
                        "프로퍼티 이름으로 변수를 매칭합니다. 프로퍼티 순서가 변경되어도 올바른 값이 할당됩니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val comparisons = listOf(
                Triple("위치 기반 (기존)", "val (a, b) = obj", "순서 의존 — 리팩토링 위험"),
                Triple("이름 기반 (2.3.20)", "val (a = propA, b = propB) = obj", "이름 매칭 — 순서 무관")
            )
            comparisons.forEach { (label, syntax, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.3f))
                    Text(text = syntax, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2), modifier = Modifier.weight(0.4f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.3f))
                }
            }
        }
    }
}

@Composable
private fun PositionBasedBugCard() {
    val user = remember { SampleUser("alice", "alice@example.com") }
    // 위치 기반 구조 분해 — 변수명이 프로퍼티명과 일치하지 않아 값이 뒤바뀜
    // component1() = username = "alice", component2() = email = "alice@example.com"
    val (email, username) = user

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "❌ 위치 기반 구조 분해의 함정",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "변수명을 email, username 순서로 선언하면 component1()=username, " +
                        "component2()=email이 할당되어 값이 뒤바뀝니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "data class SampleUser(\n" +
                        "    val username: String,  // component1()\n" +
                        "    val email: String       // component2()\n" +
                        ")\n\n" +
                        "val (email, username) = user\n" +
                        "// email = \"alice\"        ← 실제로는 username!\n" +
                        "// username = \"alice@...\" ← 실제로는 email!",
                borderColor = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(12.dp))

            ResultRow(label = "email 변수값", value = email, expected = "alice@example.com", isCorrect = false)
            Spacer(modifier = Modifier.height(4.dp))
            ResultRow(label = "username 변수값", value = username, expected = "alice", isCorrect = false)
        }
    }
}

@Composable
private fun NameBasedSolutionCard() {
    val user = remember { SampleUser("alice", "alice@example.com") }
    // 이름 기반 구조 분해 해결 — 직접 프로퍼티 접근으로 안전하게 값 추출
    val mail = user.email
    val name = user.username

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✅ 이름 기반 구조 분해 (Kotlin 2.3.20)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "= 뒤에 프로퍼티 이름을 명시하면 순서와 무관하게 올바른 값이 매칭됩니다. " +
                        "(Experimental — 컴파일러 플래그 필요)",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// Kotlin 2.3.20 Name-Based Destructuring\n" +
                        "// 컴파일러 플래그: -Xname-based-destructuring=complete\n\n" +
                        "val (mail = email, name = username) = user\n" +
                        "// mail → user.email = \"alice@example.com\" ✅\n" +
                        "// name → user.username = \"alice\"          ✅\n\n" +
                        "// 위치 기반이 필요하면 [] 구문 사용\n" +
                        "val [first, second] = user  // component1(), component2()",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            ResultRow(label = "mail 변수값", value = mail, expected = "alice@example.com", isCorrect = true)
            Spacer(modifier = Modifier.height(4.dp))
            ResultRow(label = "name 변수값", value = name, expected = "alice", isCorrect = true)
        }
    }
}

@Composable
private fun SyntaxComparisonCard() {
    var selectedCase by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "구문 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("API 응답", "좌표 변환", "Map 엔트리").forEachIndexed { index, label ->
                    Button(
                        onClick = { selectedCase = index },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedCase == index) Color(0xFF1976D2) else Color(0xFFE0E0E0)
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
                    code = "data class ApiResponse(\n" +
                            "    val status: Int,\n" +
                            "    val message: String,\n" +
                            "    val data: String\n" +
                            ")\n\n" +
                            "// ❌ 위치 기반: 순서 기억 필요\n" +
                            "val (status, message, data) = response\n\n" +
                            "// ✅ 이름 기반: 이름으로 매칭\n" +
                            "val (code = status, msg = message,\n" +
                            "     body = data) = response",
                    borderColor = Color(0xFF1976D2)
                )
                1 -> CodeBlock(
                    code = "data class Point(val x: Float, val y: Float)\n\n" +
                            "// ❌ 위치 기반: x, y 뒤바뀔 수 있음\n" +
                            "val (y, x) = point  // 실수로 뒤바뀜!\n\n" +
                            "// ✅ 이름 기반: 항상 안전\n" +
                            "val (px = x, py = y) = point",
                    borderColor = Color(0xFF1976D2)
                )
                2 -> CodeBlock(
                    code = "// Map 엔트리 구조 분해에도 적용 가능\n" +
                            "for ((k = key, v = value) in map) {\n" +
                            "    println(\"\$k: \$v\")\n" +
                            "}\n\n" +
                            "// 기존 위치 기반도 유지\n" +
                            "for ([k, v] in map) { ... }  // [] = 위치 기반",
                    borderColor = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
private fun CompilerModesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "컴파일러 모드 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            CodeBlock(
                code = "// config.gradle 또는 build.gradle.kts\n" +
                        "kotlin {\n" +
                        "    compilerOptions {\n" +
                        "        freeCompilerArgs.add(\n" +
                        "            \"-Xname-based-destructuring=complete\"\n" +
                        "        )\n" +
                        "    }\n" +
                        "}",
                borderColor = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val modes = listOf(
                Triple("only-syntax", "명시적 형태만 허용", "기존 코드 영향 없음. 점진적 마이그레이션에 안전"),
                Triple("name-mismatch", "이름 불일치 시 경고", "기존 위치 기반도 동작하지만 불일치를 감지"),
                Triple("complete", "이름 기반이 기본 동작", "위치 기반은 [] 구문으로 전환. 가장 급진적")
            )
            modes.forEach { (mode, behavior, note) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(text = mode, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2))
                    Text(text = behavior, fontSize = 12.sp, color = Color(0xFF212121))
                    Text(text = note, fontSize = 11.sp, color = Color(0xFF757575))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚠ complete 모드는 기존 Pair/Triple 구조 분해와 충돌할 수 있어 " +
                            "프로젝트 전역 적용 전 영향 분석이 필요합니다.",
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
        elevation = 4.dp,
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
                "위치 기반 구조 분해는 프로퍼티 순서에 의존 → 리팩토링 시 조용한 버그 유발 가능",
                "이름 기반 구조 분해는 val (local = property) 형태로 프로퍼티를 명시적으로 매칭",
                "complete 모드에서 기존 위치 기반은 val [a, b] = obj 구문으로 유지",
                "현재 Experimental 상태 — Kotlin 팀은 향후 이름 기반을 기본으로 전환할 예정",
                "프로젝트 적용 시 기존 Pair/Triple/when 구조 분해와의 충돌 여부를 사전 확인할 것"
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
private fun ResultRow(label: String, value: String, expected: String, isCorrect: Boolean) {
    val bgColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor = if (isCorrect) Color(0xFF388E3C) else Color(0xFFD32F2F)
    val icon = if (isCorrect) "✅" else "❌"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$icon $label", fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
        Text(text = "\"$value\"", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = textColor)
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
