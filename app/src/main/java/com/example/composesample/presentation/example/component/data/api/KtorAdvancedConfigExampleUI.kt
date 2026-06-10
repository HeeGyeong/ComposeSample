package com.example.composesample.presentation.example.component.data.api

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Advanced Ktor Client Configuration — MockEngine 으로 실서버 없이 고급 구성 시연
 * - MockEngine: 실제 네트워크를 타지 않고 요청을 코드로 받아 원하는 응답을 돌려주는 가짜 엔진.
 *   덕분에 401/503 같은 상황을 결정적으로 재현해 실제 Auth/HttpRequestRetry 플러그인 동작을 검증할 수 있다.
 * - 3가지 시나리오를 같은 패턴으로 실행하고 실시간 타임라인 로그로 비교한다.
 * - 참고 자료(URL/핵심 개념)는 exampleGuide.kt 의 "KtorAdvancedConfig" 섹션 참고.
 */
@Composable
fun KtorAdvancedConfigExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Advanced Ktor Config (Auth/Retry)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item {
                // 1. Auth bearer — 만료 토큰으로 요청 → 401 → refreshTokens 자동 호출 → 재요청 성공
                ScenarioCard(
                    title = "1. Auth bearer (토큰 리프레시)",
                    description = "install(Auth) { bearer { loadTokens / refreshTokens } }. 만료된 액세스 토큰으로 보낸 요청이 401(+ WWW-Authenticate: Bearer)을 받으면 refreshTokens() 가 자동 호출되고, 새 토큰으로 재요청해 200 을 받는다.",
                    accent = Color(0xFF1E88E5),
                    run = ::runAuthRefreshScenario
                )
            }
            item {
                // 2. HttpRequestRetry — 503 두 번 → 지수 백오프 후 재시도 → 3번째 200
                ScenarioCard(
                    title = "2. HttpRequestRetry (지수 백오프)",
                    description = "install(HttpRequestRetry) { retryOnServerErrors; delayMillis { 지수 백오프 } }. 서버가 503 을 두 번 돌려주면 재시도마다 지연을 늘려(200ms→400ms) 재요청하고, 3번째 시도에서 200 으로 복구된다.",
                    accent = Color(0xFF43A047),
                    run = ::runRetryScenario
                )
            }
            item {
                // 3. 대조군 — 플러그인 없이 401/503 이 그대로 실패로 노출
                ScenarioCard(
                    title = "3. 대조군 (플러그인 없음)",
                    description = "Auth/Retry 플러그인을 설치하지 않은 동일한 MockEngine. 401 응답이 리프레시 없이 그대로 반환되어 첫 실패가 곧 최종 결과가 된다. 고급 구성의 효과를 1·2번과 대비해 확인한다.",
                    accent = Color(0xFFE53935),
                    run = ::runNoPluginScenario
                )
            }
            item { GuideCard() }
        }
    }
}

@Composable
private fun ScenarioCard(
    title: String,
    description: String,
    accent: Color,
    run: suspend (onLog: (String) -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    val logs = remember { mutableStateListOf<String>() }
    var running by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    running = true
                    logs.clear()
                    scope.launch {
                        try {
                            // onLog 는 항상 메인 디스패처에서 상태를 갱신(엔진 콜백 스레드와 무관하게 순서 보존)
                            run { line ->
                                scope.launch(Dispatchers.Main.immediate) { logs.add(line) }
                            }
                        } catch (e: Exception) {
                            logs.add("예외: ${e::class.java.simpleName} ${e.message}")
                        } finally {
                            running = false
                        }
                    }
                },
                enabled = !running,
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(text = if (running) "실행 중..." else "시나리오 실행", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TimelineBox(logs = logs, running = running)
        }
    }
}

/** 실시간 타임라인 로그를 보여주는 다크 박스. */
@Composable
private fun TimelineBox(logs: List<String>, running: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            logs.isEmpty() && !running -> {
                Text(
                    text = "시나리오 실행을 눌러 요청 흐름을 확인하세요.",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            logs.isEmpty() && running -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFCFD8DC),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "요청 처리 중...",
                        color = Color(0xFFCFD8DC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            else -> {
                Column {
                    logs.forEach { line ->
                        Text(
                            text = line,
                            color = lineColor(line),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

/** 로그 라인 내용에 따라 색을 달리해 가독성을 높인다. */
private fun lineColor(line: String): Color = when {
    line.contains("200") || line.contains("성공") || line.contains("✓") -> Color(0xFF81C784)
    line.contains("401") || line.contains("503") || line.contains("예외") || line.contains("✗") -> Color(0xFFEF9A9A)
    line.startsWith("↻") || line.contains("refresh") || line.contains("재시도") || line.contains("백오프") -> Color(0xFFFFD54F)
    else -> Color(0xFFCFD8DC)
}

// ──────────────────────────────────────────────────────────────────────────
// 시나리오 실행 로직 — 모두 MockEngine 위에서 동작(실서버 의존 없음)
// ──────────────────────────────────────────────────────────────────────────

private const val MOCK_URL = "https://mockapi.local/protected"

/**
 * 1. Auth bearer 토큰 리프레시.
 * - loadTokens 로 만료 토큰을 선제 전송 → MockEngine 이 401(+ WWW-Authenticate: Bearer) 응답
 * - Auth 플러그인이 refreshTokens 를 자동 호출해 새 토큰 발급 → 재요청 → 200
 */
private suspend fun runAuthRefreshScenario(onLog: (String) -> Unit) {
    // 서버가 인정하는 현재 유효 토큰(리프레시로 도달해야 할 목표 값)
    val validToken = "valid-token-v2"
    var requestCount = 0

    val engine = MockEngine { request ->
        requestCount++
        val auth = request.headers[HttpHeaders.Authorization]
        onLog("→ 요청 #$requestCount  Authorization=$auth")
        if (auth == "Bearer $validToken") {
            onLog("← 200 OK  (인증 통과)")
            respond(
                content = """{"data":"protected-resource"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        } else {
            onLog("← 401 Unauthorized  (토큰 만료/불일치)")
            // 401 에 WWW-Authenticate: Bearer 가 있어야 Auth 플러그인이 refreshTokens 를 트리거한다.
            respond(
                content = """{"error":"unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer realm=\"api\"")
            )
        }
    }

    val client = HttpClient(engine) {
        install(Auth) {
            bearer {
                loadTokens {
                    onLog("loadTokens: 저장된 '만료' 액세스 토큰 로드")
                    BearerTokens(accessToken = "expired-token", refreshToken = "refresh-abc")
                }
                refreshTokens {
                    onLog("↻ refreshTokens: 401 감지 → 새 액세스 토큰 발급")
                    BearerTokens(accessToken = validToken, refreshToken = "refresh-abc")
                }
                // 401 을 기다리지 않고 첫 요청부터 토큰을 선제 전송(데모 흐름 단순화)
                sendWithoutRequest { true }
            }
        }
    }

    try {
        val resp = client.get(MOCK_URL)
        onLog("결과: ${resp.status.value}  body=${resp.bodyAsText()}")
        onLog("✓ 토큰 리프레시 후 재요청 성공 (총 ${requestCount}회 요청)")
    } finally {
        client.close()
    }
}

/**
 * 2. HttpRequestRetry 지수 백오프.
 * - 처음 두 번은 503, 세 번째에 200 → retryOnServerErrors 가 자동 재시도
 * - delayMillis 로 재시도마다 지연을 늘린다(지수 백오프).
 */
private suspend fun runRetryScenario(onLog: (String) -> Unit) {
    var attempt = 0

    val engine = MockEngine { request ->
        attempt++
        if (attempt <= 2) {
            onLog("→ 시도 #$attempt  ← 503 Service Unavailable")
            respond(
                content = """{"error":"unavailable"}""",
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        } else {
            onLog("→ 시도 #$attempt  ← 200 OK  (복구)")
            respond(
                content = """{"data":"recovered"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }

    val client = HttpClient(engine) {
        install(HttpRequestRetry) {
            // 5xx 응답이면 최대 3회까지 재시도
            retryOnServerErrors(maxRetries = 3)
            // 재시도 N(1-base)마다 지연을 2배로(200ms, 400ms, ...) — 지수 백오프
            delayMillis { retry ->
                val delay = 200L * (1 shl (retry - 1))
                onLog("⏱ 백오프 ${delay}ms 대기 후 재시도 (retry=$retry)")
                delay
            }
        }
    }

    try {
        val resp = client.get(MOCK_URL)
        onLog("결과: ${resp.status.value}  body=${resp.bodyAsText()}")
        onLog("✓ 재시도로 복구 성공 (총 ${attempt}회 시도)")
    } finally {
        client.close()
    }
}

/**
 * 3. 대조군 — 플러그인 없이 동일한 MockEngine.
 * - 401 이 리프레시 없이 그대로 반환되어 첫 실패가 곧 최종 결과가 된다.
 */
private suspend fun runNoPluginScenario(onLog: (String) -> Unit) {
    var requestCount = 0

    val engine = MockEngine { request ->
        requestCount++
        onLog("→ 요청 #$requestCount  (인증/재시도 플러그인 없음)")
        onLog("← 401 Unauthorized")
        respond(
            content = """{"error":"unauthorized"}""",
            status = HttpStatusCode.Unauthorized,
            headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer realm=\"api\"")
        )
    }

    // Auth/HttpRequestRetry 미설치 — 401 을 받아도 추가 동작이 없다.
    val client = HttpClient(engine)

    try {
        val resp = client.get(MOCK_URL)
        onLog("결과: ${resp.status.value}  body=${resp.bodyAsText()}")
        onLog("✗ 리프레시/재시도 없이 401 그대로 실패 (총 ${requestCount}회 요청)")
    } finally {
        client.close()
    }
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Advanced Ktor Client Configuration",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "실서버 없이 Ktor MockEngine 으로 401/503 상황을 결정적으로 재현해 고급 클라이언트 구성을 검증한다. " +
                        "MockEngine 은 요청을 코드로 받아 원하는 응답을 돌려주는 가짜 엔진이라, 실제 Auth·HttpRequestRetry 플러그인을 그대로 설치한 채로 동작을 눈으로 확인할 수 있다. " +
                        "아래 세 시나리오를 실행해 토큰 리프레시(1)·재시도 백오프(2)가 있을 때와 없을 때(3)를 타임라인 로그로 비교해 보라.",
                fontSize = 12.sp,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
private fun GuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "핵심 정리", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "Auth bearer: loadTokens 로 초기 토큰, refreshTokens 로 401 시 자동 갱신 — 호출부 코드 변경 없이 재인증",
                "리프레시 트리거 조건: 401 응답에 WWW-Authenticate: Bearer 헤더가 있어야 BearerAuthProvider 가 작동",
                "HttpRequestRetry: retryOnServerErrors(5xx)/retryOnException + delayMillis·exponentialDelay 로 백오프",
                "MockEngine: 실서버·실네트워크 없이 응답을 코드로 정의 → 401/503 같은 분기를 결정적으로 재현",
                "실서비스에선 MockEngine 을 보통 test 소스셋에 두고, 프로덕션은 OkHttp/Android 엔진을 사용(본 예제는 데모 목적상 메인에 배치)"
            ).forEach {
                Text(
                    text = "• $it",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}
