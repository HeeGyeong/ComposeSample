package com.example.composesample.example

import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions
import android.util.Log
import android.view.View
import android.widget.photopicker.EmbeddedPhotoPickerFeatureInfo
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.photopicker.embedded.EmbeddedPhotoPickerView
import androidx.photopicker.embedded.ExperimentalPhotoPickerApi
import androidx.photopicker.embedded.compose.EmbeddedPhotoPicker
import androidx.photopicker.embedded.compose.EmbeddedPhotoPickerState
import androidx.photopicker.embedded.compose.ExperimentalPhotoPickerComposeApi
import androidx.photopicker.embedded.compose.rememberEmbeddedPhotoPickerState
import androidx.photopicker.embedded.testing.TestEmbeddedPhotoPickerProvider
import androidx.photopicker.embedded.testing.TestEmbeddedPhotoPickerSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Embedded Photo Picker 실물 탭(#154)이 기대는 라이브러리 동작 — photopicker-embedded-testing 의 가짜 Provider 로 세션을 연다.
 * 가짜도 플랫폼 인터페이스(android.widget.photopicker.*)를 구현하므로 API 34 + U 확장 15 기기에서만 돈다(그 밖은 건너뜀).
 * given : EmbeddedPhotoPicker(Compose) · EmbeddedPhotoPickerView(View) 에 TestEmbeddedPhotoPickerProvider
 * when : 선택 · 해제 · 설정 변경 · 치우고 다시 놓기 · 가시성 · 오류
 * then : 열린 세션에는 바뀐 설정이 가지 않고, 다시 열면 state 의 선택이 미리 선택으로 넘어가며, Compose 판은 가시성을 스스로 알리지 않는다
 *
 * 실제 서비스(com.google.android.photopicker)로 잰 시간 · 화면 · 크래시는 실물 탭 카드와 가이드에 있다 — 가짜는 콜백을 바로 부른다.
 */
/*
 * 플랫폼 피커 타입이 들어간 도우미는 파일 최상위(EmbeddedPhotoPickerRealTestKt)에 둔다 — 테스트 클래스 메서드 시그니처에 있으면
 * 확장 15 미만 기기에서 JUnit 이 메서드를 읽다가 NoClassDefFoundError(initializationError)로 assume 전에 실패한다(API 35 실측).
 */
private fun info(max: Int): EmbeddedPhotoPickerFeatureInfo =
    EmbeddedPhotoPickerFeatureInfo.Builder().setMaxSelectionLimit(max).setOrderedSelection(true).build()

private fun TestEmbeddedPhotoPickerProvider.only(): TestEmbeddedPhotoPickerSession =
    sessions.single() as TestEmbeddedPhotoPickerSession

private fun TestEmbeddedPhotoPickerProvider.newer(than: TestEmbeddedPhotoPickerSession): TestEmbeddedPhotoPickerSession =
    (sessions - than).single() as TestEmbeddedPhotoPickerSession

@OptIn(ExperimentalPhotoPickerComposeApi::class, ExperimentalPhotoPickerApi::class)
class EmbeddedPhotoPickerRealTest {

    @get:Rule
    val rule = createComposeRule()

    private val a: Uri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/19")
    private val b: Uri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/20")

    @Before
    fun requireEmbeddedPicker() {
        assumeTrue(
            "API 34 + U 확장 15 기기에서만",
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 15
        )
    }

    @Test
    fun 열린_세션에는_바뀐_설정이_가지_않고_deselectUri_는_세션에_회수를_요청한다() {
        val provider = TestEmbeddedPhotoPickerProvider.get()
        var max by mutableIntStateOf(3)
        lateinit var state: EmbeddedPhotoPickerState
        val revoked = mutableListOf<List<Uri>>()
        rule.setContent {
            state = rememberEmbeddedPhotoPickerState(onUriPermissionRevoked = { revoked += it })
            val featureInfo = remember(max) { info(max) }
            EmbeddedPhotoPicker(state, Modifier.size(200.dp), provider = provider, embeddedPhotoPickerFeatureInfo = featureInfo)
        }
        rule.waitUntil(5_000) { provider.sessions.size == 1 && state.surfacePackage != null }
        val s1 = provider.only()
        assertEquals(3, s1.featureInfo.maxSelectionLimit)

        // 세션 쪽 selectUris — 가짜 세션의 선택 목록에도 넣는다(provider.notifySelectedUris 는 콜백만 부른다)
        rule.runOnIdle { s1.selectUris(listOf(a, b)) }
        rule.runOnIdle { assertEquals(setOf(a, b), state.selectedMedia) }

        // 설정을 바꿔도 LaunchedEffect 키(state · provider)가 그대로라 세션은 하나 — 열린 세션은 max 3 그대로
        rule.runOnIdle { max = 1 }
        rule.waitForIdle()
        assertEquals(1, provider.sessions.size)
        assertEquals(3, provider.only().featureInfo.maxSelectionLimit)

        // deselectUri 는 세션의 requestRevokeUriPermission 으로 간다(가짜는 바로 회수 콜백 — 실물은 약 0.5초 뒤)
        rule.runOnIdle { runBlocking { state.deselectUri(a) } }
        rule.runOnIdle {
            assertEquals(listOf(b), s1.selectedUris)
            assertEquals(listOf(listOf(a)), revoked)
            assertEquals(setOf(b), state.selectedMedia)
        }
        Log.i(TAG, "config-ignored sessions=${provider.sessions.size} max=${s1.featureInfo.maxSelectionLimit} selected=${state.selectedMedia}")
    }

    @Test
    fun 치우고_닫힘을_확인한_뒤_다시_놓으면_새_설정과_선택이_넘어간다() {
        val provider = TestEmbeddedPhotoPickerProvider.get()
        var max by mutableIntStateOf(3)
        var shown by mutableStateOf(true)
        lateinit var state: EmbeddedPhotoPickerState
        rule.setContent {
            state = rememberEmbeddedPhotoPickerState()
            val featureInfo = remember(max) { info(max) }
            if (shown) {
                EmbeddedPhotoPicker(state, Modifier.size(200.dp), provider = provider, embeddedPhotoPickerFeatureInfo = featureInfo)
            }
        }
        rule.waitUntil(5_000) { state.surfacePackage != null }
        val s1 = provider.only()
        rule.runOnIdle { s1.selectUris(listOf(b, a)) }

        // 1) 치운다 → 세션이 닫히고 surfacePackage 가 null 이 될 때까지 기다린다(실물 탭의 relaunch 와 같은 순서)
        rule.runOnIdle { shown = false }
        rule.waitUntil(5_000) { state.surfacePackage == null }
        assertTrue("옛 세션 닫힘", s1.isClosed)

        // 2) 새 설정으로 다시 놓는다
        rule.runOnIdle {
            max = 5
            shown = true
        }
        rule.waitUntil(5_000) { provider.sessions.size == 2 && state.surfacePackage != null }
        val s2 = provider.newer(than = s1)
        assertEquals(5, s2.featureInfo.maxSelectionLimit)
        // state 가 쥔 선택이 미리 선택으로 — 고른 순서(b, a)를 그대로 넘긴다(실물 피커는 다시 받을 때 id 순으로 번호를 매겼다)
        assertEquals(listOf(b, a), s2.featureInfo.preSelectedUris)
        assertEquals(listOf(b, a), s2.selectedUris)
        Log.i(TAG, "relaunch s1Closed=${s1.isClosed} s2Max=${s2.featureInfo.maxSelectionLimit} preselected=${s2.featureInfo.preSelectedUris}")
    }

    @Test
    fun Compose_판은_가시성을_스스로_알리지_않고_펼침은_세션을_연_뒤_따로_보낸다() {
        val provider = TestEmbeddedPhotoPickerProvider.get()
        lateinit var state: EmbeddedPhotoPickerState
        rule.setContent {
            state = rememberEmbeddedPhotoPickerState(initialExpandedValue = true)
            EmbeddedPhotoPicker(state, Modifier.size(200.dp), provider = provider)
        }
        rule.waitUntil(5_000) { state.surfacePackage != null }
        val s1 = provider.only()
        val ext = SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        if (ext < 21) {
            // 확장 21 미만은 FeatureInfo 에 '펼친 채 시작' 이 없어 열린 뒤 notifyPhotoPickerExpanded(true)
            assertEquals(true, s1.lastExpandedState)
        }
        rule.runOnIdle { state.isExpanded = false }
        assertEquals(false, s1.lastExpandedState)
        // EmbeddedPhotoPicker 는 notifyVisibilityChanged 를 부르지 않는다 — 앱이 수명 주기로 알려야 한다
        assertNull(s1.lastNotifiedVisibility)
        rule.runOnIdle { state.notifyVisibilityChanged(false) }
        assertEquals(false, s1.lastNotifiedVisibility)
        Log.i(TAG, "visibility ext=$ext expanded=${s1.lastExpandedState} visibility=${s1.lastNotifiedVisibility}")
    }

    @Test
    fun 세션_오류_뒤에는_저절로_다시_열지_않는다() {
        val provider = TestEmbeddedPhotoPickerProvider.get()
        lateinit var state: EmbeddedPhotoPickerState
        val errors = mutableListOf<Throwable>()
        rule.setContent {
            state = rememberEmbeddedPhotoPickerState(onError = { errors += it })
            EmbeddedPhotoPicker(state, Modifier.size(200.dp), provider = provider)
        }
        rule.waitUntil(5_000) { state.surfacePackage != null }
        val s1 = provider.only()
        val boom = IllegalStateException()
        rule.runOnIdle { provider.notifySessionError(s1, boom) }
        rule.waitUntil(5_000) { state.surfacePackage == null }
        assertSame(boom, errors.single())
        assertTrue("오류 뒤 세션 닫힘", s1.isClosed)
        // runSession 은 끝났지만 LaunchedEffect 키가 그대로라 다시 돌지 않는다 — 피커를 치웠다 다시 놓아야 새 세션
        Thread.sleep(500)
        rule.waitForIdle()
        assertEquals(1, provider.sessions.size)
        assertNull(state.surfacePackage)
    }

    @Test
    fun View_판은_열린_뒤_설정을_무시하고_setProvider_로_다시_열며_가시성은_자동으로_알린다() {
        val provider = TestEmbeddedPhotoPickerProvider.get()
        lateinit var view: EmbeddedPhotoPickerView
        rule.setContent {
            AndroidView(
                modifier = Modifier.size(200.dp),
                factory = { context ->
                    EmbeddedPhotoPickerView(context).apply {
                        setProvider(provider)
                        setEmbeddedPhotoPickerFeatureInfo(info(3))
                    }.also { view = it }
                }
            )
        }
        rule.waitUntil(5_000) { provider.sessions.size == 1 }
        val s1 = provider.only()

        rule.runOnIdle { view.setEmbeddedPhotoPickerFeatureInfo(info(1)) }
        rule.waitForIdle()
        assertEquals("열린 세션이 있으면 새로 열지 않는다", 1, provider.sessions.size)

        rule.runOnIdle { view.visibility = View.GONE }
        assertEquals(false, s1.lastNotifiedVisibility)
        rule.runOnIdle { view.visibility = View.VISIBLE }
        assertEquals(true, s1.lastNotifiedVisibility)

        rule.runOnIdle { view.setProvider(provider) }
        rule.waitUntil(5_000) { provider.sessions.size == 2 }
        val s2 = provider.newer(than = s1)
        assertTrue(s1.isClosed)
        assertEquals("마지막으로 넣은 설정으로 다시 연다", 1, s2.featureInfo.maxSelectionLimit)
        assertEquals("View 판은 앞 선택을 넘기지 않는다", emptyList<Uri>(), s2.featureInfo.preSelectedUris)
        Log.i(TAG, "view s1Closed=${s1.isClosed} s2Max=${s2.featureInfo.maxSelectionLimit}")
    }

    private companion object {
        const val TAG = "EmbeddedPickerTest"
    }
}
