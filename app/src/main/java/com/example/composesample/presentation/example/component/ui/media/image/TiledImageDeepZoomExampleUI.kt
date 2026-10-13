package com.example.composesample.presentation.example.component.ui.media.image

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 대용량 이미지 타일 딥줌 — BitmapRegionDecoder 로 보이는 영역만 타일 단위로, 배율에 맞춘 inSampleSize 로 디코딩한다.
 * - 원본(8000×6000 JPEG)은 처음 한 번 그려 캐시 폴더에 저장한다(APK 에 수십 MB 에셋을 넣지 않는다)
 * - 타일 계산·디코더는 TiledImage.kt(순수 계산은 단위 테스트 TiledImageMathTest)
 * - 참고 자료(URL/핵심 개념)는 exampleGuide.kt 의 "TiledImageDeepZoom" 섹션 참고.
 */
@Composable
fun TiledImageDeepZoomExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    var source by remember { mutableStateOf<SourceInfo?>(null) }
    var checked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        source = withContext(Dispatchers.IO) { readSourceInfo(sourceFile(context.cacheDir)) }
        checked = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Tiled Image Deep Zoom",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item { SourceCard(source = source, checked = checked, onCreated = { source = it }) }
            source?.let { info ->
                item(key = "viewer") { ViewerCard(info) }
                item(key = "whole") { WholeDecodeCard(info) }
            }
            item { GuideCard() }
        }
    }
}

@Composable
private fun ExampleCard(
    title: String,
    description: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun StatLine(text: String, color: Color = Color(0xFF37474F)) {
    Text(text = text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = color)
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "보이는 만큼만, 보이는 해상도로", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "8000×6000 사진을 한 번에 디코딩하면 ARGB_8888 로 183MB 다. 뷰어 폭이 1000px 남짓이면 전체를 볼 때는 8분의 1 해상도(1000×750)로 충분하고, " +
                    "확대했을 때는 화면에 걸친 몇 조각만 원본 해상도로 있으면 된다. BitmapRegionDecoder 는 파일에서 사각형 영역만, " +
                    "inSampleSize 는 그 영역을 2의 거듭제곱 배로 줄여 디코딩한다.",
                fontSize = 12.sp,
                color = Color(0xFF333333)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 1. 원본 준비
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun SourceCard(source: SourceInfo?, checked: Boolean, onCreated: (SourceInfo) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var creating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var createdMs by remember { mutableStateOf<Long?>(null) }
    val accent = Color(0xFF1E88E5)

    ExampleCard(
        title = "1. 원본 준비 (한 번)",
        description = "8000×6000 원본을 그려 캐시 폴더에 JPEG 로 저장한다. 1000px 블록마다 큰 이름(A1~H6), 100px 칸마다 작은 좌표가 있어 확대할수록 읽히는 글자가 늘어난다.",
        accent = accent
    ) {
        when {
            !checked -> StatLine("원본 확인 중...")
            source != null -> {
                StatLine("${source.file.name} · ${source.width}×${source.height} · 파일 ${mb(source.fileBytes)}")
                createdMs?.let { StatLine("만드는 데 ${it}ms (RGB_565 비트맵 ${mb(source.width.toLong() * source.height * 2)} 을 잠깐 잡았다 놓음)") }
            }
            else -> {
                Button(
                    onClick = {
                        creating = true
                        error = null
                        scope.launch {
                            try {
                                val created = createSourceImage(context.cacheDir)
                                createdMs = created.createdMs
                                onCreated(created)
                            } catch (e: OutOfMemoryError) {
                                error = "원본을 그릴 메모리가 부족하다: ${e.message}"
                            } finally {
                                creating = false
                            }
                        }
                    },
                    enabled = !creating,
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Text(text = if (creating) "그리는 중... (수 초)" else "원본 만들기", color = Color.White)
                }
                error?.let { StatLine(it, Color(0xFFC62828)) }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 2. 타일 뷰어
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun ViewerCard(source: SourceInfo) {
    val state = remember(source.file) { TiledImageState(source) }
    var showTiles by remember { mutableStateOf(true) }
    val bitmapPaint = remember { Paint().apply { isFilterBitmap = true } }
    val borderPaint = remember {
        Paint().apply { style = Paint.Style.STROKE; strokeWidth = 3f; color = android.graphics.Color.MAGENTA }
    }
    val labelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.MAGENTA; textSize = 30f; isFakeBoldText = true }
    }

    DisposableEffect(state) {
        onDispose { state.close() }
    }
    LaunchedEffect(state) {
        // 크기가 정해진 뒤 개요 한 장, 그다음부터는 변환이 바뀔 때마다 보이는 타일 요청
        snapshotFlow { state.viewWidth }.collect { if (it > 0) state.loadOverview() }
    }
    LaunchedEffect(state) {
        snapshotFlow { listOf(state.scale, state.offsetX, state.offsetY, state.viewWidth.toFloat()) }
            .collect { state.requestVisible() }
    }

    ExampleCard(
        title = "2. 타일 뷰어 (핀치 줌 · 드래그)",
        description = "바닥에는 전체 보기보다 한 단계 성긴 개요 한 장을 늘 깔고, 그 위에 지금 배율의 inSampleSize 로 디코딩한 512px 타일을 덮는다. 전용 스레드 하나가 보이는 타일만 디코딩하고 지나간 요청은 취소한다.",
        accent = Color(0xFF8E24AA)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clipToBounds()
                .background(Color(0xFF263238))
                .testTag("tileViewer")
                .onSizeChanged { state.onViewSize(it.width, it.height) }
                .pointerInput(state) {
                    // detectTransformGestures 는 전체 보기에서도 세로 드래그를 전부 소비해 목록이 스크롤되지 않는다 →
                    // 두 손가락이거나 확대된 상태일 때만 이벤트를 쓰고, 전체 보기의 한 손가락 드래그는 목록에 넘긴다
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.count { it.pressed }
                            if (pressed > 1 || state.scale > state.fitScale * 1.001f) {
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                val centroid = event.calculateCentroid()
                                if (zoom != 1f) state.zoomBy(zoom, centroid.x, centroid.y)
                                if (pan != Offset.Zero) state.panBy(pan.x, pan.y)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                state.tileVersion // 타일이 도착하면 다시 그린다
                val canvas = drawContext.canvas.nativeCanvas
                val s = state.scale
                val ox = state.offsetX
                val oy = state.offsetY
                state.overview?.let { overview ->
                    canvas.drawBitmap(overview, null, RectF(ox, oy, ox + source.width * s, oy + source.height * s), bitmapPaint)
                }
                state.visible.forEach { tile ->
                    val r = tile.rect
                    // 타일 경계에 틈이 생기지 않게 실수 좌표 그대로 그린다
                    val dst = RectF(ox + r.left * s, oy + r.top * s, ox + r.right * s, oy + r.bottom * s)
                    state.cached(tile.key)?.let { canvas.drawBitmap(it, null, dst, bitmapPaint) }
                    if (showTiles) {
                        canvas.drawRect(dst, borderPaint)
                        canvas.drawText("1/${tile.key.sample} ${tile.key.col},${tile.key.row}", dst.left + 8f, dst.top + 34f, labelPaint)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(onClick = { state.fit() }) { Text(text = "전체", fontSize = 12.sp) }
            OutlinedButton(onClick = { state.centerOn(state.viewportCenterX, state.viewportCenterY, 0.5f) }) { Text(text = "50%", fontSize = 12.sp) }
            OutlinedButton(onClick = { state.centerOn(0f, 0f, 1f) }) { Text(text = "1:1 왼쪽 위", fontSize = 12.sp) }
            OutlinedButton(onClick = { state.centerOn(source.width / 2f, source.height / 2f, 1f) }) { Text(text = "1:1 가운데", fontSize = 12.sp) }
            OutlinedButton(onClick = { state.centerOn(source.width.toFloat(), source.height.toFloat(), 1f) }) { Text(text = "1:1 오른쪽 아래", fontSize = 12.sp) }
            FilterChip(selected = showTiles, onClick = { showTiles = !showTiles }, label = { Text(text = "타일 경계", fontSize = 12.sp) })
            OutlinedButton(onClick = { state.clearCache() }) { Text(text = "캐시 비우기", fontSize = 12.sp) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        ViewerStats(state)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "왼쪽 위 → 가운데 → 오른쪽 아래 순으로 1:1 을 눌러 보면 타일 한 장의 디코딩이 아래로 갈수록 길어진다" +
                "(SM-A725F release: 위 70ms · 가운데 224ms · 아래 396ms, 통째 디코딩 390ms). 기본 JPEG 는 위에서 아래로 이어진 " +
                "한 줄기 압축 데이터라 아래쪽 영역을 꺼내려면 그 위를 먼저 읽어야 한다 — 타일 방식이 아끼는 것은 시간이 아니라 메모리다.",
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun ViewerStats(state: TiledImageState) {
    state.tileVersion // 타일 도착 시 숫자 갱신
    val viewport = state.viewport
    val visible = state.visible
    val visibleBytes = visible.sumOf { it.decodedWidth.toLong() * it.decodedHeight * 4 }
    StatLine("배율 ${"%.1f".format(state.scale * 100)}% (전체 보기 ${"%.1f".format(state.fitScale * 100)}%) · inSampleSize ${state.sample}")
    StatLine("보이는 영역 x ${viewport.left}~${viewport.right} · y ${viewport.top}~${viewport.bottom}")
    StatLine("보이는 타일 ${visible.size}장 · 그 디코딩 크기 ${mb(visibleBytes)}")
    StatLine("캐시 ${state.cacheCount}장 ${mb(state.cacheBytes.toLong())} / 24.0MB · 밀려남 ${state.evictedCount}")
    StatLine("디코딩 ${state.decodedCount}장 · 평균 ${"%.0f".format(state.averageDecodeMs)}ms (최근 ${"%.0f".format(state.lastDecodeMs)}ms) · 대기 ${state.inFlightCount} · 취소 ${state.cancelledCount}")
    state.overview?.let {
        StatLine("개요 ${it.width}×${it.height} (inSampleSize ${state.overviewSample}) ${mb(it.allocationByteCount.toLong())}")
    }
    // 1:1 타일의 행 구간별 평균 — 원본 6000px 는 12행(0~11)
    val bands = listOf("위 0~3행" to 0..3, "가운데 4~7행" to 4..7, "아래 8~11행" to 8..11)
        .mapNotNull { (name, rows) -> state.fullResAverage(rows)?.let { (ms, n) -> "$name ${"%.0f".format(ms)}ms(${n}장)" } }
    if (bands.isNotEmpty()) StatLine("1:1 타일 평균 · " + bands.joinToString(" · "))
}

// ──────────────────────────────────────────────────────────────────────────
// 3. 대조군 — 한 번에 전부
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun WholeDecodeCard(source: SourceInfo) {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<WholeDecodeResult?>(null) }
    val accent = Color(0xFFE53935)

    ExampleCard(
        title = "3. 대조군 — decodeFile 로 한 번에",
        description = "inSampleSize 없이 전체를 디코딩해 할당 크기와 시간을 재고, 그 비트맵을 하드웨어 가속 화면이 그릴 수 있는지 RenderNode 로 확인한 뒤 바로 놓는다.",
        accent = accent
    ) {
        listOf(1, 2, 4, 8).forEach { sample ->
            val w = (source.width + sample - 1) / sample
            val h = (source.height + sample - 1) / sample
            StatLine("inSampleSize $sample → ${w}×${h} · ARGB ${mb(argbBytes(source.width, source.height, sample))} (계산)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                running = true
                scope.launch {
                    try {
                        result = decodeWhole(source)
                    } finally {
                        running = false
                    }
                }
            },
            enabled = !running,
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Text(text = if (running) "디코딩 중..." else "한 번에 디코딩", color = Color.White)
        }
        result?.let { r ->
            Spacer(modifier = Modifier.height(6.dp))
            StatLine("결과: ${r.text}")
            r.bytes?.let { StatLine("할당 ${mb(it)} · ${r.ms}ms") }
            r.drawProbe?.let { StatLine("그리기: $it", if (it.startsWith("그릴 수")) Color(0xFF2E7D32) else Color(0xFFC62828)) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "타일 방식이 한 번에 쥐는 것은 개요 한 장과 타일 캐시(상한 24MB)뿐이다. 비트맵 픽셀은 Android 8 부터 Java 힙이 아니라 네이티브 메모리라 " +
                "Java 힙 한도(256MB)와 무관하게 디코딩은 되지만, 하드웨어 가속 캔버스가 이 크기의 비트맵은 그리기를 거부한다(위 RenderNode 결과).",
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun GuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "핵심 정리", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "BitmapRegionDecoder.decodeRegion(rect, options) — 파일에서 사각형만 디코딩. API 31 부터 newInstance(path), 그 전은 newInstance(path, false)",
                "inSampleSize 는 2의 거듭제곱 — 1/배율을 넘지 않는 가장 큰 값을 고르면 화면보다 성기게 디코딩하지 않는다",
                "타일 크기는 디코딩 픽셀 기준(512px)으로 고정 — 배율이 바뀌어도 한 장은 1MB 이하, 원본에서 덮는 넓이가 sample 배로 커진다",
                "개요 한 장을 늘 바닥에 깔아 타일이 오기 전에도 빈칸이 없다",
                "디코딩은 전용 스레드 하나에서 — 화면을 지나간 요청은 시작 전에 취소해야 지금 화면이 빨리 찬다",
                "캐시는 바이트 기준 LruCache(sizeOf = allocationByteCount), 밀려난 비트맵은 그리는 중일 수 있어 recycle 하지 않는다",
                "타일은 실수 좌표 RectF 로 그린다 — 정수로 반올림하면 타일 사이에 1px 틈이 생긴다",
                "한 번에 디코딩한 큰 비트맵은 하드웨어 가속 캔버스가 그리기를 거부한다(RenderNode 확인)",
                "기본 JPEG 는 위에서 아래로 이어진 압축 데이터 — 아래쪽 타일일수록 디코딩이 길다. 타일이 아끼는 것은 메모리다"
            ).forEach {
                Text(
                    text = "• $it",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}
