package com.example.composesample.presentation.example.component.ui.graphics

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay

/**
 * Pixelate 모자이크 Modifier 예제
 *
 * 값(가격·잔액)을 그대로 컴포즈한 채 그리기 단계에서만 모자이크로 가린다. 레이아웃 크기는 그대로이고 API 24 부터 동작한다
 * (Modifier.blur 는 API 31 미만에서 무시). 그리기 파이프라인·blockRatio·개별 vs 묶음·두 함정(지우기 생략, 레이어 없는 DstIn)·
 * 시맨틱 누출·비용을 카드로 나눈다. 구현은 PixelateModifier.kt, 출처·실측은 exampleGuide.kt.
 */
@Composable
fun PixelateExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Pixelate Modifier Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { PipelineCard() }
            item { CellByHeightCard() }
            item { BlockRatioCard() }
            item { GroupingCard() }
            item { ClearTrapCard() }
            item { FeatherLayerCard() }
            item { SemanticsCard() }
            item { BlurCompareCard() }
            item { CostCard() }
        }
    }
}

// ==================== 공통 데이터 ====================

// 같은 글자 수·고정폭 글꼴 — 값을 바꿔도 크기가 같아 비트맵이 재사용된다(함정 카드의 전제)
private val SameWidthPrices = listOf("₩1,284,500", "₩7,031,962", "₩4,558,107")
private const val PriceFontSizeSp = 28

private fun gridLabel(size: IntSize, density: Float, blockRatio: Float = PixelateDefaultBlockRatio): String {
    if (size.width == 0 || size.height == 0) return "측정 전"
    val grid = pixelGrid(size.width.toFloat(), size.height.toFloat(), blockRatio, 4f * density, 14f * density)
    return "%d×%d px · 셀 %.1f px(%.1f dp) · coarse %d×%d · fine %d×%d".format(
        java.util.Locale.ROOT,
        size.width, size.height, grid.cellPx, grid.cellPx / density,
        grid.columns, grid.rows, grid.fineColumns, grid.fineRows
    )
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard() {
    var hidden by remember { mutableStateOf(true) }
    var toggles by remember { mutableIntStateOf(0) }
    var index by remember { mutableIntStateOf(0) }
    var measured by remember { mutableStateOf(IntSize.Zero) }
    // onSizeChanged 는 크기가 바뀔 때만 불린다 — 가리기·공개를 오가도 이 수가 늘지 않으면 크기가 그대로라는 뜻
    var sizeCallbacks by remember { mutableIntStateOf(0) }

    SectionCard(
        title = "🟫 Pixelate — 값을 그린 뒤 모자이크로 가리기",
        description = "값을 자리표시자로 바꾸지 않고 실제 값을 그대로 컴포즈한 뒤, 그리기 단계에서만 작게 줄였다 단단한 블록으로 " +
            "키운다. 레이아웃은 그대로라 가렸다 공개해도 아무것도 움직이지 않는다.",
        containerColor = Color(0xFFEFEBE9)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = SameWidthPrices[index],
                fontSize = PriceFontSizeSp.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF212121),
                modifier = Modifier
                    .onSizeChanged {
                        measured = it
                        sizeCallbacks++
                    }
                    .then(if (hidden) Modifier.pixelate() else Modifier)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(if (hidden) "공개" else "가리기", Color(0xFF6D4C41)) {
                hidden = !hidden
                toggles++
            }
            DemoButton("값 바꾸기", Color(0xFF1976D2)) { index = (index + 1) % SameWidthPrices.size }
        }
        Spacer(modifier = Modifier.height(8.dp))
        InfoLine("크기", if (measured == IntSize.Zero) "측정 전" else "${measured.width}×${measured.height} px")
        InfoLine("onSizeChanged", "${sizeCallbacks}회 · 가리기/공개 ${toggles}회 전환")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox("Text(price, modifier = Modifier.pixelate())")
    }
}

// ==================== 2. 그리기 파이프라인 ====================

@Composable
private fun PipelineCard() {
    val density = LocalDensity.current.density
    var size by remember { mutableStateOf(IntSize.Zero) }
    val price = SameWidthPrices[0]

    SectionCard(
        title = "🔬 네 단계 — 격자 · 축소 · 2배 보간 · 확대",
        description = "① 셀 크기를 내용 높이 × blockRatio 로 정해 격자를 나누고 ② 셀 1개 = 픽셀 1개인 작은 비트맵에 줄여 그린다" +
            "(디테일은 여기서 사라진다) ③ 2배 비트맵에 FilterQuality.Low 로 옮겨 이웃 블록의 계단을 누그러뜨리고 ④ 원래 크기로 " +
            "FilterQuality.None 확대해 픽셀 하나를 단단한 블록 하나로 만든다. 마지막에 가장자리를 셀 한 변 폭으로 흐린다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        StageRow("원본 + 셀 격자") {
            PriceText(price, Modifier.pixelGridOverlay().onSizeChanged { size = it })
        }
        StageRow("coarse 를 곧바로 None 확대 (2배 보간 생략)") {
            PriceText(price, Modifier.pixelate(smoothing = false, featherEdges = false))
        }
        StageRow("2배 Low 보간 → None 확대 (featherEdges = false)") {
            PriceText(price, Modifier.pixelate(featherEdges = false))
        }
        StageRow("+ 가장자리 페더 (기본값)") {
            PriceText(price, Modifier.pixelate())
        }
        Spacer(modifier = Modifier.height(4.dp))
        InfoLine("이 기기의 격자", gridLabel(size, density))
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "drawWithCache {\n" +
                "    // 크기가 바뀌거나 요소가 바뀔 때(함정 ③) — 비트맵 할당\n" +
                "    val cell = (height * blockRatio).coerceIn(4.dp.toPx(), 14.dp.toPx())\n" +
                "    val coarse = ImageBitmap(cols, rows); val fine = ImageBitmap(cols * 2, rows * 2)\n" +
                "    onDrawWithContent {  // 매 프레임\n" +
                "        draw(this, layoutDirection, Canvas(coarse), coarseSize) {\n" +
                "            drawRect(Color.Transparent, blendMode = BlendMode.Src)\n" +
                "            scale(cols / width, rows / height, pivot = Offset.Zero) { drawContent() }\n" +
                "        }\n" +
                "        draw(…, Canvas(fine), fineSize) { drawImage(coarse, …, FilterQuality.Low, BlendMode.Src) }\n" +
                "        drawImage(fine, dstSize = size, filterQuality = FilterQuality.None)\n" +
                "    }\n" +
                "}"
        )
    }
}

// ==================== 3. 셀은 높이로 ====================

@Composable
private fun CellByHeightCard() {
    val density = LocalDensity.current.density
    val samples = listOf("$12" to 28, "$134,502.88" to 28, "$12" to 12, "$134,502.88" to 56)
    val sizes = remember { mutableStateOf(List(samples.size) { IntSize.Zero }) }

    SectionCard(
        title = "📏 셀 크기는 폭이 아니라 높이로",
        description = "글자의 높이는 글꼴 크기로만 정해지고 폭은 글자 수에 따라 달라진다. 높이로 셀을 정하면 같은 글꼴 크기의 " +
            "\$12 와 \$134,502.88 이 같은 블록이 되어 나란히 놓여도 어색하지 않다. 너무 작으면 4dp, 너무 크면 14dp 로 잘린다.",
        containerColor = Color(0xFFE8F5E9)
    ) {
        samples.forEachIndexed { i, (text, sp) ->
            Text(
                text = text,
                fontSize = sp.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                modifier = Modifier
                    .pixelate()
                    .onSizeChanged { measured -> sizes.value = sizes.value.toMutableList().also { it[i] = measured } }
            )
            Text(
                text = "${sp}sp · ${gridLabel(sizes.value[i], density)}",
                fontSize = 11.sp,
                color = Color(0xFF616161),
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ==================== 4. blockRatio ====================

@Composable
private fun BlockRatioCard() {
    val density = LocalDensity.current.density
    val ratios = listOf(0.08f, 0.14f, PixelateMinimumSafeBlockRatio, PixelateDefaultBlockRatio)
    val sizes = remember { mutableStateOf(List(ratios.size) { IntSize.Zero }) }

    SectionCard(
        title = "🔍 blockRatio 를 너무 낮추면 다시 읽힌다",
        description = "같은 28sp 가격을 비율만 바꿔 가렸다. README 는 0.14 에서 숫자를 알아볼 수 있고 0.08 에서는 그냥 읽힌다며 " +
            "MinimumSafeBlockRatio(0.18) 이상을 require 로 강제하라고 권한다. 0.08 은 셀이 min 4dp 에 걸린다.",
        containerColor = Color(0xFFFFF3E0)
    ) {
        ratios.forEachIndexed { i, ratio ->
            Text(
                text = "blockRatio $ratio" + when (ratio) {
                    PixelateMinimumSafeBlockRatio -> " (MinimumSafe)"
                    PixelateDefaultBlockRatio -> " (기본값)"
                    else -> ""
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            PriceText(
                SameWidthPrices[0],
                Modifier
                    .pixelate(blockRatio = ratio)
                    .onSizeChanged { measured -> sizes.value = sizes.value.toMutableList().also { it[i] = measured } }
            )
            Text(
                text = gridLabel(sizes.value[i], density, ratio),
                fontSize = 11.sp,
                color = Color(0xFF616161),
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ==================== 5. 개별 vs 묶음 ====================

@Composable
private fun GroupingCard() {
    val density = LocalDensity.current.density
    var totalSize by remember { mutableStateOf(IntSize.Zero) }
    var changeSize by remember { mutableStateOf(IntSize.Zero) }
    var groupSize by remember { mutableStateOf(IntSize.Zero) }

    SectionCard(
        title = "🧩 Text 마다 vs 부모 하나에",
        description = "Text 마다 걸면 줄마다 높이가 달라 셀 크기와 격자가 줄마다 새로 시작한다. 부모(Column)에 한 번 걸면 묶음 전체 " +
            "높이로 셀을 정해 격자 하나가 두 줄을 덮는다(원본의 Pixelate { } 컴포저블 = Box + Modifier.pixelate).",
        containerColor = Color(0xFFF3E5F5)
    ) {
        Text("Text 마다", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp)
        ) {
            Text(
                "₩1,284,500", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.pixelate().onSizeChanged { totalSize = it }
            )
            Text(
                "+3.2% 오늘", fontSize = 12.sp, color = Color(0xFF2E7D32),
                modifier = Modifier.pixelate().onSizeChanged { changeSize = it }
            )
        }
        SmallLabel("큰 줄 ${gridLabel(totalSize, density)}")
        SmallLabel("작은 줄 ${gridLabel(changeSize, density)}")

        Spacer(modifier = Modifier.height(12.dp))
        Text("부모 Column 하나에", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.pixelate().onSizeChanged { groupSize = it }) {
                Text("₩1,284,500", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("+3.2% 오늘", fontSize = 12.sp, color = Color(0xFF2E7D32))
            }
        }
        SmallLabel("묶음 ${gridLabel(groupSize, density)}")
    }
}

// ==================== 6. 함정: 지우지 않은 비트맵 ====================

@Composable
private fun ClearTrapCard() {
    var index by remember { mutableIntStateOf(0) }
    // 비트맵이 재사용되려면 요소가 같아야 한다(함정 ③) — remember 로 고정해 값만 바뀌게 한다
    val cleared = remember { Modifier.pixelate() }
    val notCleared = remember { Modifier.pixelate(clearEachFrame = false) }

    SectionCard(
        title = "⚠️ 함정 ① 재사용 비트맵을 지우지 않으면 이전 값이 비친다",
        description = "크기와 모디파이어 요소가 같으면 비트맵이 재사용된다(여기서는 remember 로 고정). 그리기 전에 투명색을 " +
            "BlendMode.Src 로 덮어 지우지 않으면 새 값이 옛 값 위에 겹쳐 그려진다. 값을 몇 번 바꿔 보세요 — 크기가 바뀌거나 " +
            "모디파이어가 새로 만들어지면 새 비트맵이라 비침도 사라진다.",
        containerColor = Color(0xFFFFEBEE)
    ) {
        StageRow("clearEachFrame = true (원본)") {
            PriceText(SameWidthPrices[index], cleared)
        }
        StageRow("clearEachFrame = false — 지우기 생략") {
            PriceText(SameWidthPrices[index], notCleared)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DemoButton("값 바꾸기", Color(0xFFC62828)) { index = (index + 1) % SameWidthPrices.size }
            Spacer(modifier = Modifier.width(8.dp))
            Text("지금 값: ${SameWidthPrices[index]}", fontSize = 12.sp, color = Color(0xFF616161))
        }
    }
}

// ==================== 7. 함정: 레이어 없는 DstIn ====================

@Composable
private fun FeatherLayerCard() {
    SectionCard(
        title = "⚠️ 함정 ② saveLayer 없이 DstIn 을 쓰면 배경까지 깎인다",
        description = "가장자리 페더는 이미 그려진 것의 알파를 그라데이션으로 깎는 BlendMode.DstIn 이다. saveLayer 로 모자이크만 " +
            "담은 레이어를 만들지 않으면 먼저 그려진 노란 배경과 카드까지 깎인다. 별도 레이어가 없는 노드는 창 버퍼에 바로 그려지므로 " +
            "알파 0 이 된 가장자리가 투명이 아니라 검은 띠로 보인다(SM-A725F · API 30 에뮬레이터 동일).",
        containerColor = Color(0xFFFFFDE7)
    ) {
        StageRow("featherEdges = false — 가장자리가 딱 잘림", background = Color(0xFFFFD54F)) {
            PriceText(SameWidthPrices[0], Modifier.pixelate(featherEdges = false))
        }
        StageRow("featherEdges = true, saveLayer 안 (원본)", background = Color(0xFFFFD54F)) {
            PriceText(SameWidthPrices[0], Modifier.pixelate())
        }
        StageRow("featherEdges = true, saveLayer 없음", background = Color(0xFFFFD54F)) {
            PriceText(SameWidthPrices[0], Modifier.pixelate(featherInLayer = false))
        }
        Spacer(modifier = Modifier.height(4.dp))
        CodeBox(
            "drawContext.canvas.saveLayer(Rect(0f, 0f, width, height), Paint())\n" +
                "drawImage(fine, dstSize = size, filterQuality = FilterQuality.None)\n" +
                "drawRect(brush = horizontalGradient(Transparent → Black), blendMode = BlendMode.DstIn)  // ×4 변\n" +
                "drawContext.canvas.restore()"
        )
    }
}

// ==================== 8. 시맨틱 누출 ====================

@Composable
private fun SemanticsCard() {
    SectionCard(
        title = "🔊 화면에선 가려도 접근성 트리에는 값이 남는다",
        description = "pixelate 는 그리기만 바꾼다. 값은 컴포지션과 시맨틱 트리에 그대로 있어 TalkBack 이 읽고, 같은 트리를 쓰는 " +
            "uiautomator 덤프에도 나온다(실측). 가린 의미를 접근성까지 맞추려면 clearAndSetSemantics 로 대체 설명을 준다. " +
            "어느 쪽이든 보안 경계는 아니다 — 메모리·디버거에는 값이 있다(README Caveats).",
        containerColor = Color(0xFFE0F7FA)
    ) {
        StageRow("Modifier.pixelate() 만") {
            PriceText(SameWidthPrices[1], Modifier.pixelate())
        }
        StageRow("+ clearAndSetSemantics { contentDescription = … }") {
            PriceText(
                SameWidthPrices[1],
                Modifier
                    .pixelate()
                    .clearAndSetSemantics { contentDescription = "프리미엄 전용 금액" }
            )
        }
        NoteBox(
            "SM-A725F uiautomator 덤프: 위 줄은 text=\"${SameWidthPrices[1]}\" 노드가 그대로 나오고, " +
                "아래 줄은 텍스트 노드 없이 content-desc=\"프리미엄 전용 금액\" 하나만 나온다."
        )
    }
}

// ==================== 9. blur 와 비교 ====================

@Composable
private fun BlurCompareCard() {
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    SectionCard(
        title = "🌫️ Modifier.blur 와 비교",
        description = "Modifier.blur 는 RenderEffect 라 API 31(Android 12) 이상에서만 동작하고 그 아래에서는 조용히 무시돼 값이 " +
            "그대로 보인다(API 30 에뮬레이터: 4dp·12dp 모두 또렷이 읽힘). pixelate 는 비트맵 그리기뿐이라 minSdk 24 부터 같은 모습이다. " +
            "31 이상에서도 반지름이 작으면 읽힌다(SM-A725F: 4dp 는 읽히고 12dp 는 가려짐).",
        containerColor = Color(0xFFECEFF1)
    ) {
        InfoLine("이 기기", "API ${Build.VERSION.SDK_INT} · blur ${if (blurSupported) "적용됨" else "무시됨(값이 그대로 보임)"}")
        Spacer(modifier = Modifier.height(4.dp))
        StageRow("Modifier.pixelate()") {
            PriceText(SameWidthPrices[2], Modifier.pixelate())
        }
        StageRow("Modifier.blur(4.dp)") {
            PriceText(SameWidthPrices[2], Modifier.blur(4.dp))
        }
        StageRow("Modifier.blur(12.dp)") {
            PriceText(SameWidthPrices[2], Modifier.blur(12.dp))
        }
    }
}

// ==================== 10. 비용 · 함정 ③ ====================

private data class CostSnapshot(val drawsPerSecond: Long, val allocations: Int, val averageMicros: Double)

private fun PixelateStats.snapshot(lastDraws: Long) = CostSnapshot((draws - lastDraws) * 2, allocations, averageMicros())

@Composable
private fun CostCard() {
    val plainStats = remember { PixelateStats() }
    val inlineStats = remember { PixelateStats() }
    val rememberedStats = remember { PixelateStats() }
    val nodeStats = remember { PixelateStats() }
    val allStats = listOf(plainStats, inlineStats, rememberedStats, nodeStats)
    // 요소를 고정하는 두 방법 — 호출부 remember, 또는 data class 요소의 Modifier.Node
    val rememberedPixelate = remember { Modifier.pixelate(stats = rememberedStats) }
    var running by remember { mutableStateOf(false) }
    var large by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }
    var snapshots by remember { mutableStateOf(List(4) { CostSnapshot(0, 0, 0.0) }) }

    // 매 프레임 값을 바꾼다 — 글자 수가 같아 크기는 그대로, 대신 이 카드가 매 프레임 리컴포즈된다
    LaunchedEffect(running) {
        while (running) {
            withFrameNanos { }
            tick++
        }
    }
    // 집계는 그리기 단계의 일반 필드 — 0.5초마다 읽어 화면 상태로 옮긴다(이 갱신도 카드를 리컴포즈한다)
    LaunchedEffect(Unit) {
        val last = LongArray(allStats.size)
        while (true) {
            delay(500)
            snapshots = allStats.mapIndexed { i, stats -> stats.snapshot(last[i]).also { last[i] = stats.draws } }
        }
    }

    val value = "₩" + (tick.toLong() * 7_919 % 10_000_000).toString().padStart(7, '0')
    val fontSize = if (large) 34.sp else PriceFontSizeSp.sp

    SectionCard(
        title = "⏱️ 비용 · 함정 ③ — 리컴포즈마다 비트맵을 새로 만든다",
        description = "원본처럼 Modifier.pixelate() = drawWithCache { … } 로 만들면 부를 때마다 새 람다가 생기고, drawWithCache 요소는 " +
            "람다 동일성으로 비교해 호출부가 리컴포즈될 때마다 캐시 블록(비트맵 두 장 할당)이 다시 돈다(ui 1.12.1 소스). README 의 " +
            "'크기마다 한 번 할당'은 요소가 고정될 때만 맞다 — remember 로 고정하거나 data class 요소의 Modifier.Node 로 만든다.",
        containerColor = Color(0xFFE8EAF6)
    ) {
        StageRow("그대로 그리기 (drawContent 만 측정)") {
            PriceText(value, Modifier.measuredPlainDraw(plainStats), fontSize)
        }
        StageRow("Modifier.pixelate() — 매번 새로 부름(원본 사용법)") {
            PriceText(value, Modifier.pixelate(stats = inlineStats), fontSize)
        }
        StageRow("remember { Modifier.pixelate() }") {
            PriceText(value, rememberedPixelate, fontSize)
        }
        StageRow("Modifier.pixelateNode() — Modifier.Node") {
            PriceText(value, Modifier.pixelateNode(stats = nodeStats), fontSize)
        }
        Spacer(modifier = Modifier.height(4.dp))
        listOf("그대로", "매번", "remember", "Node").zip(snapshots).forEach { (label, snapshot) -> CostRow(label, snapshot) }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(if (running) "멈춤" else "매 프레임 바꾸기", Color(0xFF3949AB)) { running = !running }
            DemoButton(if (large) "글꼴 28sp" else "글꼴 34sp", Color(0xFF00897B)) { large = !large }
            DemoButton("초기화", Color(0xFF616161)) { allStats.forEach { it.reset() } }
        }
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "private data class PixelateElement(val blockRatio: Float, …)\n" +
                "    : ModifierNodeElement<PixelateNode>() {\n" +
                "    override fun create() = PixelateNode(this)\n" +
                "    override fun update(node: PixelateNode) = node.update(this)\n" +
                "}\n" +
                "// 노드: 크기나 매개변수가 바뀔 때만 비트맵을 새로 만들고 draw() 에서 같은 그리기"
        )
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "멈춰 있어도 0.5초마다 집계를 화면에 옮기는 리컴포지션 때문에 '매번' 줄의 할당이 계속 오른다. 글꼴 크기를 바꾸면 " +
                "크기가 달라져 remember·Node 도 할당이 1 늘어난다. debug 빌드는 HotSwan 인터프리터가 섞여 시간이 크게 나온다 — " +
                "실측 값은 exampleGuide.kt."
        )
    }
}

private fun PixelateStats.averageMicros(): Double =
    if (draws == 0L) 0.0 else totalDrawNanos / draws / 1_000.0

@Composable
private fun CostRow(label: String, snapshot: CostSnapshot) {
    Text(
        text = "%-8s 그리기 %3d회/초 · 할당 %d · 평균 %.1f µs".format(
            java.util.Locale.ROOT, label, snapshot.drawsPerSecond, snapshot.allocations, snapshot.averageMicros
        ),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF283593)
    )
}

// ==================== 공통 조각 ====================

@Composable
private fun PriceText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = PriceFontSizeSp.sp) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF212121),
        modifier = modifier
    )
}

@Composable
private fun StageRow(
    label: String,
    background: Color = Color.White,
    content: @Composable () -> Unit
) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
    Spacer(modifier = Modifier.height(2.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        content()
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
        Text(value, fontSize = 12.sp, color = Color(0xFF424242), fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SmallLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF616161),
        fontFamily = FontFamily.Monospace,
        lineHeight = 15.sp,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun CodeBox(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp),
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = Color(0xFFE0E0E0),
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun NoteBox(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x14000000))
            .padding(10.dp),
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = Color(0xFF424242)
    )
}

@Composable
private fun DemoButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}
