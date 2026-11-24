package com.example.composesample.presentation.example.component.ui.shapes

import android.annotation.SuppressLint
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CornerRounding
import androidx.compose.foundation.shape.MorphPolygonShape
import androidx.compose.foundation.shape.PolygonShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.toPolygonShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch

private val Indigo = Color(0xFF283593)
private val Teal = Color(0xFF00695C)
private val Plum = Color(0xFF6A1B9A)
private val Amber = Color(0xFFE65100)
private val Slate = Color(0xFF37474F)
private val Muted = Color(0xFF757575)
private val Body = Color(0xFF424242)
private val Matched = Color(0xFF2E7D32)
private val Mismatch = Color(0xFFC62828)
private val AvatarBrush = Brush.linearGradient(listOf(Color(0xFF7986CB), Color(0xFF283593)))

@Composable
fun PolygonShapeExampleUI(onBackEvent: () -> Unit) {
    // 카드 결과는 카드가 스크롤로 빠졌다 돌아와도 남도록 화면 루트에 둔다
    val morphLab = remember { MorphLabState() }
    val transformLab = remember { TransformLabState() }
    val cacheBench = remember { CacheBenchState() }
    val zeroLab = remember { ZeroLabState() }
    val conversionLab = remember { ConversionLabState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4FA))
    ) {
        MainHeader(title = "PolygonShape · MorphPolygonShape", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { OverviewCard() }
            item { GalleryCard() }
            item { MorphCard(morphLab) }
            item { TransformCard(transformLab, cacheBench) }
            item { ZeroSizeCard(zeroLab) }
            item { ConversionBorderCard(conversionLab) }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Indigo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("다각형 도형과 모핑이 foundation 에", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Compose 1.13 의 PolygonShape 는 정다각형 · 별 · 필 · 필스타 · 원 · 사각형 · 직접 준 꼭짓점을 Shape 로 만든다. " +
                    "MorphPolygonShape 는 두 PolygonShape 사이를 진행률로 잇고, RoundedCornerShape · CutCornerShape 는 toPolygonShape() 로 " +
                    "바꿔 모핑에 끼운다. 지금까지는 graphics-shapes 의 RoundedPolygon · Morph 를 Path 로 직접 바꿔야 했다. opt-in 은 없다.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "이 화면은 API 를 소개하는 데서 그치지 않고 기기에서 잰다 — 진행률을 람다로 주면 리컴포지션이 0 인지, transform 이 " +
                    "왜 화면에 안 나오는지, 크기 0 에서 무엇이 죽는지, 변환이 픽셀까지 같은지.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

// ==================== 1. 팩토리와 CornerRounding ====================

@Composable
private fun GalleryCard() {
    var kind by remember { mutableStateOf(GalleryKind.Star) }
    var vertices by remember { mutableIntStateOf(5) }
    var rounding by remember { mutableStateOf(RoundingChoice.Fraction) }
    var smoothing by remember { mutableFloatStateOf(0f) }
    val hasRounding = kind.fractionBase != FractionBase.NoRounding
    val usesSmoothing = (hasRounding && rounding != RoundingChoice.None) || kind == GalleryKind.Pill

    LabCard(
        title = "1. 팩토리와 CornerRounding",
        color = Indigo,
        description = "같은 설정을 56dp · 128dp 두 칸에 그린다. dp 둥글기는 칸 크기와 무관하고, fraction 은 도형마다 기준 길이가 다르다."
    ) {
        ControlRow("도형") {
            GalleryKind.entries.forEach { LabChip(it.label, kind == it) { kind = it } }
        }
        if (kind.usesVertices) {
            ControlRow(if (kind == GalleryKind.Circle) "원을 근사할 꼭짓점 수" else "꼭짓점 · 끝 수") {
                vertexChoices.forEach { LabChip("$it", vertices == it) { vertices = it } }
            }
        }
        if (hasRounding) {
            ControlRow("CornerRounding") {
                RoundingChoice.entries.forEach { LabChip(it.label, rounding == it) { rounding = it } }
            }
        }
        if (usesSmoothing) {
            ControlRow("smoothing") {
                smoothingChoices.forEach { LabChip("$it", smoothing == it) { smoothing = it } }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Bottom) {
            gallerySizesDp.forEach { sizeDp ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 칸마다 따로 만든다 — PolygonShape 는 마지막 크기의 외곽선 하나만 캐시한다(3번 카드)
                    val shape = remember(kind, vertices, rounding, smoothing) { galleryShape(kind, vertices, rounding, smoothing) }
                    Box(
                        modifier = Modifier
                            .size(sizeDp.dp)
                            .clip(shape)
                            .background(AvatarBrush)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${formatDp(sizeDp)} 칸", fontSize = 11.sp, color = Body)
                    val radius = requestedCornerRadiusDp(kind, rounding, sizeDp, sizeDp)
                    Text(
                        text = if (radius == null) "둥글기 인자 없음" else "요청 반경 ${formatDp(radius)}",
                        fontSize = 11.sp,
                        color = Muted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FactRow(
            "fraction 의 기준 — ${kind.label}: ${kind.fractionBase.label}",
            "정다각형 · 별은 반지름(짧은 변 ÷ 2), 사각형은 짧은 변 전체, 직접 준 꼭짓점은 그 꼭짓점들이 차지한 범위의 짧은 변이다. " +
                "같은 fraction(0.25f) 이라도 128dp 칸의 정다각형은 16dp, 사각형은 32dp 를 요청한다(RoundedCornerShape(25) 와 같은 쪽은 사각형). " +
                "변에 안 들어가는 반경은 graphics-shapes 가 줄인다."
        )
        FactRow(
            "smoothing",
            "0 은 원호, 0 보다 크면 원호 양옆에 3차 곡선을 덧대 변으로 부드럽게 이어 붙인다(1 이 최대). 필은 끝의 반원과 직선을 잇는 정도다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(galleryCode(kind, vertices, rounding, smoothing))
    }
}

private fun galleryCode(kind: GalleryKind, vertices: Int, rounding: RoundingChoice, smoothing: Float): String {
    val corner = when (rounding) {
        RoundingChoice.None -> "CornerRounding.Unrounded"
        RoundingChoice.Dp -> if (smoothing == 0f) "CornerRounding.dp(8.dp)" else "CornerRounding.dp(8.dp, smoothing = ${smoothing}f)"
        RoundingChoice.Fraction -> if (smoothing == 0f) "CornerRounding.fraction(0.25f)" else "CornerRounding.fraction(0.25f, smoothing = ${smoothing}f)"
    }
    val call = when (kind) {
        GalleryKind.Regular -> "PolygonShape.regularPolygon($vertices, $corner)"
        GalleryKind.Star -> "PolygonShape.star($vertices, innerRadiusRatio = 0.5f,\n    outerRounding = $corner)"
        GalleryKind.PillStar -> "PolygonShape.pillStar($vertices, innerRadiusRatio = 0.6f,\n    outerRounding = $corner)"
        GalleryKind.Rectangle -> "PolygonShape.rectangle($corner)"
        GalleryKind.Custom -> "PolygonShape {\n    polygon(말풍선 꼭짓점 7개(size 기준 픽셀),\n        rounding = $corner)\n}"
        GalleryKind.Pill -> "PolygonShape.pill(smoothing = ${smoothing}f)"
        GalleryKind.Circle -> "PolygonShape.circle(numVertices = $vertices)"
    }
    return "// 팩토리는 상태(외곽선 캐시)를 가진 새 인스턴스 — remember 한다\n" +
        "val shape = remember {\n    $call\n}\nBox(Modifier.size(128.dp).clip(shape))"
}

// ==================== 2. 모핑 ====================

@Composable
private fun MorphCard(lab: MorphLabState) {
    val scope = rememberCoroutineScope()
    LabCard(
        title = "2. MorphPolygonShape — 아바타가 원이 되는 동안",
        color = Teal,
        description = "원문 사례: 공유 요소 전환에서 일그러진(쿠키 모양) 아바타가 커지며 원이 된다. 세 방식은 ③ 을 기준으로 한 가지씩만 다르다."
    ) {
        ControlRow("방식") {
            MorphVariant.entries.forEach { v ->
                LabChip(v.shortLabel, lab.variant == v, enabled = !lab.running) { lab.variant = v }
            }
        }
        Text(lab.variant.detail, fontSize = 12.sp, color = Body, lineHeight = 17.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((MORPH_LARGE_DP + 16f).dp),
            contentAlignment = Alignment.Center
        ) {
            MorphAvatar(lab)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { scope.launch { lab.playOnce() } }, enabled = !lab.running) {
                Text("▶ 한 번 오가기", fontSize = 12.sp)
            }
            OutlinedButton(onClick = { scope.launch { lab.measureAll() } }, enabled = !lab.running) {
                Text("▶ 세 방식 재기(각 ${MORPH_ROUNDS}번)", fontSize = 12.sp)
            }
        }
        MorphResults(lab)
        Spacer(modifier = Modifier.height(6.dp))
        FactRow(
            "진행률은 람다로",
            "MorphPolygonShape(start, end) { progress } 의 람다는 외곽선을 만들 때 읽힌다. clip · background · border 는 외곽선을 만드는 " +
                "동안의 상태 읽기를 관찰하므로 값이 바뀌면 그 단계만 다시 한다 — 리컴포지션이 없다. 계측 테스트에서 진행률을 5번 바꾸자 " +
                "clip · background · border 가 각각 외곽선을 5번 다시 만들었고 리컴포지션은 0 이었다."
        )
        FactRow(
            "remember 없이 만들면 lint 가 Error",
            "MorphPolygonShape · PolygonShape { } · transform · copy 에는 @RememberInComposition 이 붙어 컴포지션에서 remember 없이 부르면 " +
                "lint(RememberInComposition)가 Error 로 막는다. ① 은 비용을 보이려고 일부러 억제했다 — 새 인스턴스는 Morph(대응점 계산)부터 다시 한다."
        )
        FactRow(
            "크기가 함께 변하면 캐시가 깨진다",
            "끝 도형의 외곽선 캐시는 마지막 크기 하나다. ② 처럼 레이아웃 크기가 프레임마다 바뀌면 시작 · 끝 다각형과 Morph 를 매 프레임 " +
                "새로 만든다. 커지면서 모양이 바뀌는 아바타라면 칸은 큰 크기로 두고 graphicsLayer scale 로 키우면(③) 매 프레임 보간만 한다."
        )
        FactRow(
            "요소마다 인스턴스 하나",
            "KDoc: 'Create a separate instance for each morphing element.' — 크기가 다른 두 요소가 한 인스턴스를 나눠 쓰면 서로의 캐시를 지운다(3번 카드 다)."
        )
    }
}

@Composable
internal fun MorphAvatar(lab: MorphLabState) {
    key(lab.variant) {
        when (lab.variant) {
            MorphVariant.ValueNew -> ValueNewAvatar(lab)
            MorphVariant.LambdaLayout -> LambdaLayoutAvatar(lab)
            MorphVariant.LambdaScale -> LambdaScaleAvatar(lab)
        }
    }
}

/** ① 진행률을 컴포지션에서 읽고 도형을 매번 새로 만든다 — 비용을 보이려고 lint 를 억제했다 */
@SuppressLint("RememberInComposition")
@Composable
private fun ValueNewAvatar(lab: MorphLabState) {
    SideEffect { lab.recompositions.count++ }
    val start = remember { avatarStartShape() }
    val end = remember { avatarEndShape() }
    val p = lab.progress.value
    val shape = CountingShape(MorphPolygonShape(start, end, constantProgress(p)), lab.counter)
    val scale = MORPH_SMALL_DP / MORPH_LARGE_DP + (1f - MORPH_SMALL_DP / MORPH_LARGE_DP) * p
    AvatarFace(
        Modifier
            .testTag("morphAvatar")
            .size(MORPH_LARGE_DP.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
    )
}

/** ② remember 한 도형 + 람다 진행률, 크기는 레이아웃 단계에서 키운다 */
@Composable
private fun LambdaLayoutAvatar(lab: MorphLabState) {
    SideEffect { lab.recompositions.count++ }
    val shape = remember {
        CountingShape(MorphPolygonShape(avatarStartShape(), avatarEndShape(), lab.progressReader), lab.counter)
    }
    Box(modifier = Modifier.size(MORPH_LARGE_DP.dp), contentAlignment = Alignment.Center) {
        AvatarFace(
            Modifier
                .testTag("morphAvatar")
                .layout { measurable, _ ->
                    val p = lab.progress.value
                    val px = (MORPH_SMALL_DP + (MORPH_LARGE_DP - MORPH_SMALL_DP) * p).dp.roundToPx()
                    val placeable = measurable.measure(Constraints.fixed(px, px))
                    layout(px, px) { placeable.place(0, 0) }
                }
                .clip(shape)
        )
    }
}

/** ③ remember 한 도형 + 람다 진행률, 칸은 큰 크기로 두고 scale 로 키운다 */
@Composable
private fun LambdaScaleAvatar(lab: MorphLabState) {
    SideEffect { lab.recompositions.count++ }
    val shape = remember {
        CountingShape(MorphPolygonShape(avatarStartShape(), avatarEndShape(), lab.progressReader), lab.counter)
    }
    AvatarFace(
        Modifier
            .testTag("morphAvatar")
            .size(MORPH_LARGE_DP.dp)
            .graphicsLayer {
                val s = MORPH_SMALL_DP / MORPH_LARGE_DP + (1f - MORPH_SMALL_DP / MORPH_LARGE_DP) * lab.progress.value
                scaleX = s
                scaleY = s
            }
            .clip(shape)
    )
}

/** 사진 대신 그리는 얼굴 — 칸 크기에 맞춰 그린다 */
@Composable
private fun AvatarFace(modifier: Modifier) {
    Box(
        modifier = modifier
            .background(AvatarBrush)
            .drawBehind {
                val w = size.width
                val h = size.height
                drawCircle(Color.White.copy(alpha = 0.92f), radius = w * 0.17f, center = Offset(w / 2f, h * 0.4f))
                drawCircle(Color.White.copy(alpha = 0.92f), radius = w * 0.32f, center = Offset(w / 2f, h * 0.98f))
            }
    )
}

@Composable
private fun MorphResults(lab: MorphLabState) {
    if (lab.results.isEmpty()) {
        Text(
            "아직 안 쟀다 — '세 방식 재기'는 방식마다 작은 쿠키 → 큰 원 → 작은 쿠키(${MORPH_MILLIS}ms 씩)를 오가며 센다.",
            fontSize = 11.sp,
            color = Muted,
            modifier = Modifier.padding(top = 6.dp)
        )
        return
    }
    Spacer(modifier = Modifier.height(8.dp))
    TableRow(listOf("방식", "프레임", "리컴포지션", "외곽선", "1회", "프레임당"), header = true)
    MorphVariant.entries.forEach { v ->
        val run = lab.results[v] ?: return@forEach
        TableRow(
            listOf(
                v.shortLabel,
                "${run.frames}",
                "${run.recompositions}",
                "${run.outlines}",
                formatMicros(run.microsPerOutline),
                formatMicros(run.microsPerFrame)
            ),
            color = if (run.matchesExpectation) Color(0xFF212121) else Mismatch
        )
    }
    val measured = MorphVariant.entries.mapNotNull { lab.results[it] }
    Text(
        text = if (measured.all { it.matchesExpectation }) "판정: 셋 다 예상대로 — ① 만 프레임마다 리컴포즈, 셋 다 프레임마다 외곽선을 만든다"
        else "판정: 예상과 다른 줄이 있다(빨간 줄)",
        fontSize = 11.sp,
        color = if (measured.all { it.matchesExpectation }) Matched else Mismatch
    )
    val base = lab.results[MorphVariant.LambdaScale]
    if (base != null && base.microsPerFrame > 0) {
        val lines = listOfNotNull(
            lab.results[MorphVariant.ValueNew]?.let { "프레임당 ① 은 ③ 의 ${ratioLabel(it.microsPerFrame, base.microsPerFrame)} — 새 인스턴스는 Morph(대응점 계산)부터 다시 한다" },
            lab.results[MorphVariant.LambdaLayout]?.let { "프레임당 ② 는 ③ 의 ${ratioLabel(it.microsPerFrame, base.microsPerFrame)} — 리컴포지션은 0 인데 크기가 바뀌어 Morph 를 다시 만들고, 외곽선도 프레임당 ${fixed(it.outlines.toDouble() / it.frames, 1)}번이다" }
        )
        lines.forEach { Text("• $it", fontSize = 11.sp, color = Body, lineHeight = 16.sp) }
    }
    Text(
        "외곽선 시간은 clip 이 부른 createOutline 만 잰다. 시간은 기기 · 빌드마다 다르니 배수로 본다.",
        fontSize = 11.sp,
        color = Muted,
        lineHeight = 16.sp
    )
}

// ==================== 3. transform · copy · 공유 ====================

@Composable
private fun TransformCard(lab: TransformLabState, cache: CacheBenchState) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    LabCard(
        title = "3. transform 은 상태가 아니라 제자리 변경",
        color = Plum,
        description = "transform { } 은 그 PolygonShape 를 바꾼다. 블록 안에서 상태를 읽으면 외곽선을 만들 때마다 다시 읽히지만, 블록을 바꾸는 호출 자체는 아무것도 다시 그리게 하지 않는다."
    ) {
        SubTitle("가. 상태를 읽는 transform — 한 번 걸어 두면 리컴포지션 없이 돈다")
        Row(verticalAlignment = Alignment.CenterVertically) {
            StateTile(lab)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Button(onClick = { scope.launch { lab.rotateWithState() } }) { Text("▶ ${TRANSFORM_STEP_DEGREES.toInt()}° 돌리기", fontSize = 12.sp) }
                val report = lab.lastReport as? TransformReport.StateRotation
                if (report != null) {
                    Text(
                        "각도 ${report.degrees.toInt()}° · 리컴포지션 ${report.recompositions} · 외곽선 ${report.outlines}번",
                        fontSize = 11.sp,
                        color = if (report.recompositions == 0 && report.outlines > 0) Matched else Mismatch
                    )
                }
            }
        }
        CodeBlock(
            "val shape = remember {\n" +
                "  PolygonShape.star(5).apply {\n" +
                "    transform { rotate(angle.value) } // 외곽선을 만들 때 읽는다\n" +
                "  }\n" +
                "}"
        )

        Spacer(modifier = Modifier.height(12.dp))
        SubTitle("나. 클릭에서 공유 인스턴스에 transform 을 걸면")
        Text(
            "A · B 는 같은 PolygonShape 를 쓰고, C 는 그 인스턴스를 transform 전에 copy() 한 것이다. 버튼을 누르면 칸마다 외곽선을 다시 만든 횟수와, 화면의 외곽선을 만든 때의 회전을 아래 줄에 적는다.",
            fontSize = 11.sp,
            color = Muted,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        SharedTiles(lab)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { scope.launch { lab.transformSharedInPlace() } }) { Text("① 공유에 transform(+${TRANSFORM_STEP_DEGREES.toInt()}°)", fontSize = 11.sp) }
            OutlinedButton(onClick = { scope.launch { lab.remount() } }) { Text("② 칸 다시 붙이기", fontSize = 11.sp) }
        }
        TransformReportText(lab.lastReport)

        Spacer(modifier = Modifier.height(12.dp))
        SubTitle("다. 캐시는 마지막 크기 하나 — 크기가 다른 두 칸이 나눠 쓰면")
        Text(
            "한 '프레임'에 96px · 320px 칸이 외곽선을 하나씩 만든다(clip · background 가 부르는 createOutline 그대로). 네 방식을 모두 ${CACHE_WARMUP_FRAMES} 프레임씩 데운 뒤 ${CACHE_FRAMES} 프레임 평균.",
            fontSize = 11.sp,
            color = Muted,
            lineHeight = 16.sp
        )
        OutlinedButton(onClick = { scope.launch { cache.run(density) } }, enabled = !cache.running) {
            Text(if (cache.running) "재는 중…" else "▶ 재기", fontSize = 12.sp)
        }
        if (cache.results.isNotEmpty()) {
            TableRow(listOf("방식", "프레임당"), header = true)
            cache.results.forEach { TableRow(listOf(it.scenario.label, formatMicros(it.microsPerFrame))) }
            val byScenario = cache.results.associateBy { it.scenario }
            val shared = byScenario[CacheScenario.SharedTwoSizesRotating]
            val copied = byScenario[CacheScenario.CopyPerSizeRotating]
            if (shared != null && copied != null) {
                Text(
                    "• 매 프레임 회전하는 경우에도 나눠 쓰면 ${ratioLabel(shared.microsPerFrame, copied.microsPerFrame)} — 회전 행렬은 어차피 새로 적용하지만, 기반 다각형까지 매번 다시 만든다",
                    fontSize = 11.sp,
                    color = Body,
                    lineHeight = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        FactRow(
            "화면은 언제 바뀌나",
            "Background 는 크기 · shape 가 같으면 지난 외곽선을 그대로 쓰고, clip 의 레이어도 관찰한 상태가 바뀔 때만 외곽선을 다시 만든다. " +
                "그래서 ① 직후엔 셋 다 그대로이고, 색을 바꿔 다시 그려도(계측 테스트) 외곽선은 그대로였다. 크기가 바뀌거나 칸을 다시 붙여야(②) 새로 만든다."
        )
        FactRow(
            "그때는 나눠 쓴 곳이 전부 바뀐다",
            "제자리 변경이라 같은 인스턴스를 쓰는 A · B 가 함께 돈다. 상수로 둔 도형(val AvatarShape = PolygonShape.star(…))에 한 화면이 " +
                "transform 을 걸면 다른 화면도 바뀐다 — 고칠 도형은 copy() 한 것에 건다. C 처럼 먼저 copy() 한 것은 따라가지 않는다."
        )
        FactRow(
            "transform 은 마지막 것 하나",
            "transform 을 다시 부르면 이전 블록을 덧붙이지 않고 바꾼다 — ① 이 누적 각도로 rotate 하는 이유다. 블록 안의 rotate · scale · translate 는 부른 순서대로 쌓인다."
        )
    }
}

@Composable
private fun SubTitle(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121), modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
internal fun StateTile(lab: TransformLabState) {
    SideEffect { lab.stateRecompositions.count++ }
    val shape = remember { CountingShape(lab.stateShape, lab.stateCounter) }
    Box(
        modifier = Modifier
            .testTag("stateTile")
            .size(72.dp)
            .background(Plum, shape)
    )
}

@Composable
internal fun SharedTiles(lab: TransformLabState) {
    key(lab.remountKey) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SharedTile("A · 공유", "tileA", lab.shared, lab.sharedACounter, Plum) { lab.sharedDegrees }
            SharedTile("B · 공유", "tileB", lab.shared, lab.sharedBCounter, Plum) { lab.sharedDegrees }
            SharedTile("C · 먼저 copy()", "tileC", lab.copied, lab.copiedCounter, Slate) { 0f }
        }
    }
}

@Composable
private fun SharedTile(label: String, tag: String, shape: PolygonShape, counter: OutlineCounter, color: Color, degrees: () -> Float) {
    // 칸마다 껍데기는 따로(노드마다 외곽선을 캐시한다), 안의 PolygonShape 는 A · B 가 같다
    val counting = remember { CountingShape(shape, counter, degrees) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .testTag(tag)
                .size(64.dp)
                .background(color, counting)
        )
        Text(label, fontSize = 10.sp, color = Body)
    }
}

@Composable
private fun TransformReportText(report: TransformReport?) {
    val lines = when (report) {
        is TransformReport.InPlace -> listOf(
            "공유 인스턴스는 이제 ${report.sharedDegrees.toInt()}° — 그 뒤 두 프레임 동안 외곽선을 다시 만든 횟수 A ${report.rebuilt[0]} · B ${report.rebuilt[1]} · C ${report.rebuilt[2]}",
            "화면의 외곽선을 만든 때의 회전 A ${report.drawnDegrees[0].toInt()}° · B ${report.drawnDegrees[1].toInt()}° · C ${report.drawnDegrees[2].toInt()}° → " +
                if (report.rebuilt.all { it == 0 }) "화면은 그대로다" else "일부가 다시 그렸다"
        )
        is TransformReport.Remounted -> listOf(
            "다시 붙인 뒤 외곽선을 만든 때의 회전 A ${report.drawnDegrees[0].toInt()}° · B ${report.drawnDegrees[1].toInt()}° · C 0°(copy 는 따로) — 공유 인스턴스 ${report.sharedDegrees.toInt()}°"
        )
        else -> emptyList()
    }
    lines.forEach { Text("• $it", fontSize = 11.sp, color = Body, lineHeight = 16.sp) }
}

// ==================== 4. 크기 0 ====================

@Composable
private fun ZeroSizeCard(lab: ZeroLabState) {
    val density = LocalDensity.current
    var shown by remember { mutableStateOf(true) }
    LabCard(
        title = "4. 크기 0 에서 던진다 — PolygonShape 만",
        color = Mismatch,
        description = "RoundedCornerShape 는 크기 0 에서도 외곽선을 돌려주지만 PolygonShape 계열은 IllegalArgumentException 을 던진다. 그리는 중이면 앱이 죽는다."
    ) {
        Button(onClick = { lab.run(density) }) { Text("▶ 크기별로 외곽선 만들어 보기", fontSize = 12.sp) }
        if (lab.cells.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            TableRow(listOf("도형") + zeroSizes.map { sizeLabel(it) }, header = true)
            ZeroCase.entries.forEach { case ->
                val cells = zeroSizes.map { lab.cells[case to it] }
                TableRow(
                    listOf(case.label) + cells.map { if (it == null) "-" else if (it.ok) "그림" else "던짐" },
                    color = if (cells.all { it?.ok == true }) Color(0xFF212121) else Mismatch
                )
            }
            val messages = lab.cells.values.mapNotNull { it.error?.message }.distinct()
            Spacer(modifier = Modifier.height(4.dp))
            Text("던진 메시지(IllegalArgumentException):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Body)
            messages.forEach { Text("• $it", fontSize = 11.sp, color = Body, lineHeight = 16.sp) }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SubTitle("0 에서 튀어나오는 배지 — ZeroSafeShape 로 감쌌다")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                // 커질 때는 튕기고, 0 으로 갈 때는 넘치지 않게(스프링이 음수 크기로 넘어가면 size 가 던진다)
                val badge by animateDpAsState(
                    targetValue = if (shown) 44.dp else 0.dp,
                    animationSpec = if (shown) spring(dampingRatio = 0.45f, stiffness = 300f) else tween(220),
                    label = "badge"
                )
                // 첫 프레임 크기가 0 이다 — 감싸지 않은 PolygonShape 면 여기서 죽는다
                val shape = remember { ZeroSafeShape(PolygonShape.star(8, innerRadiusRatio = 0.72f, outerRounding = CornerRounding.fraction(0.3f))) }
                Box(
                    modifier = Modifier
                        .testTag("zeroBadge")
                        .size(badge.coerceAtLeast(0.dp))
                        .background(Amber, shape)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(onClick = { shown = !shown }) {
                Text(if (shown) "숨기기(→ 0dp)" else "튀어나오기(0dp →)", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        FactRow(
            "어디서 0 이 되나",
            "크기를 0 에서 키우는 애니메이션의 첫 프레임, 남은 공간이 없는 Column · Row 에 밀린 칸(계측 프로브가 이렇게 죽었다), weight 0 · fillMaxHeight(0f) 같은 곳. " +
                "clip 은 그려지지 않아도 배치 단계에서 외곽선을 만들어 죽고, background 는 그 칸을 그릴 때 죽는다(계측 테스트 둘 다 IllegalArgumentException)."
        )
        FactRow(
            "버전",
            "Compose 1.13.0 기준 실측. 1.14.0-alpha01 릴리스 노트에도 이 수정은 없다. 1px 만 있어도 그린다."
        )
        CodeBlock(
            "class ZeroSafeShape(val delegate: Shape) : Shape {\n" +
                "  override fun createOutline(size: Size, ld: LayoutDirection, d: Density) =\n" +
                "    if (size.width <= 0f || size.height <= 0f)\n" +
                "      Outline.Rectangle(Rect(Offset.Zero, size))\n" +
                "    else delegate.createOutline(size, ld, d)\n" +
                "}"
        )
    }
}

// ==================== 5. 변환 · 테두리 ====================

@Composable
private fun ConversionBorderCard(lab: ConversionLabState) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var case by remember { mutableStateOf(ConversionCase.Round24) }
    val morph = remember { Animatable(0f) }
    LabCard(
        title = "5. toPolygonShape() 와 다각형 테두리",
        color = Slate,
        description = "기존 모서리 도형을 PolygonShape 로 바꾸면 모핑 · transform 에 끼울 수 있다. 대신 픽셀까지 같지는 않다(KDoc)."
    ) {
        SubTitle("가. 원래 도형 · 변환한 도형")
        ControlRow("도형") {
            ConversionCase.entries.forEach { LabChip(it.label, case == it) { case = it } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val original = remember(case) { case.original() }
            val converted = remember(case) { case.converted() }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(96.dp).background(Slate, original))
                Text("원래", fontSize = 10.sp, color = Body)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(96.dp).background(Teal, converted))
                Text("toPolygonShape()", fontSize = 10.sp, color = Body)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(onClick = { lab.run(density) }) { Text("▶ 픽셀 비교(${CONVERSION_PX}px · 5가지)", fontSize = 12.sp) }
        if (lab.results.isNotEmpty()) {
            TableRow(listOf("도형", "다른 픽셀", "최대 채널 차"), header = true)
            lab.results.forEach {
                TableRow(listOf(it.case.label, "${it.diff.differing} / ${it.diff.total}", "${it.diff.maxChannel} / 255"))
            }
            Text(
                "소프트웨어 캔버스로 그려 비교했다. 화면(하드웨어) 계측에서도 차이는 가장자리 몇백 픽셀뿐이었다(RoundedCornerShape(24.dp) 240px: 288개 · 최대 60).",
                fontSize = 11.sp,
                color = Muted,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        SubTitle("나. 변환하면 모핑할 수 있다 — 카드 모서리 → 별")
        Row(verticalAlignment = Alignment.CenterVertically) {
            val shape = remember {
                MorphPolygonShape(
                    RoundedCornerShape(24.dp).toPolygonShape(),
                    PolygonShape.star(8, innerRadiusRatio = 0.75f, outerRounding = CornerRounding.fraction(0.2f))
                ) { morph.value }
            }
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(shape)
                    .background(AvatarBrush)
            )
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(onClick = {
                scope.launch { morph.animateTo(if (morph.value < 0.5f) 1f else 0f, tween(600)) }
            }) { Text("▶ 모핑", fontSize = 12.sp) }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SubTitle("다. Modifier.border — 다각형 테두리에 셰이더 브러시")
        val star = remember { PolygonShape.star(6, innerRadiusRatio = 0.6f, outerRounding = CornerRounding.dp(4.dp)) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BorderTile("SolidColor") { Modifier.border(6.dp, SolidColor(Plum), star.copy()) }
            BorderTile("sweepGradient") {
                Modifier.border(6.dp, Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Cyan, Color.Magenta, Color.Red)), star.copy())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RuntimeShaderBorderTile(star)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        Text("API 33+", fontSize = 11.sp, color = Muted)
                    }
                    Text("RuntimeShader", fontSize = 10.sp, color = Body)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        FactRow(
            "1.13 의 새 border",
            "1.13 은 Modifier.border 를 새로 구현했다(ComposeFoundationFlags.isNewBorderImplementationEnabled, 기본 true). 다각형 같은 일반 외곽선은 " +
                "단색 · 그라데이션이면 비트맵에, RuntimeShader 브러시면 GraphicsLayer 에 그린다. 옛 구현으로 돌리면(플래그 false) 같은 별 + RuntimeShader 가 " +
                "IllegalArgumentException: Software rendering doesn't support RuntimeShader 로 죽었다(계측 테스트). 둥근 사각형은 옛 구현에서도 그렸다."
        )
        FactRow(
            "플래그는 Modifier 를 만들 때 읽는다",
            "Modifier.border(…) 를 부르는 순간 어느 구현의 요소를 만들지 정한다 — 바꾸려면 앱 시작에 한 번. 1.14.0-alpha01 은 API 28+ 의 일반 도형 테두리를 " +
                "모두 GraphicsLayer 로 그려 안티에일리어싱이 조금 달라진다(isBorderUsesGraphicsLayerForGenericShapesEnabled)."
        )
    }
}

@Composable
private fun BorderTile(label: String, border: () -> Modifier) {
    val modifier = remember { border() }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(80.dp).then(modifier))
        Text(label, fontSize = 10.sp, color = Body)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
internal fun RuntimeShaderBorderTile(star: PolygonShape) {
    val brush = remember { HoloShaderBrush() }
    val shape = remember { star.copy() }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .testTag("shaderBorder")
                .size(80.dp)
                .border(6.dp, brush, shape)
        )
        Text("RuntimeShader", fontSize = 10.sp, color = Body)
    }
}

/** AGSL 무지개 — createShader 는 크기만 넣어 같은 셰이더를 돌려준다 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class HoloShaderBrush : ShaderBrush() {
    private val shader = RuntimeShader(HOLO_AGSL)

    override fun createShader(size: Size): Shader {
        shader.setFloatUniform("size", size.width, size.height)
        return shader
    }
}

@Composable
private fun SummaryCard() {
    LabCard(title = "정리", color = Indigo, description = "도형은 remember 해서 요소마다 하나, 움직임은 람다 · transform 블록 안에서 읽는다.") {
        listOf(
            "PolygonShape · MorphPolygonShape 는 foundation 1.13 정식 API — graphics-shapes 를 직접 쓸 필요가 없다",
            "CornerRounding.fraction 의 기준은 도형마다 다르다: 정다각형 · 별은 반지름, 사각형은 짧은 변, 꼭짓점 직접은 그 범위",
            "진행률은 람다로 — 외곽선을 만들 때 읽혀 리컴포지션 없이 그리기만 다시 한다. remember 없이 만들면 lint Error",
            "커지면서 모핑하면 크기는 scale 로 — 레이아웃 크기가 바뀌면 매 프레임 Morph 를 다시 만든다",
            "transform 은 제자리 변경이고 상태 쓰기가 아니다 — 클릭에서 부르면 화면이 안 바뀌다가 나중에 나눠 쓴 곳이 전부 바뀐다. 고칠 것은 copy() 에",
            "PolygonShape 는 크기 0 에서 IllegalArgumentException — 0 이 될 수 있는 칸은 ZeroSafeShape 로 감싼다",
            "toPolygonShape() 는 가장자리 픽셀이 조금 다르다. 다각형 + RuntimeShader 테두리는 1.13 새 border 구현에서만 된다"
        ).forEach { line ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("•", fontSize = 12.sp, color = Indigo)
                Spacer(modifier = Modifier.width(6.dp))
                Text(line, fontSize = 12.sp, color = Body, lineHeight = 18.sp)
            }
        }
    }
}

// ==================== 공통 ====================

@Composable
private fun LabCard(
    title: String,
    color: Color,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Muted, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ControlRow(label: String, chips: @Composable () -> Unit) {
    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Body)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { chips() }
}

@Composable
private fun LabChip(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(text = label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC5CAE9))
    )
}

@Composable
private fun TableRow(cells: List<String>, header: Boolean = false, color: Color = Color(0xFF212121)) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = cell,
                modifier = Modifier.weight(if (index == 0) 1.8f else 1f),
                fontSize = 11.sp,
                fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
                color = if (header) Indigo else color,
                lineHeight = 15.sp
            )
        }
    }
    HorizontalDivider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FactRow(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Text(text = body, fontSize = 12.sp, color = Body, lineHeight = 18.sp)
    }
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
            .padding(10.dp),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFECEFF1),
        lineHeight = 16.sp
    )
}
