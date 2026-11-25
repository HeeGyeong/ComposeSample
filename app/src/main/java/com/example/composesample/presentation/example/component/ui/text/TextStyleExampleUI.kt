package com.example.composesample.presentation.example.component.ui.text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.noRippleClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextStyleExampleUI(onBackEvent: () -> Unit) {
    // 줄 높이 모드 측정 결과는 카드가 스크롤로 빠졌다 돌아와도 남도록 화면 루트에 둔다
    val lineLab = remember { LineHeightLabState() }
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            MainHeader(
                title = "Text Style Example",
                onBackIconClicked = onBackEvent
            )
        }

        item {
            val visibleFontSizeUI = remember { mutableStateOf(false) }
            val fontSizeUIVisibleText = if (visibleFontSizeUI.value) "감추기" else "보기"

            Text(
                modifier = Modifier
                    .height(30.dp)
                    .noRippleClickable {
                        visibleFontSizeUI.value = !visibleFontSizeUI.value
                    },
                text = "폰트 사이즈 관련 UI $fontSizeUIVisibleText"
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (visibleFontSizeUI.value) {
                CheckFontSizeUI()

                CheckFontSizeUI(14.sp)

                CheckFontSizeUI(1.em)

                CheckFontSizeUI(2.em)

                CheckFontSizeUI(3.em)

                CheckFontSizeUI(4.em)

                CheckFontSizeUI(30.sp)

                CheckFontSizeUI((14 * 1.em.value).sp)

                CheckFontSizeUI((14 * 2.em.value).sp)

                CheckFontSizeUI((14 * 3.em.value).sp)

                CheckFontSizeUI(15.sp)

                CheckFontSizeUI(18.em)

                Spacer(modifier = Modifier.height(20.dp))
            }

            val visibleTextSpaceUI = remember { mutableStateOf(false) }
            val textSpaceUIVisibleText = if (visibleTextSpaceUI.value) "감추기" else "보기"

            Text(
                modifier = Modifier
                    .height(30.dp)
                    .noRippleClickable {
                        visibleTextSpaceUI.value = !visibleTextSpaceUI.value
                    },
                text = "텍스트 간격 관련 UI $textSpaceUIVisibleText"
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (visibleTextSpaceUI.value) {
                CheckTextSpaceUI()

                CheckTextSpaceUI(14.sp)

                CheckTextSpaceUI(1.em)

                CheckTextSpaceUI(2.em)

                Text(
                    modifier = Modifier
                        .height(30.dp),
                    text = "실 사용 예시"
                )

                Spacer(modifier = Modifier.height(10.dp))

                val exampleText = "이것은 실 사용 예시입니다. 띄어쓰기와 줄바꿈을 수행하는 텍스트는 이렇게 나오게 됩니다."

                Text(
                    modifier = Modifier
                        .wrapContentSize(),
                    text = exampleText,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    modifier = Modifier
                        .wrapContentSize(),
                    text = exampleText,
                    letterSpacing = -(0.02).em,
                    lineHeight = 1.5.em,
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // 줄마다 다른 줄 높이 — LineHeightStyle.Mode(Compose 1.13 PerLine) 비교 카드
        item { LineModeOverviewCard() }
        item { SizeMixCard(lineLab) }
        item { ScriptMixCard(lineLab) }
        item { InheritCard(lineLab) }
        item { LineModeSummaryCard() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun CheckTextSpaceUI(fontSize: TextUnit = TextUnit.Unspecified) {
    val text = "01234567890 abcdefghijklnmopqrstuvwxyz 가나다라마바사아자차카타파하"

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        text = "Size : $fontSize",
    )

    Spacer(modifier = Modifier.height(3.dp))
    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text,
        fontSize = 20.sp,
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text,
        fontSize = 20.sp,
        letterSpacing = fontSize,
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text,
        fontSize = 20.sp,
        lineHeight = fontSize,
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text,
        fontSize = 20.sp,
        letterSpacing = fontSize,
        lineHeight = fontSize,
    )

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun CheckFontSizeUI(fontSize: TextUnit = TextUnit.Unspecified) {
    val text1 = "0"
    val text2 = "a"
    val text3 = "ㄱ"

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        text = "Size : $fontSize",
    )
    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text1,
        fontSize = fontSize,
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text2,
        fontSize = fontSize,
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text3,
        fontSize = fontSize,
    )

    Spacer(modifier = Modifier.height(10.dp))
}
// ==================== 줄마다 다른 줄 높이 — LineHeightStyle.Mode(#186) ====================

private val LhIndigo = Color(0xFF283593)
private val LhMuted = Color(0xFF616161)
private val LhBody = Color(0xFF212121)
private val LhBandA = Color(0xFFE3F2FD)
private val LhBandB = Color(0xFFFFF3E0)
private val LhOverflow = Color(0xFFC62828)
private val LhInherited = Color(0xFFEF6C00)
private val LhOk = Color(0xFF2E7D32)

@Composable
private fun LhCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LhIndigo)
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun LhNote(text: String, color: Color = LhMuted) {
    Text(text = text, fontSize = 12.sp, color = color, lineHeight = 18.sp)
}

@Composable
private fun ChoiceRow(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(label, fontSize = 12.sp) }
            )
        }
    }
}

/** 이 카드의 문단을 모든 모드로 잰 결과(없으면 재는 중) */
@Composable
private fun rememberLineMeasure(lab: LineHeightLabState, sample: LineSample, wrap: LineWrap): SampleMeasure? {
    val density = LocalDensity.current
    val resolver = LocalFontFamilyResolver.current
    val widthPx = previewWidthPx(density)
    LaunchedEffect(sample, wrap, widthPx, density.density, density.fontScale) {
        lab.ensure(sample, wrap, widthPx, density, resolver)
    }
    return lab.results[sample to wrap]
}

/** px → dp 문자열(소수 첫째 자리) — 밀도를 한 번 읽어 람다로 */
@Composable
private fun rememberDpText(): (Float?) -> String {
    val density = LocalDensity.current.density
    return remember(density) { { px -> if (px == null) "–" else pxToDpText(px, density) } }
}

/** 같은 문단을 한 모드로 — 띠 = 줄 상자, 왼쪽 막대 = 상자 밖 잉크(빨강) · 물려받은 줄(주황) */
@Composable
private fun LinePreview(
    tag: String,
    text: AnnotatedString,
    mode: LineMode,
    measure: ModeMeasure?,
    inherited: List<Int> = emptyList(),
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(mode.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LhIndigo)
    Spacer(modifier = Modifier.height(4.dp))
    BasicText(
        text = text,
        modifier = Modifier
            .padding(start = 8.dp)
            .width(LH_PREVIEW_WIDTH_DP.dp)
            .testTag("linePreview-$tag-${mode.name}")
            .drawBehind {
                val l = layout ?: return@drawBehind
                val bar = 3.dp.toPx()
                for (i in 0 until l.lineCount) {
                    val top = l.getLineTop(i)
                    val bottom = l.getLineBottom(i)
                    drawRect(if (i % 2 == 0) LhBandA else LhBandB, Offset(0f, top), Size(size.width, bottom - top))
                    val flag = when {
                        i in inherited -> LhInherited
                        measure != null && i < measure.lines.size && measure.overflow(i) >= 1f -> LhOverflow
                        else -> null
                    }
                    if (flag != null) drawRect(flag, Offset(-bar - 2.dp.toPx(), top), Size(bar, bottom - top))
                }
            },
        style = lineModeStyle(mode, LhBody),
        onTextLayout = { layout = it }
    )
    Spacer(modifier = Modifier.height(10.dp))
}

/** 모드마다 — 줄 높이 · 전체 · 상자 밖 잉크 최대 · 이웃 줄 잉크 간격 최소 */
@Composable
private fun ModeTable(result: SampleMeasure?) {
    val dpText = rememberDpText()
    if (result == null) {
        LhNote("재는 중…")
        return
    }
    LineMode.entries.forEach { mode ->
        val m = result.byMode.getValue(mode)
        val bad = m.maxOverflow >= 1f || (m.minInkGap ?: 0f) < 0f
        Text(
            text = "${mode.label} — 전체 ${dpText(m.height)}dp",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = LhBody
        )
        Text(
            text = "  줄 " + m.lines.joinToString(" · ") { dpText(it.height) },
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = LhBody
        )
        Text(
            text = "  상자 밖 ${dpText(m.maxOverflow)} · 잉크 간격 ${dpText(m.minInkGap)} · 겹침 ${m.totalCollisions}px",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (bad) LhOverflow else LhOk
        )
        if (!m.isolationKeptLayout) LhNote("  ⚠ 한 줄만 그린 배치의 줄 상자가 달라 잉크 값을 믿을 수 없다", LhOverflow)
        Spacer(modifier = Modifier.height(4.dp))
    }
    LhNote(
        "단위 dp · 요청 줄 높이 ${dpText(result.requestedPx.toFloat())}dp(올림 px). 상자 밖 = 줄 잉크가 자기 상자 밖으로 나간 최대 · " +
            "잉크 간격 = 이웃 줄 잉크 사이 최소(음수면 두 줄 글자가 같은 행에 걸친다) · 겹침 = 두 줄 잉크가 같은 자리에 있는 픽셀 수."
    )
}

@Composable
private fun LineModeOverviewCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LhIndigo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("줄마다 다른 줄 높이 — LineHeightStyle.Mode", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "lineHeight 를 주면 Compose 는 문단 전체에 줄 높이 span 하나(LineHeightStyleSpan)를 건다. Fixed(기본) · Minimum · Tight 는 " +
                    "첫 줄의 글꼴 메트릭으로 한 번 계산해 모든 줄에 쓰고, Compose 1.13 의 PerLine 은 줄마다 '글꼴이 정한 높이'와 요청 높이 중 " +
                    "큰 쪽을 쓴다. lineHeight 를 주지 않으면 어떤 모드도 적용되지 않는다.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "아래 카드는 본문 14sp · lineHeight 21sp(1.5배) · 큰 글씨 30sp 굵게, 폭 260dp 로 같은 문단을 모드마다 배치하고, 한 줄만 " +
                    "보이게(나머지 글자는 투명 span) 오프스크린에 그려 그 줄 잉크가 줄 상자 밖으로 나간 정도와 이웃 줄 잉크와의 간격을 잰다. " +
                    "줄 상자가 요청 높이 그대로 보이도록 Trim.None(첫 줄 위 · 끝 줄 아래 다듬기 끔)으로 비교한다.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "띠 = 줄 상자(getLineTop ~ getLineBottom) · 왼쪽 빨간 막대 = 잉크가 상자 밖으로 1px 이상 · 주황 막대 = 앞줄 높이를 물려받은 줄",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SizeMixCard(lab: LineHeightLabState) {
    val samples = listOf(LineSample.SizeSecond, LineSample.SizeFirst)
    val compares = listOf(LineMode.Fixed, LineMode.Minimum)
    var sampleIndex by rememberSaveable { mutableIntStateOf(0) }
    var compareIndex by rememberSaveable { mutableIntStateOf(0) }
    val sample = samples[sampleIndex]
    val compare = compares[compareIndex]
    val result = rememberLineMeasure(lab, sample, LineWrap.HardBreaks)
    val text = remember(sample) { sampleText(sample, LineWrap.HardBreaks) }
    val dpText = rememberDpText()

    LhCard("① 큰 글씨가 섞인 문단 (줄마다 \\n)") {
        LhNote("가격처럼 한 줄에만 큰 글씨가 있는 목록. 줄마다 \\n 이라 줄 하나가 문단 하나다.")
        Spacer(modifier = Modifier.height(8.dp))
        ChoiceRow(samples.map { it.label }, sampleIndex) { sampleIndex = it }
        ChoiceRow(compares.map { "위: ${it.label}" }, compareIndex) { compareIndex = it }
        Spacer(modifier = Modifier.height(8.dp))
        LinePreview("size", text, compare, result?.byMode?.get(compare))
        LinePreview("size", text, LineMode.PerLine, result?.perLine)
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        ModeTable(result)
        if (result != null) {
            val natural = result.natural
            val big = natural.lines.indices.maxByOrNull { natural.lines[it].height } ?: 0
            val fixed = result.byMode.getValue(LineMode.Fixed)
            val minimum = result.byMode.getValue(LineMode.Minimum)
            Spacer(modifier = Modifier.height(6.dp))
            LhNote(
                "큰 줄(${big + 1}번째)의 자연 높이 ${dpText(natural.lines[big].height)}dp — Fixed 는 ${dpText(fixed.lines[big].height)}dp 로 두고 " +
                    "잉크가 상자 밖으로 ${dpText(fixed.overflow(big))}dp, PerLine 은 ${dpText(result.perLine.lines[big].height)}dp 로 늘리고 " +
                    "상자 밖 최대 ${dpText(result.perLine.maxOverflow)}dp.",
                LhBody
            )
            if (sample == LineSample.SizeFirst) {
                LhNote(
                    "첫 줄이 요청보다 크면 Minimum 은 그 첫 줄 높이를 모든 줄에 쓴다 — 줄 " +
                        minimum.lines.joinToString(" · ") { dpText(it.height) } + ", 전체 ${dpText(minimum.height)}dp vs PerLine ${dpText(result.perLine.height)}dp. " +
                        "Fixed 는 첫 줄 비율로 기준선 위치를 정해 모든 줄에 쓰므로 작은 줄의 아래 글자도 상자 밖으로 나간다(상자 밖 최대 ${dpText(fixed.maxOverflow)}dp).",
                    LhBody
                )
            }
        }
    }
}

@Composable
private fun ScriptMixCard(lab: LineHeightLabState) {
    val result = rememberLineMeasure(lab, LineSample.Scripts, LineWrap.HardBreaks)
    val text = remember { sampleText(LineSample.Scripts, LineWrap.HardBreaks) }
    val dpText = rememberDpText()

    LhCard("② 키 큰 문자가 섞인 문단 (줄마다 \\n)") {
        LhNote(
            "미얀마어 · 티베트어 · 크메르어 · 태국어 줄. 글자는 시스템 폴백 글꼴(Noto)이 그리고, Compose 는 폴백 글꼴의 줄 간격을 줄 메트릭에 " +
                "넣도록 배치한다(AndroidParagraph fallbackLineSpacing = true → StaticLayout.Builder.setUseLineSpacingFromFallbacks, API 28+ 에서만 넘김)."
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinePreview("script", text, LineMode.Fixed, result?.byMode?.get(LineMode.Fixed))
        LinePreview("script", text, LineMode.PerLine, result?.perLine)
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        ModeTable(result)
        if (result != null) {
            val per = result.perLine
            Spacer(modifier = Modifier.height(6.dp))
            Text("PerLine 줄마다 — 줄 상자 vs 글자 잉크 높이", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LhBody)
            per.lines.forEachIndexed { i, line ->
                val name = text.text.substring(line.start, line.end).trim().substringBefore(' ')
                val ink = per.ink[i]
                val inkHeight = ink?.let { (it.bottom - it.top).toFloat() }
                val grown = line.height > result.requestedPx + 1f
                Text(
                    text = "  $name: 상자 ${dpText(line.height)} · 잉크 ${dpText(inkHeight)}" + if (grown) "  ← 늘어남" else "",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (grown) LhInherited else LhBody
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LhNote(
                "PerLine 은 잉크가 아니라 글꼴 메트릭(ascent/descent)을 본다 — 늘어난 줄도 잉크는 요청 높이 안에 들어갈 수 있다. " +
                    "어느 문자 줄이 늘어나는지는 기기 폴백 글꼴의 메트릭에 달렸다(같은 문단이 API 35 에뮬에서는 미얀마어 · 티베트어, " +
                    "API 28 에뮬에서는 티베트어만 늘었다)."
            )
        }
    }
}

@Composable
private fun InheritCard(lab: LineHeightLabState) {
    val samples = LineSample.entries
    val wraps = listOf(LineWrap.SoftWrap, LineWrap.SoftWrapCharSpans)
    var sampleIndex by rememberSaveable { mutableIntStateOf(0) }
    var wrapIndex by rememberSaveable { mutableIntStateOf(0) }
    val sample = samples[sampleIndex]
    val wrap = wraps[wrapIndex]
    val result = rememberLineMeasure(lab, sample, wrap)
    val plain = rememberLineMeasure(lab, sample, LineWrap.SoftWrap)
    val text = remember(sample, wrap) { sampleText(sample, wrap) }
    val density = LocalDensity.current
    val resolver = LocalFontFamilyResolver.current
    val widthPx = previewWidthPx(density)
    LaunchedEffect(sample, widthPx, density.density, density.fontScale) { lab.ensureTiming(sample, widthPx, density, resolver) }
    val dpText = rememberDpText()

    LhCard("③ 자동 줄바꿈 문단 — 앞줄 높이를 물려받는 줄") {
        LhNote("같은 내용을 \\n 없이 한 문단으로. 줄은 폭 260dp 가 정한다. PerLine 문서대로라면 줄마다 max(자연 높이, 요청 높이)여야 한다.")
        Spacer(modifier = Modifier.height(8.dp))
        ChoiceRow(samples.map { it.label }, sampleIndex) { sampleIndex = it }
        ChoiceRow(wraps.map { it.label }, wrapIndex) { wrapIndex = it }
        Spacer(modifier = Modifier.height(8.dp))
        LinePreview("inherit", text, LineMode.PerLine, result?.perLine, result?.inheritedLines.orEmpty())
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        if (result == null) {
            LhNote("재는 중…")
        } else if (!result.sameBreaks) {
            LhNote("⚠ 자연 배치와 PerLine 의 줄바꿈 위치가 달라 줄끼리 비교할 수 없다", LhOverflow)
        } else {
            val expected = result.perLineExpected
            val inherited = result.inheritedLines
            Text("줄마다 — 자연 · 기대 max(자연, 요청) · PerLine 실제", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LhBody)
            result.perLine.lines.forEachIndexed { i, line ->
                Text(
                    text = "  ${i + 1}: ${dpText(result.natural.lines[i].height)} · ${dpText(expected[i])} · ${dpText(line.height)}" +
                        if (i in inherited) "  ← 물려받음" else "",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (i in inherited) LhInherited else LhBody
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LhNote(
                "물려받은 줄 ${inherited.size}개 · 전체 PerLine ${dpText(result.perLine.height)}dp vs 기대 합 ${dpText(expected.sum())}dp · " +
                    "Minimum ${dpText(result.byMode.getValue(LineMode.Minimum).height)}dp",
                if (inherited.isEmpty()) LhOk else LhInherited
            )
            if (wrap == LineWrap.SoftWrapCharSpans) {
                val moved = plain != null && plain.natural.lines.map { it.start } != result.natural.lines.map { it.start }
                if (moved) LhNote("⚠ 글자마다 span 을 씌우니 줄바꿈 위치가 span 없음과 달라졌다.", LhOverflow)
                if (sample == LineSample.Scripts) {
                    LhNote(
                        "⚠ 티베트어 · 미얀마어는 결합 기호가 구간마다 따로 셰이핑되어 점선 원(◌)으로 그려진다(위 미리보기) — 글자를 쪼개는 span 은 " +
                            "복합 문자를 망가뜨린다.",
                        LhOverflow
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LhNote(
            "원인(SDK android-35 StaticLayout.generate): 줄을 내보낸 뒤 그 줄이 스타일 구간(MetricAffectingSpan 경계) 안에서 끝나면 다음 줄의 " +
                "시작 메트릭을 fm 에서 이어받는다(fmAscent = min(defaultAscent, fm.ascent)). LineHeightSpan 이 있으면 out() 이 fm 을 그 줄의 " +
                "메트릭으로 채워 chooseHeight 에 넘기므로 큰 줄의 ascent · descent 가 다음 줄로 넘어가고, PerLine 은 넘어온 높이가 요청보다 크면 " +
                "'자연 높이가 크다'고 보고 그대로 둔다. lineHeight 가 없으면 이 span 이 없어 자연 배치는 물려받지 않고, \\n 은 문단을 새로 " +
                "시작해 초기화된다. Compose 의 LineHeightStyleSpan.chooseHeightPerLine 도 이 재사용을 주석으로 경고한다.",
            LhBody
        )
        Spacer(modifier = Modifier.height(4.dp))
        val plainMicros = lab.timings[sample to LineWrap.SoftWrap]
        val charMicros = lab.timings[sample to LineWrap.SoftWrapCharSpans]
        LhNote(
            "'글자마다 span' 은 원인 확인용이다 — 글자마다 크기 span 으로 구간을 끊으면 줄이 늘 구간 끝에서 끝나 물려받지 않는다. 쓰라는 뜻은 " +
                "아니다: 배치 1회 중앙값(PerLine, 이 기기) span 없음 ${plainMicros?.let { "${it}µs" } ?: "재는 중"} · 글자마다 " +
                (charMicros?.let { "${it}µs" } ?: "재는 중") +
                (if (plainMicros != null && charMicros != null) ratioText(charMicros, plainMicros)?.let { " (${it}배)" } ?: "" else "")
        )
    }
}

@Composable
private fun LineModeSummaryCard() {
    LhCard("정리") {
        LhNote(
            "• 줄마다 \\n 으로 나뉜 목록 · 짧은 라벨처럼 줄 하나가 문단 하나면 PerLine 은 문서대로다 — 큰 줄만 늘고 나머지는 요청 높이(①).\n" +
                "• 자동 줄바꿈되는 긴 문단에서는 큰 줄 다음 줄들이 그 높이를 물려받는다. 큰 글씨가 첫 줄이면 PerLine 이 Minimum 과 같아진다(③).\n" +
                "• PerLine 은 잉크가 아니라 글꼴 메트릭을 본다 — 키 큰 문자 줄의 상자는 그 줄 글꼴의 ascent · descent 만큼 늘어 잉크보다 크다(②).\n" +
                "• Fixed 는 높이 · 기준선 위치를 첫 줄로 정한다 — 큰 줄은 눌리고, 큰 줄이 첫 줄이면 작은 줄의 아래 글자가 밖으로 나간다(①).\n" +
                "• 물려받기를 끊으려고 글자마다 span 을 씌우면 배치가 느려지고(③의 이 기기 측정) 복합 문자의 셰이핑이 깨진다 — 줄 높이가 달라야 하는 줄은 \\n 으로 " +
                "문단을 나누는 편이 낫다(③).\n" +
                "• FontVariation.Settings.merge(같은 1.13)는 가변 폰트 파일이 앱에 없어 다루지 않는다.",
            LhBody
        )
    }
}
