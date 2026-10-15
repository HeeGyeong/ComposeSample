package com.example.composesample.presentation.example.component.system.platform.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.webkit.JavascriptInterface
import android.webkit.WebMessage
import android.webkit.WebMessagePort
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import com.google.gson.JsonParser

/**
 * WebView 브리지 예제의 카드별 WebView 홀더와 로컬 HTML 페이지.
 *
 * - WebView 는 View 라 ViewModel 에 두지 않고(액티비티 누수) 카드 컴포지션 수명으로 remember 하는 홀더가 가진다.
 * - WebViewClient·WebMessageCallback 같은 프레임워크 클래스 상속은 컴포저블 안 익명 객체 대신 이름 있는 클래스로 두고,
 *   홀더 자신의 상태 프로퍼티에 쓴다(컴포저블 by 지역 변수에 쓰면 debug HotSwan 에서 NoSuchMethodError — ComposeHotReloadGuide ⑰).
 * - @JavascriptInterface 메서드는 JavaBridge 스레드에서 불린다 → 화면 상태는 onMain 으로 메인에 넘겨 쓴다.
 * - 페이지는 loadDataWithBaseURL 로 띄운다 — 오리진은 baseUrl 의 오리진(https://bridge.example)이고 네트워크는 쓰지 않는다.
 */

const val BRIDGE_ORIGIN = "https://bridge.example"
const val OTHER_ORIGIN = "https://other.example"
private const val BRIDGE_BASE_URL = "$BRIDGE_ORIGIN/"
private const val OTHER_BASE_URL = "$OTHER_ORIGIN/"
private const val MAX_ROWS = 12

private fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

private fun threadLabel(): String = Thread.currentThread().name + if (isMainThread()) " (메인)" else ""

// ==================== 공통 홀더 ====================

abstract class BridgeCardHolder(private val defaultBaseUrl: String = BRIDGE_BASE_URL) {
    protected val main = Handler(Looper.getMainLooper())

    // 브리지 스레드도 읽으므로 @Volatile — 읽기만 하고 WebView 메서드는 부르지 않는다(부르면 예외, 2번 카드)
    @Volatile
    var webView: WebView? = null
        private set

    var pageLoads by mutableIntStateOf(0)
        private set

    private var lastBaseUrl = defaultBaseUrl
    private var lastHtml: String? = null

    protected abstract fun pageHtml(): String

    /**
     * addJavascriptInterface 는 다음 로드부터 JS 에 보인다 → 첫 로드 전에 붙인다.
     * 붙일 객체는 구체 타입으로 바로 넘긴다 — Any 로 감싸 넘기면 lint(JavascriptInterface)가 @JavascriptInterface 를 못 본다
     */
    protected abstract fun attachInterfaces(webView: WebView)

    /** release 때 removeJavascriptInterface 할 이름 — 붙어 있지 않은 이름을 지워도 문제없다 */
    protected open val interfaceNames: List<String> = emptyList()

    @SuppressLint("SetJavaScriptEnabled") // 브리지 예제라 JS 가 필요하다 — 앱에 들어 있는 로컬 HTML 만 띄운다
    fun create(context: Context): WebView {
        val created = WebView(context)
        created.settings.javaScriptEnabled = true
        created.webViewClient = BridgePageClient(this)
        webView = created
        attachInterfaces(created)
        load()
        return created
    }

    fun load(baseUrl: String = defaultBaseUrl, html: String = pageHtml()) {
        lastBaseUrl = baseUrl
        lastHtml = html
        webView?.loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null)
    }

    /** 마지막으로 띄운 페이지를 다시 띄운다 */
    fun reload() = load(lastBaseUrl, lastHtml ?: pageHtml())

    internal open fun onPageStarted(url: String?) {}

    internal open fun onPageFinished(url: String?) {
        pageLoads++
    }

    /** 브리지 콜백(JavaBridge 스레드)에서 메인으로 넘긴다 — 정리된 뒤 도착한 콜백은 버린다 */
    protected fun onMain(block: () -> Unit) {
        main.post { if (webView != null) block() }
    }

    protected fun <T> MutableList<T>.addCapped(item: T) {
        add(0, item)
        while (size > MAX_ROWS) removeAt(lastIndex)
    }

    /** AndroidView onRelease — 인터페이스 제거 → 로딩 중단 → 빈 페이지 → 자식 뷰 제거 → destroy (LIFECYCLE-01 관례) */
    open fun release(target: WebView) {
        main.removeCallbacksAndMessages(null)
        interfaceNames.forEach(target::removeJavascriptInterface)
        target.stopLoading()
        target.loadUrl("about:blank")
        target.removeAllViews()
        target.destroy()
        webView = null
    }
}

class BridgePageClient(private val holder: BridgeCardHolder) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        holder.onPageStarted(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        holder.onPageFinished(url)
    }
}

private const val PAGE_STYLE = """
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<style>
body{margin:0;padding:6px 8px;font:13px/1.4 sans-serif;background:#fff;color:#212121}
button{font:12px sans-serif;padding:6px 8px;margin:2px 2px 2px 0;border:0;border-radius:6px;background:#1565C0;color:#fff}
#out{font:11px/1.45 monospace;color:#37474F;margin-top:4px;white-space:pre-wrap}
</style>
"""

// ==================== 2번 카드: 스레드와 동기 호출 ====================

data class ThreadRow(val method: String, val thread: String, val note: String)

class ThreadCardHolder : BridgeCardHolder() {
    val calls = mutableStateListOf<ThreadRow>()
    val reports = mutableStateListOf<Pair<String, String>>()

    override fun pageHtml() = THREAD_PAGE

    override val interfaceNames = listOf("ThreadBridge")

    override fun attachInterfaces(webView: WebView) = webView.addJavascriptInterface(ThreadBridge(this), "ThreadBridge")

    /** 불린 스레드는 여기서(브리지 스레드) 읽고, 기록은 메인에서 */
    internal fun record(method: String, note: String = "") {
        val row = ThreadRow(method, threadLabel(), note)
        onMain { calls.addCapped(row) }
    }

    internal fun report(label: String, value: String) = onMain { reports.addCapped(label to value) }

    /** 브리지 스레드에서 WebView 메서드를 바로 부른다 — WebView 는 만든 스레드(메인)에서만 불러야 한다 */
    internal fun touchWebViewDirectly() {
        webView?.evaluateJavascript("1", null)
    }

    /** View.post 는 어느 스레드에서나 부를 수 있다 — 실행은 메인에서 */
    internal fun touchWebViewPosted() {
        val target = webView ?: return
        target.post { if (webView === target) target.evaluateJavascript("postedArrived()", null) }
    }

    fun clear() {
        calls.clear()
        reports.clear()
    }
}

class ThreadBridge(private val holder: ThreadCardHolder) {
    @JavascriptInterface
    fun echo(text: String?): String {
        holder.record("echo")
        return "앱이 받음: $text"
    }

    @JavascriptInterface
    fun slow(ms: Int) {
        holder.record("slow($ms)", "Thread.sleep($ms)")
        Thread.sleep(ms.toLong())
    }

    @JavascriptInterface
    fun touchWebViewDirectly() {
        try {
            holder.touchWebViewDirectly()
            holder.record("WebView 직접 호출", "예외 없음")
        } catch (e: RuntimeException) {
            val firstSentence = e.message.orEmpty().substringBefore(". ").take(90)
            holder.record("WebView 직접 호출", "${e.javaClass.simpleName}: $firstSentence")
            throw e // JS 쪽이 무엇을 받는지 보려고 그대로 던진다
        }
    }

    @JavascriptInterface
    fun touchWebViewPosted() {
        holder.record("post 로 넘김")
        holder.touchWebViewPosted()
    }

    @JavascriptInterface
    fun report(label: String, value: String) = holder.report(label, value)
}

private val THREAD_PAGE = """
<!doctype html><html><head>$PAGE_STYLE<title>Thread</title></head><body>
<button onclick="echo()">echo('안녕')</button><button onclick="slow()">slow(300)</button><br>
<button onclick="direct()">브리지 안에서 WebView 직접 호출</button><button onclick="posted()">post 로 넘겨 호출</button>
<div id="out"></div>
<script>
function show(label, value) {
  var o = document.getElementById('out');
  o.textContent = label + ' → ' + value + '\n' + o.textContent;
  ThreadBridge.report(label, String(value));
}
function echo() { show('echo 반환값', ThreadBridge.echo('안녕')); }
function slow() {
  var t0 = performance.now();
  ThreadBridge.slow(300);
  show('slow(300) 동안 JS 가 멈춘 시간', Math.round(performance.now() - t0) + 'ms');
}
function direct() {
  try { ThreadBridge.touchWebViewDirectly(); show('직접 호출', '예외 없음'); }
  catch (e) { show('직접 호출 → JS 가 받은 예외', e.message); }
}
function posted() { ThreadBridge.touchWebViewPosted(); show('post 로 넘김', '반환됨(실행은 나중)'); }
function postedArrived() { show('post 로 넘긴 evaluateJavascript', '메인에서 실행됨'); }
</script></body></html>
""".trimIndent()

// ==================== 3번 카드: 인자·반환 타입 변환 ====================

data class TypeRow(val kind: String, val js: String, val received: String)

class TypeCardHolder : BridgeCardHolder() {
    val rows = mutableStateListOf<TypeRow>()

    override fun pageHtml() = TYPE_PAGE

    override val interfaceNames = listOf("TypeProbe")

    override fun attachInterfaces(webView: WebView) = webView.addJavascriptInterface(TypeProbe(this), "TypeProbe")

    internal fun row(kind: String, js: String, received: String) = onMain { rows.add(TypeRow(kind, js, received)) }

    // 메인 Handler 큐 순서 그대로 — 비우기가 이어지는 행 추가보다 먼저 실행된다
    internal fun begin() = onMain { rows.clear() }
}

/** 반환 타입 확인용 일반 클래스 — 이 객체를 돌려주면 JS 에는 어떻게 보이나 */
class BridgeSampleUser(private val name: String) {
    @JavascriptInterface
    fun getName(): String = name
}

class TypeProbe(private val holder: TypeCardHolder) {
    @JavascriptInterface
    fun begin() = holder.begin()

    @JavascriptInterface
    fun takeString(js: String, value: String?) = holder.row("→ String", js, value?.let { "\"$it\"" } ?: "null")

    @JavascriptInterface
    fun takeInt(js: String, value: Int) = holder.row("→ Int", js, value.toString())

    @JavascriptInterface
    fun takeBoolean(js: String, value: Boolean) = holder.row("→ Boolean", js, value.toString())

    @JavascriptInterface
    fun takeLong(js: String, value: Long) = holder.row("→ Long", js, value.toString())

    @JavascriptInterface
    fun giveLong(): Long = 9_007_199_254_740_993L

    @JavascriptInterface
    fun giveNullString(): String? = null

    @JavascriptInterface
    fun giveIntArray(): IntArray = intArrayOf(1, 2, 3)

    @JavascriptInterface
    fun giveList(): List<Int> = listOf(1, 2, 3)

    @JavascriptInterface
    fun giveObject(): BridgeSampleUser = BridgeSampleUser("kim")

    // @JavascriptInterface 없음 — API 17 부터 어노테이션 없는 메서드는 JS 에 노출되지 않는다
    fun notAnnotated(): String = "보이면 안 됨"

    @JavascriptInterface
    fun report(js: String, value: String) = holder.row("JS 가 받음", js, value)
}

private val TYPE_PAGE = """
<!doctype html><html><head>$PAGE_STYLE<title>Types</title></head><body>
<button onclick="run()">타입 표 실행 (웹 → 앱 → 웹)</button>
<div id="out">버튼을 누르면 JS 가 브리지 메서드를 차례로 부른다</div>
<script>
function describe(x) {
  var s;
  try { s = JSON.stringify(x); } catch (e) { s = '(stringify 오류)'; }
  return typeof x + ' · ' + String(s);
}
function run() {
  TypeProbe.begin();
  [['"abc"', 'abc'], ['42', 42], ['3.7', 3.7], ['true', true], ['null', null], ['undefined', undefined],
   ['{a:1}', {a: 1}], ['[1,2]', [1, 2]]].forEach(function (v) { TypeProbe.takeString(v[0], v[1]); });
  [['42', 42], ['3.7', 3.7], ['-3.7', -3.7], ['"42"', '42'], ['true', true], ['null', null],
   ['undefined', undefined]].forEach(function (v) { TypeProbe.takeInt(v[0], v[1]); });
  TypeProbe.takeBoolean('"false"', 'false');
  TypeProbe.takeBoolean('1', 1);
  TypeProbe.takeLong('9007199254740993', 9007199254740993);
  TypeProbe.report('giveLong() = 9007199254740993L', String(TypeProbe.giveLong()));
  TypeProbe.report('giveNullString() = null', describe(TypeProbe.giveNullString()));
  TypeProbe.report('giveIntArray() = [1,2,3]', describe(TypeProbe.giveIntArray()));
  TypeProbe.report('giveList() = listOf(1,2,3)', describe(TypeProbe.giveList()));
  var user = TypeProbe.giveObject();
  TypeProbe.report('giveObject() = BridgeSampleUser', describe(user) + ' · getName() → ' +
    (typeof user.getName === 'function' ? user.getName() : typeof user.getName));
  TypeProbe.report('typeof notAnnotated (어노테이션 없음)', typeof TypeProbe.notAnnotated);
  TypeProbe.report('typeof getClass', typeof TypeProbe.getClass);
  try { TypeProbe.takeString('인자 1개만'); TypeProbe.report('인자 수가 틀린 호출', '예외 없음'); }
  catch (e) { TypeProbe.report('인자 수가 틀린 호출', e.message); }
  document.getElementById('out').textContent = '실행 완료 — 결과는 아래 표';
}
</script></body></html>
""".trimIndent()

// ==================== 4번 카드: evaluateJavascript ====================

data class EvalRow(val label: String, val expr: String, val raw: String?, val thread: String)

/** 앱 → 웹으로 실행할 식 — 결과 콜백에 무엇이 오는지(JSON 텍스트) 그대로 보인다 */
val EvalExpressions = listOf(
    "1 + 1",
    "'따옴표 \"안\" 문자열'",
    "({ a: 1, b: [2, 3] })",
    "undefined",
    "null",
    "document.title",
    "9007199254740993",
    "NaN",
    "new Date(0)",
    "document.body",
    "Promise.resolve(42)",
    "(function () { throw new Error('boom'); })()",
    "notDefinedVariable.x"
)

class EvaluateCardHolder : BridgeCardHolder() {
    val rows = mutableStateListOf<EvalRow>()
    val delivered = mutableStateListOf<String>()
    private var titleRun = 0
    private var titleCheckPending = false

    override fun pageHtml() = evalPage("Bridge Page")

    override val interfaceNames = listOf("EvalBridge")

    override fun attachInterfaces(webView: WebView) = webView.addJavascriptInterface(EvalBridge(this), "EvalBridge")

    fun evaluate(expr: String, label: String = "") {
        val target = webView ?: return
        target.evaluateJavascript(expr) { raw -> rows.add(EvalRow(label, expr, raw, threadLabel())) }
    }

    fun evaluateAll() {
        rows.clear()
        EvalExpressions.forEach { evaluate(it) }
    }

    /** 새 페이지를 로드하자마자 실행 vs onPageFinished 뒤 실행 — 같은 식이 어느 문서에서 도는지 */
    fun evaluateRightAfterLoad() {
        rows.clear()
        titleRun++
        titleCheckPending = true
        load(html = evalPage("새 페이지 $titleRun"))
        evaluate("document.title", "load 직후 (새 페이지 $titleRun 로드 요청)")
    }

    override fun onPageFinished(url: String?) {
        super.onPageFinished(url)
        if (titleCheckPending) {
            titleCheckPending = false
            evaluate("document.title", "onPageFinished 뒤")
        }
    }

    /** evaluateJavascript 는 Promise 를 기다리지 않는다 → 값이 생기면 웹이 브리지로 보내게 한다 */
    fun promiseViaBridge() {
        rows.clear()
        evaluate("Promise.resolve(42).then(function (v) { EvalBridge.deliver('then 에서 받은 값 ' + v); })", "Promise + 브리지")
    }

    internal fun onDeliver(value: String) {
        val thread = threadLabel()
        onMain { delivered.addCapped("$value · $thread") }
    }
}

class EvalBridge(private val holder: EvaluateCardHolder) {
    @JavascriptInterface
    fun deliver(value: String) = holder.onDeliver(value)
}

private fun evalPage(title: String) = """
<!doctype html><html><head>$PAGE_STYLE<title>$title</title></head><body>
<div><b>$title</b> — 앱이 이 문서에 식을 넣어 실행한다</div>
<script>window.appState = { count: 3 };</script>
</body></html>
""".trimIndent()

// ==================== 5번 카드: WebMessagePort ====================

class PortCardHolder : BridgeCardHolder() {
    val events = mutableStateListOf<String>()
    private var appPort: WebMessagePort? = null
    private var pingCount = 0
    private var sendToken = 0
    private var readySinceSend = false

    override fun pageHtml() = PORT_PAGE

    override fun attachInterfaces(webView: WebView) = Unit // 이 카드는 JS 인터페이스를 쓰지 않는다

    /** 채널을 만들어 웹 쪽 포트를 targetOrigin 에게 넘긴다 — 오리진이 다르면 아무 오류 없이 전달되지 않는다 */
    fun sendPort(targetOrigin: String) {
        val target = webView ?: return
        appPort?.close()
        val (app, web) = target.createWebMessageChannel()
        app.setWebMessageCallback(PortMessageCallback(this))
        appPort = app
        target.postWebMessage(WebMessage("port", arrayOf(web)), targetOrigin.toUri())
        events.addCapped("앱 → postWebMessage(port, target=$targetOrigin)")
        val token = ++sendToken
        readySinceSend = false
        main.postDelayed({
            if (token == sendToken && !readySinceSend) events.addCapped("1초 동안 ready 없음 — 포트가 전달되지 않음(앱 쪽 오류 없음)")
        }, 1_000)
    }

    fun ping() {
        val port = appPort
        if (port == null) {
            events.addCapped("포트 없음 — 먼저 포트를 전달")
            return
        }
        port.postMessage(WebMessage("ping ${++pingCount}"))
        events.addCapped("앱 → 웹: ping $pingCount")
    }

    internal fun onPortMessage(data: String?) {
        if (data.orEmpty().startsWith("ready")) readySinceSend = true
        events.addCapped("웹 → 앱: $data · ${threadLabel()}")
    }

    fun clear() = events.clear()

    override fun release(target: WebView) {
        appPort?.close()
        appPort = null
        super.release(target)
    }
}

/** WebMessagePort 콜백 — setWebMessageCallback(callback) 은 Handler 를 안 주면 메인 스레드로 온다 */
class PortMessageCallback(private val holder: PortCardHolder) : WebMessagePort.WebMessageCallback() {
    override fun onMessage(port: WebMessagePort?, message: WebMessage?) {
        holder.onPortMessage(message?.data)
    }
}

private val PORT_PAGE = """
<!doctype html><html><head>$PAGE_STYLE<title>Port</title></head><body>
<button onclick="sendFromWeb()">웹 → 앱 (포트로 보내기)</button>
<div id="out">아직 포트 없음</div>
<script>
var port = null, n = 0;
function show(t) { var o = document.getElementById('out'); o.textContent = t + '\n' + o.textContent; }
window.addEventListener('message', function (e) {
  if (e.data !== 'port' || !e.ports.length) return;
  port = e.ports[0];
  port.onmessage = function (m) { show('앱 → 웹: ' + m.data); port.postMessage('pong (' + m.data + ')'); };
  port.postMessage('ready · event.origin=' + JSON.stringify(e.origin));
  show('포트 받음 · event.origin=' + JSON.stringify(e.origin));
});
function sendFromWeb() { if (port) port.postMessage('웹 버튼 ' + (++n)); else show('포트 없음'); }
</script></body></html>
""".trimIndent()

// ==================== 6번 카드: requestId 짝짓기 · 상태 ====================

enum class EscapeMode(val label: String) { JSON("JSON 문자열 표기"), MANUAL("손 이스케이프(\\ ' \\n 만)") }

enum class PairingMode(val label: String, val js: String) { REQUEST_ID("requestId 로", "id"), FIFO("도착 순서대로(큐)", "fifo") }

class ProtocolCardHolder : BridgeCardHolder() {
    private val tracker = BridgeMessageTracker { SystemClock.elapsedRealtime() }
    var entries by mutableStateOf(emptyList<BridgeEntry>())
        private set
    val webResults = mutableStateListOf<String>()
    var escapeMode by mutableStateOf(EscapeMode.JSON)
    var pairingMode by mutableStateOf(PairingMode.REQUEST_ID)
        private set

    override fun pageHtml() = PROTOCOL_PAGE

    override val interfaceNames = listOf("ProtocolBridge")

    override fun attachInterfaces(webView: WebView) = webView.addJavascriptInterface(ProtocolBridge(this), "ProtocolBridge")

    override fun onPageFinished(url: String?) {
        super.onPageFinished(url)
        applyPairingMode()
    }

    fun changePairingMode(mode: PairingMode) {
        pairingMode = mode
        applyPairingMode()
    }

    private fun applyPairingMode() {
        webView?.evaluateJavascript("setMode('${pairingMode.js}')", null)
    }

    fun clear() {
        tracker.clear()
        entries = tracker.entries
        webResults.clear()
        applyPairingMode()
    }

    internal fun onRequest(handler: String, requestId: String, data: String?) = onMain {
        tracker.onRequest(handler, requestId, data)
        entries = tracker.entries
        dispatch(handler, requestId, data)
    }

    internal fun onFireAndForget(handler: String, data: String?) = onMain {
        tracker.onRequest(handler, null, data)
        entries = tracker.entries
    }

    internal fun onWebResult(label: String, value: String) = onMain { webResults.addCapped("$label → $value") }

    private fun dispatch(handler: String, requestId: String, data: String?) {
        when (handler) {
            "slowEcho" -> {
                val delay = runCatching { JsonParser.parseString(data).asJsonObject["delay"].asLong }.getOrDefault(100L)
                main.postDelayed({ respond(requestId, true, """{"for":"$requestId","delay":$delay}""") }, delay)
            }
            "fail" -> respond(requestId, false, """{"error":"PERMISSION_DENIED"}""")
            "plainText" -> respond(requestId, true, "첫 줄\r\n둘째 줄") // 서버 메모·클립보드처럼 JSON 이 아닌 일반 텍스트
            "noResponse" -> Unit // 일부러 응답하지 않는다 — 앱 쪽은 영영 IN_PROGRESS
            else -> respond(requestId, false, """{"error":"Unknown handler","handler":"$handler"}""")
        }
    }

    /** 응답을 JS 코드에 끼워 실행한다 — 응답 함수가 true 를 돌려주면 웹이 받은 것, SyntaxError 면 결과가 null */
    private fun respond(requestId: String, success: Boolean, data: String) {
        val target = webView ?: return
        tracker.onResponse(requestId, success, data) ?: return
        entries = tracker.entries
        val literal = when (escapeMode) {
            EscapeMode.JSON -> jsonJsLiteral(data)
            EscapeMode.MANUAL -> manualJsLiteral(data)
        }
        target.evaluateJavascript("onBridgeResponse(${jsonJsLiteral(requestId)}, $success, $literal)") { raw ->
            tracker.onDelivery(requestId, raw == "true")
            entries = tracker.entries
        }
    }
}

class ProtocolBridge(private val holder: ProtocolCardHolder) {
    @JavascriptInterface
    fun request(handler: String, requestId: String, data: String?) = holder.onRequest(handler, requestId, data)

    @JavascriptInterface
    fun fireAndForget(handler: String, data: String?) = holder.onFireAndForget(handler, data)

    @JavascriptInterface
    fun report(label: String, value: String) = holder.onWebResult(label, value)
}

private val PROTOCOL_PAGE = """
<!doctype html><html><head>$PAGE_STYLE<title>Protocol</title></head><body>
<button onclick="concurrent()">동시 요청 3개 (600·200·400ms)</button><button onclick="fail()">에러 응답</button><br>
<button onclick="noResponse()">응답 없는 요청</button><button onclick="fireAndForget()">fire-and-forget</button><button onclick="plainText()">\r 섞인 텍스트 응답</button>
<div id="out"></div>
<script>
var mode = 'id', counter = 0, pending = {}, queue = [];
function show(t) { var o = document.getElementById('out'); o.textContent = t + '\n' + o.textContent; }
function setMode(m) { mode = m; pending = {}; queue = []; show('짝짓기: ' + (m === 'id' ? 'requestId 로' : '도착 순서대로(큐)')); }
function report(label, value) { show(label + ' → ' + value); ProtocolBridge.report(label, value); }
function call(handler, data) {
  var id = 'req_' + (++counter);
  return new Promise(function (resolve, reject) {
    var entry = { id: id, resolve: resolve, reject: reject };
    entry.timer = setTimeout(function () {
      delete pending[id];
      var i = queue.indexOf(entry); if (i >= 0) queue.splice(i, 1);
      reject('3초 시간 초과');
    }, 3000);
    if (mode === 'id') pending[id] = entry; else queue.push(entry);
    ProtocolBridge.request(handler, id, data == null ? null : JSON.stringify(data));
  });
}
window.onBridgeResponse = function (id, ok, data) {
  var entry = mode === 'id' ? pending[id] : queue.shift();
  if (!entry) return false;
  delete pending[entry.id];
  clearTimeout(entry.timer);
  (ok ? entry.resolve : entry.reject)(data);
  return true;
};
function concurrent() {
  [600, 200, 400].forEach(function (d, i) {
    var label = 'ABC'.charAt(i) + '(' + d + 'ms, req_' + (counter + 1) + ')';
    var mine = 'req_' + (counter + 1);
    call('slowEcho', { delay: d }).then(function (data) {
      var got = JSON.parse(data)['for'];
      report(label, (got === mine ? '자기 응답 ' : '다른 요청의 응답 ') + got);
    }, function (err) { report(label, '실패: ' + err); });
  });
}
function fail() { call('fail').then(function (d) { report('fail', '성공?? ' + d); }, function (e) { report('fail', 'reject ' + e); }); }
function noResponse() { call('noResponse').then(function (d) { report('noResponse', d); }, function (e) { report('noResponse', e); }); }
function fireAndForget() { ProtocolBridge.fireAndForget('logEvent', JSON.stringify({ screen: 'home' })); report('logEvent', '보내고 끝(응답 안 기다림)'); }
function plainText() {
  call('plainText').then(function (d) { report('plainText', '받음 ' + JSON.stringify(d)); }, function (e) { report('plainText', e); });
}
</script></body></html>
""".trimIndent()

// ==================== 7번 카드: 모든 프레임·오리진에 주입 ====================

data class ExposureRow(val seq: Int, val claimed: String, val topUrl: String, val verdict: String)

class SecurityCardHolder : BridgeCardHolder() {
    val rows = mutableStateListOf<ExposureRow>()
    var originCheck by mutableStateOf(false)
        private set
    var attached by mutableStateOf(true)
        private set
    var typeofResult by mutableStateOf<String?>(null)
        private set
    private var seq = 0 // 메인에서만 증가 — 목록이 MAX_ROWS 로 잘려도 번호가 겹치지 않게 따로 센다

    // 브리지 스레드가 읽는 값 — WebView.getUrl() 은 브리지 스레드에서 부를 수 없어 메인이 페이지 시작 때 적어 둔다
    @Volatile
    private var topUrlForBridge = ""

    @Volatile
    private var originCheckForBridge = false

    override fun pageHtml() = SECURITY_PAGE

    override val interfaceNames = listOf("ExposedBridge")

    override fun attachInterfaces(webView: WebView) = webView.addJavascriptInterface(ExposedBridge(this), "ExposedBridge")

    override fun onPageStarted(url: String?) {
        topUrlForBridge = url.orEmpty()
    }

    fun changeOriginCheck(enabled: Boolean) {
        originCheck = enabled
        originCheckForBridge = enabled
    }

    fun loadTrusted() = load(BRIDGE_BASE_URL, SECURITY_PAGE)

    fun loadOther() = load(OTHER_BASE_URL, OTHER_PAGE)

    fun callAgain() {
        webView?.evaluateJavascript("callAgain()", null)
    }

    fun detach() {
        webView?.removeJavascriptInterface("ExposedBridge")
        attached = false
    }

    fun attach() {
        webView?.addJavascriptInterface(ExposedBridge(this), "ExposedBridge")
        attached = true
    }

    fun checkTypeof() {
        webView?.evaluateJavascript("typeof ExposedBridge") { raw -> typeofResult = "${decodeEvaluateResult(raw)} (메인 프레임, 페이지 로드 ${pageLoads}회째)" }
    }

    fun clear() {
        rows.clear()
        seq = 0
    }

    /** JavaBridge 스레드 — 어느 프레임이 불렀는지는 알 수 없다. claimed 는 호출한 쪽이 스스로 적은 값이라 믿을 수 없다 */
    internal fun onPing(claimed: String): String {
        val top = topUrlForBridge
        val topOrigin = originOf(top)
        val allowed = !originCheckForBridge || topOrigin == BRIDGE_ORIGIN
        val verdict = if (allowed) "처리" else "거부"
        onMain { rows.addCapped(ExposureRow(++seq, claimed, top, verdict)) }
        return if (allowed) "앱이 처리함" else "앱이 거부함(최상위 $topOrigin)"
    }
}

class ExposedBridge(private val holder: SecurityCardHolder) {
    @JavascriptInterface
    fun ping(claimed: String): String = holder.onPing(claimed)
}

private val SECURITY_PAGE = """
<!doctype html><html><head>$PAGE_STYLE<title>Trusted</title></head><body>
<div><b>bridge.example</b> 페이지 — 아래 iframe 3개는 이 페이지가 넣었다</div>
<div id="out"></div>
<script>
function ping(why) {
  var o = document.getElementById('out');
  o.textContent = typeof ExposedBridge !== 'object' ? '메인 프레임: ExposedBridge 없음'
    : '메인 프레임: ' + ExposedBridge.ping('메인 프레임 ' + why + ' · origin=' + self.origin);
}
function frameHtml(kind) {
  return '<!doctype html><meta charset="utf-8"><body style="margin:0;font:11px monospace;color:#4E342E">' +
    '<span id="o"></span><script>' +
    'function ping(why){var o=document.getElementById("o");' +
    'if(typeof ExposedBridge!=="object"){o.textContent="' + kind + ': ExposedBridge 없음";return;}' +
    'o.textContent="' + kind + ': "+ExposedBridge.ping("' + kind + ' "+why+" · origin="+self.origin);}' +
    'ping("로드");addEventListener("message",function(){ping("다시 호출");});' +
    '<\/script>';
}
function addFrames() {
  ['srcdoc iframe', 'sandbox iframe', 'data: iframe'].forEach(function (kind) {
    var f = document.createElement('iframe');
    f.style.cssText = 'display:block;width:100%;height:20px;border:1px dashed #BCAAA4;margin-top:3px';
    var html = frameHtml(kind);
    if (kind === 'data: iframe') { f.src = 'data:text/html;charset=utf-8,' + encodeURIComponent(html); }
    else { if (kind === 'sandbox iframe') f.setAttribute('sandbox', 'allow-scripts'); f.srcdoc = html; }
    document.body.appendChild(f);
  });
}
function callAgain() { ping('다시 호출'); for (var i = 0; i < frames.length; i++) frames[i].postMessage('again', '*'); }
ping('로드');
addFrames();
</script></body></html>
""".trimIndent()

private val OTHER_PAGE = """
<!doctype html><html><head>$PAGE_STYLE<title>Other</title></head><body>
<div><b>other.example</b> 페이지 — 같은 WebView 가 다른 오리진으로 이동했다</div>
<div id="out"></div>
<script>
function ping(why) {
  var o = document.getElementById('out');
  o.textContent = typeof ExposedBridge !== 'object' ? 'ExposedBridge 없음'
    : ExposedBridge.ping('다른 오리진 페이지 ' + why + ' · origin=' + self.origin);
}
function callAgain() { ping('다시 호출'); }
ping('로드');
</script></body></html>
""".trimIndent()
