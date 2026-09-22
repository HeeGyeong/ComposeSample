package com.example.composesample.presentation.example.component.system.security

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * 핀을 검증할 대상 호스트.
 * 프로젝트가 이미 쓰고 있는 공개 호스트를 그대로 써서 새 외부 의존 대상을 늘리지 않는다.
 */
private const val PINNING_HOST = "jsonplaceholder.typicode.com"
private const val PINNING_URL = "https://$PINNING_HOST/posts/1"

/** 네트워크가 끊긴 상태에서 화면이 오래 멈추지 않도록 짧게 잡는다. */
private const val TIMEOUT_SECONDS = 10L

/** 예외 원인 사슬을 훑을 최대 깊이 — 순환 사슬에 걸려 무한 루프에 빠지지 않도록 둔다. */
private const val MAX_CAUSE_DEPTH = 10

/**
 * App Security 예제 ① Certificate Pinning 섹션의 실제 TLS 동작을 담당한다.
 *
 * 화면(Composable)에서 직접 네트워크를 호출하지 않도록 OkHttp 호출을 전부 이 ViewModel 로 옮겼다.
 * 세 단계가 순서대로 "핀 값을 어떻게 얻고, 맞으면 어떻게 되고, 틀리면 어떻게 되는가"를 보여준다.
 *
 * 1) fetchPeerPins()        : 핀을 걸지 않은 클라이언트로 접속해 서버가 보낸 인증서 체인을 그대로 읽는다.
 *                             각 인증서를 CertificatePinner.pin() 에 넣으면 그게 바로 등록할 핀 값이다.
 * 2) requestWithCorrectPin(): 1)에서 얻은 leaf 핀(+ 상위 인증서를 backup pin)으로 요청 → 정상 응답.
 * 3) requestWithWrongPin()  : 고의로 틀린 핀으로 요청 → 실제 SSLPeerUnverifiedException.
 *
 * 핀 값을 소스에 하드코딩하지 않고 매번 조회하는 이유:
 * 실제 서버 인증서는 갱신되면 핀도 같이 바뀌기 때문에, 하드코딩한 예제는 언젠가 반드시 깨진다.
 * (그래서 이 예제는 계측 테스트 대상으로도 삼지 않는다 — 인증서 갱신일에 CI 가 실패한다.)
 */
class AppSecurityViewModel : ViewModel() {

    /**
     * 요청 결과 구분.
     * BLOCKED 는 "실패"가 아니라 핀이 제 일을 한 상태이므로 FAILED(그 밖의 오류)와 분리한다.
     */
    enum class PinOutcome {
        /** TLS 핸드셰이크 + 핀 검증 통과 → 응답 수신 */
        CONNECTED,

        /** 핀 불일치로 OkHttp 가 연결을 끊음 (SSLPeerUnverifiedException) */
        BLOCKED,

        /** 네트워크 없음 등 핀과 무관한 오류 */
        FAILED
    }

    /**
     * 서버가 내려보낸 인증서 1장에서 뽑아낸 정보.
     *
     * @property index 체인에서의 위치 (0 = leaf = 실제 서버 인증서)
     * @property pin CertificatePinner.pin() 이 만든 "sha256/..." 문자열 — 그대로 등록에 쓴다
     */
    data class CertPin(
        val index: Int,
        val pin: String,
        val subject: String,
        val issuer: String,
        val expiresOn: String
    )

    /** 단계별 실행 결과 한 줄. */
    data class PinningLog(
        val step: String,
        val outcome: PinOutcome,
        val summary: String,
        val detail: String = ""
    )

    data class UiState(
        val isRunning: Boolean = false,
        val chain: List<CertPin> = emptyList(),
        val tlsInfo: String = "",
        val logs: List<PinningLog> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    val host: String = PINNING_HOST
    val url: String = PINNING_URL

    /**
     * ① 핀 조회 — 핀을 걸지 않고 접속해 서버 인증서 체인을 읽는다.
     *
     * Response.handshake.peerCertificates 가 서버가 실제로 제시한 체인이고,
     * 그 각 장을 CertificatePinner.pin(cert) 에 넣으면 등록할 핀 문자열이 나온다.
     * 운영에서는 이 값을 빌드 시점에 미리 뽑아 소스/설정에 박아둔다.
     */
    fun fetchPeerPins() = runStep("① 핀 조회") {
        buildClient(pinner = null).newCall(request()).execute().use { response ->
            val handshake = response.handshake
                ?: return@use StepResult(
                    PinningLog(
                        step = "① 핀 조회",
                        outcome = PinOutcome.FAILED,
                        summary = "TLS 핸드셰이크 정보 없음 (평문 연결)"
                    )
                )

            val chain = handshake.peerCertificates
                .filterIsInstance<X509Certificate>()
                .mapIndexed { index, certificate ->
                    CertPin(
                        index = index,
                        // 핵심 API — 인증서에서 SPKI SHA-256 핀을 뽑아준다
                        pin = CertificatePinner.pin(certificate),
                        subject = commonName(certificate.subjectX500Principal.name),
                        issuer = commonName(certificate.issuerX500Principal.name),
                        expiresOn = formatExpiry(certificate.notAfter)
                    )
                }

            StepResult(
                log = PinningLog(
                    step = "① 핀 조회",
                    outcome = PinOutcome.CONNECTED,
                    summary = "HTTP ${response.code} — 인증서 ${chain.size}장 수신, 핀 추출 완료"
                ),
                chain = chain,
                tlsInfo = "${handshake.tlsVersion.javaName} / ${handshake.cipherSuite.javaName}"
            )
        }
    }

    /**
     * ② 올바른 핀으로 요청 — ①에서 얻은 leaf 핀을 등록한다.
     *
     * 상위(중간 CA) 인증서가 있으면 backup pin 으로 함께 등록한다.
     * leaf 만 걸면 서버 인증서가 갱신되는 순간 앱이 통째로 연결 불가가 되므로,
     * 실무에서는 교체 주기가 긴 중간 CA 핀을 backup 으로 같이 넣는 것이 정석이다.
     */
    fun requestWithCorrectPin() {
        val chain = _uiState.value.chain
        if (chain.isEmpty()) {
            appendLog(
                PinningLog(
                    step = "② 올바른 핀",
                    outcome = PinOutcome.FAILED,
                    summary = "먼저 ① 핀 조회를 실행하세요"
                )
            )
            return
        }

        val pins = listOfNotNull(chain.firstOrNull()?.pin, chain.getOrNull(1)?.pin)
        runStep("② 올바른 핀") {
            val pinner = CertificatePinner.Builder()
                .add(PINNING_HOST, *pins.toTypedArray())
                .build()

            buildClient(pinner).newCall(request()).execute().use { response ->
                StepResult(
                    PinningLog(
                        step = "② 올바른 핀",
                        outcome = PinOutcome.CONNECTED,
                        summary = "HTTP ${response.code} — 핀 ${pins.size}개 중 하나가 일치해 연결 허용",
                        detail = pins.joinToString("\n")
                    )
                )
            }
        }
    }

    /**
     * ③ 틀린 핀으로 요청 — 실제 SSLPeerUnverifiedException 을 받는다.
     *
     * 예외 메시지 안에 OkHttp 가 서버에서 받은 실제 핀 목록("Peer certificate chain")을 찍어주기 때문에,
     * 핀 값을 모를 때 일부러 틀린 핀으로 한 번 찔러보는 것도 핀을 얻는 방법이 된다.
     */
    fun requestWithWrongPin() = runStep("③ 틀린 핀") {
        val pinner = CertificatePinner.Builder()
            .add(PINNING_HOST, wrongPin())
            .build()

        buildClient(pinner).newCall(request()).execute().use { response ->
            // 여기에 도달하면 핀이 우연히 맞았다는 뜻이라 정상 흐름이 아니다
            StepResult(
                PinningLog(
                    step = "③ 틀린 핀",
                    outcome = PinOutcome.CONNECTED,
                    summary = "HTTP ${response.code} — 예상치 못한 성공"
                )
            )
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    /**
     * 단계 공통 실행부 — IO 디스패처에서 돌리고 예외를 결과로 변환한다.
     *
     * 이미 실행 중이면 무시해 버튼 연타로 요청이 겹치지 않게 한다.
     */
    private fun runStep(step: String, block: () -> StepResult) {
        if (_uiState.value.isRunning) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }

            val result = withContext(Dispatchers.IO) {
                runCatching { block() }.getOrElse { throwable -> StepResult(toLog(step, throwable)) }
            }

            _uiState.update { state ->
                state.copy(
                    isRunning = false,
                    chain = result.chain ?: state.chain,
                    tlsInfo = result.tlsInfo ?: state.tlsInfo,
                    logs = state.logs + result.log
                )
            }
        }
    }

    private fun appendLog(log: PinningLog) {
        _uiState.update { it.copy(logs = it.logs + log) }
    }

    /**
     * 핀 설정이 다른 요청마다 클라이언트를 새로 만든다.
     *
     * CertificatePinner 는 OkHttp 내부에서 Address 의 구성요소라 핀이 다르면 커넥션이 재사용되지 않지만,
     * 예제에서는 "이 요청이 어떤 핀 설정으로 나갔는가"를 분명히 하려고 매번 새로 만든다.
     */
    private fun buildClient(pinner: CertificatePinner?): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply { pinner?.let { certificatePinner(it) } }
            .build()

    private fun request(): Request = Request.Builder().url(PINNING_URL).build()

    /**
     * 던져진 예외를 화면에 보여줄 결과로 바꾼다.
     *
     * 예외를 그대로 보지 않고 원인(cause) 사슬까지 훑는 이유:
     * 디버그 빌드에서 HotSwan 인터프리터가 checked 예외를 UndeclaredThrowableException 으로 감싸기 때문에
     * (실측: 이 예제의 ③ 단계가 그대로 걸렸다) 맨 바깥 타입만 보면 핀 차단을 '알 수 없는 오류'로 잘못 분류한다.
     * 감싸는 계층은 OkHttp 인터셉터·코루틴 등에서도 생길 수 있으므로 사슬을 훑는 편이 어느 쪽이든 안전하다.
     */
    private fun toLog(step: String, throwable: Throwable): PinningLog {
        val pinFailure = throwable.causeChain()
            .filterIsInstance<SSLPeerUnverifiedException>()
            .firstOrNull()

        return if (pinFailure != null) {
            PinningLog(
                step = step,
                outcome = PinOutcome.BLOCKED,
                summary = "SSLPeerUnverifiedException — 핀 불일치로 연결 차단",
                // 이 메시지에 서버가 실제로 제시한 핀 목록이 그대로 들어있다
                detail = pinFailure.message.orEmpty()
            )
        } else {
            PinningLog(
                step = step,
                outcome = PinOutcome.FAILED,
                summary = "${throwable::class.java.simpleName} — 핀과 무관한 오류",
                detail = throwable.message.orEmpty()
            )
        }
    }

    /** 자기 자신부터 시작하는 원인 사슬. 순환 사슬에 대비해 깊이를 제한한다. */
    private fun Throwable.causeChain(): Sequence<Throwable> =
        generateSequence(this) { it.cause }.take(MAX_CAUSE_DEPTH)

    private data class StepResult(
        val log: PinningLog,
        val chain: List<CertPin>? = null,
        val tlsInfo: String? = null
    )

    private companion object {
        /**
         * 인증서 만료일 표기.
         *
         * SimpleDateFormat 은 스레드 안전하지 않아 공유 인스턴스로 두지 않고 호출마다 만든다.
         * yyyy-MM-dd 는 로캘과 무관한 고정 형식이므로 Locale.US 로 고정한다
         * (Locale.getDefault() 를 정적 필드에 담으면 실행 중 로캘 변경을 반영하지 못한다).
         */
        fun formatExpiry(date: Date): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

        /**
         * 고의로 틀린 핀.
         * 임의 문자열의 SHA-256 이라 길이(32바이트)는 형식상 유효하고 값만 절대 맞지 않는다.
         */
        fun wrongPin(): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest("intentionally-wrong-pin".toByteArray())
            return "sha256/${Base64.encodeToString(digest, Base64.NO_WRAP)}"
        }

        /** X500 DN("CN=a, O=b, C=c")에서 CN 만 뽑는다. CN 이 없으면 DN 을 그대로 쓴다. */
        fun commonName(distinguishedName: String): String =
            distinguishedName.split(",")
                .map { it.trim() }
                .firstOrNull { it.startsWith("CN=") }
                ?.removePrefix("CN=")
                ?: distinguishedName
    }
}
