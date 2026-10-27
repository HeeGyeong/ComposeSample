package com.example.composesample.example

import androidx.datastore.core.CorruptionException
import com.example.composesample.presentation.example.component.data.storage.BoundaryMode
import com.example.composesample.presentation.example.component.data.storage.BoundaryResult
import com.example.composesample.presentation.example.component.data.storage.BoundarySide
import com.example.composesample.presentation.example.component.data.storage.DemoSettings
import com.example.composesample.presentation.example.component.data.storage.DemoSettingsSerializer
import com.example.composesample.presentation.example.component.data.storage.RaceResult
import com.example.composesample.presentation.example.component.data.storage.describeFailure
import com.example.composesample.presentation.example.component.data.storage.escapeControl
import com.example.composesample.presentation.example.component.data.storage.formatSettings
import com.example.composesample.presentation.example.component.data.storage.parseSettings
import com.example.composesample.presentation.example.component.data.storage.readPrefsInt
import com.example.composesample.presentation.example.component.data.storage.shortenPaths
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * DataStore 예제의 순수 부분 검증(7번 카드의 SP 파일 읽기·결과 계산·경로 줄이기 포함).
 * given : 저장 값·파일 내용
 * when : 직렬화 왕복 / 손상 내용 읽기 / 결과 계산
 * then : 같은 값이 돌아오거나 CorruptionException
 */
class DataStoreLabTest {

    @Test
    fun `직렬화 왕복은 같은 값`() = runBlocking {
        val value = DemoSettings(count = 42, label = "두 줄\n라벨")
        val out = ByteArrayOutputStream()
        DemoSettingsSerializer.writeTo(value, out)
        val back = DemoSettingsSerializer.readFrom(ByteArrayInputStream(out.toByteArray()))
        assertEquals(DemoSettings(42, "두 줄 라벨"), back)
    }

    @Test
    fun `헤더가 다르거나 숫자가 아니면 형식이 아니다`() {
        assertNull(parseSettings("garbage"))
        assertNull(parseSettings("v1\ncount=abc\nlabel=x"))
        assertEquals(DemoSettings(3, ""), parseSettings("v1\ncount=3"))
        assertEquals("v1\ncount=0\nlabel=기본값", formatSettings(DemoSettings()))
    }

    @Test
    fun `손상 내용을 읽으면 CorruptionException`() = runBlocking {
        try {
            DemoSettingsSerializer.readFrom(ByteArrayInputStream("\u0000broken".toByteArray()))
            fail("예외가 나야 한다")
        } catch (e: CorruptionException) {
            // 메시지에 NUL 이 그대로 들어가지 않는다
            assertEquals("읽을 수 없는 형식: \"\\x00broken\"", e.message)
        }
    }

    @Test
    fun `잃어버린 갱신 수와 예외 설명`() {
        assertEquals(37, RaceResult("SharedPreferences", expected = 100, actual = 63, millis = 5).lost)
        assertEquals("a\\x00b · c\\x7F", escapeControl("a\u0000b\nc\u007F"))
        assertEquals("IllegalStateException: multiple", describeFailure(IllegalStateException("multiple")))
        assertEquals("RuntimeException: outer ← IllegalStateException: inner", describeFailure(RuntimeException("outer", IllegalStateException("inner"))))
        assertEquals("RuntimeException ← IllegalStateException: x", describeFailure(RuntimeException(IllegalStateException("x"))))
    }

    @Test
    fun `SP 파일 XML 에서 int 를 캐시 없이 읽는다`() {
        val xml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n    <int name=\"count\" value=\"146\" />\n</map>\n"
        assertEquals(146, readPrefsInt(xml, "count"))
        assertNull(readPrefsInt(xml, "other"))
        assertNull(readPrefsInt("<map />", "count"))
    }

    @Test
    fun `두 프로세스 결과의 기대값과 잃은 갱신 수`() {
        val side = BoundarySide("main", lastSeen = 87, millis = 10)
        val result = BoundaryResult(BoundaryMode.SINGLE, 100, side, side, onDisk = 146, observed = listOf(146), reread = 146, diskAfter = 151)
        assertEquals(200, result.expected)
        assertEquals(54, result.lost)
        assertNull(result.copy(onDisk = null).lost)
    }

    @Test
    fun `실패 메시지의 절대 경로는 파일 이름만 남긴다`() {
        val message = "IOException: Unable to rename /data/data/com.example.composesample/files/datastore_demo/boundary.settings.tmp to " +
            "/data/data/com.example.composesample/files/datastore_demo/boundary.settings. This likely means … ← NoSuchFileException: " +
            "/data/data/com.example.composesample/files/datastore_demo/boundary.settings.tmp -> /data/data/com.example.composesample/files/datastore_demo/boundary.settings"
        assertEquals(
            "IOException: Unable to rename boundary.settings.tmp to boundary.settings. This likely means … ← NoSuchFileException: " +
                "boundary.settings.tmp -> boundary.settings",
            shortenPaths(message)
        )
    }
}
