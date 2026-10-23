package com.example.composesample.presentation.example.component.system.platform.contacts

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Photo
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import android.provider.ContactsPickerSessionContract
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * 이 예제가 만드는 연락처 이름의 표식 — 확인 조회·정리는 이 표식이 든 연락처만 다룬다.
 * '시작한다' 로 가르지 않는다: 연락처 provider 가 이름을 성·이름으로 나눠 표시 이름을 다시 만들며 공백을 바꾼다
 * ("CS152 편집기 2" → "CS152편집기 2" — 편집기로 저장한 것도, applyBatch 로 직접 넣은 것도 같다).
 */
internal const val DEMO_MARK = "CS152"

/** 표준 밖 MIME — 원문: 편집기 인텐트의 Insert.DATA 에 넣은 표준 밖 MIME 행은 조용히 버려진다 */
internal const val CUSTOM_MIME = "vnd.android.cursor.item/vnd.com.example.composesample.demo"

/** 대조군으로 넣는 표준 행(Website)의 값 */
internal const val DEMO_WEBSITE = "https://example.com/cs152"

/** 매니페스트의 WRITE_CONTACTS maxSdkVersion — AndroidManifest.xml 과 같은 값 */
internal const val WRITE_CONTACTS_MAX_SDK = 36

/** 공식 문서의 내비게이션 우회 엑스트라(API 15+) — 상수가 없어 문자열 그대로 쓴다 */
internal const val EXTRA_FINISH_ON_SAVE = "finishActivityOnSaveCompleted"

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)

internal fun nowText(): String = timeFormat.format(Date())

// ==================== 순수 로직 (단위 테스트 대상) ====================

fun demoName(kind: String, n: Int): String = "$DEMO_MARK $kind $n"

fun isDemoName(name: String?): Boolean = name?.contains(DEMO_MARK) == true

/** 이름 비교는 공백을 빼고 — 표시 이름은 성·이름 사이 공백이 바뀌어 저장된다 */
fun sameName(a: String?, b: String?): Boolean =
    a != null && b != null && a.filterNot { it.isWhitespace() } == b.filterNot { it.isWhitespace() }

/** 예제 전화번호 — 매칭은 숫자만 비교한다(편집기가 하이픈·공백을 바꿔 저장하기 때문) */
fun demoPhone(n: Int): String = "010-1520-%04d".format(Locale.ROOT, n % 10_000)

fun digitsOf(phone: String?): String = phone.orEmpty().filter { it.isDigit() }

/**
 * 이 기기에서 WRITE_CONTACTS 를 요청할 수 있는지. 매니페스트 maxSdkVersion 보다 높은 기기에서는 그 권한이
 * 요청 목록에서 아예 빠진다 — 다음 릴리스를 타깃한 앱이 겪을 '쓰기 거부'를 이 예제는 이 선언으로 미리 재현한다.
 */
fun writeRequestable(deviceSdk: Int, maxSdk: Int = WRITE_CONTACTS_MAX_SDK): Boolean = deviceSdk <= maxSdk

enum class EditorAction(val label: String) {
    INSERT("ACTION_INSERT"),
    EDIT("ACTION_EDIT"),
    INSERT_OR_EDIT("ACTION_INSERT_OR_EDIT"),
}

/** 편집기에 넘긴 값 — 저장 뒤 실제로 남았는지 대조하는 기준 */
data class EditorRequest(
    val id: Int,
    val action: EditorAction,
    /** ACTION_INSERT 에서만 미리 채운다 */
    val name: String?,
    val phone: String,
    val withWebsite: Boolean,
    val withCustomMime: Boolean,
    val withPhoto: Boolean,
    val finishOnSave: Boolean,
)

/** 확인 조회로 읽은 data 행 하나(ContactsContract.Data 뷰) */
data class DataRow(
    val contactId: Long,
    val lookupKey: String?,
    val displayName: String?,
    val mime: String,
    val data1: String?,
    val hasBlob: Boolean,
    val accountType: String?,
    val accountName: String?,
)

enum class FieldOutcome(val mark: String) {
    KEPT("✓ 남음"),
    DROPPED("✗ 사라짐"),
    NOT_SENT("— 안 보냄"),
}

data class FieldCheck(val label: String, val outcome: FieldOutcome)

/**
 * 편집기에 넣어 보낸 값이 저장된 연락처의 data 행에 남았는지 항목별로 가른다.
 * rows 는 '그 요청이 만든(또는 고친) 연락처 하나'의 행이어야 한다.
 */
fun checkFields(req: EditorRequest, rows: List<DataRow>): List<FieldCheck> {
    fun has(mime: String, match: (DataRow) -> Boolean = { true }) = rows.any { it.mime == mime && match(it) }
    fun outcome(sent: Boolean, kept: Boolean) = when {
        !sent -> FieldOutcome.NOT_SENT
        kept -> FieldOutcome.KEPT
        else -> FieldOutcome.DROPPED
    }
    return listOf(
        FieldCheck(
            "이름",
            outcome(req.name != null, has(StructuredName.CONTENT_ITEM_TYPE) { sameName(it.data1, req.name) || sameName(it.displayName, req.name) })
        ),
        FieldCheck("전화번호", outcome(true, has(Phone.CONTENT_ITEM_TYPE) { digitsOf(it.data1) == digitsOf(req.phone) })),
        FieldCheck("웹사이트(표준 행, 대조군)", outcome(req.withWebsite, has(Website.CONTENT_ITEM_TYPE) { it.data1 == DEMO_WEBSITE })),
        FieldCheck("표준 밖 MIME 행", outcome(req.withCustomMime, has(CUSTOM_MIME))),
        FieldCheck("사진 행", outcome(req.withPhoto, has(Photo.CONTENT_ITEM_TYPE) { it.hasBlob })),
    )
}

/** 피커 결과 커서의 한 행 — 공식 문서 예제의 투영(LOOKUP_KEY · DISPLAY_NAME_PRIMARY · MIMETYPE · DATA1) */
data class PickedRow(val lookupKey: String?, val name: String?, val mime: String?, val data1: String?)

data class PickedContact(
    val lookupKey: String,
    val name: String,
    val phones: List<String>,
    val emails: List<String>,
    val others: List<String>,
)

/**
 * 같은 사람의 여러 행을 LOOKUP_KEY 로 묶는다(공식 문서 권장). 처음 나온 순서를 지킨다.
 * LOOKUP_KEY 가 없는 행(연락처 표 행만 있는 레거시 결과)은 이름으로 대신 묶는다.
 */
fun groupPicked(rows: List<PickedRow>): List<PickedContact> {
    val order = LinkedHashMap<String, PickedContact>()
    rows.forEach { row ->
        val key = row.lookupKey ?: "name:${row.name}"
        val prev = order[key] ?: PickedContact(key, row.name.orEmpty(), emptyList(), emptyList(), emptyList())
        val value = row.data1
        order[key] = when {
            value == null -> prev
            row.mime == Phone.CONTENT_ITEM_TYPE -> prev.copy(phones = prev.phones + value)
            row.mime == Email.CONTENT_ITEM_TYPE -> prev.copy(emails = prev.emails + value)
            row.mime == StructuredName.CONTENT_ITEM_TYPE -> prev
            else -> prev.copy(others = prev.others + value)
        }
    }
    return order.values.toList()
}

enum class FailureKind(val label: String) {
    SECURITY("SecurityException — 권한 없음"),
    ILLEGAL_ARGUMENT("IllegalArgumentException"),
    UNSUPPORTED("UnsupportedOperationException"),
    OTHER("그 밖의 예외"),
}

/**
 * 예외를 원인 사슬로 가른다 — debug(HotSwan 재작성)는 checked 예외를 UndeclaredThrowableException 으로
 * 감쌀 수 있어(ComposeHotReloadGuide) 바깥 타입만 보면 틀린다.
 */
fun classify(t: Throwable): FailureKind {
    val chain = generateSequence(t) { it.cause }.take(8).toList()
    return when {
        chain.any { it is SecurityException } -> FailureKind.SECURITY
        chain.any { it is UnsupportedOperationException } -> FailureKind.UNSUPPORTED
        chain.any { it is IllegalArgumentException } -> FailureKind.ILLEGAL_ARGUMENT
        else -> FailureKind.OTHER
    }
}

/**
 * '타입: 메시지 첫 줄'. 감싸기만 하는 예외(HotSwan 의 UndeclaredThrowableException 등)는 건너뛴다.
 * 그 안쪽 원인(RemoteException 의 원격 스택)까지는 내려가지 않는다 — 시스템이 붙인 메시지가 바깥 예외에 있다.
 */
fun describeFailure(t: Throwable): String {
    val shown = generateSequence(t) { it.cause }.take(8).firstOrNull {
        it !is java.lang.reflect.UndeclaredThrowableException && it !is java.lang.reflect.InvocationTargetException
    } ?: t
    val message = shown.message?.substringBefore('\n')?.take(140)?.let { ": $it" }.orEmpty()
    return "${shown.javaClass.simpleName}$message"
}

// ==================== 기록 행 ====================

data class DirectWriteRow(
    val id: Int,
    val time: String,
    val name: String,
    val permissionGranted: Boolean,
    val ok: Boolean,
    val detail: String,
)

data class EditorRow(
    val request: EditorRequest,
    val time: String,
    val handler: String,
    val resultCode: Int? = null,
    val resultUri: String? = null,
    val returnedAt: String? = null,
    val grantRead: Boolean = false,
    val checks: List<FieldCheck>? = null,
    val savedTo: String? = null,
    val note: String? = null,
)

enum class PickMode(val label: String) {
    SESSION("ACTION_PICK_CONTACTS"),
    PICK_SYSTEM("ACTION_PICK + USE_SYSTEM_CONTACTS_PICKER"),
    LEGACY("ACTION_PICK (PickContact)"),
}

data class PickRow(
    val id: Int,
    val time: String,
    val mode: PickMode,
    val handler: String,
    val resultCode: Int? = null,
    val uri: String? = null,
    val grantRead: Boolean = false,
    val contacts: List<PickedContact>? = null,
    val lines: List<String> = emptyList(),
)

/** 확인 조회로 묶은 예제 연락처 하나 */
data class DemoContact(val contactId: Long, val lookupKey: String?, val name: String, val account: String, val mimes: List<String>)

// ==================== 실험 상태 홀더 ====================

/**
 * 연락처 실험 상태 — 직접 쓰기 · 편집기 인텐트 결과 · 피커 결과 조회를 맡는다.
 * 조회·쓰기는 IO 스레드, 화면 상태 쓰기는 메인 스레드에서 한다. 화면은 이 객체를 remember 로 들고 있다.
 */
class ContactsLab(private val context: Context, private val scope: CoroutineScope) {
    private val resolver = context.contentResolver
    // 번호는 이름·전화번호에 그대로 쓰여 확인 조회의 열쇠가 된다 — 프로세스가 다시 떠도 겹치지 않게 시작값을 무작위로
    private val nextId = AtomicInteger((100..899).random() * 10)

    val directRows = mutableStateListOf<DirectWriteRow>()
    val editorRows = mutableStateListOf<EditorRow>()
    val pickRows = mutableStateListOf<PickRow>()
    val demoContacts = mutableStateListOf<DemoContact>()

    /** ACTION_EDIT 대상 — 편집기 결과 URI · 피커 LOOKUP_KEY · 확인 목록에서 정한다 */
    var editTarget by mutableStateOf<EditTarget?>(null)
    var demoNote by mutableStateOf<String?>(null)

    fun newId(): Int = nextId.getAndIncrement()

    // -------------------- 받은 URI 기록 --------------------

    /**
     * 마지막으로 받은 피커 URI — 프로세스·액티비티가 바뀐 뒤에도 같은 URI 로 다시 조회해 보려고 SharedPreferences 에 적는다
     * (rememberSaveable 은 같은 액티비티가 복원될 때만 남아 '새 액티비티에서 조회' 를 시험할 수 없다).
     */
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    var savedPick by mutableStateOf(loadSavedPick())
        private set

    fun rememberPicked(uri: Uri) {
        prefs.edit {
            putString(KEY_URI, uri.toString())
            putInt(KEY_PID, Process.myPid())
            putString(KEY_TIME, nowText())
        }
        savedPick = loadSavedPick()
    }

    private fun loadSavedPick(): SavedPick? = prefs.getString(KEY_URI, null)?.let {
        SavedPick(it.toUri(), prefs.getInt(KEY_PID, 0), prefs.getString(KEY_TIME, null).orEmpty())
    }

    fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /** 이 기기에서 매니페스트가 실제로 요청하는 연락처 권한 — maxSdkVersion 을 넘은 기기에서는 빠진다 */
    fun requestedContactPermissions(): List<String> {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        }
        return info.requestedPermissions.orEmpty().filter { it.contains("CONTACTS") }.map { it.substringAfterLast('.') }
    }

    /** 이 인텐트를 처리할 액티비티 — 매니페스트 <queries> 에 같은 인텐트를 선언해야 보인다(API 30+) */
    fun handlerOf(intent: Intent): String {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        } ?: return "처리 앱 없음(또는 <queries> 미선언)"
        val a = info.activityInfo
        return "${a.packageName}/${a.name.substringAfterLast('.')}"
    }

    // -------------------- ① 직접 쓰기 --------------------

    /** 원문 Before 코드 — RawContacts 한 행 + back-reference 로 묶은 Data 두 행을 한 번에 넣는다 */
    fun writeDirectly() {
        val id = newId()
        val name = demoName("직접", id)
        val phone = demoPhone(id)
        val permission = granted(Manifest.permission.WRITE_CONTACTS)
        scope.launch {
            val row = withContext(Dispatchers.IO) {
                try {
                    val ops = arrayListOf(
                        ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                            // 계정 없음(null) = 기기 로컬 연락처
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build(),
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(StructuredName.DISPLAY_NAME, name)
                            .build(),
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                            .withValue(Phone.NUMBER, phone)
                            .build(),
                    )
                    val results = resolver.applyBatch(ContactsContract.AUTHORITY, ops)
                    DirectWriteRow(
                        id, nowText(), name, permission, ok = true,
                        detail = "결과 ${results.size}개 · 첫 결과 ${results.firstOrNull()?.uri}"
                    )
                } catch (t: Throwable) {
                    DirectWriteRow(id, nowText(), name, permission, ok = false, detail = "${classify(t).label} · ${describeFailure(t)}")
                }
            }
            directRows.add(row)
        }
    }

    // -------------------- ② 편집기 인텐트 --------------------

    fun editorIntent(req: EditorRequest, target: Uri?): Intent {
        val intent = when (req.action) {
            // 원문 After 코드의 모양 — 타입은 Contacts.CONTENT_URI 로부터 시스템이 정한다(dir/contact)
            EditorAction.INSERT -> Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
            // 이미 아는 연락처 — lookup URI 를 data 로
            EditorAction.EDIT -> Intent(Intent.ACTION_EDIT, target)
            // data 없이 타입만 — 편집기가 '새로 만들기 / 기존 연락처 고르기' 를 보여 준다
            EditorAction.INSERT_OR_EDIT -> Intent(Intent.ACTION_INSERT_OR_EDIT).setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE)
        }
        return intent.apply {
            req.name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
            putExtra(ContactsContract.Intents.Insert.PHONE, req.phone)
            if (req.finishOnSave) putExtra(EXTRA_FINISH_ON_SAVE, true)
            val data = arrayListOf<ContentValues>()
            if (req.withWebsite) {
                data += ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, Website.CONTENT_ITEM_TYPE)
                    put(Website.URL, DEMO_WEBSITE)
                }
            }
            if (req.withCustomMime) {
                data += ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, CUSTOM_MIME)
                    put(ContactsContract.Data.DATA1, "custom ${req.id}")
                }
            }
            if (req.withPhoto) {
                data += ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                    put(Photo.PHOTO, demoPhotoBytes(req.id))
                }
            }
            if (data.isNotEmpty()) putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data)
        }
    }

    fun onEditorLaunched(req: EditorRequest, handler: String) {
        editorRows.add(EditorRow(request = req, time = nowText(), handler = handler))
    }

    fun onEditorLaunchFailed(req: EditorRequest, t: Throwable) {
        editorRows.add(EditorRow(request = req, time = nowText(), handler = "열지 못함", note = describeFailure(t)))
    }

    fun onEditorResult(req: EditorRequest, resultCode: Int, data: Intent?) {
        val uri = data?.data
        val grant = data != null && data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0
        updateEditor(req.id) { it.copy(resultCode = resultCode, resultUri = uri?.toString(), returnedAt = nowText(), grantRead = grant) }
        // 결과로 연락처 URI 가 오면 다음 ACTION_EDIT 대상으로 쓴다
        if (uri != null) editTarget = EditTarget(uri, "편집기 결과 #${req.id}")
        verifyEditor(req)
    }

    /** 저장된 결과를 READ_CONTACTS 로 다시 읽어 보낸 값이 남았는지 대조한다 */
    fun verifyEditor(req: EditorRequest) {
        if (!granted(Manifest.permission.READ_CONTACTS)) {
            updateEditor(req.id) { it.copy(note = "확인 조회는 READ_CONTACTS 가 필요하다(6번 카드)") }
            return
        }
        scope.launch {
            val outcome = withContext(Dispatchers.IO) {
                runCatching {
                    val rows = queryDemoRows()
                    // 요청마다 전화번호가 달라 그 번호를 가진 연락처로 찾는다(표시 이름은 공백이 바뀌어 저장된다)
                    val contactId = rows.firstOrNull { it.mime == Phone.CONTENT_ITEM_TYPE && digitsOf(it.data1) == digitsOf(req.phone) }?.contactId
                    contactId?.let { id -> rows.filter { it.contactId == id } }
                }
            }
            outcome.fold(
                onSuccess = { rows ->
                    if (rows == null) {
                        updateEditor(req.id) { it.copy(checks = null, note = "저장된 예제 연락처를 찾지 못함 — 저장하지 않았거나 예제 밖 연락처에 붙였다") }
                    } else {
                        val first = rows.first()
                        updateEditor(req.id) {
                            it.copy(
                                checks = checkFields(req, rows),
                                savedTo = "${first.displayName} · 계정 ${accountText(first)}",
                                note = null
                            )
                        }
                    }
                    refreshDemoContactsNow()
                },
                onFailure = { t -> updateEditor(req.id) { it.copy(note = "확인 실패 — ${describeFailure(t)}") } }
            )
        }
    }

    private fun updateEditor(id: Int, transform: (EditorRow) -> EditorRow) {
        val index = editorRows.indexOfFirst { it.request.id == id }
        if (index >= 0) editorRows[index] = transform(editorRows[index])
    }

    // -------------------- ③ 피커 --------------------

    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    fun sessionPickerIntent(fields: List<String>, multiple: Boolean, limit: Int, matchAll: Boolean, useSystemExtra: Boolean): Intent =
        Intent(ContactsPickerSessionContract.ACTION_PICK_CONTACTS).apply {
            if (useSystemExtra) putExtra(Intent.EXTRA_USE_SYSTEM_CONTACTS_PICKER, true)
            putStringArrayListExtra(ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS, ArrayList(fields))
            if (multiple) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                putExtra(ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_SELECTION_LIMIT, limit)
            }
            putExtra(ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_MATCH_ALL_DATA_FIELDS, matchAll)
        }

    /** targetSdk 37 미만 앱이 기존 ACTION_PICK 그대로 새 피커를 쓰는 방법(공식 문서 '이전 타깃에서 시험') */
    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    fun pickWithSystemPickerIntent(legacy: Intent): Intent =
        Intent(legacy).putExtra(Intent.EXTRA_USE_SYSTEM_CONTACTS_PICKER, true)

    fun onPickLaunched(id: Int, mode: PickMode, handler: String) {
        pickRows.add(PickRow(id = id, time = nowText(), mode = mode, handler = handler))
    }

    fun onPickLaunchFailed(id: Int, mode: PickMode, t: Throwable) {
        pickRows.add(PickRow(id = id, time = nowText(), mode = mode, handler = "열지 못함", lines = listOf(describeFailure(t))))
    }

    fun onPickResult(id: Int, resultCode: Int, data: Intent?) {
        val uri = data?.data
        val grant = data != null && data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0
        val clipCount = data?.clipData?.itemCount
        updatePick(id) {
            it.copy(
                resultCode = resultCode,
                uri = uri?.toString(),
                grantRead = grant,
                lines = listOfNotNull(clipCount?.let { n -> "clipData 항목 $n 개" })
            )
        }
        if (uri != null) queryPicked(id, uri)
    }

    /**
     * 받은 URI 를 조회한다. 세션 URI 는 공식 문서의 투영 그대로, 레거시 연락처 URI 는 연락처 표에 MIMETYPE·DATA1 이
     * 없어 투영 없이(null) 읽고 있는 열만 쓴다.
     */
    fun queryPicked(id: Int, uri: Uri, withSelection: Boolean = false) {
        val isSession = isSessionUri(uri)
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val projection = if (isSession) PICK_PROJECTION else null
                    val selection = if (withSelection) "${ContactsContract.Data.MIMETYPE} = ?" else null
                    val args = if (withSelection) arrayOf(Phone.CONTENT_ITEM_TYPE) else null
                    resolver.query(uri, projection, selection, args, null)?.use { c -> readPicked(c) to c.count }
                        ?: (emptyList<PickedRow>() to -1)
                }
            }
            val label = (if (withSelection) "selection 넣어 조회" else "조회") + readState()
            result.fold(
                onSuccess = { (rows, count) ->
                    updatePick(id) {
                        it.copy(
                            contacts = groupPicked(rows),
                            lines = it.lines + "$label: 커서 ${if (count < 0) "null" else "${count}행"}"
                        )
                    }
                },
                onFailure = { t -> updatePick(id) { it.copy(lines = it.lines + "$label 실패 — ${classify(t).label} · ${describeFailure(t)}") } }
            )
        }
    }

    /** 투영 없이(null) 조회해 어떤 열이 오는지, 계정 열이 비었는지 본다 — 공식 문서: 계정 메타데이터는 지워진다 */
    fun inspectColumns(id: Int, uri: Uri) {
        scope.launch {
            val line = withContext(Dispatchers.IO) {
                runCatching {
                    resolver.query(uri, null, null, null, null)?.use { c ->
                        val accountCols = c.columnNames.filter { it.startsWith("account") }
                        val accountValues = if (c.moveToFirst()) {
                            accountCols.joinToString { col -> "$col=${c.getString(c.getColumnIndexOrThrow(col))}" }
                        } else "행 없음"
                        "열 ${c.columnCount}개 · 계정 열 ${accountCols.size}개(${accountValues.ifEmpty { "없음" }})"
                    } ?: "커서 null"
                }.getOrElse { t -> "열 조회 실패 — ${describeFailure(t)}" }
            }
            updatePick(id) { it.copy(lines = it.lines + line) }
        }
    }

    /** 레거시 연락처 URI 의 하위 data 경로 — URI 하나에만 준 임시 권한이 하위 경로까지 미치는지 */
    fun queryLegacyDataDirectory(id: Int, contactUri: Uri) {
        val dataUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY)
        scope.launch {
            val line = withContext(Dispatchers.IO) {
                runCatching {
                    resolver.query(dataUri, arrayOf(ContactsContract.Data.MIMETYPE), null, null, null)?.use { "data 하위 ${it.count}행 읽음" }
                        ?: "커서 null"
                }.getOrElse { t -> "data 하위 조회 실패 — ${classify(t).label}" } + readState()
            }
            updatePick(id) { it.copy(lines = it.lines + line) }
        }
    }

    /** 조회 결과는 READ_CONTACTS 유무에 따라 달라질 수 있어 그때의 상태를 같이 적는다 */
    private fun readState(): String = if (granted(Manifest.permission.READ_CONTACTS)) "(READ_CONTACTS 있음)" else "(READ_CONTACTS 없음)"

    private fun updatePick(id: Int, transform: (PickRow) -> PickRow) {
        val index = pickRows.indexOfFirst { it.id == id }
        if (index >= 0) pickRows[index] = transform(pickRows[index])
    }

    // -------------------- 확인 조회 · 정리 --------------------

    fun refreshDemoContacts() {
        if (!granted(Manifest.permission.READ_CONTACTS)) {
            demoNote = "READ_CONTACTS 가 없어 목록을 읽을 수 없다"
            demoContacts.clear()
            return
        }
        scope.launch { refreshDemoContactsNow() }
    }

    private suspend fun refreshDemoContactsNow() {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val total = resolver.query(ContactsContract.Contacts.CONTENT_URI, arrayOf(ContactsContract.Contacts._ID), null, null, null)
                    ?.use { it.count } ?: 0
                val contacts = queryDemoRows().groupBy { it.contactId }.map { (id, rows) ->
                    val first = rows.first()
                    DemoContact(id, first.lookupKey, first.displayName.orEmpty(), accountText(first), rows.map { shortMime(it.mime) }.distinct())
                }
                total to contacts
            }
        }
        result.fold(
            onSuccess = { (total, contacts) ->
                demoContacts.clear()
                demoContacts.addAll(contacts)
                demoNote = "이 앱이 READ_CONTACTS 로 볼 수 있는 연락처: 전체 ${total}명(이름은 읽지 않음) · 그중 예제 연락처 ${contacts.size}명"
            },
            onFailure = { t -> demoNote = "목록 조회 실패 — ${describeFailure(t)}" }
        )
    }

    /** 예제 연락처(이름에 CS152)만 지운다 — 쓰기라 WRITE_CONTACTS 가 필요하다. 삭제용 인텐트는 없다(원문) */
    fun deleteDemoContacts() {
        scope.launch {
            val note = withContext(Dispatchers.IO) {
                runCatching {
                    val rawIds = resolver.query(
                        ContactsContract.RawContacts.CONTENT_URI,
                        arrayOf(ContactsContract.RawContacts._ID, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY),
                        "${ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY} LIKE ?",
                        arrayOf("%$DEMO_MARK%"),
                        null
                    )?.use { c ->
                        buildList {
                            while (c.moveToNext()) {
                                if (isDemoName(c.getString(1))) add(c.getLong(0))
                            }
                        }
                    }.orEmpty()
                    val deleted = rawIds.sumOf { raw ->
                        resolver.delete(ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, raw), null, null)
                    }
                    "예제 연락처 raw ${rawIds.size}개 중 ${deleted}개 삭제"
                }.getOrElse { t -> "삭제 실패 — ${classify(t).label} · ${describeFailure(t)}" }
            }
            demoNote = note
            if (granted(Manifest.permission.READ_CONTACTS)) refreshDemoContactsNow()
        }
    }

    /** 예제 연락처의 data 행 전부 — 이름에 표식이 든 것만 */
    private fun queryDemoRows(): List<DataRow> {
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.LOOKUP_KEY,
            ContactsContract.Data.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            Photo.PHOTO,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.RawContacts.ACCOUNT_NAME,
        )
        return resolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            "${ContactsContract.Data.DISPLAY_NAME_PRIMARY} LIKE ?",
            arrayOf("%$DEMO_MARK%"),
            null
        )?.use { c ->
            buildList {
                while (c.moveToNext()) {
                    val name = c.getString(2)
                    if (!isDemoName(name)) continue
                    val mime = c.getString(3) ?: continue
                    add(
                        DataRow(
                            contactId = c.getLong(0),
                            lookupKey = c.getString(1),
                            displayName = name,
                            mime = mime,
                            // 사진 행의 DATA1 은 사진 파일 id 라 문자열로 읽지 않는다
                            data1 = if (mime == Photo.CONTENT_ITEM_TYPE) null else c.getString(4),
                            hasBlob = mime == Photo.CONTENT_ITEM_TYPE && c.getBlob(5) != null,
                            accountType = c.getString(6),
                            accountName = c.getString(7),
                        )
                    )
                }
            }
        }.orEmpty()
    }

    companion object {
        /** ContactsPickerSessionContract.AUTHORITY(API 37) 의 값 — 37 미만에서도 URI 를 가를 수 있게 문자열로 */
        const val SESSION_AUTHORITY = "com.android.contacts.picker.sessions"

        /**
         * 받은 세션 URI 는 authority 앞에 사용자 id 가 붙는다("content://0@com.android.contacts.picker.sessions/sessions/…") —
         * authority 비교는 빗나가므로 host 로 가른다.
         */
        fun isSessionUri(uri: Uri): Boolean = uri.host == SESSION_AUTHORITY

        private const val PREFS_NAME = "contact_picker_demo"
        private const val KEY_URI = "last_uri"
        private const val KEY_PID = "last_pid"
        private const val KEY_TIME = "last_time"

        /** 공식 문서 예제의 투영 */
        val PICK_PROJECTION = arrayOf(
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
        )

        /** 피커에 요청할 수 있는 필드 */
        val PICK_FIELDS = listOf(
            "전화" to Phone.CONTENT_ITEM_TYPE,
            "이메일" to Email.CONTENT_ITEM_TYPE,
            "주소" to StructuredPostal.CONTENT_ITEM_TYPE,
        )
    }
}

/** ACTION_EDIT 대상 연락처 */
data class EditTarget(val uri: Uri, val source: String)

/** SharedPreferences 에 적어 둔 마지막 피커 URI */
data class SavedPick(val uri: Uri, val pid: Int, val time: String)

/** 커서에 있는 열만 골라 읽는다 — 레거시 연락처 URI 결과에는 MIMETYPE·DATA1 이 없다 */
private fun readPicked(c: Cursor): List<PickedRow> {
    fun idx(name: String) = c.getColumnIndex(name).takeIf { it >= 0 }
    val lookup = idx(ContactsContract.Contacts.LOOKUP_KEY)
    val name = idx(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
    val mime = idx(ContactsContract.Data.MIMETYPE)
    val data1 = idx(ContactsContract.Data.DATA1)
    return buildList {
        while (c.moveToNext()) {
            add(
                PickedRow(
                    lookupKey = lookup?.let { c.getString(it) },
                    name = name?.let { c.getString(it) },
                    mime = mime?.let { c.getString(it) },
                    data1 = data1?.let { c.getString(it) },
                )
            )
        }
    }
}

private fun accountText(row: DataRow): String =
    if (row.accountType == null && row.accountName == null) "없음(기기 로컬)" else "${row.accountType} / ${row.accountName}"

fun shortMime(mime: String): String = when (mime) {
    StructuredName.CONTENT_ITEM_TYPE -> "이름"
    Phone.CONTENT_ITEM_TYPE -> "전화"
    Email.CONTENT_ITEM_TYPE -> "이메일"
    Website.CONTENT_ITEM_TYPE -> "웹사이트"
    Photo.CONTENT_ITEM_TYPE -> "사진"
    CUSTOM_MIME -> "표준 밖 MIME"
    else -> mime.substringAfterLast('/')
}

/** 편집기에 미리 넣을 작은 사진 — 96px 색 사각형에 번호 */
private fun demoPhotoBytes(n: Int): ByteArray {
    val bitmap = createBitmap(96, 96)
    val canvas = Canvas(bitmap)
    canvas.drawColor(0xFF00897B.toInt())
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("$n", 48f, 60f, paint)
    return ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        bitmap.recycle()
        out.toByteArray()
    }
}
