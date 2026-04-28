package com.example.composesample.presentation.example.component.architecture.development.preview

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun PreviewOnlyAnnotationExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        MainHeader(title = "Preview-only Composable (@RequiresOptIn)", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PreviewOnlySectionTitle("1. 문제: Preview Helper의 프로덕션 유출") }
            item { ProblemCard() }

            item { PreviewOnlySectionTitle("2. 해결책: @RequiresOptIn 어노테이션 정의") }
            item { SolutionAnnotationCard() }

            item { PreviewOnlySectionTitle("3. Preview 전용 Composable에 적용") }
            item { ApplyAnnotationCard() }

            item { PreviewOnlySectionTitle("4. @Preview 함수에서 @OptIn으로 허용") }
            item { OptInUsageCard() }

            item { PreviewOnlySectionTitle("5. RequiresOptIn.Level 비교") }
            item { LevelComparisonCard() }

            item { PreviewOnlySectionTitle("6. LocalInspectionMode와의 차이") }
            item { ComparisonWithInspectionModeCard() }

            item { PreviewOnlySectionTitle("7. 실무 적용 패턴") }
            item { PracticalPatternsCard() }
        }
    }
}

// ── Section 1: 문제 정의 ─────────────────────────────────────────────────────

@Composable
private fun ProblemCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Preview 전용 Helper Composable이 프로덕션 코드에서 실수로 호출될 수 있습니다.",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 문제 코드: 제약 없는 PreviewHelper
            CodeBlock(
                label = "❌ 문제: 어디서든 호출 가능",
                labelColor = Color(0xFFFF6B6B),
                code = """// Preview를 위한 목업 데이터를 포함한 Helper
@Composable
fun UserCardPreviewHelper(
    user: User = User.mock() // 목업 팩토리
) {
    UserCard(user = user)
}

// ⚠ 실수로 프로덕션에서 호출해도 컴파일 성공
@Composable
fun HomeScreen() {
    UserCardPreviewHelper() // 컴파일 에러 없음!
}"""
            )

            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                text = "UserCardPreviewHelper는 Preview용 목업 데이터를 포함하지만, " +
                        "컴파일러는 이를 알 수 없어 프로덕션 호출을 막지 못합니다.",
                color = Color(0xFFFF6B6B)
            )
        }
    }
}

// ── Section 2: 어노테이션 정의 ───────────────────────────────────────────────

@Composable
private fun SolutionAnnotationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "@RequiresOptIn으로 커스텀 opt-in 어노테이션을 만듭니다. " +
                        "이를 적용한 API는 @OptIn 없이 호출하면 컴파일 에러가 발생합니다.",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                label = "✅ @PreviewOnly 어노테이션 정의",
                labelColor = Color(0xFF50C878),
                code = """@RequiresOptIn(
    message = "이 Composable은 Preview 전용입니다. " +
              "프로덕션 코드에서 사용하지 마세요.",
    level = RequiresOptIn.Level.ERROR  // 컴파일 에러
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS
)
annotation class PreviewOnly"""
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 속성 설명
            val properties = listOf(
                Triple("message", "IDE와 컴파일러가 표시할 경고/에러 메시지", Color(0xFF4A90D9)),
                Triple("level", "ERROR(컴파일 중단) 또는 WARNING(경고만)", Color(0xFF7B68EE)),
                Triple("@Retention(BINARY)", "바이트코드에 유지 → 다른 모듈에서도 적용", Color(0xFFFF8C00)),
                Triple("@Target", "어노테이션 적용 대상 지정", Color(0xFF50C878))
            )
            properties.forEach { (prop, desc, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFF383838), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = prop,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = color,
                        modifier = Modifier.width(130.dp)
                    )
                    Text(text = desc, fontSize = 11.sp, color = Color(0xFFAAAAAA), lineHeight = 16.sp)
                }
            }
        }
    }
}

// ── Section 3: 어노테이션 적용 ───────────────────────────────────────────────

@Composable
private fun ApplyAnnotationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            CodeBlock(
                label = "Preview 전용 Composable에 @PreviewOnly 적용",
                labelColor = Color(0xFF50C878),
                code = """@PreviewOnly  // ← 이 함수는 Preview에서만 허용
@Composable
fun UserCardPreviewHelper(
    user: User = User.mock()
) {
    UserCard(user = user)
}

// ❌ 이제 프로덕션에서 호출하면 컴파일 에러!
@Composable
fun HomeScreen() {
    UserCardPreviewHelper() // Error: This declaration
                            // is opt-in and its usage
                            // must be marked with...
}"""
            )

            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                text = "@PreviewOnly가 적용된 함수는 @OptIn(PreviewOnly::class) 없이 호출하면 " +
                        "즉시 컴파일 에러가 발생합니다. 실수 자체가 불가능해집니다.",
                color = Color(0xFF50C878)
            )
        }
    }
}

// ── Section 4: @OptIn 사용법 ─────────────────────────────────────────────────

@Composable
private fun OptInUsageCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "@Preview 함수에서 @OptIn으로 명시적 허용 의도를 표현합니다.",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                label = "✅ @Preview 함수에서 올바른 사용",
                labelColor = Color(0xFF4A90D9),
                code = """// @OptIn으로 PreviewOnly API 사용 허용 선언
@OptIn(PreviewOnly::class)
@Preview(showBackground = true)
@Composable
fun UserCardPreview() {
    UserCardPreviewHelper() // ✅ 컴파일 성공
}

// 여러 Preview를 하나의 어노테이션으로 묶을 때도 동일하게
@OptIn(PreviewOnly::class)
@PreviewLightDark
@Composable
fun UserCardDarkPreview() {
    UserCardPreviewHelper()
}"""
            )

            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                text = "@OptIn은 '나는 이 API가 Preview 전용임을 알고 의도적으로 사용합니다'라는 " +
                        "명시적 선언입니다. 코드 리뷰에서도 의도가 명확해집니다.",
                color = Color(0xFF4A90D9)
            )
        }
    }
}

// ── Section 5: Level 비교 ────────────────────────────────────────────────────

@Composable
private fun LevelComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val levels = listOf(
                Triple(
                    "RequiresOptIn.Level.ERROR",
                    "호출 시 컴파일 에러 → 빌드 자체가 실패.\n실수를 원천 차단할 때 사용. Preview 전용 API에 권장.",
                    Color(0xFFFF6B6B)
                ),
                Triple(
                    "RequiresOptIn.Level.WARNING",
                    "호출 시 경고만 발생 → 빌드는 성공.\n실험적 API처럼 '주의하되 사용 가능'한 경우에 적합.",
                    Color(0xFFFFD700)
                )
            )
            levels.forEach { (level, desc, color) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFF383838), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = level,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = desc, fontSize = 12.sp, color = Color(0xFFAAAAAA), lineHeight = 17.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            InfoBox(
                text = "Preview 전용 Composable에는 ERROR 레벨을 사용하세요. " +
                        "실수로 프로덕션에 포함될 경우 런타임 버그로 이어질 수 있기 때문입니다.",
                color = Color(0xFFFF8C00)
            )
        }
    }
}

// ── Section 6: LocalInspectionMode와 비교 ────────────────────────────────────

@Composable
private fun ComparisonWithInspectionModeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val comparisons = listOf(
                listOf("구분", "@RequiresOptIn\n(@PreviewOnly)", "LocalInspectionMode"),
                listOf("감지 시점", "컴파일 타임", "런타임"),
                listOf("용도", "호출 자체를 막음", "조건부 로직 분기"),
                listOf("실수 방지", "완전 차단", "런타임에만 감지"),
                listOf("코드 예시", "@PreviewOnly\nfun Helper() {...}", "if (LocalInspection\nMode.current) {...}")
            )

            comparisons.forEachIndexed { index, row ->
                val isHeader = index == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isHeader) Color(0xFF383838) else Color(0xFF2D2D2D),
                            RoundedCornerShape(if (isHeader) 6.dp else 0.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        val color = when {
                            isHeader -> Color(0xFFCCCCCC)
                            colIndex == 1 -> Color(0xFF50C878)
                            colIndex == 2 -> Color(0xFF4A90D9)
                            else -> Color(0xFF888888)
                        }
                        Text(
                            text = cell,
                            fontSize = if (isHeader) 11.sp else 11.sp,
                            color = color,
                            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            lineHeight = 15.sp,
                            fontFamily = if (colIndex > 0 && !isHeader) FontFamily.Monospace else FontFamily.Default
                        )
                    }
                }
                if (!isHeader) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(Color(0xFF444444))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            InfoBox(
                text = "두 방법은 상호 보완적입니다. @RequiresOptIn으로 호출을 막고, " +
                        "LocalInspectionMode로 Preview 환경별 런타임 분기를 처리하세요.",
                color = Color(0xFF7B68EE)
            )
        }
    }
}

// ── Section 7: 실무 패턴 ─────────────────────────────────────────────────────

@Composable
private fun PracticalPatternsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val patterns = listOf(
                "목업 데이터 공급 함수" to """@PreviewOnly
@Composable
fun ArticleListPreview() {
    // 실제 DB/API 호출 없이 목업 리스트 제공
    ArticleList(articles = Article.mockList())
}""",
                "Preview 전용 래퍼" to """@PreviewOnly
@Composable
fun AppThemePreviewWrapper(
    content: @Composable () -> Unit
) {
    // 테마, DI, Navigation 제공 없이 래핑
    MaterialTheme { content() }
}""",
                "모듈 경계에서 활용" to """// :feature 모듈의 내부 Preview Helper가
// :app 모듈로 유출되지 않도록 방지
// → @PreviewOnly + internal 조합으로 완전 격리
@PreviewOnly
internal fun FeatureScreenPreviewHelper() {
    FeatureScreen(viewModel = FakeViewModel())
}"""
            )

            patterns.forEachIndexed { index, (title, code) ->
                if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                CodeBlock(
                    label = "패턴 ${index + 1}: $title",
                    labelColor = Color(0xFFFF8C00),
                    code = code
                )
            }
        }
    }
}

// ── 공통 컴포넌트 ─────────────────────────────────────────────────────────────

@Composable
private fun PreviewOnlySectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFE0E0E0),
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun CodeBlock(label: String, labelColor: Color, code: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1A1A2E))
                .border(1.dp, Color(0xFF444466), RoundedCornerShape(6.dp))
                .padding(12.dp)
        ) {
            Text(
                text = code,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE0E0E0),
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun InfoBox(text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(color, RoundedCornerShape(2.dp))
                .align(Alignment.Top)
                .padding(top = 5.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = color.copy(alpha = 0.9f), lineHeight = 18.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
private fun PreviewOnlyAnnotationPreview() {
    PreviewOnlyAnnotationExampleUI(onBackEvent = {})
}
