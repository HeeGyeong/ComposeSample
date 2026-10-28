package com.example.composesample.presentation.example.component.system.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 앱 보안 실무 예제
 *
 * 3가지 보안 패턴을 한 화면에서 데모합니다.
 * 1) Certificate Pinning  — 실제 OkHttp CertificatePinner 로 공개 호스트에 TLS 요청 (핀 조회 / 일치 / 불일치)
 *    1-2) 인증서 차단 목록 — 같은 비밀 요청을 검사 자리(Interceptor · 네트워크 Interceptor · HostnameVerifier)만 바꿔 비교 
 * 2) Secure Storage      — AndroidKeyStore + AES-GCM 으로 평문 암호화/복호화 (EncryptedSharedPreferences 내부 동작과 동일)
 * 3) Play Integrity Mock — 토큰 디코딩 + verdict 필드 파싱 (실제 호출 없이 Mock JSON)
 */
@Composable
fun AppSecurityExampleUI(onBackEvent: () -> Unit) {
    val viewModel: AppSecurityViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B2A33))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2C3E50))
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "App Security 실무",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CertificatePinningSection(viewModel)
            CertBlocklistSection(viewModel)
            SecureStorageSection()
            PlayIntegritySection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================================
// 1) Certificate Pinning — 실제 OkHttp CertificatePinner 로 TLS 요청
// ============================================================

/**
 * 실제 공개 호스트에 TLS 요청을 보내 핀 검증을 시연한다.
 *
 * 네트워크 호출은 전부 [AppSecurityViewModel] 이 담당하고 여기서는 상태만 그린다.
 * 핀 값을 소스에 박지 않고 ①에서 매번 조회하는 이유는 ViewModel KDoc 참고
 * (인증서가 갱신되면 핀도 바뀌므로 하드코딩한 예제는 언젠가 반드시 깨진다).
 */
@Composable
private fun CertificatePinningSection(viewModel: AppSecurityViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    SectionCard(title = "① Certificate Pinning (실제 TLS 핸드셰이크)") {
        BodyText("MITM 공격을 차단하기 위해 서버 인증서의 공개키 해시(SHA-256 SPKI)를 클라이언트에 사전 고정합니다. 아래 버튼은 시뮬레이션이 아니라 실제로 요청을 보내며, 핀이 틀리면 OkHttp 가 SSLPeerUnverifiedException 으로 연결을 끊습니다.")

        Spacer(modifier = Modifier.height(8.dp))
        MonoText("host = ${viewModel.host}")
        MonoText("url  = ${viewModel.url}")
        if (uiState.tlsInfo.isNotEmpty()) {
            MonoText("tls  = ${uiState.tlsInfo}")
        }

        Spacer(modifier = Modifier.height(12.dp))
        // 좁은 화면(360dp)에서도 버튼이 잘리지 않도록 FlowRow 로 감싼다
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallButton("① 핀 조회", enabled = !uiState.isRunning) { viewModel.fetchPeerPins() }
            SmallButton("② 올바른 핀", enabled = !uiState.isRunning) { viewModel.requestWithCorrectPin() }
            SmallButton("③ 틀린 핀", enabled = !uiState.isRunning) { viewModel.requestWithWrongPin() }
            SmallButton("로그 지우기", enabled = uiState.logs.isNotEmpty()) { viewModel.clearLogs() }
        }

        if (uiState.isRunning) {
            Spacer(modifier = Modifier.height(8.dp))
            MonoText("요청 중...")
        }

        if (uiState.chain.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "서버가 제시한 인증서 체인 — 이 핀 값을 그대로 등록한다",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            uiState.chain.forEach { CertPinRow(it) }
        }

        uiState.logs.forEach { log ->
            Spacer(modifier = Modifier.height(8.dp))
            PinningLogRow(log)
        }

        Spacer(modifier = Modifier.height(12.dp))
        BodyText("코드 대신 설정으로 거는 방법 — 아래는 앱 전역에 적용되는 선언형 경로입니다. 이 예제는 다른 네트워크 예제까지 영향받지 않도록 코드 경로만 실제로 적용하고, 선언형은 코드 블록으로만 보여줍니다.")
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(NETWORK_SECURITY_CONFIG_SNIPPET)
    }
}

/** 인증서 1장 — leaf(0)가 서버 자신이고 그 위가 중간 CA·루트 CA 다. */
@Composable
private fun CertPinRow(certPin: AppSecurityViewModel.CertPin) {
    val role = when (certPin.index) {
        0 -> "leaf (서버)"
        else -> "CA #${certPin.index}"
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "[${certPin.index}] $role  CN=${certPin.subject}",
            color = Color(0xFF90CAF9),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        MonoText(certPin.pin)
        MonoText("issuer=${certPin.issuer}  expires=${certPin.expiresOn}")
    }
}

/** 단계별 실행 결과 — 차단(BLOCKED)은 실패가 아니라 핀이 제 일을 한 상태라 색을 따로 준다. */
@Composable
private fun PinningLogRow(log: AppSecurityViewModel.PinningLog) {
    val color = when (log.outcome) {
        AppSecurityViewModel.PinOutcome.CONNECTED -> Color(0xFF81C784)
        AppSecurityViewModel.PinOutcome.BLOCKED -> Color(0xFFFFB74D)
        AppSecurityViewModel.PinOutcome.FAILED -> Color(0xFFEF9A9A)
    }

    Column {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                    append("${log.step} · ${log.outcome.name}")
                }
            },
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
        MonoText(log.summary)
        if (log.detail.isNotEmpty()) {
            CodeBlock(log.detail)
        }
    }
}

/**
 * res/xml/network_security_config.xml 로 거는 선언형 핀 — 실행하지 않고 코드로만 보여준다.
 *
 * 코드 경로(CertificatePinner)와 달리 매니페스트에 연결하는 순간 앱 전체 트래픽에 적용되므로,
 * 핀이 만료되거나 틀리면 이 앱의 다른 네트워크 예제까지 전부 끊긴다.
 * expiration 을 넘기면 그 시점부터 핀 검사를 건너뛰어, 핀 갱신을 놓쳤을 때 앱이 죽는 대신 풀리게 한다.
 */
private val NETWORK_SECURITY_CONFIG_SNIPPET = """
    <!-- res/xml/network_security_config.xml -->
    <network-security-config>
        <domain-config>
            <domain includeSubdomains="true">example.com</domain>
            <pin-set expiration="2027-01-01">
                <!-- 현재 인증서 -->
                <pin digest="SHA-256">AAAA...=</pin>
                <!-- backup pin: 교체 주기가 긴 중간 CA — 없으면 인증서 갱신 시 앱이 연결 불가가 된다 -->
                <pin digest="SHA-256">BBBB...=</pin>
            </pin-set>
        </domain-config>
    </network-security-config>

    <!-- AndroidManifest.xml -->
    <application android:networkSecurityConfig="@xml/network_security_config" />
""".trimIndent()

// ============================================================
// 1-2) 인증서 차단 목록 — 검사 자리에 따라 차단 전에 무엇이 나가는가
// ============================================================

/**
 * ①-2 인증서 차단 목록과 검사 시점 — 같은 비밀 요청을 검사 자리(A·B·C)만 바꿔 보내고
 * EventListener 타임라인으로 '차단 전에 무엇이 나갔는가'를 비교한다. 네트워크 호출은 ViewModel → CertBlocklistLab.
 */
@Composable
private fun CertBlocklistSection(viewModel: AppSecurityViewModel) {
    val state = viewModel.blocklist.collectAsStateWithLifecycle().value

    SectionCard(title = "①-2 인증서 차단 목록 — 어디서 검사하느냐에 따라 비밀이 먼저 나간다") {
        BodyText(
            "CA 개인 키가 유출되면(CVE-2026-111943) 그 CA 로 아무 호스트의 인증서나 만들 수 있고, 기기의 신뢰 루트가 패치될 때까지 " +
                "그 인증서는 정상 검증을 통과한다. 그래서 앱이 '이 인증서는 믿지 않는다'는 차단 목록을 직접 건다. 핀(① 허용 목록)은 호스트마다 " +
                "'이것만 믿는다'를 걸어 인증서가 바뀌면 깨지지만, 차단 목록은 모든 호스트에 한 번에 걸리고 평소엔 아무 영향이 없다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        BodyText(
            "실험: 이 호스트 체인의 중간 CA 를 '유출됐다'고 가정해 차단 목록에 넣고, 가짜 Authorization 헤더와 본문을 담은 같은 POST 를 " +
                "세 자리에서 검사한다. 대조는 검사 자리 없이 보낸다(OkHttp 5.5.1 내장 목록에는 이 호스트가 없다)."
        )
        Spacer(modifier = Modifier.height(8.dp))
        MonoText("POST ${viewModel.blocklistUrl}")
        MonoText("Authorization: Bearer demo-token-157 (가짜)")
        state.blockTarget?.let { target ->
            MonoText("차단 목록 = CVE 8개 + [${target.index}] CN=${target.subject}")
            MonoText("  SPKI SHA-256 ${target.spkiSha256.take(16)}…")
        }

        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(BLOCKLIST_SNIPPET)

        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CheckPlacement.entries.forEach { placement ->
                SmallButton(placement.mark, enabled = !state.isRunning) { viewModel.runBlocklistTrial(placement) }
            }
            SmallButton("같은 클라이언트로 2번", enabled = !state.isRunning) { viewModel.measureBlocklistReuse() }
            SmallButton("지우기", enabled = state.trials.isNotEmpty() || state.reuse != null) { viewModel.clearBlocklist() }
        }
        if (state.isRunning) {
            Spacer(modifier = Modifier.height(8.dp))
            MonoText("요청 중...")
        }
        state.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            CodeBlock(it)
        }

        state.trials.forEach { trial ->
            Spacer(modifier = Modifier.height(10.dp))
            TrialBlock(trial)
        }
        state.reuse?.let {
            Spacer(modifier = Modifier.height(10.dp))
            ReuseBlock(it)
        }

        if (state.chain.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Heading("OkHttp 5.5.1 내장 차단 목록(${CVE_2026_111943_SPKI_SHA256.size}개)과 이 체인")
            state.chain.forEach { cert ->
                MonoText("[${cert.index}] CN=${cert.subject} · ${cert.spkiSha256.take(16)}… · ${if (cert.inBuiltInList) "목록에 있음" else "없음"}")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Finding(
            "실측(Pixel Tablet 에뮬레이터 API 35 debug·release · Pixel 4 에뮬레이터 API 30 debug 모두 같음 — 체인 typicode.com → WE1 → GTS Root R4, WE1 을 차단 목록에 넣음): " +
                "대조는 201 — OkHttp 5.5.1 내장 목록 8개에 이 체인은 없다. A 는 requestHeadersEnd(헤더 7개, Authorization 포함)·requestBodyEnd(64B) 가 " +
                "끝나고 서버가 201 과 함께 본문을 그대로 돌려준 뒤(id 101)에야 차단했다 — 비밀은 이미 서버에 있다. B 는 connectEnd(h2)·connectionAcquired " +
                "다음, 요청 헤더를 쓰기 전에 차단해 요청 헤더·본문이 0 이었다(chain.connection() 은 null 아님). 연결은 맺었으므로 HTTP/2 서두는 나간다."
        )
        Finding(
            "C 는 secureConnectStart 다음 verify 가 false 를 돌려 secureConnectEnd 없이 연결을 버렸다 — 요청 바이트 0. 그런데 OkHttp 는 이를 그 IP 의 " +
                "연결 실패로 보고 다음 IPv4 주소로 TLS 를 다시 맺어 verify 가 2번 불렸고, 앱이 받은 예외는 첫 경로(IPv6 — 이 에뮬레이터는 연결 불가)의 " +
                "ConnectException 이었다(어느 예외가 맨 바깥에 오는지는 경로 순서에 달렸다). 인증서 거절은 suppressed 안에 'Hostname … not verified' 와 leaf 인증서(CN=typicode.com)로만 남는다 — 막힌 것은 " +
                "WE1 인데 호스트 이름 불일치처럼 읽힌다. 예외 타입 하나로 분기하지 말고 원인 사슬과 suppressed 를 훑고, 차단 사유는 verifier 안에서 따로 남긴다."
        )
        Finding(
            "같은 클라이언트로 2번: 커넥션 1개(h2)에 verify 1번, 네트워크 Interceptor 2번 — verify 는 커넥션을 맺을 때만 불린다. 실행 중에 차단 목록을 " +
                "바꾸면 풀에 남은 커넥션은 다시 검사되지 않으니 connectionPool.evictAll() 로 쉬는 커넥션을 비운다."
        )
        Finding(
            "OkHttp 4.12.1·5.5.1 내장 목록은 C 와 같은 자리에서 검사한다 — ConnectPlan.connectTls 가 hostnameVerifier.verify 를 통과한 직후 " +
                "CertificateBlocklist, 그다음 CertificatePinner(①) 순서이고(javap), 메시지가 'not verified (blocklisted by OkHttp)' 라 구분된다. " +
                "같은 변경 기록에 따르면 Google Play 서비스가 있는 Android 기기는 자동 업데이트를 받았고, 내장 차단은 JVM·AOSP 기기를 위한 것이다. " +
                "그 버전 미만의 OkHttp 에서는 C 처럼 직접 건다."
        )
    }
}

/** 검사 자리 하나 — 결과, 차단 전에 나간 것, 이벤트 타임라인 */
@Composable
private fun TrialBlock(trial: BlocklistTrial) {
    val color = when {
        trial.blocked -> Color(0xFFFFB74D)
        trial.failure != null -> Color(0xFFEF9A9A)
        else -> Color(0xFF81C784)
    }
    Column {
        Text(
            text = "${trial.placement.mark} ${trial.placement.label}",
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        when {
            trial.blocked -> MonoText("차단 · ${trial.sent.label}")
            trial.failure != null -> MonoText("차단과 무관한 오류")
            else -> MonoText("통과 · HTTP ${trial.httpCode}")
        }
        MonoText("검사 코드 호출 ${trial.checkCalls}번")
        trial.serverReply?.let { MonoText("차단 전에 받은 응답: $it") }
        trial.failure?.let { CodeBlock(it) }
        CodeBlock(trial.events.joinToString("\n") { "%5dms  %-19s %s".format(it.atMs, it.name, it.detail).trimEnd() })
    }
}

@Composable
private fun ReuseBlock(reuse: ReuseCheck) {
    Column {
        Heading("같은 클라이언트로 ${reuse.requests}번 요청 (차단 목록 비움, ${reuse.protocol})")
        MonoText("새 커넥션 ${reuse.connectionsOpened}개")
        MonoText("HostnameVerifier.verify ${reuse.verifyCalls}번 — 커넥션을 맺을 때만")
        MonoText("네트워크 Interceptor ${reuse.networkInterceptorCalls}번 — 요청마다")
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text = text,
        color = Color(0xFFB0BEC5),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun Finding(text: String) {
    Text(text = text, color = Color(0xFF90A4AE), fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
}

/** gist 개정판의 핵심 — 기본 HostnameVerifier 에 위임하고 체인의 SPKI SHA-256 을 대조한다 */
private val BLOCKLIST_SNIPPET = """
    // ✗ A: 응답을 받은 뒤 검사 — 헤더·본문은 이미 나갔다
    val response = chain.proceed(chain.request())
    if (response.handshake.hasBlocked()) throw SSLPeerUnverifiedException(…)

    // ✓ C: TLS 직후, 요청을 쓰기 전에 검사
    class BlocklistVerifier(
      private val delegate: HostnameVerifier = OkHttpClient().hostnameVerifier,
    ) : HostnameVerifier {
      override fun verify(host: String, session: SSLSession) =
        delegate.verify(host, session) &&
          session.peerCertificates.none { (it as X509Certificate).sha256Hash() in blocked }
    }
    client.newBuilder().hostnameVerifier(BlocklistVerifier()).build()
""".trimIndent()

private sealed interface VerifyResult {
    val text: String
    data class Ok(override val text: String) : VerifyResult
    data class Err(override val text: String) : VerifyResult
}

// ============================================================
// 2) Secure Storage — AndroidKeyStore AES-GCM 암호화/복호화
// ============================================================

private const val SECURE_KEY_ALIAS = "app_security_example_key"
private const val GCM_TAG_LENGTH_BITS = 128

@Composable
private fun SecureStorageSection() {
    var plaintext by remember { mutableStateOf("내 카드 번호 1234-5678-9012-3456") }
    var cipherBase64 by remember { mutableStateOf("") }
    var ivBase64 by remember { mutableStateOf("") }
    var decrypted by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    SectionCard(title = "② Secure Storage (AndroidKeyStore + AES-GCM)") {
        BodyText("EncryptedSharedPreferences 내부 동작과 동일한 패턴 — AndroidKeyStore 에 AES 키를 생성하여 export 불가능한 상태로 보관하고, AES-GCM 으로 평문을 암호화합니다. 키 자체는 앱 외부로 노출되지 않으며, IV 는 매 암호화마다 재생성해 저장해야 합니다.")

        Spacer(modifier = Modifier.height(8.dp))
        Text("평문", color = Color(0xFFB0BEC5), fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF263238), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            BasicTextField(
                value = plaintext,
                onValueChange = { plaintext = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("암호화") {
                runCatching {
                    val (iv, cipher) = encryptAesGcm(plaintext.toByteArray())
                    ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
                    cipherBase64 = Base64.encodeToString(cipher, Base64.NO_WRAP)
                    decrypted = ""
                    error = null
                }.onFailure { error = "암호화 실패: ${it.message}" }
            }
            SmallButton("복호화") {
                runCatching {
                    val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
                    val cipher = Base64.decode(cipherBase64, Base64.NO_WRAP)
                    decrypted = String(decryptAesGcm(iv, cipher))
                    error = null
                }.onFailure { error = "복호화 실패: ${it.message}" }
            }
        }

        if (cipherBase64.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            MonoText("IV (Base64): $ivBase64")
            MonoText("CIPHER (Base64): ${cipherBase64.take(72)}${if (cipherBase64.length > 72) "..." else ""}")
        }

        if (decrypted.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ResultText(VerifyResult.Ok("✅ 복호화 결과: $decrypted"))
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            ResultText(VerifyResult.Err(it))
        }
    }
}

private fun getOrCreateSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    keyStore.getKey(SECURE_KEY_ALIAS, null)?.let { return it as SecretKey }

    val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    val spec = KeyGenParameterSpec.Builder(
        SECURE_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()
    generator.init(spec)
    return generator.generateKey()
}

private fun encryptAesGcm(plain: ByteArray): Pair<ByteArray, ByteArray> {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
    val encrypted = cipher.doFinal(plain)
    // KeyStore 가 자동 생성한 IV (매 호출마다 재생성됨 — 직접 주입 금지)
    return cipher.iv to encrypted
}

private fun decryptAesGcm(iv: ByteArray, cipher: ByteArray): ByteArray {
    val c = Cipher.getInstance("AES/GCM/NoPadding")
    c.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
    return c.doFinal(cipher)
}

// ============================================================
// 3) Play Integrity Mock — verdict 페이로드 디코딩
// ============================================================

@Composable
private fun PlayIntegritySection() {
    var verdict by remember { mutableStateOf<MockIntegrityVerdict?>(null) }

    SectionCard(title = "③ Play Integrity API (Mock 응답 디코딩)") {
        BodyText("실제 호출은 Google Play 서비스 + 백엔드 디코딩 키가 필요합니다. 여기서는 응답 페이로드 형태를 Mock 으로 재현해, 어떤 verdict 필드를 검증해야 하는지 보여줍니다.")

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("정상 단말") { verdict = mockVerdict(genuine = true) }
            SmallButton("루팅/에뮬레이터") { verdict = mockVerdict(genuine = false) }
        }

        verdict?.let { v ->
            Spacer(modifier = Modifier.height(8.dp))
            MonoText("appRecognitionVerdict: ${v.appRecognitionVerdict}")
            MonoText("deviceRecognitionVerdict: ${v.deviceRecognitionVerdict}")
            MonoText("appLicensingVerdict: ${v.appLicensingVerdict}")
            MonoText("nonceMatched: ${v.nonceMatched}")

            Spacer(modifier = Modifier.height(8.dp))
            val ok = v.appRecognitionVerdict == "PLAY_RECOGNIZED" &&
                v.deviceRecognitionVerdict == "MEETS_DEVICE_INTEGRITY" &&
                v.nonceMatched
            ResultText(
                if (ok) VerifyResult.Ok("✅ 모든 verdict 통과 — 민감 동작 허용")
                else VerifyResult.Err("❌ verdict 실패 — 민감 동작 차단 권장")
            )
        }
    }
}

private data class MockIntegrityVerdict(
    val appRecognitionVerdict: String,
    val deviceRecognitionVerdict: String,
    val appLicensingVerdict: String,
    val nonceMatched: Boolean
)

private fun mockVerdict(genuine: Boolean): MockIntegrityVerdict {
    val nonce = "nonce-${System.currentTimeMillis()}"
    val expectedHash = MessageDigest.getInstance("SHA-256")
        .digest(nonce.toByteArray())
        .joinToString("") { "%02x".format(it) }
    // 검증 시연을 위해 서버 nonce 와 동일하다고 가정 (실제로는 서버가 응답 nonce 와 비교)
    return if (genuine) {
        MockIntegrityVerdict(
            appRecognitionVerdict = "PLAY_RECOGNIZED",
            deviceRecognitionVerdict = "MEETS_DEVICE_INTEGRITY",
            appLicensingVerdict = "LICENSED",
            nonceMatched = expectedHash.isNotEmpty()
        )
    } else {
        MockIntegrityVerdict(
            appRecognitionVerdict = "UNRECOGNIZED_VERSION",
            deviceRecognitionVerdict = "MEETS_BASIC_INTEGRITY",
            appLicensingVerdict = "UNLICENSED",
            nonceMatched = false
        )
    }
}

// ============================================================
// 공통 UI 컴포넌트
// SectionCard는 system/security 패키지 공용(SecurityUiComponents.kt)으로 이동
// ============================================================

@Composable
private fun BodyText(text: String) {
    Text(text = text, color = Color(0xFFCFD8DC), fontSize = 13.sp)
}

@Composable
private fun MonoText(text: String) {
    Text(
        text = text,
        color = Color(0xFFB0BEC5),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

@Composable
private fun ResultText(result: VerifyResult) {
    val color = when (result) {
        is VerifyResult.Ok -> Color(0xFF81C784)
        is VerifyResult.Err -> Color(0xFFEF9A9A)
    }
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                append(result.text)
            }
        },
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun SmallButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF455A64),
            disabledContainerColor = Color(0xFF37474F)
        )
    ) {
        Text(
            text = label,
            color = if (enabled) Color.White else Color(0xFF78909C),
            fontSize = 13.sp
        )
    }
}

/**
 * 코드/예외 전문 표시용 블록.
 * 핀 불일치 예외 메시지는 인증서 체인이 통째로 들어와 길기 때문에 가로 스크롤을 둔다.
 */
@Composable
private fun CodeBlock(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16232B), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF9FB6C1),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
