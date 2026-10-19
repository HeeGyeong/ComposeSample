package com.example.composesample.presentation.example.component.system.deeplink

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * 딥링크 수신 데모의 설정·기록 저장소. 프로세스가 죽은 뒤 링크로 콜드 스타트해도 같은 설정으로 처리하고,
 * 예제 화면(DynamicAppLinks 의 '실제 수신' 탭)이 기록을 읽을 수 있도록 SharedPreferences 에 남긴다.
 */
class DeepLinkDemoStore private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("deeplink_demo", Context.MODE_PRIVATE)
    private val format = SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<DeepLinkSettings> = _settings.asStateFlow()

    private val _log = MutableStateFlow(prefs.getString(KEY_LOG, null)?.split(SEPARATOR)?.filter { it.isNotEmpty() }.orEmpty())
    val log: StateFlow<List<String>> = _log.asStateFlow()

    fun updateSettings(transform: (DeepLinkSettings) -> DeepLinkSettings) {
        val next = transform(_settings.value)
        _settings.value = next
        prefs.edit {
            putString(KEY_PARSER, next.parser.name)
            putBoolean(KEY_GUARD, next.recreateGuard)
            putBoolean(KEY_PENDING, next.pendingSlot)
            putBoolean(KEY_SYNTH, next.synthesizeBackStack)
        }
    }

    /** 최신이 앞. 시각·pid 를 붙여 프로세스가 바뀐 것(콜드 스타트)이 보이게 한다 */
    fun addLog(line: String) {
        val stamped = "${format.format(Date())} pid ${Process.myPid()} · $line"
        _log.update { (listOf(stamped) + it).take(MAX_LOG) }
        prefs.edit { putString(KEY_LOG, _log.value.joinToString(SEPARATOR)) }
    }

    fun clearLog() {
        _log.value = emptyList()
        prefs.edit { remove(KEY_LOG) }
    }

    private fun loadSettings() = DeepLinkSettings(
        parser = runCatching { ParserMode.valueOf(prefs.getString(KEY_PARSER, null).orEmpty()) }.getOrDefault(ParserMode.SCHEME_AWARE),
        recreateGuard = prefs.getBoolean(KEY_GUARD, true),
        pendingSlot = prefs.getBoolean(KEY_PENDING, true),
        synthesizeBackStack = prefs.getBoolean(KEY_SYNTH, true)
    )

    companion object {
        private const val KEY_PARSER = "parser"
        private const val KEY_GUARD = "recreate_guard"
        private const val KEY_PENDING = "pending_slot"
        private const val KEY_SYNTH = "synthesize_back_stack"
        private const val KEY_LOG = "log"
        private const val SEPARATOR = "\u001E"
        private const val MAX_LOG = 40

        @Volatile
        private var instance: DeepLinkDemoStore? = null

        fun get(context: Context): DeepLinkDemoStore =
            instance ?: synchronized(this) {
                instance ?: DeepLinkDemoStore(context.applicationContext).also { instance = it }
            }
    }
}

/** 액티비티 인스턴스 번호(프로세스 안에서만) — standard 로 두 개가 쌓이는 것을 보인다 */
private val instanceCounter = AtomicInteger()

/**
 * 커스텀 스킴 `composesample://` 를 받는 액티비티(launchMode singleTop — 매니페스트).
 * - 콜드: 이 액티비티가 없을 때 링크가 오면 onCreate. savedInstanceState 가 null 일 때만 처리한다
 *   (재생성·프로세스 종료 뒤 복원에도 같은 intent 가 붙어 있어 다시 처리하면 두 번 이동한다)
 * - 웜: 이 액티비티가 맨 위에 있을 때 링크가 오면 onNewIntent(singleTop). standard 면 새 인스턴스가 쌓인다
 * - onCreate 는 setContent 보다 먼저라 그 순간 화면(백스택)이 없다 → [DeepLinkHandler] 의 보류 칸
 */
open class DeepLinkDemoActivity : ComponentActivity() {

    protected open val launchModeLabel: String = "singleTop"

    private val instanceNo = instanceCounter.incrementAndGet()
    private lateinit var store: DeepLinkDemoStore
    private lateinit var handler: DeepLinkHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = DeepLinkDemoStore.get(this)
        handler = DeepLinkHandler(settings = { store.settings.value }, log = { store.addLog("#$instanceNo $it") })

        val restored = savedInstanceState != null
        val raw = intent?.data?.toString()
        store.addLog(
            "#$instanceNo onCreate(${if (restored) "재생성 — savedInstanceState 있음" else "새로 시작"}) · task $taskId · $launchModeLabel" +
                (raw?.let { " · intent.data $it" } ?: "")
        )
        if (raw != null) {
            if (!restored || !store.settings.value.recreateGuard) {
                logAndroidUri(raw)
                handler.handle(raw, if (restored) "onCreate(재생성 — 같은 intent 다시 처리)" else "onCreate(콜드)")
            } else {
                store.addLog("#$instanceNo 재생성이라 링크를 다시 처리하지 않음(savedInstanceState 확인)")
            }
        }

        setContent {
            DeepLinkDemoScreen(
                store = store,
                handler = handler,
                header = "인스턴스 #$instanceNo · task $taskId · $launchModeLabel",
                scheme = if (launchModeLabel == "standard") DEEP_LINK_SCHEME_STANDARD else DEEP_LINK_SCHEME,
                onFinish = { finish() }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val raw = intent.data?.toString() ?: return
        logAndroidUri(raw)
        handler.handle(raw, "onNewIntent(웜)")
    }

    /** 같은 문자열을 android.net.Uri 로도 나눠 본다 — java.net.URI 와 host·path 가 같다는 근거 */
    private fun logAndroidUri(raw: String) {
        val uri = raw.toUri()
        store.addLog("#$instanceNo android.net.Uri host=${uri.host} pathSegments=${uri.pathSegments}")
    }
}

/** 같은 화면을 launchMode standard 로 — 스킴 `composesample-standard://` */
class DeepLinkStandardActivity : DeepLinkDemoActivity() {
    override val launchModeLabel: String = "standard"
}

@Composable
private fun DeepLinkDemoScreen(
    store: DeepLinkDemoStore,
    handler: DeepLinkHandler,
    header: String,
    scheme: String,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val settings by store.settings.collectAsState()
    val log by store.log.collectAsState()

    // NavController 처럼 백스택은 화면이 들고, 재생성·프로세스 종료 뒤 복원에도 남는다
    val backStack: SnapshotStateList<String> = rememberSaveable(
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) { mutableStateListOf() }
    val navigator = remember(backStack) {
        DeepLinkNavigator { destination, synthesize ->
            val next = nextBackStack(backStack.toList(), destination, synthesize)
            backStack.clear()
            backStack.addAll(next)
        }
    }
    // 첫 컴포지션이 끝난 뒤에야 내비게이션이 '준비됨' — 그 전에 온 링크는 보류 칸에서 꺼낸다
    DisposableEffect(handler, navigator) {
        handler.attach(navigator)
        onDispose { handler.detach() }
    }
    // 하나 남았을 때의 뒤로 = 액티비티 종료(합성 백스택이 없으면 링크 화면에서 바로 앱 밖으로 나간다)
    BackHandler(enabled = backStack.size > 1) { backStack.removeAt(backStack.lastIndex) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Deep Link Demo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Text(header, fontSize = 12.sp, color = Color(0xFF616161), fontFamily = FontFamily.Monospace)
        }
        item {
            DemoCard("현재 화면", Color(0xFFE3F2FD)) {
                val top = backStack.lastOrNull()
                Text(screenTitle(top), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                Spacer(modifier = Modifier.height(4.dp))
                Text(screenBody(top), fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 18.sp)
                if (top != null && top.startsWith("invalid/")) {
                    DemoButton("홈으로", Color(0xFF1565C0)) {
                        backStack.clear()
                        backStack.add("home")
                    }
                }
            }
        }
        item {
            DemoCard("백스택 (아래가 맨 위)", Color(0xFFECEFF1)) {
                if (backStack.isEmpty()) Mono("비어 있음")
                backStack.forEachIndexed { index, route -> Mono("${index + 1}. $route") }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "뒤로: 둘 이상이면 하나 꺼내고, 하나 남았으면 이 액티비티를 닫는다.",
                    fontSize = 12.sp, color = Color(0xFF616161)
                )
                DemoButton("닫기", Color(0xFF616161)) { onFinish() }
            }
        }
        item {
            DemoCard("이 화면 위에서 링크 열기 (웜 — singleTop 은 onNewIntent, standard 는 새 인스턴스)", Color(0xFFE8F5E9)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("$scheme://order/77", "$scheme://profile/lee", "$scheme://nope").forEach { link ->
                        DemoButton(link.substringAfter("://"), Color(0xFF2E7D32)) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
                        }
                    }
                }
            }
        }
        item {
            DemoCard("설정 (다음 링크부터 — 예제 탭과 같은 값)", Color(0xFFFFF3E0)) {
                SettingsChips(settings) { transform -> store.updateSettings(transform) }
            }
        }
        item {
            DemoCard("수신 기록 (최신이 위)", Color(0xFFF3E5F5)) {
                log.take(16).forEach { Mono(it) }
            }
        }
    }
}

private fun screenTitle(route: String?): String = when {
    route == null -> "(빈 화면)"
    route == "home" -> "홈"
    route.startsWith("order/") -> "주문 ${route.removePrefix("order/")} 상세"
    route.startsWith("profile/") -> "프로필 ${route.removePrefix("profile/")}"
    else -> "잘못된 링크"
}

private fun screenBody(route: String?): String = when {
    route == null -> "백스택이 비어 있다 — 링크가 내비게이션 준비 전에 와서 버려졌다(보류 칸 꺼짐)."
    route == "home" -> "앱의 첫 화면."
    route.startsWith("invalid/") -> "사유: ${route.removePrefix("invalid/")} — 추측해서 열지 않고 여기로 보낸다."
    else -> "링크에서 꺼낸 값으로 연 화면."
}

/** 설정 칩 — 예제 탭과 데모 액티비티가 함께 쓴다 */
@Composable
internal fun SettingsChips(settings: DeepLinkSettings, onChange: ((DeepLinkSettings) -> DeepLinkSettings) -> Unit) {
    Column {
        Text("파서", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ParserMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.parser == mode,
                    onClick = { onChange { it.copy(parser = mode) } },
                    label = { Text(mode.label, fontSize = 12.sp) }
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = settings.recreateGuard,
                onClick = { onChange { it.copy(recreateGuard = !it.recreateGuard) } },
                label = { Text("재생성이면 다시 처리 안 함", fontSize = 12.sp) }
            )
            FilterChip(
                selected = settings.pendingSlot,
                onClick = { onChange { it.copy(pendingSlot = !it.pendingSlot) } },
                label = { Text("준비 전 링크 보류", fontSize = 12.sp) }
            )
            FilterChip(
                selected = settings.synthesizeBackStack,
                onClick = { onChange { it.copy(synthesizeBackStack = !it.synthesizeBackStack) } },
                label = { Text("백스택 합성(홈 → 목적지)", fontSize = 12.sp) }
            )
        }
    }
}

@Composable
private fun DemoCard(title: String, color: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun Mono(text: String) {
    Text(text, fontSize = 11.sp, color = Color(0xFF424242), fontFamily = FontFamily.Monospace, lineHeight = 15.sp)
}

@Composable
private fun DemoButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}
