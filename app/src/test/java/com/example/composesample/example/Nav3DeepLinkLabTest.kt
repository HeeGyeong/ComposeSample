package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.navigation.SHOP_PATTERNS
import com.example.composesample.presentation.example.component.architecture.navigation.ShopKey
import com.example.composesample.presentation.example.component.architecture.navigation.buildDeepLinkPresets
import com.example.composesample.presentation.example.component.architecture.navigation.defaultBackStack
import com.example.composesample.presentation.example.component.architecture.navigation.describeArguments
import com.example.composesample.presentation.example.component.architecture.navigation.describeNumbers
import com.example.composesample.presentation.example.component.architecture.navigation.describeShopKey
import com.example.composesample.presentation.example.component.architecture.navigation.missingNumbers
import com.example.composesample.presentation.example.component.architecture.navigation.productBackStack
import kotlinx.serialization.KSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Nav3 딥링크·결과 전달 예제의 JVM 쪽 검증 — 매칭 자체는 DeepLinkUri(=android.net.Uri) 때문에 기기에서만 돈다.
 * given : 매처 패턴 표 · 백스택 규칙 · 결과 번호 목록
 * when : 패턴 자리표시자와 키의 직렬화 필드 대조 · 백스택 합성 · 잃어버린 번호 계산
 * then : 자리표시자 이름이 전부 키 필드에 있고(오타는 매칭이 조용히 실패한다), 백스택은 Home 으로 시작해 키로 끝난다
 */
class Nav3DeepLinkLabTest {

    private val serializers: Map<String, KSerializer<*>> = mapOf(
        "Home" to ShopKey.Home.serializer(),
        "Category" to ShopKey.Category.serializer(),
        "Product" to ShopKey.Product.serializer(),
        "NewArrivals" to ShopKey.NewArrivals.serializer(),
        "Order(앱 스킴)" to ShopKey.Order.serializer(),
        "Order(웹)" to ShopKey.Order.serializer(),
        "Search" to ShopKey.Search.serializer(),
        "Promo" to ShopKey.Promo.serializer(),
        "Flag" to ShopKey.Flag.serializer(),
        "Sorted" to ShopKey.Sorted.serializer(),
        "Coupon" to ShopKey.Coupon.serializer(),
        "Bundle" to ShopKey.Bundle.serializer(),
    )

    @Test
    fun `패턴의 자리표시자는 전부 대상 키의 직렬화 필드 이름이다`() {
        val placeholder = Regex("\\{(.+?)\\}")
        SHOP_PATTERNS.filter { it.name in serializers }.forEach { p ->
            val descriptor = serializers.getValue(p.name).descriptor
            val fields = (0 until descriptor.elementsCount).map { descriptor.getElementName(it) }.toSet()
            val names = placeholder.findAll(p.pattern).map { it.groupValues[1] }.toList()
            names.forEach { name -> assertTrue("${p.name} 의 {$name} 가 필드 $fields 에 없다", name in fields) }
        }
        // 매처 이름은 겹치지 않고, ShareInbox(필터만) 를 빼면 모두 직렬화 대상이 있다
        assertEquals(SHOP_PATTERNS.size, SHOP_PATTERNS.map { it.name }.toSet().size)
        assertEquals(setOf("ShareInbox"), SHOP_PATTERNS.map { it.name }.toSet() - serializers.keys)
    }

    @Test
    fun `상품 백스택은 푸시로 오면 Home 바로 위, 링크로 오면 카테고리를 거친다`() {
        val push = ShopKey.Product("42", "push")
        assertEquals(listOf(ShopKey.Home, push), productBackStack(push))
        val link = ShopKey.Product("42")
        assertEquals(listOf(ShopKey.Home, ShopKey.Category("all"), link), productBackStack(link))
    }

    @Test
    fun `기본 백스택은 Home 위에 하나이고 Home 이면 Home 하나다`() {
        assertEquals(listOf(ShopKey.Home), defaultBackStack(ShopKey.Home))
        assertEquals(listOf(ShopKey.Home, ShopKey.Order(4821)), defaultBackStack(ShopKey.Order(4821)))
    }

    @Test
    fun `프리셋 이름은 겹치지 않고 요청은 누를 때 만든다`() {
        val presets = buildDeepLinkPresets() // build 람다는 부르지 않으므로 JVM 에서도 만들어진다
        assertEquals(18, presets.size)
        assertEquals(presets.size, presets.map { it.label }.toSet().size)
    }

    @Test
    fun `어디에도 안 보인 번호와 번호 줄이기`() {
        assertEquals(listOf(65, 66, 67, 68, 69, 70), missingNumbers(70, (1..64).toList(), emptyList()))
        assertEquals(listOf(2), missingNumbers(10, listOf(1, 3, 4, 5, 6, 7, 8, 9, 10), emptyList()))
        assertEquals(emptyList<Int>(), missingNumbers(10, listOf(1, 3, 4, 5, 6, 7, 8, 9, 10), listOf(2)))
        assertEquals("-", describeNumbers(emptyList()))
        assertEquals("1,2,3,4,5,6,…,62,63,64 (64개)", describeNumbers((1..64).toList()))
    }

    @Test
    fun `키와 인자 표시`() {
        assertEquals("Search(query=\"\", page=1)", describeShopKey(ShopKey.Search("")))
        assertEquals("Product(id=42, ref=direct)", describeShopKey(ShopKey.Product("42")))
        assertEquals("없음", describeArguments(emptyMap()))
        assertEquals("q=\"a\"|\"b\"", describeArguments(mapOf("q" to listOf("a", "b"))))
    }
}
