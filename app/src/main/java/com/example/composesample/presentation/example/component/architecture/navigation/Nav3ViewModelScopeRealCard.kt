package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

/**
 * Nav3 ViewModel 스코프 예제의 실물 카드(#117).
 * 같은 백스택을 ① 엔트리 데코레이터 없이 ② lifecycle-viewmodel-navigation3 의 rememberViewModelStoreNavEntryDecorator 로 그리고,
 * 각 화면이 viewModel() 로 받은 인스턴스 번호 · onCleared 기록을 보인다.
 *
 * ViewModel 은 액티비티 저장소에 만들지 않는다 — 방식마다 rememberViewModelStoreOwner()(그 방식이 컴포지션을 떠나면 정리,
 * 회전 같은 재생성은 넘김)를 부모로 쓴다. 백스택 · 장식된 엔트리도 화면 최상위에서 만들어, 카드가 스크롤로 빠져도 남는다.
 */
@Serializable
sealed interface ScopeRealKey : NavKey {
    @Serializable
    data object Home : ScopeRealKey

    @Serializable
    data class Detail(val n: Int) : ScopeRealKey
}

internal fun scopeRealLabel(key: NavKey): String = when (key) {
    ScopeRealKey.Home -> "Home"
    is ScopeRealKey.Detail -> "Detail ${key.n}"
    else -> key.toString()
}

internal enum class ScopeRealMode(val label: String) {
    NoDecorator("데코레이터 없음"),
    EntryDecorator("rememberViewModelStoreNavEntryDecorator")
}

/** ViewModel 번호와 onCleared 기록 — 화면에 보이는 상태 */
internal class ScopeRealTracker {
    private var nextId = 1
    val log = mutableStateListOf<String>()

    fun newId(): Int = nextId++

    fun onCleared(id: Int, createdFor: String) {
        log.add(0, "ViewModel #$id onCleared (처음 만든 화면: $createdFor)")
        while (log.size > MAX_LOG) log.removeAt(log.lastIndex)
    }

    companion object {
        const val MAX_LOG = 6
    }
}

/** 화면마다 하나씩 얻는(또는 함께 쓰는) 카운터 ViewModel — 진짜 androidx.lifecycle.ViewModel */
internal class ScopeCounterViewModel(
    private val tracker: ScopeRealTracker,
    val createdFor: String
) : ViewModel() {
    val id: Int = tracker.newId()
    var count by mutableIntStateOf(0)
        private set

    fun increment() {
        count++
    }

    public override fun onCleared() {
        tracker.onCleared(id, createdFor)
    }
}

/** 화면 최상위에서 만든 실물 데모 상태 */
internal class ScopeRealDemo(
    val mode: ScopeRealMode,
    val backStack: NavBackStack<NavKey>,
    val entries: List<NavEntry<NavKey>>,
    val tracker: ScopeRealTracker,
    val onModeChange: (ScopeRealMode) -> Unit
)

/**
 * 화면 최상위에서 부른다. 방식을 바꾸면 key(mode) 아래가 통째로 새로 만들어진다 — 옛 방식의 저장소(modeOwner)와
 * 데코레이터가 컴포지션을 떠나며 그 방식의 ViewModel 이 모두 정리된다(onCleared 기록). 손으로 clear() 하지 않는다.
 */
@Composable
internal fun rememberScopeRealDemo(): ScopeRealDemo {
    val tracker = remember { ScopeRealTracker() }
    var mode by rememberSaveable { mutableStateOf(ScopeRealMode.EntryDecorator) }
    return key(mode) {
        // 이 방식 범위의 저장소 — 액티비티 저장소를 쓰지 않는다. 데코레이터 없는 방식의 viewModel() 은 여기에 바로 만든다
        val modeOwner = rememberViewModelStoreOwner()
        val backStack = rememberNavBackStack(ScopeRealKey.Home)
        // key(mode) 안이라 아래 분기는 이 그룹에서 바뀌지 않는다 — 분기 안 remember 호출이 안전하다
        val decorators: List<NavEntryDecorator<NavKey>> = when (mode) {
            ScopeRealMode.NoDecorator -> listOf(rememberSaveableStateHolderNavEntryDecorator())
            ScopeRealMode.EntryDecorator -> listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                // 부모를 이 방식 범위 저장소로 — 엔트리 저장소도 방식을 바꾸거나 예제를 떠나면 함께 정리된다
                rememberViewModelStoreNavEntryDecorator(modeOwner)
            )
        }
        val sharedOwner = if (mode == ScopeRealMode.NoDecorator) modeOwner else null
        val entries = rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = decorators,
            entryProvider = entryProvider {
                entry<ScopeRealKey.Home> {
                    ScopeCounterPane("Home", ScopeHomeColor, tracker, sharedOwner) {
                        RealButton("Detail 1 →", ScopeHomeColor) { backStack.add(ScopeRealKey.Detail(1)) }
                    }
                }
                entry<ScopeRealKey.Detail> { key ->
                    ScopeCounterPane("Detail ${key.n}", ScopeDetailColor, tracker, sharedOwner) {
                        RealButtonRow {
                            RealButton("Detail ${key.n + 1} →", ScopeDetailColor) { backStack.add(ScopeRealKey.Detail(key.n + 1)) }
                            RealButton("← Pop", ScopeGray) { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
                        }
                    }
                }
            }
        )
        ScopeRealDemo(
            mode = mode,
            backStack = backStack,
            entries = entries,
            tracker = tracker,
            onModeChange = { next -> mode = next }
        )
    }
}

private val ScopeHomeColor = Color(0xFF5E35B1)
private val ScopeDetailColor = Color(0xFF00838F)
private val ScopeGray = Color(0xFF546E7A)

/** 화면 하나 — viewModel() 이 어느 저장소에서 오는지만 방식에 따라 다르다 */
@Composable
private fun ScopeCounterPane(
    name: String,
    color: Color,
    tracker: ScopeRealTracker,
    sharedOwner: ViewModelStoreOwner?,
    actions: @Composable () -> Unit
) {
    // 데코레이터가 있으면 LocalViewModelStoreOwner 가 엔트리 저장소다. 없으면 화면 범위 저장소를 함께 쓴다
    val owner = sharedOwner ?: checkNotNull(LocalViewModelStoreOwner.current) { "ViewModelStoreOwner 없음" }
    val vm: ScopeCounterViewModel = viewModel(viewModelStoreOwner = owner) { ScopeCounterViewModel(tracker, name) }
    RealPane(name, color) {
        RealInfoLine("ViewModel", "#${vm.id} (처음 만든 화면: ${vm.createdFor})")
        RealInfoLine("count", "${vm.count}")
        RealButton("+1", color) { vm.increment() }
        actions()
    }
}

@Composable
internal fun Nav3ViewModelScopeRealCard(demo: ScopeRealDemo) {
    RealNavCard(
        title = "rememberViewModelStoreNavEntryDecorator",
        subtitle = "위 세 카드가 흉내 낸 것을 lifecycle-viewmodel-navigation3 2.11.1 로 실제로 돌린다. 화면마다 viewModel() 을 부르고, " +
            "받은 인스턴스 번호와 onCleared 를 기록한다."
    ) {
        Text(text = "엔트리 데코레이터", fontSize = 12.sp, color = Color(0xFF424242))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ScopeRealMode.entries.forEach { mode ->
                FilterChip(
                    selected = demo.mode == mode,
                    onClick = { demo.onModeChange(mode) },
                    label = { Text(text = mode.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC8E6C9))
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        RealNavFrame(height = 230.dp) {
            NavDisplay(
                entries = demo.entries,
                onBack = { if (demo.backStack.size > 1) demo.backStack.removeAt(demo.backStack.lastIndex) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        RealInfoLine("백스택", demo.backStack.joinToString(" › ") { scopeRealLabel(it) })
        Text(text = "onCleared 기록 (최근이 위)", fontSize = 12.sp, color = Color(0xFF424242))
        if (demo.tracker.log.isEmpty()) {
            RealCaption("(아직 없음 — Detail 을 쌓고 Pop 해 보자)")
        } else {
            demo.tracker.log.forEach { RealCaption(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        RealCaption(
            "• 데코레이터 없음: 모든 화면의 viewModel() 이 같은 저장소 · 같은 키(클래스 이름)를 써서 인스턴스 하나를 함께 쓴다 — " +
                "Detail 에서 올린 count 가 Home 에도 보이고, Pop 해도 onCleared 가 오지 않는다(위 'Nav3 Default' 카드).\n" +
                "• rememberViewModelStoreNavEntryDecorator: 엔트리(contentKey)마다 저장소가 따로 생겨 화면마다 새 번호를 받고, " +
                "Pop 하면 그 엔트리의 저장소가 비워져 onCleared 가 기록된다(위 'Nav2 Auto-Scope' 와 같은 결과).\n" +
                "• 이 데코레이터는 SavedStateHandle 을 위해 rememberSaveableStateHolderNavEntryDecorator 와 함께 쓴다(공식 문서). " +
                "ViewModel 은 액티비티가 아니라 방식 범위 저장소(rememberViewModelStoreOwner)에 만들어, 방식을 바꾸거나 예제를 닫으면 함께 정리된다."
        )
    }
}
