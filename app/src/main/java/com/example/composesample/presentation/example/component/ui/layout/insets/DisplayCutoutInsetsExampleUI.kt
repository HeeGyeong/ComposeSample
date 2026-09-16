package com.example.composesample.presentation.example.component.ui.layout.insets

import android.app.Activity
import android.graphics.Path as AndroidPath
import android.os.Build
import android.view.DisplayCutout
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.cutoutPath
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.mandatorySystemGestures
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.recalculateWindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.waterfall
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.example.composesample.presentation.MainHeader
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 디스플레이 컷아웃 & 인셋 예제
 * - 인셋은 "피해야 하는 거리"지만 종류마다 **무엇을 피하는지**가 다르다. `safeDrawing`/`safeGestures`/
 *   `safeContent` 는 측정값이 아니라 기본 인셋들의 **합집합**이고, 그 조합이 서로 다르다.
 * - 기기에 컷아웃이 있다고 `displayCutout` 인셋이 들어오지는 않는다. 내 윈도우가 그 영역까지
 *   확장해야 값이 생긴다 — 화면에서 `layoutInDisplayCutoutMode` 와 edge-to-edge 를 직접 켜고 끄며
 *   같은 기기에서 0 과 실제 값이 갈리는 것을 보여준다.
 * - 인셋 패딩 modifier 는 **자기 위치를 모른다.** 화면 한가운데 카드에 `safeDrawingPadding()` 을
 *   걸어도 상태바·내비바 높이가 그대로 들어간다. `consumeWindowInsets` 와 `recalculateWindowInsets`
 *   로 그 값이 0 이 되는 것까지 나란히 본다.
 * - ⚠️ 이 화면 자체가 함정 하나를 밟고 고쳤다 — 윈도우 설정을 되돌리는 `DisposableEffect` 를
 *   3번 카드 **안**에 두었더니, 그 카드가 스크롤로 사라질 때마다 onDispose 가 돌아 설정이
 *   원복됐다. 실기기 계측으로 확인한 증상은 스크롤 뒤 Compose 가 idle 에 도달하지 못하는 것이었고,
 *   윈도우 상태를 화면 루트(`rememberWindowModeController`)로 올리자 사라졌다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 참고.
 */

// ==================== 인셋 읽기 ====================

/** 인셋 네 변의 px 값. Compose 의 WindowInsets 는 px 정수만 돌려준다. */
private data class InsetValues(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val isZero: Boolean get() = left == 0 && top == 0 && right == 0 && bottom == 0
}

/**
 * 화면이 쓰는 인셋을 한곳에서 읽어 둔 값 묶음.
 *
 * 카드마다 `WindowInsets.xxx` 를 읽어도 되지만, 그러면 카드가 스크롤로 사라질 때 구독이 끊긴다.
 * WindowInsetsHolder 는 구독자 수가 0 → 1 이 될 때 리스너를 다시 달고 `requestApplyInsets()` 를
 * 호출하므로(바이트코드 확인), 구독은 리스트 밖에서 한 번만 잡고 카드에는 값으로 내려보낸다.
 */
private class InsetsSnapshot(
    val rows: List<Pair<String, InsetValues>>,
    val displayCutout: InsetValues,
    val safeDrawing: WindowInsets,
    val cutoutPath: Path?
)

@Composable
private fun rememberInsetsSnapshot(): InsetsSnapshot {
    val safeDrawing = WindowInsets.safeDrawing
    val displayCutout = WindowInsets.displayCutout.readValues()
    return InsetsSnapshot(
        rows = listOf(
            "statusBars" to WindowInsets.statusBars.readValues(),
            "navigationBars" to WindowInsets.navigationBars.readValues(),
            "captionBar" to WindowInsets.captionBar.readValues(),
            "systemBars" to WindowInsets.systemBars.readValues(),
            "ime" to WindowInsets.ime.readValues(),
            "displayCutout" to displayCutout,
            "waterfall" to WindowInsets.waterfall.readValues(),
            "systemGestures" to WindowInsets.systemGestures.readValues(),
            "mandatorySystem…" to WindowInsets.mandatorySystemGestures.readValues(),
            "tappableElement" to WindowInsets.tappableElement.readValues(),
            "safeDrawing" to safeDrawing.readValues(),
            "safeGestures" to WindowInsets.safeGestures.readValues(),
            "safeContent" to WindowInsets.safeContent.readValues()
        ),
        displayCutout = displayCutout,
        safeDrawing = safeDrawing,
        cutoutPath = WindowInsets.cutoutPath
    )
}

/**
 * 액티비티 윈도우의 컷아웃 모드·edge-to-edge 를 들고 있는 컨트롤러.
 *
 * 화면 루트에서 remember 해야 한다. 리스트 항목 안에서 만들면 그 항목이 스크롤로 사라질 때
 * DisposableEffect 의 onDispose 가 돌아 사용자가 켜 둔 설정이 임의로 원복된다.
 */
/** `LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT` 의 값. API 28 미만에서 상수를 참조하지 않기 위한 대체. */
private const val CUTOUT_MODE_DEFAULT_VALUE = 0

private class WindowModeController(
    private val activity: Activity?,
    val originalCutoutMode: Int
) {
    var cutoutMode by mutableIntStateOf(originalCutoutMode)
        private set
    var edgeToEdge by mutableStateOf(false)
        private set

    fun selectCutoutMode(mode: Int) {
        cutoutMode = mode
        apply()
    }

    fun toggleEdgeToEdge() {
        edgeToEdge = !edgeToEdge
        apply()
    }

    fun restore() {
        val window = activity?.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val params = window.attributes
            params.layoutInDisplayCutoutMode = originalCutoutMode
            window.attributes = params
        }
    }

    private fun apply() {
        val window = activity?.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, !edgeToEdge)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val params = window.attributes
            params.layoutInDisplayCutoutMode = cutoutMode
            window.attributes = params
        }
    }
}

@Composable
private fun rememberWindowModeController(): WindowModeController {
    // activity-compose 1.13.0 의 LocalActivity 로 액티비티를 얻는다. Context 를 재귀로 풀어내는
    // 관용구(`when (this) { is Activity -> ... }`)는 JDK 21 typeSwitch invokedynamic 으로 컴파일되고,
    // 디버그 빌드의 HotSwan 인터프리터가 그 BSM 을 구현하지 않아 화면이 통째로 렌더되지 않는다(실측).
    val activity = LocalActivity.current
    val controller = remember(activity) {
        // DEFAULT 는 0 이고 API 28 미만에는 상수 자체가 없으므로, 가드 안에서만 상수를 읽는다.
        val original = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity?.window?.attributes?.layoutInDisplayCutoutMode
                ?: WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        } else {
            CUTOUT_MODE_DEFAULT_VALUE
        }
        WindowModeController(activity = activity, originalCutoutMode = original)
    }
    // 화면을 떠날 때만 원래 윈도우 설정으로 되돌린다.
    DisposableEffect(controller) {
        onDispose { controller.restore() }
    }
    return controller
}

@Composable
private fun WindowInsets.readValues(): InsetValues {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    return InsetValues(
        left = getLeft(density, layoutDirection),
        top = getTop(density),
        right = getRight(density, layoutDirection),
        bottom = getBottom(density)
    )
}

/** px 를 소수 1자리 dp 문자열로. 화면에 숫자를 그대로 적기 위한 표기용 변환. */
private fun Int.toDpText(density: Density): String {
    val dp = with(density) { this@toDpText.toDp().value }
    return "${(dp * 10).roundToInt() / 10f}"
}

/**
 * 모드 값을 이름으로. `LAYOUT_IN_DISPLAY_CUTOUT_MODE_*` 는 API 28 상수라 분기 안에서만 참조한다
 * (컴파일 타임 상수라 런타임 문제는 없지만, 가드 밖 참조는 lint 가 NewApi 로 잡는다).
 */
private fun cutoutModeName(mode: Int): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return "API 28 미만 — 모드 개념 없음"
    return when (mode) {
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT -> "DEFAULT"
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES -> "SHORT_EDGES"
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER -> "NEVER"
        else -> mode.toString()
    }
}

// ==================== 화면 ====================

@Composable
fun DisplayCutoutInsetsExampleUI(onBackEvent: () -> Unit) {
    // 인셋은 리스트 밖에서 한 번만 구독한다. 카드는 값만 받는다.
    val insets = rememberInsetsSnapshot()
    // 윈도우 설정도 화면 루트가 들고 있는다. 리스트 항목 안에 두면 그 카드가 스크롤로 사라질 때마다
    // onDispose 가 돌아 설정이 제멋대로 원복된다(3번 카드가 설명하는 함정).
    val windowMode = rememberWindowModeController()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "디스플레이 컷아웃 & 인셋",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { LiveInsetsCard(insets = insets) }
            item { WindowModeCard(insets = insets, windowMode = windowMode) }
            item { PaddingCompareCard(insets = insets) }
            item { ConsumeCard(insets = insets) }
            item { CutoutShapeCard(insets = insets) }
            item { PitfallCard() }
        }
    }
}

// ==================== 1. 안전 영역 3종의 합집합 규칙 ====================

@Composable
private fun ConceptCard() {
    SectionCard(title = "1. safe* 는 기본 인셋의 합집합이다") {
        BodyText(
            "safeDrawing / safeGestures / safeContent 는 따로 측정되는 값이 아니라 기본 인셋들을 " +
                "union 한 결과다. 조합이 서로 달라서 \"안전\"이 가리키는 범위도 다르다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("안전 영역", "합집합 구성", isHeader = true)
        TableRow("safeDrawing", "systemBars ∪ ime ∪ displayCutout")
        TableRow("safeGestures", "tappableElement ∪ mandatorySystemGestures ∪ systemGestures ∪ waterfall")
        TableRow("safeContent", "safeDrawing ∪ safeGestures")
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "foundation-layout 1.11.4 의 WindowInsetsHolder 생성자 바이트코드에서 확인한 조합이다. " +
                "문서가 아니라 실제 union 호출 순서를 그대로 옮겼다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "여기서 곧바로 함정이 하나 나온다 — waterfall 은 safeDrawing 에 없다. " +
                "가장자리가 휘어진 기기에서 그리기 영역만 맞추면, 휜 면에 글자가 얹힌다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("기본 인셋", "의미", isHeader = true)
        TableRow("displayCutout", "컷아웃(노치·펀치홀)을 피하려면 각 변에서 띄워야 하는 거리")
        TableRow("waterfall", "휘어진 가장자리의 곡면 영역")
        TableRow("systemGestures", "시스템이 제스처로 가져가는 영역")
        TableRow("mandatorySystemGestures", "앱이 가로챌 수 없는 제스처 영역(홈 인디케이터 등)")
        TableRow("tappableElement", "시스템이 탭을 받는 영역(내비게이션 바가 보일 때)")
    }
}

// ==================== 2. 지금 이 화면이 받는 인셋 ====================

@Composable
private fun LiveInsetsCard(insets: InsetsSnapshot) {
    val density = LocalDensity.current

    SectionCard(title = "2. 이 기기·이 윈도우의 실측값") {
        BodyText("지금 이 컴포지션이 읽은 값이다. 3번 카드에서 윈도우 모드를 바꾸면 그 자리에서 바뀐다.")
        Spacer(modifier = Modifier.height(10.dp))

        insets.rows.forEach { (name, values) ->
            InsetRow(name = name, values = values, density = density)
        }

        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "L·T·R·B 순서. 파란 값이 0 이 아닌 인셋이다. 기본 상태에서 전부 0 이면 정상이다 — " +
                "decorFitsSystemWindows 가 true 인 창은 시스템 바 영역을 이미 잘라낸 뒤라 " +
                "컴포즈까지 내려오는 인셋이 없다."
        )
    }
}

@Composable
private fun InsetRow(name: String, values: InsetValues, density: Density) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.width(112.dp),
            fontSize = 11.sp,
            color = Color(0xFF424242)
        )
        Text(
            text = "${values.left} · ${values.top} · ${values.right} · ${values.bottom} px",
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (values.isZero) Color(0xFF9E9E9E) else Color(0xFF1565C0)
        )
        Text(
            text = if (values.isZero) {
                "-"
            } else {
                "T${values.top.toDpText(density)} B${values.bottom.toDpText(density)}dp"
            },
            modifier = Modifier.width(96.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF757575)
        )
    }
}

// ==================== 3. 기기의 컷아웃 vs 내 윈도우의 인셋 ====================

@Composable
private fun WindowModeCard(insets: InsetsSnapshot, windowMode: WindowModeController) {
    val context = LocalContext.current

    // 기기가 물리적으로 갖고 있는 컷아웃. 윈도우 설정과 무관하게 Display 가 알려준다(API 29+).
    val deviceCutout = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.getDisplayOrDefault(context).cutout
        } else {
            null
        }
    }

    SectionCard(title = "3. 기기에 컷아웃이 있어도 인셋은 0 일 수 있다") {
        BodyText(
            "컷아웃 인셋은 \"기기에 컷아웃이 있는가\"가 아니라 \"내 윈도우가 그 영역까지 확장했는가\"로 " +
                "결정된다. 둘을 나란히 두면 차이가 보인다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        ResultRow("기기(Display)", deviceCutoutText(deviceCutout))
        // deviceCutout 은 API 29+ 에서만 채워지지만, 호출부에도 버전 가드를 둬야 lint 가 통과한다.
        if (deviceCutout != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ResultRow("safeInset", deviceSafeInsetText(deviceCutout))
            ResultRow("boundingRect", deviceBoundingRectText(deviceCutout))
        }
        ResultRow(
            "내 윈도우",
            "displayCutout = ${insets.displayCutout.left} · ${insets.displayCutout.top} · " +
                "${insets.displayCutout.right} · ${insets.displayCutout.bottom} px"
        )

        Spacer(modifier = Modifier.height(12.dp))
        BodyText("윈도우 설정을 바꿔 위 값이 어떻게 달라지는지 직접 확인한다.")
        Spacer(modifier = Modifier.height(8.dp))

        ResultRow("현재 모드", cutoutModeName(windowMode.cutoutMode))
        ResultRow("edge-to-edge", if (windowMode.edgeToEdge) "ON (decorFitsSystemWindows=false)" else "OFF")

        Spacer(modifier = Modifier.height(8.dp))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DemoButton(text = "DEFAULT", color = Color(0xFF546E7A)) {
                    windowMode.selectCutoutMode(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT)
                }
                DemoButton(text = "SHORT_EDGES", color = Color(0xFF1976D2)) {
                    windowMode.selectCutoutMode(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
                }
                DemoButton(text = "NEVER", color = Color(0xFF8D6E63)) {
                    windowMode.selectCutoutMode(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            DemoButton(
                text = if (windowMode.edgeToEdge) "edge-to-edge 끄기" else "edge-to-edge 켜기",
                color = Color(0xFF00897B)
            ) {
                windowMode.toggleEdgeToEdge()
            }
        } else {
            CaptionText("layoutInDisplayCutoutMode 는 API 28 부터라 이 기기에서는 조작할 수 없다.")
        }

        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            "// 컷아웃 영역까지 창을 넓히는 두 축\n" +
                "WindowCompat.setDecorFitsSystemWindows(window, false)   // edge-to-edge\n" +
                "window.attributes = window.attributes.also {\n" +
                "    it.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES\n" +
                "}"
        )
        CaptionText(
            "원복은 화면 루트의 DisposableEffect 가 맡는다. 이 카드 안에 두면 카드가 스크롤로 " +
                "사라질 때마다 설정이 되돌아간다 — 7번 카드의 함정."
        )
    }
}

private fun deviceCutoutText(cutout: DisplayCutout?): String = when {
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> "Display.getCutout() 은 API 29+"
    cutout == null -> "컷아웃 없음"
    else -> "컷아웃 있음"
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun deviceSafeInsetText(cutout: DisplayCutout): String =
    "${cutout.safeInsetLeft} · ${cutout.safeInsetTop} · " +
        "${cutout.safeInsetRight} · ${cutout.safeInsetBottom} px"

@RequiresApi(Build.VERSION_CODES.Q)
private fun deviceBoundingRectText(cutout: DisplayCutout): String {
    val rects = cutout.boundingRects
    if (rects.isEmpty()) return "0개"
    val first = rects.first()
    return "${rects.size}개 · 첫 사각형 (${first.left}, ${first.top})~(${first.right}, ${first.bottom})"
}

// ==================== 4. 패딩 modifier 가 실제로 넣는 값 ====================

@Composable
private fun PaddingCompareCard(insets: InsetsSnapshot) {
    val density = LocalDensity.current
    val cutout = insets.rows.first { it.first == "displayCutout" }.second
    val drawing = insets.rows.first { it.first == "safeDrawing" }.second
    val content = insets.rows.first { it.first == "safeContent" }.second

    SectionCard(title = "4. 인셋 패딩 modifier 는 위치를 모른다") {
        BodyText(
            "아래 세 상자는 화면 한가운데 있는데도 윈도우 인셋을 그대로 적용받는다. " +
                "파란 영역이 남은 자리이고, 줄어든 만큼이 그 modifier 가 넣은 여백이다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        PaddingDemoRow(
            label = "displayCutoutPadding()",
            insetModifier = Modifier.displayCutoutPadding(),
            values = cutout,
            density = density
        )
        PaddingDemoRow(
            label = "safeDrawingPadding()",
            insetModifier = Modifier.safeDrawingPadding(),
            values = drawing,
            density = density
        )
        PaddingDemoRow(
            label = "safeContentPadding()",
            insetModifier = Modifier.safeContentPadding(),
            values = content,
            density = density
        )

        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "상자 높이(72dp)보다 위아래 여백이 크면 파란 영역은 아예 사라진다. " +
                "카드가 이미 그만큼 아래에 있는데도 한 번 더 밀어낸 결과다."
        )
    }
}

// insetModifier 는 이 요소의 modifier 가 아니라 "시연 대상" 이라 이름을 modifier 로 두지 않는다.
@Suppress("ModifierParameter")
@Composable
private fun PaddingDemoRow(
    label: String,
    insetModifier: Modifier,
    values: InsetValues,
    density: Density
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFFE3F2FD), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(insetModifier)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1976D2), RoundedCornerShape(4.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "들어가는 여백 = L${values.left} · T${values.top} · R${values.right} · B${values.bottom} px " +
                "(세로 합 ${(values.top + values.bottom).toDpText(density)}dp)"
        )
    }
}

// ==================== 5. 소비와 재계산 ====================

@Composable
private fun ConsumeCard(insets: InsetsSnapshot) {
    SectionCard(title = "5. 여백을 0 으로 만드는 두 가지 방법") {
        BodyText(
            "같은 safeDrawingPadding() 이라도 앞에 무엇을 두느냐로 결과가 달라진다. " +
                "consumeWindowInsets 는 \"이미 처리했다\"고 선언하는 것이고, " +
                "recalculateWindowInsets 는 배치된 위치를 기준으로 인셋을 다시 계산한다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        ConsumeDemoRow(
            label = "safeDrawingPadding()  // 그대로",
            insetModifier = Modifier.safeDrawingPadding()
        )
        ConsumeDemoRow(
            label = "consumeWindowInsets(safeDrawing) 뒤에 같은 패딩",
            insetModifier = Modifier
                .consumeWindowInsets(insets.safeDrawing)
                .safeDrawingPadding()
        )
        ConsumeDemoRow(
            label = "recalculateWindowInsets() 뒤에 같은 패딩",
            insetModifier = Modifier
                .recalculateWindowInsets()
                .safeDrawingPadding()
        )

        Spacer(modifier = Modifier.height(6.dp))
        BodyText(
            "두 번째·세 번째 상자는 파란 영역이 그대로 남는다. 위 두 modifier 가 앞에서 인셋을 " +
                "지워 버려 뒤의 패딩이 0 을 적용하기 때문이다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        TableRow("modifier", "역할", isHeader = true)
        TableRow("windowInsetsPadding", "여백 적용 + 소비를 함께 한다")
        TableRow("padding(asPaddingValues)", "여백만 준다. 소비 기록이 남지 않는다")
        TableRow("consumeWindowInsets", "여백 없이 소비만 선언한다")
        TableRow("recalculateWindowInsets", "배치 위치 기준으로 인셋을 다시 계산해 아래로 내려준다")
    }
}

// 위와 같은 이유로 modifier 이름을 쓰지 않는다.
@Suppress("ModifierParameter")
@Composable
private fun ConsumeDemoRow(label: String, insetModifier: Modifier) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(insetModifier)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

// ==================== 6. 컷아웃 모양 그리기 ====================

@Composable
private fun CutoutShapeCard(insets: InsetsSnapshot) {
    val view = LocalView.current
    // 디스플레이 외곽(라운드 코너) Path. 인셋이 바뀌면 다시 읽는다.
    val displayShapePath: AndroidPath? = remember(insets.cutoutPath, view) {
        ViewCompat.getRootWindowInsets(view)?.displayShape?.path
    }

    SectionCard(title = "6. 컷아웃과 디스플레이 외곽을 Path 로 받기") {
        BodyText(
            "인셋이 네 변의 거리라면 Path 는 실제 모양이다. 컷아웃 모양은 WindowInsets.cutoutPath" +
                "(Compose 1.11 신규), 화면 외곽 모양은 WindowInsetsCompat.displayShape 가 준다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PathPreview(
                modifier = Modifier.weight(1f),
                title = "cutoutPath",
                path = insets.cutoutPath,
                emptyMessage = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    "API 31 미만은 항상 null"
                } else {
                    "null — 창이 컷아웃까지 확장하지 않았다(3번 카드)"
                }
            )
            PathPreview(
                modifier = Modifier.weight(1f),
                title = "displayShape",
                path = displayShapePath?.asComposePath(),
                emptyMessage = "빈 Path — 외곽 정보를 만들 수 없었다"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        ResultRow("cutoutPath", pathBoundsText(insets.cutoutPath))
        ResultRow("displayShape", pathBoundsText(displayShapePath?.asComposePath()))

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "cutoutPath 는 DisplayCutoutCompat.getCutoutPath() 를 그대로 Compose Path 로 바꾼 값이고, " +
                "그 구현은 API 31 미만에서 null 을 돌려준다(바이트코드 확인)."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "displayShape 는 API 34 전용처럼 보이지만 그렇지 않다 — core 1.18.0 은 API 34 미만에서 " +
                "디스플레이 크기와 네 모서리 반경(DisplayCompat.getRoundedCorner)으로 둥근 사각형을 " +
                "직접 합성한다. 그래서 플랫폼 DisplayShape 가 없는 기기에서도 값이 나오지만, " +
                "그 모양은 플랫폼이 준 것이 아니라 반경으로 재구성한 근사치다."
        )
    }
}

@Composable
private fun PathPreview(
    modifier: Modifier = Modifier,
    title: String,
    path: Path?,
    emptyMessage: String
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(Color(0xFFECEFF1), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            val bounds = path?.getBounds()
            if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
                Text(
                    text = emptyMessage,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = 10.sp,
                    color = Color(0xFF78909C),
                    lineHeight = 14.sp
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    val scale = min(size.width / bounds.width, size.height / bounds.height)
                    withTransform({
                        translate(
                            left = (size.width - bounds.width * scale) / 2f - bounds.left * scale,
                            top = (size.height - bounds.height * scale) / 2f - bounds.top * scale
                        )
                        scale(scale, scale, pivot = Offset.Zero)
                    }) {
                        drawPath(path = path, color = Color(0xFF1565C0))
                    }
                }
            }
        }
    }
}

private fun pathBoundsText(path: Path?): String {
    if (path == null) return "null"
    val bounds = path.getBounds()
    if (bounds.width <= 0f || bounds.height <= 0f) return "빈 Path (isEmpty)"
    return "(${bounds.left.roundToInt()}, ${bounds.top.roundToInt()})~" +
        "(${bounds.right.roundToInt()}, ${bounds.bottom.roundToInt()}) px"
}

// ==================== 7. 함정 ====================

@Composable
private fun PitfallCard() {
    SectionCard(title = "7. 함정") {
        PitfallRow(
            "기기의 컷아웃과 내 인셋은 별개다",
            "Display.getCutout() 이 컷아웃을 알려줘도, 창이 그 영역까지 확장하지 않으면 " +
                "displayCutout 인셋은 0 이다. 3번 카드에서 모드를 바꾸면 값이 생긴다."
        )
        PitfallRow(
            "리스트 항목 안의 DisposableEffect 는 화면 이탈이 아니라 스크롤 아웃에서 돈다",
            "윈도우 설정을 원복하는 onDispose 를 3번 카드 안에 두었더니, 카드가 화면 밖으로 " +
                "밀릴 때마다 설정이 되돌아갔다(스크롤 후 Compose 가 idle 에 도달하지 못하는 것으로 " +
                "드러남). 화면 수명과 묶일 상태는 리스트 밖, 화면 루트에서 remember 해야 한다."
        )
        PitfallRow(
            "padding(asPaddingValues()) 는 소비하지 않는다",
            "여백은 생기지만 소비 기록이 남지 않아 자식이 같은 인셋을 다시 적용한다. " +
                "적용과 소비를 함께 하려면 windowInsetsPadding(), 소비만 하려면 consumeWindowInsets()."
        )
        PitfallRow(
            "safeDrawing 에는 waterfall 이 없다",
            "waterfall 은 safeGestures 쪽에만 union 된다. 곡면 엣지 기기에서 그리기 영역만 " +
                "맞추면 휜 면에 콘텐츠가 올라간다."
        )
        PitfallRow(
            "displayShape 는 null 로 판정할 수 없다",
            "API 34 미만에서도 core 가 모서리 반경으로 모양을 합성해 돌려주고, 그마저 실패하면 " +
                "null 이 아니라 빈 Path 를 가진 EMPTY 객체를 준다. isEmpty 로 봐야 한다."
        )
        PitfallRow(
            "cutoutPath 는 API 31 부터",
            "DisplayCutoutCompat.getCutoutPath() 가 31 미만에서 null 을 반환하므로 " +
                "WindowInsets.cutoutPath 도 null 이다. 그 아래에서는 boundingRects(API 29+)의 " +
                "사각형 근사가 한계다."
        )
    }
}

// ==================== 공통 컴포넌트 ====================

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
            modifier = Modifier.width(118.dp),
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
            modifier = Modifier.width(88.dp),
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
