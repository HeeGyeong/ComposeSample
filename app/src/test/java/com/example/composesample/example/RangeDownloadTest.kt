package com.example.composesample.example

import com.example.composesample.presentation.example.component.data.download.ClientOptions
import com.example.composesample.presentation.example.component.data.download.FakeFileServer
import com.example.composesample.presentation.example.component.data.download.RANGE_FILE_SIZE
import com.example.composesample.presentation.example.component.data.download.RANGE_FILE_URL
import com.example.composesample.presentation.example.component.data.download.RangeDownloader
import com.example.composesample.presentation.example.component.data.download.ReplaceTiming
import com.example.composesample.presentation.example.component.data.download.ResumeCheck
import com.example.composesample.presentation.example.component.data.download.RunOutcome
import com.example.composesample.presentation.example.component.data.download.StoredDownloadState
import com.example.composesample.presentation.example.component.data.download.StoredSegment
import com.example.composesample.presentation.example.component.data.download.TokenBucket
import com.example.composesample.presentation.example.component.data.download.Trap
import com.example.composesample.presentation.example.component.data.download.parseRange
import com.example.composesample.presentation.example.component.data.download.planSegments
import com.example.composesample.presentation.example.component.data.download.runResumeScenario
import com.example.composesample.presentation.example.component.data.download.runTrap
import com.example.composesample.presentation.example.component.data.download.toDownloadState
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * RangeDownloadExample 의 엔진 검증 — 화면 카드가 쓰는 시나리오 함수를 그대로 실행한다.
 * given : MockEngine 에 물린 FakeFileServer(2MiB, ETag "v1") · 임시 폴더의 RangeDownloader · 속도 제한 없음
 * when : 분할 다운로드 / 40% 일시정지 후 재개 / 서버 파일 교체 / 함정 스위치
 * then : 결과 파일이 기대 버전과 같은 크기·CRC 인가, 서버가 받은 요청이 기대와 같은가
 */
class RangeDownloadTest {

    private val dir: File = Files.createTempDirectory("range").toFile()
    private val server = FakeFileServer()
    private val client = HttpClient(server.engine())
    private val downloader = RangeDownloader(client, dir)

    @After
    fun tearDown() {
        client.close()
        dir.deleteRecursively()
    }

    @Test
    fun `크기를 연결 수만큼 나누고 Range 미지원이면 하나로 받는다`() {
        val four = planSegments(RANGE_FILE_SIZE.toLong(), rangeSupported = true, connections = 4)
        assertEquals(listOf(0L, 524288L, 1048576L, 1572864L), four.map { it.start })
        assertEquals(RANGE_FILE_SIZE - 1L, four.last().end)
        assertEquals(1, planSegments(RANGE_FILE_SIZE.toLong(), rangeSupported = false, connections = 4).size)
    }

    @Test
    fun `Range 헤더 해석 — 열린 끝과 범위 밖`() {
        assertEquals(0L to 0L, parseRange("bytes=0-0", 100))
        assertEquals(40L to 99L, parseRange("bytes=40-", 100))
        assertEquals(90L to 99L, parseRange("bytes=90-200", 100))
        assertNull(parseRange("bytes=100-", 100))
    }

    @Test
    fun `연결 4개로 나눠 받으면 v1 과 CRC 가 같고 서버는 HEAD 1 · 206 4 를 받는다`() {
        runBlocking {
            val outcome = downloader.start(RANGE_FILE_URL, ClientOptions(connections = 4, bytesPerSecond = 0))

            assertTrue(outcome.toString(), outcome is RunOutcome.Completed)
            assertEquals(server.crcOfVersion(1), (outcome as RunOutcome.Completed).crc)
            assertEquals(mapOf("HEAD 200" to 1, "GET 206" to 4), server.hits.groupingBy { "${it.method} ${it.status}" }.eachCount())
            assertFalse(downloader.partFile.exists())
            assertFalse(downloader.stateFile.exists())
        }
    }

    @Test
    fun `HEAD 를 거부하면 첫 1바이트 GET 의 Content-Range 로 크기를 안다`() {
        runBlocking {
            server.headAllowed = false

            val info = downloader.resolve(RANGE_FILE_URL)

            assertFalse(info.viaHead)
            assertEquals(206, info.status)
            assertEquals(RANGE_FILE_SIZE.toLong(), info.size)
            assertTrue(info.acceptRanges)
            assertEquals("\"v1\"", info.etag)
        }
    }

    @Test
    fun `40퍼센트에서 멈췄다가 If-Range 로 이어받으면 나머지만 206 으로 받는다`() {
        runBlocking {
            // Given
            val paused = downloader.start(RANGE_FILE_URL, ClientOptions(bytesPerSecond = 0, pauseAtFraction = 0.4f))
            assertTrue(paused is RunOutcome.Paused)
            val doneBefore = (paused as RunOutcome.Paused).state.doneBytes
            assertTrue(downloader.partFile.exists() && downloader.stateFile.exists())
            val from = server.hits.size

            // When
            val outcome = downloader.resume(ClientOptions(bytesPerSecond = 0))

            // Then: 재개 요청은 전부 If-Range 를 단 206, 받은 바이트는 남은 만큼
            assertTrue(outcome is RunOutcome.Completed)
            assertEquals(server.crcOfVersion(1), (outcome as RunOutcome.Completed).crc)
            val resumed = server.hits.drop(from)
            assertTrue(resumed.all { it.method == "GET" && it.status == 206 && it.ifRange == "\"v1\"" })
            assertEquals(RANGE_FILE_SIZE - doneBefore, outcome.receivedBytes)
        }
    }

    @Test
    fun `재개 확인 3가지 × 교체 시점 2가지 — HEAD 비교는 확인 뒤 교체를 놓치고 확인 없음은 둘 다 섞인다`() {
        runBlocking {
            val expected = mapOf(
                (ResumeCheck.IF_RANGE to ReplaceTiming.WHILE_PAUSED) to true,
                (ResumeCheck.IF_RANGE to ReplaceTiming.AFTER_CHECK) to true,
                (ResumeCheck.HEAD_COMPARE to ReplaceTiming.WHILE_PAUSED) to true,
                (ResumeCheck.HEAD_COMPARE to ReplaceTiming.AFTER_CHECK) to false,
                (ResumeCheck.NONE to ReplaceTiming.WHILE_PAUSED) to false,
                (ResumeCheck.NONE to ReplaceTiming.AFTER_CHECK) to false
            )
            expected.forEach { (key, ok) ->
                val result = runResumeScenario(server, downloader, key.first, key.second)
                assertEquals("$key → ${result.verdict} / ${result.requests}", ok, result.ok)
                if (!ok) {
                    // 섞인 파일: 크기는 같고 앞 40% 남짓은 v1, 나머지는 v2 와 같다
                    assertEquals(RANGE_FILE_SIZE.toLong(), result.size)
                    assertTrue(
                        "$key v1=${result.sameAsV1} v2=${result.sameAsV2} ${result.requests}",
                        result.sameAsV1 > RANGE_FILE_SIZE * 0.35 && result.sameAsV2 > RANGE_FILE_SIZE * 0.5
                    )
                }
            }
        }
    }

    @Test
    fun `Range 미지원 서버의 200 을 이어 쓰면 파일이 커지고 확인하면 처음부터 받아 맞는다`() {
        runBlocking {
            val careless = runTrap(server, downloader, Trap.STATUS_200, careful = false)
            assertFalse(careless.verdict, careless.ok)
            assertTrue(careless.size > RANGE_FILE_SIZE)

            val careful = runTrap(server, downloader, Trap.STATUS_200, careful = true)
            assertTrue(careful.verdict, careful.ok)
            assertEquals(1, careful.restarts)
        }
    }

    @Test
    fun `끊긴 응답을 완료로 보면 크기는 같은데 내용이 틀리고 길이를 확인하면 재시도로 맞춘다`() {
        runBlocking {
            val careless = runTrap(server, downloader, Trap.EARLY_EOF, careful = false)
            assertFalse(careless.verdict, careless.ok)
            assertEquals(RANGE_FILE_SIZE.toLong(), careless.size)

            val careful = runTrap(server, downloader, Trap.EARLY_EOF, careful = true)
            assertTrue(careful.verdict, careful.ok)
            // 512KiB 구간 4개를 300KiB 씩 끊어 보내므로 구간마다 1번 다시 요청한다
            assertEquals(4, careful.retries)
        }
    }

    @Test
    fun `토큰 버킷은 버킷을 넘는 만큼 속도에 맞춰 쉰다`() {
        runBlocking {
            var now = 0L
            var slept = 0L
            val bucket = TokenBucket(
                bytesPerSecond = 64 * 1024,
                capacity = 16 * 1024,
                nanoTime = { now },
                sleep = { ms -> slept += ms; now += ms * 1_000_000 }
            )

            repeat(10) { bucket.acquire(16 * 1024) }

            // 160KiB 중 처음 16KiB 는 버킷에 있던 몫, 나머지 144KiB 를 64KiB/s 로 → 2.25초
            assertEquals(2250L, slept)
        }
    }

    @Test
    fun `상태 파일에 필드가 빠지면 이어받지 않고 null 이다`() {
        assertNull(StoredDownloadState(RANGE_FILE_URL, null, "\"v1\"", null, true, listOf(StoredSegment(0, 9, 3))).toDownloadState())
        assertNull(StoredDownloadState(RANGE_FILE_URL, 10, "\"v1\"", null, true, listOf(StoredSegment(0, null, 3))).toDownloadState())
        assertEquals(3L, StoredDownloadState(RANGE_FILE_URL, 10, "\"v1\"", null, null, listOf(StoredSegment(0, 9, 3))).toDownloadState()?.doneBytes)
    }
}
