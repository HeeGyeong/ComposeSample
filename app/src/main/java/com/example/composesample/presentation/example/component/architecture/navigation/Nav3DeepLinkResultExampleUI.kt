package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.composesample.presentation.MainHeader

/**
 * Navigation 3 1.2.0 의 Deep Link API · Result API 실물 예제.
 *
 * 위쪽은 NavDisplay 로 그린 미니 쇼핑 앱(rememberNavBackStack — 재생성·프로세스 종료 뒤 복원), 아래 카드에서
 * 요청을 매처 카탈로그(Nav3DeepLinkLab.kt)에 대 보고, 고른 결과의 백스택으로 미니 앱을 바꾼다.
 * 결과 전달은 AddressPicker 가 ResultEventBus 로 보낸 주소를 Checkout 이 ResultEffect / conflateAsState 로 받는다.
 */
@Composable
fun Nav3DeepLinkResultExampleUI(onBackEvent: () -> Unit) {
    val backStack = rememberNavBackStack(ShopKey.Home)
    val bus = rememberResultEventBus()
    val matchers = remember { buildShopMatchers() }
    val presets = remember { buildDeepLinkPresets() }
    val log = remember { ShopResultLog() }
    // 보낸 개수는 재생성·프로세스 종료를 넘겨 '잃어버린 결과'를 셀 수 있게 저장한다
    var sentTotal by rememberSaveable { mutableIntStateOf(0) }
    // 버스 인스턴스 이력 — 재생성·프로세스 종료 뒤 새 버스가 생기는 것을 보이려고 저장한다
    var busHistory by rememberSaveable { mutableStateOf("") }
    val busId = System.identityHashCode(bus)
    LaunchedEffect(busId) {
        if (busHistory.split(",").lastOrNull() != busId.toString()) {
            busHistory = if (busHistory.isEmpty()) busId.toString() else "$busHistory,$busId"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        MainHeader(title = "Nav3 딥링크·결과 전달", onBackIconClicked = onBackEvent)
        Text(
            text = "백스택: " + backStack.joinToString(" › ") { describeShopKey(it as ShopKey) },
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF455A64),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            ShopNavHost(
                backStack = backStack,
                bus = bus,
                log = log,
                onSend = { count, popAfter ->
                    repeat(count) { bus.sendResult(shopAddress(sentTotal + it + 1)) }
                    sentTotal += count
                    if (popAfter && backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                }
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                DeepLinkCard(presets = presets, matchers = matchers) { stack ->
                    backStack.clear()
                    backStack.addAll(stack)
                }
            }
            item { MatcherRuleCard() }
            item {
                ResultCard(
                    log = log,
                    sentTotal = sentTotal,
                    onSendNow = {
                        repeat(10) { bus.sendResult(shopAddress(sentTotal + it + 1)) }
                        sentTotal += 10
                    },
                    onReset = { log.reset(); sentTotal = 0 }
                )
            }
            item { LostResultCard(busHistory = busHistory) }
            item { SummaryCard() }
        }
    }
}

/** Checkout 이 받은 결과 — 번호로 어느 수집기가 무엇을 가져갔는지 본다 */
@Stable
private class ShopResultLog {
    var alsoConflate by mutableStateOf(false)
    val effectNumbers = mutableStateListOf<Int>()
    val stateNumbers = mutableStateListOf<Int>()

    fun reset() {
        effectNumbers.clear()
        stateNumbers.clear()
    }
}

// ==================== 미니 앱 ====================

@Composable
private fun ShopNavHost(
    backStack: NavBackStack<NavKey>,
    bus: ResultEventBus,
    log: ShopResultLog,
    onSend: (count: Int, popAfter: Boolean) -> Unit,
) {
    fun push(key: ShopKey) = backStack.add(key)
    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { pop() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // 엔트리마다 LocalResultEventBus 로 같은 버스를 준다(위에서 끌어올린 bus)
            rememberResultEventBusNavEntryDecorator(bus),
        ),
        entryProvider = entryProvider {
            entry<ShopKey.Home> {
                ShopPane("Home", "미니 쇼핑 앱의 첫 화면") {
                    OutlinedButton(onClick = { push(ShopKey.Checkout) }) { Text("결제 →", fontSize = 12.sp) }
                    OutlinedButton(onClick = { push(ShopKey.Category("shoes")) }) { Text("신발 →", fontSize = 12.sp) }
                }
            }
            entry<ShopKey.Category> { key ->
                ShopPane("Category", describeShopKey(key)) {
                    OutlinedButton(onClick = { push(ShopKey.Product("42")) }) { Text("상품 42 →", fontSize = 12.sp) }
                    BackButton(::pop)
                }
            }
            entry<ShopKey.Checkout> {
                CheckoutPane(log = log, onPick = { push(ShopKey.AddressPicker) }, onBack = ::pop)
            }
            entry<ShopKey.AddressPicker> {
                AddressPickerPane(onSend = onSend, onBack = ::pop)
            }
            // 나머지는 딥링크로만 들어오는 화면 — 키 내용만 보인다
            entry<ShopKey.Product> { key -> ShopPane("Product", describeShopKey(key)) { BackButton(::pop) } }
            entry<ShopKey.NewArrivals> { ShopPane("NewArrivals", "products/new 정확한 경로") { BackButton(::pop) } }
            entry<ShopKey.Order> { key -> ShopPane("Order", describeShopKey(key)) { BackButton(::pop) } }
            entry<ShopKey.Search> { key -> ShopPane("Search", describeShopKey(key)) { BackButton(::pop) } }
            entry<ShopKey.Promo> { ShopPane("Promo", "promo 아래 아무 경로") { BackButton(::pop) } }
            entry<ShopKey.ShareInbox> { ShopPane("ShareInbox", "공유 인텐트(URI 없음)") { BackButton(::pop) } }
            entry<ShopKey.Flag> { key -> ShopPane("Flag", describeShopKey(key)) { BackButton(::pop) } }
            entry<ShopKey.Sorted> { key -> ShopPane("Sorted", describeShopKey(key)) { BackButton(::pop) } }
            entry<ShopKey.Coupon> { key -> ShopPane("Coupon", describeShopKey(key)) { BackButton(::pop) } }
            entry<ShopKey.Bundle> { key -> ShopPane("Bundle", describeShopKey(key)) { BackButton(::pop) } }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShopPane(title: String, subtitle: String, actions: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF546E7A))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { actions() }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    OutlinedButton(onClick = onBack) { Text("← 뒤로", fontSize = 12.sp) }
}

/**
 * 결과를 받는 화면. ResultEffect 는 이벤트(받은 것 전부), conflateAsState 는 상태(마지막 값)다.
 * 둘 다 같은 키(결과 타입의 클래스 이름)의 채널 하나를 수집한다.
 */
@Composable
private fun CheckoutPane(log: ShopResultLog, onPick: () -> Unit, onBack: () -> Unit) {
    ResultEffect<ShopAddress> { address -> log.effectNumbers.add(address.no) }
    var latestByState: ShopAddress? = null
    if (log.alsoConflate) {
        val latest by LocalResultEventBus.current.conflateAsState<ShopAddress?>(null)
        latestByState = latest
        LaunchedEffect(latest) { latest?.let { log.stateNumbers.add(it.no) } }
    }
    ShopPane(
        title = "Checkout",
        subtitle = "ResultEffect 로 받은 번호: ${describeNumbers(log.effectNumbers)}" +
            if (log.alsoConflate) "\nconflateAsState 마지막: ${latestByState?.label ?: "-"}" else ""
    ) {
        OutlinedButton(onClick = onPick) { Text("주소 고르기 →", fontSize = 12.sp) }
        BackButton(onBack)
    }
}

@Composable
private fun AddressPickerPane(onSend: (count: Int, popAfter: Boolean) -> Unit, onBack: () -> Unit) {
    ShopPane("AddressPicker", "LocalResultEventBus.current.sendResult(주소) 로 보낸다") {
        OutlinedButton(onClick = { onSend(1, true) }) { Text("1개 보내고 돌아가기", fontSize = 12.sp) }
        OutlinedButton(onClick = { onSend(10, true) }) { Text("10개 보내고 돌아가기", fontSize = 12.sp) }
        OutlinedButton(onClick = { onSend(70, true) }) { Text("70개 보내고 돌아가기", fontSize = 12.sp) }
        OutlinedButton(onClick = { onSend(3, false) }) { Text("3개 보내고 머무르기", fontSize = 12.sp) }
        BackButton(onBack)
    }
}

// ==================== 카드 ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeepLinkCard(
    presets: List<DeepLinkPreset>,
    matchers: List<ShopMatcher>,
    onOpen: (List<ShopKey>) -> Unit,
) {
    var report by remember { mutableStateOf<DeepLinkReport?>(null) }
    var selected by rememberSaveable { mutableStateOf("") }
    var custom by rememberSaveable { mutableStateOf("https://$SHOP_HOST/c/shoes") }

    DeepLinkSectionCard(title = "1. 딥링크 매칭 — 요청 하나를 모든 매처에") {
        DeepLinkBody(
            "프리셋을 누르면 같은 DeepLinkRequest 를 아래 매처 13개에 대고(match), 맞은 것 중 MatchResult 비교로 가장 " +
                "구체적인 하나를 고릅니다. withBackStack 이 만든 백스택으로 위 미니 앱을 바꿀 수 있습니다."
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            presets.forEach { p ->
                FilterChip(
                    selected = selected == p.label,
                    onClick = {
                        selected = p.label
                        report = resolveDeepLink(p.source, p.build(), matchers)
                    },
                    label = { Text(p.label, fontSize = 11.sp) }
                )
            }
        }
        OutlinedTextField(
            value = custom,
            onValueChange = { custom = it },
            label = { Text("직접 입력한 URI", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                selected = ""
                report = runCatching { resolveDeepLink(custom, DeepLinkRequest(custom), matchers) }.getOrNull()
            }) { Text("입력한 URI 매칭", fontSize = 12.sp) }
            val stack = report?.winnerHit?.backStack
            OutlinedButton(onClick = { stack?.let(onOpen) }, enabled = stack != null) {
                Text("이 백스택으로 열기", fontSize = 12.sp)
            }
        }
        DeepLinkMono(report?.lines()?.joinToString("\n") ?: "프리셋을 고르세요")
        DeepLinkNote(
            "연 백스택은 시스템 뒤로가기로 하나씩 줄고(NavDisplay 의 onBack), Home 하나만 남으면 뒤로가기는 이 예제(액티비티)를 닫는다."
        )
    }
}

@Composable
private fun MatcherRuleCard() {
    DeepLinkSectionCard(title = "2. 매처 목록과 고르는 규칙") {
        DeepLinkMono(SHOP_PATTERNS.joinToString("\n") { "${it.name}: ${it.pattern}" })
        DeepLinkBody(
            "• 고르는 순서(UriMatchResult.compareTo): 정확한 경로 > 경로 인자가 많은 쪽 > 인자가 있는 쪽 > 인자 총수, 동률이면 목록 앞\n" +
                "• 스킴 없는 패턴은 http/https 만, authority 는 대소문자만 무시하고 정확히 같아야 한다\n" +
                "• 인자는 키의 직렬화로 채운다 — 형 변환 실패·필수 인자 누락은 null(매칭 아님)\n" +
                "• 빈 쿼리 값: String 은 \"\" 그대로, 그 밖의 형은 '값 없음'이라 기본값(page=1) — 기본값 없는 Int? 는 null 이 아니라 누락으로 실패\n" +
                "• Boolean 은 toBooleanStrict(true/false 만 — True·1 거부), enum 은 이름 대소문자까지 같아야 한다"
        )
        DeepLinkNote(
            "여러 매처 중 고르는 헬퍼는 없다 — MatchResult 가 Comparable 이라 앱이 맞은 것 중 가장 큰 것을 고른다(withBackStack 결과도 " +
                "감싼 원래 결과로 비교). ⚠ 지원하지 않는 키 모양(Map 필드)은 null 이 아니라 match() 가 IllegalArgumentException 을 던진다 — " +
                "UriDeepLinkMatcher 는 DeepLinkDecoderException 만 null 로 바꾼다. 그래서 이 예제는 매처마다 try 로 감싼다."
        )
    }
}

@Composable
private fun ResultCard(log: ShopResultLog, sentTotal: Int, onSendNow: () -> Unit, onReset: () -> Unit) {
    DeepLinkSectionCard(title = "3. 결과 전달 — ResultEventBus") {
        DeepLinkBody(
            "미니 앱에서 Home → 결제 → 주소 고르기로 가서 주소를 보냅니다. Checkout 은 ResultEffect 로 받고, 스위치를 켜면 " +
                "같은 키(결과 타입의 클래스 이름)를 conflateAsState 로도 받습니다. 키마다 채널 하나라 결과는 방송이 아니라 분배됩니다."
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = log.alsoConflate, role = Role.Switch) { log.alsoConflate = it },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("같은 키를 conflateAsState 로도 받기", fontSize = 13.sp, modifier = Modifier.weight(1f))
            Switch(checked = log.alsoConflate, onCheckedChange = null)
        }
        val missing = missingNumbers(sentTotal, log.effectNumbers, log.stateNumbers)
        DeepLinkMono(
            "보낸 누적 $sentTotal (rememberSaveable)\n" +
                "ResultEffect 가 받음 ${log.effectNumbers.size}: ${describeNumbers(log.effectNumbers)}\n" +
                "conflateAsState 가 보인 값 ${log.stateNumbers.size}: ${describeNumbers(log.stateNumbers)}\n" +
                "어디에도 안 보인 번호 ${missing.size}: ${describeNumbers(missing)}"
        )
        DeepLinkNote(
            "실측(Pixel 4 API 30 에뮬): Checkout 이 안 보일 때 보낸 10개는 채널 버퍼에 쌓였다가, Checkout 이 다시 그려지면 먼저 붙은 " +
                "ResultEffect 가 한꺼번에 가져갔다 — 같은 키의 conflateAsState 는 0개. Checkout 이 보일 때 '지금 10개 보내기'로 두 수집기가 " +
                "모두 기다리는 중에 보내면 나뉘었다(ResultEffect 9 · conflateAsState #2). conflateAsState 는 State 라 같은 값(#2)이 다시 오면 " +
                "가져가고도 바뀐 줄 모른다. 같은 키는 한 곳에서만 받는다."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Checkout 이 화면에 있을 때 누르면 두 수집기가 모두 기다리는 중에 결과가 도착한다
            OutlinedButton(onClick = onSendNow) { Text("지금 10개 보내기", fontSize = 12.sp) }
            OutlinedButton(onClick = onReset) { Text("기록 지우기", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun LostResultCard(busHistory: String) {
    DeepLinkSectionCard(title = "4. 결과가 사라지는 경우") {
        DeepLinkMono("ResultEventBus 이력 " + busHistory.split(",").joinToString(" → ") { "#$it" })
        DeepLinkBody(
            "• 버퍼 64: Checkout 이 안 보일 때 70개를 보내면 64개만 도착하고 65~70 은 사라진다 — 채널(BUFFERED·SUSPEND)이 가득 차면 " +
                "sendResult 의 trySend 는 기다리지 않고 실패한다(예외·로그 없음)\n" +
                "• 재생성(다크 모드 전환)·프로세스 종료(am kill → 최근 앱): 백스택은 rememberNavBackStack 으로 Home › Checkout › " +
                "AddressPicker 그대로 돌아오지만 버스는 remember 라 새 인스턴스 — 'AddressPicker 에서 3개 보내고 머무르기' 뒤 돌아가면 0개 도착\n" +
                "• KDoc 그대로 'not saved across configuration changes or process death' — 꼭 전달돼야 하는 값은 키 인자나 저장되는 상태로"
        )
    }
}

@Composable
private fun SummaryCard() {
    DeepLinkSectionCard(title = "5. 정리") {
        DeepLinkBody(
            "• 딥링크: UriDeepLinkMatcher(직렬화 키) · StaticKeyDeepLinkMatcher(필터만) · DeepLinkRequest(uri 문자열·Intent) — 고르기는 앱이 compareTo 로\n" +
                "• withBackStack 으로 합성 백스택을 매처 옆에 둔다(조건부도 가능 — ref=push 면 Category 생략)\n" +
                "• 스킴 없는 패턴은 http/https 만 · 커스텀 스킴은 첫 부분이 authority(composesample://order/{no} 의 order)\n" +
                "• 결과: 키마다 채널 하나 — 한 곳에서만 받고, 버퍼 64 를 넘기지 말고, 재생성·프로세스 종료에는 기대지 않는다"
        )
    }
}

// ==================== 공통 조각 ====================

@Composable
private fun DeepLinkSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun DeepLinkBody(text: String) {
    Text(text = text, fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF37474F))
}

@Composable
private fun DeepLinkNote(text: String) {
    Text(text = text, fontSize = 11.sp, lineHeight = 16.sp, color = Color(0xFF607D8B))
}

@Composable
private fun DeepLinkMono(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECEFF1), RoundedCornerShape(8.dp))
            .padding(10.dp)
    )
}
