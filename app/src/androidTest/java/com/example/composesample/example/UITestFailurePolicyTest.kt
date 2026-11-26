package com.example.composesample.example

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.compose.ui.test.ComposeUiTestConfig
import androidx.compose.ui.test.FailureArtifact
import androidx.compose.ui.test.FailureContext
import androidx.compose.ui.test.TestFailureHandler
import androidx.compose.ui.test.TestFailurePolicy
import androidx.compose.ui.test.TestFailurePolicy.CaptureMode
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.io.PlatformTestStorageRegistry
import com.example.composesample.presentation.example.component.architecture.development.test.UITestExampleUI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 실패 순간 자동 저장 — TestFailurePolicy(ui-test 1.13). 일부러 틀린 기대로 runComposeUiTest 를 실패시키고
 * 바깥에서 그 실패를 잡아 본다 — 스위트에는 '실패하는 테스트' 가 남지 않는다.
 * given : UITestExampleUI 에 10 + 20 을 넣고 계산(결과: 30)
 * when : 결과가 "결과: 31" 이라고 단언(틀림) — 정책을 기본값 · 켬 · 끔 · 던지는 핸들러로 바꿔 가며
 * then : 기본값은 핸들러만 불리고 파일 0, 켜면 스크린샷 · 트리 두 파일이 FileTestStorage 폴더에, 핸들러 예외는 원래 실패에 suppressed 로
 *
 * 스위트 인자로도 켤 수 있다(-e androidx.compose.ui.test.failure.isScreenshotCaptureEnabled true …) — 테스트는 인자를 읽어 기대를 맞춘다.
 */
class UITestFailurePolicyTest {

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val args get() = InstrumentationRegistry.getArguments()

    private fun suiteArg(name: String): Boolean =
        args.getString("androidx.compose.ui.test.failure.$name")?.toBoolean() ?: false

    /** FileTestStorage(androidx.test monitor) 와 같은 규칙 — 인자 additionalTestOutputDir, 없으면 API 29+ 미디어 폴더 · 그 아래는 외부 캐시 */
    private fun outputDir(): File {
        args.getString("additionalTestOutputDir")?.let { return File(it) }
        val context = instrumentation.targetContext
        @Suppress("DEPRECATION")
        val media = if (Build.VERSION.SDK_INT >= 29) {
            context.externalMediaDirs.firstOrNull { Environment.getExternalStorageState(it) == Environment.MEDIA_MOUNTED }
        } else {
            null
        }
        return File(media ?: context.externalCacheDir ?: context.cacheDir, "additionalTestOutputDir")
    }

    /** 일부러 틀린 기대로 실패시키고, 잡은 실패와 핸들러가 받은 문맥을 돌려준다 */
    private fun failingRun(policy: TestFailurePolicy): Pair<Throwable, List<FailureContext>> {
        val seen = mutableListOf<FailureContext>()
        val recording = TestFailurePolicy(
            screenshotCaptureMode = policy.screenshotCaptureMode,
            uiHierarchyCaptureMode = policy.uiHierarchyCaptureMode,
            failureHandlers = policy.failureHandlers + TestFailureHandler { seen += it }
        )
        val error = runCatching {
            runComposeUiTest(ComposeUiTestConfig(failurePolicy = recording)) {
                setContent { UITestExampleUI(onBackEvent = {}) }
                onNodeWithTag("firstNumber").performTextInput("10")
                onNodeWithTag("secondNumber").performTextInput("20")
                onNodeWithTag("calculateButton").performClick()
                onNodeWithTag("resultText").assertTextEquals("결과: 31") // 실제는 30 — 일부러 틀린 기대
            }
        }.exceptionOrNull() ?: throw AssertionError("실패해야 하는 실행이 통과했다")
        return error to seen
    }

    @Test
    fun 기본값이면_핸들러는_불리고_캡처는_스위트_인자를_따른다() {
        val (error, seen) = failingRun(TestFailurePolicy())
        assertTrue("원래 실패 ${error::class.java.name}", error is AssertionError)
        assertEquals("핸들러는 한 번", 1, seen.size)
        assertSame("핸들러가 받은 error 는 던져진 그 실패", error, seen[0].error)
        val expected = listOfNotNull(
            FailureArtifact.Type.Screenshot.takeIf { suiteArg("isScreenshotCaptureEnabled") },
            FailureArtifact.Type.UiHierarchy.takeIf { suiteArg("isUiHierarchyCaptureEnabled") }
        )
        assertEquals("Unspecified = 스위트 인자(없으면 꺼짐)", expected, seen[0].artifacts.map { it.type })
        Log.i(TAG, "default storage=${PlatformTestStorageRegistry.getInstance()::class.java.name} artifacts=${seen[0].artifacts.map { it.fileName }}")
        deleteArtifacts(seen[0])
    }

    @Test
    fun 켜면_스크린샷과_트리_두_파일이_테스트_저장소에_남는다() {
        val (error, seen) = failingRun(TestFailurePolicy(CaptureMode.Enabled, CaptureMode.Enabled))
        assertTrue(error is AssertionError)
        val artifacts = seen.single().artifacts
        assertEquals(listOf(FailureArtifact.Type.Screenshot, FailureArtifact.Type.UiHierarchy), artifacts.map { it.type })
        // 이름은 같은 System.nanoTime() 앞머리 + 고정 꼬리
        val shot = artifacts[0].fileName
        val tree = artifacts[1].fileName
        assertTrue(shot, Regex("""\d+_screenshot\.png""").matches(shot))
        assertTrue(tree, Regex("""\d+_ui\.txt""").matches(tree))
        assertEquals(shot.substringBefore('_'), tree.substringBefore('_'))

        val dir = outputDir()
        val png = File(dir, shot)
        val txt = File(dir, tree)
        assertTrue("스크린샷 파일 ${png.absolutePath}", png.isFile && png.length() > 0)
        assertTrue("트리 파일 ${txt.absolutePath}", txt.isFile && txt.length() > 0)
        // 스크린샷은 UiAutomation.takeScreenshot — 컴포즈 노드가 아니라 기기 화면 전체
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(png.absolutePath, bounds)
        val metrics = instrumentation.targetContext.resources.displayMetrics
        assertEquals("스크린샷 폭 = 화면 폭", metrics.widthPixels, bounds.outWidth)
        // 트리는 View 계층 사이에 컴포즈 시맨틱 트리를 끼워 넣은 글 — 실패한 노드의 실제 값이 들어 있다
        val dump = txt.readText()
        assertTrue("머리글", dump.contains("--- View and Compose Hierarchy ---"))
        assertTrue("실제 결과 문구", dump.contains("결과: 30"))
        assertTrue("testTag", dump.contains("resultText"))
        Log.i(
            TAG,
            "enabled storage=${PlatformTestStorageRegistry.getInstance()::class.java.simpleName} dir=${dir.absolutePath} " +
                "png=${png.length()}B ${bounds.outWidth}x${bounds.outHeight} txt=${txt.length()}B lines=${dump.lines().size} " +
                "views=${dump.lines().count { it.contains("{id=") }} composeRoot=${dump.lines().count { it.contains("Node #") }}"
        )
        deleteArtifacts(seen.single())
    }

    @Test
    fun 명시한_Disabled_는_스위트_인자를_이긴다() {
        val (_, seen) = failingRun(TestFailurePolicy(CaptureMode.Disabled, CaptureMode.Disabled))
        assertEquals(emptyList<FailureArtifact>(), seen.single().artifacts)
    }

    @Test
    fun 핸들러가_던져도_원래_실패가_남고_그_예외는_suppressed_로_붙는다() {
        val boom = TestFailureHandler { throw IllegalStateException("handler boom") }
        val (error, seen) = failingRun(TestFailurePolicy(failureHandlers = listOf(boom)))
        assertTrue("가려지지 않은 원래 실패", error is AssertionError)
        assertTrue("suppressed ${error.suppressed.toList()}", error.suppressed.any { it is IllegalStateException && it.message == "handler boom" })
        assertEquals("던진 핸들러 뒤 핸들러도 불린다", 1, seen.size)
        deleteArtifacts(seen.single()) // 스위트 인자로 켠 실행이면 여기서도 두 파일이 생긴다
    }

    /** 남은 파일을 지운다 — 손으로 꺼내 보려면 -e keepFailureArtifacts true */
    private fun deleteArtifacts(context: FailureContext) {
        if (args.getString("keepFailureArtifacts") == "true") return
        context.artifacts.forEach { File(outputDir(), it.fileName).delete() }
    }

    private companion object {
        const val TAG = "FailurePolicyTest"
    }
}
