package com.example.composesample.presentation.example.component.ui.media.picker

import android.os.Build
import android.os.ext.SdkExtensions
import android.view.View
import android.widget.photopicker.EmbeddedPhotoPickerFeatureInfo
import android.widget.photopicker.EmbeddedPhotoPickerProviderFactory
import android.widget.photopicker.EmbeddedPhotoPickerSession
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.photopicker.embedded.EmbeddedPhotoPickerView
import androidx.photopicker.embedded.ExperimentalPhotoPickerApi
import androidx.photopicker.embedded.compose.EmbeddedPhotoPicker
import androidx.photopicker.embedded.compose.ExperimentalPhotoPickerComposeApi
import androidx.photopicker.embedded.compose.rememberEmbeddedPhotoPickerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/*
 * Embedded Photo Picker 실물 탭(#154) — photopicker-embedded(-compose) 1.0.0-alpha03.
 * View 판 예제는 EmbeddedPhotoPickerView(AndroidView 로 감쌈), Compose 판 예제는 EmbeddedPhotoPicker + rememberEmbeddedPhotoPickerState.
 * 조건(API 34 + U 확장 15)이 안 되는 기기에서는 조건 카드만 보인다 — 피커 클래스를 부르는 코드는 조건 분기 안의 함수에만 있다.
 */

private val Accent = Color(0xFF1976D2)
private val Body = Color(0xFF37474F)
private val Muted = Color(0xFF607D8B)
private val Good = Color(0xFF2E7D32)
private val Bad = Color(0xFFC62828)

/** 피커를 다시 놓기 전에 옛 세션이 닫히길(surfacePackage == null) 기다리는 시간 */
private const val SESSION_CLOSE_WAIT_MILLIS = 3000L

/** 앱에서 해제를 요청하고 회수 콜백을 기다리는 시간 */
private const val DESELECT_WAIT_MILLIS = 2000L
private val COLLAPSED_PICKER_HEIGHT = 260.dp
private val EXPANDED_PICKER_HEIGHT = 460.dp
private val MAX_SELECTION_OPTIONS = listOf(1, 3, 5)

/** 스크롤 안 피커 아래 여백 — 피커를 헤더 밑까지 밀어 올릴 손잡이 */
private val SCROLL_PULL_SPACE = 560.dp

/** 강조색(@ColorLong) — 피커 탭 칩 · 체크 표시에 칠해진다 */
private val PICKER_ACCENT: Long = android.graphics.Color.pack(0xFF1976D2.toInt())

/** 잰 기기 — 두 탭 카드 제목에 쓴다 */
private const val MEASURED_ON = "Pixel 8 API 36 · U 확장 17 에뮬레이터, debug"

/** Compose 판 실물 탭에서 잰 것 */
private val composeFacts = listOf(
    "누를 때마다 onUriPermissionGranted 1회 — content://media/picker/0/com.android.providers.media.photopicker/media/<id>. 받자마자 원본 바이트 그대로 읽힌다(넣은 JPEG 61,975B).",
    "앱에서 해제(deselectUri) → 약 0.5초 뒤 onUriPermissionRevoked 가 오고 state.selectedMedia 에서도 빠진다. 피커 체크가 풀리고 순서 번호를 다시 매긴다. 회수 뒤 읽기는 SecurityException.",
    "열린 세션에 넘긴 설정은 무시된다 — max 3 으로 연 뒤 1 로 바꿔도 세 번째가 골라지고 네 번째에 'Select up to 3 items'. EmbeddedPhotoPicker 의 LaunchedEffect 키가 state · provider 뿐이라 featureInfo 는 세션을 열 때 한 번 쓰인다.",
    "크래시 함정: 같은 state 로 옛 세션이 살아 있는 동안 새 EmbeddedPhotoPicker 를 만들면(key · if 분기 · 부모 이동) API 36 에서 NullPointerException(SurfaceView.clearChildSurfacePackage). 이 탭은 치우고 → surfacePackage 가 null 이 된 뒤 → 다시 놓는다(닫힘부터 다시 열림까지 0.5~1.1초).",
    "다시 열면 state.selectedMedia 가 미리 선택으로 넘어가 콜백 한 번에 세 URI 를 다시 받는다. 순서 번호는 고른 순서(1·3·2)가 아니라 id 순(1·2·3)으로 바뀐다.",
    "max 1(단일 선택)은 체크 표시를 그리지 않는다 — 누르면 권한, 같은 것을 다시 누르면 회수, 다른 것은 'Select up to 1 items'. 고른 것은 앱이 보여 줘야 한다.",
    "닫을 때 서비스가 메시지 없는 IllegalStateException 을 onError 로 보낼 때가 있다(Compose 판 9번 중 4번 · View 판 2번 중 2번, 닫기 요청 뒤 20~600ms). onError 는 state 하나에 묶여 있어 그대로 대체 UI 로 넘기면 정상 재개에서도 넘어간다.",
    "세션 오류(onError) 뒤에는 저절로 다시 열지 않는다 — runSession 은 끝나지만 LaunchedEffect 키(state · provider)가 그대로라서. 피커를 치웠다 다시 놓아야 새 세션(가짜 Provider 계측 테스트).",
    "가시성은 스스로 알리지 않는다 — 이 탭이 ON_STOP · ON_START 에 notifyVisibilityChanged 를 불러 서비스 로그 'Session visibility has changed: false · true' 를 확인했다.",
    "스크롤 안에 두면 피커(SurfaceView setZOrderOnTop)가 스크롤 영역 밖의 헤더 · 탭 위에 그대로 그려진다 — Compose clip 이 다른 서피스에 걸리지 않는다. 피커 위 드래그는 피커 그리드가 가져간다.",
    "펼침(isExpanded = true)은 Photos · Albums 탭과 '선택한 사진에만 접근' 안내를 그린다. 확장 17 은 펼친 채 시작(21)이 없어 라이브러리가 세션을 연 뒤 notifyPhotoPickerExpanded 로 보낸다."
)

/** View 판 실물 탭에서 잰 것 */
private val viewFacts = listOf(
    "setEmbeddedPhotoPickerFeatureInfo 는 세션이 없을 때만 연다 — 열린 뒤 max 1 을 넣어도 세 번째가 골라졌다. 다시 적용하려면 setProvider(같은 Provider)로 닫고 마지막 설정으로 다시 연다(약 0.9초).",
    "다시 연 세션은 선택 표시가 비어 있다 — View 판은 앞 선택을 미리 선택으로 넘기지 않는다(setPreSelectedUris 는 앱 몫). 회수 콜백은 오지 않았고, 앞 세션에서 받은 URI 는 그대로 읽혔다.",
    "view.visibility = GONE · VISIBLE 이 세션에 자동으로 전달된다(서비스 로그). 다시 보이면 피커가 그대로 돌아온다.",
    "리스너는 뷰 하나에 붙어 세션이 바뀌어도 같은 곳으로 온다 — setProvider 뒤 364~579ms 에 닫힌 세션의 IllegalStateException 이 onSessionError 로 왔다. 오류마다 세션 참조를 지우면 새 세션을 잃는다.",
    "앱 쪽 해제는 session.requestRevokeUriPermission — Compose 판 deselectUri 가 부르는 같은 메서드다(소스)."
)

// ───────────────────────── Compose 판 ─────────────────────────

/** Compose 판 예제의 '실물' 탭 */
@Composable
fun EmbeddedPickerComposeRealTab() {
    val support = remember { currentEmbeddedPickerSupport() }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= EMBEDDED_PICKER_MIN_EXTENSION
    ) {
        ComposeRealPicker(support)
    } else {
        UnsupportedRealTab(support)
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
@OptIn(ExperimentalPhotoPickerComposeApi::class)
@Composable
private fun ComposeRealPicker(support: EmbeddedPickerSupport) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lab = remember { PickerLabState(context.contentResolver, scope) }
    var maxSelection by remember { mutableIntStateOf(3) }
    var ordered by remember { mutableStateOf(true) }
    var reopenOnConfig by remember { mutableStateOf(false) }
    var pickerInScroll by remember { mutableStateOf(false) }
    var pickerShown by remember { mutableStateOf(true) }
    var completeCount by remember { mutableIntStateOf(0) }

    val state = rememberEmbeddedPhotoPickerState(
        onError = { lab.sessionError("onError", it) },
        onUriPermissionGranted = { lab.granted(it) },
        onUriPermissionRevoked = { lab.revoked(it) },
        onSelectionComplete = {
            lab.log.add(PickerEventKind.Complete)
            completeCount++
        }
    )
    // 선택 완료 콜백에서 바로 state 를 건드릴 수 없어(생성 중인 값) 횟수로 넘겨 접는다
    LaunchedEffect(completeCount) {
        if (completeCount > 0) state.isExpanded = false
    }

    // 세션 열림 · 닫힘은 surfacePackage 로 본다(열리면 값, 끝나면 null)
    LaunchedEffect(state) {
        var wasOpen = false
        snapshotFlow { state.surfacePackage != null }.distinctUntilChanged().collect { open ->
            if (open) {
                lab.sessionOpened("max $maxSelection · 순서 $ordered · 미리 선택 ${state.selectedMedia.size}")
            } else if (wasOpen) {
                lab.log.add(PickerEventKind.SessionClosed)
            }
            wasOpen = open
        }
    }

    // Compose 판은 가시성을 스스로 알리지 않는다 — 앱이 수명 주기로 알린다(View 판은 setVisibility · 창 가시성 훅이 대신)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state) {
        val observer = LifecycleEventObserver { _, event ->
            val visible = when (event) {
                Lifecycle.Event.ON_START -> true
                Lifecycle.Event.ON_STOP -> false
                else -> return@LifecycleEventObserver
            }
            state.notifyVisibilityChanged(visible)
            lab.log.add(PickerEventKind.Visibility, "$visible · 세션 ${if (state.surfacePackage != null) "있음" else "없음(무시)"}")
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    /*
     * 피커 컴포저블을 새로 놓아야 할 때(설정 다시 적용 · 자리 이동)는 먼저 치우고, 옛 세션이 닫힌 뒤(surfacePackage == null) 놓는다.
     * 같은 state 로 옛 세션이 살아 있는 동안 새 EmbeddedPhotoPicker 를 만들면(key · if 분기 · 부모 이동) alpha03 은 API 36+ 에서 앱이 죽는다 —
     * 새 SurfaceView 가 옛 surfacePackage 를 넘겨받고, 옛 SurfaceView 가 떨어지며 그 패키지를 release 하고, 세션 종료로 값이 null 이 되면
     * update 블록의 clearChildSurfacePackage()(API 36+ 분기)가 풀린 패키지의 SurfaceControl(null)을 reparent 해 NullPointerException.
     */
    fun relaunch(reason: String, change: () -> Unit) {
        scope.launch {
            lab.closeRequested()
            pickerShown = false
            val closed = withTimeoutOrNull(SESSION_CLOSE_WAIT_MILLIS) {
                snapshotFlow { state.surfacePackage }.first { it == null }
                true
            } ?: false
            change()
            if (!closed) {
                lab.log.add(PickerEventKind.Error, "옛 세션이 ${SESSION_CLOSE_WAIT_MILLIS}ms 안에 닫히지 않아 피커를 다시 놓지 않음")
                return@launch
            }
            pickerShown = true
            lab.log.add(PickerEventKind.Config, "$reason — 피커를 치우고 세션이 닫힌 뒤 다시 놓음")
        }
    }

    val featureInfo = remember(maxSelection, ordered) {
        EmbeddedPhotoPickerFeatureInfo.Builder()
            .setMaxSelectionLimit(maxSelection)
            .setOrderedSelection(ordered)
            .setMimeTypes(listOf("image/*"))
            .setAccentColor(PICKER_ACCENT)
            .build()
    }
    val pickerHeight by animateDpAsState(
        targetValue = if (state.isExpanded) EXPANDED_PICKER_HEIGHT else COLLAPSED_PICKER_HEIGHT,
        label = "pickerHeight"
    )

    // featureInfo 는 세션을 열 때 한 번만 쓰인다 — EmbeddedPhotoPicker 의 LaunchedEffect 키는 state · provider 뿐
    val picker: @Composable (Modifier) -> Unit = { modifier ->
        if (pickerShown) {
            EmbeddedPhotoPicker(state = state, modifier = modifier, embeddedPhotoPickerFeatureInfo = featureInfo)
        } else {
            Box(modifier = modifier.background(Color(0xFFECEFF1)), contentAlignment = Alignment.Center) {
                Text("옛 세션이 닫히길 기다리는 중", fontSize = 12.sp, color = Muted)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PickerSupportCard(support)
            RealCard(title = "설정 · 상태 (EmbeddedPhotoPicker)") {
                OptionChips(
                    label = "setMaxSelectionLimit",
                    options = MAX_SELECTION_OPTIONS,
                    selected = maxSelection,
                    onSelect = { max ->
                        if (reopenOnConfig) {
                            relaunch("max $max 적용") { maxSelection = max }
                        } else {
                            maxSelection = max
                            lab.log.add(PickerEventKind.Config, "max $max — 열린 세션은 옛 한도 그대로")
                        }
                    }
                )
                SwitchRow("setOrderedSelection", ordered) { on ->
                    if (reopenOnConfig) {
                        relaunch("순서 번호 $on 적용") { ordered = on }
                    } else {
                        ordered = on
                        lab.log.add(PickerEventKind.Config, "순서 번호 $on — 열린 세션은 그대로")
                    }
                }
                SwitchRow("설정을 바꾸면 세션 다시 열기(치우고 → 닫힘 확인 → 다시 놓기)", reopenOnConfig) { reopenOnConfig = it }
                SwitchRow("피커를 위 스크롤 안에 두기", pickerInScroll) { on ->
                    relaunch(if (on) "스크롤 안으로" else "스크롤 밖으로") { pickerInScroll = on }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            state.isExpanded = !state.isExpanded
                            lab.log.add(PickerEventKind.Expanded, "isExpanded = ${state.isExpanded}")
                        },
                        modifier = Modifier.testTag("pickerExpandButton")
                    ) { Text(if (state.isExpanded) "접기" else "펼치기", fontSize = 12.sp) }
                }
                StatLine(
                    "세션 열림 ${lab.sessionsOpened}회 · state.selectedMedia ${state.selectedMedia.size}개 · " +
                        "받은 URI ${lab.items.size}개 · isExpanded ${state.isExpanded} · 진짜 오류 ${lab.realErrors}"
                )
            }
            PickedItemsCard(lab) { key ->
                val uri = lab.uriOf(key) ?: return@PickedItemsCard
                scope.launch {
                    lab.deselectRequested(key)
                    runCatching { state.deselectUri(uri) }
                        .onFailure { lab.log.add(PickerEventKind.Error, "deselectUri: ${it::class.java.simpleName}") }
                    delay(DESELECT_WAIT_MILLIS)
                    if (lab.unanswered(DESELECT_WAIT_MILLIS).any { it.key == key }) {
                        lab.log.add(PickerEventKind.AppDeselect, "${DESELECT_WAIT_MILLIS}ms 동안 회수 콜백 없음 · $key")
                    }
                }
            }
            EventLogCard(lab.log)
            FactsCard(composeFacts)
            if (pickerInScroll) {
                picker(
                    Modifier
                        .fillMaxWidth()
                        .height(pickerHeight)
                )
                // 피커 위에서 시작한 드래그는 피커(다른 프로세스 창)가 가져간다 — 아래 여백을 잡고 밀어 피커를 위 헤더 밑까지 올린다
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SCROLL_PULL_SPACE)
                        .background(Color(0xFFECEFF1)),
                    contentAlignment = Alignment.Center
                ) { Text("여기를 잡고 위로 밀기", fontSize = 12.sp, color = Muted) }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (!pickerInScroll) {
            picker(
                Modifier
                    .fillMaxWidth()
                    .height(pickerHeight)
                    .testTag("composeRealPicker")
            )
        }
    }
}

// ───────────────────────── View 판 ─────────────────────────

/** View 판 예제의 '실물' 탭 */
@Composable
fun EmbeddedPhotoPickerViewRealTab() {
    val support = remember { currentEmbeddedPickerSupport() }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= EMBEDDED_PICKER_MIN_EXTENSION
    ) {
        ViewRealPicker(support)
    } else {
        UnsupportedRealTab(support)
    }
}

/** AndroidView 가 만든 뷰를 버튼에서 쓰려고 담아 두는 곳(스냅숏 상태 아님 — factory 안에서 상태를 쓰지 않는다) */
@OptIn(ExperimentalPhotoPickerApi::class)
private class PickerViewRef {
    var view: EmbeddedPhotoPickerView? = null
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
@OptIn(ExperimentalPhotoPickerApi::class)
@Composable
private fun ViewRealPicker(support: EmbeddedPickerSupport) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lab = remember { PickerLabState(context.contentResolver, scope) }
    val ref = remember { PickerViewRef() }
    val provider = remember { EmbeddedPhotoPickerProviderFactory.create(context.applicationContext) }
    var maxSelection by remember { mutableIntStateOf(3) }
    var session by remember { mutableStateOf<EmbeddedPhotoPickerSession?>(null) }
    var hidden by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    fun featureInfo(max: Int): EmbeddedPhotoPickerFeatureInfo =
        EmbeddedPhotoPickerFeatureInfo.Builder()
            .setMaxSelectionLimit(max)
            .setOrderedSelection(true)
            .setMimeTypes(listOf("image/*"))
            .setAccentColor(PICKER_ACCENT)
            .build()

    val listener = remember(lab) {
        object : EmbeddedPhotoPickerView.EmbeddedPhotoPickerStateChangeListener {
            override fun onSessionOpened(newSession: EmbeddedPhotoPickerSession) {
                session = newSession
                lab.sessionOpened("max $maxSelection")
            }

            override fun onSessionError(throwable: Throwable) {
                // 리스너는 세션마다가 아니라 뷰 하나에 붙는다 — 닫은 세션의 오류로 새 세션 참조를 지우지 않는다
                val closing = lab.sessionError("onSessionError", throwable)
                if (!closing) session = null
            }

            override fun onUriPermissionGranted(uris: List<android.net.Uri>) = lab.granted(uris)
            override fun onUriPermissionRevoked(uris: List<android.net.Uri>) = lab.revoked(uris)
            override fun onSelectionComplete() = lab.log.add(PickerEventKind.Complete)
        }
    }
    val pickerHeight by animateDpAsState(
        targetValue = if (expanded) EXPANDED_PICKER_HEIGHT else COLLAPSED_PICKER_HEIGHT,
        label = "viewPickerHeight"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PickerSupportCard(support)
            RealCard(title = "설정 · 상태 (EmbeddedPhotoPickerView)") {
                OptionChips(
                    label = "setEmbeddedPhotoPickerFeatureInfo(max)",
                    options = MAX_SELECTION_OPTIONS,
                    selected = maxSelection,
                    onSelect = { max ->
                        maxSelection = max
                        ref.view?.setEmbeddedPhotoPickerFeatureInfo(featureInfo(max))
                        lab.log.add(PickerEventKind.Config, "setEmbeddedPhotoPickerFeatureInfo(max $max) · 세션 ${if (session != null) "열린 채" else "없음"}")
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        lab.closeRequested()
                        ref.view?.setProvider(provider)
                        session = null
                        lab.log.add(PickerEventKind.Config, "setProvider 다시 — 열린 세션을 닫고 마지막 설정으로 연다")
                    }) { Text("setProvider 다시", fontSize = 12.sp) }
                    OutlinedButton(onClick = {
                        hidden = !hidden
                        ref.view?.visibility = if (hidden) View.GONE else View.VISIBLE
                        lab.log.add(PickerEventKind.Visibility, "view.visibility = ${if (hidden) "GONE" else "VISIBLE"} → 세션에 자동 전달")
                    }) { Text(if (hidden) "보이기" else "숨기기", fontSize = 12.sp) }
                    OutlinedButton(onClick = {
                        expanded = !expanded
                        session?.notifyPhotoPickerExpanded(expanded)
                        lab.log.add(PickerEventKind.Expanded, "notifyPhotoPickerExpanded($expanded)${if (session == null) " · 세션 없음" else ""}")
                    }) { Text(if (expanded) "접기" else "펼치기", fontSize = 12.sp) }
                }
                StatLine("세션 열림 ${lab.sessionsOpened}회 · 받은 URI ${lab.items.size}개 · 설정 max $maxSelection")
            }
            PickedItemsCard(lab) { key ->
                val uri = lab.uriOf(key) ?: return@PickedItemsCard
                val current = session
                lab.deselectRequested(key)
                if (current == null) {
                    lab.log.add(PickerEventKind.Error, "세션 없음 — requestRevokeUriPermission 못 부름")
                } else {
                    current.requestRevokeUriPermission(listOf(uri))
                    scope.launch {
                        delay(DESELECT_WAIT_MILLIS)
                        if (lab.unanswered(DESELECT_WAIT_MILLIS).any { it.key == key }) {
                            lab.log.add(PickerEventKind.AppDeselect, "${DESELECT_WAIT_MILLIS}ms 동안 회수 콜백 없음 · $key")
                        }
                    }
                }
            }
            EventLogCard(lab.log)
            FactsCard(viewFacts)
            Spacer(modifier = Modifier.height(8.dp))
        }
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(pickerHeight)
                .testTag("viewRealPicker"),
            factory = { viewContext ->
                EmbeddedPhotoPickerView(viewContext).apply {
                    addEmbeddedPhotoPickerStateChangeListener(listener)
                    // Provider 가 먼저 — 설정을 넣는 순간 세션을 열려고 해 Provider 가 없으면 IllegalStateException
                    setProvider(provider)
                    setEmbeddedPhotoPickerFeatureInfo(featureInfo(maxSelection))
                }.also { ref.view = it }
            },
            onRelease = { view ->
                view.removeEmbeddedPhotoPickerStateChangeListener(listener)
                ref.view = null
            }
        )
    }
}

// ───────────────────────── 공용 카드 ─────────────────────────

/** 조건이 안 되는 기기 — 숫자와 이유만 보인다 */
@Composable
private fun UnsupportedRealTab(support: EmbeddedPickerSupport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PickerSupportCard(support)
        RealCard(title = "이 기기에서는 실물을 띄울 수 없다") {
            Note(
                "피커 클래스(android.widget.photopicker.*)는 API 34 이상이면서 U 확장 15 이상인 기기에만 있다. " +
                    "이 탭은 조건 분기 밖에서 그 클래스를 부르지 않으므로 여기서도 크래시 없이 열린다. 다른 탭의 시뮬레이션은 어느 기기에서나 같은 흐름을 보여 준다."
            )
        }
    }
}

@Composable
private fun PickerSupportCard(support: EmbeddedPickerSupport) {
    RealCard(title = "이 기기 조건 — ${if (support.available) "실물 사용 가능" else "실물 불가"}") {
        Text(
            support.reason,
            fontSize = 12.sp,
            color = if (support.available) Good else Bad,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        PickerFeatureLevel.entries.forEach { level ->
            Row(modifier = Modifier.padding(vertical = 1.dp)) {
                Text(
                    if (support.supports(level)) "✅" else "—",
                    fontSize = 11.sp,
                    modifier = Modifier.width(22.dp)
                )
                Text("확장 ${level.extension}+ · ${level.label}", fontSize = 11.sp, color = Body, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun PickedItemsCard(lab: PickerLabState, onDeselect: (String) -> Unit) {
    RealCard(title = "받은 URI (콜백 순서)") {
        if (lab.items.isEmpty()) {
            Note("아래 피커에서 사진을 누르면 onUriPermissionGranted 로 URI 가 들어온다")
        } else {
            Text(
                "모두 다시 읽기",
                fontSize = 11.sp,
                color = Accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Accent, RoundedCornerShape(6.dp))
                    .clickable { lab.rereadAll() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        lab.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val thumb = lab.thumbnails[item.key]
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    if (thumb != null) {
                        Image(bitmap = thumb, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${shortUri(item.key)} · ${if (item.selected) "선택됨" else "회수됨"}" + if (item.grantCount > 1) " · 받음 ${item.grantCount}회" else "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.selected) Body else Muted
                    )
                    Text(
                        "받을 때 ${item.readAtGrant?.summary() ?: "읽는 중"}" +
                            (item.readAfterRevoke?.let { " · 회수 뒤 ${it.summary()}" } ?: "") +
                            (item.latestRead?.let { " · 다시 읽기 ${it.summary()}" } ?: "") +
                            (item.revokeLatencyMillis?.let { " · 해제 요청 뒤 ${it}ms 에 회수" } ?: ""),
                        fontSize = 10.sp,
                        color = Muted
                    )
                }
                if (item.selected) {
                    Text(
                        "앱에서 해제",
                        fontSize = 11.sp,
                        color = Accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Accent, RoundedCornerShape(6.dp))
                            .clickable { onDeselect(item.key) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventLogCard(log: PickerEventLog) {
    RealCard(title = "이벤트 기록 (최근이 위)") {
        if (log.events.isEmpty()) Note("아직 없음")
        log.events.take(14).forEach { e ->
            Text(
                "+${e.atMillis}ms ${e.kind.label}${if (e.detail.isNotEmpty()) " · ${shortenUris(e.detail)}" else ""}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = if (e.kind == PickerEventKind.Error) Bad else Body,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun FactsCard(facts: List<String>) {
    RealCard(title = "잰 것 ($MEASURED_ON)") {
        facts.forEachIndexed { i, fact ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("${i + 1}.", fontSize = 11.sp, color = Accent, modifier = Modifier.width(20.dp))
                Text(fact, fontSize = 11.sp, color = Body, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun RealCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Accent)
            content()
        }
    }
}

@Composable
private fun OptionChips(label: String, options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 11.sp, color = Body, modifier = Modifier.weight(1f))
        options.forEach { option ->
            val isSelected = option == selected
            Text(
                "$option",
                fontSize = 12.sp,
                color = if (isSelected) Color.White else Accent,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Accent else Color(0xFFE3F2FD))
                    .clickable { onSelect(option) }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, color = Body, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun StatLine(text: String) {
    Text(text, fontSize = 11.sp, color = Body, fontFamily = FontFamily.Monospace, lineHeight = 15.sp)
}

@Composable
private fun Note(text: String) {
    Text(text, fontSize = 11.sp, color = Muted, lineHeight = 16.sp)
}
