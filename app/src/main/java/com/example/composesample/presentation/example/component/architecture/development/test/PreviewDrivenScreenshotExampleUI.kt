package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Preview-Driven Screenshot Testing Example
 * - @Preview 를 source of truth 로 삼는 스크린샷 테스트 각도
 * - Paparazzi/Roborazzi 실행 메커니즘은 ScreenshotTestingExampleUI 참조
 */

// ==================== 매트릭스 차원 정의 ====================

/** locale 차원 — 표시 텍스트와 레이아웃 방향(RTL)을 함께 결정 */
private data class LocaleSpec(
    val tag: String,
    val label: String,
    val text: SampleStrings,
    val rtl: Boolean
)

/** 한 컴포넌트가 locale 별로 가지는 문자열 집합 (실제 프로젝트의 strings.xml 대응) */
private data class SampleStrings(
    val title: String,
    val subtitle: String,
    val action: String
)

private val localeOptions = listOf(
    LocaleSpec(
        tag = "en",
        label = "en",
        text = SampleStrings("Welcome back", "Sign in to continue", "Sign In"),
        rtl = false
    ),
    LocaleSpec(
        tag = "ko",
        label = "ko",
        text = SampleStrings("다시 오셨네요", "계속하려면 로그인하세요", "로그인"),
        rtl = false
    ),
    LocaleSpec(
        tag = "ar",
        label = "ar (RTL)",
        text = SampleStrings("مرحبًا بعودتك", "سجّل الدخول للمتابعة", "تسجيل الدخول"),
        rtl = true
    )
)

private val fontScaleOptions = listOf(0.85f, 1.0f, 1.3f)
private val themeOptions = listOf(false, true) // false = Light, true = Dark

/** 매트릭스의 한 셀 = 하나의 골든 이미지에 대응 */
private data class PreviewSpec(
    val locale: LocaleSpec,
    val fontScale: Float,
    val dark: Boolean
)

@Composable
fun PreviewDrivenScreenshotExampleUI(onBackEvent: () -> Unit) {
    // 선택된 차원 값 (초기값: 매트릭스 폭증을 바로 보여주기 위해 일부만 선택)
    val selectedLocales = remember { mutableStateListOf("en", "ko") }
    val selectedFontScales = remember { mutableStateListOf(1.0f, 1.3f) }
    val selectedThemes = remember { mutableStateListOf(false, true) }

    // 데카르트 곱으로 매트릭스 파생 — @Preview 하나가 N×M×K 변형으로 확장되는 핵심
    val matrix: List<PreviewSpec> = remember(
        selectedLocales.toList(),
        selectedFontScales.toList(),
        selectedThemes.toList()
    ) {
        val locales = localeOptions.filter { it.tag in selectedLocales }
        buildList {
            for (locale in locales) {
                for (scale in fontScaleOptions.filter { it in selectedFontScales }) {
                    for (dark in themeOptions.filter { it in selectedThemes }) {
                        add(PreviewSpec(locale, scale, dark))
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackEvent.invoke() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
                Text(
                    text = "Preview-Driven Screenshot Testing",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            ConceptCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            DimensionSelectorCard(
                selectedLocales = selectedLocales,
                selectedFontScales = selectedFontScales,
                selectedThemes = selectedThemes
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            MatrixCountCard(matrix = matrix, selectedLocales, selectedFontScales, selectedThemes)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            LiveMatrixCard(matrix = matrix)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            DerivedCodeCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            MappingCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** ① 개념 — @Preview 를 source of truth 로 삼는다는 핵심 아이디어 */
@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "@Preview = Source of Truth",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "스크린샷 테스트를 개별 테스트 함수로 일일이 작성하는 대신,\n" +
                        "이미 작성해 둔 @Preview 를 단일 진실 공급원으로 삼아\n" +
                        "locale × fontScale × theme 같은 변형 매트릭스를 자동으로 파생한다.\n\n" +
                        "→ Preview 하나만 유지하면 테스트 커버리지가 차원 곱만큼 늘어나고,\n" +
                        "   디자인 변경 시 Preview·테스트를 따로 동기화할 필요가 없다.",
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}

/** ② 차원 선택 — FilterChip 토글로 매트릭스 축을 구성 */
@Composable
private fun DimensionSelectorCard(
    selectedLocales: MutableList<String>,
    selectedFontScales: MutableList<Float>,
    selectedThemes: MutableList<Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "매트릭스 차원 선택",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "각 축의 값을 켜고 끄면 아래 매트릭스가 데카르트 곱으로 즉시 갱신됩니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "locale", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                localeOptions.forEach { locale ->
                    ToggleChip(
                        label = locale.label,
                        selected = locale.tag in selectedLocales,
                        onClick = { toggle(selectedLocales, locale.tag) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "fontScale", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fontScaleOptions.forEach { scale ->
                    ToggleChip(
                        label = "${scale}x",
                        selected = scale in selectedFontScales,
                        onClick = { toggle(selectedFontScales, scale) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "theme", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                themeOptions.forEach { dark ->
                    ToggleChip(
                        label = if (dark) "Dark" else "Light",
                        selected = dark in selectedThemes,
                        onClick = { toggle(selectedThemes, dark) }
                    )
                }
            }
        }
    }
}

/** 토글 가능한 칩 (M3 FilterChip Experimental 회피용 자체 구현) */
@Composable
private fun ToggleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFF1565C0) else Color.White
    val fg = if (selected) Color.White else Color(0xFF616161)
    val borderColor = if (selected) Color(0xFF1565C0) else Color(0xFFBDBDBD)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** ③ 매트릭스 변형 수 표시 — N×M×K 폭증을 수식으로 시각화 */
@Composable
private fun MatrixCountCard(
    matrix: List<PreviewSpec>,
    selectedLocales: List<String>,
    selectedFontScales: List<Float>,
    selectedThemes: List<Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "파생된 매트릭스",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val l = selectedLocales.size
            val f = selectedFontScales.size
            val t = selectedThemes.size
            Text(
                text = "$l locale × $f fontScale × $t theme = ${matrix.size} 변형",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "→ 각 변형이 곧 하나의 골든 스크린샷에 대응합니다. " +
                        "Preview 코드를 늘리지 않고도 축을 추가하는 것만으로 커버리지가 곱으로 증가합니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }
    }
}

/** ④ 라이브 매트릭스 — 선택된 변형을 실제로 렌더링 */
@Composable
private fun LiveMatrixCard(matrix: List<PreviewSpec>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "라이브 렌더링 (스크린샷 대상)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CompositionLocalProvider 로 LocalDensity(fontScale)·LocalLayoutDirection(RTL) 을 실제 적용해 " +
                        "동일 컴포넌트를 각 변형으로 그립니다. 캡처 도구가 보는 픽셀이 바로 이것입니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (matrix.isEmpty()) {
                Text(
                    text = "선택된 차원이 없습니다. 위에서 각 축의 값을 하나 이상 켜 주세요.",
                    fontSize = 13.sp,
                    color = Color(0xFFB71C1C)
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    matrix.forEach { spec ->
                        MatrixCell(spec = spec)
                    }
                }
            }
        }
    }
}

/** 매트릭스 한 셀 — 라벨 + 실제 변형이 적용된 샘플 컴포넌트 */
@Composable
private fun MatrixCell(spec: PreviewSpec) {
    val baseDensity = LocalDensity.current
    Column(
        modifier = Modifier
            .width(170.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Text(
            text = "${spec.locale.label} · ${spec.fontScale}x · ${if (spec.dark) "Dark" else "Light"}",
            fontSize = 10.sp,
            color = Color(0xFF9E9E9E),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        // 변형 적용: fontScale 은 Density 로, RTL 은 LayoutDirection 으로 실제 주입
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = baseDensity.density,
                fontScale = spec.fontScale
            ),
            LocalLayoutDirection provides
                if (spec.locale.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        ) {
            SampleLoginCard(text = spec.locale.text, dark = spec.dark)
        }
    }
}

/** 스크린샷 대상이 되는 샘플 컴포넌트 — locale/fontScale/theme 에 반응 */
@Composable
private fun SampleLoginCard(text: SampleStrings, dark: Boolean) {
    val bg = if (dark) Color(0xFF1E1E1E) else Color.White
    val fg = if (dark) Color(0xFFECECEC) else Color(0xFF212121)
    val subFg = if (dark) Color(0xFFB0B0B0) else Color(0xFF757575)
    val accent = Color(0xFF1565C0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(10.dp)
    ) {
        Text(text = text.title, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = text.subtitle, color = subFg, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(accent)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text.action, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** ⑤ 매트릭스를 코드로 표현 — @PreviewParameter / 멀티프리뷰 */
@Composable
private fun DerivedCodeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "매트릭스를 코드로 표현하기",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            CodeSnippet(
                code = """
// 1) 멀티프리뷰 애노테이션으로 차원을 한 곳에 정의
@Preview(name = "light", uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "dark",  uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "small", fontScale = 0.85f)
@Preview(name = "large", fontScale = 1.3f)
@Preview(name = "ko",    locale = "ko")
@Preview(name = "ar",    locale = "ar") // RTL
annotation class PreviewMatrix

// 2) @Preview = source of truth. 테스트는 이 Preview 를 그대로 캡처
@PreviewMatrix
@Composable
fun LoginCardPreview() {
    LoginCard()
}

// 3) 데이터 변형이 필요하면 @PreviewParameter 로 추가 축 구성
@PreviewMatrix
@Composable
fun LoginCardPreview(
    @PreviewParameter(LoginStateProvider::class) state: LoginUiState
) {
    LoginCard(state)
}

// → 스크린샷 도구(Compose Preview Screenshot Testing / Roborazzi)가
//   @Preview 들을 수집해 각 조합을 골든 이미지로 캡처
                """.trimIndent()
            )
        }
    }
}

/** ⑥ 골든 이미지 매핑 + 기존 예제 참조 */
@Composable
private fun MappingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "골든 이미지 매핑 & 다음 단계",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "매트릭스 셀 1개 = 골든 이미지 1개 (예: LoginCard_ko_1.3x_dark.png)",
                "차원 축을 늘리면 커버리지가 곱으로 증가하므로, 회귀 위험이 큰 축만 선별",
                "Preview 를 단일 진실로 두면 디자인 변경 시 테스트를 따로 수정할 필요 없음",
                "RTL·큰 fontScale 은 레이아웃 깨짐이 가장 잘 드러나는 고가치 축",
                "실제 캡처/검증 메커니즘(record/verify, Paparazzi vs Roborazzi)은 " +
                        "'Screenshot Testing (Paparazzi / Roborazzi)' 예제 참조",
                "AGP 8.5+ 의 Compose Preview Screenshot Testing 플러그인은 " +
                        "@Preview 를 직접 입력으로 받아 이 파이프라인을 공식 지원"
            ).forEach { line ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "• $line", fontSize = 13.sp)
            }
        }
    }
}

/** 코드 스니펫 표시용 박스 */
@Composable
private fun CodeSnippet(code: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFECEFF1),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/** 선택 리스트 토글 헬퍼 — 마지막 1개는 비우지 않도록 유지 */
private fun <T> toggle(list: MutableList<T>, value: T) {
    if (value in list) {
        if (list.size > 1) list.remove(value)
    } else {
        list.add(value)
    }
}
