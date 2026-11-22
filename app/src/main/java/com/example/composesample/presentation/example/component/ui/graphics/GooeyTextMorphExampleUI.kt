package com.example.composesample.presentation.example.component.ui.graphics

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.get
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.component.ValueSlider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Gooey 텍스트 모핑 예제
 *
 * 두 단어를 겹쳐 그리고 떠나는 쪽은 점점 흐리게 · 오는 쪽은 또렷하게 만든 뒤, 부모 레이어 한 곳에서
 * `graphicsLayer { colorFilter = ColorFilter.colorMatrix(알파 행 gain · bias) }` 로 알파를 임계 처리하면 흐린 꼬리가
 * 단단한 덩어리가 되고 겹친 덩어리가 하나로 녹아 붙는다. 원문의 세 주장(부모 한 레이어에서만 붙는다 · 오프셋은
 * 0~255 척도 · 블러가 레이어 경계에서 잘린다)을 PixelCopy 로 읽은 화면 픽셀로 잰다 — 둘째 · 셋째는 그대로 맞고,
 * 첫째는 '다리'만큼의 차이였다. 계산은 GooeyMorphLab.kt, 출처 · 실측은 exampleGuide.kt.
 */
@Composable
fun GooeyTextMorphExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Gooey Text Morph Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { LayersCard() }
            item { CurvesCard() }
            item { ParentLayerCard() }
            item { OffsetScaleCard() }
            item { PaddingCard() }
        }
    }
}

// ==================== 공통 데이터 ====================

private val MorphWords = listOf("Compose", "Gooey", "Morph", "Android")
private val InkColor = Color(0xFF1A237E)
private val InkArgb = InkColor.toArgb()
private val DefaultConfig = GooeyMorphConfig()

// 블러 · 진행은 코드 기본값 그대로 두고 임계만 데모 페이지 값 — 임계 하나의 차이만 비교한다
private val LiveThresholdConfig = DefaultConfig.copy(
    thresholdGain = GooeyLivePagePreset.thresholdGain,
    thresholdBias = GooeyLivePagePreset.thresholdBias
)

/** 임계를 어디에 거는가 */
private enum class Threshold { Parent, PerWord, None }

private fun blurSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private fun thresholdFilter(gain: Float, bias: Float): ColorFilter =
    ColorFilter.colorMatrix(ColorMatrix(GooeyMorph.thresholdMatrix(gain, bias)))

private fun Float.fmt(digits: Int = 2): String = String.format(Locale.US, "%.${digits}f", this)

// ==================== 그리기 ====================

/**
 * 두 단어를 겹쳐 그리는 무대. fraction 은 람다로 받아 graphicsLayer 블록 안에서만 읽는다 —
 * 매 프레임 바뀌는 값이 레이어 속성만 고치고 리컴포지션을 만들지 않는다.
 */
@Composable
private fun GooeyWords(
    outgoing: String,
    incoming: String,
    fraction: () -> Float,
    modifier: Modifier = Modifier,
    config: GooeyMorphConfig = DefaultConfig,
    threshold: Threshold = Threshold.Parent,
    blur: Boolean = true,
    padded: Boolean = true,
    fontSize: TextUnit = 40.sp
) {
    val filter = remember(config.thresholdGain, config.thresholdBias) {
        thresholdFilter(config.thresholdGain, config.thresholdBias)
    }
    // 원문: 블러 상한 + 8dp — 흐린 꼬리가 임계 레이어 경계 안에 들어오게
    val pad = if (padded) (config.blurMaxDp + 8f).dp else 0.dp
    Box(
        modifier = modifier
            .graphicsLayer {
                // 부모 한 곳 — 두 단어가 한 오프스크린 레이어에 합쳐진 뒤 임계된다
                if (threshold == Threshold.Parent) colorFilter = filter
                clip = false
            }
            .then(if (threshold == Threshold.PerWord) Modifier else Modifier.padding(pad)),
        contentAlignment = Alignment.Center
    ) {
        for (isOutgoing in listOf(true, false)) {
            val text = if (isOutgoing) outgoing else incoming
            if (threshold == Threshold.PerWord) {
                // 단어마다 자기 레이어에서 임계 — 같은 자리에 겹쳐 있어도 서로의 꼬리를 모른다
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            colorFilter = filter
                            clip = false
                        }
                        .padding(pad)
                ) {
                    MorphWord(text, isOutgoing, fraction, config, blur, fontSize)
                }
            } else {
                MorphWord(text, isOutgoing, fraction, config, blur, fontSize)
            }
        }
    }
}

@Composable
private fun MorphWord(
    text: String,
    isOutgoing: Boolean,
    fraction: () -> Float,
    config: GooeyMorphConfig,
    blur: Boolean,
    fontSize: TextUnit
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        color = InkColor,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.graphicsLayer {
            val f = fraction()
            val t = if (isOutgoing) {
                GooeyMorph.outgoingT(f, config.morphSequence)
            } else {
                GooeyMorph.incomingT(f, config.morphSequence)
            }
            alpha = GooeyMorph.alpha(t)
            val blurPx = if (blur) GooeyMorph.blurDp(t, config.blurBaseDp, config.blurMaxDp).dp.toPx() else 0f
            // BlurEffect 는 RenderEffect — API 31 미만에서는 레이어가 무시한다(크로스페이드 + 임계만 남는다)
            renderEffect = if (blurPx > 0f) BlurEffect(blurPx, blurPx, TileMode.Decal) else null
            clip = false
        }
    )
}

// ==================== 1. 개요 ====================

/** withFrameMillis 루프가 움직이는 진행 + 무대 리컴포지션 집계 */
private class MorphPlayer(val config: GooeyMorphConfig) {
    val clock = MorphClock(config)
    var index by mutableIntStateOf(0)
    var fraction by mutableFloatStateOf(0f)
    var playing by mutableStateOf(true)

    // SideEffect 가 세는 일반 필드 — 상태가 아니라서 세는 일 자체가 다시 그리기를 만들지 않는다
    var stageCompositions = 0
}

@Composable
private fun OverviewCard() {
    var livePreset by remember { mutableStateOf(false) }
    val config = if (livePreset) GooeyLivePagePreset else DefaultConfig
    val player = remember(config) { MorphPlayer(config) }
    var thresholdOn by remember { mutableStateOf(true) }
    var shownCompositions by remember { mutableIntStateOf(0) }

    // 원문과 같은 프레임 루프 — 프레임 사이 시간(dt)으로 진행을 넘긴다. 쉬었다 돌아온 첫 프레임이 튀지 않게 dt 는 0.1초로 자른다
    LaunchedEffect(player, player.playing) {
        if (!player.playing) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (true) {
            withFrameMillis { now ->
                val dt = ((now - last) / 1000f).coerceAtMost(0.1f)
                last = now
                if (player.clock.advance(dt, MorphWords.size)) player.index = player.clock.index
                player.fraction = player.clock.fraction
            }
        }
    }
    // 집계를 0.5초마다 화면 상태로 옮긴다(이 갱신은 카드를 다시 그리지만 무대는 건너뛴다)
    LaunchedEffect(player) {
        while (true) {
            delay(500)
            shownCompositions = player.stageCompositions
        }
    }

    SectionCard(
        title = "🫠 Gooey 모핑 — 흐린 꼬리를 알파 임계로 굳히기",
        description = "두 단어를 같은 자리에 겹쳐 그리고, 떠나는 쪽은 점점 흐리고 옅게 · 오는 쪽은 또렷하고 진하게 만든다. 둘을 " +
            "감싼 부모 레이어 한 곳에 알파 행만 gain 18 · bias −7 인 ColorMatrix 를 걸면 옅은 블러 꼬리가 단단한 잉크가 되고, " +
            "겹친 꼬리끼리 하나로 녹아 붙는다.",
        containerColor = Color(0xFFE8EAF6)
    ) {
        StageBox {
            MorphStage(player, thresholdOn)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(if (player.playing) "멈춤" else "재생", Color(0xFF3949AB)) {
                if (player.playing) player.clock.seek(player.fraction)
                player.playing = !player.playing
            }
            DemoButton(if (thresholdOn) "임계 끄기" else "임계 켜기", Color(0xFF00897B)) { thresholdOn = !thresholdOn }
            DemoButton(if (livePreset) "코드 기본값" else "데모 페이지 값", Color(0xFF6D4C41)) { livePreset = !livePreset }
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (player.playing) {
            SmallLabel("멈추면 진행(fraction)을 끌어 한 장면씩 볼 수 있다")
        } else {
            ValueSlider(
                value = player.fraction,
                onValueChange = {
                    player.clock.seek(it)
                    player.fraction = it
                }
            )
            FractionValues(player.fraction, config)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val i = player.index
        InfoLine("단어", "${MorphWords[i]} → ${MorphWords[(i + 1) % MorphWords.size]}")
        InfoLine("무대 리컴포지션", "${shownCompositions}회 (단어가 바뀔 때만 +1)")
        InfoLine(
            if (livePreset) "데모 페이지 값" else "코드 기본값",
            "모핑 ${config.morphSeconds.fmt()}s · 쉼 ${config.cooldownSeconds.fmt()}s · 블러 ${config.blurBaseDp.fmt()}/" +
                "${config.blurMaxDp.fmt()}dp · gain ${config.thresholdGain.fmt(1)} · bias ${config.thresholdBias.fmt()} · " +
                "p ${GooeyMorph.hold(config.morphSequence).fmt(3)}"
        )
        InfoLine(
            "이 기기",
            "API ${Build.VERSION.SDK_INT} · 블러 " + if (blurSupported()) "적용" else "무시(RenderEffect 는 API 31+)"
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "Box(Modifier.graphicsLayer { colorFilter = threshold }) {   // 부모 한 곳\n" +
                "    Box(Modifier.padding(blurMax + 8.dp)) {\n" +
                "        Text(old, Modifier.graphicsLayer {\n" +
                "            alpha = t1.pow(0.4f)\n" +
                "            renderEffect = BlurEffect(blur(t1), blur(t1), TileMode.Decal)\n" +
                "        })\n" +
                "        Text(new, /* 같은 식, t2 */)\n" +
                "    }\n" +
                "}\n" +
                "// threshold = ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(\n" +
                "//     1,0,0,0,0,  0,1,0,0,0,  0,0,1,0,0,  0,0,0, 18f, -7f)))"
        )
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "코드 기본값 18 · −7 은 입력 알파 약 6% 만 넘어도 255 로 올린다 — 떠나는 단어의 블러가 거의 끝까지 덩어리로 남아 " +
                "오는 단어를 덮고, 덩어리 바깥엔 블러의 8비트 알파 1~14 단계가 계단 띠로 보인다. 원문 데모 페이지 값" +
                "(3.9 · −38.25 — 약 4% ~ 29% 사이를 편다)은 글자가 일찍 드러나고 띠가 얇다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "진행은 graphicsLayer 블록 안에서만 읽어 매 프레임 레이어 속성만 바뀐다 — 무대는 단어가 바뀔 때만 리컴포즈되고, " +
                "멈춘 채 슬라이더를 끌어도 0 이다(원문은 진행을 컴포지션에서 읽는다). API 31 미만에서는 블러가 없어 두 단어가 " +
                "임계를 지나며 툭 바뀌기만 한다."
        )
    }
}

@Composable
private fun MorphStage(player: MorphPlayer, thresholdOn: Boolean) {
    SideEffect { player.stageCompositions++ }
    val i = player.index
    GooeyWords(
        outgoing = MorphWords[i],
        incoming = MorphWords[(i + 1) % MorphWords.size],
        fraction = { player.fraction },
        config = player.config,
        threshold = if (thresholdOn) Threshold.Parent else Threshold.None,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FractionValues(fraction: Float, config: GooeyMorphConfig = DefaultConfig) {
    val seq = config.morphSequence
    val out = GooeyMorph.look(GooeyMorph.outgoingT(fraction, seq), config)
    val inc = GooeyMorph.look(GooeyMorph.incomingT(fraction, seq), config)
    SmallLabel(
        "f ${fraction.fmt()} · 떠남 t1 ${out.t.fmt()} α ${out.alpha.fmt()} 블러 ${out.blurDp.fmt(1)}dp · " +
            "옴 t2 ${inc.t.fmt()} α ${inc.alpha.fmt()} 블러 ${inc.blurDp.fmt(1)}dp"
    )
}

// ==================== 2. 세 겹 ====================

@Composable
private fun LayersCard() {
    var fraction by remember { mutableFloatStateOf(0.6f) }
    SectionCard(
        title = "🔬 세 겹 — 알파 · 블러 · 부모 임계",
        description = "같은 진행에서 한 겹씩 더해 본다. 알파만이면 두 단어가 겹쳐 비치는 크로스페이드, 블러를 더하면 뿌연 번짐, " +
            "부모에 임계를 걸면 번짐이 단단한 덩어리로 굳는다. 마지막 줄은 블러 없이 임계만 — API 31 미만 기기의 모습이다" +
            "(그 기기에서는 ②가 ①과, ③이 ④와 같다).",
        containerColor = Color(0xFFE3F2FD)
    ) {
        ValueSlider(value = fraction, onValueChange = { fraction = it })
        FractionValues(fraction)
        Spacer(modifier = Modifier.height(8.dp))
        val f = { fraction }
        StageRow("① 알파만 (블러 · 임계 없음)") {
            GooeyWords("Gooey", "Morph", f, Modifier.fillMaxWidth(), threshold = Threshold.None, blur = false, fontSize = 32.sp)
        }
        StageRow("② + 블러 (임계 없음)") {
            GooeyWords("Gooey", "Morph", f, Modifier.fillMaxWidth(), threshold = Threshold.None, fontSize = 32.sp)
        }
        StageRow("③ + 부모 임계 = Gooey") {
            GooeyWords("Gooey", "Morph", f, Modifier.fillMaxWidth(), fontSize = 32.sp)
        }
        StageRow("④ 임계만 (블러 없음 — API 31 미만)") {
            GooeyWords("Gooey", "Morph", f, Modifier.fillMaxWidth(), blur = false, fontSize = 32.sp)
        }
    }
}

// ==================== 3. 곡선 ====================

@Composable
private fun CurvesCard() {
    val config = DefaultConfig
    val hold = GooeyMorph.hold(config.morphSequence)
    val capT = GooeyMorph.capT(config.blurBaseDp, config.blurMaxDp)
    SectionCard(
        title = "📈 곡선 — 앞 1/4 버티기 · t^0.4 · blurBase/t − blurBase",
        description = "진행 f 를 떠나는 쪽 t1 · 오는 쪽 t2 로 나눈다. 앞 p = morphSequence / 2 = 0.25 동안은 옛 단어가 그대로 " +
            "버티고 나머지 3/4 에서 교차한다. 알파 t^0.4 는 늦게까지 진하게 남아 임계에 넘길 꼬리를 늘리고, 블러는 t 가 " +
            "줄수록 빠르게 커지다 상한 40dp 에서 멈춘다.",
        containerColor = Color(0xFFFFF8E1)
    ) {
        CurveChart(config)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Legend(Color(0xFF3949AB), "α 떠남")
            Legend(Color(0xFFE53935), "α 옴")
            Legend(Color(0xFF3949AB), "블러 떠남", dashed = true)
            Legend(Color(0xFFE53935), "블러 옴", dashed = true)
        }
        Spacer(modifier = Modifier.height(8.dp))
        InfoLine("버티는 앞부분 p", "${hold.fmt()} (0.499 상한)")
        InfoLine("블러 상한에 닿는 t", "${capT.fmt(3)} = base / (base + max)")
        Spacer(modifier = Modifier.height(4.dp))
        listOf(1f, 0.5f, 0.25f, capT, 0.1f).forEach { t ->
            val look = GooeyMorph.look(t, config)
            SmallLabel("t ${t.fmt(3)} → α ${look.alpha.fmt()} · 블러 ${look.blurDp.fmt(1)}dp")
        }
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "val p = (morphSequence * 0.5f).coerceIn(0f, 0.499f)\n" +
                "val t1 = ((1f - f) / (1f - p)).coerceIn(0f, 1f)   // 떠남\n" +
                "val t2 = ((f - p) / (1f - p)).coerceIn(0f, 1f)    // 옴\n" +
                "alpha = t.pow(0.4f)\n" +
                "blur = min(blurBase / max(t, 0.001f) - blurBase, blurMax)"
        )
    }
}

@Composable
private fun CurveChart(config: GooeyMorphConfig) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val w = size.width
        val h = size.height
        val seq = config.morphSequence
        val hold = GooeyMorph.hold(seq)
        // 버티는 구간 음영
        drawRect(Color(0x14000000), topLeft = Offset.Zero, size = androidx.compose.ui.geometry.Size(w * hold, h))
        drawLine(Color(0xFFBDBDBD), Offset(0f, h), Offset(w, h))
        val steps = 120
        fun series(color: Color, dashed: Boolean, value: (Float) -> Float) {
            val path = androidx.compose.ui.graphics.Path()
            for (s in 0..steps) {
                val f = s / steps.toFloat()
                val x = f * w
                val y = h - value(f).coerceIn(0f, 1f) * h
                if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path,
                color,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null
                )
            )
        }
        series(Color(0xFF3949AB), false) { GooeyMorph.alpha(GooeyMorph.outgoingT(it, seq)) }
        series(Color(0xFFE53935), false) { GooeyMorph.alpha(GooeyMorph.incomingT(it, seq)) }
        series(Color(0xFF3949AB), true) {
            GooeyMorph.blurDp(GooeyMorph.outgoingT(it, seq), config.blurBaseDp, config.blurMaxDp) / config.blurMaxDp
        }
        series(Color(0xFFE53935), true) {
            GooeyMorph.blurDp(GooeyMorph.incomingT(it, seq), config.blurBaseDp, config.blurMaxDp) / config.blurMaxDp
        }
    }
}

@Composable
private fun Legend(color: Color, label: String, dashed: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(width = 16.dp, height = 8.dp)) {
            drawLine(
                color,
                Offset(0f, size.height / 2),
                Offset(size.width, size.height / 2),
                strokeWidth = 2.dp.toPx(),
                pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(4f, 3f)) else null
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = Color(0xFF424242))
    }
}

// ==================== 4. 주장 ① 부모 한 레이어 ====================

private const val DotSizeDp = 40
private const val DotBlurDp = 12f
private const val GapMaxDp = 80
private const val GapStepDp = 2

// 블러 12dp(시그마 약 7.4dp)의 꼬리가 임계 뒤 0 이 되는 거리보다 넉넉하게 — 가장자리 잉크로 확인
private const val DotPadDp = 32

/** 간격 하나에서 두 줄의 덩어리 수와 가운데 세로줄 잉크(목 굵기) */
private data class GapSample(
    val gapDp: Int,
    val parentBlobs: Int,
    val separateBlobs: Int,
    val parentNeck: Int,
    val separateNeck: Int
)

@Composable
private fun ParentLayerCard() {
    val window = LocalActivity.current?.window
    val scope = rememberCoroutineScope()
    var gapDp by remember { mutableIntStateOf(24) }
    var liveThreshold by remember { mutableStateOf(false) }
    val config = if (liveThreshold) LiveThresholdConfig else DefaultConfig
    var running by remember { mutableStateOf(false) }
    var samples by remember { mutableStateOf<List<GapSample>>(emptyList()) }
    var words by remember { mutableStateOf<Pair<InkStats, InkStats>?>(null) }
    var message by remember { mutableStateOf("") }
    val parentDots = remember { MeasuredBounds() }
    val separateDots = remember { MeasuredBounds() }
    val parentWords = remember { MeasuredBounds() }
    val separateWords = remember { MeasuredBounds() }

    SectionCard(
        title = "🧪 주장 ① — '부모 한 레이어에서만 녹아 붙는다'",
        description = "원문: 단어마다 따로 임계하면 두 덩어리가 끝내 붙지 않는다. 임계는 그 레이어에 이미 합쳐진 알파에 걸리므로 " +
            "부모 한 곳이면 두 꼬리가 겹친 사이 공간이 다리로 이어지는 건 맞다. 하지만 따로 걸어도 굳은 모양이 맞닿으면 " +
            "픽셀은 이어진다 — 간격을 0 → 80dp 로 훑으며 화면 픽셀(PixelCopy)로 덩어리 수와 가운데 목 굵기를 세어 차이를 잰다.",
        containerColor = Color(0xFFE8F5E9)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(if (liveThreshold) "임계 18 · −7 로" else "임계 3.9 · −38.25 로", Color(0xFF6D4C41)) {
                if (!running) {
                    liveThreshold = !liveThreshold
                    samples = emptyList()
                    words = null
                }
            }
        }
        SmallLabel(
            "점 간격 ${gapDp}dp · 지름 ${DotSizeDp}dp · 블러 ${DotBlurDp.roundToInt()}dp · " +
                "gain ${config.thresholdGain.fmt(1)} · bias ${config.thresholdBias.fmt()}"
        )
        ValueSlider(
            value = gapDp.toFloat(),
            onValueChange = { gapDp = (it / GapStepDp).roundToInt() * GapStepDp },
            valueRange = 0f..GapMaxDp.toFloat(),
            enabled = !running
        )
        StageRow("부모 한 레이어") {
            BlurredDots(gapDp, separate = false, config = config, modifier = Modifier.measured(parentDots))
        }
        StageRow("점마다 따로") {
            BlurredDots(gapDp, separate = true, config = config, modifier = Modifier.measured(separateDots))
        }
        DemoButton(if (running) "측정 중…" else "간격 훑기 · 덩어리 세기", Color(0xFF2E7D32)) {
            if (running) return@DemoButton
            if (window == null || !pixelCopyAvailable()) {
                message = "PixelCopy(창) 는 API 26 이상"
                return@DemoButton
            }
            if (!parentDots.fullyVisible || !separateDots.fullyVisible) {
                message = "점 두 줄이 화면에 다 보이게 스크롤한 뒤 다시 누른다"
                return@DemoButton
            }
            scope.launch {
                running = true
                message = ""
                val original = gapDp
                val result = mutableListOf<GapSample>()
                for (gap in 0..GapMaxDp step GapStepDp) {
                    gapDp = gap
                    awaitFrames()
                    val parent = WindowCapture.analyze(window, parentDots.bounds, InkArgb)
                    val separate = WindowCapture.analyze(window, separateDots.bounds, InkArgb)
                    if (parent == null || separate == null) {
                        message = "PixelCopy 실패(간격 ${gap}dp)"
                        break
                    }
                    result += GapSample(gap, parent.blobs, separate.blobs, parent.centerColumnInk, separate.centerColumnInk)
                }
                gapDp = original
                samples = result
                running = false
                GooeyProbe.log(
                    "gap gain=${config.thresholdGain} " + result.joinToString(" ") {
                        "${it.gapDp}:${it.parentBlobs}/${it.separateBlobs}:${it.parentNeck}/${it.separateNeck}"
                    }
                )
            }
        }
        if (message.isNotEmpty()) SmallLabel(message)
        if (samples.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            InfoLine(
                "한 덩어리 최대 간격",
                "부모 ${mergedUpTo(samples) { it.parentBlobs }} · 따로 ${mergedUpTo(samples) { it.separateBlobs }}"
            )
            samples.filter { it.gapDp % 8 == 0 }.forEach {
                SmallLabel(
                    "간격 ${it.gapDp.toString().padStart(2)}dp — 부모 덩어리 ${it.parentBlobs} · 목 ${it.parentNeck}px  |  " +
                        "따로 덩어리 ${it.separateBlobs} · 목 ${it.separateNeck}px"
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        val wordFraction = { 0.55f }
        StageRow("단어 — 부모 한 레이어 (f 0.55)") {
            GooeyWords(
                "Gooey", "Morph", wordFraction,
                Modifier
                    .fillMaxWidth()
                    .measured(parentWords),
                config = config,
                fontSize = 32.sp
            )
        }
        StageRow("단어 — 단어마다 따로 (f 0.55)") {
            GooeyWords(
                "Gooey", "Morph", wordFraction,
                Modifier
                    .fillMaxWidth()
                    .measured(separateWords),
                config = config,
                threshold = Threshold.PerWord,
                fontSize = 32.sp
            )
        }
        DemoButton("단어 두 줄 덩어리 세기", Color(0xFF2E7D32)) {
            if (window == null || !pixelCopyAvailable()) {
                message = "PixelCopy(창) 는 API 26 이상"
                return@DemoButton
            }
            if (!parentWords.fullyVisible || !separateWords.fullyVisible) {
                message = "단어 두 줄이 화면에 다 보이게 스크롤한 뒤 다시 누른다"
                return@DemoButton
            }
            scope.launch {
                message = ""
                val parentWord = WindowCapture.analyze(window, parentWords.bounds, InkArgb)
                val separateWord = WindowCapture.analyze(window, separateWords.bounds, InkArgb)
                words = if (parentWord != null && separateWord != null) parentWord to separateWord else null
                GooeyProbe.log(
                    "words gain=${config.thresholdGain} parent=${parentWord?.summary()} separate=${separateWord?.summary()}"
                )
            }
        }
        words?.let { (parent, separate) ->
            InfoLine("단어 부모", parent.summary())
            InfoLine("단어 따로", separate.summary())
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "부모가 더하는 것은 다리뿐이다(API 35 에뮬) — 한 덩어리로 이어지는 간격이 2dp 더 길고(18 · −7 은 28 vs 26dp, " +
                "3.9 · −38.25 는 16 vs 14dp) 가운데 목이 6~14px 굵다. 다리가 끊기는 순간엔 가운데에 방울이 따로 남기도 한다" +
                "(18 · −7, 30dp 에서 덩어리 3). 이 모핑처럼 두 단어가 같은 자리에 겹치면 단어마다 따로 걸어도 그림이 거의 " +
                "같다 — f 0.55 잉크 차 18 · −7 은 0.2%, 문턱이 높은 3.9 · −38.25 는 8%."
        )
    }
}

private fun mergedUpTo(samples: List<GapSample>, blobs: (GapSample) -> Int): String {
    val merged = samples.takeWhile { blobs(it) == 1 }
    return if (merged.isEmpty()) "없음" else "${merged.last().gapDp}dp"
}

private fun InkStats.summary(): String = "덩어리 $blobs · 잉크 ${inkPixels}px · 옅은 ${hazePixels}px · 가장자리 ${borderInk}px"

@Composable
private fun BlurredDots(gapDp: Int, separate: Boolean, config: GooeyMorphConfig, modifier: Modifier = Modifier) {
    val filter = remember(config.thresholdGain, config.thresholdBias) {
        thresholdFilter(config.thresholdGain, config.thresholdBias)
    }
    val shift = (gapDp + DotSizeDp) / 2f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height((DotSizeDp + 2 * DotPadDp).dp)
            .graphicsLayer {
                if (!separate) colorFilter = filter
                clip = false
            },
        contentAlignment = Alignment.Center
    ) {
        for (sign in listOf(-1f, 1f)) {
            if (separate) {
                Box(
                    modifier = Modifier
                        .offset(x = (shift * sign).dp)
                        .graphicsLayer {
                            colorFilter = filter
                            clip = false
                        }
                        .padding(DotPadDp.dp)
                ) {
                    BlurredDot()
                }
            } else {
                Box(modifier = Modifier.offset(x = (shift * sign).dp)) { BlurredDot() }
            }
        }
    }
}

@Composable
private fun BoxScope.BlurredDot() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(DotSizeDp.dp)
            .graphicsLayer {
                val px = DotBlurDp.dp.toPx()
                renderEffect = BlurEffect(px, px, TileMode.Decal)
                clip = false
            }
            .background(InkColor, CircleShape)
    )
}

/** 상태를 바꾼 뒤 그 모습이 창 버퍼에 올라갈 때까지 */
private suspend fun awaitFrames(count: Int = 3) {
    repeat(count) { withFrameNanos { } }
}

// ==================== 5. 주장 ② 오프셋 척도 ====================

/** 검정을 입력 알파로 그린 견본에 gain · bias 를 건다 */
private data class Swatch(val inputAlpha: Int, val gain: Float, val bias: Float)

private val Swatches = listOf(
    Swatch(128, 1f, 0f),
    Swatch(128, 1f, -64f),
    Swatch(128, 1f, -0.25f),
    Swatch(10, 18f, -7f),
    Swatch(20, 18f, -7f)
)

/** 같은 필터를 소프트웨어 캔버스(ImageBitmap)에 그려 출력 알파를 바로 읽는다 */
private fun softwareFilteredAlpha(swatch: Swatch): Int {
    val bitmap = ImageBitmap(4, 4)
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.Black.copy(alpha = swatch.inputAlpha / 255f)
        colorFilter = thresholdFilter(swatch.gain, swatch.bias)
    }
    canvas.drawRect(0f, 0f, 4f, 4f, paint)
    return bitmap.asAndroidBitmap()[1, 1] ushr 24
}

@Composable
private fun OffsetScaleCard() {
    val window = LocalActivity.current?.window
    val scope = rememberCoroutineScope()
    val software = remember { Swatches.map { softwareFilteredAlpha(it) } }
    val swatchBounds = remember { Swatches.map { MeasuredBounds() } }
    var screen by remember { mutableStateOf<List<Float>?>(null) }
    val liveBounds = remember { MeasuredBounds() }
    val portedBounds = remember { MeasuredBounds() }
    var haze by remember { mutableStateOf<Pair<InkStats, InkStats>?>(null) }
    var message by remember { mutableStateOf("") }
    val live = LiveThresholdConfig
    val ported = live.copy(thresholdBias = live.thresholdBias / 255f)

    SectionCard(
        title = "🎚️ 주장 ② — 오프셋 열은 0~255 척도",
        description = "Android ColorMatrix 는 A' = gain·A + bias 를 채널과 같은 0~255 에서 계산해 자른다(문서). 그래서 bias −7 은 " +
            "'알파 7 빼기'이고, gain 18 이면 입력 알파 0.4 이하는 0 · 14.6(약 6%) 이상은 255 가 된다. 원문은 옛 Compose " +
            "Multiplatform(Skia)이 같은 값을 −1~1 로 읽었다고 한다. 검정 견본으로 두 척도의 예상과 실제 출력을 대조하고, " +
            "−1~1 척도로 계산한 bias(−38.25 / 255)를 그대로 옮기면 문턱이 사라져 옅은 띠가 남는 것을 본다.",
        containerColor = Color(0xFFFCE4EC)
    ) {
        Swatches.forEachIndexed { i, s ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, Color(0xFFE0E0E0))
                        .background(Color.White)
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .measured(swatchBounds[i])
                            .graphicsLayer { colorFilter = thresholdFilter(s.gain, s.bias) }
                            .background(Color.Black.copy(alpha = s.inputAlpha / 255f))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    SmallLabel("입력 α ${s.inputAlpha} · gain ${s.gain.fmt(0)} · bias ${s.bias.fmt(2)}")
                    val asBytes = GooeyMorph.thresholdAlpha(s.inputAlpha.toFloat(), s.gain, s.bias)
                    val asUnit = GooeyMorph.thresholdAlphaNormalizedOffset(s.inputAlpha.toFloat(), s.gain, s.bias)
                    SmallLabel(
                        "예상 0~255 ${asBytes.fmt(1)} · −1~1 ${asUnit.fmt(1)} · 소프트웨어 ${software[i]}" +
                            (screen?.getOrNull(i)?.let { if (it.isNaN()) " · 화면 밖" else " · 화면 ${it.fmt(1)}" } ?: "")
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        InfoLine(
            "18 · −7",
            "입력 α ≤ ${GooeyMorph.cutoff(18f, -7f).fmt()} → 0 · α ≥ ${GooeyMorph.saturation(18f, -7f).fmt(1)} → 255"
        )
        InfoLine(
            "3.9 · −38.25",
            "입력 α ≤ ${GooeyMorph.cutoff(live.thresholdGain, live.thresholdBias).fmt(1)} → 0 · " +
                "α ≥ ${GooeyMorph.saturation(live.thresholdGain, live.thresholdBias).fmt(1)} → 255"
        )
        Spacer(modifier = Modifier.height(8.dp))
        val wordFraction = { 0.40f }
        StageRow("데모 페이지 임계 3.9 · −38.25") {
            GooeyWords(
                "Gooey", "Morph", wordFraction,
                Modifier
                    .fillMaxWidth()
                    .measured(liveBounds),
                config = live,
                fontSize = 32.sp
            )
        }
        StageRow("−1~1 척도 값을 옮겨 온 3.9 · ${ported.thresholdBias.fmt(3)}") {
            GooeyWords(
                "Gooey", "Morph", wordFraction,
                Modifier
                    .fillMaxWidth()
                    .measured(portedBounds),
                config = ported,
                fontSize = 32.sp
            )
        }
        DemoButton("화면 픽셀 읽기", Color(0xFFAD1457)) {
            if (window == null || !pixelCopyAvailable()) {
                message = "PixelCopy(창) 는 API 26 이상"
                return@DemoButton
            }
            val rowsVisible = liveBounds.fullyVisible && portedBounds.fullyVisible
            if (swatchBounds.none { it.fullyVisible } && !rowsVisible) {
                message = "견본이나 두 줄이 화면에 보이게 스크롤한 뒤 다시 누른다"
                return@DemoButton
            }
            scope.launch {
                // 화면에 다 보이는 것만 읽는다(작은 화면은 두 번에 나눠 잰다)
                message = if (rowsVisible) "" else "두 줄은 화면에 다 보일 때만 잰다"
                screen = swatchBounds.mapIndexed { i, b ->
                    if (!b.fullyVisible) return@mapIndexed screen?.getOrNull(i) ?: Float.NaN
                    // 테두리 영향이 없는 가운데 절반만
                    val r = b.bounds
                    val inner = Rect(r.left + r.width / 4, r.top + r.height / 4, r.right - r.width / 4, r.bottom - r.height / 4)
                    WindowCapture.read(window, inner)?.let { InkAnalysis.blackAlphaOnWhite(it.pixels) } ?: Float.NaN
                }
                val a = if (rowsVisible) WindowCapture.analyze(window, liveBounds.bounds, InkArgb) else null
                val b = if (rowsVisible) WindowCapture.analyze(window, portedBounds.bounds, InkArgb) else null
                if (a != null && b != null) haze = a to b
                GooeyProbe.log(
                    "offset software=$software screen=${screen?.map { it.fmt(1) }} " +
                        "| live=${a?.summary()} ported=${b?.summary()}"
                )
            }
        }
        if (message.isNotEmpty()) SmallLabel(message)
        haze?.let { (a, b) ->
            InfoLine("3.9 · −38.25", a.summary())
            InfoLine("3.9 · ${ported.thresholdBias.fmt(3)}", b.summary())
        }
    }
}

// ==================== 6. 주장 ③ 패딩 ====================

/** f 하나에서 두 줄의 가장자리 잉크 */
private data class EdgeSample(val fraction: Float, val padded: Int, val bare: Int)

@Composable
private fun PaddingCard() {
    val window = LocalActivity.current?.window
    val scope = rememberCoroutineScope()
    var fraction by remember { mutableFloatStateOf(0.45f) }
    var running by remember { mutableStateOf(false) }
    var samples by remember { mutableStateOf<List<EdgeSample>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    val paddedBounds = remember { MeasuredBounds() }
    val bareBounds = remember { MeasuredBounds() }

    SectionCard(
        title = "✂️ 주장 ③ — 블러는 임계 레이어 경계에서 잘린다",
        description = "colorFilter 를 건 레이어는 자기 크기의 오프스크린에 그려진 뒤 합성된다. 글자의 블러는 clip = false 라 " +
            "글자 밖으로 번지지만, 임계 레이어가 글자 크기뿐이면 그 경계에서 잘려 덩어리 위아래가 칼로 자른 듯 평평해진다. " +
            "원문처럼 블러 상한 + 8dp 를 안쪽 패딩으로 두면 꼬리가 레이어 안에 들어온다. 빨간 테두리가 레이어 경계다.",
        containerColor = Color(0xFFFFF3E0)
    ) {
        ValueSlider(value = fraction, onValueChange = { fraction = it }, enabled = !running)
        FractionValues(fraction)
        Spacer(modifier = Modifier.height(8.dp))
        val f = { fraction }
        StageRow("패딩 48dp (블러 상한 40 + 8)") {
            BoundsOutline {
                GooeyWords("Gooey", "Morph", f, Modifier.fillMaxWidth().measured(paddedBounds), fontSize = 32.sp)
            }
        }
        StageRow("패딩 0") {
            BoundsOutline {
                GooeyWords(
                    "Gooey", "Morph", f,
                    Modifier
                        .fillMaxWidth()
                        .measured(bareBounds),
                    padded = false,
                    fontSize = 32.sp
                )
            }
        }
        DemoButton(if (running) "측정 중…" else "f 0 → 1 훑기 · 가장자리 잉크", Color(0xFFEF6C00)) {
            if (running) return@DemoButton
            if (window == null || !pixelCopyAvailable()) {
                message = "PixelCopy(창) 는 API 26 이상"
                return@DemoButton
            }
            if (!paddedBounds.fullyVisible || !bareBounds.fullyVisible) {
                message = "두 줄이 화면에 다 보이게 스크롤한 뒤 다시 누른다"
                return@DemoButton
            }
            scope.launch {
                running = true
                message = ""
                val original = fraction
                val result = mutableListOf<EdgeSample>()
                for (s in 0..20) {
                    fraction = s / 20f
                    awaitFrames()
                    val padded = WindowCapture.analyze(window, paddedBounds.bounds, InkArgb)
                    val bare = WindowCapture.analyze(window, bareBounds.bounds, InkArgb)
                    if (padded == null || bare == null) {
                        message = "PixelCopy 실패(f ${fraction.fmt()})"
                        break
                    }
                    result += EdgeSample(fraction, padded.borderInk, bare.borderInk)
                }
                fraction = original
                samples = result
                running = false
                GooeyProbe.log("edge " + result.joinToString(" ") { "${it.fraction.fmt()}:${it.padded}/${it.bare}" })
            }
        }
        if (message.isNotEmpty()) SmallLabel(message)
        if (samples.isNotEmpty()) {
            val maxPadded = samples.maxBy { it.padded }
            val maxBare = samples.maxBy { it.bare }
            InfoLine("가장자리 잉크 최대", "패딩 48dp ${maxPadded.padded}px · 패딩 0 ${maxBare.bare}px (f ${maxBare.fraction.fmt()})")
            SmallLabel("f 0.00~1.00 21장 — 0 이면 레이어 경계에 닿은 잉크가 없다(잘리지 않았다)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측(API 35 에뮬): 패딩 48dp 는 f 21장 모두 가장자리 잉크 0px, 패딩 0 은 f 0.30~0.95 에서 255~450px — 블러가 " +
                "가장 큰 구간 내내 잘린다. TileMode.Decal 은 레이어 밖을 투명으로 보고 흐린다. 기본값 Clamp 는 가장자리 픽셀을 바깥으로 늘여 번지게 한다 — " +
                "글자처럼 가장자리가 투명한 그림에는 차이가 작지만 원문은 Decal 을 쓴다."
        )
    }
}

@Composable
private fun BoundsOutline(content: @Composable () -> Unit) {
    // 테두리를 레이어 바깥 2dp 에 그린다 — 경계 안쪽 1px 를 세는 측정에 테두리가 섞이지 않게
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE53935))
            .padding(2.dp)
    ) {
        content()
    }
}

// 화면 픽셀 읽기(WindowCapture · MeasuredBounds)는 같은 패키지 WindowPixelCapture.kt — drawMesh 리본 예제와 함께 쓴다

/** 측정 결과를 logcat 에도 남긴다(adb 로 대조할 때 — 화면 글자 긁기보다 정확하다) */
private object GooeyProbe {
    fun log(message: String) {
        android.util.Log.i("GooeyMorph", message)
    }
}

// ==================== 공통 조각 ====================

@Composable
private fun StageBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

@Composable
private fun StageRow(label: String, content: @Composable () -> Unit) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
    Spacer(modifier = Modifier.height(2.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
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
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
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
            .background(Color(0x14000000), RoundedCornerShape(8.dp))
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
