package com.example.composesample.presentation.example.component.system.platform.clipboard

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.data.ProcessTextKey
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.foundation.text.contextmenu.modifier.filterTextContextMenuComponents
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/** 지연 읽기·쓰기 대기 시간 — 그 사이 알림창을 내리거나 홈으로 나가 포커스를 잃게 한다 */
private const val DELAY_MS = 5_000L

/**
 * 클립보드 접근 제한과 텍스트 처리 메뉴 — API 29 의 '포커스 없는 읽기 = null', 31 의 붙여넣기 알림, 33 의 민감 표시,
 * 그리고 텍스트 선택 메뉴에 항목을 더하는 PROCESS_TEXT 액티비티를 Compose·View 필드에서 나란히 잰다.
 * 실험 로직은 같은 폴더 ClipboardLab.kt, 메뉴 항목 액티비티는 ProcessTextDemoActivity.kt, 참고 자료는 exampleGuide.kt.
 */
@Composable
fun ClipboardProcessTextExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lab = remember {
        ClipboardLab(
            context = context,
            isFocused = { view.hasWindowFocus() },
            lifecycleState = { lifecycle.currentState.name },
        )
    }
    DisposableEffect(lab) {
        lab.start()
        onDispose {
            lab.stop()
            // 화면을 나갈 때 지금 클립이 이 예제 것이면 실험 전 클립으로 되돌린다
            lab.restore("화면 나감")
        }
    }
    // 메뉴 항목은 켜 두면 기기의 모든 앱에 보이므로 화면을 나가면 매니페스트 기본값(꺼짐)으로 되돌린다
    DisposableEffect(Unit) {
        onDispose { if (isDemoMenuEnabled(context)) setDemoMenuEnabled(context, false) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "클립보드 제한과 텍스트 처리 메뉴",
            onBackIconClicked = onBackEvent
        )
        // LazyColumn 대신 Column — AndroidView(EditText) 필드와 선택 상태가 스크롤로 재사용되지 않게
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IntroCard()
            FocusReadCard(lab)
            ListenerCard(lab)
            PasteNoticeCard()
            SensitiveCard(lab)
            RestoreCard(lab)
            ProcessTextMenuCard()
            ConnectedFieldCard()
            SummaryCard()
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun IntroCard() {
    ExampleCard(title = "1. 클립보드는 '포커스 있는 앱'만 읽는다", accent = Color(0xFF37474F)) {
        BodyText(
            "백그라운드에서 클립보드를 몰래 읽던 앱들 때문에 Android 는 버전마다 제한을 더했다. 이 화면은 그 규칙을 실제 호출로 " +
                "확인하고, 반대로 앱이 다른 앱의 선택 메뉴에 항목을 더하는 PROCESS_TEXT 를 Compose·View 필드에서 대조한다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledLine("API 29", "창 포커스가 없으면 getPrimaryClip() 은 null, 변경 리스너도 불리지 않는다", Color(0xFFC62828))
        LabeledLine("API 31", "다른 앱이 넣은 클립을 읽으면 시스템이 '붙여넣음' 알림을 띄운다", Color(0xFFEF6C00))
        LabeledLine("API 33", "ClipDescription 의 EXTRA_IS_SENSITIVE 로 시스템 복사 미리보기를 가린다", Color(0xFF6A1B9A))
        LabeledLine("API 23 · 30", "PROCESS_TEXT 메뉴 항목 · 다른 앱 처리기를 보려면 <queries>", Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "이 예제는 실험용 문자열을 클립보드에 넣는다. 처음 쓰기 직전의 클립을 보관했다가, 화면을 나갈 때 지금 클립이 " +
                "이 예제 것이면 원래대로 되돌린다(6번 카드). 다른 앱의 클립은 내용 대신 길이·MIME 만 보인다. " +
                "이 기기: API ${Build.VERSION.SDK_INT}."
        )
    }
}

// ==================== 2. 포커스 없는 읽기 ====================

@Composable
private fun FocusReadCard(lab: ClipboardLab) {
    val scope = rememberCoroutineScope()
    val windowInfo = LocalWindowInfo.current
    var readOnRefocus by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf(false) }
    val currentReadOnRefocus by rememberUpdatedState(readOnRefocus)

    // 포커스가 돌아온 순간 다시 읽기 — 백그라운드에서 못 읽은 것을 메우는 정석
    LaunchedEffect(windowInfo, lab) {
        snapshotFlow { windowInfo.isWindowFocused }
            .drop(1)
            .filter { it && currentReadOnRefocus }
            .collect { lab.read("포커스 복귀") }
    }

    ExampleCard(title = "2. 읽기 — 화면에 보여도 포커스가 없으면 null", accent = Color(0xFFC62828)) {
        BodyText(
            "'5초 뒤 읽기'를 누르고 5초 안에 알림창을 내리거나 홈으로 나가 보자. 알림창만 내려도 액티비티는 RESUMED 그대로지만 " +
                "창 포커스를 잃어 읽기가 거부된다. 거부여도 예외는 없고 null·hasPrimaryClip false 라 '비었다'와 구분되지 않는다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ActionButton("예제 문자열 복사") { lab.write("clip-${(100..999).random()}", trigger = "버튼") }
            ActionButton("지금 읽기") { lab.read("버튼") }
            ActionButton(if (pending) "5초 대기 중…" else "5초 뒤 읽기") {
                if (!pending) {
                    pending = true
                    scope.launch {
                        delay(DELAY_MS)
                        lab.read("5초 뒤")
                        pending = false
                    }
                }
            }
        }
        FilterChip(
            selected = readOnRefocus,
            onClick = { readOnRefocus = !readOnRefocus },
            label = { Text("포커스가 돌아오면 다시 읽기", fontSize = 12.sp) }
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (lab.reads.isEmpty()) CaptionText("아직 읽지 않았다.")
        lab.reads.asReversed().forEach { ReadRowView(it) }
    }
}

@Composable
private fun ReadRowView(row: ClipReadRow) {
    val color = when (row.verdict) {
        ReadVerdict.OURS -> Color(0xFF2E7D32)
        ReadVerdict.OTHER -> Color(0xFF1565C0)
        ReadVerdict.EMPTY -> Color(0xFF757575)
        ReadVerdict.DENIED -> Color(0xFFC62828)
    }
    val s = row.snapshot
    val detail = when {
        !s.clipPresent -> "hasPrimaryClip=${s.hasPrimaryClip}"
        s.ownText != null -> "\"${s.ownText}\"" + if (s.sensitive) " · 민감 표시" else ""
        else -> "${s.length}자 · ${s.mimeTypes.joinToString()}" + if (s.sensitive) " · 민감 표시" else ""
    }
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        MonoText("${row.time} ${row.trigger} · 포커스 ${if (row.focused) "있음" else "없음"} · ${row.lifecycle}")
        Text(text = "${row.verdict.label}  $detail", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, lineHeight = 17.sp)
    }
}

// ==================== 3. 변경 리스너 ====================

@Composable
private fun ListenerCard(lab: ClipboardLab) {
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf(false) }

    ExampleCard(title = "3. 변경 리스너 — 포커스가 없을 때의 변경은 영영 안 온다", accent = Color(0xFFAD1457)) {
        BodyText(
            "addPrimaryClipChangedListener 는 이 화면이 떠 있는 동안 등록돼 있다. 쓰기는 포커스 없이도 되므로 '5초 뒤 복사' 로 " +
                "포커스가 없을 때 클립을 바꿔 보면 그 쓰기는 리스너가 알아채지 못한다. 돌아와도 놓친 호출은 다시 오지 않는다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("지금 복사") { lab.write("now-${(100..999).random()}", trigger = "지금") }
            ActionButton(if (pending) "5초 대기 중…" else "5초 뒤 복사") {
                if (!pending) {
                    pending = true
                    // 보관은 포커스가 있는 지금 해 둔다 — 5초 뒤에는 원래 클립을 읽을 수 없다
                    lab.takeBackupIfNeeded()
                    scope.launch {
                        delay(DELAY_MS)
                        lab.write("later-${(100..999).random()}", trigger = "5초 뒤")
                        pending = false
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "쓴 횟수 ${lab.writeCount} · 리스너가 알아챈 쓰기 ${lab.noticedWriteCount} · 리스너 호출 ${lab.listenerCount}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (lab.writeCount == lab.noticedWriteCount) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
        lab.events.asReversed().forEach { event ->
            MonoText("${event.time} ${event.text} · 포커스 ${if (event.focused) "있음" else "없음"}")
        }
        Spacer(modifier = Modifier.height(4.dp))
        BulletText(
            "API 31+ 는 쓰기 한 번에 리스너가 두 번 온다 — 시스템이 클립 텍스트를 분류(링크·주소 감지)한 뒤 한 번 더 알린다" +
                "(기록의 '분류 미완료 → 완료'). 리스너 호출 수로 쓰기를 세면 두 배가 된다."
        )
        BulletText("다른 앱의 복사(이 앱에 포커스가 없을 때)도 오지 않는다 — 실시간 감시는 불가능하고, 포커스가 돌아올 때 다시 읽는다(2번 카드의 칩).")
        CaptionText("실측: AOSP 에뮬레이터 API 34·35 release — 포커스 있는 쓰기 1회에 리스너 2회, 알림창 아래 쓰기·설정 앱 복사는 0회.")
    }
}

// ==================== 4. 붙여넣기 알림 ====================

@Composable
private fun PasteNoticeCard() {
    ExampleCard(title = "4. 다른 앱의 클립을 읽으면 시스템이 알린다 (API 31+)", accent = Color(0xFFEF6C00)) {
        BodyText(
            "다른 앱에서 무언가를 복사한 뒤 돌아와 2번 카드의 '지금 읽기'를 누르면, getPrimaryClip() 이 불린 순간 시스템이 " +
                "'ComposeSample 에서 클립보드의 내용을 붙여넣음' 류의 알림을 띄운다. 앱은 이 알림을 막거나 감지할 수 없다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        BulletText("같은 클립을 다시 읽을 때는 뜨지 않는다 — 클립 하나에 앱마다 한 번이다.")
        BulletText("이 예제가 넣은 클립(같은 앱)을 읽을 때는 뜨지 않는다.")
        BulletText(
            "알림은 getPrimaryClip() 에서만 띄운다 — hasPrimaryClip()·getPrimaryClipDescription() 은 내용을 넘기지 않아 알림이 없으니 " +
                "붙여넣기 버튼 활성화 판단은 이것으로 한다. 기본 키보드(IME)는 알림 대상에서 빠진다."
        )
        CaptionText(
            "실측: AOSP 에뮬레이터 API 34 — 설정 앱에서 복사한 4자 클립을 읽자 \"ComposeSample pasted from your clipboard\", " +
                "곧바로 다시 읽으면 없음. 제외 조건은 AOSP ClipboardService(showAccessNotificationLocked) 기준."
        )
    }
}

// ==================== 5. 민감 표시 ====================

@Composable
private fun SensitiveCard(lab: ClipboardLab) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var note by remember { mutableStateOf<String?>(null) }

    fun copy(sensitive: Boolean) {
        lab.takeBackupIfNeeded()
        val text = if (sensitive) "OTP 482913" else "plain 482913"
        scope.launch {
            // Compose 의 ClipEntry 는 플랫폼 ClipData 를 감싼 것 — extras 도 그대로 시스템까지 간다
            clipboard.setClipEntry(ClipEntry(demoClip(text, sensitive)))
            lab.noteComposeWrite(if (sensitive) "민감" else "일반")
            note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                "API ${Build.VERSION.SDK_INT}: 복사 확인은 시스템이 보여 주므로 앱 토스트는 띄우지 않았다"
            } else {
                Toast.makeText(context, "복사됨", Toast.LENGTH_SHORT).show()
                "API ${Build.VERSION.SDK_INT}: 시스템 복사 확인이 없어 앱이 '복사됨' 토스트를 띄웠다"
            }
        }
    }

    ExampleCard(title = "5. 민감 표시 — EXTRA_IS_SENSITIVE (API 33+)", accent = Color(0xFF6A1B9A)) {
        BodyText(
            "Android 13 부터 복사하면 시스템이 화면 구석에 복사 미리보기를 띄운다. 비밀번호·OTP 처럼 가려야 할 값은 " +
                "ClipDescription extras 에 EXTRA_IS_SENSITIVE 를 실어 미리보기를 점으로 가린다. 33 미만은 같은 키를 문자열로 넣는다."
        )
        CodeBlock(
            "val clip = ClipData.newPlainText(label, text).apply {\n" +
                "    description.extras = PersistableBundle().apply {\n" +
                "        putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)  // < 33: \"android.content.extra.IS_SENSITIVE\"\n" +
                "    }\n" +
                "}\n" +
                "clipboard.setClipEntry(ClipEntry(clip))   // Compose LocalClipboard"
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("일반으로 복사") { copy(sensitive = false) }
            ActionButton("민감 표시로 복사") { copy(sensitive = true) }
            ActionButton("다시 읽기") { lab.read("민감 확인") }
        }
        note?.let { CaptionText(it) }
        CaptionText("다시 읽으면 2번 카드 기록에 '민감 표시' 가 붙는지로 extras 가 시스템을 왕복했는지 본다.")
        CaptionText(
            "실측: AOSP 에뮬레이터 API 34 — 일반 클립은 미리보기에 \"clip-819\" 가 그대로, 민감 표시 클립은 \"••••••\" 로 가려지고 " +
                "다시 읽어도 플래그가 남는다. 미리보기 모양은 제조사 UI 마다 다를 수 있다."
        )
    }
}

// ==================== 6. 되돌리기 ====================

@Composable
private fun RestoreCard(lab: ClipboardLab) {
    ExampleCard(title = "6. 실험이 끝나면 원래 클립으로", accent = Color(0xFF00695C)) {
        BodyText(
            "이 예제는 처음 쓰기 직전에 사용자의 클립을 한 번 보관한다. 되돌릴 때는 지금 클립이 이 예제 것일 때만 덮는다 — " +
                "그 사이 다른 앱에서 복사했다면 그것을 지우지 않으려고. 화면을 나갈 때도 같은 규칙으로 자동 실행된다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        ActionButton("지금 되돌리기") { lab.restore("버튼") }
        lab.restoreNote?.let { CaptionText(it) }
    }
}

// ==================== 7. 텍스트 선택 메뉴(PROCESS_TEXT) ====================

@Composable
private fun ProcessTextMenuCard() {
    val context = LocalContext.current
    var menuEnabled by remember { mutableStateOf(isDemoMenuEnabled(context)) }
    var refresh by remember { mutableIntStateOf(0) }
    val handlers = remember(menuEnabled, refresh) { processTextHandlers(context) }
    val receipts by ProcessTextLog.receipts.collectAsState()

    // 다른 앱에서 돌아올 때 목록을 다시 읽는다
    LifecycleResumeEffect(Unit) {
        menuEnabled = isDemoMenuEnabled(context)
        refresh++
        onPauseOrDispose { }
    }

    ExampleCard(title = "7. 선택 메뉴에 항목 더하기 — PROCESS_TEXT", accent = Color(0xFF1565C0)) {
        BodyText(
            "ACTION_PROCESS_TEXT(text/plain) 를 받는 exported 액티비티를 두면 다른 앱의 텍스트 선택 메뉴에 'ComposeSample 변환'이 " +
                "생긴다. 켜 두면 기기의 모든 앱에 보이므로 매니페스트는 enabled=\"false\" 이고 여기서만 켠다(화면을 나가면 꺼짐)."
        )
        Spacer(modifier = Modifier.height(6.dp))
        FilterChip(
            selected = menuEnabled,
            onClick = {
                setDemoMenuEnabled(context, !menuEnabled)
                menuEnabled = isDemoMenuEnabled(context)
                refresh++
            },
            label = { Text(if (menuEnabled) "메뉴 항목 켜짐" else "메뉴 항목 꺼짐 — 눌러서 켜기", fontSize = 12.sp) }
        )
        SubTitle("이 앱이 보는 처리 앱 (queryIntentActivities)")
        if (handlers.isEmpty()) CaptionText("없음")
        handlers.forEach { MonoText("${if (it.ours) "★" else "·"} ${it.label} — ${it.packageName}") }
        CaptionText(
            "API 30+ 는 패키지 가시성 규칙으로 매니페스트에 <queries><intent> PROCESS_TEXT 를 선언해야 다른 앱 처리기가 보인다. " +
                "이 질의 결과가 곧 Compose·View 선택 메뉴에 오르는 항목이다(자기 앱 것은 선언 없이도 보인다). " +
                "실측(Galaxy A72): 선언 전 0개 → 선언 후 사전·Gemini·Whale 3개, Compose 메뉴에도 같이 늘었다."
        )

        SubTitle("단어 하나를 길게 눌러 선택 → 메뉴에서 'ComposeSample 변환'")
        DemoFields()

        BulletText(
            "View 필드는 id 가 있어야 항목이 뜬다 — TextView.canProcessText() 가 NO_ID 면 false 다(결과를 같은 뷰로 돌려받으려는 규칙). " +
                "코드로 만든 EditText 는 id 가 없어 'id 없음' 필드처럼 메뉴에 아무것도 안 뜬다."
        )
        BulletText("읽기 전용 TextView 는 결과를 받으면 바꾸지 않고 토스트로 보여 준다.")
        BulletText(
            "메뉴 이름: Compose 메뉴는 액티비티 라벨('ComposeSample 변환'), Galaxy 의 View 메뉴는 앱 이름('ComposeSample')을 쓴다."
        )

        SubTitle("받은 기록 (ProcessTextDemoActivity)")
        if (receipts.isEmpty()) CaptionText("아직 없다.")
        receipts.asReversed().forEach { ReceiptView(it) }
        if (receipts.isNotEmpty()) {
            ActionButton("기록 지우기") { ProcessTextLog.clear() }
        }
        CaptionText(
            "실측(Galaxy A72, API 33): Compose TextField 는 READONLY=true·callingActivity=null 로 결과 미반영, SelectionContainer 는 " +
                "READONLY=false·null. View EditText(id)는 false·BlogExampleActivity 로 결과가 대문자로 바뀌고, 다른 앱(설정 검색창)도 " +
                "false·SearchActivity 로 바뀌었다. 앱을 다시 설치하거나 프로세스가 죽어도 켜진 상태는 남는다 — 이 화면을 ← 로 나갈 때 꺼진다."
        )
    }
}

@Composable
private fun DemoFields() {
    val composeState = rememberTextFieldState(DemoField.COMPOSE_FIELD.sample)
    FieldLabel(DemoField.COMPOSE_FIELD)
    OutlinedTextField(state = composeState, modifier = Modifier.fillMaxWidth())

    FieldLabel(DemoField.COMPOSE_SELECTION)
    SelectionContainer {
        Text(DemoField.COMPOSE_SELECTION.sample, fontSize = 16.sp, modifier = Modifier.padding(vertical = 6.dp))
    }

    // View 쪽은 id 가 있어야 PROCESS_TEXT 항목이 뜬다(TextView.canProcessText 가 NO_ID 면 false) —
    // 결과를 startActivityForResult 로 돌려받고, 재생성 뒤에도 같은 뷰를 찾으려는 규칙이다
    FieldLabel(DemoField.VIEW_EDIT)
    AndroidView(
        factory = { ctx ->
            EditText(ctx).apply {
                id = View.generateViewId()
                setText(DemoField.VIEW_EDIT.sample)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    FieldLabel(DemoField.VIEW_TEXT)
    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                id = View.generateViewId()
                text = DemoField.VIEW_TEXT.sample
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextIsSelectable(true)
                setPadding(0, 12, 0, 12)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    FieldLabel(DemoField.VIEW_EDIT_NO_ID)
    AndroidView(
        factory = { ctx ->
            EditText(ctx).apply {
                setText(DemoField.VIEW_EDIT_NO_ID.sample)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FieldLabel(field: DemoField) {
    Text(
        text = "${field.label} · ${if (field.editable) "편집 가능" else "읽기 전용"}",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF455A64),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ReceiptView(receipt: ProcessTextReceipt) {
    val check = checkReceipt(receipt)
    val source = check.field?.label ?: "다른 앱 또는 판별 불가"
    val readonlyText = when (check.readonlyOk) {
        null -> "READONLY=${receipt.readonlyExtra}"
        true -> "READONLY=${receipt.readonlyExtra} (맞음)"
        false -> "READONLY=${receipt.readonlyExtra} (반대 — 기대 ${check.expectedReadonly})"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        MonoText("#${receipt.id} ${receipt.time} \"${receipt.text}\" · $source")
        Text(
            text = readonlyText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (check.readonlyOk == false) Color(0xFFC62828) else Color(0xFF424242)
        )
        Text(
            text = if (check.canReturnResult) {
                "callingActivity=${receipt.callingActivity} — 결과가 돌아간다"
            } else {
                "callingActivity=null — startActivity 로 열려 결과를 받을 곳이 없다"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (check.canReturnResult) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
        MonoText("referrer=${receipt.referrer} · ${receipt.outcome}")
    }
}

// ==================== 8. Compose 필드에 결과 받기 ====================

/** 직접 붙인 메뉴 항목의 키 — 기본 PROCESS_TEXT 항목(ProcessTextKey)과 겹치지 않게 */
private data class ConnectedProcessKey(val index: Int)

@Composable
private fun ConnectedFieldCard() {
    val context = LocalContext.current
    val state = rememberTextFieldState(DemoField.CONNECTED.sample)
    var pendingRange by remember { mutableStateOf<TextRange?>(null) }
    var resultNote by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    val handlers = remember(refresh) { processTextHandlers(context) }
    LifecycleResumeEffect(Unit) {
        refresh++
        onPauseOrDispose { }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val range = pendingRange
        pendingRange = null
        val replacement = result.data?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        resultNote = if (result.resultCode == Activity.RESULT_OK && replacement != null && range != null &&
            range.max <= state.text.length
        ) {
            state.edit { replace(range.min, range.max, replacement) }
            "결과 반영: \"$replacement\""
        } else {
            "반영 안 함 — resultCode ${result.resultCode}, 결과 ${replacement?.let { "\"$it\"" } ?: "없음"}"
        }
    }

    ExampleCard(title = "8. Compose 필드에 결과를 받으려면 — 메뉴 항목을 직접 단다", accent = Color(0xFF2E7D32)) {
        BodyText(
            "Compose 기본 메뉴의 PROCESS_TEXT 항목은 startActivity 로 열어 결과가 돌아오지 않는다. 같은 필드에서 기본 항목을 " +
                "filterTextContextMenuComponents 로 빼고, appendTextContextMenuComponents 로 '처리 앱 ↩' 항목을 붙여 " +
                "ActivityResult 로 연 뒤 선택 범위를 결과로 바꾼다. READONLY 도 편집 가능 필드에 맞게 false 로 보낸다."
        )
        CodeBlock(
            "Modifier\n" +
                "    .filterTextContextMenuComponents { it.key !is ProcessTextKey }\n" +
                "    .appendTextContextMenuComponents {\n" +
                "        handlers.forEachIndexed { i, h ->\n" +
                "            item(key = ConnectedProcessKey(i), label = \"\${h.label} ↩\") {\n" +
                "                pendingRange = state.selection\n" +
                "                launcher.launch(processTextIntent(h, selected, readonly = false))\n" +
                "                close()\n" +
                "            }\n" +
                "        }\n" +
                "    }"
        )
        FieldLabel(DemoField.CONNECTED)
        OutlinedTextField(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .filterTextContextMenuComponents { it.key !is ProcessTextKey }
                .appendTextContextMenuComponents {
                    val selection = state.selection
                    if (selection.collapsed) return@appendTextContextMenuComponents
                    separator()
                    handlers.forEachIndexed { index, handler ->
                        item(key = ConnectedProcessKey(index), label = "${handler.label} ↩") {
                            val selected = state.text.substring(selection.min, selection.max)
                            pendingRange = selection
                            resultNote = try {
                                launcher.launch(processTextIntent(handler, selected, readonly = false))
                                "열림: ${handler.label} — \"$selected\""
                            } catch (e: ActivityNotFoundException) {
                                pendingRange = null
                                "열 수 없음 — ${e.javaClass.simpleName}(메뉴 항목이 그 사이 꺼졌을 수 있다)"
                            }
                            close()
                        }
                    }
                    separator()
                }
        )
        resultNote?.let { CaptionText(it) }
        CaptionText(
            "이 필드의 처리 앱 항목은 '↩' 가 붙은 것뿐이다(잘라내기·복사 같은 기본 항목은 그대로). 7번 카드에서 메뉴 항목을 켜야 " +
                "'ComposeSample 변환 ↩' 도 보인다. 실측(Galaxy A72): READONLY=false·BlogExampleActivity 로 열려 \"connected ECHO\" 로 바뀌었다."
        )
    }
}

// ==================== 9. 정리 ====================

@Composable
private fun SummaryCard() {
    ExampleCard(title = "9. 정리", accent = Color(0xFF37474F)) {
        BulletText("클립보드 읽기·변경 알림은 창 포커스가 조건이다. 백그라운드 감시는 불가능 — 포커스가 돌아올 때 다시 읽는다.")
        BulletText("null 은 '비었다'와 '거부됐다'를 구분하지 않는다. 붙여넣기 버튼은 hasPrimaryClip·description 으로 판단한다.")
        BulletText("민감한 값은 EXTRA_IS_SENSITIVE 를 싣는다. API 33+ 는 시스템이 복사를 알리므로 앱 토스트를 겹쳐 띄우지 않는다.")
        BulletText("PROCESS_TEXT 액티비티는 결과를 줘도 받는 쪽이 startActivityForResult 로 열었을 때만 전달된다.")
        BulletText("코드로 만든 View 텍스트 필드에는 id 를 준다 — id 가 없으면 선택 메뉴에 처리 앱 항목이 하나도 안 뜬다.")
        BulletText("Compose 기본 메뉴는 startActivity 로 열고 READONLY 를 편집 가능 여부 그대로 넣는다 — 결과가 필요하면 항목을 직접 단다.")
        BulletText("다른 앱 처리기를 메뉴에 띄우려면(API 30+) <queries> 에 PROCESS_TEXT 인텐트를 선언한다.")
    }
}

// ==================== 공통 UI ====================

@Composable
private fun ExampleCard(title: String, accent: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accent)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

/** 라벨·값을 한 덩어리로 — Row(라벨, 값)는 라벨이 길면 값 칸이 0 폭에 가까워져 글자가 세로로 쌓인다 */
@Composable
private fun LabeledLine(label: String, value: String, labelColor: Color) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = labelColor)) { append(label) }
            append("  ")
            append(value)
        },
        fontSize = 12.sp,
        color = Color(0xFF424242),
        lineHeight = 17.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun SubTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF263238),
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
}

@Composable
private fun MonoText(text: String) {
    Text(text = text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF616161), lineHeight = 16.sp)
}

@Composable
private fun BulletText(text: String) {
    Text(
        text = "· $text",
        fontSize = 12.sp,
        color = Color(0xFF616161),
        lineHeight = 18.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
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
