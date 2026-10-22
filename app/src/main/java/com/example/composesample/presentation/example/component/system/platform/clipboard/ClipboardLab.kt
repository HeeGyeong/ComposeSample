package com.example.composesample.presentation.example.component.system.platform.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/** 이 예제가 클립보드에 넣는 클립의 라벨 — 다시 읽을 때 '이 예제가 넣은 클립'인지 가르는 표식 */
internal const val DEMO_CLIP_LABEL = "ComposeSample 클립보드 예제"

/**
 * `ClipDescription.EXTRA_IS_SENSITIVE`(API 33) 의 값. 33 미만에는 상수가 없어 같은 문자열을 직접 넣는다
 * (공식 문서 권장 — 키는 같고, 그 키를 읽는 시스템 UI 가 33 부터 생겼을 뿐이다).
 */
internal const val EXTRA_IS_SENSITIVE_KEY = "android.content.extra.IS_SENSITIVE"

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)

internal fun nowText(): String = timeFormat.format(Date())

// ==================== 순수 로직 (단위 테스트 대상) ====================

/** 클립보드 읽기 한 번에서 얻은 값 — 내용 자체는 이 예제가 넣은 클립일 때만 담는다 */
data class ClipSnapshot(
    val clipPresent: Boolean,
    val hasPrimaryClip: Boolean,
    val ours: Boolean,
    val sensitive: Boolean,
    val length: Int,
    val mimeTypes: List<String>,
    val ownText: String?,
)

enum class ReadVerdict(val label: String) {
    OURS("읽음 · 이 예제가 넣은 클립"),
    OTHER("읽음 · 다른 앱의 클립(내용은 표시하지 않음)"),
    EMPTY("비어 있음"),
    DENIED("null — 포커스가 없어 거부됨"),
}

/**
 * 같은 null 이라도 '정말 비었다'와 '거부됐다'를 API 는 구분해 주지 않는다(hasPrimaryClip 도 false).
 * 읽은 순간 창 포커스가 있었는지로만 가를 수 있다.
 */
fun verdictOf(focused: Boolean, snapshot: ClipSnapshot): ReadVerdict = when {
    snapshot.clipPresent -> if (snapshot.ours) ReadVerdict.OURS else ReadVerdict.OTHER
    !focused -> ReadVerdict.DENIED
    else -> ReadVerdict.EMPTY
}

/** 텍스트 선택 메뉴 실험에 쓰는 필드. 표본 단어가 서로 겹치지 않아 받은 문자열로 어느 필드인지 가른다 */
enum class DemoField(val label: String, val editable: Boolean, val requestsResult: Boolean, val sample: String) {
    COMPOSE_FIELD("Compose TextField", editable = true, requestsResult = false, sample = "compose alpha"),
    COMPOSE_SELECTION("SelectionContainer", editable = false, requestsResult = false, sample = "selection bravo"),
    VIEW_EDIT("View EditText(id 있음)", editable = true, requestsResult = true, sample = "edittext charlie"),
    VIEW_TEXT("View TextView(선택 가능·id 있음)", editable = false, requestsResult = true, sample = "textview delta"),
    VIEW_EDIT_NO_ID("View EditText(id 없음)", editable = true, requestsResult = true, sample = "noid foxtrot"),
    CONNECTED("Compose TextField + 직접 연결", editable = true, requestsResult = true, sample = "connected echo"),
}

/**
 * 받은 선택 문자열이 어느 필드의 표본에서 왔는지. 대소문자는 무시한다(변환 결과를 다시 선택해도 찾도록).
 * 두 필드 이상에 걸리거나 어디에도 없으면 null — 다른 앱에서 온 것이거나 판별 불가.
 */
fun sourceOf(selected: String): DemoField? {
    val needle = selected.trim()
    if (needle.length < 2) return null
    val hits = DemoField.entries.filter { it.sample.contains(needle, ignoreCase = true) }
    return hits.singleOrNull()
}

/** 처리 앱이 돌려줄 결과 — 영문은 대문자로, 대소문자가 없는 글자(한글 등)는 꺾쇠로 감싼다 */
fun processText(text: String): String {
    val upper = text.uppercase(Locale.ROOT)
    return if (upper != text) upper else "「$text」"
}

/** PROCESS_TEXT 한 번을 받은 기록 — ProcessTextDemoActivity 가 남기고 예제 화면이 읽는다 */
data class ProcessTextReceipt(
    val id: Int,
    val time: String,
    val text: String,
    /** EXTRA_PROCESS_TEXT_READONLY — 엑스트라 자체가 없으면 null */
    val readonlyExtra: Boolean?,
    /** startActivityForResult 로 열렸을 때의 호출 액티비티 — startActivity 면 null(결과를 받을 곳이 없다) */
    val callingActivity: String?,
    /** Activity.referrer — 어느 앱이 열었나(startActivity 여도 채워진다) */
    val referrer: String?,
    val outcome: String,
)

/** 필드별 기대값과 실제 받은 값 비교 결과 */
data class ReceiptCheck(
    val field: DemoField?,
    val expectedReadonly: Boolean?,
    val readonlyOk: Boolean?,
    val canReturnResult: Boolean,
)

fun checkReceipt(receipt: ProcessTextReceipt): ReceiptCheck {
    val field = sourceOf(receipt.text)
    val expected = field?.let { !it.editable }
    return ReceiptCheck(
        field = field,
        expectedReadonly = expected,
        readonlyOk = if (expected == null || receipt.readonlyExtra == null) null else receipt.readonlyExtra == expected,
        canReturnResult = receipt.callingActivity != null,
    )
}

/**
 * 프로세스 안 공유 기록. ProcessTextDemoActivity 는 다른 앱이 띄울 수도 있어 화면 ViewModel 이 없을 수 있으므로
 * 액티비티와 예제 화면이 이 객체 하나를 같이 본다(프로세스가 죽으면 비워진다 — 예제 기록용이라 영속하지 않는다).
 */
object ProcessTextLog {
    private val _receipts = MutableStateFlow<List<ProcessTextReceipt>>(emptyList())
    val receipts: StateFlow<List<ProcessTextReceipt>> = _receipts.asStateFlow()

    // `private var nextId = 1` 로 두면 debug(HotSwan 재작성)에서 object 의 private var 가 읽을 때마다 초기값(1)으로 돌아와
    // 번호가 늘 1 이다(ComposeHotReloadGuide 참고) — 값 보관함(AtomicInteger)은 val 이라 영향이 없다
    private val nextId = AtomicInteger(1)

    fun add(text: String, readonlyExtra: Boolean?, callingActivity: String?, referrer: String?): Int {
        val id = nextId.getAndIncrement()
        _receipts.update {
            (it + ProcessTextReceipt(id, nowText(), text, readonlyExtra, callingActivity, referrer, outcome = "열림")).takeLast(MAX_RECEIPTS)
        }
        return id
    }

    fun setOutcome(id: Int, outcome: String) {
        _receipts.update { list -> list.map { if (it.id == id) it.copy(outcome = outcome) else it } }
    }

    fun clear() {
        _receipts.value = emptyList()
    }

    private const val MAX_RECEIPTS = 12
}

// ==================== 텍스트 선택 메뉴(PROCESS_TEXT) 플랫폼 호출 ====================

/** 이 앱이 보는 PROCESS_TEXT 처리 앱 하나 */
data class ProcessTextHandler(val label: String, val packageName: String, val activityName: String, val ours: Boolean)

/**
 * Compose·View 텍스트 메뉴가 항목을 만들 때와 같은 질의. API 30+ 에서는 패키지 가시성 규칙이 걸려,
 * 매니페스트 `<queries>` 없이는 다른 앱 처리기가 결과에서 빠진다(자기 앱 것은 항상 보인다).
 * 다른 앱 것은 exported 이고 권한이 없을 때만 메뉴에 오른다 — Compose(ProcessTextApi23Impl)·View(Editor) 의 같은 필터.
 */
fun processTextHandlers(context: Context): List<ProcessTextHandler> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_PROCESS_TEXT).setType("text/plain")
    return pm.queryIntentActivities(intent, 0)
        .filter { info ->
            info.activityInfo.packageName == context.packageName ||
                (info.activityInfo.exported && info.activityInfo.permission == null)
        }
        .map { info ->
            ProcessTextHandler(
                label = info.loadLabel(pm).toString(),
                packageName = info.activityInfo.packageName,
                activityName = info.activityInfo.name,
                ours = info.activityInfo.packageName == context.packageName,
            )
        }
}

private fun demoComponent(context: Context) = ComponentName(context, ProcessTextDemoActivity::class.java)

/** 매니페스트 기본값은 꺼짐(enabled="false"). ENABLED 로 바꿨을 때만 켜진 것으로 본다 */
fun isDemoMenuEnabled(context: Context): Boolean =
    context.packageManager.getComponentEnabledSetting(demoComponent(context)) ==
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED

/**
 * 켜기 = ENABLED, 끄기 = DEFAULT(매니페스트 값 false 로 복귀). DONT_KILL_APP 이 없으면 컴포넌트 상태가 바뀔 때
 * 앱 프로세스가 종료된다.
 */
fun setDemoMenuEnabled(context: Context, enabled: Boolean) {
    val state = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
    context.packageManager.setComponentEnabledSetting(demoComponent(context), state, PackageManager.DONT_KILL_APP)
}

/** Compose 메뉴가 만드는 것과 같은 인텐트 — 단 READONLY 는 편집 가능 여부의 반대(프레임워크 TextView 와 같은 뜻)로 */
fun processTextIntent(handler: ProcessTextHandler, text: String, readonly: Boolean): Intent =
    Intent(Intent.ACTION_PROCESS_TEXT)
        .setType("text/plain")
        .setClassName(handler.packageName, handler.activityName)
        .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, readonly)

// ==================== 클립보드 실험 상태 홀더 ====================

/** 읽기 기록 한 줄 */
data class ClipReadRow(
    val time: String,
    val trigger: String,
    val focused: Boolean,
    val lifecycle: String,
    val snapshot: ClipSnapshot,
) {
    val verdict: ReadVerdict get() = verdictOf(focused, snapshot)
}

/** 쓰기·리스너 이벤트 한 줄 */
data class ClipEventRow(val time: String, val text: String, val focused: Boolean)

/**
 * 클립보드 실험의 상태 홀더. 화면이 remember 하고 DisposableEffect 로 start/stop 한다.
 * 리스너는 인터페이스 구현이라 컴포저블 안 익명 객체(프레임워크 클래스 상속) 함정과 무관하다.
 *
 * @param isFocused 읽는 순간 창 포커스 — API 29+ 클립보드 읽기 허용 조건
 * @param lifecycleState 읽는 순간 수명주기 — 포커스와 수명주기가 다르게 움직이는 것을 보이려고
 */
class ClipboardLab(
    context: Context,
    private val isFocused: () -> Boolean,
    private val lifecycleState: () -> String,
) : ClipboardManager.OnPrimaryClipChangedListener {

    private val clipboard = context.getSystemService(ClipboardManager::class.java)

    val reads = mutableStateListOf<ClipReadRow>()
    val events = mutableStateListOf<ClipEventRow>()
    var writeCount by mutableIntStateOf(0)
        private set
    var listenerCount by mutableIntStateOf(0)
        private set

    /** 쓰기 뒤 리스너가 한 번이라도 불린 쓰기 수 — API 31+ 는 쓰기 한 번에 리스너가 두 번 올 수 있어 호출 수와 따로 센다 */
    var noticedWriteCount by mutableIntStateOf(0)
        private set
    private var awaitingNotice = false
    var restoreNote by mutableStateOf<String?>(null)
        private set

    /** 실험 전 사용자의 클립 — 이 예제가 처음 쓰기 직전에 한 번 보관하고, 나갈 때 되돌린다 */
    private var backup: ClipData? = null
    private var backupTaken = false

    fun start() = clipboard.addPrimaryClipChangedListener(this)

    fun stop() = clipboard.removePrimaryClipChangedListener(this)

    override fun onPrimaryClipChanged() {
        listenerCount++
        if (awaitingNotice) {
            noticedWriteCount++
            awaitingNotice = false
        }
        // 리스너는 포커스가 있을 때만 오므로 여기서는 설명(description)을 읽을 수 있다 — 분류 상태로 두 번째 호출의 정체를 본다
        val status = classificationLabel(clipboard.primaryClipDescription)
        events.add(ClipEventRow(nowText(), "리스너 호출 #$listenerCount" + (status?.let { " · 분류 $it" } ?: ""), isFocused()))
        trim(events)
    }

    fun read(trigger: String): ClipReadRow {
        val focused = isFocused()
        val has = clipboard.hasPrimaryClip()
        val clip = clipboard.primaryClip
        val row = ClipReadRow(nowText(), trigger, focused, lifecycleState(), snapshotOf(has, clip))
        reads.add(row)
        trim(reads)
        return row
    }

    /** 사용자의 원래 클립을 한 번만 보관한다 — 포커스가 있을 때(버튼을 누른 순간) 불러야 읽힌다 */
    fun takeBackupIfNeeded() {
        if (backupTaken) return
        backup = clipboard.primaryClip
        backupTaken = true
    }

    fun write(text: String, trigger: String) {
        takeBackupIfNeeded()
        clipboard.setPrimaryClip(demoClip(text, sensitive = false))
        writeCount++
        awaitingNotice = true
        events.add(ClipEventRow(nowText(), "$trigger 쓰기 #$writeCount", isFocused()))
        trim(events)
    }

    /** LocalClipboard 로 넣은 쓰기도 세기만 한다(넣는 일은 화면이 Compose API 로 한다) */
    fun noteComposeWrite(trigger: String) {
        writeCount++
        awaitingNotice = true
        events.add(ClipEventRow(nowText(), "$trigger 쓰기 #$writeCount", isFocused()))
        trim(events)
    }

    /**
     * 지금 클립이 이 예제가 넣은 것일 때만 보관해 둔 원래 클립으로 되돌린다 — 그 사이 다른 앱에서 복사한 것을
     * 덮어쓰지 않으려고. 포커스가 없으면 지금 클립을 확인할 수 없으니 되돌리지 않는다.
     */
    fun restore(trigger: String): String {
        val note = when {
            !backupTaken -> "되돌릴 것 없음(이 예제가 아직 쓰지 않음)"
            else -> {
                val description = clipboard.primaryClipDescription
                when {
                    description == null -> "되돌리지 않음 — 지금 클립을 읽을 수 없다(포커스 없음 또는 비어 있음)"
                    description.label != DEMO_CLIP_LABEL -> "되돌리지 않음 — 지금 클립은 이 예제가 넣은 것이 아니다"
                    else -> restoreBackup()
                }
            }
        }
        restoreNote = "$trigger · $note"
        return note
    }

    private fun restoreBackup(): String {
        val original = backup
        return try {
            when {
                original != null -> {
                    clipboard.setPrimaryClip(original)
                    "원래 클립으로 되돌림"
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    clipboard.clearPrimaryClip()
                    "원래 비어 있었으므로 비움"
                }
                else -> "원래 비어 있었지만 API 28 미만은 clearPrimaryClip 이 없어 그대로 둠"
            }.also {
                backup = null
                backupTaken = false
            }
        } catch (e: SecurityException) {
            // 다른 앱의 content:// URI 가 든 클립은 이 앱이 다시 넣을 권한이 없을 수 있다
            "되돌리기 실패 — ${e.javaClass.simpleName}"
        }
    }

    private fun snapshotOf(has: Boolean, clip: ClipData?): ClipSnapshot {
        if (clip == null) {
            return ClipSnapshot(false, has, ours = false, sensitive = false, length = 0, mimeTypes = emptyList(), ownText = null)
        }
        val description = clip.description
        val ours = description.label == DEMO_CLIP_LABEL
        val text = if (clip.itemCount > 0) clip.getItemAt(0).text?.toString() else null
        return ClipSnapshot(
            clipPresent = true,
            hasPrimaryClip = has,
            ours = ours,
            sensitive = description.isSensitive(),
            length = text?.length ?: 0,
            mimeTypes = (0 until description.mimeTypeCount).map { description.getMimeType(it) },
            // 다른 앱의 클립 내용은 화면·로그에 옮기지 않는다
            ownText = if (ours) text else null,
        )
    }

    // 타입 매개변수 대신 스타 프로젝션 — 제네릭 메서드는 HotSwan 이 재작성하지 못한다(GENERIC_DECLARATION)
    private fun trim(list: MutableList<*>) {
        while (list.size > MAX_ROWS) list.removeAt(0)
    }

    private companion object {
        const val MAX_ROWS = 8
    }
}

/** 이 예제의 클립 — 민감 표시는 ClipDescription 의 extras 에 싣는다(setExtras 는 API 24) */
fun demoClip(text: String, sensitive: Boolean): ClipData =
    ClipData.newPlainText(DEMO_CLIP_LABEL, text).apply {
        if (sensitive) {
            description.extras = PersistableBundle().apply { putBoolean(EXTRA_IS_SENSITIVE_KEY, true) }
        }
    }

fun ClipDescription.isSensitive(): Boolean = extras?.getBoolean(EXTRA_IS_SENSITIVE_KEY, false) == true

/**
 * API 31+ 시스템은 클립을 받은 뒤 텍스트 분류(링크·주소 등)를 따로 돌리고, 끝나면 리스너에 한 번 더 알린다.
 * 그 상태를 이름으로 — 31 미만은 분류가 없어 null.
 */
fun classificationLabel(description: ClipDescription?): String? {
    if (description == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    return when (description.classificationStatus) {
        ClipDescription.CLASSIFICATION_NOT_COMPLETE -> "미완료"
        ClipDescription.CLASSIFICATION_NOT_PERFORMED -> "생략"
        ClipDescription.CLASSIFICATION_COMPLETE -> "완료"
        else -> "알 수 없음(${description.classificationStatus})"
    }
}
