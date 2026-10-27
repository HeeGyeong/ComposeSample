package com.example.composesample.presentation.example.component.ui.media.picker

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch

/**
 * 시스템 Photo Picker 실물 — PickVisualMedia / PickMultipleVisualMedia 를 실제로 띄워
 * 기기마다 어떤 피커가 뜨는지, 요청 옵션이 어떤 엑스트라로 가는지, 받은 URI 의 권한 수명과 위치 메타데이터를 본다.
 * 실험 로직은 같은 폴더 PhotoPickerLab.kt, 참고 자료는 exampleGuide.kt.
 */
@Composable
fun SystemPhotoPickerExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val lab = remember { PhotoPickerLab(context) }
    // 권한·저장한 URI 는 다른 화면·프로세스를 다녀오면 바뀌므로 돌아올 때마다 다시 읽는다
    var resumeTick by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        resumeTick++
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "시스템 Photo Picker 실물",
            onBackIconClicked = onBackEvent
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TierCard(lab, resumeTick)
            SinglePickCard(lab)
            MultiplePickCard(lab)
            LifetimeCard(lab, resumeTick)
            LocationCard(lab)
            LocationCleanupCard()
            SummaryCard()
        }
    }
}

/** 결과 한 건 — 원래 결과 + 받은 URI 를 읽어 본 내용 */
private data class PickRecord(val raw: RawResult, val inspections: List<UriInspection>)

// ==================== 1. 어떤 피커가 뜨나 ====================

@Composable
private fun TierCard(lab: PhotoPickerLab, resumeTick: Int) {
    val context = LocalContext.current
    val lines = remember(resumeTick) {
        val single = PickVisualMedia().createIntent(context, PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        buildList {
            add("API ${Build.VERSION.SDK_INT} · ${lab.extensionLines().joinToString()}")
            add("isPhotoPickerAvailable(context) = ${PickVisualMedia.isPhotoPickerAvailable(context)}")
            add("PickVisualMedia 가 고른 단계 → ${tierOf(single.action)?.label ?: single.action}")
            add("  그 인텐트의 처리 앱 → ${lab.handlerOf(single)}")
            add("ACTION_PICK_IMAGES 처리 앱 전부:")
            lab.handlersOf(Intent(ACTION_PICK_IMAGES).setType("image/*")).ifEmpty { listOf("없음") }.forEach { add("  $it") }
            add("대체 피커(시스템 앱만) → ${lab.fallbackHandler()}")
            add("문서 선택기 → ${lab.handlerOf(lab.openDocumentIntent())}")
            add("getPickImagesMaxLimit() = ${lab.pickImagesMaxLimit() ?: "없음(시스템 피커 없음)"}")
        }
    }

    ExampleCard(title = "1. 어떤 피커가 뜨나 — 폴백 사슬", accent = Color(0xFF4527A0)) {
        BodyText(
            "PickVisualMedia 는 READ_MEDIA_* 권한 없이 사용자가 고른 사진만 받는 계약이다. 피커 화면은 기기마다 다른데, " +
                "createIntent 가 아래 세 단계 중 처음 되는 것을 고른다(androidx.activity 1.13.0 소스)."
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledLine(PickerTier.SYSTEM.label, "API 33 이상, 또는 API 30~32 에 SDK 확장 R 2 이상(Google Play 시스템 업데이트)", Color(0xFF2E7D32))
        LabeledLine(
            PickerTier.SYSTEM_FALLBACK.label,
            "①이 없을 때 시스템 앱(MATCH_SYSTEM_ONLY)이 처리하는 대체 액션. 일반 앱이 등록한 것은 무시된다",
            Color(0xFFEF6C00)
        )
        LabeledLine(
            PickerTier.OPEN_DOCUMENT.label,
            "둘 다 없으면 문서 선택기 — 최대 개수·순서·탭·강조색 엑스트라를 아예 보내지 않는다",
            Color(0xFFC62828)
        )
        SubTitle("이 기기")
        lines.forEach { MonoText(it) }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "1.13.0 소스에는 GMS 피커 액션 상수(com.google.android.gms.provider.action.PICK_IMAGES)가 남아 있지만 createIntent 는 쓰지 않는다 — " +
                "단계는 셋뿐이다. 실측: Galaxy A72(API 33, 확장 22)는 ① 처리 앱이 둘이고 실제로 뜨는 것은 우선순위가 높은 새 " +
                "com.google.android.photopicker 앱, Pixel Tablet 에뮬레이터(API 35, 확장 13)는 MediaProvider 모듈 하나, " +
                "Pixel 4 에뮬레이터(API 30, 확장 R 0, Google Play 서비스 있음)는 ①②가 없어 ③ 문서 선택기. " +
                "처리 앱 이름은 매니페스트 <queries> 로 보인다 — 선언 전에는 MediaProvider 모듈만 보여 A72 에서 엉뚱한 앱을 가리켰고, " +
                "문서 선택기는 '없음' 이었다(실행은 선언 없이도 된다)."
        )
    }
}

// ==================== 2. 한 장 ====================

@Composable
private fun SinglePickCard(lab: PhotoPickerLab) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var options by remember { mutableStateOf(PickOptions()) }
    val records = remember { mutableStateListOf<PickRecord>() }
    var launchError by remember { mutableStateOf<String?>(null) }
    val contract = remember { PickVisualMedia() }
    val intent = remember(options) { runCatching { contract.createIntent(context, options.toRequest()) } }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val parsed = listOfNotNull(contract.parseResult(result.resultCode, result.data))
        val source = "한 장 · ${tierOf(intent.getOrNull()?.action)?.name ?: "?"}"
        scope.launch { records.add(0, record(lab, source, result, parsed)) }
    }

    ExampleCard(title = "2. 한 장 — PickVisualMedia", accent = Color(0xFF1565C0)) {
        BodyText("칩으로 요청을 바꾸면 contract.createIntent 가 만든 인텐트가 아래에 그대로 펼쳐진다. 실행하면 피커가 돌려준 결과 코드·URI·권한 플래그를 그대로 보인다.")
        SubTitle("미디어 종류")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MediaChoice.entries.forEach { m -> OptionChip(m.label, options.media == m) { options = options.copy(media = m) } }
        }
        SubTitle("처음 탭 · 강조색")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionChip("사진 탭", !options.albumsTab) { options = options.copy(albumsTab = false) }
            OptionChip("앨범 탭", options.albumsTab) { options = options.copy(albumsTab = true) }
            AccentChoice.entries.forEach { a -> OptionChip(a.label, options.accent == a) { options = options.copy(accent = a) } }
        }
        SubTitle("contract 가 만든 인텐트")
        intent.fold(
            onSuccess = { lab.describeIntent(it).forEach { line -> MonoText(line) } },
            onFailure = { MonoText(describeFailure(it)) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("피커 열기", enabled = intent.isSuccess) {
            launchError = launchOrError { launcher.launch(intent.getOrThrow()) }
        }
        launchError?.let { MonoText(it) }
        records.take(3).forEach { RecordBlock(it) }
        Spacer(modifier = Modifier.height(4.dp))
        CodeBlock(
            "val pick = rememberLauncherForActivityResult(PickVisualMedia()) { uri -> … }\n" +
                "pick.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))"
        )
        CaptionText(
            "한 장 계약은 maxItems·순서 엑스트라를 보내지 않는다. 탭·강조색은 문서상 API 35 / SDK 확장 R 12 부터라 그보다 낮은 피커는 무시할 수 있고, " +
                "강조색은 setAccentColor 를 부를 때만 붙는다."
        )
        CaptionText(
            "실측(Pixel Tablet API 35, 확장 13): 결과는 data 와 clipData 1개 둘 다, 권한 플래그 READ | PERSISTABLE. 표시 이름은 원래 파일명이 아니라 " +
                "'<id>.jpg' 다. 앨범 탭·GIF 만·동영상 필터가 반영됐고, 강조색은 진한 보라도 아주 밝은 노랑도 그대로 칠했다(거부 없음). " +
                "Galaxy A72 는 사용자 사진이 보이므로 열고 취소만 했다 — 취소는 RESULT_CANCELED · data null."
        )
    }
}

// ==================== 3. 여러 장 ====================

@Composable
private fun MultiplePickCard(lab: PhotoPickerLab) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val limit = remember { lab.pickImagesMaxLimit() }
    var maxChoice by remember { mutableStateOf<Int?>(3) }
    var contractMax by remember { mutableStateOf<Int?>(null) }
    var ordered by remember { mutableStateOf(false) }
    val records = remember { mutableStateListOf<PickRecord>() }
    var launchError by remember { mutableStateOf<String?>(null) }
    // 생성자 값이 null 이면 기본 생성자 — 기본값이 곧 피커 한도다
    val contract = remember(contractMax) { contractMax?.let { PickMultipleVisualMedia(it) } ?: PickMultipleVisualMedia() }
    val options = PickOptions(maxItems = maxChoice, ordered = ordered)
    val intent = remember(contract, maxChoice, ordered) { runCatching { contract.createIntent(context, options.toRequest()) } }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val parsed = contract.parseResult(result.resultCode, result.data)
        val source = "여러 장 · ${tierOf(intent.getOrNull()?.action)?.name ?: "?"}"
        scope.launch { records.add(0, record(lab, source, result, parsed)) }
    }

    ExampleCard(title = "3. 여러 장 — PickMultipleVisualMedia", accent = Color(0xFF00695C)) {
        BodyText(
            "최대 개수는 계약 생성자 값과 요청 값 중 작은 쪽이다. 시스템 피커에서는 그 값이 2 이상·getPickImagesMaxLimit() 이하여야 하고, " +
                "어기면 createIntent 가 예외를 던진다."
        )
        SubTitle("요청 maxItems")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionChip("기본값", maxChoice == null) { maxChoice = null }
            OptionChip("1", maxChoice == 1) { maxChoice = 1 }
            OptionChip("3", maxChoice == 3) { maxChoice = 3 }
            limit?.let { OptionChip("한도+1 (${it + 1})", maxChoice == it + 1) { maxChoice = it + 1 } }
        }
        SubTitle("계약 생성자 · 순서")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionChip("PickMultipleVisualMedia()", contractMax == null) { contractMax = null }
            limit?.let { OptionChip("PickMultipleVisualMedia(${it + 1})", contractMax == it + 1) { contractMax = it + 1 } }
            OptionChip("고른 순서대로", ordered) { ordered = !ordered }
        }
        SubTitle("contract 가 만든 인텐트")
        intent.fold(
            onSuccess = { lab.describeIntent(it).forEach { line -> MonoText(line) } },
            onFailure = {
                MonoText(describeFailure(it))
                CaptionText("launcher.launch(request) 안에서 createIntent 가 불리므로, 잡지 않으면 실행 버튼을 누르는 순간 앱이 죽는다.")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("피커 열기", enabled = intent.isSuccess) {
            launchError = launchOrError { launcher.launch(intent.getOrThrow()) }
        }
        launchError?.let { MonoText(it) }
        records.take(3).forEach { RecordBlock(it) }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "요청만 한도를 넘기면 기본 생성자 값(한도)과의 작은 쪽으로 조용히 잘린다 — 예외는 생성자에도 큰 값을 줬을 때, 또는 요청이 1 일 때 " +
                "(setMaxItems 의 @IntRange(from = 2) 는 상수 인자만 lint 가 잡는다). 돌려준 순서는 결과 목록 순서 그대로다."
        )
        CaptionText(
            "실측(API 35): 요청 101 + 기본 생성자 → PICK_IMAGES_MAX = 100, 생성자 101 + 요청 101·요청 1 → IllegalArgumentException. " +
                "썸네일을 3→1→2 순서로 탭하면 '고른 순서대로' 를 꺼도 켜도 결과는 탭한 순서였다 — 켜면 타일에 1·2·3 번호가 보이는 것만 다르다. " +
                "결과는 data null · clipData 3개."
        )
        CaptionText(
            "API 30 에뮬레이터(③단계)에서는 OPEN_DOCUMENT + ALLOW_MULTIPLE = true 만 가고 최대 개수·순서·탭 엑스트라는 없다. 요청 1 도 예외 없이 열린다 — " +
                "같은 코드가 기기에 따라 크래시이기도, 무시되기도 한다."
        )
    }
}

// ==================== 4. 권한 수명 ====================

@Composable
private fun LifetimeCard(lab: PhotoPickerLab, resumeTick: Int) {
    val scope = rememberCoroutineScope()
    var refresh by remember { mutableIntStateOf(0) }
    val saved = remember(resumeTick, refresh) { lab.loadSaved() }
    val persisted = remember(resumeTick, refresh) { lab.persistedSummary() }
    val reads = remember { mutableStateListOf<UriInspection>() }
    var actionResult by remember { mutableStateOf<String?>(null) }

    ExampleCard(title = "4. 받은 URI 는 언제까지 읽히나", accent = Color(0xFF6D4C41)) {
        BodyText(
            "피커가 준 읽기 권한은 임시다. 마지막으로 받은 URI 를 SharedPreferences 에 적어 두고, 앱을 죽였다 살리거나 " +
                "이 화면을 새로 연 뒤에도 읽히는지 본다. 오래 쓰려면 takePersistableUriPermission 으로 영구 권한을 받는다."
        )
        SubTitle("마지막으로 받은 URI")
        if (saved == null) {
            MonoText("없음 — 2·3·5번 카드에서 먼저 고른다")
        } else {
            MonoText("${saved.source} · ${saved.uris.size}개 · ${saved.time}")
            MonoText("받을 때 pid ${saved.pid} · 지금 pid ${Process.myPid()}${if (saved.pid == Process.myPid()) " (같은 프로세스)" else " (새 프로세스)"}")
            saved.uris.take(3).forEach { MonoText("  $it") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SmallButton("다시 읽기") {
                val uris = saved?.uris.orEmpty()
                scope.launch {
                    reads.clear()
                    uris.forEach { reads.add(lab.inspect(it)) }
                }
            }
            SmallButton("영구 권한 받기") {
                actionResult = saved?.uris?.joinToString("\n") { lab.persist(it) } ?: "받은 URI 없음"
                refresh++
            }
            SmallButton("영구 권한 놓기") {
                actionResult = "놓은 영구 권한 ${lab.releaseSaved()}개"
                refresh++
            }
            SmallButton("기록 지우기") {
                lab.releaseSaved()
                lab.clearSaved()
                reads.clear()
                actionResult = null
                refresh++
            }
        }
        actionResult?.let { MonoText(it) }
        reads.forEach { InspectionLines(it) }
        SubTitle("이 예제가 쥔 영구 URI 권한 ${persisted.first.size}개 · 다른 예제 것 ${persisted.second}개")
        persisted.first.take(5).forEach { MonoText(it) }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "실측(API 35): 같은 화면, 그리고 HOME → am kill → 다시 열어 같은 화면이 복원(새 pid)됐을 때는 읽혔다. 시스템 BACK 으로 화면을 끝내고 " +
                "새로 연 화면과 force-stop 뒤에는 SecurityException('does not have permission to access picker uri') — 연락처 피커(#152)처럼 " +
                "프로세스가 아니라 받은 액티비티에 묶인다. takePersistableUriPermission 은 성공했고 force-stop 뒤에도 읽혔으며, 놓으면 다시 SecurityException."
        )
    }
}

// ==================== 5. 위치 메타데이터 ====================

@Composable
private fun LocationCard(lab: PhotoPickerLab) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val records = remember { mutableStateListOf<PickRecord>() }
    var original by remember { mutableStateOf<String?>(null) }
    var launchError by remember { mutableStateOf<String?>(null) }
    val pickContract = remember { PickVisualMedia() }

    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val parsed = listOfNotNull(pickContract.parseResult(result.resultCode, result.data))
        scope.launch { records.add(0, record(lab, "위치 · PickVisualMedia", result, parsed)) }
    }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val parsed = listOfNotNull(result.data?.data.takeIf { result.resultCode == Activity.RESULT_OK })
        scope.launch { records.add(0, record(lab, "위치 · ACTION_OPEN_DOCUMENT", result, parsed)) }
    }

    ExampleCard(title = "5. 위치 메타데이터(EXIF GPS)", accent = Color(0xFF2E7D32)) {
        BodyText(
            "같은 사진을 시스템 피커와 문서 선택기(계약의 ③단계와 같은 인텐트)로 각각 받아 EXIF 의 GPS 태그를 읽는다. " +
                "촬영 시각·기종 태그는 대조군이다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ActionButton("시스템 피커로 받기") {
                launchError = launchOrError {
                    pickLauncher.launch(pickContract.createIntent(context, PickVisualMediaRequest(PickVisualMedia.ImageOnly)))
                }
            }
            ActionButton("문서 선택기로 받기") {
                launchError = launchOrError { documentLauncher.launch(lab.openDocumentIntent()) }
            }
        }
        launchError?.let { MonoText(it) }
        records.take(4).forEach { RecordBlock(it) }
        val latestPicker = records.firstOrNull { it.raw.source.contains("PickVisualMedia") }?.raw?.parsed?.firstOrNull()
        if (latestPicker != null) {
            Spacer(modifier = Modifier.height(4.dp))
            SmallButton("피커 URI 로 원본 요청(setRequireOriginal)") {
                scope.launch { original = lab.openOriginal(latestPicker) }
            }
            original?.let { MonoText(it) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "실측(API 35, 이 앱 미디어 권한·ACCESS_MEDIA_LOCATION 0): 같은 사진을 피커로 받으면 GPS 값과 N/E 글자가 0 으로 지워지고(태그 자리는 남음) " +
                "촬영 시각·기종은 그대로다. 문서 선택기의 최근 이미지(MediaDocumentsProvider)로 받으면 위치 37.56654, 126.97796 이 그대로 남고, " +
                "이름도 원래 파일명, 권한은 READ | WRITE | PERSISTABLE. 피커 URI 에 setRequireOriginal 은 " +
                "UnsupportedOperationException('Require Original is not supported for Picker URI')."
        )
        CaptionText(
            "photopicker 1.0.0-alpha03 의 setLocationMetadataAccessRequested(true) 는 피커가 사용자에게 위치 공유를 묻게 하는 요청이다. " +
                "SDK 확장 T 23 이상에서만 엑스트라가 붙고, 그 밖에서는 조용히 무시된다(이 기기: ${lab.extensionLines().joinToString()})."
        )
    }
}

// ==================== 7. 정리 ====================

@Composable
private fun SummaryCard() {
    ExampleCard(title = "7. 정리 — 언제 무엇을", accent = Color(0xFF37474F)) {
        BulletText("사진 몇 장을 받는 기능이면 READ_MEDIA_IMAGES 대신 PickVisualMedia — 권한 대화상자도, 부분 접근(READ_MEDIA_VISUAL_USER_SELECTED) 처리도 필요 없다")
        BulletText("'어떤 피커가 뜨나' 는 API 레벨이 아니라 SDK 확장 버전과 시스템 앱으로 정해진다 — 옵션이 지켜지는지도 피커마다 다르다")
        BulletText("여러 장 최대 개수는 생성자 값과 요청 값 중 작은 쪽 — 그 값이 1 이거나 getPickImagesMaxLimit() 를 넘으면 launch 순간 예외")
        BulletText("받은 URI 를 나중에도 쓰려면 takePersistableUriPermission, 아니면 바로 앱 저장소로 복사한다")
        BulletText("위치가 필요한 앱은 피커 URI 에서 GPS 가 지워져 오는 것을 전제로 — 사용자에게 묻는 요청(alpha03)은 지원 기기에서만")
        BulletText("지워진 위치는 '없음' 이 아니라 0 이다 — getLatLong 이 true · (0, 0) 이므로 0 위치는 위치 없음으로 판정한다(Null Island)")
        BulletText("반대로 ③단계(문서 선택기)로 떨어진 기기에서는 위치가 그대로 온다 — 올리기 전에 위치를 떼야 하는 앱은 경로와 상관없이 사본에서 직접 지운다")
        BulletText("태그 제거(saveAttributes)는 형식·API 를 탄다(HEIC 불가 · WebP 는 API 30 불가) — 안 되면 재인코딩, 재인코딩은 방향을 픽셀에 먼저 반영한다")
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("앱 화면 안에 피커를 넣는 임베디드 피커는 이 폴더의 두 시뮬레이션 예제, 파일 전반은 SAF 예제, 권한 방식은 TargetSDK34Permission 예제가 다룬다.")
    }
}

// ==================== 결과 표시 ====================

private suspend fun record(lab: PhotoPickerLab, source: String, result: ActivityResult, parsed: List<android.net.Uri>): PickRecord {
    lab.save(source, parsed)
    val raw = lab.rawResult(source, result.resultCode, result.data, parsed)
    return PickRecord(raw, parsed.map { lab.inspect(it) })
}

/** 피커가 없는 기기에서 ActivityNotFoundException 을 화면에 남긴다 */
internal fun launchOrError(block: () -> Unit): String? = try {
    block()
    null
} catch (e: ActivityNotFoundException) {
    describeFailure(e)
}

@Composable
private fun RecordBlock(record: PickRecord) {
    val raw = record.raw
    SubTitle("${raw.time} · ${raw.source}")
    MonoText("${resultCodeText(raw.resultCode)} · data ${raw.dataUri ?: "null"} · clipData ${raw.clipCount}개")
    MonoText("권한 플래그 ${grantFlagsText(raw.flags)} · parseResult → ${raw.parsed.size}개")
    record.inspections.forEachIndexed { index, inspection -> InspectionLines(inspection, index + 1) }
}

@Composable
private fun InspectionLines(inspection: UriInspection, index: Int? = null) {
    val head = index?.let { "#$it " }.orEmpty()
    MonoText("$head${inspection.uri}")
    MonoText("  ${inspection.displayName ?: "이름 ?"} · ${inspection.mime ?: "MIME ?"} · 크기 ${inspection.size ?: "?"} · 읽은 바이트 ${inspection.bytesRead ?: "-"}")
    inspection.failure?.let { MonoText("  ✗ $it") }
    if (inspection.mime?.startsWith("image/") == true && inspection.failure == null) {
        val gps = inspection.gps
        MonoText("  GPS: ${gpsState(gps).label} · ${formatLatLong(gps?.latLong)}")
        gps?.let { MonoText("  원문 ${it.latitudeRef ?: "-"} ${it.latitude ?: "-"} / ${it.longitudeRef ?: "-"} ${it.longitude ?: "-"}") }
        MonoText("  촬영 시각 ${inspection.dateTimeOriginal ?: "-"} · 기종 ${inspection.model ?: "-"}")
    }
}

// ==================== 공통 UI ====================

private fun resultCodeText(code: Int): String = when (code) {
    Activity.RESULT_OK -> "RESULT_OK"
    Activity.RESULT_CANCELED -> "RESULT_CANCELED"
    else -> "resultCode $code"
}

@Composable
internal fun ExampleCard(title: String, accent: Color, content: @Composable () -> Unit) {
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
internal fun SubTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF263238),
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label, fontSize = 12.sp) })
}

@Composable
internal fun ActionButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
internal fun SmallButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) { Text(text = label, fontSize = 12.sp) }
}

@Composable
internal fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
internal fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
}

@Composable
internal fun MonoText(text: String) {
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
internal fun CodeBlock(text: String) {
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
