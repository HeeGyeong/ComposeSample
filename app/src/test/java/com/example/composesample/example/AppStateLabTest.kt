package com.example.composesample.example

import android.os.Trace
import androidx.appstate.statestore.StateStore
import androidx.appstate.transform.testing.transformTest
import com.example.composesample.presentation.example.component.architecture.pattern.appstate.AutoClearCase
import com.example.composesample.presentation.example.component.architecture.pattern.appstate.Tier
import com.example.composesample.presentation.example.component.architecture.pattern.appstate.TierKey
import com.example.composesample.presentation.example.component.architecture.pattern.appstate.runAutoClear
import com.example.composesample.presentation.example.component.architecture.pattern.appstate.runKeyIdentity
import com.example.composesample.presentation.example.component.architecture.pattern.appstate.runPersistence
import com.example.composesample.presentation.example.component.architecture.pattern.appstate.runTierBranch
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.file.Files

/**
 * AppState 예제(#163)의 시나리오 — 화면이 부르는 함수를 그대로 JVM 에서 돌린다(androidx.appstate 1.0.0-alpha02).
 * given : StateStore 키 · autoClearKey · transform 분기 · syncToDataStore 파일
 * when : 키를 다시 읽고 / 사용자를 바꾸고 / 등급 · 화질을 바꾸고 / 같은 파일로 세 번 켠다
 * then : class 키는 새 칸, 먼저 읽은 키는 자동 정리가 안 걸리고, 떠난 분기는 수집을 멈추며, null · 자동 정리는 파일에 안 남는다
 */
class AppStateLabTest {

    @Before
    fun mockTrace() {
        // transform · 자동 정리 감시는 runtime-android 컴포지션이라 android.os.Trace 를 부른다 — 단위 테스트 android.jar 엔 구현이 없다
        mockkStatic(Trace::class)
        every { Trace.beginSection(any()) } returns Unit
        every { Trace.endSection() } returns Unit
    }

    @After
    fun unmockTrace() {
        unmockkStatic(Trace::class)
    }

    @Test
    fun class_키는_인스턴스마다_다른_칸이다() {
        val r = runKeyIdentity()
        assertEquals(3, r.objectRead)
        assertEquals(0, r.classRead)
        assertEquals(3, r.keyCount)
    }

    @Test
    fun 자동_정리는_첫_setState_때만_걸리고_그때_값과_달라져야_지운다() = runBlocking {
        val results = AutoClearCase.entries.associateWith { runAutoClear(it) }
        results.values.forEach { println("autoClear ${it.case} cleared=${it.cleared} after=${it.clearedAfterMillis} cart=${it.cartAfter} leftover=${it.leftoverRecomposers}") }
        assertTrue(results.getValue(AutoClearCase.Plain).cleared)
        assertFalse("먼저 getState 하면 감시가 안 걸린다", results.getValue(AutoClearCase.ReadFirst).cleared)
        assertTrue("손님(null)일 때 담은 장바구니는 로그인하면 지워진다", results.getValue(AutoClearCase.Guest).cleared)
        assertFalse("같은 값으로 다시 쓰면 그대로", results.getValue(AutoClearCase.SameUser).cleared)
        assertEquals("정리된 키는 감시 컴포지션을 닫는다", 0, results.getValue(AutoClearCase.Plain).leftoverRecomposers)
        assertEquals("감시가 안 걸리면 남는 것도 없다", 0, results.getValue(AutoClearCase.ReadFirst).leftoverRecomposers)
        assertEquals("정리되지 않은 감시는 Recomposer 하나를 쥔 채 남는다", 1, results.getValue(AutoClearCase.SameUser).leftoverRecomposers)
    }

    @Test
    fun transform_은_떠난_분기의_수집을_멈추고_Flow_판과_같은_값을_낸다() = runBlocking {
        val r = runTierBranch()
        r.steps.forEach { println("tier ${it.action} t='${it.transformValue}' f='${it.flowValue}' tc=${it.transformCollectors} fc=${it.flowCollectors} runs=${it.transformRuns} tms=${it.transformMillis} fms=${it.flowMillis}") }
        println("thread ${r.transformThread}")
        r.steps.forEach { assertEquals(it.action, it.flowValue, it.transformValue) }
        val byAction = r.steps.associateBy { it.action }
        assertEquals(0, byAction.getValue("시작(Free)").transformCollectors)
        assertEquals(1, byAction.getValue("Premium 으로").transformCollectors)
        assertEquals(1, byAction.getValue("Premium 으로").flowCollectors)
        assertEquals("Free 로 바뀌면 분기가 빠지며 수집 취소", 0, byAction.getValue("Free 로").transformCollectors)
        assertEquals(0, byAction.getValue("Free 로").flowCollectors)
        assertEquals("Free 인 채 화질이 바뀌어도 transform 은 다시 돌지 않는다", 0, byAction.getValue("Free 인 채 화질 SD").transformRuns)
        assertTrue(r.transformThread, r.transformThread.startsWith("DefaultDispatcher"))
    }

    @Test
    fun null_과_자동_정리는_파일에_남지_않아_다음_실행에_되살아난다() = runBlocking {
        val dir = Files.createTempDirectory("appstate").toFile()
        try {
            val r = runPersistence(dir)
            r.steps.forEach { println("persist ${it.label}: ${it.detail}") }
            println("file ${r.fileAfterFirstRun}")
            val steps = r.steps.associateBy { it.label }
            assertTrue(steps.getValue("1회차 로그아웃 · 사용자 변경").detail.contains("프로필 민지 그대로"))
            assertTrue(steps.getValue("1회차 로그아웃 · 사용자 변경").detail.contains("장바구니 사과 그대로"))
            assertTrue(steps.getValue("2회차 복원").detail.contains("프로필 민지"))
            assertTrue(steps.getValue("2회차 복원").detail.contains("[사과]"))
            assertTrue(steps.getValue("2회차 사용자 변경").detail.contains("그대로"))
            assertTrue("복원이 앞선 쓰기를 덮는다", steps.getValue("3회차 복원 전 쓰기").detail.contains("저장소 'dark'"))
            assertEquals("저장 이름은 키 class 의 qualifiedName", setOf("ThemeKey", "ProfileKey", "SavedCartKey"), r.keyNames.map { it.substringAfterLast('.') }.toSet())
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun transformTest_로_프레임을_손으로_민다() = transformTest {
        val store = StateStore()
        val scope = TransformScope()
        val policy = scope.launchTransform("") {
            if (store.getState(TierKey).value == Tier.Premium) "광고 없음" else "광고 + 저화질"
        }
        backgroundScope.launch(testDispatcher) { scope.run() }
        advance()
        assertEquals("광고 + 저화질", policy.value)
        store.setState(TierKey, Tier.Premium)
        assertEquals("advance 전에는 그대로", "광고 + 저화질", policy.value)
        advance()
        assertEquals("광고 없음", policy.value)
    }
}
