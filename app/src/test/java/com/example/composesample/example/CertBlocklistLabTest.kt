package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.security.CVE_2026_111943_SPKI_SHA256
import com.example.composesample.presentation.example.component.system.security.SentBeforeBlock
import com.example.composesample.presentation.example.component.system.security.blockTargetIndex
import com.example.composesample.presentation.example.component.system.security.describeFailure
import com.example.composesample.presentation.example.component.system.security.firstBlocked
import com.example.composesample.presentation.example.component.system.security.peerRejection
import com.example.composesample.presentation.example.component.system.security.sentBeforeBlock
import okio.ByteString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.lang.reflect.UndeclaredThrowableException
import java.net.ConnectException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * App Security 예제 ①-2(인증서 차단 목록과 검사 시점)의 판정 로직 검증 — 화면이 쓰는 함수를 그대로 부른다.
 * given : EventListener 이벤트 순서(에뮬레이터 실측 모양) · 체인 SPKI 해시 · OkHttp 가 돌려준 예외 모양
 * when : 차단 전에 나간 것 판정 · 차단 대상 고르기 · 예외에서 인증서 거절 찾기
 * then : A 는 헤더·본문, B 는 연결만, C 는 0 이고 C 의 거절은 suppressed 안에서도 찾는다
 */
class CertBlocklistLabTest {

    @Test
    fun `앱 목록은 OkHttp 5_5_1 내장 차단 목록과 같은 8개다`() {
        // OkHttp 를 올렸을 때 내장 목록이 바뀌면 이 테스트가 먼저 알려준다
        val field = Class.forName("okhttp3.internal.tls.CertificateBlocklist")
            .getDeclaredField("blockedCertificateSha256s")
            .apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val builtIn = (field.get(null) as Set<ByteString>).map { it.hex() }.toSet()
        assertEquals(8, CVE_2026_111943_SPKI_SHA256.size)
        assertEquals(builtIn, CVE_2026_111943_SPKI_SHA256)
    }

    @Test
    fun `차단 전에 나간 것 — A 는 헤더와 본문, B 는 연결만, C 는 0`() {
        val common = listOf("callStart", "dnsEnd", "connectStart", "connectFailed", "connectStart", "secureConnectStart")
        val appInterceptor = common + listOf(
            "secureConnectEnd", "connectEnd", "connectionAcquired", "requestHeadersEnd", "requestBodyEnd",
            "responseHeadersEnd", "CHECK blocked", "callFailed"
        )
        val networkInterceptor = common + listOf("secureConnectEnd", "connectEnd", "connectionAcquired", "CHECK blocked", "callFailed")
        val hostnameVerifier = common + listOf("CHECK blocked", "connectFailed", "connectStart", "secureConnectStart", "CHECK blocked", "callFailed")

        assertEquals(SentBeforeBlock.HEADERS_AND_BODY, sentBeforeBlock(appInterceptor))
        assertEquals(SentBeforeBlock.CONNECTION_ONLY, sentBeforeBlock(networkInterceptor))
        assertEquals(SentBeforeBlock.NOTHING, sentBeforeBlock(hostnameVerifier))
        // 본문 없는 GET 이 헤더만 쓰고 막혔다면
        assertEquals(SentBeforeBlock.HEADERS, sentBeforeBlock(listOf("secureConnectEnd", "requestHeadersEnd")))
    }

    @Test
    fun `차단 대상은 중간 CA, 체인 대조는 첫 일치 위치`() {
        assertEquals(1, blockTargetIndex(3))
        assertEquals(0, blockTargetIndex(1))
        assertNull(blockTargetIndex(0))

        // 실측 체인 typicode.com → WE1 → GTS Root R4 (SPKI SHA-256 앞부분)
        val chain = listOf("92e8fc9e20c967b2", "908769e8d34477cc", "9847e5653e5e9e84")
        assertNull(firstBlocked(chain, CVE_2026_111943_SPKI_SHA256))
        assertEquals(1, firstBlocked(chain, setOf("908769e8d34477cc")))
    }

    @Test
    fun `인증서 거절은 원인 사슬과 suppressed 에서 찾는다`() {
        val rejection = SSLPeerUnverifiedException(
            "Hostname jsonplaceholder.typicode.com not verified:\n" +
                "    |    certificate: sha256/kuj8niDJZ7L/FhVll1wOJFjX6v0tfSGasazraQ0i1a0=\n" +
                "    |    DN: CN=typicode.com\n" +
                "    |    subjectAltNames: [typicode.com, *.typicode.com]"
        )
        // C — 첫 경로(IPv6)가 연결 실패, 다음 IPv4 경로의 verify 거절은 suppressed 로 붙는다(에뮬레이터 실측 모양)
        val connect = ConnectException("Failed to connect to jsonplaceholder.typicode.com/[2606:4700:3037::ac43:a797]:443")
            .apply { addSuppressed(rejection) }
        assertSame(rejection, peerRejection(connect))
        assertEquals(
            "ConnectException: Failed to connect to jsonplaceholder.typicode.com/[2606:4700:3037::ac43:a797]:443\n" +
                "└ suppressed: SSLPeerUnverifiedException: Hostname jsonplaceholder.typicode.com not verified:\n" +
                "  certificate: sha256/kuj8niDJZ7L/FhVll1wOJFjX6v0tfSGasazraQ0i1a0=\n" +
                "  DN: CN=typicode.com",
            describeFailure(connect)
        )

        // 디버그 빌드에서 감싸는 계층이 생겨도 원인 사슬로 찾는다
        val wrapped = UndeclaredThrowableException(rejection)
        assertSame(rejection, peerRejection(wrapped))
        assertEquals(true, describeFailure(wrapped).contains("└ cause: SSLPeerUnverifiedException"))

        // A·B — 인터셉터가 직접 던진 거절은 그대로
        val direct = SSLPeerUnverifiedException("forbidden certificate!")
        assertEquals("SSLPeerUnverifiedException: forbidden certificate!", describeFailure(direct))

        // 거절이 없는 오류
        assertNull(peerRejection(ConnectException("Failed to connect")))
    }
}
