package com.example.composesample.presentation.example.component.ui.graphics

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

/**
 * Mesh Gradient — MeshGradientPainter (Compose 1.12 신규)
 *
 * rows·columns 는 "패치" 수이고 꼭짓점은 (rows+1)×(columns+1) 개다. 꼭짓점마다 위치(0~1 정규화)·색·
 * 좌상우하 베지어 제어점(꼭짓점 기준 상대 오프셋)을 주면, 렌더러가 패치를 베지어 곡면으로 잘게 나눠
 * Canvas.drawVertices(TRIANGLES) 로 그린다. 공개 진입점은 MeshGradientPainter 하나다
 * (MeshGradientConfig·MeshGradientRenderer 는 Kotlin internal).
 *
 * 렌더러는 색을 지정하지 않은 기본 Paint(검정)로 drawVertices 를 부르는데, 꼭짓점 색에 Paint 색을 곱하는
 * 기기에서는 메시 전체가 검게 나온다. 화면이 뜰 때 이를 판정해 호환 모드(흰 Paint 소프트웨어 렌더)를 자동으로 켠다.
 * 참고 자료와 핵심 개념은 같은 폴더의 exampleGuide.kt 참고.
 */
@Composable
fun MeshGradientExampleUI(
    onBackEvent: () -> Unit
) {
    val density = LocalDensity.current
    // 화면이 뜰 때 한 번, 이 기기의 drawVertices 가 Paint 색을 곱하는지 직접 그려서 판정한다
    val support = remember(density) { measureColorSupport(density) }
    var compatMode by remember(support) { mutableStateOf(support.affected) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Mesh Gradient Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard(compat = compatMode, affected = support.affected) }
            item { CompatibilityCard(support = support, compat = compatMode, onCompatChange = { compatMode = it }) }
            item { ColorSpaceCard(compat = compatMode) }
            item { GridInterpolationCard(compat = compatMode) }
            item { VertexEditorCard(compat = compatMode) }
            item { AnimatedMeshCard(compat = compatMode) }
            item { PitfallCard(compat = compatMode) }
        }
    }
}

// ==================== 공통 데이터 · 계산 ====================

// 3×3 꼭짓점(2×2 패치) 기본 색 — 행 우선(row-major) 순서
private val HeroColors = listOf(
    Color(0xFF3949AB), Color(0xFF8E24AA), Color(0xFFD81B60),
    Color(0xFF00ACC1), Color(0xFFFFFFFF), Color(0xFFFF7043),
    Color(0xFF43A047), Color(0xFFFDD835), Color(0xFFFB8C00)
)

private val MeshPalette = listOf(
    Color(0xFFFF5252), Color(0xFFFFB300), Color(0xFF66BB6A), Color(0xFF29B6F6),
    Color(0xFF7E57C2), Color(0xFFEC407A), Color(0xFF26A69A), Color(0xFFFFFFFF)
)

private val AuroraColors = listOf(
    Color(0xFF0D1B2A), Color(0xFF1B998B), Color(0xFF2EC4B6),
    Color(0xFF7B2CBF), Color(0xFFE0AAFF), Color(0xFF3A86FF)
)

private const val EditorRows = 2
private const val EditorColumns = 2

/**
 * 2×2 패치 메시. skipCenter 가 true 면 가운데 꼭짓점(1,1)의 setVertex 를 빼먹는다(함정 카드용).
 * Painter 는 onDraw 마다 이 블록을 다시 실행한다(1.12.1 바이트코드: onDraw → configure(block) → renderer.draw).
 */
private fun heroMeshPainter(skipCenter: Boolean = false): MeshGradientPainter =
    MeshGradientPainter(rows = 2, columns = 2, hasBicubicColor = true) {
        for (row in 0..2) {
            for (column in 0..2) {
                if (skipCenter && row == 1 && column == 1) continue
                // 가운데 꼭짓점만 살짝 오른쪽 위로 옮겨 곡면이 휘는 것을 보이게 한다
                val position = if (row == 1 && column == 1) Offset(0.62f, 0.42f) else Offset(column / 2f, row / 2f)
                setVertex(row, column, position = position, color = HeroColors[row * 3 + column])
            }
        }
    }

/** 왼쪽 두 꼭짓점은 빨강, 오른쪽 두 꼭짓점은 파랑인 1×1 메시 — 가로 그라데이션과 같은 색 배치 */
private fun redBlueMeshPainter(): MeshGradientPainter =
    MeshGradientPainter(rows = 1, columns = 1) {
        setVertex(0, 0, position = Offset(0f, 0f), color = Color.Red)
        setVertex(0, 1, position = Offset(1f, 0f), color = Color.Blue)
        setVertex(1, 0, position = Offset(0f, 1f), color = Color.Red)
        setVertex(1, 1, position = Offset(1f, 1f), color = Color.Blue)
    }

// ==================== 호환 모드(흰 Paint 소프트웨어 렌더) ====================

/**
 * drawVertices 만 가로채 Paint 색을 흰색으로 바꿔 그리는 소프트웨어 캔버스.
 * 꼭짓점 색에 Paint 색을 곱하는(MODULATE) 기기에서도 흰색(1.0)을 곱하므로 꼭짓점 색이 그대로 나온다.
 * Paint 색을 무시하는 기기에서는 결과가 달라지지 않는다.
 */
private class WhiteVertexPaintCanvas(bitmap: Bitmap) : android.graphics.Canvas(bitmap) {
    private val whitePaint = android.graphics.Paint()

    override fun drawVertices(
        mode: VertexMode,
        vertexCount: Int,
        verts: FloatArray,
        vertOffset: Int,
        texs: FloatArray?,
        texOffset: Int,
        colors: IntArray?,
        colorOffset: Int,
        indices: ShortArray?,
        indexOffset: Int,
        indexCount: Int,
        paint: android.graphics.Paint
    ) {
        whitePaint.set(paint)
        whitePaint.color = android.graphics.Color.WHITE
        super.drawVertices(
            mode, vertexCount, verts, vertOffset, texs, texOffset,
            colors, colorOffset, indices, indexOffset, indexCount, whitePaint
        )
    }
}

/** 화면 밖 ImageBitmap(소프트웨어 캔버스)에 그린다 — 픽셀을 직접 읽어 실측할 때 쓴다 */
private fun renderToBitmap(
    width: Int,
    height: Int,
    density: Density,
    whiteVertexPaint: Boolean,
    block: DrawScope.() -> Unit
): ImageBitmap {
    val bitmap = ImageBitmap(width, height)
    val androidBitmap = bitmap.asAndroidBitmap()
    val canvas = if (whiteVertexPaint) WhiteVertexPaintCanvas(androidBitmap) else android.graphics.Canvas(androidBitmap)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, ComposeCanvas(canvas), Size(width.toFloat(), height.toFloat())) {
        block()
    }
    return bitmap
}

private fun Painter.renderToBitmap(width: Int, height: Int, density: Density, whiteVertexPaint: Boolean): ImageBitmap =
    renderToBitmap(width, height, density, whiteVertexPaint) { draw(size) }

/**
 * 원래 Painter 를 매 draw 마다 WhiteVertexPaintCanvas 에 그린 뒤 이미지로 옮기는 호환 Painter.
 * 원래 Painter 의 블록은 여전히 이 onDraw 안에서 실행되므로 블록의 State 읽기도 draw 단계에서 그대로 관찰된다.
 * 대가는 하드웨어 대신 소프트웨어로 그린다는 점 — 애니메이션 카드에서 그리기 비용으로 비교할 수 있다.
 */
private class WhiteVertexPaintPainter(private val delegate: Painter) : Painter() {
    private var buffer: ImageBitmap? = null

    override val intrinsicSize: Size
        get() = delegate.intrinsicSize

    override fun DrawScope.onDraw() {
        val width = size.width.roundToInt()
        val height = size.height.roundToInt()
        if (width <= 0 || height <= 0) return
        val bitmap = buffer?.takeIf { it.width == width && it.height == height }
            ?: ImageBitmap(width, height).also { buffer = it }
        val androidBitmap = bitmap.asAndroidBitmap()
        androidBitmap.eraseColor(android.graphics.Color.TRANSPARENT)
        CanvasDrawScope().draw(this, layoutDirection, ComposeCanvas(WhiteVertexPaintCanvas(androidBitmap)), size) {
            with(delegate) { draw(size) }
        }
        drawImage(bitmap)
    }
}

/** compat 이면 호환 Painter 로 감싼다 */
@Composable
private fun rememberDisplayPainter(painter: MeshGradientPainter, compat: Boolean): Painter =
    remember(painter, compat) { if (compat) WhiteVertexPaintPainter(painter) else painter }

private data class ColorSupport(
    val blackPaintVertices: Color,
    val whitePaintVertices: Color,
    val meshDefault: Color,
    val meshWhitePaint: Color
) {
    /** 기본 경로의 메시가 검게 나오고, 흰 Paint 로는 색이 나오면 영향 받는 기기다 */
    val affected: Boolean
        get() = meshDefault.isNearlyBlack() && !meshWhitePaint.isNearlyBlack()
}

private fun Color.isNearlyBlack(): Boolean = red + green + blue < 0.05f

/** 플랫폼 drawVertices 를 직접 불러 왼쪽 빨강·오른쪽 파랑 사각형의 가운데 픽셀을 읽는다 */
private fun platformVerticesMidpoint(paintColor: Int): Color {
    val verts = floatArrayOf(0f, 0f, 64f, 0f, 0f, 16f, 64f, 0f, 64f, 16f, 0f, 16f)
    val red = android.graphics.Color.RED
    val blue = android.graphics.Color.BLUE
    // API 29 미만은 colors 길이를 vertexCount(= x·y 값 개수 12) 만큼 요구하므로 넉넉히 준다
    val colors = intArrayOf(red, blue, red, blue, blue, red, red, blue, red, blue, blue, red)
    val bitmap = createBitmap(64, 16)
    android.graphics.Canvas(bitmap).drawVertices(
        android.graphics.Canvas.VertexMode.TRIANGLES, 12, verts, 0, null, 0, colors, 0, null, 0, 0,
        android.graphics.Paint().apply { color = paintColor }
    )
    return Color(bitmap[32, 8])
}

private fun measureColorSupport(density: Density): ColorSupport {
    val mesh = redBlueMeshPainter()
    return ColorSupport(
        blackPaintVertices = platformVerticesMidpoint(android.graphics.Color.BLACK),
        whitePaintVertices = platformVerticesMidpoint(android.graphics.Color.WHITE),
        meshDefault = Color(mesh.renderToBitmap(64, 16, density, whiteVertexPaint = false).asAndroidBitmap()[32, 8]),
        meshWhitePaint = Color(mesh.renderToBitmap(64, 16, density, whiteVertexPaint = true).asAndroidBitmap()[32, 8])
    )
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard(compat: Boolean, affected: Boolean) {
    val painter = remember { heroMeshPainter() }
    val displayPainter = rememberDisplayPainter(painter, compat)

    SectionCard(
        title = "🌈 Mesh Gradient — 꼭짓점마다 색을 주는 2D 그라데이션",
        description = "선형·방사형 그라데이션은 한 축을 따라 색이 변하지만, 메시 그라데이션은 격자의 꼭짓점마다 색과 위치를 준다. " +
            "Compose 1.12 의 ui 모듈에 MeshGradientPainter 가 추가됐고, 꼭짓점은 블록 안의 MeshGradientScope.setVertex 로 채운다.",
        containerColor = Color(0xFFE8EAF6)
    ) {
        if (affected) {
            WarningBanner(
                if (compat) "이 기기는 기본 렌더가 검게 나와 호환 모드로 그리고 있다 — 바로 아래 카드 참고"
                else "호환 모드가 꺼져 있다 — 이 기기에서는 메시가 검게 보이는 것이 정상(버그 재현)"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Image(
            painter = displayPainter,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FeatureChip("MeshGradientPainter", Color(0xFF3949AB))
            FeatureChip("setVertex", Color(0xFF00897B))
        }

        Spacer(modifier = Modifier.height(12.dp))

        CodeBox(
            code = "val painter = MeshGradientPainter(rows = 2, columns = 2,\n" +
                "    hasBicubicColor = true) {\n" +
                "    // 패치 2×2 → 꼭짓점 3×3. 위치는 0~1 정규화 좌표\n" +
                "    setVertex(row = 1, column = 1,\n" +
                "        position = Offset(0.62f, 0.42f), color = Color.White)\n" +
                "    // … 나머지 8개 꼭짓점도 전부 지정해야 한다\n" +
                "}\n\n" +
                "Image(painter, null, Modifier.fillMaxWidth().height(160.dp))\n\n" +
                "// DrawScope 에서 직접 그릴 때\n" +
                "Canvas(modifier) { with(painter) { draw(size) } }"
        )

        Spacer(modifier = Modifier.height(12.dp))

        NoteBox(
            "제어점(left/top/right/bottom)은 절대 좌표가 아니라 꼭짓점 기준 상대 오프셋이다. 생략(Offset.Unspecified)하면 " +
                "이웃 꼭짓점 방향 × 거리 × 0.33 으로 추론해 채운다. opt-in 은 필요 없다. 바이트코드에는 MeshGradientConfig·" +
                "MeshGradientRenderer 도 public 으로 보이지만 Kotlin internal 이라 앱에서 쓰면 컴파일 오류가 난다 — 공개 진입점은 Painter 하나다."
        )
    }
}

// ==================== 2. 기기 호환성 ====================

@Composable
private fun CompatibilityCard(
    support: ColorSupport,
    compat: Boolean,
    onCompatChange: (Boolean) -> Unit
) {
    val rawPainter = remember { heroMeshPainter() }
    val compatPainter = remember(rawPainter) { WhiteVertexPaintPainter(rawPainter) }

    SectionCard(
        title = "🧪 이 기기에서 색이 제대로 나오는가",
        description = "1.12.1 렌더러는 색을 지정하지 않은 기본 Paint(검정)로 android.graphics.Canvas.drawVertices 를 부른다. " +
            "기기의 drawVertices 가 꼭짓점 색에 Paint 색을 곱하면(MODULATE) 메시 전체가 검게 나온다. " +
            "아래 값은 이 화면이 뜰 때 소프트웨어 캔버스에 직접 그려 가운데 픽셀을 읽은 실측이다.",
        containerColor = if (support.affected) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    ) {
        Text(
            text = "${Build.MANUFACTURER} ${Build.MODEL} · API ${Build.VERSION.SDK_INT}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )
        Spacer(modifier = Modifier.height(6.dp))
        SampleRow("drawVertices + 검정 Paint", support.blackPaintVertices)
        SampleRow("drawVertices + 흰 Paint", support.whitePaintVertices)
        SampleRow("MeshGradientPainter 기본 렌더", support.meshDefault)
        SampleRow("MeshGradientPainter + 흰 Paint 캔버스", support.meshWhitePaint)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (support.affected) "판정: 영향 받음 — 기본 렌더가 검정" else "판정: 영향 없음 — Paint 색과 무관하게 꼭짓점 색이 나온다",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (support.affected) Color(0xFFC62828) else Color(0xFF2E7D32)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PreviewPanel(label = "기본 렌더(화면)", modifier = Modifier.weight(1f)) {
                Image(
                    painter = rawPainter,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
            PreviewPanel(label = "호환 모드", modifier = Modifier.weight(1f)) {
                Image(
                    painter = compatPainter,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SwitchRow(label = "화면 전체에 호환 모드 적용", checked = compat, onCheckedChange = onCompatChange)

        Spacer(modifier = Modifier.height(8.dp))

        NoteBox(
            "호환 모드는 drawVertices 만 가로채 Paint 색을 흰색으로 바꾸는 소프트웨어 캔버스(android.graphics.Canvas 상속)에 그린 뒤 " +
                "이미지로 옮긴다. 흰색(1.0)을 곱하므로 꼭짓점 색이 그대로 나오고, Paint 색을 무시하는 기기에서는 결과가 같다. " +
                "대신 하드웨어가 아니라 CPU 로 래스터하므로 느리다 — 애니메이션 카드에서 켜고 끄며 비교할 수 있다."
        )
    }
}

// ==================== 3. 색 보간 공간 실측 ====================

private data class ColorSpaceSamples(
    val linearGradient: Color,
    val mesh: Color,
    val okLabLerp: Color
)

/**
 * 빨강→파랑을 두 방식으로 ImageBitmap 에 그리고 가운데 픽셀을 읽는다.
 * Brush 는 플랫폼 LinearGradient(sRGB 보간), 메시는 렌더러가 색을 OkLab 으로 바꿔 보간한다.
 */
private fun sampleMidpoints(density: Density, compat: Boolean): ColorSpaceSamples {
    val width = 256
    val height = 16

    val linear = renderToBitmap(width, height, density, whiteVertexPaint = false) {
        drawRect(Brush.horizontalGradient(listOf(Color.Red, Color.Blue)))
    }.asAndroidBitmap()[width / 2, height / 2]

    val mesh = redBlueMeshPainter().renderToBitmap(width, height, density, whiteVertexPaint = compat)
        .asAndroidBitmap()[width / 2, height / 2]

    // Compose 의 Color lerp 는 OkLab 에서 보간한다 — 메시가 OkLab 이라면 이 값과 같아야 한다
    return ColorSpaceSamples(Color(linear), Color(mesh), lerp(Color.Red, Color.Blue, 0.5f))
}

@Composable
private fun ColorSpaceCard(compat: Boolean) {
    val density = LocalDensity.current
    val meshPainter = remember { redBlueMeshPainter() }
    val displayPainter = rememberDisplayPainter(meshPainter, compat)
    var samples by remember { mutableStateOf<ColorSpaceSamples?>(null) }

    LaunchedEffect(density, compat) {
        samples = sampleMidpoints(density, compat)
    }

    SectionCard(
        title = "🎨 같은 두 색, 다른 보간 공간",
        description = "빨강→파랑을 Brush.horizontalGradient 와 1×1 메시로 각각 그렸다. 메시 렌더러는 꼭짓점 색을 OkLab 으로 바꿔 섞기 때문에 " +
            "가운데가 sRGB 보간보다 밝고 덜 탁하다. 아래 값은 ImageBitmap 에 그려 가운데 픽셀을 직접 읽은 실측이다."
    ) {
        StripLabel("Brush.horizontalGradient (sRGB)")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.horizontalGradient(listOf(Color.Red, Color.Blue)))
        )
        Spacer(modifier = Modifier.height(8.dp))
        StripLabel("MeshGradientPainter 1×1 (OkLab)")
        Image(
            painter = displayPainter,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        val measured = samples
        if (measured == null) {
            Text(text = "측정 중…", fontSize = 12.sp, color = Color.Gray)
        } else {
            SampleRow("선형 그라데이션 가운데 픽셀", measured.linearGradient)
            SampleRow("메시 가운데 픽셀${if (compat) " (호환 모드)" else ""}", measured.mesh)
            SampleRow("lerp(Red, Blue, 0.5f) — OkLab 기대값", measured.okLabLerp)

            Spacer(modifier = Modifier.height(8.dp))

            val meshGap = maxChannelGap(measured.mesh, measured.okLabLerp)
            val linearGap = maxChannelGap(measured.linearGradient, measured.okLabLerp)
            NoteBox(
                "OkLab 기대값과의 최대 채널 차이 — 메시 $meshGap / 선형 그라데이션 $linearGap (0~255). " +
                    "메시 쪽이 거의 0 이면 렌더러가 OkLab 보간을 한다는 뜻이다. 메시가 #000000 이면 호환성 카드의 문제다."
            )
        }
    }
}

private fun maxChannelGap(a: Color, b: Color): Int = listOf(
    abs(a.red - b.red), abs(a.green - b.green), abs(a.blue - b.blue)
).maxOf { (it * 255).roundToInt() }

@Composable
private fun SampleRow(label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(1.dp, Color(0xFFB0BEC5), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 12.sp, color = Color(0xFF424242), modifier = Modifier.weight(1f))
        Text(
            text = color.toHex(),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF1976D2)
        )
    }
}

private fun Color.toHex(): String = "#%06X".format(toArgb() and 0xFFFFFF)

// ==================== 4. 격자 크기 · 보간 대조 ====================

/** 결정적 의사난수(-1~1) — 슬라이더를 움직여도 같은 꼭짓점은 같은 방향으로만 흔들리게 한다 */
private fun pseudoRandom(row: Int, column: Int, salt: Int): Float {
    val hash = (row * 73856093) xor (column * 19349663) xor (salt * 83492791)
    return ((hash ushr 8) and 0xFFFF) / 65535f * 2f - 1f
}

private fun gridPosition(row: Int, column: Int, rows: Int, columns: Int, jitter: Float): Offset {
    // 바깥 테두리 꼭짓점은 테두리 위에 고정해야 사각형이 빈틈없이 채워진다
    val dx = if (column == 0 || column == columns) 0f else pseudoRandom(row, column, 0) * jitter / columns
    val dy = if (row == 0 || row == rows) 0f else pseudoRandom(row, column, 1) * jitter / rows
    return Offset(column / columns.toFloat() + dx, row / rows.toFloat() + dy)
}

private fun gridColor(row: Int, column: Int): Color = MeshPalette[(row * 3 + column * 5) % MeshPalette.size]

@Composable
private fun GridInterpolationCard(compat: Boolean) {
    var rows by remember { mutableIntStateOf(2) }
    var columns by remember { mutableIntStateOf(2) }
    var jitter by remember { mutableFloatStateOf(0.25f) }
    var showVertices by remember { mutableStateOf(true) }

    SectionCard(
        title = "🔲 격자 크기 · bilinear vs bicubic",
        description = "같은 꼭짓점 데이터를 hasBicubicColor 만 바꿔 나란히 그렸다. bilinear 는 패치마다 네 모서리 색만 보고 섞어 " +
            "패치 경계에서 색의 기울기가 꺾이고(격자 자국), bicubic(Catmull-Rom)은 이웃 패치의 꼭짓점까지 보고 이어 붙여 경계가 매끄럽다."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InterpolationPanel(
                label = "bilinear (기본값)",
                rows = rows,
                columns = columns,
                jitter = jitter,
                bicubic = false,
                showVertices = showVertices,
                compat = compat,
                modifier = Modifier.weight(1f)
            )
            InterpolationPanel(
                label = "bicubic",
                rows = rows,
                columns = columns,
                jitter = jitter,
                bicubic = true,
                showVertices = showVertices,
                compat = compat,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "패치 $rows×$columns = ${rows * columns}개 · 꼭짓점 ${rows + 1}×${columns + 1} = ${(rows + 1) * (columns + 1)}개",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LabeledSlider(
            label = "rows",
            valueText = "$rows",
            value = rows.toFloat(),
            valueRange = 1f..4f,
            steps = 2,
            onValueChange = { rows = it.roundToInt() }
        )
        LabeledSlider(
            label = "columns",
            valueText = "$columns",
            value = columns.toFloat(),
            valueRange = 1f..4f,
            steps = 2,
            onValueChange = { columns = it.roundToInt() }
        )
        LabeledSlider(
            label = "흔들기",
            valueText = "%.2f".format(jitter),
            value = jitter,
            valueRange = 0f..0.45f,
            onValueChange = { jitter = it }
        )
        SwitchRow(label = "꼭짓점 표시", checked = showVertices, onCheckedChange = { showVertices = it })

        Spacer(modifier = Modifier.height(8.dp))

        NoteBox(
            "모양(꼭짓점 위치·곡면)은 두 모드가 똑같고 색 보간만 다르다 — 렌더러는 위치에 베지어(Bernstein) 기저를, " +
                "색에는 bilinear 또는 Catmull-Rom 기저를 따로 쓴다. 기본값은 hasBicubicColor = false 다."
        )
    }
}

@Composable
private fun InterpolationPanel(
    label: String,
    rows: Int,
    columns: Int,
    jitter: Float,
    bicubic: Boolean,
    showVertices: Boolean,
    compat: Boolean,
    modifier: Modifier = Modifier
) {
    // rows·columns·hasBicubicColor 는 생성자 인자라 바뀌면 Painter 를 새로 만든다
    val painter = remember(rows, columns, jitter, bicubic) {
        MeshGradientPainter(rows = rows, columns = columns, hasBicubicColor = bicubic) {
            for (row in 0..rows) {
                for (column in 0..columns) {
                    setVertex(
                        row,
                        column,
                        position = gridPosition(row, column, rows, columns, jitter),
                        color = gridColor(row, column)
                    )
                }
            }
        }
    }
    val displayPainter = rememberDisplayPainter(painter, compat)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            with(displayPainter) { draw(size) }
            if (showVertices) {
                val positions = (0..rows).flatMap { row ->
                    (0..columns).map { column -> gridPosition(row, column, rows, columns, jitter) }
                }
                drawVertexDots(positions, selected = -1)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
    }
}

/** positions 는 0~1 정규화 좌표(행 우선) */
private fun DrawScope.drawVertexDots(positions: List<Offset>, selected: Int) {
    positions.forEachIndexed { index, position ->
        val center = Offset(position.x * size.width, position.y * size.height)
        val radius = if (index == selected) 9.dp.toPx() else 5.dp.toPx()
        drawCircle(color = Color.White, radius = radius, center = center)
        drawCircle(color = Color(0xFF263238), radius = radius, center = center, style = Stroke(width = 1.5.dp.toPx()))
    }
}

// ==================== 5. 꼭짓점 · 제어점 · 색 편집 ====================

private data class EditableVertex(
    val position: Offset,
    val color: Color,
    val manualHandles: Boolean = false,
    val angleDegrees: Float = 0f,
    val handleLength: Float = 0.17f
)

/** 한 꼭짓점의 네 제어점(꼭짓점 기준 상대 오프셋, 정규화 좌표) */
private data class VertexHandles(
    val left: Offset,
    val top: Offset,
    val right: Offset,
    val bottom: Offset
)

private fun defaultEditableVertices(): List<EditableVertex> = List((EditorRows + 1) * (EditorColumns + 1)) { index ->
    val row = index / (EditorColumns + 1)
    val column = index % (EditorColumns + 1)
    EditableVertex(
        position = Offset(column / EditorColumns.toFloat(), row / EditorRows.toFloat()),
        color = HeroColors[index]
    )
}

/** 회전 각도만큼 돌린 오른쪽·아래 제어점. 왼쪽·위는 그 반대 방향으로 둬서 곡면이 꼭짓점에서 꺾이지 않게 한다 */
private fun manualHandles(vertex: EditableVertex): VertexHandles {
    val radians = Math.toRadians(vertex.angleDegrees.toDouble())
    val cosine = cos(radians).toFloat()
    val sine = sin(radians).toFloat()
    val right = Offset(cosine, sine) * vertex.handleLength
    val bottom = Offset(-sine, cosine) * vertex.handleLength
    return VertexHandles(left = -right, top = -bottom, right = right, bottom = bottom)
}

private fun Offset.normalizedOrZero(): Offset {
    val length = getDistance()
    return if (length == 0f) Offset.Zero else this / length
}

/**
 * 제어점을 생략했을 때 렌더러가 채우는 값을 같은 규칙으로 계산한다(1.12.1 바이트코드에서 옮긴 규칙).
 * 가로 방향 = normalize(오른쪽 이웃 − 왼쪽 이웃) × 0.33, left = −방향 × |왼쪽 이웃 − P|, right = 방향 × |오른쪽 이웃 − P|.
 * 세로도 같다. 테두리처럼 이웃이 없으면 자기 자신을 이웃으로 쓴다.
 * 렌더러가 채운 실제 배열은 internal 이라 읽을 수 없으므로, 아래 "추론 규칙 검증" 으로 픽셀 단위 일치를 확인한다.
 */
private fun inferredHandles(positions: List<Offset>, index: Int): VertexHandles {
    val stride = EditorColumns + 1
    val row = index / stride
    val column = index % stride
    val center = positions[index]
    val left = if (column > 0) positions[index - 1] else center
    val right = if (column < EditorColumns) positions[index + 1] else center
    val top = if (row > 0) positions[index - stride] else center
    val bottom = if (row < EditorRows) positions[index + stride] else center
    val horizontal = (right - left).normalizedOrZero() * 0.33f
    val vertical = (bottom - top).normalizedOrZero() * 0.33f
    return VertexHandles(
        left = horizontal * -(left - center).getDistance(),
        top = vertical * -(top - center).getDistance(),
        right = horizontal * (right - center).getDistance(),
        bottom = vertical * (bottom - center).getDistance()
    )
}

private fun editorPainter(vertices: List<EditableVertex>, explicitInference: Boolean = false): MeshGradientPainter =
    MeshGradientPainter(rows = EditorRows, columns = EditorColumns, hasBicubicColor = true) {
        // Painter 는 onDraw 마다 이 블록을 다시 실행한다 — 스냅샷 리스트를 여기서 읽으면 draw 단계에서 관찰된다
        val positions = vertices.map { it.position }
        vertices.forEachIndexed { index, vertex ->
            val row = index / (EditorColumns + 1)
            val column = index % (EditorColumns + 1)
            val handles = when {
                vertex.manualHandles -> manualHandles(vertex)
                explicitInference -> inferredHandles(positions, index)
                else -> null
            }
            if (handles == null) {
                setVertex(row, column, position = vertex.position, color = vertex.color)
            } else {
                setVertex(
                    row,
                    column,
                    position = vertex.position,
                    color = vertex.color,
                    leftControlPoint = handles.left,
                    topControlPoint = handles.top,
                    rightControlPoint = handles.right,
                    bottomControlPoint = handles.bottom
                )
            }
        }
    }

/**
 * 제어점을 생략한 Painter 와, 같은 자리에 inferredHandles 계산값을 직접 넣은 Painter 를 각각 그려 픽셀을 비교한다.
 * 기기와 무관하게 비교가 성립하도록 항상 흰 Paint 캔버스로 그리고, 그림이 비어 있으면(색이 거의 한 가지) 판정하지 않는다
 * — 두 그림이 모두 검정이면 차이 0 이 나오는 거짓 통과를 막기 위해서다.
 */
private fun verifyInference(vertices: List<EditableVertex>, density: Density): String {
    val width = 240
    val height = 200
    val autoPixels = IntArray(width * height)
    val explicitPixels = IntArray(width * height)
    editorPainter(vertices).renderToBitmap(width, height, density, whiteVertexPaint = true).readPixels(autoPixels)
    editorPainter(vertices, explicitInference = true).renderToBitmap(width, height, density, whiteVertexPaint = true)
        .readPixels(explicitPixels)
    val distinctColors = autoPixels.toSet().size
    if (distinctColors < 16) return "판정 불가 — 그림에 색이 ${distinctColors}가지뿐(빈 그림)"
    var maxGap = 0
    for (i in autoPixels.indices) {
        val a = autoPixels[i]
        val b = explicitPixels[i]
        for (shift in intArrayOf(0, 8, 16, 24)) {
            maxGap = max(maxGap, abs(((a ushr shift) and 0xFF) - ((b ushr shift) and 0xFF)))
        }
    }
    return "자동(생략) vs 계산값 직접 지정 — 240×200 픽셀(색 ${distinctColors}가지) 최대 채널 차이 $maxGap" +
        if (maxGap == 0) " → 일치" else " → 불일치"
}

private fun nearestVertex(vertices: List<EditableVertex>, touch: Offset, canvasSize: IntSize): Int? {
    val touchRadius = canvasSize.width * 0.12f
    fun distanceTo(index: Int): Float {
        val position = vertices[index].position
        return (Offset(position.x * canvasSize.width, position.y * canvasSize.height) - touch).getDistance()
    }
    return vertices.indices.minByOrNull { distanceTo(it) }?.takeIf { distanceTo(it) <= touchRadius }
}

@Composable
private fun VertexEditorCard(compat: Boolean) {
    val density = LocalDensity.current
    val vertices = remember { mutableStateListOf<EditableVertex>().apply { addAll(defaultEditableVertices()) } }
    var selected by remember { mutableIntStateOf(4) }
    var verification by remember { mutableStateOf<String?>(null) }
    val painter = remember { editorPainter(vertices) }
    val displayPainter = rememberDisplayPainter(painter, compat)

    val current = vertices[selected]
    val selectedRow = selected / (EditorColumns + 1)
    val selectedColumn = selected % (EditorColumns + 1)

    SectionCard(
        title = "✋ 꼭짓점 · 제어점 · 색 편집",
        description = "꼭짓점을 탭해 고르고 드래그해 옮긴다. 흰 선은 선택한 꼭짓점의 네 제어점이다 — 직접 지정 모드는 회전·길이로 만든 값, " +
            "자동 모드는 setVertex 에서 생략했을 때 렌더러가 채우는 값을 같은 규칙으로 계산해 그린 것이다."
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFECEFF1))
                .pointerInput(Unit) {
                    detectTapGestures { tap ->
                        nearestVertex(vertices, tap, size)?.let { selected = it }
                    }
                }
                .pointerInput(Unit) {
                    // 꼭짓점 근처에서 시작한 드래그만 꼭짓점을 옮긴다
                    var dragging = false
                    detectDragGestures(
                        onDragStart = { start ->
                            val hit = nearestVertex(vertices, start, size)
                            dragging = hit != null
                            hit?.let { selected = it }
                        },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (dragging) {
                                val vertex = vertices[selected]
                                val moved = Offset(
                                    (vertex.position.x + dragAmount.x / size.width).coerceIn(0f, 1f),
                                    (vertex.position.y + dragAmount.y / size.height).coerceIn(0f, 1f)
                                )
                                vertices[selected] = vertex.copy(position = moved)
                            }
                        }
                    )
                }
        ) {
            with(displayPainter) { draw(size) }
            val positions = vertices.map { it.position }
            val handles = if (vertices[selected].manualHandles) {
                manualHandles(vertices[selected])
            } else {
                inferredHandles(positions, selected)
            }
            drawHandles(positions[selected], handles)
            drawVertexDots(positions, selected)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "선택: ($selectedRow, $selectedColumn)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MeshPalette.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color == current.color) 3.dp else 1.dp,
                            color = if (color == current.color) Color(0xFF263238) else Color(0xFFB0BEC5),
                            shape = CircleShape
                        )
                        .clickable { vertices[selected] = current.copy(color = color) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !current.manualHandles,
                onClick = { vertices[selected] = current.copy(manualHandles = false) },
                label = { Text("제어점 자동", fontSize = 12.sp) }
            )
            FilterChip(
                selected = current.manualHandles,
                onClick = { vertices[selected] = current.copy(manualHandles = true) },
                label = { Text("직접 지정", fontSize = 12.sp) }
            )
        }

        if (current.manualHandles) {
            LabeledSlider(
                label = "회전",
                valueText = "${current.angleDegrees.roundToInt()}°",
                value = current.angleDegrees,
                valueRange = -90f..90f,
                onValueChange = { vertices[selected] = current.copy(angleDegrees = it) }
            )
            LabeledSlider(
                label = "길이",
                valueText = "%.2f".format(current.handleLength),
                value = current.handleLength,
                valueRange = 0f..0.4f,
                onValueChange = { vertices[selected] = current.copy(handleLength = it) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CodeBox(code = setVertexCode(selectedRow, selectedColumn, current))

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    vertices.clear()
                    vertices.addAll(defaultEditableVertices())
                    selected = 4
                    verification = null
                }
            ) {
                Text("초기화", fontSize = 12.sp)
            }
            OutlinedButton(onClick = { verification = verifyInference(vertices.toList(), density) }) {
                Text("추론 규칙 검증", fontSize = 12.sp)
            }
        }

        verification?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = it, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF2E7D32))
        }

        Spacer(modifier = Modifier.height(8.dp))

        NoteBox(
            "가운데 꼭짓점을 '직접 지정' 으로 두고 회전을 주면 네 패치가 소용돌이처럼 비틀린다. 제어점 길이 0 은 그 꼭짓점에서 곡면을 " +
                "뾰족하게 만든다. 테두리 꼭짓점을 안쪽으로 끌면 메시 밖은 칠해지지 않는다(배경이 드러남)."
        )
    }
}

private fun DrawScope.drawHandles(position: Offset, handles: VertexHandles) {
    val center = Offset(position.x * size.width, position.y * size.height)
    listOf(handles.left, handles.top, handles.right, handles.bottom).forEach { handle ->
        val tip = center + Offset(handle.x * size.width, handle.y * size.height)
        drawLine(color = Color.White, start = center, end = tip, strokeWidth = 2.dp.toPx())
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = tip)
        drawCircle(color = Color(0xFF263238), radius = 4.dp.toPx(), center = tip, style = Stroke(width = 1.dp.toPx()))
    }
}

private fun setVertexCode(row: Int, column: Int, vertex: EditableVertex): String {
    fun Offset.code() = "Offset(%.2ff, %.2ff)".format(x, y)
    val header = "setVertex($row, $column,\n" +
        "    position = ${vertex.position.code()},\n" +
        "    color = Color(0xFF${vertex.color.toHex().drop(1)})"
    if (!vertex.manualHandles) {
        return "$header\n    // 제어점 생략 → Offset.Unspecified → 이웃 방향 × 0.33 으로 추론\n)"
    }
    val handles = manualHandles(vertex)
    return "$header,\n" +
        "    leftControlPoint = ${handles.left.code()},\n" +
        "    topControlPoint = ${handles.top.code()},\n" +
        "    rightControlPoint = ${handles.right.code()},\n" +
        "    bottomControlPoint = ${handles.bottom.code()}\n)"
}

// ==================== 6. 애니메이션 · 그리기 비용 ====================

/** draw 람다와 1초 주기 표시 코루틴이 같은 메인 스레드에서만 만지는 계측 값 — State 가 아니어야 읽어도 리컴포지션이 안 생긴다 */
private class MeshDrawStats {
    var draws = 0
    var drawNanos = 0L
    var recompositions = 0

    fun record(nanos: Long) {
        draws++
        drawNanos += nanos
    }

    fun drain(): Pair<Int, Long> {
        val result = draws to drawNanos
        draws = 0
        drawNanos = 0L
        return result
    }
}

private fun auroraPosition(row: Int, column: Int, gridSize: Int, time: Float): Offset {
    val phase = row * 1.7f + column * 2.3f
    val amplitude = 0.3f / gridSize
    // 모서리는 고정, 테두리는 테두리를 따라서만, 안쪽은 두 축 모두 움직인다
    val dx = if (column == 0 || column == gridSize) 0f else sin(time * 1.3f + phase) * amplitude
    val dy = if (row == 0 || row == gridSize) 0f else cos(time * 1.1f + phase * 1.3f) * amplitude
    return Offset(column / gridSize.toFloat() + dx, row / gridSize.toFloat() + dy)
}

private fun auroraColor(row: Int, column: Int): Color = AuroraColors[(row * 2 + column * 3) % AuroraColors.size]

@Composable
private fun AnimatedMeshCard(compat: Boolean) {
    var running by remember { mutableStateOf(true) }
    var bicubic by remember { mutableStateOf(true) }
    var gridSize by remember { mutableIntStateOf(3) }
    val time = remember { mutableFloatStateOf(0f) }
    val stats = remember { MeshDrawStats() }

    // 프레임마다 시간만 올린다. 이 값은 Painter 블록(= draw 단계)에서만 읽힌다
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                time.floatValue += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }

    SectionCard(
        title = "🌌 애니메이션 · 그리기 비용 실측",
        description = "안쪽 꼭짓점을 매 프레임 움직인다. 시간 값은 컴포저블 본문이 아니라 Painter 블록(onDraw 안에서 실행)에서만 읽으므로 " +
            "프레임마다 draw 단계만 다시 돈다. 아래 숫자는 1초마다 갱신되는 실측값이다${if (compat) "(지금은 호환 모드)" else ""}."
    ) {
        AnimatedMeshCanvas(gridSize = gridSize, bicubic = bicubic, compat = compat, time = time, stats = stats)

        Spacer(modifier = Modifier.height(8.dp))

        MeshStatsLine(stats = stats)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(2, 3, 5).forEach { size ->
                FilterChip(
                    selected = gridSize == size,
                    onClick = { gridSize = size },
                    label = { Text("$size×$size", fontSize = 12.sp) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { running = !running },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text(if (running) "정지" else "재생", fontSize = 12.sp)
            }
        }
        SwitchRow(label = "hasBicubicColor", checked = bicubic, onCheckedChange = { bicubic = it })

        Spacer(modifier = Modifier.height(8.dp))

        NoteBox(
            "캔버스 리컴포지션 누적은 격자·보간·호환 모드를 바꿀 때만 오르고 재생 중에는 그대로여야 한다. 그리기 시간은 블록 실행·패치 분할·" +
                "색 계산을 거쳐 drawVertices 가 기록될 때까지의 CPU 비용이다(기본 렌더는 GPU 래스터 시간 제외, 호환 모드는 CPU 래스터까지 포함). " +
                "렌더러는 패치 하나를 화면에서 약 8px 간격으로 4~64등분하므로, 격자를 늘려도 삼각형 총량이 비례해 늘지는 않는다."
        )
    }
}

@Composable
private fun AnimatedMeshCanvas(
    gridSize: Int,
    bicubic: Boolean,
    compat: Boolean,
    time: FloatState,
    stats: MeshDrawStats
) {
    val painter = remember(gridSize, bicubic) {
        MeshGradientPainter(rows = gridSize, columns = gridSize, hasBicubicColor = bicubic) {
            val t = time.floatValue // onDraw 안에서 실행되므로 draw 단계 읽기
            for (row in 0..gridSize) {
                for (column in 0..gridSize) {
                    setVertex(
                        row,
                        column,
                        position = auroraPosition(row, column, gridSize, t),
                        color = auroraColor(row, column)
                    )
                }
            }
        }
    }
    val displayPainter = rememberDisplayPainter(painter, compat)

    // 이 함수가 다시 컴포즈될 때만 증가한다
    SideEffect { stats.recompositions++ }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        val start = System.nanoTime()
        with(displayPainter) { draw(size) }
        stats.record(System.nanoTime() - start)
    }
}

@Composable
private fun MeshStatsLine(stats: MeshDrawStats) {
    var line by remember { mutableStateOf("측정 중…") }

    LaunchedEffect(stats) {
        while (true) {
            delay(1_000)
            val (draws, nanos) = stats.drain()
            val averageMicros = if (draws == 0) 0L else nanos / draws / 1_000
            line = "지난 1초 그리기 ${draws}회 · 평균 ${averageMicros}µs/회 · 캔버스 리컴포지션 누적 ${stats.recompositions}회"
        }
    }

    Text(
        text = line,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F)
    )
}

// ==================== 7. 함정 ====================

// rows = 0 은 생성자 예외를 보여주려고 일부러 넣은 값이다(@IntRange(from = 1) 위반을 lint 가 오류로 잡는다)
@SuppressLint("Range")
@Composable
private fun PitfallCard(compat: Boolean) {
    val density = LocalDensity.current
    val fullPainter = remember { heroMeshPainter() }
    val missingPainter = remember { heroMeshPainter(skipCenter = true) }
    val fullDisplay = rememberDisplayPainter(fullPainter, compat)
    val missingDisplay = rememberDisplayPainter(missingPainter, compat)
    var measuredSize by remember { mutableStateOf<IntSize?>(null) }

    // 잘못된 인자가 실제로 언제·어떤 예외를 던지는지 그대로 잡아 보여준다
    val rowsError = remember { runCatching { MeshGradientPainter(rows = 0, columns = 2) {} }.exceptionOrNull() }
    val rangePainterCreated = remember {
        runCatching { MeshGradientPainter(rows = 1, columns = 1) { setVertex(2, 0, position = Offset.Zero, color = Color.Red) } }.isSuccess
    }
    val rangeDrawError = remember(density) {
        runCatching {
            MeshGradientPainter(rows = 1, columns = 1) {
                setVertex(2, 0, position = Offset.Zero, color = Color.Red)
            }.renderToBitmap(8, 8, density, whiteVertexPaint = false)
        }.exceptionOrNull()
    }

    SectionCard(
        title = "⚠️ 함정",
        description = "빈칸이 있어도 컴파일은 되고 예외도 없이 그려지는 경우가 있다. 아래는 모두 이 화면에서 실제로 실행한 결과다.",
        containerColor = Color(0xFFFFF8E1)
    ) {
        PitfallTitle("1. 꼭짓점을 하나라도 빼먹으면")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PreviewPanel(label = "9개 모두 지정", modifier = Modifier.weight(1f)) {
                Image(
                    painter = fullDisplay,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
            PreviewPanel(label = "(1, 1) 누락", modifier = Modifier.weight(1f)) {
                Image(
                    painter = missingDisplay,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        PitfallBody(
            "그릴 때마다 위치를 0, 색을 Transparent 로 초기화한 뒤 블록을 실행한다. 빼먹은 꼭짓점은 " +
                "왼쪽 위 (0, 0) 의 투명 점이 되어 주변 네 패치가 그쪽으로 끌려간다 — 예외도 경고도 없다."
        )

        Spacer(modifier = Modifier.height(12.dp))

        PitfallTitle("2. Painter 에는 고유 크기가 없다")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFECEFF1), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            // 크기 modifier 없이 Image 에 넣으면 어떻게 측정되는지 그대로 잰다
            Image(
                painter = fullPainter,
                contentDescription = null,
                modifier = Modifier.onSizeChanged { measuredSize = it }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        PitfallBody(
            "intrinsicSize.isUnspecified = ${fullPainter.intrinsicSize.isUnspecified} · " +
                "크기 없이 넣은 Image 의 측정 크기 = ${measuredSize?.let { "${it.width}×${it.height}px" } ?: "측정 전"}. " +
                "Image/paint 에는 반드시 크기(fillMaxWidth + height, size, aspectRatio 등)를 줘야 보인다."
        )

        Spacer(modifier = Modifier.height(12.dp))

        PitfallTitle("3. 잘못된 인자 — 생성 시점 vs 그리는 시점")
        CodeBox(
            code = "MeshGradientPainter(rows = 0, columns = 2) {}\n→ ${rowsError.describe()}\n\n" +
                "MeshGradientPainter(1, 1) { setVertex(2, 0, …) }\n" +
                "→ 생성: ${if (rangePainterCreated) "성공(예외 없음)" else "실패"}\n" +
                "→ 그리기: ${rangeDrawError.describe()}"
        )
        Spacer(modifier = Modifier.height(6.dp))
        PitfallBody(
            "rows·columns 는 패치 수라 1 이상이고 생성자에서 바로 검사한다. 반면 setVertex 의 row(0..rows)·column(0..columns) 범위는 " +
                "블록이 실행되는 그리는 시점에야 검사되므로, 화면에 붙인 Painter 라면 draw 단계에서 앱이 죽는다."
        )

        Spacer(modifier = Modifier.height(12.dp))

        PitfallTitle("4. API 29 미만 + 하드웨어 가속")
        PitfallBody(
            "하드웨어 가속 지원표에서 drawVertices 는 API 29 부터다. 렌더러에 API 29 미만용 colors 버퍼 분기는 있지만, " +
                "minSdk 24 앱이라면 API 24~28 기기에서 화면에 제대로 그려지는지 따로 확인해야 한다. " +
                "이 기기: API ${Build.VERSION.SDK_INT} → ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "지원 범위" else "확인 필요"}."
        )
    }
}

private fun Throwable?.describe(): String =
    if (this == null) "예외 없음" else "${this::class.simpleName}: $message"

@Composable
private fun PreviewPanel(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFECEFF1))
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
    }
}

@Composable
private fun PitfallTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFE65100),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun PitfallBody(text: String) {
    Text(text = text, fontSize = 12.sp, color = Color(0xFF5D4037), lineHeight = 17.sp)
}

// ==================== 공통 UI ====================

@Composable
private fun SectionCard(
    title: String,
    description: String,
    containerColor: Color = Color(0xFFFAFAFA),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF616161)
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun WarningBanner(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFEBEE)
    ) {
        Text(
            text = "⚠️ $text",
            modifier = Modifier.padding(10.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFC62828)
        )
    }
}

@Composable
private fun StripLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF616161),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(64.dp),
            fontSize = 12.sp,
            color = Color(0xFF424242)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1976D2),
                activeTrackColor = Color(0xFF1976D2)
            )
        )
        Text(
            text = valueText,
            modifier = Modifier.width(44.dp),
            fontSize = 12.sp,
            color = Color(0xFF1976D2),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF424242), modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FeatureChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NoteBox(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1976D2).copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(10.dp),
            fontSize = 12.sp,
            color = Color(0xFF37474F),
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun CodeBox(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF263238)
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 16.sp
        )
    }
}
