package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.media.picker.CLOSING_ERROR_WINDOW_MILLIS
import com.example.composesample.presentation.example.component.ui.media.picker.EmbeddedPickerSupport
import com.example.composesample.presentation.example.component.ui.media.picker.PickedItem
import com.example.composesample.presentation.example.component.ui.media.picker.PickerEventKind
import com.example.composesample.presentation.example.component.ui.media.picker.PickerEventLog
import com.example.composesample.presentation.example.component.ui.media.picker.PickerFeatureLevel
import com.example.composesample.presentation.example.component.ui.media.picker.deselectRequested
import com.example.composesample.presentation.example.component.ui.media.picker.granted
import com.example.composesample.presentation.example.component.ui.media.picker.isClosingError
import com.example.composesample.presentation.example.component.ui.media.picker.revoked
import com.example.composesample.presentation.example.component.ui.media.picker.shortUri
import com.example.composesample.presentation.example.component.ui.media.picker.shortenUris
import com.example.composesample.presentation.example.component.ui.media.picker.unansweredDeselects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Embedded Photo Picker 실물 탭(#154)의 판정 · 기록 로직 — 화면이 쓰는 함수를 그대로 부른다.
 * given : SDK_INT · U 확장 숫자 / 권한 받음 · 회수 · 앱 해제 요청 순서 / 닫기 요청 뒤 경과 시간
 * when : 조건 판정 · 이력 갱신 · 기록 · 오류 분류
 * then : 확장 13 인 API 35 는 불가, 받은 순서 · 회수 지연 · 다시 받기가 이력에 남고, 닫은 직후 오류만 닫힌 세션 것으로 본다
 */
class EmbeddedPickerRealLabTest {

    private val u19 = "content://media/picker/0/com.android.providers.media.photopicker/media/19"
    private val u20 = "content://media/picker/0/com.android.providers.media.photopicker/media/20"
    private val u21 = "content://media/picker/0/com.android.providers.media.photopicker/media/21"

    @Test
    fun 조건은_API_와_U_확장_둘_다_본다() {
        // 실측 기기 세 대의 숫자
        assertTrue(EmbeddedPickerSupport(sdkInt = 36, extensionU = 17).available) // Pixel 8 API 36 에뮬
        assertFalse(EmbeddedPickerSupport(sdkInt = 35, extensionU = 13).available) // Pixel Tablet API 35 — API 만 보면 '가용' 으로 오판
        assertFalse(EmbeddedPickerSupport(sdkInt = 30, extensionU = 0).available)
        assertFalse(EmbeddedPickerSupport(sdkInt = 33, extensionU = 22).available) // 확장이 높아도 API 34 미만은 클래스가 없다
        assertTrue(EmbeddedPickerSupport(sdkInt = 35, extensionU = 13).reason.contains("확장 15 미만"))
        assertTrue(EmbeddedPickerSupport(sdkInt = 33, extensionU = 22).reason.contains("API 34"))
    }

    @Test
    fun 기능별_확장_요구치를_넘는_것만_쓸_수_있다() {
        val ext17 = EmbeddedPickerSupport(36, 17)
        assertEquals(listOf(PickerFeatureLevel.Base), PickerFeatureLevel.entries.filter { ext17.supports(it) })
        val ext21 = EmbeddedPickerSupport(36, 21)
        assertEquals(
            listOf(PickerFeatureLevel.Base, PickerFeatureLevel.Highlight, PickerFeatureLevel.LaunchExpanded),
            PickerFeatureLevel.entries.filter { ext21.supports(it) }
        )
        // 조건 자체가 안 되면 기본도 없다
        assertFalse(EmbeddedPickerSupport(35, 13).supports(PickerFeatureLevel.Base))
    }

    @Test
    fun 기록은_최근이_위고_잘려도_종류별_개수는_남는다() {
        var t = 1_000L
        val log = PickerEventLog(now = { t }, capacity = 3)
        log.add(PickerEventKind.SessionOpened, "#1")
        t += 500
        repeat(4) { log.add(PickerEventKind.Granted, "g$it"); t += 100 }
        assertEquals(3, log.events.size)
        assertEquals("g3", log.events.first().detail)
        assertEquals(800L, log.events.first().atMillis) // 첫 이벤트 기준 경과
        assertEquals(4, log.count(PickerEventKind.Granted))
        assertEquals(1, log.count(PickerEventKind.SessionOpened))
        assertEquals(5, log.events.first().seq)
    }

    @Test
    fun 받은_순서대로_쌓고_다시_받으면_회수_기록을_지운다() {
        var items = emptyList<PickedItem>().granted(listOf(u19), 10).granted(listOf(u21), 20).granted(listOf(u20), 30)
        assertEquals(listOf(u19, u21, u20), items.map { it.key }) // 누른 순서(1 · 3 · 2)
        items = items.deselectRequested(u21, 100).revoked(listOf(u21), 615)
        val revoked = items.single { it.key == u21 }
        assertFalse(revoked.selected)
        assertEquals(515L, revoked.revokeLatencyMillis) // 실측 '요청 뒤 515ms' 와 같은 계산
        // 세션을 다시 열면 미리 선택이 한 번에 다시 온다 — 회수 기록은 지우고 받은 횟수만 늘린다
        items = items.granted(listOf(u19, u20, u21), 700)
        assertTrue(items.all { it.selected })
        assertEquals(listOf(2, 2, 2), items.map { it.grantCount })
        assertNull(items.single { it.key == u21 }.revokeLatencyMillis)
    }

    @Test
    fun 해제를_요청하지_않은_회수는_지연을_남기지_않는다() {
        val items = emptyList<PickedItem>().granted(listOf(u19), 0).revoked(listOf(u19), 50) // 피커 안에서 다시 눌러 해제
        assertNull(items.single().revokeLatencyMillis)
    }

    @Test
    fun 기다린_시간_동안_회수_콜백이_없으면_따로_골라낸다() {
        val items = emptyList<PickedItem>().granted(listOf(u19, u20), 0).deselectRequested(u19, 100)
        assertEquals(emptyList<PickedItem>(), items.unansweredDeselects(now = 1_000, waitMillis = 2_000))
        assertEquals(listOf(u19), items.unansweredDeselects(now = 2_100, waitMillis = 2_000).map { it.key })
        assertEquals(emptyList<PickedItem>(), items.revoked(listOf(u19), 600).unansweredDeselects(now = 5_000, waitMillis = 2_000))
    }

    @Test
    fun 닫기_요청_직후_오류만_닫힌_세션_것으로_본다() {
        assertFalse(isClosingError(null)) // 닫은 적 없음 — 진짜 오류
        assertTrue(isClosingError(26)) // Compose 판 실측
        assertTrue(isClosingError(579)) // View 판 setProvider 실측
        assertTrue(isClosingError(CLOSING_ERROR_WINDOW_MILLIS))
        assertFalse(isClosingError(CLOSING_ERROR_WINDOW_MILLIS + 1))
        assertFalse(isClosingError(-5))
    }

    @Test
    fun URI_는_끝_두_마디로_줄인다() {
        assertEquals("…/media/19", shortUri(u19))
        assertEquals("a/b", shortUri("a/b"))
        assertEquals("권한 받음 …/media/19, …/media/20", shortenUris("권한 받음 $u19, $u20"))
        assertEquals("URI 없음", shortenUris("URI 없음"))
    }
}
