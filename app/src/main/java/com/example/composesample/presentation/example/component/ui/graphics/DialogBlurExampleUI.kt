package com.example.composesample.presentation.example.component.ui.graphics

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch

/**
 * 다이얼로그 배경 블러 예제
 * - "블러"라는 한 단어가 서로 다른 세 가지를 가리킨다 — **내 컴포저블 안**(`Modifier.blur`),
 *   **내 윈도우의 배경**(`Window.setBackgroundBlurRadius`), **내 윈도우 뒤의 다른 윈도우**
 *   (`LayoutParams.blurBehindRadius` + `FLAG_BLUR_BEHIND`). 셋은 흐려지는 대상이 다르다.
 * - 뒤 두 개는 **API 31+ 전용이고, 그마저도 기기가 꺼 둘 수 있다**(`isCrossWindowBlurEnabled`).
 *   이 화면은 그 값을 실시간으로 읽어 보여주고, 꺼졌을 때 코드가 성공해도 화면이 그대로인 상황을 드러낸다.
 * - minSdk 24 인 프로젝트이므로 하위 폴백도 함께 만든다 — `GraphicsLayer` 로 배경을 캡처해
 *   소프트웨어 블러를 돌리고, 그 비용(ms)을 화면이 직접 잰다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "Dialog Background Blur" 섹션 참고.
 */

private const val BLUR_MAX_RADIUS = 80

// ==================== 소프트웨어 블러(하위 폴백) ====================

/**
 * 다운스케일 + 박스 블러 2패스.
 *
 * RenderScript 는 API 31 에서 폐기됐고 `Modifier.blur` 도 31+ 라, 그 아래에서 쓸 수 있는 것은
 * 결국 직접 계산하는 방법뿐이다. 반지름을 키우는 대신 **먼저 줄이는 것**이 비용을 좌우한다.
 */
private fun softwareBlur(source: Bitmap, downscale: Int, radius: Int): Bitmap {
    val w = (source.width / downscale).coerceAtLeast(1)
    val h = (source.height / downscale).coerceAtLeast(1)
    val small = Bitmap.createScaledBitmap(source, w, h, true)
    val pixels = IntArray(w * h)
    small.getPixels(pixels, 0, w, 0, 0, w, h)

    val r = radius.coerceIn(1, 25)
    val buffer = IntArray(w * h)

    // 가로 패스 → 세로 패스. 박스 블러를 두 번 지나가면 가우시안에 가까워진다.
    boxBlurPass(pixels, buffer, w, h, r, horizontal = true)
    boxBlurPass(buffer, pixels, w, h, r, horizontal = false)

    small.setPixels(pixels, 0, w, 0, 0, w, h)
    return small
}

private fun boxBlurPass(
    input: IntArray,
    output: IntArray,
    width: Int,
    height: Int,
    radius: Int,
    horizontal: Boolean
) {
    val outer = if (horizontal) height else width
    val inner = if (horizontal) width else height

    for (o in 0 until outer) {
        for (i in 0 until inner) {
            var a = 0
            var red = 0
            var green = 0
            var blue = 0
            var count = 0
            for (k in -radius..radius) {
                val p = i + k
                if (p < 0 || p >= inner) continue
                val index = if (horizontal) o * width + p else p * width + o
                val color = input[index]
                a += (color ushr 24) and 0xFF
                red += (color ushr 16) and 0xFF
                green += (color ushr 8) and 0xFF
                blue += color and 0xFF
                count++
            }
            val index = if (horizontal) o * width + i else i * width + o
            output[index] = ((a / count) shl 24) or
                ((red / count) shl 16) or
                ((green / count) shl 8) or
                (blue / count)
        }
    }
}

// ==================== 윈도우 블러 적용 ====================

/** 다이얼로그 윈도우에 세 값을 한 번에 반영한다. API 31 미만에서는 dim 만 걸린다. */
private fun Window.applyBlur(behindRadius: Int, backgroundRadius: Int, dim: Float) {
    val params = attributes
    params.dimAmount = dim
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        params.blurBehindRadius = behindRadius
    }
    attributes = params

    addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (behindRadius > 0) {
            addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        } else {
            clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
        setBackgroundBlurRadius(backgroundRadius)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun WindowManager.crossWindowBlurEnabled(): Boolean = isCrossWindowBlurEnabled

// ==================== 화면 ====================

@Composable
fun DialogBlurExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "다이얼로그 배경 블러",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { SupportCard() }
            item { WindowBlurCard() }
            item { FallbackCard() }
            item { ComparisonCard() }
            item { PitfallCard() }
        }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun ConceptCard() {
    SectionCard(title = "1. '블러'는 세 가지를 가리킨다") {
        BodyText(
            "같은 단어를 쓰지만 흐려지는 대상이 다르다. 다이얼로그 뒤가 흐려지길 원했는데 " +
                "`Modifier.blur` 를 걸어 놓고 왜 안 되냐고 묻는 일이 여기서 생긴다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("API", "흐려지는 대상 / 최소 버전", isHeader = true)
        TableRow("Modifier.blur", "그 컴포저블이 그린 내용 / API 31+(하위는 조용히 무시)")
        TableRow("setBackgroundBlurRadius", "내 윈도우의 배경 영역 / API 31+")
        TableRow("blurBehindRadius", "내 윈도우 **뒤의 다른 윈도우** / API 31+ & FLAG_BLUR_BEHIND 필요")
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "다이얼로그에서 \"뒤 화면이 흐려지는\" 효과는 세 번째다. 앞의 둘은 다이얼로그 창 **안쪽**에서 끝난다. " +
                "그리고 셋 다 API 31 이 하한이라, minSdk 24 인 프로젝트에서는 폴백이 따로 필요하다."
        )
    }
}

// ==================== 2. 지원 여부 실측 ====================

@Composable
private fun SupportCard() {
    val context = LocalContext.current
    val windowManager = remember(context) {
        context.getSystemService(WindowManager::class.java)
    }
    var crossWindowEnabled by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowManager.crossWindowBlurEnabled()
        )
    }

    // 값이 고정이 아니다 — 배터리 세이버나 개발자 옵션으로 런타임에 꺼진다. 그래서 구독한다.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        DisposableEffect(windowManager) {
            val listener = java.util.function.Consumer<Boolean> { enabled ->
                crossWindowEnabled = enabled
            }
            windowManager.addCrossWindowBlurEnabledListener(listener)
            onDispose { windowManager.removeCrossWindowBlurEnabledListener(listener) }
        }
    }

    SectionCard(title = "2. 이 기기는 지금 블러를 켜 두고 있나") {
        BodyText(
            "API 31 이상이어도 끝이 아니다. 시스템이 크로스 윈도우 블러를 끄면 값을 넣어도 화면은 그대로다 — " +
                "**코드는 성공하고 그림만 안 나온다.** 그래서 적용 전에 이 값을 읽어야 한다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        ResultRow("SDK_INT", "${Build.VERSION.SDK_INT} (블러 API 하한 31)")
        ResultRow(
            "블러 API",
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "사용 가능" else "사용 불가 — 폴백 필요"
        )
        ResultRow(
            "크로스 윈도우",
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> "해당 없음"
                crossWindowEnabled -> "켜짐"
                else -> "꺼짐 — 값을 넣어도 안 보인다"
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "배터리 세이버를 켜거나 개발자 옵션의 창 블러를 끄면 이 줄이 바뀐다. " +
                "리스너(addCrossWindowBlurEnabledListener)로 구독하고 있어 화면을 나갔다 들어올 필요가 없다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("실측", "SM-A725F / Android 13(API 33) 계측 결과", isHeader = true)
        TableRow("SDK", "33 — 블러 API 는 사용 가능한 버전")
        TableRow("시스템", "isCrossWindowBlurEnabled = **false**")
        TableRow("적용", "blurBehindRadius=40 · dim=0.4 · 두 플래그 모두 set 성공")
        CaptionText(
            "값도 플래그도 전부 들어갔는데 시스템 스위치가 꺼져 있다 — **코드는 성공하고 그림만 없는** 바로 그 상황이다. " +
                "API 31+ 라는 조건만 보고 분기했다면 이 기기에서 원인을 찾느라 한참 걸렸을 것이다."
        )
    }
}

// ==================== 3. 윈도우 블러 ====================

@Composable
private fun WindowBlurCard() {
    var showDialog by remember { mutableStateOf(false) }
    var behindRadius by remember { mutableIntStateOf(40) }
    var backgroundRadius by remember { mutableIntStateOf(20) }
    var dim by remember { mutableFloatStateOf(0.3f) }

    SectionCard(title = "3. 다이얼로그 윈도우에 직접 걸기") {
        BodyText(
            "Compose 의 `Dialog` 는 자체 윈도우를 쓴다. 그 윈도우 객체는 " +
                "`LocalView.current.parent` 를 `DialogWindowProvider` 로 캐스팅해 얻는다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "val window = (LocalView.current.parent as? DialogWindowProvider)?.window\n" +
                "window?.attributes = window.attributes.apply {\n" +
                "    blurBehindRadius = 40      // 뒤 윈도우 (API 31+)\n" +
                "    dimAmount = 0.3f\n" +
                "}\n" +
                "window?.addFlags(FLAG_BLUR_BEHIND or FLAG_DIM_BEHIND)\n" +
                "window?.setBackgroundBlurRadius(20)   // 내 윈도우 배경 (API 31+)"
        )
        Spacer(modifier = Modifier.height(10.dp))

        SliderRow("뒤 블러", behindRadius, BLUR_MAX_RADIUS) { behindRadius = it }
        SliderRow("배경 블러", backgroundRadius, BLUR_MAX_RADIUS) { backgroundRadius = it }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "어둡게",
                modifier = Modifier.width(64.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
            Slider(
                value = dim,
                onValueChange = { dim = it },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "%.2f".format(dim),
                modifier = Modifier.width(40.dp),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF616161)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        DemoButton("다이얼로그 열기", Color(0xFF3949AB)) { showDialog = true }

        if (showDialog) {
            BlurDialog(
                behindRadius = behindRadius,
                backgroundRadius = backgroundRadius,
                dim = dim,
                onDismiss = { showDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "두 값을 따로 움직여 보면 차이가 분명해진다 — **뒤 블러**는 다이얼로그 밖 화면 전체가 흐려지고, " +
                "**배경 블러**는 다이얼로그 창이 덮은 영역 안에서만 일어난다. 어둡게(dim)는 블러와 별개로 겹쳐 적용된다."
        )
    }
}

@Composable
private fun BlurDialog(
    behindRadius: Int,
    backgroundRadius: Int,
    dim: Float,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val view = LocalView.current
        val window = (view.parent as? DialogWindowProvider)?.window

        DisposableEffect(window, behindRadius, backgroundRadius, dim) {
            window?.apply {
                // 배경 블러는 윈도우 배경이 반투명해야 보인다 — 완전 투명이면 블러를 얹을 바탕이 없다.
                setBackgroundDrawable(ColorDrawable(AndroidColor.argb(70, 255, 255, 255)))
                applyBlur(behindRadius, backgroundRadius, dim)
            }
            onDispose { }
        }

        Card(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xE6FFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "블러 적용됨",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ResultRow("뒤 블러", "$behindRadius px")
                ResultRow("배경 블러", "$backgroundRadius px")
                ResultRow("dim", "%.2f".format(dim))
                Spacer(modifier = Modifier.height(6.dp))
                CaptionText(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        "이 기기는 블러 API 를 지원한다. 시스템이 꺼 뒀다면 숫자만 남고 그림은 없다."
                    } else {
                        "이 기기는 API 31 미만이라 dim 만 적용된다."
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                DemoButton("닫기", Color(0xFF546E7A)) { onDismiss() }
            }
        }
    }
}

// ==================== 4. 하위 폴백 ====================

@Composable
private fun FallbackCard() {
    val scope = rememberCoroutineScope()
    val layer = rememberGraphicsLayer()
    var blurred by remember { mutableStateOf<ImageBitmap?>(null) }
    var elapsedMs by remember { mutableStateOf<String?>(null) }
    var sourceSize by remember { mutableStateOf<String?>(null) }
    var captureError by remember { mutableStateOf<String?>(null) }
    var downscale by remember { mutableIntStateOf(8) }

    SectionCard(title = "4. API 31 미만에서는 직접 만든다") {
        BodyText(
            "하위 버전에는 블러 API 가 없다(RenderScript 도 31 에서 폐기됐다). 남은 길은 " +
                "**뒤에 있을 화면을 캡처해서 흐리게 만든 그림을 대신 그리는 것**이다. " +
                "아래 상자를 캡처해 그 과정을 그대로 재현한다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 캡처 대상: drawWithContent 로 레이어에 기록해 두면 나중에 비트맵으로 꺼낼 수 있다.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .drawWithContent {
                    // 레이어에 기록해 두면 나중에 toImageBitmap() 으로 꺼낼 수 있다.
                    layer.record { this@drawWithContent.drawContent() }
                    drawLayer(layer)
                }
                .background(Color(0xFF1A237E), RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "캡처 대상", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "이 영역이 다이얼로그 뒤에 있다고 치자. 캡처 → 축소 → 박스 블러 2패스.",
                    fontSize = 11.sp,
                    color = Color(0xFFC5CAE9),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        SliderRow("축소 배율", downscale, 16, min = 1) { downscale = it }
        DemoButton("캡처 + 블러", Color(0xFF00897B)) {
            // 아직 한 번도 그려지지 않은 레이어를 캡처하면 toImageBitmap() 이
            // IllegalArgumentException("width & height must be > 0") 으로 터진다. 먼저 확인한다.
            val recorded = layer.size
            if (recorded.width == 0 || recorded.height == 0) {
                captureError = "레이어가 아직 그려지지 않았다(size=${recorded.width}x${recorded.height})"
                blurred = null
                return@DemoButton
            }
            captureError = null
            scope.launch {
                val started = System.nanoTime()
                val captured = layer.toImageBitmap()
                val android = captured.asAndroidBitmap()
                sourceSize = "${android.width}x${android.height}"
                val result = softwareBlur(android, downscale.coerceAtLeast(1), radius = 6)
                elapsedMs = "%.1f".format((System.nanoTime() - started) / 1_000_000.0)
                blurred = result.asImageBitmap()
            }
        }

        captureError?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            ResultRow("캡처 불가", message)
            CaptionText(
                "화면이 보이지 않는 상태(잠금 화면 뒤 등)에서는 draw 패스가 돌지 않아 레이어가 비어 있다. " +
                    "이 상태로 toImageBitmap() 을 부르면 예외가 난다 — 계측 테스트에서 실제로 확인한 실패다."
            )
        }

        blurred?.let { image ->
            Spacer(modifier = Modifier.height(10.dp))
            ResultRow("원본 크기", sourceSize ?: "-")
            ResultRow("축소 후", "${image.width}x${image.height}")
            ResultRow("소요", "${elapsedMs}ms")
            Spacer(modifier = Modifier.height(6.dp))
            Image(
                bitmap = image,
                contentDescription = "블러 결과",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            CaptionText(
                "축소 배율을 키우면 시간이 급격히 줄어든다 — 블러 비용은 픽셀 수에 비례하므로 " +
                    "반지름을 키우기 전에 먼저 줄이는 것이 정석이다."
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "이 방식의 한계는 분명하다 — **정지 화면이다.** 캡처한 순간의 그림이라 뒤 콘텐츠가 움직여도 따라가지 않고, " +
                "다른 앱 위에 뜬 창은 애초에 캡처할 수 없다. 크로스 윈도우 블러가 API 로 제공되는 이유가 여기에 있다."
        )
    }
}

// ==================== 5. 비교 ====================

@Composable
private fun ComparisonCard() {
    SectionCard(title = "5. 무엇을 고를까") {
        TableRow("방식", "범위 / 비용 / 제약", isHeader = true)
        TableRow("blurBehindRadius", "다른 윈도우까지 / 시스템이 처리 / 31+ & 시스템 스위치")
        TableRow("setBackgroundBlur", "내 윈도우 안 / 시스템이 처리 / 31+ & 반투명 배경 필요")
        TableRow("Modifier.blur", "그 컴포저블 / GPU / 31+, 하위는 무시")
        TableRow("캡처 + SW 블러", "캡처한 뷰 / CPU(축소 필수) / 정지 화면, 창 밖은 불가")
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "실무 조합은 보통 이렇다 — **31+ 이고 시스템 스위치가 켜져 있으면 blurBehindRadius, " +
                "아니면 dim 만 진하게.** 캡처 폴백은 \"블러가 디자인의 핵심\"일 때만 값을 한다. " +
                "정지 화면이라는 사실을 사용자가 알아채기 때문이다."
        )
    }
}

// ==================== 6. 함정 ====================

@Composable
private fun PitfallCard() {
    SectionCard(title = "6. 걸리는 것들") {
        PitfallRow(
            "Modifier.blur 는 하위에서 조용히 무시된다",
            "예외도 경고도 없다. API 31 미만에서 아무 일도 일어나지 않는 것이 정상 동작이라 " +
                "\"코드는 맞는데 안 된다\"로 보인다."
        )
        PitfallRow(
            "지원 여부는 두 겹이다",
            "SDK_INT 만 보면 부족하다. isCrossWindowBlurEnabled 가 false 면 값을 넣어도 그림이 없다. " +
                "배터리 세이버·개발자 옵션·저사양 기기에서 실제로 꺼진다."
        )
        PitfallRow(
            "FLAG_BLUR_BEHIND 를 빠뜨리면 반지름만 남는다",
            "blurBehindRadius 를 넣어도 플래그가 없으면 적용되지 않는다. 반대로 반지름을 0 으로 되돌릴 때는 " +
                "플래그도 함께 내려야 한다."
        )
        PitfallRow(
            "배경 블러는 반투명 배경이 있어야 보인다",
            "Compose Dialog 의 윈도우 배경은 기본이 투명이라 setBackgroundBlurRadius 를 걸 바탕이 없다. " +
                "이 예제는 알파 70 짜리 ColorDrawable 을 깔아 두고 적용한다."
        )
        PitfallRow(
            "DialogWindowProvider 캐스팅은 구조 의존이다",
            "LocalView.current.parent 가 DialogWindowProvider 라는 보장은 Compose 구현에 기대는 것이다. " +
                "null 체크 없이 쓰면 다른 컨테이너에서 터진다."
        )
        PitfallRow(
            "그려지지 않은 레이어는 캡처할 수 없다",
            "GraphicsLayer 는 draw 패스에서 record 될 때 크기가 정해진다. 한 번도 그려지지 않았으면 size 가 0x0 이고 " +
                "toImageBitmap() 은 IllegalArgumentException(\"width & height must be > 0\") 으로 터진다. " +
                "실기기 계측(잠금 화면 뒤라 draw 패스가 없는 상태)에서 이 예외를 그대로 확인했다 — 캡처 전에 size 를 확인해야 한다."
        )
        PitfallRow(
            "캡처 폴백은 정지 화면이다",
            "캡처 시점의 그림이라 뒤가 움직여도 갱신되지 않는다. 애니메이션이 있는 배경에 쓰면 " +
                "\"멈춘 화면\"으로 보인다. 필요하면 주기적으로 다시 캡처해야 하고, 그 순간 비용이 다시 든다."
        )
    }
}

// ==================== 공통 요소 ====================

@Composable
private fun SliderRow(
    label: String,
    value: Int,
    max: Int,
    min: Int = 0,
    onChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.width(64.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$value",
            modifier = Modifier.width(40.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF616161)
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun TableRow(
    first: String,
    second: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = first,
            modifier = Modifier.width(96.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(80.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(2.dp))
        CaptionText(description)
    }
}

@Composable
private fun DemoButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
