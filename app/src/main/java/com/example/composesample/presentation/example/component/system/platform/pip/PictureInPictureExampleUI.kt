package com.example.composesample.presentation.example.component.system.platform.pip

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.PictureInPictureParamsCompat
import androidx.core.util.Consumer
import com.example.composesample.presentation.MainHeader

/**
 * Picture-in-Picture compat 예제
 * - androidx.core 1.18.0 의 PictureInPictureParamsCompat 로 PiP 파라미터를 한 번만 구성하고,
 *   toPictureInPictureParams() 가 API 레벨(33/31/26)에 따라 어떤 필드를 잘라내는지 대조한다.
 * - 만든 파라미터를 플랫폼 getter 로 되읽어(= API 33+) "무엇이 실제로 반영됐는가"를 화면에 표시한다.
 * - 종횡비 검증 범위와 enterPictureInPictureMode 의 resumed 요구를 실기기 실측값으로 보여준다.
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

// ==================== 파라미터 구성부 (Compose 밖에서 단독 검증 가능) ====================

/** 데모가 조작하는 PiP 파라미터 옵션 */
internal data class PipOptions(
    val aspectRatio: Rational = Rational(16, 9),
    val useSourceRectHint: Boolean = true,
    val seamlessResizeEnabled: Boolean = true,
    /** setEnabled = autoEnter(API 31+). 화면을 벗어날 때 시스템이 알아서 PiP 로 넣는다 */
    val autoEnterEnabled: Boolean = false,
    val useExpandedAspectRatio: Boolean = true,
    val useTitle: Boolean = true
)

/**
 * 옵션대로 compat 파라미터를 만든다.
 *
 * **이 Builder 자체에는 API 게이팅이 없다** — minSdk 24 에서도 9개 필드를 모두 채울 수 있고,
 * 잘라내기는 [PictureInPictureParamsCompat.toPictureInPictureParams] 변환 시점에 일어난다.
 */
internal fun buildPipParams(
    options: PipOptions,
    sourceRectHint: Rect?
): PictureInPictureParamsCompat =
    PictureInPictureParamsCompat.Builder()
        .setAspectRatio(options.aspectRatio)
        .setSeamlessResizeEnabled(options.seamlessResizeEnabled)
        .setEnabled(options.autoEnterEnabled)
        .apply {
            if (options.useSourceRectHint && sourceRectHint != null) {
                setSourceRectHint(sourceRectHint)
            }
            if (options.useExpandedAspectRatio) {
                setExpandedAspectRatio(Rational(4, 3))
            }
            if (options.useTitle) {
                setTitle("ComposeSample 재생 중")
                setSubTitle("PiP compat 예제")
            }
        }
        .build()

/** 변환된 플랫폼 파라미터에서 되읽은 값. getter 는 전부 API 33 부터라 그 미만에서는 읽을 수 없다. */
internal data class PipReadback(val label: String, val value: String)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun readBackPipParams(params: PictureInPictureParams): List<PipReadback> = listOf(
    PipReadback("aspectRatio", params.aspectRatio?.toString() ?: "null"),
    PipReadback("expandedAspectRatio", params.expandedAspectRatio?.toString() ?: "null"),
    PipReadback("sourceRectHint", params.sourceRectHint?.toShortString() ?: "null"),
    // getActions() 는 스텁상 non-null 이고 실측도 빈 리스트였다(compat 이 항상 리스트를 넘기기 때문)
    PipReadback("actions", "${params.actions.size} 개"),
    PipReadback("closeAction", params.closeAction?.toString() ?: "null"),
    PipReadback("title", params.title?.toString() ?: "null"),
    PipReadback("subtitle", params.subtitle?.toString() ?: "null"),
    PipReadback("isSeamlessResizeEnabled", params.isSeamlessResizeEnabled.toString()),
    PipReadback("isAutoEnterEnabled", params.isAutoEnterEnabled.toString())
)

/** Compose 의 Context 체인을 거슬러 호스트 Activity 를 찾는다(프로젝트 공통 패턴) */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// ==================== 화면 ====================

@Composable
fun PictureInPictureExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var options by remember { mutableStateOf(PipOptions()) }
    var sourceRectHint by remember { mutableStateOf<Rect?>(null) }
    var isInPipMode by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity?.isInPictureInPictureMode == true
        )
    }
    var lastResult by remember { mutableStateOf("아직 시도하지 않음") }

    // PiP 진입/복귀 콜백. ComponentActivity 가 OnPictureInPictureModeChangedProvider 를 구현하므로
    // 액티비티를 수정하지 않고도 화면 쪽에서 구독할 수 있다.
    DisposableEffect(activity) {
        val componentActivity = activity as? ComponentActivity
        val listener = Consumer<androidx.core.app.PictureInPictureModeChangedInfo> { info ->
            isInPipMode = info.isInPictureInPictureMode
        }
        componentActivity?.addOnPictureInPictureModeChangedListener(listener)
        onDispose { componentActivity?.removeOnPictureInPictureModeChangedListener(listener) }
    }

    // PiP 창은 아주 작다. 카드 목록을 그대로 두면 아무것도 읽히지 않으므로 최소 UI 로 바꾼다.
    if (isInPipMode) {
        PipModeContent(options)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Picture-in-Picture compat",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PipConceptCard() }
            item {
                PipDemoCard(
                    options = options,
                    onOptionsChange = { options = it },
                    sourceRectHint = sourceRectHint,
                    onSourceRectHint = { sourceRectHint = it },
                    lastResult = lastResult,
                    onResult = { lastResult = it },
                    activity = activity
                )
            }
            item { PipReadbackCard(options = options, sourceRectHint = sourceRectHint) }
            item { PipApiTierCard() }
            item { PipAspectRatioCard(activity = activity) }
            item { PipPitfallCard() }
        }
    }
}

/** PiP 창에서 보여줄 최소 화면 — 실제 앱이라면 영상 프레임만 남기는 자리다 */
@Composable
private fun PipModeContent(options: PipOptions) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF102027)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PiP 모드",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${options.aspectRatio.numerator}:${options.aspectRatio.denominator}",
                fontSize = 12.sp,
                color = Color(0xFFB0BEC5)
            )
        }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun PipConceptCard() {
    PipSectionCard(title = "1. compat 이 대신 해주는 일") {
        BodyText(
            "PiP 파라미터는 API 레벨마다 담을 수 있는 필드가 다르다. 플랫폼 " +
                "PictureInPictureParams.Builder 를 직접 쓰면 setTitle 은 API 33+, " +
                "setSeamlessResizeEnabled 는 31+ 하는 식으로 호출부마다 버전 분기를 써야 한다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "PictureInPictureParamsCompat 은 9개 필드를 버전 분기 없이 한 번에 받아 두고, " +
                "toPictureInPictureParams() 로 변환할 때 그 기기가 이해하는 만큼만 옮겨 담는다. " +
                "즉 분기는 호출부가 아니라 변환 한 곳으로 모인다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeText(
            "PictureInPictureParamsCompat.Builder()\n" +
                "    .setAspectRatio(Rational(16, 9))\n" +
                "    .setTitle(\"재생 중\")          // API 33+ 에서만 반영\n" +
                "    .setSeamlessResizeEnabled(true) // API 31+ 에서만 반영\n" +
                "    .build()\n" +
                "    .toPictureInPictureParams()     // @RequiresApi(26)"
        )
        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "PiP 진입 경로는 세 가지다 — ① 사용자가 누르는 버튼에서 enterPictureInPictureMode() " +
                "② setEnabled(true)(=autoEnter, API 31+)로 홈 제스처 시 시스템이 자동 진입 " +
                "③ launch-into-pip(API 31+, 처음부터 PiP 로 시작). 이 예제는 ①과 ②를 다룬다."
        )
    }
}

// ==================== 2. 라이브 데모 ====================

@Composable
private fun PipDemoCard(
    options: PipOptions,
    onOptionsChange: (PipOptions) -> Unit,
    sourceRectHint: Rect?,
    onSourceRectHint: (Rect) -> Unit,
    lastResult: String,
    onResult: (String) -> Unit,
    activity: Activity?
) {
    PipSectionCard(title = "2. 파라미터를 바꿔 PiP 로 들어가기") {
        BodyText(
            "아래 상자가 PiP 로 이어질 콘텐츠라고 가정한다. 상자의 화면 좌표를 " +
                "onGloballyPositioned 로 읽어 sourceRectHint 로 넘기면, 시스템이 축소 애니메이션의 " +
                "출발 지점으로 사용한다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(options.aspectRatio.toFloat().coerceIn(0.5f, 2.3f))
                .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                .onGloballyPositioned { coordinates ->
                    val bounds = coordinates.boundsInWindow()
                    onSourceRectHint(
                        Rect(
                            bounds.left.toInt(),
                            bounds.top.toInt(),
                            bounds.right.toInt(),
                            bounds.bottom.toInt()
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "콘텐츠 영역 ${options.aspectRatio.numerator}:${options.aspectRatio.denominator}",
                fontSize = 12.sp,
                color = Color(0xFFCFD8DC)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Rational(16, 9), Rational(1, 1), Rational(9, 16)).forEach { ratio ->
                val selected = ratio == options.aspectRatio
                DemoButton(
                    text = "${ratio.numerator}:${ratio.denominator}",
                    color = if (selected) Color(0xFF1976D2) else Color(0xFF90A4AE)
                ) {
                    onOptionsChange(options.copy(aspectRatio = ratio))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OptionSwitch("sourceRectHint 사용", options.useSourceRectHint) {
            onOptionsChange(options.copy(useSourceRectHint = it))
        }
        OptionSwitch("seamlessResizeEnabled (API 31+)", options.seamlessResizeEnabled) {
            onOptionsChange(options.copy(seamlessResizeEnabled = it))
        }
        OptionSwitch("autoEnter = setEnabled (API 31+)", options.autoEnterEnabled) {
            onOptionsChange(options.copy(autoEnterEnabled = it))
        }
        OptionSwitch("expandedAspectRatio (API 33+)", options.useExpandedAspectRatio) {
            onOptionsChange(options.copy(useExpandedAspectRatio = it))
        }
        OptionSwitch("title / subTitle (API 33+)", options.useTitle) {
            onOptionsChange(options.copy(useTitle = it))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = "PiP 진입", color = Color(0xFF1976D2)) {
                onResult(enterPip(activity, options, sourceRectHint))
            }
            DemoButton(text = "파라미터만 적용", color = Color(0xFF546E7A)) {
                onResult(applyPipParams(activity, options, sourceRectHint))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        ResultRow("결과", lastResult)
        ResultRow(
            "sourceRectHint",
            sourceRectHint?.toShortString() ?: "아직 측정되지 않음"
        )
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "autoEnter 를 켠 뒤 홈으로 나가면 버튼을 누르지 않아도 PiP 로 들어간다(API 31+). " +
                "그 동작은 '파라미터만 적용'으로 파라미터를 먼저 심어 둬야 한다."
        )
    }
}

/** 실제 진입. API 26 미만에는 PiP 자체가 없다. */
private fun enterPip(activity: Activity?, options: PipOptions, hint: Rect?): String {
    if (activity == null) return "Activity 를 찾지 못함"
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "API 26 미만 — PiP 미지원"

    val params = buildPipParams(options, hint).toPictureInPictureParams()
    return runCatching { activity.enterPictureInPictureMode(params) }
        .fold(
            onSuccess = { entered -> if (entered) "진입 성공 (반환 true)" else "거부됨 (반환 false)" },
            onFailure = { throwable -> "${throwable.javaClass.simpleName}: ${throwable.message}" }
        )
}

/** 진입하지 않고 파라미터만 갱신 — autoEnter 를 쓰려면 이 경로가 필요하다 */
private fun applyPipParams(activity: Activity?, options: PipOptions, hint: Rect?): String {
    if (activity == null) return "Activity 를 찾지 못함"
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "API 26 미만 — PiP 미지원"

    val params = buildPipParams(options, hint).toPictureInPictureParams()
    return runCatching { activity.setPictureInPictureParams(params) }
        .fold(
            onSuccess = { "setPictureInPictureParams 적용됨" },
            onFailure = { throwable -> "${throwable.javaClass.simpleName}: ${throwable.message}" }
        )
}

// ==================== 3. 변환 결과 되읽기 ====================

@Composable
private fun PipReadbackCard(options: PipOptions, sourceRectHint: Rect?) {
    PipSectionCard(title = "3. 변환된 파라미터 되읽기 (이 기기 실측)") {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            BodyText(
                "PictureInPictureParams 의 getter 9개는 전부 API 33 부터다(클래스 자체는 26부터). " +
                    "이 기기는 API ${Build.VERSION.SDK_INT} 라 되읽기가 불가능하다 — " +
                    "값이 안 들어간 게 아니라 확인할 수단이 없는 것이다."
            )
            return@PipSectionCard
        }

        val readback = readBackPipParams(buildPipParams(options, sourceRectHint).toPictureInPictureParams())
        BodyText(
            "2번 카드의 옵션으로 만든 compat 파라미터를 변환한 뒤, 플랫폼 객체의 getter 로 " +
                "그대로 되읽은 값이다. 스위치를 끄면 해당 줄이 null 로 바뀐다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        ExtrasRow("getter", "값", isHeader = true)
        readback.forEach { ExtrasRow(it.label, it.value) }
        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "실측 함정: actions 를 한 번도 설정하지 않아도 플랫폼 toString() 은 hasSetActions=true 로 " +
                "찍힌다. compat 이 빈 리스트를 항상 넘기기 때문이며, getActions() 는 정직하게 0개다."
        )
    }
}

// ==================== 4. API 티어 ====================

@Composable
private fun PipApiTierCard() {
    PipSectionCard(title = "4. 변환 시점에 잘려나가는 필드") {
        BodyText(
            "toPictureInPictureParams() 는 SDK_INT 를 33 → 31 → 그 외 순서로 비교해 세 갈래로 갈린다. " +
                "아래는 각 갈래가 플랫폼 Builder 에 실제로 넘기는 인자다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        ExtrasRow("API", "옮겨지는 필드", isHeader = true)
        ExtrasRow("33+", "9개 전부 (aspectRatio·actions·sourceRectHint·seamlessResize·autoEnter·expandedAspectRatio·closeAction·title·subTitle)")
        ExtrasRow("31~32", "5개 — expandedAspectRatio·closeAction·title·subTitle 소실")
        ExtrasRow("26~30", "3개 — 위 4개에 더해 seamlessResize·autoEnter 도 소실")
        ExtrasRow("~25", "PiP 없음. toPictureInPictureParams() 자체가 @RequiresApi(26)")

        Spacer(modifier = Modifier.height(10.dp))
        val tier = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> "33+ (전부 반영)"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> "31~32 (4개 소실)"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> "26~30 (6개 소실)"
            else -> "PiP 미지원"
        }
        ResultRow("이 기기(API ${Build.VERSION.SDK_INT})", tier)
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "잘린 필드는 예외를 던지지 않고 조용히 사라진다. 그래서 '설정했는데 안 보인다'는 " +
                "버그가 되기 쉬운데, 3번 카드처럼 되읽어 보면 바로 드러난다."
        )
    }
}

// ==================== 5. 종횡비 검증 ====================

@Composable
private fun PipAspectRatioCard(activity: Activity?) {
    var log by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    PipSectionCard(title = "5. 종횡비에는 허용 범위가 있다") {
        BodyText(
            "시스템은 너무 극단적인 비율을 거부한다. 아래 버튼은 그 비율로 " +
                "setPictureInPictureParams 를 실제로 호출해 결과를 그대로 보여준다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "239:100" to Rational(239, 100),
                "240:100" to Rational(240, 100),
                "4184:10000" to Rational(4184, 10000)
            ).forEach { (label, ratio) ->
                DemoButton(text = label, color = Color(0xFF546E7A)) {
                    val result = applyPipParams(
                        activity,
                        PipOptions(aspectRatio = ratio, useSourceRectHint = false),
                        null
                    )
                    log = (listOf(label to result) + log).take(4)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        if (log.isEmpty()) {
            CaptionText("버튼을 누르면 이 자리에 호출 결과가 쌓인다(최근 4건).")
        } else {
            log.forEach { (label, result) -> ResultRow(label, result) }
        }

        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "실측(SM-A725F/API 33) — 예외 메시지는 \"Aspect ratio is too extreme " +
                "(must be between 0.418410 and 2.390000)\" 이고, 2.39 는 통과하지만 2.4 는 거부된다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "⚠️ 하한은 메시지를 그대로 읽으면 안 된다. 0.418410 은 1/2.39 = 0.41841004… 를 반올림해 " +
                "찍은 값이라, 메시지와 같아 보이는 4184:10000(=0.4184)도 실제로는 미달이라 거부된다. " +
                "안전하게는 9:16(=0.5625) 처럼 여유 있는 비율을 쓴다."
        )
    }
}

// ==================== 6. 함정 정리 ====================

@Composable
private fun PipPitfallCard() {
    PipSectionCard(title = "6. 실기기에서 걸리는 것들") {
        PitfallRow(
            "매니페스트 선언이 먼저다",
            "android:supportsPictureInPicture=\"true\" 가 없으면 enterPictureInPictureMode() 가 " +
                "아무 일도 하지 않는다. 이 예제를 위해 BlogExampleActivity 에 선언을 추가했다."
        )
        PitfallRow(
            "configChanges 를 빠뜨리면 상태가 날아간다",
            "PiP 진입/복귀는 화면 크기 변경이라, screenSize|smallestScreenSize|screenLayout|orientation 을 " +
                "선언하지 않으면 액티비티가 재생성된다."
        )
        PitfallRow(
            "enter 는 resumed 를 요구하고, set 은 아니다",
            "실측: 정지 상태에서 enterPictureInPictureMode() 는 " +
                "IllegalStateException(\"Activity must be resumed to enter picture-in-picture\") 를 던지지만, " +
                "같은 상태에서 setPictureInPictureParams() 는 정상 동작했다. " +
                "그래서 autoEnter 용 파라미터는 미리 심어 둘 수 있다."
        )
        PitfallRow(
            "getter 는 API 33+",
            "PictureInPictureParams 클래스는 API 26 부터지만 getAspectRatio/getTitle 등 9개 getter는 " +
                "전부 33 부터다(SDK api-versions.xml 확인). 그 미만에서는 '넣은 값'을 되읽을 수 없다."
        )
        PitfallRow(
            "PiP 창은 아주 작다",
            "PiP 모드에서는 목록·헤더를 그대로 두면 안 된다. 이 예제는 " +
                "addOnPictureInPictureModeChangedListener 로 모드를 감지해 최소 화면으로 바꾼다."
        )
        PitfallRow(
            "⚠️ 이 사이클의 미검증 항목",
            "실제 PiP 창 진입만 자동 검증하지 못했다 — 연결된 실기기가 키가드로 잠겨 있어 액티비티가 " +
                "RESUMED 로 올라가지 못했고(그래서 위의 IllegalStateException 을 측정할 수 있었다), " +
                "대체로 띄운 API 34 에뮬레이터는 앱 APK 설치가 유지되지 않았다. 파라미터 구성·변환·되읽기·" +
                "종횡비 검증·상태 요구 차이는 모두 실기기에서 측정했다. 창 렌더는 화면을 켠 상태에서 위 " +
                "버튼을 직접 누르면 확인된다."
        )
    }
}

// ==================== 공통 요소 ====================

@Composable
private fun PipSectionCard(
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
private fun CodeText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECEFF1), RoundedCornerShape(6.dp))
            .padding(10.dp),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F),
        lineHeight = 16.sp
    )
}

@Composable
private fun ExtrasRow(
    key: String,
    value: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = key,
            modifier = Modifier.width(130.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161)
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            text = label,
            modifier = Modifier.width(110.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
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
private fun OptionSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = Color(0xFF424242)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1976D2))
        )
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
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
