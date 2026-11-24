package com.example.composesample.presentation.example.component.ui.graphics

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch

private val Indigo = Color(0xFF283593)
private val Teal = Color(0xFF00695C)
private val Plum = Color(0xFF6A1B9A)
private val Slate = Color(0xFF37474F)
private val Muted = Color(0xFF757575)
private val Body = Color(0xFF424242)
private val Matched = Color(0xFF2E7D32)
private val Mismatch = Color(0xFFC62828)
private val StripeColors = listOf(Color(0xFFE53935), Color(0xFFFDD835), Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF8E24AA))

@Composable
fun ProgressiveBlurExampleUI(onBackEvent: () -> Unit) {
    // 카드 결과 · 스크롤 상태는 카드가 스크롤로 빠졌다 돌아와도 남도록 화면 루트에 둔다
    val galleryLab = remember { GalleryLabState() }
    val edgeLab = remember { EdgeLabState() }
    val glassLab = remember { GlassLabState() }
    val returnLab = remember { ReturnLabState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4FA))
    ) {
        MainHeader(title = "Modifier.blur { } — 점진 블러", onBackIconClicked = onBackEvent)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { OverviewCard() }
            item { SpecGalleryCard(galleryLab) }
            item { EdgeCard(edgeLab) }
            item { GlassCard(glassLab) }
            item { ReturnCard(returnLab) }
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
            Text("자리마다 세기가 다른 블러", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Compose 1.13 의 Modifier.blur { } 는 BlurScope 에서 blurRadiusSpec · edgeTreatment · alpha 를 정한다. " +
                    "BlurRadiusSpec 은 균일 · 세로 · 가로 · 대각선 · 방사형 그라데이션과 stop 2~16개, 셰이더 마스크(알파 = 세기)를 만든다. " +
                    "블록은 레이어를 설정할 때 실행되어 그 안의 상태 읽기는 리컴포지션 없이 레이어 효과만 바꾼다.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "뒤를 흐리는 '배경 블러'가 아니다 — 이 Modifier 를 단 컴포저블 자신의 그림을 흐린다. 헤더 밑을 흐리려면 헤더가 아니라 " +
                    "그 아래로 지나가는 목록에 단다(3번 카드). 균일 반경은 API 31+, 위치마다 다른 반경은 API 33+.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

// ==================== 1. 반경 모양 ====================

@Composable
private fun SpecGalleryCard(lab: GalleryLabState) {
    val window = LocalActivity.current?.window
    val scope = rememberCoroutineScope()
    val sdk = Build.VERSION.SDK_INT
    LabCard(
        title = "1. BlurRadiusSpec — 반경이 자리마다 다르다",
        color = Indigo,
        description = "같은 그림에 일곱 가지 스펙을 건다. 'r' 은 가장 센 곳의 반경이다. 아래 '재기'는 블러를 껐다 켜며 같은 자리의 선명도를 PixelCopy 로 읽어, 이 기기에서 실제로 흐려졌는지 본다."
    ) {
        ControlRow("스펙") {
            SpecKind.entries.forEach { LabChip(it.label, lab.kind == it, enabled = !lab.running) { lab.kind = it } }
        }
        ControlRow("r") {
            galleryRadiusChoices.forEach { r -> LabChip("${r.toInt()}dp", lab.radiusDp == r, enabled = !lab.running) { lab.radiusDp = r } }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BlurSampleContent(
            Modifier
                .testTag("galleryContent")
                .measured(lab.bounds)
                .blur {
                    blurRadiusSpec = if (lab.blurOff) BlurRadiusSpec.uniform(0.dp) else specFor(lab.kind, lab.radiusDp, size)
                }
        )
        Spacer(modifier = Modifier.height(6.dp))
        val support = specSupport(lab.kind, sdk)
        Text(
            "이 기기 API $sdk — ${lab.kind.label}: ${support.label} (최소 API ${lab.kind.minApi})",
            fontSize = 12.sp,
            color = if (support.blurs) Matched else Mismatch
        )
        CodeBlock("Modifier.blur {\n  blurRadiusSpec = ${lab.kind.code}\n}")
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                if (window != null && pixelCopyAvailable()) {
                    scope.launch { lab.measureAll(sdk) { WindowCapture.read(window, it) } }
                }
            },
            enabled = !lab.running && window != null && pixelCopyAvailable()
        ) { Text(if (lab.running) "재는 중…" else "▶ 일곱 가지 모두 재기(지금 r)", fontSize = 12.sp) }
        if (lab.results.isNotEmpty()) {
            TableRow(listOf("스펙", "이 기기 예상", "블러 전 대비 선명도", "판정"), header = true)
            SpecKind.entries.forEach { k ->
                val r = lab.results[k] ?: return@forEach
                TableRow(
                    listOf(k.label, if (r.support.blurs) "흐려짐" else "선명", ratioText(r.ratio), if (r.matchesExpectation) "예상대로" else "다름"),
                    color = if (r.matchesExpectation) Color(0xFF212121) else Mismatch
                )
            }
            Text(
                "선명도는 이웃 픽셀 밝기 차의 평균이다. 블러 전 대비 ${fixed2(BLURRED_BELOW, 1)}배 밑이면 '흐려짐'. 그라데이션은 일부만 흐리므로 균일보다 덜 떨어진다.",
                fontSize = 11.sp,
                color = Muted,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        FactRow(
            "API 가 둘로 갈린다",
            "균일(uniform)은 RenderEffect.createBlurEffect 라 API 31+, 나머지는 RuntimeShader 패스라 API 33+ 다(ui-graphics 1.13.0 sources). " +
                "API 31~32 에서 그라데이션을 주면 예외 없이 identity 효과 — 그냥 선명하게 그린다. API 31 미만은 Modifier 자체를 무시한다."
        )
        FactRow(
            "stop 은 2~16개",
            "stop 목록은 fraction 순으로 정렬해 쓰고, 개수가 2~16 을 벗어나면 IllegalArgumentException. 3번 카드는 stop 네 개(r · r · 0 · 0)로 헤더 높이까지는 r 을 유지하고 그 아래에서 0 으로 내린다."
        )
        FactRow(
            "셰이더 마스크는 매번 새것",
            "BlurRadiusSpec.shader 는 셰이더 알파(0~1)를 세기로 쓴다 — 그라데이션 셰이더도 된다. 같은지 비교할 수 없어 블록이 돌 때마다 새 효과가 된다(KDoc: 셰이더는 한 번 만들어 재사용)."
        )
    }
}

/** 선명도 차이가 잘 보이는 그림 — 굵기가 다른 줄무늬와 글자 */
@Composable
private fun BlurSampleContent(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 6.dp.toPx()
            var x = 0f
            var i = 0
            while (x < size.width) {
                drawRect(StripeColors[i % StripeColors.size], topLeft = Offset(x, 0f), size = Size(step / 2f, size.height))
                x += step
                i++
            }
        }
        Text(
            "Satin glass 1.13",
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
    }
}

// ==================== 2. 가장자리 처리 ====================

@Composable
private fun EdgeCard(lab: EdgeLabState) {
    val window = LocalActivity.current?.window
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    LabCard(
        title = "2. edgeTreatment — 가장자리와 바깥",
        color = Teal,
        description = "블러는 칸 밖의 픽셀을 섞어야 해서 가장자리 처리가 필요하다. Rectangle(기본)은 칸에 잘라 가장자리 픽셀을 늘이고(Clamp), Unbounded 는 자르지 않고 칸 밖을 투명으로 본다(Decal)."
    ) {
        ControlRow("edgeTreatment") {
            EdgeChoice.entries.forEach { LabChip(it.label, lab.edge == it, enabled = !lab.running) { lab.edge = it } }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .testTag("edgeStage")
                .measured(lab.bounds)
                .background(Color.White)
                .padding(EDGE_MARGIN_DP.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 110.dp)
                    .blur {
                        blurRadiusSpec = BlurRadiusSpec.uniform(if (lab.blurOff) 0.dp else EDGE_RADIUS_DP.dp)
                        edgeTreatment = lab.edge.treatment
                    }
                    .background(Indigo)
            ) {
                Text("EDGE", modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
        }
        CodeBlock("Modifier.blur {\n  blurRadiusSpec = BlurRadiusSpec.uniform(${EDGE_RADIUS_DP.toInt()}.dp)\n  edgeTreatment = ${lab.edge.code}\n}")
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                if (window != null && pixelCopyAvailable()) {
                    val marginPx = with(density) { EDGE_MARGIN_DP.dp.roundToPx() }
                    scope.launch { lab.measureAll(marginPx, android.graphics.Color.WHITE) { WindowCapture.read(window, it) } }
                }
            },
            enabled = !lab.running && window != null && pixelCopyAvailable()
        ) { Text(if (lab.running) "재는 중…" else "▶ 세 가지 재기", fontSize = 12.sp) }
        if (lab.results.isNotEmpty()) {
            TableRow(listOf("edgeTreatment", "칸 밖으로 번진 픽셀", "안쪽 가장자리 밝아짐"), header = true)
            EdgeChoice.entries.forEach { e ->
                val r = lab.results[e] ?: return@forEach
                TableRow(listOf(e.label, "${r.newSpill}", "+${fixed2(r.ringBrighter, 1)}"))
            }
            Text(
                "칸 밖 ${EDGE_MARGIN_DP.toInt()}dp 띠에서 흰 바탕과 다른 픽셀 수(블러 전 대비), 칸 안쪽 3px 띠의 평균 밝기 변화(0~255).",
                fontSize = 11.sp,
                color = Muted,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        FactRow(
            "블록의 기본값은 Rectangle",
            "blur { } 블록은 실행될 때마다 edgeTreatment 를 Rectangle 로 되돌린 뒤 블록을 돈다(BlurScopeImpl.reset). 바꾸려면 블록 안에서 매번 적는다."
        )
        FactRow(
            "Unbounded 는 칸 밖으로 번진다",
            "Unbounded 는 레이어를 자르지 않아 흐린 그림이 칸 밖까지 퍼지고, 가장자리는 투명과 섞여 바탕이 비친다. 이웃과 겹칠 수 있으니 여백을 둔다. Shape 를 주면 그 모양으로 자르고 Clamp 한다."
        )
    }
}

// ==================== 3. satin glass 헤더 ====================

@Composable
private fun GlassCard(lab: GlassLabState) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    LabCard(
        title = "3. satin glass 헤더 — 스크롤 위치로 반경을 정한다",
        color = Plum,
        description = "원문 사례: 헤더 밑으로 지나가는 목록만 흐린다(헤더 높이 띠 + 그 아래 ${GLASS_FADE_DP.toInt()}dp 에서 0). 스크롤할수록 진해진다. 블러는 목록에, 스크림은 헤더에 단다."
    ) {
        ControlRow("방식") {
            GlassVariant.entries.forEach { LabChip(it.label, lab.variant == it, enabled = !lab.running) { lab.variant = it } }
        }
        Text(lab.variant.detail, fontSize = 12.sp, color = Body, lineHeight = 17.sp)
        Spacer(modifier = Modifier.height(8.dp))
        GlassStage(lab)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = { scope.launch { lab.measureAll(with(density) { GLASS_SCROLL_DP.dp.toPx() }) } },
            enabled = !lab.running
        ) { Text(if (lab.running) "재는 중…" else "▶ 두 방식 재기(${GLASS_SCROLL_DP.toInt()}dp 스크롤)", fontSize = 12.sp) }
        if (lab.results.isNotEmpty()) {
            TableRow(listOf("방식", "프레임", "리컴포지션", "블러 블록", "스크림 그리기"), header = true)
            GlassVariant.entries.forEach { v ->
                val r = lab.results[v] ?: return@forEach
                TableRow(
                    listOf(v.label, "${r.frames}", "${r.recompositions}", "${r.blockRuns}", "${r.scrimDraws}"),
                    color = if (r.matchesExpectation) Color(0xFF212121) else Mismatch
                )
            }
            val all = GlassVariant.entries.mapNotNull { lab.results[it] }
            Text(
                if (all.all { it.matchesExpectation }) "판정: 예상대로 — ① 만 스크롤 프레임마다 리컴포즈, ② 는 0 이고 블러 블록 · 스크림만 다시 돈다"
                else "판정: 예상과 다른 줄이 있다(빨간 줄)",
                fontSize = 11.sp,
                color = if (all.all { it.matchesExpectation }) Matched else Mismatch
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        FactRow(
            "블록은 레이어 블록이다",
            "blur { } 는 placeWithLayer 의 레이어 블록으로 돈다. 그 안에서 읽은 상태가 바뀌면 레이어 효과만 바꾸고 내용은 다시 기록하지 않는다(BlurNode 주석). " +
                "그래서 스크롤 위치는 블록 안에서 읽는다 — 컴포지션에서 읽으면(①) 목록을 품은 함수가 프레임마다 다시 실행된다."
        )
        FactRow(
            "헤더가 아니라 목록을 흐린다",
            "Modifier.blur 는 자기 그림만 흐린다. 헤더에 달면 헤더 글자가 흐려질 뿐 그 밑의 목록은 그대로다. 그래서 목록 전체에 세로 stop 네 개(위 띠 r · 헤더 끝 r · ${GLASS_FADE_DP.toInt()}dp 아래 0 · 끝 0)를 건다."
        )
        CodeBlock(
            "LazyColumn(\n" +
                "  modifier = Modifier.blur {\n" +
                "    val p = glassProgress(listState.firstVisibleItemIndex,\n" +
                "        listState.firstVisibleItemScrollOffset, headerPx)\n" +
                "    blurRadiusSpec = BlurRadiusSpec.verticalGradient(\n" +
                "        glassStops(p, size.height.value)) // 띠 + fade\n" +
                "  }\n" +
                ")\n" +
                "Header(Modifier.drawBehind { drawRect(scrim(p)) }) // 스크림은 그리기 단계"
        )
    }
}

/** 목록 + 헤더 — 방식이 바뀌면 목록을 품은 함수를 통째로 바꾼다 */
@Composable
internal fun GlassStage(lab: GlassLabState) {
    Box(
        modifier = Modifier
            .testTag("glassStage")
            .fillMaxWidth()
            .height(320.dp)
            .clipToBounds()
            .background(Color(0xFFECEFF1))
    ) {
        key(lab.variant) {
            when (lab.variant) {
                GlassVariant.Composition -> GlassListComposition(lab)
                GlassVariant.Block -> GlassListBlock(lab)
            }
        }
        GlassHeader(lab)
    }
}

@Composable
private fun GlassListBlock(lab: GlassLabState) {
    SideEffect { lab.hostRecompositions.count++ }
    val rampPx = with(LocalDensity.current) { GLASS_HEADER_DP.dp.toPx() }
    GlassList(
        lab,
        Modifier.blur {
            lab.blockRuns++
            val p = glassProgress(lab.listState.firstVisibleItemIndex, lab.listState.firstVisibleItemScrollOffset, rampPx)
            blurRadiusSpec = glassSpec(p, size.height.value)
        }
    )
}

/** ① 스크롤 위치를 컴포지션에서 읽는다 — 비용을 보이려고 lint(FrequentlyChangingValue)를 억제했다 */
@SuppressLint("FrequentlyChangingValue")
@Composable
private fun GlassListComposition(lab: GlassLabState) {
    SideEffect { lab.hostRecompositions.count++ }
    val rampPx = with(LocalDensity.current) { GLASS_HEADER_DP.dp.toPx() }
    // 컴포지션에서 읽는다 — 스크롤 한 프레임마다 이 함수가 다시 실행된다
    val p = glassProgress(lab.listState.firstVisibleItemIndex, lab.listState.firstVisibleItemScrollOffset, rampPx)
    GlassList(
        lab,
        Modifier.blur {
            lab.blockRuns++
            blurRadiusSpec = glassSpec(p, size.height.value)
        }
    )
}

@Composable
private fun GlassList(lab: GlassLabState, modifier: Modifier) {
    LazyColumn(
        state = lab.listState,
        modifier = Modifier
            .testTag("glassList")
            .fillMaxSize()
            .then(modifier),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = GLASS_HEADER_DP.dp + 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(40) { GlassRow(it) }
    }
}

@Composable
private fun GlassRow(index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(StripeColors[index % StripeColors.size], RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) { Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text("메일 제목 ${index + 1} — 회의 자료 공유", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Text("미리보기: 오늘 오후 회의 전에 첨부한 문서를 한 번 확인해 주세요.", fontSize = 11.sp, color = Body)
        }
    }
}

@Composable
private fun GlassHeader(lab: GlassLabState) {
    val rampPx = with(LocalDensity.current) { GLASS_HEADER_DP.dp.toPx() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(GLASS_HEADER_DP.dp)
            .drawBehind {
                // 그리기 단계에서 읽는다 — 리컴포지션 없이 스크림만 진해진다
                lab.scrimDraws++
                val p = glassProgress(lab.listState.firstVisibleItemIndex, lab.listState.firstVisibleItemScrollOffset, rampPx)
                drawRect(Color.White.copy(alpha = 0.2f + 0.5f * p))
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text("받은 편지함", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
    }
}

// ==================== 4. 공유 요소가 헤더 밑으로 돌아올 때 ====================

private val ReturnBounds = BoundsTransform { _, _ -> tween(RETURN_TRANSITION_MILLIS, easing = LinearEasing) }

@Composable
private fun ReturnCard(lab: ReturnLabState) {
    val window = LocalActivity.current?.window
    val scope = rememberCoroutineScope()
    val offsetPx = with(LocalDensity.current) { RETURN_SCROLL_OFFSET_DP.dp.roundToPx() }
    LabCard(
        title = "4. 원문 함정 — 공유 요소가 헤더 밑으로 돌아오면",
        color = Mismatch,
        description = "칸을 눌러 크게 연 뒤 돌아오면, 전환 동안 칸은 공유 요소 오버레이에 그려져 목록의 blur 가 닿지 않는다. 헤더 밑 자리에 도착하는 순간 갑자기 흐려진다."
    ) {
        ControlRow("방식") {
            ReturnVariant.entries.forEach {
                LabChip(it.label, lab.variant == it, enabled = !lab.running && lab.selected == null) { lab.variant = it }
            }
        }
        Text(lab.variant.detail, fontSize = 12.sp, color = Body, lineHeight = 17.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ReturnStage(lab)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { scope.launch { lab.placeTargetUnderHeader(offsetPx) } },
                enabled = !lab.running && lab.selected == null
            ) { Text("${RETURN_TARGET_INDEX + 1}번 칸을 헤더 밑에", fontSize = 12.sp) }
            Button(
                onClick = {
                    if (window != null && pixelCopyAvailable()) {
                        scope.launch { lab.measureAll(offsetPx) { WindowCapture.read(window, it) } }
                    }
                },
                enabled = !lab.running && window != null && pixelCopyAvailable()
            ) { Text(if (lab.running) "재는 중…" else "▶ 세 방식 재기", fontSize = 12.sp) }
        }
        Text(
            "칸을 누르면 열리고 '← 목록으로'로 돌아온다. 재기는 ${RETURN_TARGET_INDEX + 1}번 칸을 헤더 밑에 두고 같은 일을 세 방식으로 한 번씩 한다(카드가 화면에 다 보일 때).",
            fontSize = 11.sp,
            color = Muted,
            lineHeight = 16.sp
        )
        if (lab.results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            TableRow(listOf("방식", "전환 85%", "끝난 첫 프레임", "+150ms", "+800ms", "한 번에 떨어짐"), header = true)
            ReturnVariant.entries.forEach { v ->
                val r = lab.results[v] ?: return@forEach
                TableRow(
                    listOf(
                        v.label,
                        ratioText(r.ratio(r.during)),
                        ratioText(r.ratio(r.firstAfter)),
                        ratioText(r.ratio(r.after150)),
                        ratioText(r.ratio(r.after800)),
                        r.jump?.let { fixed2(it) } ?: "-"
                    )
                )
            }
            Text(
                "값은 ${RETURN_TARGET_INDEX + 1}번 칸이 돌아올 자리의 선명도 — 블러를 끄고 스크림만 있는 상태 = 1. 1 보다 크면 스크림 위(오버레이)에 그려진 것, 작을수록 흐리다. " +
                    "전환 85% 는 시작 뒤 ${(RETURN_TRANSITION_MILLIS * RETURN_SAMPLE_FRACTION).toInt()}ms(칸이 거의 도착한 때).",
                fontSize = 11.sp,
                color = Muted,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        FactRow(
            "왜 갑자기 흐려지나",
            "공유 요소는 전환 동안 SharedTransitionLayout 의 오버레이(맨 위)에 그려진다. 목록에 단 blur 는 목록 레이어의 그림만 흐리므로 날아오는 칸에는 닿지 않고, " +
                "헤더의 스크림보다도 위에 있다. 전환이 끝나 칸이 목록으로 돌아가는 프레임에 블러 · 스크림이 한꺼번에 걸린다."
        )
        FactRow(
            "두 가지 해법",
            "② renderInOverlayDuringTransition = false 는 칸을 제자리(목록 안)에서 그려 전환 내내 blur 를 받는다 — 대신 다른 칸 · 목록 경계에 가려질 수 있다(KDoc: 잘림이나 갑작스러운 알파 변화가 없을 때만). " +
                "③ 원문은 뒤로 가는 전환 동안 블러를 끄고 스크림만 두었다가, 끝난 뒤 ${BLUR_FADE_IN_MILLIS}ms 에 걸쳐 블러를 올린다 — 스크림이 걸리는 순간은 남지만 흐림은 서서히 온다."
        )
    }
}

@Composable
internal fun ReturnStage(lab: ReturnLabState) {
    val scope = rememberCoroutineScope()
    SharedTransitionLayout(
        modifier = Modifier
            .testTag("returnStage")
            .fillMaxWidth()
            .height(360.dp)
            .clipToBounds()
            .background(Color(0xFFECEFF1))
    ) {
        val shared = this
        SideEffect { lab.transitionActive = { shared.isTransitionActive } }
        AnimatedContent(
            targetState = lab.selected,
            transitionSpec = { fadeIn(tween(RETURN_TRANSITION_MILLIS)) togetherWith fadeOut(tween(RETURN_TRANSITION_MILLIS)) },
            label = "returnStage"
        ) { selected ->
            if (selected == null) {
                ReturnGrid(lab, shared, this) { index -> scope.launch { lab.open(index) } }
            } else {
                ReturnDetail(selected, lab, shared, this) { scope.launch { lab.back() } }
            }
        }
    }
}

@Composable
private fun ReturnGrid(
    lab: ReturnLabState,
    shared: SharedTransitionScope,
    visibility: AnimatedVisibilityScope,
    onOpen: (Int) -> Unit
) {
    with(shared) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(RETURN_COLUMNS),
                state = lab.gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .blur { blurRadiusSpec = glassSpec(lab.blurAmount.value, size.height.value) },
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = GLASS_HEADER_DP.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(RETURN_SPACING_DP.dp),
                verticalArrangement = Arrangement.spacedBy(RETURN_SPACING_DP.dp)
            ) {
                items(RETURN_TILE_COUNT, key = { it }) { i ->
                    ReturnTile(
                        i,
                        Modifier
                            .then(if (i == RETURN_TARGET_INDEX) Modifier.measured(lab.targetBounds) else Modifier)
                            .sharedElement(
                                rememberSharedContentState("returnTile-$i"),
                                visibility,
                                boundsTransform = ReturnBounds,
                                renderInOverlayDuringTransition = lab.variant != ReturnVariant.InPlace
                            )
                            .fillMaxWidth()
                            .height(RETURN_TILE_HEIGHT_DP.dp)
                            .clickable(enabled = !lab.running) { onOpen(i) }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GLASS_HEADER_DP.dp)
                    .background(Color.White.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("갤러리", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            }
        }
    }
}

@Composable
private fun ReturnDetail(
    index: Int,
    lab: ReturnLabState,
    shared: SharedTransitionScope,
    visibility: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    with(shared) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ReturnTile(
                index,
                Modifier
                    .sharedElement(
                        rememberSharedContentState("returnTile-$index"),
                        visibility,
                        boundsTransform = ReturnBounds,
                        renderInOverlayDuringTransition = lab.variant != ReturnVariant.InPlace
                    )
                    .size(width = 240.dp, height = 170.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onBack, enabled = !lab.running) { Text("← 목록으로", fontSize = 12.sp) }
        }
    }
}

/** 줄무늬와 번호 — 흐려지면 선명도가 크게 떨어지는 그림 */
@Composable
private fun ReturnTile(index: Int, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(StripeColors[index % StripeColors.size])
            .drawBehind {
                val step = 8.dp.toPx()
                var x = -size.height
                while (x < size.width) {
                    drawLine(Color.White.copy(alpha = 0.7f), Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = 2.dp.toPx())
                    x += step
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text("${index + 1}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun SummaryCard() {
    LabCard(title = "정리", color = Indigo, description = "흐릴 것에 달고, 움직이는 값은 블록 안에서 읽는다.") {
        listOf(
            "Modifier.blur { } 는 자기 그림을 흐린다 — 헤더 밑 유리 효과는 헤더가 아니라 그 아래 목록에 세로 stop 으로 건다",
            "균일은 API 31+, 그라데이션 · stop · 셰이더 마스크는 API 33+ — 31~32 는 선명하게(identity), 31 미만은 무시",
            "블록은 레이어 블록이다 — 스크롤 위치를 그 안에서 읽으면 리컴포지션 없이 효과만 바뀐다",
            "블록의 edgeTreatment 는 매번 Rectangle 로 돌아간다. Unbounded 는 칸 밖으로 번지고 가장자리가 비친다",
            "공유 요소 전환 동안 칸은 오버레이에 그려져 blur · 스크림 밖이다 — 끝나는 프레임에 한꺼번에 흐려진다. 원문은 블러를 끄고 끝난 뒤 서서히 올렸다"
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
                modifier = Modifier.weight(if (index == 0) 1.6f else 1f),
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
