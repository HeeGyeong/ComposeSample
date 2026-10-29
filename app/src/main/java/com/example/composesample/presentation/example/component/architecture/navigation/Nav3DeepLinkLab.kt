package com.example.composesample.presentation.example.component.architecture.navigation

import android.content.Intent
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatchResult
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.StaticKeyDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.UriMatchResult
import androidx.navigation3.runtime.deeplink.WrappedMatchResult
import androidx.navigation3.runtime.deeplink.actionFilter
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.deeplink.withBackStack
import kotlinx.serialization.Serializable

/**
 * Nav3 딥링크·결과 전달 예제의 매처 카탈로그·요청 프리셋·판정 — 화면과 단위 테스트가 함께 쓴다.
 *
 * 딥링크는 navigation3-runtime 1.2.0 의 `androidx.navigation3.runtime.deeplink` 로만 처리한다.
 * Android 에서 DeepLinkUri 는 android.net.Uri 의 typealias 라, 매처를 만드는 [buildShopMatchers] 와
 * [resolveDeepLink] 는 기기에서만 돈다(JVM 단위 테스트는 패턴 표·백스택 규칙·문자열 정리만 본다).
 */

// ==================== 백스택 키 ====================

/**
 * 미니 쇼핑 앱의 화면 키. UriDeepLinkMatcher 가 직렬화로 키를 만들고, rememberNavBackStack 이
 * 재생성·프로세스 종료 뒤 복원하므로 전부 @Serializable + NavKey 다.
 */
@Serializable
sealed interface ShopKey : NavKey {
    @Serializable
    data object Home : ShopKey

    @Serializable
    data class Category(val name: String) : ShopKey

    /** ref 는 쿼리에 없으면 기본값 — 경로 인자 id 는 필수 */
    @Serializable
    data class Product(val id: String, val ref: String = "direct") : ShopKey

    @Serializable
    data object NewArrivals : ShopKey

    @Serializable
    data class Order(val no: Int) : ShopKey

    @Serializable
    data class Search(val query: String, val page: Int = 1) : ShopKey

    @Serializable
    data object Promo : ShopKey

    @Serializable
    data object ShareInbox : ShopKey

    // 키 모양 실험 — 디코더 규칙을 보이려고만 둔다
    @Serializable
    data class Flag(val on: Boolean) : ShopKey

    @Serializable
    data class Sorted(val sort: ShopSort) : ShopKey

    /** 기본값 없는 nullable — 값이 없으면 null 이 아니라 '필수 인자 누락'으로 매칭 실패 */
    @Serializable
    data class Coupon(val code: String, val amount: Int?) : ShopKey

    /** Map 필드 — 디코더가 DeepLinkDecoderException 이 아닌 IllegalArgumentException 을 던진다 */
    @Serializable
    data class Bundle(val id: String, val tags: Map<String, String> = emptyMap()) : ShopKey

    // 결과 전달 실험 화면
    @Serializable
    data object Checkout : ShopKey

    @Serializable
    data object AddressPicker : ShopKey
}

@Serializable
enum class ShopSort { PRICE, NEW }

/** 화면·로그에 쓰는 짧은 이름 */
internal fun describeShopKey(key: ShopKey): String = when (key) {
    ShopKey.Home -> "Home"
    is ShopKey.Category -> "Category(${key.name})"
    is ShopKey.Product -> "Product(id=${key.id}, ref=${key.ref})"
    ShopKey.NewArrivals -> "NewArrivals"
    is ShopKey.Order -> "Order(no=${key.no})"
    is ShopKey.Search -> "Search(query=\"${key.query}\", page=${key.page})"
    ShopKey.Promo -> "Promo"
    ShopKey.ShareInbox -> "ShareInbox"
    is ShopKey.Flag -> "Flag(on=${key.on})"
    is ShopKey.Sorted -> "Sorted(sort=${key.sort})"
    is ShopKey.Coupon -> "Coupon(code=${key.code}, amount=${key.amount})"
    is ShopKey.Bundle -> "Bundle(id=${key.id}, tags=${key.tags})"
    ShopKey.Checkout -> "Checkout"
    ShopKey.AddressPicker -> "AddressPicker"
}

// ==================== 백스택 규칙 (withBackStack 이 부른다) ====================

/** 푸시 알림(ref=push)으로 들어오면 Home 바로 위, 그 밖에는 카테고리를 거쳐 온 것처럼 쌓는다 — 조건부 백스택 */
internal fun productBackStack(product: ShopKey.Product): List<ShopKey> =
    if (product.ref == "push") {
        listOf(ShopKey.Home, product)
    } else {
        listOf(ShopKey.Home, ShopKey.Category("all"), product)
    }

/** 나머지는 Home 위에 하나 — Home 자체면 Home 하나 */
internal fun defaultBackStack(key: ShopKey): List<ShopKey> =
    if (key == ShopKey.Home) listOf(ShopKey.Home) else listOf(ShopKey.Home, key)

// ==================== 매처 카탈로그 ====================

/** 매처 이름·패턴(화면 표시용 문자열) — 순서가 동률일 때의 우선순위다 */
internal data class ShopPattern(val name: String, val pattern: String)

internal const val SHOP_HOST = "shop.example.com"

internal val SHOP_PATTERNS: List<ShopPattern> = listOf(
    ShopPattern("Home", "https://$SHOP_HOST/home"),
    ShopPattern("Category", "https://$SHOP_HOST/c/{name}"),
    ShopPattern("Product", "https://$SHOP_HOST/products/{id}?ref={ref}"),
    ShopPattern("NewArrivals", "https://$SHOP_HOST/products/new"),
    ShopPattern("Order(앱 스킴)", "composesample://order/{no}"),
    ShopPattern("Order(웹)", "https://$SHOP_HOST/orders/{no}"),
    ShopPattern("Search", "https://$SHOP_HOST/search?q={query}&page={page}"),
    ShopPattern("Promo", "$SHOP_HOST/promo/.*"),
    ShopPattern("ShareInbox", "action=SEND · mimeType=text/plain (URI 없음)"),
    ShopPattern("Flag", "https://$SHOP_HOST/flags/{on}"),
    ShopPattern("Sorted", "https://$SHOP_HOST/sort/{sort}"),
    ShopPattern("Coupon", "https://$SHOP_HOST/coupon/{code}?amount={amount}"),
    ShopPattern("Bundle", "https://$SHOP_HOST/bundle/{id}"),
)

internal class ShopMatcher(
    val name: String,
    val pattern: String,
    val matcher: DeepLinkMatcher<ShopKey, DeepLinkMatcher.MatchResult<ShopKey>>,
)

private fun pattern(name: String): String = SHOP_PATTERNS.first { it.name == name }.pattern

/** 카탈로그를 만든다 — DeepLinkUri(=android.net.Uri) 를 파싱하므로 기기에서만 */
internal fun buildShopMatchers(): List<ShopMatcher> {
    fun uri(name: String) = DeepLinkUri(pattern(name))
    val list = listOf(
        "Home" to UriDeepLinkMatcher(uri("Home"), ShopKey.Home.serializer())
            .withDefaultBackStack(),
        "Category" to UriDeepLinkMatcher(uri("Category"), ShopKey.Category.serializer())
            .withDefaultBackStack(),
        "Product" to UriDeepLinkMatcher(uri("Product"), ShopKey.Product.serializer())
            .withBackStack<ShopKey.Product, ShopKey> { result -> productBackStack(result.key) },
        "NewArrivals" to UriDeepLinkMatcher(uri("NewArrivals"), ShopKey.NewArrivals.serializer())
            .withDefaultBackStack(),
        "Order(앱 스킴)" to UriDeepLinkMatcher(uri("Order(앱 스킴)"), ShopKey.Order.serializer())
            .withDefaultBackStack(),
        "Order(웹)" to UriDeepLinkMatcher(uri("Order(웹)"), ShopKey.Order.serializer())
            .withDefaultBackStack(),
        "Search" to UriDeepLinkMatcher(uri("Search"), ShopKey.Search.serializer())
            .withDefaultBackStack(),
        "Promo" to UriDeepLinkMatcher(uri("Promo"), ShopKey.Promo.serializer())
            .withDefaultBackStack(),
        // URI 가 없는 공유 인텐트 — 필터만 보고 고정 키를 돌려준다
        "ShareInbox" to StaticKeyDeepLinkMatcher<ShopKey>(
            key = ShopKey.ShareInbox,
            filters = listOf(
                DeepLinkMatcher.actionFilter(Intent.ACTION_SEND),
                DeepLinkMatcher.mimeTypeFilter("text/plain"),
            ),
        ).withDefaultBackStack(),
        "Flag" to UriDeepLinkMatcher(uri("Flag"), ShopKey.Flag.serializer()).withDefaultBackStack(),
        "Sorted" to UriDeepLinkMatcher(uri("Sorted"), ShopKey.Sorted.serializer()).withDefaultBackStack(),
        "Coupon" to UriDeepLinkMatcher(uri("Coupon"), ShopKey.Coupon.serializer()).withDefaultBackStack(),
        "Bundle" to UriDeepLinkMatcher(uri("Bundle"), ShopKey.Bundle.serializer()).withDefaultBackStack(),
    )
    return list.map { (name, matcher) -> ShopMatcher(name, pattern(name), matcher) }
}

// 키 타입을 ShopKey 로 고정한 비제네릭 확장 — 매처는 out 공변이라 UriDeepLinkMatcher<ShopKey.X> 도 받는다
// (제네릭으로 두면 debug 의 HotSwan 이 'GENERIC_DECLARATION' 으로 건너뛴다)
private fun DeepLinkMatcher<ShopKey, DeepLinkMatcher.MatchResult<ShopKey>>.withDefaultBackStack() =
    withBackStack<ShopKey, ShopKey> { result -> defaultBackStack(result.key) }

// ==================== 요청 프리셋 ====================

/** 화면의 프리셋 칩 하나 — 문자열 URI 또는 Intent 로 요청을 만든다 */
internal class DeepLinkPreset(
    val label: String,
    val source: String,
    val build: () -> DeepLinkRequest,
)

internal fun uriPreset(label: String, uri: String) =
    DeepLinkPreset(label, uri) { DeepLinkRequest(uri) }

internal fun buildDeepLinkPresets(): List<DeepLinkPreset> = listOf(
    uriPreset("푸시로 상품", "https://$SHOP_HOST/products/42?ref=push"),
    uriPreset("링크로 상품", "https://$SHOP_HOST/products/42"),
    uriPreset("products/new", "https://$SHOP_HOST/products/new"),
    uriPreset("앱 스킴 주문", "composesample://order/4821"),
    uriPreset("스킴·host 엇갈림", "composesample://app/order/4821"),
    uriPreset("주문번호 abc", "https://$SHOP_HOST/orders/abc"),
    uriPreset("빈 검색", "https://$SHOP_HOST/search?q=&page="),
    uriPreset("한글 검색", "https://$SHOP_HOST/search?q=%ED%95%9C%EA%B8%80&page=2"),
    uriPreset("http·대문자 host", "http://SHOP.example.com/promo/fall/sale"),
    uriPreset("ftp 스킴", "ftp://$SHOP_HOST/promo/x"),
    DeepLinkPreset("공유 Intent", "Intent(ACTION_SEND, type=text/plain, EXTRA_TEXT)") {
        DeepLinkRequest(
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "같이 볼래요?")
        )
    },
    DeepLinkPreset("보기 Intent", "Intent(ACTION_VIEW, https://$SHOP_HOST/products/7)") {
        DeepLinkRequest(Intent(Intent.ACTION_VIEW, "https://$SHOP_HOST/products/7".toUri()))
    },
    uriPreset("Boolean True", "https://$SHOP_HOST/flags/True"),
    uriPreset("Boolean true", "https://$SHOP_HOST/flags/true"),
    uriPreset("enum price", "https://$SHOP_HOST/sort/price"),
    uriPreset("enum PRICE", "https://$SHOP_HOST/sort/PRICE"),
    uriPreset("Int? 없음", "https://$SHOP_HOST/coupon/A1"),
    uriPreset("Map 필드", "https://$SHOP_HOST/bundle/9"),
)

// ==================== 판정 ====================

/** 매처 하나의 결과 */
internal sealed interface MatcherOutcome {
    data class Hit(
        val key: ShopKey,
        val arguments: Map<String, List<String>>,
        val backStack: List<ShopKey>,
        val result: DeepLinkMatcher.MatchResult<ShopKey>,
    ) : MatcherOutcome

    data object Miss : MatcherOutcome

    /** match() 가 null 대신 던진 예외 — 지원하지 않는 키 모양 */
    data class Threw(val error: Throwable) : MatcherOutcome
}

internal data class DeepLinkReport(
    val source: String,
    val request: String,
    val rows: List<Pair<String, MatcherOutcome>>,
    val winner: String?,
    val winnerHit: MatcherOutcome.Hit?,
)

/** 모든 매처에 같은 요청을 대고, 맞은 것 중 MatchResult.compareTo 로 가장 큰 것을 고른다(동률이면 목록 앞) */
internal fun resolveDeepLink(source: String, request: DeepLinkRequest, matchers: List<ShopMatcher>): DeepLinkReport {
    val rows = matchers.map { m ->
        val outcome = try {
            val result = m.matcher.match(request)
            if (result == null) {
                MatcherOutcome.Miss
            } else {
                MatcherOutcome.Hit(
                    key = result.key,
                    arguments = argumentsOf(result),
                    backStack = (result as? BackStackMatchResult<*, *>)?.backStack?.filterIsInstance<ShopKey>()
                        ?: listOf(result.key),
                    result = result,
                )
            }
        } catch (e: Exception) {
            MatcherOutcome.Threw(e)
        }
        m.name to outcome
    }
    var winner: Pair<String, MatcherOutcome.Hit>? = null
    rows.forEach { (name, outcome) ->
        if (outcome is MatcherOutcome.Hit) {
            val current = winner
            if (current == null || current.second.result < outcome.result) winner = name to outcome
        }
    }
    return DeepLinkReport(source, request.toString(), rows, winner?.first, winner?.second)
}

private fun argumentsOf(result: DeepLinkMatcher.MatchResult<ShopKey>): Map<String, List<String>> {
    val inner: DeepLinkMatcher.MatchResult<*> = if (result is WrappedMatchResult<*>) result.matchResult else result
    return (inner as? UriMatchResult<*>)?.arguments.orEmpty()
}

/** 보고서를 줄로 — 화면과 측정 로그가 같은 문장을 쓴다 */
internal fun DeepLinkReport.lines(): List<String> = buildList {
    add("요청: $source")
    rows.forEach { (name, outcome) ->
        when (outcome) {
            is MatcherOutcome.Hit -> add("✓ $name → ${describeShopKey(outcome.key)} · 인자 ${describeArguments(outcome.arguments)}")
            is MatcherOutcome.Threw -> add("⚠ $name → 예외 ${outcome.error.javaClass.simpleName}: ${outcome.error.message?.take(70)}")
            MatcherOutcome.Miss -> Unit
        }
    }
    val misses = rows.count { it.second == MatcherOutcome.Miss }
    add(if (misses == rows.size) "✗ 매처 ${misses}개 모두 null" else "✗ 나머지 ${misses}개는 null")
    val hit = winnerHit
    if (hit == null) {
        add("결과: 맞는 매처 없음")
    } else {
        add("선택: $winner (맞은 ${rows.count { it.second is MatcherOutcome.Hit }}개 중)")
        add("백스택: ${hit.backStack.joinToString(" › ") { describeShopKey(it) }}")
    }
}

internal fun describeArguments(arguments: Map<String, List<String>>): String =
    if (arguments.isEmpty()) "없음" else arguments.entries.joinToString(", ") { (k, v) -> "$k=${v.joinToString("|") { "\"$it\"" }}" }

// ==================== 결과 전달 실험 ====================

/** AddressPicker → Checkout 으로 돌려주는 결과. 번호로 '어느 것이 어디로 갔나'를 본다 */
internal data class ShopAddress(val no: Int, val label: String)

internal fun shopAddress(no: Int): ShopAddress = ShopAddress(no, "주소 #$no")

/** 보낸 1..sent 중 받은 쪽 어디에도 없는 번호 — 잃어버린 결과 */
internal fun missingNumbers(sent: Int, vararg received: List<Int>): List<Int> {
    val got = received.flatMap { it }.toSet()
    return (1..sent).filterNot { it in got }
}

/** 번호 목록을 짧게 — 12개를 넘으면 앞 6 … 뒤 3 */
internal fun describeNumbers(numbers: List<Int>): String = when {
    numbers.isEmpty() -> "-"
    numbers.size <= 12 -> numbers.joinToString(",")
    else -> numbers.take(6).joinToString(",") + ",…," + numbers.takeLast(3).joinToString(",") + " (${numbers.size}개)"
}
