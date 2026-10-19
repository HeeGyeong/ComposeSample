package com.example.composesample.presentation.example.component.system.deeplink

import java.net.URI

/**
 * 딥링크 수신의 순수 Kotlin 부분 — 안드로이드 클래스 없이 JVM 단위 테스트(DeepLinkCoreTest)로 검증한다.
 *
 * 출처: KMP Bits "Box, Box: Deep Links in Kotlin Multiplatform"(Android Weekly #749) 의 Android 수신 절반.
 * 플랫폼(액티비티)은 문자열만 받아 넘기고, 문자열 → 목적지 해석·백스택 결정·"내비게이션 준비 전" 보류는 여기서 한다.
 */

/** 이 예제가 받는 스킴 — singleTop 액티비티 / standard 액티비티 */
const val DEEP_LINK_SCHEME = "composesample"
const val DEEP_LINK_SCHEME_STANDARD = "composesample-standard"

sealed interface DeepLinkDestination {
    data object Home : DeepLinkDestination
    data class OrderDetail(val orderId: String) : DeepLinkDestination
    data class Profile(val userId: String) : DeepLinkDestination
    /** 해석할 수 없는 링크 — 추측하거나 죽지 말고 명시적으로 '잘못된 링크' 로 보낸다 */
    data class Invalid(val raw: String, val reason: String) : DeepLinkDestination
}

enum class ParserMode(val label: String) {
    SCHEME_AWARE("스킴별 분기 (host 포함)"),
    PATH_ONLY("https 처럼 path 만 (함정)")
}

private val ORDER_ID = Regex("\\d{1,9}")
private val USER_ID = Regex("[a-z0-9_]{1,20}")

/**
 * 원문의 함정: 커스텀 스킴 `composesample://order/4821` 은 `order` 가 **host**, path 는 `/4821` 이다.
 * https 링크(`https://example.com/order/4821`)처럼 path 만 보면 첫 단어가 빠져 Unknown 이 된다.
 * java.net.URI 와 android.net.Uri 가 같은 결과를 낸다(예제 화면이 기록마다 android.net.Uri 값을 함께 찍는다).
 */
fun parseDeepLink(raw: String, mode: ParserMode): DeepLinkDestination {
    val uri = runCatching { URI(raw) }.getOrNull() ?: return DeepLinkDestination.Invalid(raw, "URI 해석 실패")
    val scheme = uri.scheme?.lowercase() ?: return DeepLinkDestination.Invalid(raw, "스킴 없음")
    val path = uri.rawPath.orEmpty().split('/').filter { it.isNotEmpty() }
    val segments = when (mode) {
        ParserMode.PATH_ONLY -> path
        ParserMode.SCHEME_AWARE -> if (scheme == "http" || scheme == "https") path else listOfNotNull(uri.host) + path
    }
    return when (val first = segments.firstOrNull()) {
        null -> DeepLinkDestination.Home
        "order" -> {
            val id = segments.getOrNull(1) ?: return DeepLinkDestination.Invalid(raw, "주문 번호 없음")
            // 밖에서 온 값이다 — 형식을 검증한 뒤에만 화면으로 넘긴다
            if (id.matches(ORDER_ID)) DeepLinkDestination.OrderDetail(id) else DeepLinkDestination.Invalid(raw, "주문 번호 형식 아님: $id")
        }
        "profile" -> {
            val id = segments.getOrNull(1) ?: return DeepLinkDestination.Invalid(raw, "사용자 id 없음")
            if (id.matches(USER_ID)) DeepLinkDestination.Profile(id) else DeepLinkDestination.Invalid(raw, "사용자 id 형식 아님: $id")
        }
        else -> DeepLinkDestination.Invalid(raw, "알 수 없는 경로: $first")
    }
}

/** 백스택 항목(문자열로 두어 rememberSaveable 이 그대로 저장한다) */
fun routeOf(destination: DeepLinkDestination): String = when (destination) {
    DeepLinkDestination.Home -> "home"
    is DeepLinkDestination.OrderDetail -> "order/${destination.orderId}"
    is DeepLinkDestination.Profile -> "profile/${destination.userId}"
    is DeepLinkDestination.Invalid -> "invalid/${destination.reason}"
}

/**
 * 링크로 들어왔을 때의 백스택. 콜드 스타트면 뒤에 아무것도 없으므로, 합성하면 [홈, 목적지] → 뒤로 = 홈,
 * 합성하지 않으면 [목적지] → 뒤로 = 앱 종료. 이미 화면이 있으면(웜) 목적지만 위에 쌓는다.
 */
fun nextBackStack(current: List<String>, destination: DeepLinkDestination, synthesize: Boolean): List<String> {
    val route = routeOf(destination)
    if (current.isNotEmpty()) return current + route
    return if (destination == DeepLinkDestination.Home || !synthesize) listOf(route) else listOf("home", route)
}

data class DeepLinkSettings(
    val parser: ParserMode = ParserMode.SCHEME_AWARE,
    /** true = savedInstanceState 가 null 일 때만 onCreate 에서 링크를 처리 */
    val recreateGuard: Boolean = true,
    /** true = 내비게이션이 준비되기 전에 온 링크를 보류했다가 준비되면 배출 */
    val pendingSlot: Boolean = true,
    val synthesizeBackStack: Boolean = true
)

/** 백스택을 쥔 쪽(화면). 첫 컴포지션 뒤에야 생긴다 — rememberNavController 처럼 */
fun interface DeepLinkNavigator {
    fun open(destination: DeepLinkDestination, synthesize: Boolean)
}

/**
 * 액티비티가 들고 있는 단일 진입점. onCreate(콜드)와 onNewIntent(웜)가 둘 다 [handle] 로 들어온다.
 * onCreate 는 setContent 보다 먼저라 그 순간 [DeepLinkNavigator] 가 없다 — 보류 칸이 없으면 링크는 그대로 사라진다.
 */
class DeepLinkHandler(
    private val settings: () -> DeepLinkSettings,
    private val log: (String) -> Unit
) {
    private var navigator: DeepLinkNavigator? = null
    private var pending: Pair<DeepLinkDestination, String>? = null

    fun handle(raw: String, source: String) {
        val current = settings()
        val destination = parseDeepLink(raw, current.parser)
        val nav = navigator
        when {
            nav != null -> go(nav, destination, source, current)
            current.pendingSlot -> {
                pending = destination to source
                log("$source · $raw → ${describe(destination)} · 내비게이션 준비 전 → 보류")
            }
            else -> log("$source · $raw → ${describe(destination)} · 내비게이션 준비 전 → 버려짐(보류 칸 없음)")
        }
    }

    /** 화면이 첫 컴포지션을 마치면 부른다 — 보류분이 있으면 지금 연다 */
    fun attach(nav: DeepLinkNavigator) {
        navigator = nav
        val waiting = pending ?: return
        pending = null
        go(nav, waiting.first, "${waiting.second} → 보류분 배출", settings())
    }

    fun detach() {
        navigator = null
    }

    private fun go(nav: DeepLinkNavigator, destination: DeepLinkDestination, source: String, current: DeepLinkSettings) {
        nav.open(destination, current.synthesizeBackStack)
        log("$source → ${describe(destination)} · 열기")
    }

    companion object {
        fun describe(destination: DeepLinkDestination): String = when (destination) {
            DeepLinkDestination.Home -> "홈"
            is DeepLinkDestination.OrderDetail -> "주문 ${destination.orderId}"
            is DeepLinkDestination.Profile -> "프로필 ${destination.userId}"
            is DeepLinkDestination.Invalid -> "잘못된 링크(${destination.reason})"
        }
    }
}

/** 예제 화면의 시험 링크 */
val DEEP_LINK_SAMPLES: List<String> = listOf(
    "$DEEP_LINK_SCHEME://order/4821",
    "$DEEP_LINK_SCHEME://profile/kim_01",
    "$DEEP_LINK_SCHEME://order/",
    "$DEEP_LINK_SCHEME://order/abc",
    "$DEEP_LINK_SCHEME://nope"
)
