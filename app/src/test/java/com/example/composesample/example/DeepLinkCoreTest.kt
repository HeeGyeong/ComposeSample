package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.deeplink.DeepLinkDestination
import com.example.composesample.presentation.example.component.system.deeplink.DeepLinkHandler
import com.example.composesample.presentation.example.component.system.deeplink.DeepLinkNavigator
import com.example.composesample.presentation.example.component.system.deeplink.DeepLinkSettings
import com.example.composesample.presentation.example.component.system.deeplink.ParserMode
import com.example.composesample.presentation.example.component.system.deeplink.nextBackStack
import com.example.composesample.presentation.example.component.system.deeplink.parseDeepLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * DynamicAppLinksExample '실제 수신' 탭의 핵심부 검증 — 데모 액티비티가 쓰는 파서·백스택·핸들러를 그대로 실행한다.
 * given : 커스텀 스킴·https 링크, 설정(파서·보류 칸·백스택 합성)
 * when : 해석 / 콜드(내비게이션 준비 전) 처리 / 재생성 때 같은 링크 재처리
 * then : 목적지·백스택이 기대와 같은가
 */
class DeepLinkCoreTest {

    @Test
    fun `커스텀 스킴은 첫 단어가 host 라 스킴별 분기여야 https 와 같은 목적지가 된다`() {
        val expected = DeepLinkDestination.OrderDetail("4821")
        assertEquals(expected, parseDeepLink("composesample://order/4821", ParserMode.SCHEME_AWARE))
        assertEquals(expected, parseDeepLink("https://example.com/order/4821", ParserMode.SCHEME_AWARE))
        // 함정: path 만 보면 order 가 host 로 빠져 4821 이 첫 경로가 된다
        assertEquals(
            DeepLinkDestination.Invalid("composesample://order/4821", "알 수 없는 경로: 4821"),
            parseDeepLink("composesample://order/4821", ParserMode.PATH_ONLY)
        )
        // https 는 두 방식이 같다
        assertEquals(expected, parseDeepLink("https://example.com/order/4821", ParserMode.PATH_ONLY))
    }

    @Test
    fun `밖에서 온 값은 검증하고 모르는 링크는 Invalid 로`() {
        val mode = ParserMode.SCHEME_AWARE
        assertEquals(DeepLinkDestination.Invalid("composesample://order/", "주문 번호 없음"), parseDeepLink("composesample://order/", mode))
        assertEquals(DeepLinkDestination.Invalid("composesample://order/abc", "주문 번호 형식 아님: abc"), parseDeepLink("composesample://order/abc", mode))
        assertEquals(DeepLinkDestination.Invalid("composesample://nope", "알 수 없는 경로: nope"), parseDeepLink("composesample://nope", mode))
        assertEquals(DeepLinkDestination.Profile("kim_01"), parseDeepLink("composesample://profile/kim_01", mode))
        assertTrue(parseDeepLink("composesample://profile/../../x", mode) is DeepLinkDestination.Invalid)
        assertTrue(parseDeepLink("not a uri", mode) is DeepLinkDestination.Invalid)
        assertEquals(DeepLinkDestination.Home, parseDeepLink("https://example.com/", mode))
    }

    @Test
    fun `콜드 백스택은 합성하면 홈 아래에, 웜이면 위에 쌓는다`() {
        val order = DeepLinkDestination.OrderDetail("1")
        assertEquals(listOf("home", "order/1"), nextBackStack(emptyList(), order, synthesize = true))
        assertEquals(listOf("order/1"), nextBackStack(emptyList(), order, synthesize = false))
        assertEquals(listOf("home", "order/1", "order/1"), nextBackStack(listOf("home", "order/1"), order, synthesize = true))
        assertEquals(listOf("home"), nextBackStack(emptyList(), DeepLinkDestination.Home, synthesize = true))
    }

    @Test
    fun `내비게이션 준비 전에 온 링크 — 보류 칸이 있으면 준비 뒤 열고, 없으면 버린다`() {
        for (pending in listOf(true, false)) {
            val stack = mutableListOf<String>()
            val log = mutableListOf<String>()
            val handler = DeepLinkHandler(settings = { DeepLinkSettings(pendingSlot = pending) }, log = { log += it })
            handler.handle("composesample://order/4821", "onCreate(콜드)")
            assertTrue(stack.isEmpty())
            handler.attach(navigatorOf(stack))
            if (pending) {
                assertEquals(listOf("home", "order/4821"), stack)
                assertTrue(log.last().contains("보류분 배출"))
            } else {
                assertTrue(stack.isEmpty())
                assertTrue(log.single().contains("버려짐"))
            }
        }
    }

    @Test
    fun `재생성 때 같은 링크를 다시 처리하면 같은 주문이 한 번 더 쌓인다`() {
        val stack = mutableListOf<String>()
        // 첫 실행
        DeepLinkHandler(settings = { DeepLinkSettings() }, log = {}).apply {
            handle("composesample://order/4821", "onCreate(콜드)")
            attach(navigatorOf(stack))
        }
        // 재생성: 백스택은 복원되고(rememberSaveable), 새 액티비티의 handler 가 같은 intent 를 다시 받는다
        DeepLinkHandler(settings = { DeepLinkSettings(recreateGuard = false) }, log = {}).apply {
            handle("composesample://order/4821", "onCreate(재생성)")
            attach(navigatorOf(stack))
        }
        assertEquals(listOf("home", "order/4821", "order/4821"), stack)
    }

    private fun navigatorOf(stack: MutableList<String>) = DeepLinkNavigator { destination, synthesize ->
        val next = nextBackStack(stack.toList(), destination, synthesize)
        stack.clear()
        stack.addAll(next)
    }
}
