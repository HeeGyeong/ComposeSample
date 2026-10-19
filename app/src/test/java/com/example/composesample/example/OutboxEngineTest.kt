package com.example.composesample.example

import com.example.composesample.presentation.example.component.data.outbox.FakeChatServer
import com.example.composesample.presentation.example.component.data.outbox.LostResponseException
import com.example.composesample.presentation.example.component.data.outbox.MARK_LOSE_ACK
import com.example.composesample.presentation.example.component.data.outbox.MARK_REJECT
import com.example.composesample.presentation.example.component.data.outbox.MARK_TRANSIENT
import com.example.composesample.presentation.example.component.data.outbox.OutboxClient
import com.example.composesample.presentation.example.component.data.outbox.SendDecision
import com.example.composesample.presentation.example.component.data.outbox.SendResult
import com.example.composesample.presentation.example.component.data.outbox.classifyFailure
import com.example.composesample.presentation.example.component.data.outbox.decideStatus
import com.example.composesample.presentation.example.component.data.outbox.exponentialBackoffMillis
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.lang.reflect.UndeclaredThrowableException
import java.nio.file.Files

/**
 * OfflineOutboxExample 의 엔진 검증 — 워커가 쓰는 클라이언트·분류 함수와 가짜 서버를 그대로 실행한다.
 * given : MockEngine 에 물린 FakeChatServer(지연 0, 임시 폴더 파일에 저장) · OutboxClient
 * when : 같은 clientId 재전송 / 응답 유실 / 새 id 재시도 / 503·400·401 / 오프라인 / 서버 재시작
 * then : 서버에 저장된 건수·seq 가 기대와 같은가, 클라이언트가 결과를 기대대로 분류하는가
 */
class OutboxEngineTest {

    private val dir: File = Files.createTempDirectory("outbox").toFile()
    private val file = File(dir, "server.json")
    private val server = FakeChatServer(file).apply { latencyMs = 0 }
    private val http = HttpClient(server.engine())
    private val client = OutboxClient(http)

    @After
    fun tearDown() {
        http.close()
        dir.deleteRecursively()
    }

    @Test
    fun `같은 clientId 를 두 번 보내면 서버는 한 번만 저장하고 같은 seq 를 돌려준다`() = runBlocking {
        val first = client.send("c", "id-1", "안녕") as SendResult.Sent
        val second = client.send("c", "id-1", "안녕") as SendResult.Sent
        assertEquals(1L, first.seq)
        assertFalse(first.replayed)
        assertEquals(first.seq, second.seq)
        assertEquals(first.serverId, second.serverId)
        assertTrue(second.replayed)
        assertEquals(1, server.messages("c").size)
    }

    @Test
    fun `응답을 잃으면 재시도 사유이고 서버에는 이미 저장돼 있다`() = runBlocking {
        val lost = client.send("c", "id-1", "$MARK_LOSE_ACK 안녕")
        assertTrue(lost is SendResult.Retry)
        assertEquals(1, server.messages("c").size)
        // 같은 id 로 다시 보내면 원래 seq 를 돌려받고 서버는 여전히 1건
        val retry = client.send("c", "id-1", "$MARK_LOSE_ACK 안녕") as SendResult.Sent
        assertEquals(1L, retry.seq)
        assertTrue(retry.replayed)
        assertEquals(1, server.messages("c").size)
    }

    @Test
    fun `응답을 잃은 뒤 새 id 로 다시 보내면 서버에 두 건이 된다`() = runBlocking {
        client.send("c", "id-1", "$MARK_LOSE_ACK 안녕")
        val retry = client.send("c", "id-2", "$MARK_LOSE_ACK 안녕") as SendResult.Sent
        assertEquals(2L, retry.seq)
        assertFalse(retry.replayed)
        assertEquals(listOf("id-1", "id-2"), server.messages("c").map { it.clientId })
    }

    @Test
    fun `503 은 저장 없이 재시도 사유 — 같은 본문 표식은 한 번만 걸린다`() = runBlocking {
        val first = client.send("c", "id-1", "$MARK_TRANSIENT 첫째")
        assertEquals(SendResult.Retry("503"), first)
        assertEquals(0, server.messages("c").size)
        // 그사이 둘째가 먼저 저장되면 seq 1 을 가져간다 → 첫째는 seq 2
        assertEquals(1L, (client.send("c", "id-2", "둘째") as SendResult.Sent).seq)
        assertEquals(2L, (client.send("c", "id-1", "$MARK_TRANSIENT 첫째") as SendResult.Sent).seq)
        assertEquals(listOf("둘째", "$MARK_TRANSIENT 첫째"), server.messages("c").map { it.body })
    }

    @Test
    fun `응답만 잃은 첫째는 이미 저장돼 있어 뒤에 보낸 둘째보다 앞 seq 를 지킨다`() = runBlocking {
        client.send("c", "id-1", "$MARK_LOSE_ACK 첫째")
        assertEquals(2L, (client.send("c", "id-2", "둘째") as SendResult.Sent).seq)
        assertEquals(1L, (client.send("c", "id-1", "$MARK_LOSE_ACK 첫째") as SendResult.Sent).seq)
        assertEquals(listOf("id-1", "id-2"), server.messages("c").map { it.clientId })
    }

    @Test
    fun `400 은 영구 실패이고 저장하지 않는다`() = runBlocking {
        assertEquals(SendResult.Permanent("400"), client.send("c", "id-1", "$MARK_REJECT 첫째"))
        assertEquals(0, server.messages("c").size)
    }

    @Test
    fun `401 은 토큰을 한 번 갱신하고 다시 보내 성공한다`() = runBlocking {
        server.expireTokenNext.set(1)
        val sent = client.send("c", "id-1", "안녕") as SendResult.Sent
        assertEquals("token-2", client.token)
        assertTrue(sent.note.contains("토큰 갱신 후"))
        assertEquals(listOf(401, 201), server.hits().asReversed().map { it.status })
    }

    @Test
    fun `오프라인이면 IOException 사슬이라 재시도 사유`() = runBlocking {
        server.offline = true
        val result = client.send("c", "id-1", "안녕")
        assertTrue(result is SendResult.Retry)
        assertEquals(0, server.messages("c").size)
    }

    @Test
    fun `서버를 새로 만들어도 파일에서 읽어 같은 clientId 를 알아보고 seq 를 이어 매긴다`() = runBlocking {
        client.send("c", "id-1", "$MARK_LOSE_ACK 안녕")
        // 앱 프로세스가 죽고 새 프로세스의 서버 객체가 같은 파일을 읽는 상황
        val restarted = FakeChatServer(file).apply { latencyMs = 0 }
        HttpClient(restarted.engine()).use { http2 ->
            val client2 = OutboxClient(http2)
            val retry = client2.send("c", "id-1", "$MARK_LOSE_ACK 안녕") as SendResult.Sent
            assertEquals(1L, retry.seq)
            assertTrue(retry.replayed)
            assertEquals(2L, (client2.send("c", "id-2", "다음") as SendResult.Sent).seq)
        }
        Unit
    }

    @Test
    fun `seq 는 대화마다 따로 매긴다`() = runBlocking {
        assertEquals(1L, (client.send("a", "id-1", "x") as SendResult.Sent).seq)
        assertEquals(1L, (client.send("b", "id-2", "y") as SendResult.Sent).seq)
        assertEquals(2L, (client.send("a", "id-3", "z") as SendResult.Sent).seq)
    }

    @Test
    fun `재시도 분류 — 상태 코드와 예외 사슬`() {
        assertEquals(SendDecision.SUCCESS, decideStatus(201))
        assertEquals(SendDecision.RETRY, decideStatus(408))
        assertEquals(SendDecision.RETRY, decideStatus(429))
        assertEquals(SendDecision.RETRY, decideStatus(500))
        assertEquals(SendDecision.RETRY, decideStatus(503))
        assertEquals(SendDecision.PERMANENT, decideStatus(400))
        assertEquals(SendDecision.PERMANENT, decideStatus(404))
        assertEquals(SendDecision.REFRESH_THEN_RETRY, decideStatus(401))
        assertEquals(SendDecision.RETRY, classifyFailure(LostResponseException("x")))
        // debug(HotSwan)에서는 람다가 던진 checked 예외가 감싸여 나온다 — 사슬로 보면 같은 판정
        assertEquals(SendDecision.RETRY, classifyFailure(UndeclaredThrowableException(LostResponseException("x"))))
        assertEquals(SendDecision.PERMANENT, classifyFailure(IllegalStateException("x")))
    }

    @Test
    fun `지수 백오프는 10초에서 두 배씩 — 4번째 시도 전까지 10·20·40초`() {
        assertEquals(listOf(10_000L, 20_000L, 40_000L), exponentialBackoffMillis(4, 10_000L))
    }
}
