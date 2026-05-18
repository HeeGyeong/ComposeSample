package com.example.composesample.presentation.example.component.ui.style

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * Foundation Style API 비교 예제
 *
 * Compose 1.11에서 도입된 Foundation Style API의 핵심 아이디어를
 * 외부 실험 의존성 없이 자체 구현으로 재현한다.
 *
 * - Section A (Legacy): MaterialTheme + 별도 CompositionLocal로
 *   토큰을 따로따로 확장하는 기존 방식 — 토큰 종류가 늘수록 Local 개수가 늘고
 *   Provider 트리가 깊어짐
 * - Section B (Style API): Typography/Color/Shape/Spacing 을 묶은 단일
 *   Immutable Style 데이터 클래스를 하나의 CompositionLocal 로 전파 —
 *   `LocalAppStyle.current` 한 번으로 모든 토큰 접근, copy() 로 부분 오버라이드
 */
@Composable
fun FoundationStyleApiExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Foundation Style API (Compose 1.11)",
            onBackIconClicked = onBackEvent
        )

        // 사용자가 토글하는 현재 스타일 (Light / Dark / Brand)
        var selectedPreset by remember { mutableStateOf(StylePreset.Light) }
        val currentStyle = remember(selectedPreset) { selectedPreset.toStyle() }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item {
                PresetSelectorCard(
                    selected = selectedPreset,
                    onSelected = { selectedPreset = it }
                )
            }
            item { LegacyApproachCard(preset = selectedPreset) }
            item {
                // Style API 진입점: 단일 CompositionLocalProvider 로 모든 토큰 주입
                CompositionLocalProvider(LocalAppStyle provides currentStyle) {
                    StyleApiApproachCard()
                }
            }
            item { CopyOverrideCard(baseStyle = currentStyle) }
            item { ComparisonSummaryCard() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Style API 모델 (자체 구현)
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class AppTypography(
    val title: TextStyle,
    val body: TextStyle,
    val caption: TextStyle
)

@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val surface: Color,
    val onSurface: Color,
    val accent: Color
)

@Immutable
data class AppShapes(
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape
)

@Immutable
data class AppSpacing(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp
)

/**
 * Foundation Style API 의 핵심: 디자인 토큰을 하나의 Immutable 객체로 묶어
 * 단일 CompositionLocal 을 통해 트리에 전파한다.
 */
@Immutable
data class AppStyle(
    val typography: AppTypography,
    val colors: AppColors,
    val shapes: AppShapes,
    val spacing: AppSpacing
)

// staticCompositionLocalOf: 값이 거의 바뀌지 않을 때 권장 (전체 트리 invalidate)
val LocalAppStyle = staticCompositionLocalOf<AppStyle> {
    error("LocalAppStyle 이 제공되지 않음 — Provider 로 감싸야 합니다.")
}

enum class StylePreset(val label: String) {
    Light("Light"),
    Dark("Dark"),
    Brand("Brand");

    fun toStyle(): AppStyle = when (this) {
        Light -> lightStyle
        Dark -> darkStyle
        Brand -> brandStyle
    }
}

private val baseTypography = AppTypography(
    title = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    body = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    caption = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

private val baseShapes = AppShapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

private val baseSpacing = AppSpacing(xs = 4.dp, sm = 8.dp, md = 16.dp, lg = 24.dp)

private val lightStyle = AppStyle(
    typography = baseTypography,
    colors = AppColors(
        primary = Color(0xFF6750A4),
        onPrimary = Color.White,
        surface = Color(0xFFF7F2FA),
        onSurface = Color(0xFF1C1B1F),
        accent = Color(0xFF7D5260)
    ),
    shapes = baseShapes,
    spacing = baseSpacing
)

private val darkStyle = AppStyle(
    typography = baseTypography,
    colors = AppColors(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        accent = Color(0xFFEFB8C8)
    ),
    shapes = baseShapes,
    spacing = baseSpacing
)

private val brandStyle = AppStyle(
    typography = baseTypography.copy(
        title = baseTypography.title.copy(fontFamily = FontFamily.Serif),
        body = baseTypography.body.copy(fontFamily = FontFamily.Serif)
    ),
    colors = AppColors(
        primary = Color(0xFF006D77),
        onPrimary = Color.White,
        surface = Color(0xFFEDF6F9),
        onSurface = Color(0xFF003B47),
        accent = Color(0xFFE29578)
    ),
    shapes = AppShapes(
        small = RoundedCornerShape(2.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp)
    ),
    spacing = baseSpacing
)

// ─────────────────────────────────────────────────────────────────────────────
// Legacy 방식 (비교용): 토큰을 개별 CompositionLocal 로 분리
// ─────────────────────────────────────────────────────────────────────────────

private val LocalLegacyAccentColor = compositionLocalOf { Color(0xFF7D5260) }
private val LocalLegacyCornerRadius = compositionLocalOf { 12.dp }
private val LocalLegacyCaptionStyle = compositionLocalOf {
    TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
}

// ─────────────────────────────────────────────────────────────────────────────
// UI Cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Foundation Style API 란?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Compose 1.11 의 실험적 Foundation Style API 는 typography/colors/shapes/spacing 같은 " +
                        "디자인 토큰을 단일 Immutable 객체(Style)로 묶고, 하나의 CompositionLocal 로 트리에 전파한다. " +
                        "기존처럼 토큰 종류마다 별도 Local 을 만들 필요가 없고, copy() 로 부분 오버라이드가 깔끔해진다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun PresetSelectorCard(
    selected: StylePreset,
    onSelected: (StylePreset) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Style Preset 전환",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Preset 을 바꾸면 Style API 카드의 typography/colors/shapes 가 한 번에 갱신된다.",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StylePreset.entries.forEach { preset ->
                    val isSelected = preset == selected
                    Button(
                        onClick = { onSelected(preset) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF6750A4) else Color(0xFFE7E0EC),
                            contentColor = if (isSelected) Color.White else Color(0xFF1C1B1F)
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(preset.label, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegacyApproachCard(preset: StylePreset) {
    // Legacy: 토큰 종류별로 Provider 를 따로 쌓는다 — Preset 별 매핑도 수동
    val accent = when (preset) {
        StylePreset.Light -> Color(0xFF7D5260)
        StylePreset.Dark -> Color(0xFFEFB8C8)
        StylePreset.Brand -> Color(0xFFE29578)
    }
    val radius = when (preset) {
        StylePreset.Brand -> 4.dp
        else -> 12.dp
    }

    CompositionLocalProvider(
        LocalLegacyAccentColor provides accent,
        LocalLegacyCornerRadius provides radius
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "A. Legacy — MaterialTheme + 개별 CompositionLocal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E24AA)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "토큰 종류가 늘 때마다 Local 하나씩 추가 → Provider 트리가 깊어진다.",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(12.dp))

                LegacySwatch()
            }
        }
    }
}

@Composable
private fun LegacySwatch() {
    val accent = LocalLegacyAccentColor.current
    val radius = LocalLegacyCornerRadius.current
    val caption = LocalLegacyCaptionStyle.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(accent, RoundedCornerShape(radius))
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "Accent / Radius / Caption — Local 3개 조회",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "tokens 개수만큼 Local + Provider 가 필요함",
                color = Color.White.copy(alpha = 0.9f),
                style = caption
            )
        }
    }
}

@Composable
private fun StyleApiApproachCard() {
    // Style API: 단 한 줄로 모든 토큰 접근
    val style = LocalAppStyle.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = style.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = style.colors.surface)
    ) {
        Column(modifier = Modifier.padding(style.spacing.md)) {
            Text(
                text = "B. Foundation Style API — 단일 Style 전파",
                style = style.typography.title,
                color = style.colors.primary
            )
            Spacer(modifier = Modifier.height(style.spacing.xs))
            Text(
                text = "LocalAppStyle.current 하나로 typography·colors·shapes·spacing 전부 접근. " +
                        "Preset 변경 시 Style 객체만 교체하면 됨.",
                style = style.typography.body,
                color = style.colors.onSurface
            )
            Spacer(modifier = Modifier.height(style.spacing.md))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(style.colors.primary, style.shapes.large)
                    .border(2.dp, style.colors.accent, style.shapes.large)
                    .padding(style.spacing.md),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "primary + accent + shapes.large + spacing.md",
                        style = style.typography.body,
                        color = style.colors.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "토큰 4종 — 모두 같은 Style 객체에서 일관 조회",
                        style = style.typography.caption,
                        color = style.colors.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CopyOverrideCard(baseStyle: AppStyle) {
    // 부분 오버라이드: copy() 로 spacing 만 키운 자식 트리
    val denseChild = remember(baseStyle) {
        baseStyle.copy(spacing = AppSpacing(xs = 2.dp, sm = 4.dp, md = 8.dp, lg = 12.dp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "C. copy() 로 부분 오버라이드",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "자식 트리에서 spacing 만 절반으로 줄인 dense 변형. typography·colors·shapes 는 그대로 상속.",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(12.dp))

            CompositionLocalProvider(LocalAppStyle provides baseStyle) {
                SwatchByStyle(label = "기본 spacing.md=${baseStyle.spacing.md}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            CompositionLocalProvider(LocalAppStyle provides denseChild) {
                SwatchByStyle(label = "dense spacing.md=${denseChild.spacing.md}")
            }
        }
    }
}

@Composable
private fun SwatchByStyle(label: String) {
    val style = LocalAppStyle.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(style.colors.primary, style.shapes.medium)
            .padding(style.spacing.md)
    ) {
        Text(
            text = label,
            style = style.typography.body,
            color = style.colors.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ComparisonSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "정리: 언제 Style API 가 유리한가",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF311B92)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val rows = listOf(
                "토큰 갯수" to "Legacy: Local N개 / Style API: Local 1개",
                "테마 스위칭" to "Legacy: Provider 여러 개 갱신 / Style API: Style 객체 1개 교체",
                "부분 오버라이드" to "Legacy: 해당 Local 만 새로 Provide / Style API: style.copy(...) 한 줄",
                "MaterialTheme 와의 관계" to "공존 가능 — Style API 는 Foundation 레이어에서 Material 비의존 토큰까지 다룸",
                "주의" to "현재 Compose 1.11 단계에선 Experimental — 프로덕션 적용 전 안정화 단계 확인"
            )
            rows.forEach { (k, v) ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "• $k",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF311B92),
                        modifier = Modifier.weight(0.32f)
                    )
                    Text(
                        text = v,
                        fontSize = 12.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.weight(0.68f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MaterialTheme.colorScheme.primary = ${MaterialTheme.colorScheme.primary} (Material 토큰은 그대로 사용)",
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
        }
    }
}
