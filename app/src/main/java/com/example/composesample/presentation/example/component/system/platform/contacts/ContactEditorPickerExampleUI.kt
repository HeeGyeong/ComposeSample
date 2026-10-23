package com.example.composesample.presentation.example.component.system.platform.contacts

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.ContactsContract
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
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.composesample.presentation.MainHeader

/**
 * 연락처 쓰기는 편집기 인텐트로 · 읽기는 Contact Picker 로 — 다음 Android 릴리스를 타깃하면 WRITE_CONTACTS 직접 쓰기가
 * 거부된다. 같은 추가·수정을 직접 쓰기와 편집기 인텐트로 나란히 하고, 읽기는 API 37 Contact Picker 와 레거시 ACTION_PICK 을 대조한다.
 * 실험 로직은 같은 폴더 ContactsLab.kt, 참고 자료는 exampleGuide.kt.
 */
@Composable
fun ContactEditorPickerExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lab = remember { ContactsLab(context, scope) }
    // 권한은 설정 화면·다른 앱에서 바뀔 수 있어 돌아올 때마다 다시 읽는다
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
            title = "연락처 편집기 인텐트와 Contact Picker",
            onBackIconClicked = onBackEvent
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IntroCard(lab, resumeTick)
            DirectWriteCard(lab, resumeTick)
            InsertEditorCard(lab)
            EditExistingCard(lab)
            PickerCard(lab)
            DemoContactsCard(lab, resumeTick)
            MigrationCard(lab)
            SummaryCard()
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun IntroCard(lab: ContactsLab, resumeTick: Int) {
    val context = LocalContext.current
    val device = remember(resumeTick) {
        buildList {
            add("API ${Build.VERSION.SDK_INT} · 이 앱 targetSdk ${context.applicationInfo.targetSdkVersion}")
            add("편집기(ACTION_INSERT) → ${lab.handlerOf(Intent(Intent.ACTION_INSERT).setType(ContactsContract.Contacts.CONTENT_TYPE))}")
            add("피커(ACTION_PICK) → ${lab.handlerOf(ActivityResultContracts.PickContact().createIntent(context, null))}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                add("ACTION_PICK_CONTACTS → ${lab.handlerOf(lab.sessionPickerIntent(listOf(), false, 1, false, false))}")
            }
            add("매니페스트가 이 기기에서 요청하는 연락처 권한: ${lab.requestedContactPermissions().ifEmpty { listOf("없음") }.joinToString()}")
            add(
                "허용 상태: WRITE_CONTACTS ${yesNo(lab.granted(Manifest.permission.WRITE_CONTACTS))} · " +
                    "READ_CONTACTS ${yesNo(lab.granted(Manifest.permission.READ_CONTACTS))}"
            )
        }
    }

    ExampleCard(title = "1. 연락처 쓰기는 편집기로, 읽기는 피커로", accent = Color(0xFF37474F)) {
        BodyText(
            "WRITE_CONTACTS 는 사용자 확인 없이, 백그라운드에서도 주소록 전체에 쓸 수 있는 넓은 권한이다. Android 는 이 쓰기를 " +
                "연락처 앱의 편집기(사용자가 보고 저장)로 옮기고, 읽기는 사용자가 고른 연락처만 받는 Contact Picker 로 옮기는 중이다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledLine("다음 릴리스(2027)", "이 버전을 타깃한 앱은 WRITE_CONTACTS 로 쓰면 거부된다 — 추가·수정은 편집기 인텐트(권한 없음)로", Color(0xFFC62828))
        LabeledLine("Android 17 QPR2", "MANAGE_CONTACTS 특수 앱 접근 — 동기화·백업·스팸 차단처럼 연락처 관리가 본업인 앱만", Color(0xFFEF6C00))
        LabeledLine("Android 17 (API 37)", "Contact Picker — READ_CONTACTS 없이, 고른 연락처의 요청한 필드만 읽는다", Color(0xFF1565C0))
        SubTitle("이 기기")
        device.forEach { MonoText(it) }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "이 예제가 만드는 연락처는 이름에 '$DEMO_MARK' 가 들어 있다. 확인 조회·삭제는 그 연락처만 다루고, 다른 연락처는 개수만 센다. " +
                "처리 앱 이름은 매니페스트 <queries> 에 같은 인텐트를 선언해서 보인다(API 30+ 패키지 가시성)."
        )
    }
}

// ==================== 2. 직접 쓰기 (Before) ====================

@Composable
private fun DirectWriteCard(lab: ContactsLab, resumeTick: Int) {
    var requestNote by remember { mutableStateOf<String?>(null) }
    val writeGranted = remember(resumeTick, requestNote) { lab.granted(Manifest.permission.WRITE_CONTACTS) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        requestNote = "${nowText()} 요청 결과: ${if (granted) "허용" else "거부"}"
    }
    val requestable = writeRequestable(Build.VERSION.SDK_INT)

    ExampleCard(title = "2. Before — WRITE_CONTACTS 로 직접 쓰기", accent = Color(0xFFC62828)) {
        BodyText(
            "원문의 Before 코드다. RawContacts 한 행과, back-reference 로 그 행에 묶은 Data 두 행(이름·전화)을 applyBatch 한 번에 넣는다. " +
                "화면도 사용자 확인도 없다 — 그래서 다음 릴리스를 타깃하면 권한이 있어도 거부된다."
        )
        CodeBlock(
            "// Requires android.permission.WRITE_CONTACTS\n" +
                "val ops = arrayListOf(\n" +
                "    newInsert(RawContacts.CONTENT_URI).withValue(ACCOUNT_TYPE, …).build(),\n" +
                "    newInsert(Data.CONTENT_URI).withValueBackReference(Data.RAW_CONTACT_ID, 0)\n" +
                "        .withValue(MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)…,\n" +
                "    newInsert(Data.CONTENT_URI).withValueBackReference(Data.RAW_CONTACT_ID, 0)\n" +
                "        .withValue(MIMETYPE, Phone.CONTENT_ITEM_TYPE)…\n" +
                ")\n" +
                "contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)"
        )
        MonoText("WRITE_CONTACTS 허용 ${yesNo(writeGranted)} · 이 기기에서 요청 가능 ${yesNo(requestable)}")
        if (!requestable) {
            CaptionText(
                "이 기기(API ${Build.VERSION.SDK_INT})는 매니페스트의 maxSdkVersion=$WRITE_CONTACTS_MAX_SDK 보다 높아 WRITE_CONTACTS 가 요청 목록에 없다. " +
                    "다음 릴리스를 타깃한 앱이 겪을 거부를 이 선언으로 미리 본다(7번 카드)."
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("WRITE_CONTACTS 요청") { launcher.launch(Manifest.permission.WRITE_CONTACTS) }
            ActionButton("직접 저장") { lab.writeDirectly() }
        }
        requestNote?.let { CaptionText(it) }
        CaptionText(
            "실측: API 35 에뮬레이터 — 허용하면 저장은 화면 없이 곧바로 끝난다(결과 3개, raw_contacts/N). 같은 연락처 그룹이라 " +
                "그 뒤 READ_CONTACTS 요청은 대화상자 없이 허용됐다 — 쓰기를 허용받으면 읽기도 사실상 함께 열린다. " +
                "API 37(이 예제의 maxSdkVersion=$WRITE_CONTACTS_MAX_SDK 를 넘음) — 요청은 대화상자 없이 거부, 저장은 SecurityException" +
                "('Permission Denial: writing … raw_contacts'). 권한이 하나도 없으면 'opening provider … requires READ_CONTACTS or WRITE_CONTACTS'."
        )
        lab.directRows.asReversed().forEach { row ->
            Column(modifier = Modifier.padding(vertical = 3.dp)) {
                MonoText("#${row.id} ${row.time} \"${row.name}\" · 권한 ${yesNo(row.permissionGranted)}")
                Text(
                    text = (if (row.ok) "저장됨 · " else "실패 · ") + row.detail,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (row.ok) Color(0xFF2E7D32) else Color(0xFFC62828),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ==================== 3. 편집기로 새 연락처 ====================

@Composable
private fun InsertEditorCard(lab: ContactsLab) {
    var finishOnSave by remember { mutableStateOf(false) }
    var withWebsite by remember { mutableStateOf(true) }
    var withCustom by remember { mutableStateOf(true) }
    var withPhoto by remember { mutableStateOf(true) }
    val launch = rememberEditorLauncher(lab)

    ExampleCard(title = "3. After — 편집기 인텐트로 새 연락처 (ACTION_INSERT)", accent = Color(0xFF2E7D32)) {
        BodyText(
            "원문의 After 코드: Intent(ACTION_INSERT, Contacts.CONTENT_URI) 에 Insert.NAME·PHONE 을 넣는다. 권한이 필요 없다 — 저장은 " +
                "연락처 앱이 사용자에게 보여 주고 한다. 이 예제는 결과를 보려고 ActivityResult 로 열고, 저장된 연락처를 다시 읽어 " +
                "넣어 보낸 값이 남았는지 대조한다(6번 카드의 READ_CONTACTS 필요)."
        )
        CodeBlock(
            "Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {\n" +
                "    putExtra(Insert.NAME, name)\n" +
                "    putExtra(Insert.PHONE, phone)\n" +
                "    putExtra(\"finishActivityOnSaveCompleted\", true)        // 칩\n" +
                "    putParcelableArrayListExtra(Insert.DATA, arrayListOf(  // 칩\n" +
                "        websiteRow, customMimeRow, photoRow))\n" +
                "}"
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionChip("finishActivityOnSaveCompleted", finishOnSave) { finishOnSave = !finishOnSave }
            OptionChip("웹사이트 행(표준)", withWebsite) { withWebsite = !withWebsite }
            OptionChip("표준 밖 MIME 행", withCustom) { withCustom = !withCustom }
            OptionChip("사진 행", withPhoto) { withPhoto = !withPhoto }
        }
        ActionButton("편집기 열기 (ACTION_INSERT)") {
            val id = lab.newId()
            launch(
                EditorRequest(
                    id = id,
                    action = EditorAction.INSERT,
                    name = demoName("편집기", id),
                    phone = demoPhone(id),
                    withWebsite = withWebsite,
                    withCustomMime = withCustom,
                    withPhoto = withPhoto,
                    finishOnSave = finishOnSave,
                )
            )
        }
        lab.editorRows.filter { it.request.action == EditorAction.INSERT }.asReversed().forEach { EditorRowView(lab, it) }
        Spacer(modifier = Modifier.height(4.dp))
        BulletText(
            "Google 연락처(API 35·37 에뮬레이터): 반 시트 'Add to contacts' 로 열리고 Save 를 누르면 곧바로 이 앱으로 돌아온다 — " +
                "finishActivityOnSaveCompleted 유무와 상관없다. 결과는 RESULT_OK + 연락처 lookup URI(읽기 권한 부여 플래그 없음)."
        )
        BulletText(
            "그런데 연락처 앱을 처음 쓴 직후(앱 데이터를 지운 직후도) 첫 저장은 RESULT_CANCELED·data 없음인데 실제로는 저장돼 있었다 " +
                "— 결과 코드로 저장 여부를 판단하지 않는다. BACK 은 대화상자 없이 닫히고 저장하지 않는다(RESULT_CANCELED)."
        )
        BulletText(
            "넣어 보낸 행: 이름·전화·웹사이트(표준 행)는 남고, 표준 밖 MIME 행과 사진 행은 편집기에 보이지도 않고 조용히 사라졌다. " +
                "이름 'CS152 편집기 N' 은 성 'CS152'·이름 '편집기 N' 으로 나뉘어 표시 이름이 'CS152편집기 N' 이 된다. 저장 계정은 기기(계정 없음)."
        )
        BulletText(
            "Samsung 연락처(Galaxy A72, API 33): 전체 화면 편집기가 먼저 '연락처를 저장할 위치' 계정 선택을 띄우고, 이름은 나누지 않으며 " +
                "사진 행을 편집기에 보여 줬다(실기기라 저장은 하지 않고 취소 → RESULT_CANCELED). 같은 인텐트도 편집기마다 다르다."
        )
    }
}

/** 편집기 인텐트를 ActivityResult 로 여는 런처 — 요청을 기억해 두었다가 결과와 짝짓는다 */
@Composable
private fun rememberEditorLauncher(lab: ContactsLab): (EditorRequest) -> Unit {
    var pending by remember { mutableStateOf<EditorRequest?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val req = pending ?: return@rememberLauncherForActivityResult
        pending = null
        lab.onEditorResult(req, result.resultCode, result.data)
    }
    return { req ->
        val intent = lab.editorIntent(req, lab.editTarget?.uri)
        try {
            pending = req
            launcher.launch(intent)
            lab.onEditorLaunched(req, lab.handlerOf(intent))
        } catch (e: ActivityNotFoundException) {
            pending = null
            lab.onEditorLaunchFailed(req, e)
        }
    }
}

@Composable
private fun EditorRowView(lab: ContactsLab, row: EditorRow) {
    val req = row.request
    val options = listOfNotNull(
        "finish".takeIf { req.finishOnSave },
        "웹사이트".takeIf { req.withWebsite },
        "MIME".takeIf { req.withCustomMime },
        "사진".takeIf { req.withPhoto },
    ).ifEmpty { listOf("추가 행 없음") }.joinToString("·")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        MonoText("#${req.id} ${row.time} ${req.action.label} · $options")
        MonoText("→ ${row.handler}")
        row.resultCode?.let { code ->
            Text(
                text = "${row.returnedAt} 돌아옴 · ${resultCodeText(code)} · data ${row.resultUri ?: "없음"}" +
                    if (row.grantRead) " · 읽기 권한 부여 플래그" else "",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (code == android.app.Activity.RESULT_OK) Color(0xFF2E7D32) else Color(0xFF757575),
                lineHeight = 17.sp
            )
        }
        row.savedTo?.let { MonoText("저장 위치: $it") }
        row.checks?.forEach { check ->
            Text(
                text = "${check.outcome.mark}  ${check.label}",
                fontSize = 12.sp,
                color = when (check.outcome) {
                    FieldOutcome.KEPT -> Color(0xFF2E7D32)
                    FieldOutcome.DROPPED -> Color(0xFFC62828)
                    FieldOutcome.NOT_SENT -> Color(0xFF9E9E9E)
                },
                lineHeight = 16.sp
            )
        }
        row.note?.let { CaptionText(it) }
        if (row.resultCode != null) {
            SmallButton("다시 확인") { lab.verifyEditor(req) }
        }
    }
}

// ==================== 4. 기존 연락처에 더하기 ====================

@Composable
private fun EditExistingCard(lab: ContactsLab) {
    var finishOnSave by remember { mutableStateOf(false) }
    val launch = rememberEditorLauncher(lab)
    val target = lab.editTarget

    fun request(action: EditorAction): EditorRequest {
        val id = lab.newId()
        return EditorRequest(
            id = id, action = action, name = null, phone = demoPhone(id),
            withWebsite = false, withCustomMime = false, withPhoto = false, finishOnSave = finishOnSave,
        )
    }

    ExampleCard(title = "4. 기존 연락처에 더하기 — ACTION_EDIT · ACTION_INSERT_OR_EDIT", accent = Color(0xFF00695C)) {
        BodyText(
            "원문은 Data.CONTENT_URI 에 전화번호 행을 직접 넣던 코드를 두 인텐트로 바꾼다. 어느 연락처인지 알면 ACTION_EDIT(연락처 URI), " +
                "모르면 ACTION_INSERT_OR_EDIT(data 없이 타입만) — 편집기가 '새로 만들기 / 기존 연락처 고르기' 를 보여 준다."
        )
        CodeBlock(
            "Intent(Intent.ACTION_EDIT, contactUri).putExtra(Insert.PHONE, phone)\n" +
                "Intent(Intent.ACTION_INSERT_OR_EDIT).apply {\n" +
                "    type = Contacts.CONTENT_ITEM_TYPE\n" +
                "    putExtra(Insert.PHONE, phone)\n" +
                "}"
        )
        MonoText(
            target?.let { "ACTION_EDIT 대상: ${it.source}\n${it.uri}" }
                ?: "ACTION_EDIT 대상 없음 — 3번 결과 URI · 5번 피커 결과 · 6번 목록에서 정한다"
        )
        OptionChip("finishActivityOnSaveCompleted", finishOnSave) { finishOnSave = !finishOnSave }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("ACTION_EDIT + 전화번호", enabled = target != null) { launch(request(EditorAction.EDIT)) }
            ActionButton("ACTION_INSERT_OR_EDIT + 전화번호") { launch(request(EditorAction.INSERT_OR_EDIT)) }
        }
        lab.editorRows.filter { it.request.action != EditorAction.INSERT }.asReversed().forEach { EditorRowView(lab, it) }
        Spacer(modifier = Modifier.height(4.dp))
        BulletText(
            "Google 연락처(API 35·37): ACTION_EDIT 은 'Add to existing contact' 시트에 대상 이름과 새 번호만 보여 주고, Save 하면 " +
                "기존 연락처에 번호가 하나 더 붙는다(RESULT_OK + 같은 lookup URI). ACTION_INSERT_OR_EDIT 은 연락처 검색 시트 — " +
                "고른 연락처에 번호가 붙고 그 연락처의 URI 가 돌아온다."
        )
        BulletText(
            "권한 없이 이어지는 경로: 5번 Contact Picker 로 고른 연락처의 LOOKUP_KEY → lookup URI → ACTION_EDIT 으로 번호를 붙였다" +
                "(READ·WRITE 모두 없음, API 37). 읽기는 피커, 쓰기는 편집기."
        )
        BulletText("Samsung 연락처(API 33): ACTION_INSERT_OR_EDIT 은 연락처 선택 화면으로 열리고, BACK 은 RESULT_CANCELED.")
    }
}

// ==================== 5. 피커로 읽기 ====================

@Composable
private fun PickerCard(lab: ContactsLab) {
    val context = LocalContext.current
    val fieldState = remember { mutableStateOf(setOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) }
    var multiple by remember { mutableStateOf(false) }
    var matchAll by remember { mutableStateOf(false) }
    var useSystemExtra by remember { mutableStateOf(true) }
    var pending by remember { mutableStateOf<Pair<Int, PickMode>?>(null) }
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val (id, _) = pending ?: return@rememberLauncherForActivityResult
        pending = null
        lab.onPickResult(id, result.resultCode, result.data)
        result.data?.data?.let { lab.rememberPicked(it) }
    }

    fun open(mode: PickMode, intent: Intent) {
        val id = lab.newId()
        try {
            pending = id to mode
            launcher.launch(intent)
            lab.onPickLaunched(id, mode, lab.handlerOf(intent))
        } catch (e: ActivityNotFoundException) {
            pending = null
            lab.onPickLaunchFailed(id, mode, e)
        }
    }

    ExampleCard(title = "5. 읽기 — Contact Picker 와 레거시 ACTION_PICK", accent = Color(0xFF1565C0)) {
        BodyText(
            "Contact Picker(API 37)는 필요한 필드(전화·이메일·주소)를 MIME 으로 요청하고, 사용자가 고른 것만 세션 URI 하나로 돌려준다. " +
                "READ_CONTACTS 가 필요 없고, 받은 URI 는 결과를 받은 화면이 살아 있는 동안만 읽힌다. 37 미만은 레거시 ACTION_PICK(PickContact)."
        )
        CodeBlock(
            "Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS).apply {\n" +
                "    putExtra(Intent.EXTRA_USE_SYSTEM_CONTACTS_PICKER, true)          // 칩\n" +
                "    putStringArrayListExtra(EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,\n" +
                "        arrayListOf(Phone.CONTENT_ITEM_TYPE, Email.CONTENT_ITEM_TYPE))\n" +
                "    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)                      // 칩\n" +
                "    putExtra(EXTRA_PICK_CONTACTS_SELECTION_LIMIT, 3)\n" +
                "    putExtra(EXTRA_PICK_CONTACTS_MATCH_ALL_DATA_FIELDS, false)      // 칩\n" +
                "}\n" +
                "// 결과: data = 세션 URI → contentResolver.query(uri, projection, null, null, null)"
        )
        SubTitle("요청할 필드")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ContactsLab.PICK_FIELDS.forEach { (label, mime) ->
                OptionChip(label, mime in fieldState.value) {
                    fieldState.value = if (mime in fieldState.value) fieldState.value - mime else fieldState.value + mime
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionChip("여러 명(최대 3)", multiple) { multiple = !multiple }
            OptionChip("모든 필드 가진 연락처만", matchAll) { matchAll = !matchAll }
            OptionChip("USE_SYSTEM_CONTACTS_PICKER", useSystemExtra) { useSystemExtra = !useSystemExtra }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ActionButton("Contact Picker", enabled = supported) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                    val fields = ContactsLab.PICK_FIELDS.map { it.second }.filter { it in fieldState.value }
                    open(PickMode.SESSION, lab.sessionPickerIntent(fields, multiple, 3, matchAll, useSystemExtra))
                }
            }
            ActionButton("ACTION_PICK + 새 피커", enabled = supported) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                    open(PickMode.PICK_SYSTEM, lab.pickWithSystemPickerIntent(ActivityResultContracts.PickContact().createIntent(context, null)))
                }
            }
            ActionButton("레거시 PickContact") {
                open(PickMode.LEGACY, ActivityResultContracts.PickContact().createIntent(context, null))
            }
        }
        if (!supported) CaptionText("이 기기(API ${Build.VERSION.SDK_INT})에는 Contact Picker 가 없다 — 레거시 ACTION_PICK 만 연다.")

        lab.pickRows.asReversed().forEach { PickRowView(lab, it) }

        SubTitle("실측 — API 37 에뮬레이터, targetSdk 35 인 이 앱")
        BulletText(
            "새 피커는 반 시트로 '고른 연락처의 정보만 이 앱이 본다' 를 알린다. 요청 필드를 가진 연락처만 보이고, 한 명 모드에서도 " +
                "고른 뒤 Done 을 눌러야 한다. 전화가 둘인 연락처는 펼쳐져 번호 하나를 고른다."
        )
        BulletText(
            "결과는 content://0@com.android.contacts.picker.sessions/sessions/<uuid> — authority 앞에 사용자 id 가 붙는다(authority 비교는 " +
                "빗나간다) — 와 읽기 권한 부여 플래그. 행은 필드 하나당 하나라 전화·이메일을 다 고른 사람은 2행(3명에 4행) → LOOKUP_KEY 로 묶는다."
        )
        BulletText(
            "여러 명(최대 3)에서 4번째는 'Can share only 3 contacts at a time'. '모든 필드 가진 연락처만' 을 켜면 전화·이메일을 다 가진 1명만 남는다."
        )
        BulletText(
            "selection 을 넣으면 UnsupportedOperationException('Querying Session Uri with selection or selection arguments is not allowed.'). " +
                "투영 없이 읽으면 열 81개에 계정 열은 0개 — 계정 정보는 지워져 온다."
        )
        BulletText(
            "USE_SYSTEM_CONTACTS_PICKER 를 빼고 ACTION_PICK_CONTACTS 를 보내면 '이 작업을 처리할 수 있는 앱이 없음' 선택 화면이 뜨고 RESULT_CANCELED — " +
                "예외도 없고 resolveActivity 는 시스템 피커를 가리킨다. targetSdk 37 미만 앱에는 이 엑스트라가 필수다."
        )
        BulletText(
            "ACTION_PICK(PickContact) 은 targetSdk 35 앱에는 여전히 연락처 앱 피커('Choose a contact')로 열린다. USE_SYSTEM_CONTACTS_PICKER 를 " +
                "붙이면 새 피커 화면이지만 결과는 세션 URI 가 아니라 연락처 lookup URI(content://0@com.android.contacts/…)다."
        )
        BulletText(
            "레거시 결과는 연락처 표 한 행이라 전화·이메일이 없고, 그 URI 의 하위 data 경로는 READ_CONTACTS 없이 SecurityException — " +
                "임시 권한은 받은 URI 하나에만 걸린다(API 35 에서도 같음)."
        )
        BulletText(
            "Samsung 연락처(API 33): 레거시 피커를 BACK 으로 닫으면 resultCode 1100 — RESULT_CANCELED(0) 가 아니다(2회 같음). " +
                "결과는 'RESULT_OK 인가' 로 가른다."
        )

        lab.savedPick?.let { saved ->
            SubTitle("받은 URI 를 나중에 다시 조회")
            MonoText("받은 시각 ${saved.time} · 받은 프로세스 pid ${saved.pid} · 지금 pid ${Process.myPid()}\n${saved.uri}")
            SmallButton("적어 둔 URI 다시 조회") {
                val id = lab.newId()
                lab.onPickLaunched(id, if (ContactsLab.isSessionUri(saved.uri)) PickMode.SESSION else PickMode.LEGACY, "적어 둔 URI")
                lab.queryPicked(id, saved.uri)
            }
            CaptionText(
                "실측(세션·레거시 URI 모두, API 35·37, debug·release 같음): 같은 화면이면 프로세스를 죽였다 살려도(am kill → 최근 앱 복원, 새 pid) " +
                    "읽혔고, 같은 프로세스라도 화면을 끝내고 새로 연 화면이나 force-stop 뒤에는 SecurityException 이었다. 임시 권한은 " +
                    "프로세스가 아니라 결과를 받은 액티비티에 묶여 있다 — 필요한 값은 받자마자 저장한다(공식 문서 권고)."
            )
        }
    }
}

@Composable
private fun PickRowView(lab: ContactsLab, row: PickRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        MonoText("#${row.id} ${row.time} ${row.mode.label}")
        MonoText("→ ${row.handler}")
        row.resultCode?.let { code ->
            Text(
                text = "${resultCodeText(code)} · data ${row.uri ?: "없음"}" + if (row.grantRead) " · 읽기 권한 부여 플래그" else "",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (code == android.app.Activity.RESULT_OK) Color(0xFF2E7D32) else Color(0xFF757575),
                lineHeight = 17.sp
            )
        }
        row.contacts?.let { contacts ->
            if (contacts.isEmpty()) CaptionText("고른 연락처 없음")
            contacts.forEach { c ->
                val parts = listOfNotNull(
                    c.phones.takeIf { it.isNotEmpty() }?.let { "전화 ${it.joinToString()}" },
                    c.emails.takeIf { it.isNotEmpty() }?.let { "이메일 ${it.joinToString()}" },
                    c.others.takeIf { it.isNotEmpty() }?.let { "기타 ${it.joinToString()}" },
                )
                Text(
                    text = "· ${c.name.ifEmpty { "(이름 없음)" }}  ${parts.joinToString(" · ").ifEmpty { "전화·이메일 값 없음" }}",
                    fontSize = 12.sp,
                    color = Color(0xFF263238),
                    lineHeight = 17.sp
                )
            }
        }
        row.lines.forEach { MonoText(it) }
        val uri = row.uri?.toUri()
        if (uri != null) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallButton("selection 넣어 조회") { lab.queryPicked(row.id, uri, withSelection = true) }
                SmallButton("열 전체 보기") { lab.inspectColumns(row.id, uri) }
                if (!ContactsLab.isSessionUri(uri)) {
                    SmallButton("하위 data 조회") { lab.queryLegacyDataDirectory(row.id, uri) }
                }
                pickedLookupUri(row)?.let { lookupUri ->
                    SmallButton("편집 대상으로") { lab.editTarget = EditTarget(lookupUri, "피커 결과 #${row.id}") }
                }
            }
        }
    }
}

/** 편집 대상 URI — 세션 결과는 첫 연락처의 LOOKUP_KEY 로, 레거시 결과는 받은 연락처 URI 그대로 */
private fun pickedLookupUri(row: PickRow): android.net.Uri? {
    val uri = row.uri?.toUri() ?: return null
    if (!ContactsLab.isSessionUri(uri)) return uri
    val key = row.contacts?.firstOrNull()?.lookupKey?.takeIf { !it.startsWith("name:") } ?: return null
    return android.net.Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, key)
}

// ==================== 6. 확인용 읽기와 정리 ====================

@Composable
private fun DemoContactsCard(lab: ContactsLab, resumeTick: Int) {
    var requestNote by remember { mutableStateOf<String?>(null) }
    val readGranted = remember(resumeTick, requestNote) { lab.granted(Manifest.permission.READ_CONTACTS) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        requestNote = "${nowText()} 요청 결과: ${if (granted) "허용" else "거부"}"
        if (granted) lab.refreshDemoContacts()
    }

    ExampleCard(title = "6. 확인용 읽기 — READ_CONTACTS 는 주소록 전체를 연다", accent = Color(0xFF6A1B9A)) {
        BodyText(
            "편집기에 넣어 보낸 값이 정말 저장됐는지는 다시 읽어 봐야 안다. 피커 없이 읽으려면 READ_CONTACTS 인데, 이 권한 하나로 " +
                "주소록 전체가 열린다. 이 예제는 이름에 '$DEMO_MARK' 가 든 예제 연락처만 읽고 나머지는 개수만 센다."
        )
        MonoText("READ_CONTACTS 허용 ${yesNo(readGranted)}")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ActionButton("READ_CONTACTS 요청") { launcher.launch(Manifest.permission.READ_CONTACTS) }
            ActionButton("예제 연락처 읽기") { lab.refreshDemoContacts() }
            ActionButton("예제 연락처 지우기") { lab.deleteDemoContacts() }
        }
        requestNote?.let { CaptionText(it) }
        lab.demoNote?.let { CaptionText(it) }
        lab.demoContacts.forEach { c ->
            Column(modifier = Modifier.padding(vertical = 3.dp)) {
                MonoText("${c.name} · 계정 ${c.account}")
                MonoText("  행: ${c.mimes.joinToString()}")
                c.lookupKey?.let { key ->
                    SmallButton("편집 대상으로") {
                        lab.editTarget = EditTarget(
                            android.net.Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, key),
                            "목록 — ${c.name}"
                        )
                    }
                }
            }
        }
        CaptionText("지우기는 쓰기라 WRITE_CONTACTS 가 필요하다. 연락처를 지우는 인텐트는 없다(원문) — 권한이 없으면 연락처 앱에서 직접 지운다.")
        CaptionText(
            "실측: API 37 의 READ_CONTACTS 대화상자는 '이 기기의 연락처와 계정에 액세스' — 연락처를 저장할 수 있는 계정 목록까지 보게 된다고 알린다. " +
                "API 35 는 '연락처에 액세스'."
        )
    }
}

// ==================== 7. 이전 요령 ====================

@Composable
private fun MigrationCard(lab: ContactsLab) {
    val requested = remember { lab.requestedContactPermissions() }
    ExampleCard(title = "7. 옮기는 순서와 maxSdkVersion", accent = Color(0xFFEF6C00)) {
        BodyText(
            "편집기 인텐트는 예전 버전에서도 기기의 연락처 앱이 처리하므로 대부분의 앱은 모든 기기에서 인텐트만 쓰면 된다. " +
                "옛 기기 편집기의 문제(사진 누락 등)를 피해야 한다면 WRITE_CONTACTS 를 maxSdkVersion 으로 좁혀 남긴다(원문)."
        )
        CodeBlock(
            "<uses-permission android:name=\"android.permission.READ_CONTACTS\" />\n" +
                "<!-- 이 예제: 36 까지만 요청 → API 37 기기에서 '쓰기 권한 없음' 을 미리 본다 -->\n" +
                "<uses-permission\n" +
                "    android:name=\"android.permission.WRITE_CONTACTS\"\n" +
                "    android:maxSdkVersion=\"$WRITE_CONTACTS_MAX_SDK\" />"
        )
        MonoText("이 기기(API ${Build.VERSION.SDK_INT})의 요청 목록: ${requested.ifEmpty { listOf("없음") }.joinToString()}")
        CaptionText("실측: API 37 에뮬레이터는 READ_CONTACTS 만(dumpsys package 도 같음), API 33·35 는 둘 다. 37 에서 WRITE 요청은 대화상자 없이 거부된다.")
        Spacer(modifier = Modifier.height(4.dp))
        BulletText("찾을 것: WRITE_CONTACTS · RawContacts.CONTENT_URI · Data.CONTENT_URI · applyBatch(ContactsContract.AUTHORITY, …)")
        BulletText("사용자가 하는 쓰기는 ACTION_INSERT · ACTION_EDIT · ACTION_INSERT_OR_EDIT 로. SHOW_OR_CREATE_CONTACT 는 새 시스템 편집기가 받지 않는다.")
        BulletText("Insert.DATA 에는 CommonDataKinds 표준 MIME 만 — 표준 밖 MIME 행은 조용히 버려진다(3번 카드에서 확인).")
        BulletText("사진은 다음 릴리스의 Insert.EXTRA_PHOTO_URI(+ clipData · FLAG_GRANT_READ_URI_PERMISSION) — SDK 37 에는 아직 없다.")
        BulletText("삭제 인텐트는 없다. 대량 수정·삭제가 본업이면 MANAGE_CONTACTS(17 QPR2, 설정 > 특수 앱 접근) + Play 신고 — SDK 37 에는 아직 없다.")
        BulletText("읽기는 Contact Picker 로 — 필요한 필드만 요청하고, 세션 URI 로 읽은 값은 받자마자 저장한다(5번 카드 아래).")
    }
}

// ==================== 8. 정리 ====================

@Composable
private fun SummaryCard() {
    ExampleCard(title = "8. 정리", accent = Color(0xFF37474F)) {
        BulletText("WRITE_CONTACTS 직접 쓰기는 다음 릴리스 타깃부터 거부된다 — 사용자 확인 없는 넓은 권한이라서다.")
        BulletText("추가·수정은 편집기 인텐트로. 권한이 없고, 저장은 사용자가 편집기에서 확인한다.")
        BulletText("편집기 동작(계정 선택·이름 나누기·사진 행)은 연락처 앱마다 다르다 — 표준 필드만 넣는다.")
        BulletText("결과 코드로 저장 여부를 판단하지 않는다 — 저장했는데 RESULT_CANCELED, 취소했는데 1100 이 실제로 왔다.")
        BulletText("읽기는 Contact Picker(API 37) — 고른 연락처의 요청 필드만, 받은 액티비티가 살아 있는 동안 읽힌다. selection 은 넣지 않는다.")
        BulletText("targetSdk 37 미만 앱은 ACTION_PICK_CONTACTS 에 USE_SYSTEM_CONTACTS_PICKER 를 꼭 붙인다 — 없으면 처리 앱이 없다.")
        BulletText("옛 기기 문제 때문에 WRITE_CONTACTS 를 남겨야 하면 maxSdkVersion 으로 좁힌다.")
    }
}

// ==================== 공통 UI ====================

private fun yesNo(value: Boolean) = if (value) "예" else "아니오"

private fun resultCodeText(code: Int): String = when (code) {
    android.app.Activity.RESULT_OK -> "RESULT_OK"
    android.app.Activity.RESULT_CANCELED -> "RESULT_CANCELED"
    else -> "resultCode $code"
}

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
private fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label, fontSize = 12.sp) })
}

@Composable
private fun ActionButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun SmallButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) { Text(text = label, fontSize = 12.sp) }
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
