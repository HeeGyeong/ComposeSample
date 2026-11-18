package com.example.composesample.presentation.example.component.ui.graphics

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Canvas.drawMesh 리본 예제(API 34)
 *
 * 정점 셰이더가 정점마다 위치와 굵기를 정하는 TRIANGLE_STRIP 메시 한 장으로, 펄스가 지나가며 부푸는 리본을 그린다.
 * 프레임마다 CPU 가 넘기는 값은 uniform uTime 하나다. drawLine 반복 · 세그먼트마다 다른 굵기와 대조하고, 원문이 설명 없이 쓴
 * BlendMode.DST 와 함정 4가지(uniform 을 안 넣으면 빈 화면 · 이름 오타 · 소프트웨어 캔버스 · 생성 뒤 버퍼 수정)를 화면 픽셀로 잰다.
 * API 34 타입은 MeshRibbonApi34.kt, 식은 MeshRibbonLab.kt, 출처 · 실측은 exampleGuide.kt.
 */
@Composable
fun MeshRibbonExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Canvas.drawMesh Ribbon Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { ComparisonCard() }
            item { WireframeCard() }
            item { BlendCard() }
            item { TrapsCard() }
        }
    }
}

// ==================== 공통 ====================

private val StartColor = Color(0xFF1E88E5)
private val EndColor = Color(0xFFE53935)
private const val StartArgb = 0xFF1E88E5.toInt()
private const val EndArgb = 0xFFE53935.toInt()
private const val StaticTime = 0.3f

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
private fun meshSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

/** 높이에 맞춘 리본 놓임 — 부풀어도(1.8배) · 파도를 쳐도(0.9배) 칸 안에 들어오게 */
private fun ribbonGeometry(size: Size): RibbonGeometry {
    val half = size.height * 0.16f
    val margin = half * 2.2f
    return RibbonGeometry(margin, size.height / 2f, size.width - margin, size.height / 2f, half)
}

private fun Float.fmt(digits: Int = 2): String = String.format(Locale.US, "%.${digits}f", this)

private fun Int.hex(): String = String.format(Locale.US, "#%06X", this and 0xFFFFFF)

/** 리본 시계 — 초 단위 uTime. 그리기 단계에서만 읽어 리컴포지션 없이 다시 그린다 */
private class RibbonClock(initial: Float = 0f) {
    var time by mutableFloatStateOf(initial)
    var playing by mutableStateOf(true)
}

@Composable
private fun RunClock(clock: RibbonClock) {
    LaunchedEffect(clock, clock.playing) {
        if (!clock.playing) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                clock.time += ((now - last) / 1_000_000_000f).coerceAtMost(0.1f)
                last = now
            }
        }
    }
}

/** 그리기 단계 집계 — 일반 필드(상태 아님)라 세는 일이 다시 그리기를 만들지 않는다 */
private class DrawStats {
    var frames = 0L
    var nanos = 0L
    var callsPerFrame = 0

    fun record(elapsed: Long, calls: Int) {
        frames++
        nanos += elapsed
        callsPerFrame = calls
    }

    fun reset() {
        frames = 0
        nanos = 0
    }
}

/** 트랩 카드가 그려진 렌더러를 붙잡고 다시 그리게 하려고 쓰는 손잡이 */
private class RendererHolder {
    var renderer: RibbonRenderer? = null
    val redraw = mutableIntStateOf(0)
}

/**
 * drawMesh 리본 한 칸. API 34 미만은 같은 위치 식을 CPU 에서 Path 로 채운 대체 그림.
 * Mesh 는 drawWithCache 블록(크기가 바뀔 때만)에서 만들고, 프레임마다 uTime 만 넣는다(원문과 같은 구조).
 */
@Composable
private fun MeshRibbon(
    time: () -> Float,
    modifier: Modifier = Modifier,
    blend: RibbonBlend = RibbonBlend.Dst,
    paintColor: Int = 0xFF000000.toInt(),
    allUniforms: Boolean = true,
    wireframe: Boolean = false,
    stats: DrawStats? = null,
    holder: RendererHolder? = null
) {
    Box(
        modifier = modifier
            .graphicsLayer()
            .drawWithCache {
                val geometry = ribbonGeometry(size)
                val renderer = if (meshSupported()) {
                    MeshRibbonApi34.create(size.width, size.height, geometry, StartArgb, EndArgb, allUniforms)
                } else {
                    null
                }
                holder?.renderer = renderer
                onDrawBehind {
                    holder?.redraw?.intValue
                    val t = time()
                    val started = System.nanoTime()
                    if (renderer != null) {
                        drawIntoCanvas { renderer.draw(it.nativeCanvas, t, blend, paintColor) }
                    } else {
                        drawCpuRibbon(t, geometry, StartColor, EndColor)
                    }
                    stats?.record(System.nanoTime() - started, 1)
                    if (wireframe) drawRibbonWireframe(t, geometry)
                }
            }
    )
}

/** drawLine 반복 — 가운데 선을 80조각으로 나눠 조각마다 그리기 호출 하나(MeshRibbonDrawing.kt) */
@Composable
private fun LineRibbon(time: () -> Float, variableWidth: Boolean, modifier: Modifier = Modifier, stats: DrawStats? = null) {
    Canvas(modifier = modifier.graphicsLayer()) {
        val t = time()
        val started = System.nanoTime()
        val calls = drawLineRibbon(t, ribbonGeometry(size), variableWidth, StartColor, EndColor)
        stats?.record(System.nanoTime() - started, calls)
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard() {
    val clock = remember { RibbonClock() }
    RunClock(clock)
    // 리본 칸의 리컴포지션 — 시간이 그리기 단계에서만 읽히면 재생 중에도 늘지 않는다
    val compositions = remember { IntArray(1) }
    var shown by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            shown = compositions[0]
        }
    }

    SectionCard(
        title = "🎀 drawMesh — 정점 셰이더가 정점마다 굵기를 정하는 리본",
        description = "정점 162개(진행 0~1 × 좌우 ±1)를 TRIANGLE_STRIP 으로 한 번에 그린다. 위치 · 굵기 · 파도 · 부푸는 펄스는 " +
            "전부 정점 셰이더가 uniform(uStart · uEnd · uHalfWidth · uTime)으로 계산하고, 프래그먼트 셰이더가 가운데서 떨어진 거리로 " +
            "가장자리를 부드럽게 · 진행 거리로 색을 섞는다. 프레임마다 CPU 가 넘기는 값은 uTime 하나다.",
        containerColor = Color(0xFFE8EAF6)
    ) {
        StageBox {
            OverviewStage(clock, compositions)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(if (clock.playing) "멈춤" else "재생", Color(0xFF3949AB)) { clock.playing = !clock.playing }
        }
        if (!clock.playing) {
            Slider(value = RibbonMath.fract(clock.time), onValueChange = { clock.time = it })
        }
        InfoLine("이 기기", "API ${Build.VERSION.SDK_INT} · " + if (meshSupported()) "drawMesh" else "drawMesh 없음(API 34+) → CPU Path 대체")
        InfoLine("정점 · 프레임마다", "${RibbonMath.vertexCount()}개 · uTime 1개(setFloatUniform) + drawMesh 1회")
        InfoLine("리본 칸 리컴포지션", "${shown}회 (재생 중에도 그대로)")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "val spec = MeshSpecification.make(\n" +
                "    arrayOf(Attribute(TYPE_FLOAT2, 0, \"corner\")), 8,\n" +
                "    arrayOf(Varying(TYPE_FLOAT3, \"stroke\")), VERTEX, FRAGMENT)   // 한 번만\n" +
                "Modifier.graphicsLayer().drawWithCache {\n" +
                "    val mesh = Mesh(spec, Mesh.TRIANGLE_STRIP, corners, 162, RectF(0f, 0f, w, h))\n" +
                "        .apply { setFloatUniform(\"uStart\", …); setColorUniform(\"uStartColor\", …) }\n" +
                "    onDrawBehind {\n" +
                "        mesh.setFloatUniform(\"uTime\", time())\n" +
                "        drawIntoCanvas { it.nativeCanvas.drawMesh(mesh, BlendMode.DST, paint) }\n" +
                "    }\n" +
                "}"
        )
    }
}

/** 리본 칸 — 별도 컴포저블이라 인자(같은 인스턴스)가 그대로면 카드가 다시 그려져도 건너뛴다 */
@Composable
private fun OverviewStage(clock: RibbonClock, compositions: IntArray) {
    SideEffect { compositions[0]++ }
    MeshRibbon(time = { clock.time }, modifier = Modifier.fillMaxWidth().height(140.dp))
}

// ==================== 2. drawLine 반복과 대조 ====================

@Composable
private fun ComparisonCard() {
    val clock = remember { RibbonClock() }
    RunClock(clock)
    val stats = remember { List(3) { DrawStats() } }
    var lines by remember { mutableStateOf(List(3) { "" }) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            lines = stats.map { s ->
                val avgMicros = if (s.frames == 0L) 0.0 else s.nanos / s.frames / 1000.0
                "그리기 호출 ${s.callsPerFrame}회/프레임 · ${s.frames}프레임/초 · CPU 기록 평균 ${String.format(Locale.US, "%.1f", avgMicros)} µs"
                    .also { s.reset() }
            }
        }
    }

    SectionCard(
        title = "📏 drawLine 반복 · 조각마다 굵기 · drawMesh",
        description = "원문: 경로를 잘게 나눠 drawLine 을 반복하면 조각마다 그리기 호출이 따로 생기고 굵기는 하나뿐이다. 앞쪽은 맞다 — " +
            "80조각이면 80회. 굵기는 조각마다 strokeWidth 를 바꾸면 달라지지만, 끝이 각진 조각들이라 꺾이는 곳마다 틈과 겹침이 생긴다. " +
            "메시는 한 번의 호출로 이어진 가장자리를 만든다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        StageRow("① drawLine 80회 · 굵기 하나") {
            LineRibbon({ clock.time }, variableWidth = false, modifier = Modifier.fillMaxWidth().height(120.dp), stats = stats[0])
        }
        SmallLabel(lines[0])
        StageRow("② drawLine 80회 · 조각마다 굵기") {
            LineRibbon({ clock.time }, variableWidth = true, modifier = Modifier.fillMaxWidth().height(120.dp), stats = stats[1])
        }
        SmallLabel(lines[1])
        StageRow("③ drawMesh 1회 · 정점마다 굵기") {
            MeshRibbon({ clock.time }, modifier = Modifier.fillMaxWidth().height(120.dp), stats = stats[2])
        }
        SmallLabel(lines[2])
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(if (clock.playing) "멈춤" else "재생", Color(0xFF1565C0)) { clock.playing = !clock.playing }
        }
        if (!clock.playing) {
            Slider(value = RibbonMath.fract(clock.time), onValueChange = { clock.time = it })
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "CPU 기록 시간은 그리기 명령을 기록하는 데 든 시간뿐이다(GPU 가 래스터화하는 시간은 아님). 1초 창 하나는 GC 등으로 수백 µs " +
                "튈 수 있어 몇 번 이어 본다. drawLine 쪽은 점 81개를 " +
                "프레임마다 CPU 에서 계산하고, 메시는 uniform 하나를 넣는다. debug 빌드는 HotSwan 인터프리터가 섞여 크게 나온다 — " +
                "release 실측은 exampleGuide.kt. API 34 미만에서 ③은 같은 식의 CPU Path 대체라 호출 수가 1 로 보인다."
        )
    }
}

// ==================== 3. 삼각형 보기 ====================

@Composable
private fun WireframeCard() {
    val clock = remember { RibbonClock(initial = 0.35f).apply { playing = false } }
    RunClock(clock)
    var showMesh by remember { mutableStateOf(true) }
    SectionCard(
        title = "🔺 정점 162개 · 삼각형 160개",
        description = "노란 점은 정점 셰이더와 같은 식을 CPU 에서 계산한 정점 위치다. 정점마다 corner 두 숫자만 들고 있고(버퍼는 " +
            "만들 때 한 번), 위치는 uniform 과 함께 GPU 가 매번 계산한다. 펄스가 지나는 자리의 정점 쌍이 벌어지는 것이 '정점마다 굵기'다.",
        containerColor = Color(0xFFFFF8E1)
    ) {
        StageBox {
            if (showMesh) {
                MeshRibbon({ clock.time }, modifier = Modifier.fillMaxWidth().height(160.dp), wireframe = true)
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) { drawRibbonWireframe(clock.time, ribbonGeometry(size)) }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(value = RibbonMath.fract(clock.time), onValueChange = { clock.time = it; clock.playing = false })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(if (clock.playing) "멈춤" else "재생", Color(0xFFF57F17)) { clock.playing = !clock.playing }
            DemoButton(if (showMesh) "정점만" else "메시와 함께", Color(0xFF6D4C41)) { showMesh = !showMesh }
        }
        val pulse = RibbonMath.fract(clock.time)
        InfoLine("펄스 위치", "진행 ${pulse.fmt()} · 그 자리 반지름 = halfWidth × ${(RibbonMath.radius(pulse, clock.time, 1f)).fmt()}")
        InfoLine("먼 곳", "halfWidth × ${(RibbonMath.radius(RibbonMath.fract(pulse + 0.5f), clock.time, 1f)).fmt()}")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "// corner = (진행 0~1, 좌우 ±1) — 버퍼에는 이 두 숫자만\n" +
                "float radius = uHalfWidth * (0.65 + 1.15 * exp(-(pulse * pulse) / 0.025));\n" +
                "float across = a.corner.y * (radius + 1.5);\n" +
                "v.position = uStart + dir * a.corner.x + normal * (wave + across);\n" +
                "v.stroke = float3(along, across, radius);   // 프래그먼트가 가장자리 · 색에 쓴다"
        )
    }
}

// ==================== 4. BlendMode ====================

private data class BlendTile(val blend: RibbonBlend, val paintColor: Int, val paintLabel: String)

private val BlendTiles = listOf(
    BlendTile(RibbonBlend.Dst, 0xFF000000.toInt(), "Paint() 검정"),
    BlendTile(RibbonBlend.Dst, 0xFFFFFFFF.toInt(), "흰 Paint"),
    BlendTile(RibbonBlend.Modulate, 0xFF000000.toInt(), "Paint() 검정"),
    BlendTile(RibbonBlend.Modulate, 0xFFFFFFFF.toInt(), "흰 Paint"),
    BlendTile(RibbonBlend.Src, 0xFF000000.toInt(), "Paint() 검정"),
    BlendTile(RibbonBlend.Src, 0xFFFFFFFF.toInt(), "흰 Paint")
)

private val TileBackground = Color(0xFFE0E0E0)

@Composable
private fun BlendCard() {
    val window = LocalActivity.current?.window
    val scope = rememberCoroutineScope()
    val bounds = remember { BlendTiles.map { MeasuredBounds() } }
    var colors by remember { mutableStateOf<List<String>?>(null) }
    var message by remember { mutableStateOf("") }

    SectionCard(
        title = "🎨 BlendMode — 원문의 DST 는 'Paint 를 무시'",
        description = "drawMesh(mesh, blendMode, paint) 는 메시 색을 dst, Paint 색을 src 로 섞는다(Canvas 문서). null 이면 MODULATE " +
            "— 둘을 곱한다. 그래서 기본 Paint()(검정)로 null 을 넘기면 리본이 검게 나온다(#107 MeshGradient 의 drawVertices 와 같은 축). " +
            "원문의 DST 는 Paint 를 버리고 메시 색만 쓰며, SRC 는 반대로 메시 모양에 Paint 색만 칠한다.",
        containerColor = Color(0xFFFCE4EC)
    ) {
        if (!meshSupported()) {
            SmallLabel("API ${Build.VERSION.SDK_INT} — drawMesh 는 API 34 부터라 이 카드는 비어 있다")
            return@SectionCard
        }
        BlendTiles.chunked(2).forEachIndexed { row, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEachIndexed { col, tile ->
                    val index = row * 2 + col
                    Column(modifier = Modifier.weight(1f)) {
                        SmallLabel("${tile.blend.label} · ${tile.paintLabel}")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .background(TileBackground, RoundedCornerShape(6.dp))
                        ) {
                            MeshRibbon(
                                { StaticTime },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .measured(bounds[index]),
                                blend = tile.blend,
                                paintColor = tile.paintColor
                            )
                        }
                        colors?.getOrNull(index)?.let { SmallLabel("가운데 $it") }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        DemoButton("가운데 색 읽기", Color(0xFFAD1457)) {
            if (window == null || !pixelCopyAvailable()) {
                message = "PixelCopy(창) 는 API 26 이상"
                return@DemoButton
            }
            if (bounds.any { !it.fullyVisible }) {
                message = "타일 여섯 개가 화면에 다 보이게 스크롤한 뒤 다시 누른다"
                return@DemoButton
            }
            scope.launch {
                message = ""
                colors = bounds.map { b ->
                    // 리본 가운데(진행 0.5)의 작은 사각형 — CPU 거울 식으로 위치를 구한다
                    val r = b.bounds
                    val local = FloatArray(2)
                    RibbonMath.position(0.5f, 0f, StaticTime, ribbonGeometry(Size(r.width, r.height)), local)
                    val cx = r.left + local[0]
                    val cy = r.top + local[1]
                    val probe = Rect(cx - 2f, cy - 2f, cx + 2f, cy + 2f)
                    WindowCapture.read(window, probe)?.let { RibbonMath.averageColor(it.pixels).hex() } ?: "실패"
                }
                MeshRibbonProbe.log("blend " + BlendTiles.zip(colors.orEmpty()).joinToString(" ") { (t, c) -> "${t.blend}/${t.paintColor.hex()}=$c" })
            }
        }
        if (message.isNotEmpty()) SmallLabel(message)
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "canvas.drawMesh(mesh, null, Paint())            // MODULATE × 검정 → 검게\n" +
                "canvas.drawMesh(mesh, BlendMode.DST, paint)    // 메시 색만(원문)\n" +
                "canvas.drawMesh(mesh, BlendMode.MODULATE, Paint().apply { color = WHITE })  // 흰색을 곱해 그대로"
        )
    }
}

// ==================== 5. 함정 ====================

@Composable
private fun TrapsCard() {
    val window = LocalActivity.current?.window
    val scope = rememberCoroutineScope()
    val snippetBounds = remember { MeasuredBounds() }
    val fullBounds = remember { MeasuredBounds() }
    val bufferBounds = remember { MeasuredBounds() }
    val holder = remember { RendererHolder() }
    var bufferKey by remember { mutableIntStateOf(0) }
    var snippetResult by remember { mutableStateOf("") }
    var softwareResult by remember { mutableStateOf("") }
    var bufferResult by remember { mutableStateOf(listOf<String>()) }

    SectionCard(
        title = "🪤 함정 네 가지",
        description = "① 원문 스니펫은 uTime 만 넣는다 — 나머지 uniform 은 0 으로 남아(방향 0 → normalize 가 NaN, 색 0 = 투명) 아무것도 " +
            "안 그려진다. ② uniform 이름이 틀리면 문서는 IllegalArgumentException 이라지만, 그 메시지를 만드는 JNI 호출이 깨진 문자열을 " +
            "넘겨 디버깅 가능 앱(CheckJNI)은 catch 로 못 막는 네이티브 abort 로 죽는다 — 그래서 이 카드엔 그 버튼이 없다. " +
            "③ 비트맵 같은 소프트웨어 캔버스에는 그릴 수 없다. ④ 정점 버퍼는 Mesh 를 만들 때 복사된다 — 만든 뒤 버퍼를 고쳐도 모양이 " +
            "그대로이고, 다시 만들어야 바뀐다.",
        containerColor = Color(0xFFFFF3E0)
    ) {
        if (!meshSupported()) {
            SmallLabel("API ${Build.VERSION.SDK_INT} — drawMesh 는 API 34 부터라 이 카드는 비어 있다")
            return@SectionCard
        }
        StageRow("① 원문 스니펫 그대로(uTime 만)") {
            MeshRibbon({ StaticTime }, modifier = Modifier.fillMaxWidth().height(90.dp).measured(snippetBounds), allUniforms = false)
        }
        StageRow("① 나머지 uniform 까지 넣음") {
            MeshRibbon(
                { StaticTime },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .measured(fullBounds)
            )
        }
        DemoButton("① 그려진 픽셀 세기", Color(0xFFEF6C00)) {
            if (window == null || !pixelCopyAvailable()) return@DemoButton
            if (!snippetBounds.fullyVisible || !fullBounds.fullyVisible) {
                snippetResult = "두 줄이 화면에 다 보이게 스크롤한 뒤 다시 누른다"
                return@DemoButton
            }
            scope.launch {
                val a = WindowCapture.read(window, snippetBounds.bounds)?.let { RibbonMath.paintedPixels(it.pixels, 0xFFFFFFFF.toInt()) }
                val b = WindowCapture.read(window, fullBounds.bounds)?.let { RibbonMath.paintedPixels(it.pixels, 0xFFFFFFFF.toInt()) }
                snippetResult = "그려진 픽셀 — uTime 만: $a · 전부: $b"
                MeshRibbonProbe.log("snippet onlyTime=$a all=$b")
            }
        }
        if (snippetResult.isNotEmpty()) SmallLabel(snippetResult)
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("③ 비트맵 캔버스에 그리기", Color(0xFF6A1B9A)) {
                softwareResult = MeshRibbonApi34.probeSoftwareCanvas(400f, 100f, ribbonGeometry(Size(400f, 100f)))
                MeshRibbonProbe.log("software $softwareResult")
            }
        }
        CodeBox(
            "// ② API 35 에뮬 실측 — mesh.setFloatUniform(\"uTme\", t)\n" +
                "// release : IllegalArgumentException: unable to find uniform named P瘱  (이름이 깨져 나온다)\n" +
                "// debug   : JNI DETECTED ERROR IN APPLICATION: input is not valid Modified UTF-8 → abort\n" +
                "private const val U_TIME = \"uTime\"            // 이름은 한 곳에\n" +
                "val VERTEX = \"\"\"uniform float \$U_TIME; …\"\"\"\n" +
                "mesh.setFloatUniform(U_TIME, t)"
        )
        if (softwareResult.isNotEmpty()) SmallLabel("③ $softwareResult")
        Spacer(modifier = Modifier.height(10.dp))

        StageRow("④ 정점 버퍼 — 만든 뒤 고치기") {
            key(bufferKey) {
                MeshRibbon(
                    { StaticTime },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .measured(bufferBounds),
                    holder = holder
                )
            }
        }
        suspend fun countBuffer(label: String) {
            if (window == null || !pixelCopyAvailable() || !bufferBounds.fullyVisible) {
                bufferResult = bufferResult + "$label: 화면에 다 보이게 스크롤"
                return
            }
            repeat(3) { withFrameNanos { } }
            val n = WindowCapture.read(window, bufferBounds.bounds)?.let { RibbonMath.paintedPixels(it.pixels, 0xFFFFFFFF.toInt()) }
            bufferResult = bufferResult + "$label: 그려진 픽셀 $n"
            MeshRibbonProbe.log("buffer $label=$n")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("버퍼 고치기(굵기 0)", Color(0xFF00897B)) {
                scope.launch {
                    holder.renderer?.flattenBufferAfterBuild()
                    holder.redraw.intValue++
                    countBuffer("버퍼만 고침")
                }
            }
            DemoButton("메시 다시 만들기", Color(0xFF00897B)) {
                scope.launch {
                    holder.renderer?.rebuild()
                    holder.redraw.intValue++
                    countBuffer("다시 만듦")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("처음 상태 세기", Color(0xFF546E7A)) { scope.launch { countBuffer("처음") } }
            DemoButton("되돌리기", Color(0xFF546E7A)) {
                bufferKey++
                bufferResult = emptyList()
            }
        }
        bufferResult.forEach { SmallLabel(it) }
    }
}

/** 측정 결과를 logcat 에도 남긴다(adb 로 대조할 때) */
private object MeshRibbonProbe {
    fun log(message: String) {
        android.util.Log.i("MeshRibbon", message)
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
    Spacer(modifier = Modifier.height(6.dp))
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
