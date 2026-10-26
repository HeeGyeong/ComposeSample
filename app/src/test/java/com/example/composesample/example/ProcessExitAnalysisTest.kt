package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.platform.exitinfo.CrashRecord
import com.example.composesample.presentation.example.component.system.platform.exitinfo.ExitSnapshot
import com.example.composesample.presentation.example.component.system.platform.exitinfo.Experiment
import com.example.composesample.presentation.example.component.system.platform.exitinfo.ExperimentKind
import com.example.composesample.presentation.example.component.system.platform.exitinfo.MAX_STATE_SUMMARY_BYTES
import com.example.composesample.presentation.example.component.system.platform.exitinfo.decodeCrash
import com.example.composesample.presentation.example.component.system.platform.exitinfo.decodeExperiment
import com.example.composesample.presentation.example.component.system.platform.exitinfo.encodeCrash
import com.example.composesample.presentation.example.component.system.platform.exitinfo.encodeExperiment
import com.example.composesample.presentation.example.component.system.platform.exitinfo.appFrameCount
import com.example.composesample.presentation.example.component.system.platform.exitinfo.isSuperseded
import com.example.composesample.presentation.example.component.system.platform.exitinfo.mainThreadStack
import com.example.composesample.presentation.example.component.system.platform.exitinfo.markerLine
import com.example.composesample.presentation.example.component.system.platform.exitinfo.matchExit
import com.example.composesample.presentation.example.component.system.platform.exitinfo.probeText
import com.example.composesample.presentation.example.component.system.platform.exitinfo.summaryOf
import com.example.composesample.presentation.example.component.system.platform.exitinfo.utf8Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * '프로세스 종료 사후 분석' 예제의 순수 로직 검증.
 * given : 끝내기 직전 남긴 실험·크래시 기록, 시스템 종료 기록, ANR 트레이스 텍스트
 * when : 직렬화 왕복 / pid 대조 / main 스레드 스택 추출 / 상태 요약 크기
 * then : 다음 실행에서 화면이 맞춰 보일 값
 */
class ProcessExitAnalysisTest {

    private fun exit(pid: Int, at: Long, reason: Int = 6) = ExitSnapshot(
        pid = pid, reason = reason, status = 0, timestamp = at, processName = "p", importance = 100,
        pssKb = 0, rssKb = 0, description = null, summary = null, traceBytes = null
    )

    @Test
    fun `실험 기록은 한 줄로 쓰고 그대로 읽힌다`() {
        val e = Experiment(ExperimentKind.ANR, pid = 4321, triggeredAt = 1_000L, summary = "cs159|ANR|pid=4321|10:00:00")
        assertEquals(e, decodeExperiment(encodeExperiment(e)))
        assertNull(decodeExperiment("깨진 줄"))
        assertNull(decodeExperiment(encodeExperiment(e).replace("ANR", "UNKNOWN")))
    }

    @Test
    fun `크래시 메시지의 줄바꿈은 한 줄로 바뀐다`() {
        val c = CrashRecord(pid = 7, at = 5L, thread = "main", exception = "java.lang.IllegalStateException", message = "첫 줄\n둘째 줄")
        assertEquals(c.copy(message = "첫 줄 둘째 줄"), decodeCrash(encodeCrash(c)))
    }

    @Test
    fun `같은 pid 이고 실험 뒤에 끝난 기록만 맞춘다`() {
        val e = Experiment(ExperimentKind.CRASH, pid = 100, triggeredAt = 50_000L, summary = "s")
        val reused = exit(pid = 100, at = 10_000L)       // 같은 pid 를 쓰던 예전 프로세스
        val other = exit(pid = 200, at = 51_000L)
        val mine = exit(pid = 100, at = 50_300L, reason = 4)
        assertEquals(mine, matchExit(e, listOf(other, reused, mine)))
        assertNull(matchExit(e, listOf(other, reused)))
    }

    @Test
    fun `같은 pid 에서 뒤에 또 실험했으면 앞 실험은 끝나지 않은 것`() {
        val first = Experiment(ExperimentKind.ANR, pid = 3486, triggeredAt = 1_000L, summary = "a")
        val second = Experiment(ExperimentKind.ANR, pid = 3486, triggeredAt = 2_000L, summary = "b")
        val other = Experiment(ExperimentKind.CRASH, pid = 77, triggeredAt = 3_000L, summary = "c")
        val all = listOf(second, first, other)
        assertTrue(isSuperseded(first, all))
        assertFalse(isSuperseded(second, all))
        assertFalse(isSuperseded(other, all))
    }

    @Test
    fun `ANR 트레이스에서 main 스레드 스택만 다음 스레드 전까지 뽑는다`() {
        val trace = """
            ----- pid 4321 at 2026-10-26 10:00:00.000 -----
            Cmd line: com.example.composesample

            "main" prio=5 tid=1 Sleeping
              | group="main" sCount=1 ucsCount=0 flags=1 obj=0x72f0 self=0xb400
              | held mutexes=
              native: #00 pc 0005f3c8  /apex/com.android.runtime/lib64/bionic/libc.so (nanosleep+8)
              at java.lang.Thread.sleep(Native method)
              - sleeping on <0x0a1b2c3d> (a java.lang.Object)
              at java.lang.Thread.sleep(Thread.java:450)
              at com.example.composesample.ProcessExitAnalysisViewModel.blockMainThreadForAnr(ProcessExitAnalysisViewModel.kt:90)
              at com.example.composesample.ProcessExitAnalysisViewModel.triggerAnr(ProcessExitAnalysisViewModel.kt:84)

            "Signal Catcher" daemon prio=10 tid=2 Runnable
              at nothing.Else(Else.java:1)
        """.trimIndent()
        val stack = mainThreadStack(trace)
        assertEquals("native: #00 pc 0005f3c8  /apex/com.android.runtime/lib64/bionic/libc.so (nanosleep+8)", stack.first())
        assertEquals(6, stack.size)
        assertEquals(5, markerLine(stack, "blockMainThreadForAnr"))
        assertNull(markerLine(stack, "없는함수"))
        assertEquals(2, appFrameCount(stack, "com.example.composesample"))
        assertFalse(stack.any { it.contains("nothing.Else") })
        assertEquals(listOf("native: #00 pc 0005f3c8  /apex/com.android.runtime/lib64/bionic/libc.so (nanosleep+8)", "at java.lang.Thread.sleep(Native method)"), mainThreadStack(trace, maxLines = 2))
        assertTrue(mainThreadStack("main 이 없는 텍스트").isEmpty())
    }

    @Test
    fun `실험 상태 요약은 ASCII 라 한도 안에 넉넉하다`() {
        val summary = summaryOf(ExperimentKind.KILL_SELF, pid = 2_147_483_647, at = 0L)
        assertTrue(summary.startsWith("cs159|KILL_SELF|pid=2147483647|"))
        assertTrue(utf8Size(summary) <= MAX_STATE_SUMMARY_BYTES)
        assertEquals(summary.length, utf8Size(summary))
    }

    @Test
    fun `한도 실험 문자열 — 같은 129바이트에 ASCII 는 129자, 한글은 43자`() {
        assertEquals(129, utf8Size(probeText(129, korean = false)))
        assertEquals(129, probeText(129, korean = false).length)
        assertEquals(129, utf8Size(probeText(129, korean = true)))
        assertEquals(43, probeText(129, korean = true).length)
        // 128 바이트 한글 = 42자 + 남는 2바이트는 ASCII
        assertEquals(128, utf8Size(probeText(128, korean = true)))
        assertEquals(44, probeText(128, korean = true).length)
    }
}
