package com.example.composesample.presentation.example.component.system.platform.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.composesample.presentation.MainHeader

/**
 * WebView JS ↔ 네이티브 브리지 예제
 *
 * 웹 → 앱(addJavascriptInterface), 앱 → 웹(evaluateJavascript), 양방향(WebMessagePort) 세 통로를 카드마다 로컬 HTML 로 띄워
 * 스레드·동기성·타입 변환·결과 형식·오리진·requestId 짝짓기·주입 범위를 실측한다. 웹 → 앱은 카드 안 웹 페이지의 버튼,
 * 앱 → 웹은 앱(Compose) 버튼이다. WebView 홀더·페이지는 WebViewBridgePages.kt, 순수 로직은 BridgeProtocol.kt, 출처는 exampleGuide.kt.
 *
 * 카드 사이를 오가도 WebView 가 다시 만들어지지 않도록 LazyColumn 대신 스크롤 Column 을 쓴다(지나간 카드의 기록 보존).
 */
@Composable
fun WebViewBridgeExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "WebView Bridge Example",
            onBackIconClicked = onBackEvent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OverviewCard()
            ThreadCard()
            TypeCard()
            EvaluateCard()
            PortCard()
            ProtocolCard()
            SecurityCard()
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard() {
    SectionCard(
        title = "🌉 WebView ↔ 앱 세 통로",
        description = "웹이 앱을 부르는 길, 앱이 웹을 부르는 길, 둘이 주고받는 포트. 셋은 불리는 스레드·기다리는 방식·" +
            "주고받는 값이 다르다. 아래 카드의 웹 페이지는 전부 앱에 들어 있는 로컬 HTML(loadDataWithBaseURL)이다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        InfoLine("웹 → 앱", "addJavascriptInterface + @JavascriptInterface")
        SmallLabel("JS 는 반환될 때까지 멈춘다 · JavaBridge 스레드 · 원시값·String 만 온전하다")
        Spacer(modifier = Modifier.height(6.dp))
        InfoLine("앱 → 웹", "evaluateJavascript(script) { result -> }")
        SmallLabel("기다리지 않는다 · 결과 콜백은 메인 · 결과는 JSON 텍스트(예외도 null)")
        Spacer(modifier = Modifier.height(6.dp))
        InfoLine("양방향", "createWebMessageChannel + postWebMessage")
        SmallLabel("포트로 문자열 · 콜백은 메인 · 보낼 때 받을 오리진을 정한다(메인 프레임)")
        Spacer(modifier = Modifier.height(10.dp))
        CodeBox(
            """
            // AndroidView onRelease — 이 화면의 모든 WebView 가 같은 순서로 정리된다
            webView.removeJavascriptInterface("ThreadBridge")
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.removeAllViews()
            webView.destroy()
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측 환경: SM-A725F(API 33) · Android System WebView 154 · debug·release 동일. 웹 → 앱은 카드 안 웹 페이지의 버튼, " +
                "앱 → 웹은 그 아래 앱 버튼이다."
        )
    }
}

// ==================== 2. 스레드와 동기 호출 ====================

@Composable
private fun ThreadCard() {
    val holder = remember { ThreadCardHolder() }

    SectionCard(
        title = "🧵 ① 웹 → 앱 — 어느 스레드에서, 얼마나 기다리나",
        description = "웹 페이지 버튼이 @JavascriptInterface 메서드를 부른다. 앱은 불린 스레드를, 웹은 반환값과 기다린 시간을 " +
            "기록한다. 브리지 안에서 WebView 를 바로 부르면 무슨 일이 생기는지도 본다.",
        containerColor = Color(0xFFE8F5E9)
    ) {
        BridgeWebView(holder, 132.dp)
        Spacer(modifier = Modifier.height(8.dp))
        SubTitle("앱이 기록한 호출 (최신이 위)")
        if (holder.calls.isEmpty()) SmallLabel("아직 호출 없음 — 위 웹 페이지의 버튼을 누른다")
        holder.calls.forEach { SmallLabel("${it.method} · ${it.thread}${if (it.note.isEmpty()) "" else " · ${it.note}"}") }
        Spacer(modifier = Modifier.height(6.dp))
        SubTitle("웹이 받은 결과")
        holder.reports.forEach { (label, value) -> SmallLabel("$label → $value") }
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton("기록 지우기", Color(0xFF607D8B)) { holder.clear() }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 네 호출 모두 JavaBridge 스레드다. slow(300) 동안 JS 는 약 308ms 멈췄다 — 브리지 메서드가 끝날 때까지 " +
                "페이지 스크립트가 다음 줄로 가지 않는다. 브리지 안에서 WebView 를 바로 부르면 RuntimeException" +
                "(A WebView method was called on thread 'JavaBridge')이고, 앱은 죽지 않은 채 JS 가 " +
                "'Java exception was raised during method invocation' 오류를 받는다. View.post 로 넘기면 메인에서 실행된다.\n" +
                "→ 오래 걸리는 일은 브리지 메서드 밖으로 넘기고, 결과는 ⑤ 처럼 requestId 를 붙여 돌려준다."
        )
    }
}

// ==================== 3. 타입 변환 ====================

@Composable
private fun TypeCard() {
    val holder = remember { TypeCardHolder() }

    SectionCard(
        title = "🔤 ② 인자·반환 타입은 브리지가 바꾼다",
        description = "JS 값을 String·Int·Boolean·Long 매개변수로 받고, Long·null·배열·List·객체를 돌려준다. " +
            "브리지는 맞지 않는 값을 오류 없이 바꿔 넘기므로, 실제로 무엇이 도착하는지 표로 본다.",
        containerColor = Color(0xFFFFF8E1)
    ) {
        BridgeWebView(holder, 64.dp)
        Spacer(modifier = Modifier.height(8.dp))
        if (holder.rows.isEmpty()) SmallLabel("웹 페이지의 '타입 표 실행' 을 누른다")
        holder.rows.forEach { SmallLabel("${it.kind.padEnd(9)} ${it.js} ⇒ ${it.received}") }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: String 매개변수에 undefined·객체·배열을 넘기면 오류 없이 문자열 \"undefined\" 가 오고 null 만 null 이다. " +
                "Int 는 소수점 아래를 버리고(3.7 → 3, -3.7 → -3) 문자열·불리언·null·undefined 는 0, Boolean 은 true 가 아니면 false. " +
                "2^53+1 은 JS 숫자로 만드는 순간 9007199254740992 가 된다. 반환은 null String → undefined, IntArray → undefined, " +
                "List·일반 객체 → 빈 객체({})인데, 돌려준 객체의 @JavascriptInterface 메서드(getName)는 JS 에서 불린다 — " +
                "돌려준 객체도 브리지가 된다. 어노테이션 없는 메서드·getClass 는 보이지 않고, 인자 수가 틀리면 'Method not found'.\n" +
                "→ 값은 JSON 문자열 하나로 주고받고 양쪽에서 파싱한다."
        )
    }
}

// ==================== 4. evaluateJavascript ====================

@Composable
private fun EvaluateCard() {
    val holder = remember { EvaluateCardHolder() }

    SectionCard(
        title = "📤 ③ 앱 → 웹 — evaluateJavascript 결과는 JSON 텍스트",
        description = "앱이 식을 실행하고 콜백으로 받은 문자열을 그대로 적는다. 문자열 결과에는 따옴표가 붙어 오고, " +
            "Promise·DOM 노드·예외는 값 대신 다른 것이 온다. 새 페이지를 로드하자마자 실행하면 어느 문서에서 도는지도 본다.",
        containerColor = Color(0xFFF3E5F5)
    ) {
        BridgeWebView(holder, 44.dp)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("식 ${EvalExpressions.size}개 실행", Color(0xFF7B1FA2)) { holder.evaluateAll() }
            DemoButton("로드 직후 vs onPageFinished", Color(0xFF6A1B9A)) { holder.evaluateRightAfterLoad() }
            DemoButton("Promise 값 받기", Color(0xFF4A148C)) { holder.promiseViaBridge() }
        }
        Spacer(modifier = Modifier.height(6.dp))
        holder.rows.forEach { row ->
            val label = if (row.label.isEmpty()) "" else "[${row.label}] "
            SmallLabel("$label${row.expr}  ⇒  ${row.raw ?: "(null 인자)"}  · ${row.thread}")
        }
        if (holder.delivered.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            SubTitle("웹이 브리지로 보낸 값")
            holder.delivered.forEach { SmallLabel(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 콜백은 메인 스레드, 결과는 JSON 텍스트다 — 문자열은 따옴표·이스케이프가 붙은 채 오므로 JSON 으로 풀어야 한다. " +
                "undefined·NaN·throw·ReferenceError 는 전부 null 이라 '값이 null' 과 구분되지 않는다(예외 내용은 콘솔에만). " +
                "Promise·DOM 노드는 {}, new Date(0) 은 0.0, 2^53+1 은 9.007199254740992e+15. 로드를 요청한 직후 실행한 식은 " +
                "이전 문서에서 돌았다(제목이 앞 페이지) — onPageFinished 뒤에 실행한다. Promise 값은 then 에서 브리지로 받는데, " +
                "그 호출은 다시 JavaBridge 스레드로 온다."
        )
    }
}

// ==================== 5. WebMessagePort ====================

@Composable
private fun PortCard() {
    val holder = remember { PortCardHolder() }

    SectionCard(
        title = "📮 ④ WebMessagePort — 보낼 때 받을 오리진을 정한다",
        description = "앱이 메시지 채널을 만들어 한쪽 포트를 페이지에 넘긴다. 이 페이지의 오리진은 $BRIDGE_ORIGIN 이다. " +
            "대상 오리진이 맞을 때·다를 때·* 일 때 포트가 전달되는지, 이후 메시지가 어느 스레드로 오는지 본다.",
        containerColor = Color(0xFFE0F7FA)
    ) {
        BridgeWebView(holder, 84.dp)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("포트 전달 · $BRIDGE_ORIGIN", Color(0xFF00838F)) { holder.sendPort(BRIDGE_ORIGIN) }
            DemoButton("포트 전달 · $OTHER_ORIGIN", Color(0xFFC62828)) { holder.sendPort(OTHER_ORIGIN) }
            DemoButton("포트 전달 · *", Color(0xFF00695C)) { holder.sendPort("*") }
            DemoButton("앱 → 웹 ping", Color(0xFF006064)) { holder.ping() }
            DemoButton("기록 지우기", Color(0xFF607D8B)) { holder.clear() }
        }
        Spacer(modifier = Modifier.height(6.dp))
        holder.events.forEach { SmallLabel(it) }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 대상 오리진이 페이지 오리진과 같거나 * 이면 포트가 전달되고, 다르면 앱 쪽엔 아무 오류 없이 전달되지 않는다 — " +
                "그 뒤 보낸 ping 도 사라지고, 페이지 콘솔에만 'target origin … does not match' 가 남는다. 포트 콜백은 메인 스레드, " +
                "페이지가 받은 event.origin 은 빈 문자열이다. postWebMessage 는 메인 프레임으로 가므로, 포트를 받은 페이지만 " +
                "앱과 말할 수 있다 — 모든 프레임에 붙는 ⑥ 의 인터페이스와 다른 점이다."
        )
    }
}

// ==================== 6. requestId 짝짓기 · 상태 ====================

@Composable
private fun ProtocolCard() {
    val holder = remember { ProtocolCardHolder() }

    SectionCard(
        title = "🔁 ⑤ requestId 로 요청과 응답 짝짓기",
        description = "웹이 request(handler, requestId, data) 로 부르면 앱은 일을 마친 뒤 onBridgeResponse(requestId, ok, data) 를 " +
            "실행해 돌려준다(dari 샘플과 같은 모양). 앱은 상태(IN_PROGRESS·SUCCESS·ERROR)와 웹 수신 여부를 따로 적는다.",
        containerColor = Color(0xFFFCE4EC)
    ) {
        BridgeWebView(holder, 104.dp)
        Spacer(modifier = Modifier.height(8.dp))
        SubTitle("웹의 짝짓기 방식")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PairingMode.entries.forEach { mode ->
                FilterChip(
                    selected = holder.pairingMode == mode,
                    onClick = { holder.changePairingMode(mode) },
                    label = { Text(mode.label, fontSize = 12.sp) }
                )
            }
        }
        SubTitle("응답을 JS 코드에 끼워 넣는 방식")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EscapeMode.entries.forEach { mode ->
                FilterChip(
                    selected = holder.escapeMode == mode,
                    onClick = { holder.escapeMode = mode },
                    label = { Text(mode.label, fontSize = 12.sp) }
                )
            }
        }
        val sample = "첫 줄\r\n둘째 줄"
        SmallLabel("\"첫 줄\\r\\n둘째 줄\" 을 넣은 리터럴의 날 줄바꿈 수 — JSON ${rawNewlineCount(jsonJsLiteral(sample))} · 손 ${rawNewlineCount(manualJsLiteral(sample))}")
        Spacer(modifier = Modifier.height(6.dp))
        SubTitle("앱 기록 (#·handler·requestId·상태·웹 수신·소요)")
        if (holder.entries.isEmpty()) SmallLabel("아직 요청 없음 — 웹 페이지의 버튼을 누른다")
        holder.entries.takeLast(12).forEach { entry ->
            val duration = entry.durationMs?.let { "${it}ms" } ?: "-"
            SmallLabel("#${entry.seq} ${entry.handler} ${entry.requestId ?: "(id 없음)"} · ${entry.status} · ${entry.delivery} · $duration")
        }
        Spacer(modifier = Modifier.height(6.dp))
        SubTitle("웹이 받은 결과 (최신이 위)")
        holder.webResults.forEach { SmallLabel(it) }
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton("기록 지우기", Color(0xFF607D8B)) { holder.clear() }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: requestId 로 짝지으면 응답이 B(200)·C(400)·A(600) 순서로 와도 셋 다 자기 응답을 받는다. 도착 순서(큐)로 " +
                "짝지으면 셋 다 남의 응답을 받았는데 앱 기록은 셋 다 SUCCESS·DELIVERED — 어긋남은 웹에서만 보인다. 응답 없는 요청은 " +
                "앱에서 영영 IN_PROGRESS, 웹은 3초 시간 초과로 끝낸다. 손 이스케이프로 \\r 섞인 일반 텍스트를 넣으면 응답 스크립트가 " +
                "SyntaxError(Invalid or unexpected token)라 앱은 SUCCESS 인데 웹 수신 FAILED, 웹은 시간 초과다. JSON 문자열 표기는 " +
                "날 줄바꿈이 0 이라 그대로 받는다. dari 샘플은 JSONObject.toString() 결과(이미 \\r 이 이스케이프됨)만 넘겨 이 경로를 타지 않는다."
        )
    }
}

// ==================== 7. 모든 프레임·오리진에 주입 ====================

@Composable
private fun SecurityCard() {
    val holder = remember { SecurityCardHolder() }

    SectionCard(
        title = "🛡️ ⑥ 인터페이스는 모든 프레임·모든 오리진에 주입된다",
        description = "신뢰 페이지 안의 iframe 3개(srcdoc·sandbox·data:)와, 같은 WebView 가 이동한 다른 오리진 페이지가 " +
            "모두 같은 브리지를 부를 수 있는지 본다. 앱 쪽 메서드에는 누가 불렀는지 알려 주는 정보가 없다.",
        containerColor = Color(0xFFEFEBE9)
    ) {
        BridgeWebView(holder, 140.dp)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("신뢰 페이지", Color(0xFF5D4037)) { holder.loadTrusted() }
            DemoButton("다른 오리진으로 이동", Color(0xFFC62828)) { holder.loadOther() }
            DemoButton("지금 페이지에서 다시 호출", Color(0xFF4E342E)) { holder.callAgain() }
            DemoButton("typeof 확인", Color(0xFF3E2723)) { holder.checkTypeof() }
            if (holder.attached) {
                DemoButton("인터페이스 제거", Color(0xFF8D6E63)) { holder.detach() }
            } else {
                DemoButton("인터페이스 다시 붙이기", Color(0xFF8D6E63)) { holder.attach() }
            }
            DemoButton("기록 지우기", Color(0xFF607D8B)) { holder.clear() }
        }
        Row {
            FilterChip(
                selected = holder.originCheck,
                onClick = { holder.changeOriginCheck(!holder.originCheck) },
                label = { Text("앱 쪽 최상위 오리진 검사 ($BRIDGE_ORIGIN 만)", fontSize = 12.sp) }
            )
        }
        holder.typeofResult?.let { SmallLabel("typeof ExposedBridge → $it") }
        Spacer(modifier = Modifier.height(6.dp))
        SubTitle("앱이 받은 호출 (#·자칭 출처 / 최상위 URL·판정)")
        holder.rows.forEach { row ->
            SmallLabel("#${row.seq} ${row.claimed}\n    최상위 ${row.topUrl} · ${row.verdict}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 신뢰 페이지 안의 srcdoc·sandbox·data: iframe 이 모두 브리지를 불렀고(sandbox·data: 는 origin=null), 같은 WebView 가 " +
                "other.example 로 이동해도 그대로 불렸다. 앱 메서드가 아는 것은 인자뿐이라 iframe 과 메인 프레임을 가를 수 없다.\n" +
                "앱 쪽 최상위 오리진 검사(onPageStarted 로 적어 둔 URL)는 늦다 — 로드 즉시 부른 호출이 콜백보다 먼저 와서, " +
                "other.example 의 첫 호출은 이전 URL 로 '처리'(우회), 신뢰 페이지의 첫 호출은 '거부'됐다(3회 반복 동일). " +
                "화면을 처음 열 때의 호출은 최상위 URL 이 아직 빈 값이었다(어느 호출까지 늦는지는 실행마다 다르다).\n" +
                "removeJavascriptInterface 뒤에도 지금 페이지에서는 typeof 가 object 이고 호출이 앱에 닿았다 — 다음 로드부터 사라진다.\n" +
                "→ 신뢰하는 콘텐츠만 띄운다. 보낸 프레임의 오리진이 필요하면 androidx.webkit 의 addWebMessageListener" +
                "(허용 오리진 규칙 + 메시지마다 sourceOrigin·isMainFrame)를 쓴다 — 이 프로젝트에는 아직 없는 의존성이다."
        )
    }
}

// ==================== 공통 ====================

@Composable
private fun BridgeWebView(holder: BridgeCardHolder, height: Dp) {
    val shape = RoundedCornerShape(8.dp)
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .border(1.dp, Color(0xFFB0BEC5), shape),
        factory = { context -> holder.create(context) },
        // Composable 이탈 시 인터페이스·타이머·네이티브 리소스 해제 — 누락하면 WebView 가 액티비티를 붙잡는다
        onRelease = { webView -> holder.release(webView) }
    )
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SubTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF455A64),
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
        Text(value, fontSize = 12.sp, color = Color(0xFF424242), fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SmallLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF616161),
        fontFamily = FontFamily.Monospace,
        lineHeight = 15.sp,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun CodeBox(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp),
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = Color(0xFFE0E0E0),
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun NoteBox(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x14000000))
            .padding(10.dp),
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = Color(0xFF424242)
    )
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
