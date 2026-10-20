package com.example.composesample.presentation.example.component.system.platform.language

import android.app.Activity
import android.app.Application
import android.app.LocaleConfig
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import com.example.composesample.R
import com.example.composesample.util.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/** ViewModel 인스턴스 번호 — 재생성 뒤에도 같은 번호면 ViewModel 은 살아남은 것이다 */
private val viewModelCounter = AtomicInteger()

/** 날짜 형식 비교용 고정 시각(2026-10-20 03:00 UTC) — 실행할 때마다 같은 날짜를 쓴다 */
private const val SAMPLE_DATE_MILLIS = 1_792_465_200_000L

/** 버튼으로 고를 수 있는 앱 언어 목록(언어 태그, 쉼표 구분). 빈 문자열 = 시스템 기본으로 되돌리기 */
internal val APP_LOCALE_CHOICES = listOf("ko", "en", "ja", "ja,en", "fy,en")

/**
 * '시스템 앱 언어' 탭의 ViewModel — 언어 변경 호출과 기록을 맡는다.
 * 앱 언어를 바꾸면 액티비티는 재생성되지만 이 ViewModel 은 BlogExampleActivity 의 ViewModelStore 에 있어 살아남는다 —
 * 그래서 재생성 전후의 기록을 한 줄로 이어 볼 수 있다(프로세스가 죽으면 기록도 사라진다).
 */
class AppLocaleViewModel(application: Application) : AndroidViewModel(application) {
    val instanceNumber: Int = viewModelCounter.incrementAndGet()

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log: StateFlow<List<String>> = _log.asStateFlow()

    private var screenCount = 0
    private var lastActivityId: String? = null

    /** 탭이 새로 컴포즈될 때마다 부른다 — 액티비티 인스턴스가 바뀌었으면 재생성이다 */
    fun onScreenShown(activityId: String) {
        screenCount++
        val head = "화면 #$screenCount · 액티비티 $activityId · ViewModel #$instanceNumber · pid ${Process.myPid()}"
        val tail = when (val previous = lastActivityId) {
            null -> "첫 진입"
            activityId -> "같은 액티비티(탭 전환)"
            else -> "새 액티비티 — 이전 $previous 는 재생성으로 사라짐"
        }
        lastActivityId = activityId
        record("$head — $tail · 앱 언어 ${appLocalesText()}")
    }

    /** LocaleManager(API 33+)로 앱 언어를 바꾼다. 빈 문자열이면 시스템 기본으로 되돌린다 */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setAppLocales(tags: String) {
        val localeManager = localeManager()
        val before = appLocalesText()
        localeManager.setApplicationLocales(LocaleList.forLanguageTags(tags))
        record(
            "LocaleManager.setApplicationLocales(\"$tags\") — 호출 전 $before → 호출 직후 읽기 ${appLocalesText()}" +
                " · 액티비티 configuration 은 아직 ${getApplication<Application>().resources.configuration.locales.toLanguageTags()}"
        )
    }

    /**
     * 같은 일을 AppCompatDelegate 로 시도한다. API 33+ 에서는 '살아 있는 AppCompatDelegate' 에서 얻은 컨텍스트로
     * LocaleManager 를 부르는데, 이 앱의 액티비티는 전부 ComponentActivity 라 그 경로가 비어 있다.
     */
    fun setViaAppCompat(tags: String) {
        val before = appLocalesText()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
        val after = appLocalesText()
        val compatRead = AppCompatDelegate.getApplicationLocales().toLanguageTags().bracketed()
        record(
            "AppCompatDelegate.setApplicationLocales(\"$tags\") — 앱 언어 $before → $after · " +
                "AppCompatDelegate.getApplicationLocales() = $compatRead"
        )
    }

    fun clearLog() {
        _log.value = emptyList()
    }

    /** 실제 앱 언어 — 33+ 은 LocaleManager, 미만은 core 의 LocaleManagerCompat(AppCompat 자동 저장 파일) */
    private fun appLocalesText(): String =
        LocaleManagerCompat.getApplicationLocales(getApplication()).toLanguageTags().bracketed()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun localeManager(): LocaleManager =
        getApplication<Application>().getSystemService(LocaleManager::class.java)

    private fun record(line: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(Date())
        _log.update { (it + "$time $line").takeLast(40) }
    }
}

private fun String.bracketed(): String = "[$this]"

/** 한 시점의 로케일 값들 — '값이 어디에 들어가나' 카드의 행 */
private fun readLocaleRows(activity: Context, composeString: String): List<Pair<String, String>> {
    val app = activity.applicationContext
    val rows = mutableListOf<Pair<String, String>>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = activity.getSystemService(LocaleManager::class.java)
        rows += "LocaleManager.applicationLocales" to localeManager.applicationLocales.toLanguageTags().bracketed()
        rows += "LocaleManager.systemLocales" to localeManager.systemLocales.toLanguageTags().bracketed()
    }
    rows += "LocaleManagerCompat.getApplicationLocales" to
        LocaleManagerCompat.getApplicationLocales(activity).toLanguageTags().bracketed()
    rows += "AppCompatDelegate.getApplicationLocales" to
        AppCompatDelegate.getApplicationLocales().toLanguageTags().bracketed()
    rows += "액티비티 configuration.locales" to activity.resources.configuration.locales.toLanguageTags().bracketed()
    rows += "Application configuration.locales" to app.resources.configuration.locales.toLanguageTags().bracketed()
    rows += "Resources.getSystem() locales" to Resources.getSystem().configuration.locales.toLanguageTags().bracketed()
    rows += "Locale.getDefault()" to Locale.getDefault().toLanguageTag()
    rows += "stringResource(welcome_message)" to composeString
    rows += "Application getString(welcome_message)" to app.getString(R.string.welcome_message)
    rows += "기존 탭 LanguageManager 컨텍스트" to
        "${LanguageManager.createLocalizedContext(activity).getString(R.string.welcome_message)} " +
        "(저장값 ${LanguageManager.getCurrentLanguageDisplayName(activity)})"
    rows += "DateFormat FULL(Locale.getDefault)" to
        DateFormat.getDateInstance(DateFormat.FULL).format(Date(SAMPLE_DATE_MILLIS))
    return rows
}

/** 앱 매니페스트의 localeConfig 를 시스템이 어떻게 읽는지 — LocaleConfig(API 33+) */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun readLocaleConfig(context: Context): String {
    val config = LocaleConfig(context)
    val status = when (config.status) {
        LocaleConfig.STATUS_SUCCESS -> "STATUS_SUCCESS"
        LocaleConfig.STATUS_NOT_SPECIFIED -> "STATUS_NOT_SPECIFIED(선언 없음)"
        LocaleConfig.STATUS_PARSING_FAILED -> "STATUS_PARSING_FAILED"
        else -> "알 수 없음(${config.status})"
    }
    return "$status · supportedLocales ${config.supportedLocales?.toLanguageTags().orEmpty().bracketed()}"
}

private fun Activity?.shortId(): String = if (this == null) "없음" else "@" + Integer.toHexString(System.identityHashCode(this))

/**
 * LocalLanguageChange 예제의 '시스템 앱 언어' 탭. 첫 탭(컨텍스트 교체)은 SharedPreferences 에 저장한 값으로
 * 그 화면의 컨텍스트만 바꾼다 — 이 탭은 API 33 LocaleManager 로 앱 전체의 언어를 시스템에 맡긴다.
 * 카드 문구의 값은 Galaxy A72(API 33, 시스템 언어 ko-KR 우선) debug·release 와 에뮬레이터 API 35(en-US)·30 실측이다.
 */
@Composable
internal fun AppLocaleTab() {
    val viewModel: AppLocaleViewModel = koinViewModel()
    val log by viewModel.log.collectAsState()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    // AppCompat 시도처럼 재생성이 없는 호출 뒤에 값을 다시 읽기 위한 키
    var refreshTick by remember { mutableIntStateOf(0) }
    val composeString = stringResource(R.string.welcome_message)
    val rows = remember(refreshTick, composeString) { readLocaleRows(context, composeString) }

    LaunchedEffect(Unit) { viewModel.onScreenShown(activity.shortId()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LocaleCard(
                title = "🌐 앱 언어 바꾸기 — LocaleManager",
                description = "LocaleManager.setApplicationLocales(LocaleList) 는 이 앱 전체의 언어를 시스템에 맡긴다. " +
                    "빈 목록이면 시스템 언어를 따르는 기본 상태로 돌아간다.",
                color = Color(0xFFE3F2FD)
            ) {
                if (supported) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        APP_LOCALE_CHOICES.forEach { tags ->
                            ActionButton(tags, Color(0xFF1565C0)) { viewModel.setAppLocales(tags) }
                        }
                        ActionButton("시스템 기본", Color(0xFF455A64)) { viewModel.setAppLocales("") }
                    }
                    Spacer(Modifier.height(6.dp))
                    Note(
                        "값이 바뀌면 이 앱의 액티비티가 전부 재생성된다(configChanges 에 locale 이 없다) — 기록 카드에서 액티비티 번호가 " +
                            "바뀌고 ViewModel 번호·pid 는 그대로다. 같은 값을 다시 넣으면 아무 일도 없다."
                    )
                    Note(
                        "호출 직후 getApplicationLocales() 는 이미 새 값인데 그 순간 configuration 은 이전 값이다 — 화면 반영은 재생성 뒤다."
                    )
                    Note("앱 언어는 시스템이 저장한다 — 프로세스를 죽이고 다시 열어도(새 pid) 처음부터 그 언어로 뜬다.")
                } else {
                    Note(
                        "이 기기는 API ${Build.VERSION.SDK_INT} — LocaleManager 는 API 33 부터다. AppCompat 백포트는 " +
                            "AppCompatActivity 에서만 동작하는데 이 예제 화면은 ComponentActivity 라 이 탭에서는 바꾸지 않는다.",
                        Color(0xFFC62828)
                    )
                }
            }
        }
        item {
            LocaleCard(
                title = "📋 값이 어디에 들어가나",
                description = "앱 언어를 바꾼 뒤 각 출처가 무엇을 돌려주는지. 화면이 재생성되면 자동으로 다시 읽는다.",
                color = Color(0xFFF3E5F5)
            ) {
                rows.forEach { (label, value) -> ValueRow(label, value) }
                Spacer(Modifier.height(4.dp))
                ActionButton("다시 읽기", Color(0xFF6A1B9A)) { refreshTick++ }
                Spacer(Modifier.height(6.dp))
                Note(
                    "configuration 은 앱 언어를 시스템 목록 앞에 붙인다 — en 이면 [en, 시스템 목록…]. Locale.getDefault()·날짜 형식도 " +
                        "따라 바뀐다. 첫 탭 방식은 Locale.getDefault 를 건드리지 않는다(저장값이 한국어여도 en)."
                )
                Note(
                    "Resources.getSystem() 도 앱 언어가 섞인 목록을 준다 — 기기의 실제 언어는 LocaleManager.systemLocales 로 읽는다."
                )
                Note(
                    "ja — 앱에 values-ja 가 없어 문자열은 기본 values(한국어)로 떨어지고 날짜만 일본어다. 시스템이 영어인 기기" +
                        "(API 35 에뮬레이터)에서도 '환영합니다!' — 기본 values 가 한국어라서다.",
                    Color(0xFFAD1457)
                )
                Note(
                    "ja,en — en 으로 넘어갈 것 같지만 그대로 한국어다. APK 에 라이브러리(androidx 등)의 values-ja 가 들어 있어 시스템은 " +
                        "ja 를 '있는 언어'로 고른다. APK 에 없는 fy 는 fy,en 이면 en 이 맨 앞으로 올라와 'Welcome!' 이 된다. " +
                        "AGP androidResources.localeFilters 로 ko·en 만 남기면 ja,en 도 en 이 됐다(임시 빌드 실측, 이 앱 설정은 그대로).",
                    Color(0xFFAD1457)
                )
            }
        }
        item {
            LocaleCard(
                title = "📜 기록 — 재생성·ViewModel·pid",
                description = "탭이 새로 그려질 때마다 액티비티 인스턴스를 적는다. 번호가 바뀌면 재생성이다.",
                color = Color(0xFFFFFDE7)
            ) {
                if (log.isEmpty()) {
                    Note("기록 없음", Color.Gray)
                } else {
                    log.forEach { line -> LogLine(line) }
                }
                Spacer(Modifier.height(4.dp))
                ActionButton("기록 지우기", Color(0xFF757575)) { viewModel.clearLog() }
            }
        }
        item {
            LocaleCard(
                title = "⚠️ AppCompatDelegate 로 바꾸기",
                description = "AppCompatDelegate.setApplicationLocales(LocaleListCompat) 를 이 화면(ComponentActivity)에서 부른다.",
                color = Color(0xFFFFF3E0)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("AppCompat · en", Color(0xFFE65100)) {
                        viewModel.setViaAppCompat("en")
                        refreshTick++
                    }
                    ActionButton("AppCompat · 빈 목록", Color(0xFF8D6E63)) {
                        viewModel.setViaAppCompat("")
                        refreshTick++
                    }
                }
                Spacer(Modifier.height(6.dp))
                Note(
                    "API 33+ — AppCompat 은 살아 있는 AppCompatDelegate(AppCompatActivity)에서 컨텍스트를 얻어 LocaleManager 를 부른다. " +
                        "이 앱은 ComponentActivity 뿐이라 아무 일도 하지 않는다 — 예외도 로그도 없다.",
                    Color(0xFFBF360C)
                )
                Note(
                    "같은 이유로 AppCompatDelegate.getApplicationLocales() 는 앱 언어가 en 이어도 [] 다. " +
                        "읽기는 core 의 LocaleManagerCompat.getApplicationLocales(context) 가 맞다."
                )
                Note(
                    "API 33 미만(API 30 에뮬레이터) — getApplicationLocales() 는 [en] 을 돌려주지만 메모리 값일 뿐 configuration 은 " +
                        "그대로다. 적용 대상은 AppCompatActivity 이고, 저장은 매니페스트 autoStoreLocales 서비스를 둬야 한다."
                )
            }
        }
        item {
            LocaleCard(
                title = "⚙️ localeConfig — 시스템 설정 '앱 언어'",
                description = "매니페스트 android:localeConfig(res/xml/locales_config.xml)가 시스템 설정에 보여 줄 언어 목록이다.",
                color = Color(0xFFE8F5E9)
            ) {
                if (supported) {
                    ValueRow("LocaleConfig(context)", readLocaleConfig(context))
                    Spacer(Modifier.height(4.dp))
                    ActionButton("이 앱의 언어 설정 열기", Color(0xFF2E7D32)) {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Note(
                        "선언 전 빌드에서는 같은 버튼(같은 intent)의 설정 화면이 뜨자마자 닫혔다. 선언 뒤에는 '시스템 기본값'·추천" +
                            "(시스템 언어 중 ko·en 에 맞는 것)·모든 언어 목록이 나온다."
                    )
                    Note(
                        "설정에서 English → United States 를 고르면 en-US 로 저장되고, 앱으로 돌아오면 화면이 이미 재생성돼 있다."
                    )
                    Note(
                        "목록은 설정 화면용일 뿐이다 — LocaleManager 로는 목록에 없는 ja·fy 도 그대로 들어갔다(API 33·35)."
                    )
                } else {
                    Note("LocaleConfig·앱 언어 설정 화면은 API 33 부터다.", Color(0xFFC62828))
                }
            }
        }
        item {
            LocaleCard(
                title = "🔁 첫 탭(컨텍스트 교체)과 대조",
                description = "같은 '언어 바꾸기' 지만 값을 누가 갖고 어디까지 바뀌는지가 다르다.",
                color = Color(0xFFE0F7FA)
            ) {
                Note("범위 — 첫 탭: 그 컨텍스트로 읽은 문자열만 / 이 탭: 앱 configuration 전체(모든 액티비티·Application·Locale.getDefault·날짜 형식)")
                Note("저장 — 첫 탭: 앱의 SharedPreferences / 이 탭: 시스템. 둘 다 프로세스 종료·재설치(install -r)에 남는다")
                Note("화면 반영 — 첫 탭: 상태를 바꿔 다시 그린다(재생성 없음) / 이 탭: 액티비티 재생성")
                Note("밖에서 바꾸기 — 첫 탭: 불가 / 이 탭: 시스템 설정 '앱 언어'·adb")
                Note(
                    "섞어 쓰면 — 이 탭에서 en 으로 바꿔도 첫 탭은 저장값(한국어)으로 그린다. LanguageManager 가 configuration 의 " +
                        "언어 목록을 한 언어로 덮어써서다 — 헤더 제목이 탭마다 다른 언어가 된다.",
                    Color(0xFFAD1457)
                )
            }
        }
        item {
            LocaleCard(
                title = "🖥 밖에서 바꾸기 — adb",
                description = "앱이 떠 있으면 바로 재생성된다. --locales 를 빼면 빈 목록(시스템 기본)이다.",
                color = Color(0xFFECEFF1)
            ) {
                CommandLine("adb shell cmd locale set-app-locales ${context.packageName} --locales en")
                CommandLine("adb shell cmd locale get-app-locales ${context.packageName}")
                Spacer(Modifier.height(6.dp))
                Note(
                    "함정 — adb shell pm clear 는 앱 언어도 지운다([en] → []). 데이터를 지우고 시작하는 UI 테스트는 언어가 매번 " +
                        "시스템 기본으로 돌아간다. 재설치(install -r)는 남고, 삭제 뒤 설치는 [] 다(API 35 에뮬레이터).",
                    Color(0xFFAD1457)
                )
            }
        }
    }
}

@Composable
private fun LocaleCard(
    title: String,
    description: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(description, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 18.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

/** 라벨·값을 한 덩어리 텍스트로 — Row 로 나누면 긴 라벨에 값 칸이 눌려 한 글자씩 쌓인다 */
@Composable
private fun ValueRow(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Color(0xFF616161), fontSize = 12.sp)) { append(label) }
            append("\n")
            withStyle(SpanStyle(color = Color.Black, fontSize = 13.sp, fontFamily = FontFamily.Monospace)) { append(value) }
        },
        lineHeight = 17.sp,
        modifier = Modifier.padding(vertical = 3.dp)
    )
}

@Composable
private fun LogLine(line: String) {
    Text(
        text = line,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF263238),
        lineHeight = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding(6.dp)
    )
}

@Composable
private fun CommandLine(command: String) {
    Text(
        text = command,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFECEFF1),
        lineHeight = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF263238))
            .padding(8.dp)
    )
}

@Composable
private fun Note(text: String, color: Color = Color(0xFF37474F)) {
    Text(
        text = "• $text",
        fontSize = 13.sp,
        color = color,
        lineHeight = 18.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun ActionButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 13.sp)
    }
}
