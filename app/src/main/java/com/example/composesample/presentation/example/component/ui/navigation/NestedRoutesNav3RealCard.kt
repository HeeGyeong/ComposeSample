package com.example.composesample.presentation.example.component.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.composesample.presentation.example.component.architecture.navigation.RealButton
import com.example.composesample.presentation.example.component.architecture.navigation.RealButtonRow
import com.example.composesample.presentation.example.component.architecture.navigation.RealCaption
import com.example.composesample.presentation.example.component.architecture.navigation.RealInfoLine
import com.example.composesample.presentation.example.component.architecture.navigation.RealNavCard
import com.example.composesample.presentation.example.component.architecture.navigation.RealNavFrame
import com.example.composesample.presentation.example.component.architecture.navigation.RealPane
import com.example.composesample.presentation.example.component.architecture.navigation.ScopeCounterViewModel
import com.example.composesample.presentation.example.component.architecture.navigation.ScopeRealTracker
import kotlinx.serialization.Serializable

/**
 * Nested Routes 예제의 실물 카드(#117) — 탭마다 따로 사는 백스택.
 * 탭마다 rememberNavBackStack + rememberDecoratedNavEntries(엔트리 저장 상태 · 엔트리 ViewModel 저장소)를 화면 최상위에서 만들고,
 * NavDisplay 에는 지금 탭의 장식된 엔트리만 넘긴다. 다른 탭의 엔트리는 NavDisplay 밖에서도 장식 상태가 남아 있어
 * 탭을 바꿨다 돌아와도 그 탭의 깊이 · 저장 상태 · ViewModel 이 그대로다(공식 문서의 'MULTIPLE BACKSTACKS' 모양).
 */
@Serializable
enum class RealTab(val title: String) {
    Home("Home"),
    Prayers("Prayers"),
    Plans("Plans"),
    User("User")
}

@Serializable
sealed interface TabRealKey : NavKey {
    /** 탭의 첫 화면 — 탭마다 다른 키라 contentKey 가 겹치지 않는다 */
    @Serializable
    data class Root(val tab: RealTab) : TabRealKey

    @Serializable
    data class Item(val tab: RealTab, val n: Int) : TabRealKey
}

internal fun tabRealLabel(key: NavKey): String = when (key) {
    is TabRealKey.Root -> key.tab.title
    is TabRealKey.Item -> "${key.tab.title}/${key.n}"
    else -> key.toString()
}

/** 화면 최상위에서 만든 실물 데모 상태 */
internal class TabsRealDemo(
    val current: RealTab,
    val onSelect: (RealTab) -> Unit,
    val stacks: Map<RealTab, NavBackStack<NavKey>>,
    val entries: Map<RealTab, List<NavEntry<NavKey>>>,
    val tracker: ScopeRealTracker
)

private val TabColor = Color(0xFF00695C)
private val TabGray = Color(0xFF546E7A)

/**
 * 화면 최상위에서 부른다. 탭 넷을 반복문이 아니라 따로 부른다 — rememberSaveable 은 호출 위치로 저장 키를 정하므로
 * 같은 위치를 반복하면 탭끼리 저장 상태가 섞일 수 있다.
 */
@Composable
internal fun rememberTabsRealDemo(): TabsRealDemo {
    var current by rememberSaveable { mutableStateOf(RealTab.Home) }
    val tracker = remember { ScopeRealTracker() }
    val homeStack = rememberNavBackStack(TabRealKey.Root(RealTab.Home))
    val prayersStack = rememberNavBackStack(TabRealKey.Root(RealTab.Prayers))
    val plansStack = rememberNavBackStack(TabRealKey.Root(RealTab.Plans))
    val userStack = rememberNavBackStack(TabRealKey.Root(RealTab.User))
    val homeEntries = rememberTabEntries(homeStack, tracker)
    val prayersEntries = rememberTabEntries(prayersStack, tracker)
    val plansEntries = rememberTabEntries(plansStack, tracker)
    val userEntries = rememberTabEntries(userStack, tracker)
    return TabsRealDemo(
        current = current,
        onSelect = { current = it },
        stacks = mapOf(
            RealTab.Home to homeStack,
            RealTab.Prayers to prayersStack,
            RealTab.Plans to plansStack,
            RealTab.User to userStack
        ),
        entries = mapOf(
            RealTab.Home to homeEntries,
            RealTab.Prayers to prayersEntries,
            RealTab.Plans to plansEntries,
            RealTab.User to userEntries
        ),
        tracker = tracker
    )
}

/** 탭 하나의 장식된 엔트리 — 데코레이터는 탭마다 따로(같은 데코레이터를 여러 백스택에 쓰지 않는다) */
@Composable
private fun rememberTabEntries(backStack: NavBackStack<NavKey>, tracker: ScopeRealTracker): List<NavEntry<NavKey>> {
    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    return rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<TabRealKey.Root> { key ->
                TabPane(tabRealLabel(key), tracker) {
                    RealButton("${key.tab.title}/1 →", TabColor) { backStack.add(TabRealKey.Item(key.tab, 1)) }
                }
            }
            entry<TabRealKey.Item> { key ->
                TabPane(tabRealLabel(key), tracker) {
                    RealButtonRow {
                        RealButton("${key.tab.title}/${key.n + 1} →", TabColor) { backStack.add(TabRealKey.Item(key.tab, key.n + 1)) }
                        RealButton("← Pop", TabGray) { pop() }
                    }
                }
            }
        }
    )
}

/** 탭 안 화면 — 엔트리 ViewModel 번호와 rememberSaveable 카운트로 '남아 있음' 을 보인다 */
@Composable
private fun TabPane(name: String, tracker: ScopeRealTracker, actions: @Composable () -> Unit) {
    val vm: ScopeCounterViewModel = viewModel { ScopeCounterViewModel(tracker, name) }
    var taps by rememberSaveable { mutableIntStateOf(0) }
    RealPane(name, TabColor) {
        RealInfoLine("ViewModel", "#${vm.id}")
        RealInfoLine("rememberSaveable", "$taps")
        RealButton("탭 상태 +1", TabColor) { taps++ }
        actions()
    }
}

@Composable
internal fun NestedRoutesRealCard(demo: TabsRealDemo) {
    val currentStack = demo.stacks.getValue(demo.current)
    RealNavCard(
        title = "탭마다 백스택 — rememberDecoratedNavEntries",
        subtitle = "위 카드들의 시뮬레이터 대신 navigation3 1.2.2 로 탭 넷의 백스택을 실제로 따로 둔다. " +
            "탭을 바꿔도 다른 탭의 깊이 · 저장 상태 · ViewModel 이 남는다."
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RealTab.entries.forEach { tab ->
                FilterChip(
                    selected = demo.current == tab,
                    onClick = { demo.onSelect(tab) },
                    label = { Text(text = "${tab.title} (${demo.stacks.getValue(tab).size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFB2DFDB))
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        RealNavFrame(height = 250.dp) {
            NavDisplay(
                entries = demo.entries.getValue(demo.current),
                onBack = { if (currentStack.size > 1) currentStack.removeAt(currentStack.lastIndex) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        RealTab.entries.forEach { tab ->
            RealInfoLine(tab.title, demo.stacks.getValue(tab).joinToString(" › ") { tabRealLabel(it) })
        }
        Text(text = "onCleared 기록 (최근이 위)", fontSize = 12.sp, color = Color(0xFF424242))
        if (demo.tracker.log.isEmpty()) {
            RealCaption("(아직 없음 — 탭 안에서 쌓고 Pop 해 보자. 탭을 바꾸는 것만으로는 오지 않는다)")
        } else {
            demo.tracker.log.forEach { RealCaption(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        RealCaption(
            "• 탭마다 rememberNavBackStack 하나 + rememberDecoratedNavEntries 하나. NavDisplay 에는 지금 탭의 엔트리만 넘긴다.\n" +
                "• 탭을 바꾸면 그 탭의 엔트리는 화면에서만 빠진다 — 백스택에서 빠진 게 아니라 Pop 처리(onPop)가 오지 않고, " +
                "돌아오면 ViewModel 번호와 rememberSaveable 값이 그대로다.\n" +
                "• 탭 안에서 Pop 하면 그 엔트리의 저장 상태와 ViewModel 이 정리된다(onCleared 기록).\n" +
                "• 지금 탭이 첫 화면보다 깊으면 시스템 뒤로 가기는 그 탭의 백스택을 먼저 줄인다. 첫 화면이면 예제를 닫는다."
        )
    }
}
