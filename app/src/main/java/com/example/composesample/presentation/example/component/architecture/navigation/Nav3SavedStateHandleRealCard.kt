package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * Nav3 SavedStateHandle 예제의 실물 카드(#117).
 * 키에는 id 만 담고(@Serializable), 화면의 ViewModel 이 id 로 저장소에서 객체를 다시 찾는다. 화면이 바꾸는 값은
 * 엔트리 ViewModel 의 SavedStateHandle 에 둔다 — rememberNavBackStack(키 직렬화) + 엔트리 저장 상태가 재생성 · 프로세스 종료를 넘긴다.
 */
@Serializable
sealed interface SshRealKey : NavKey {
    @Serializable
    data object Home : SshRealKey

    /**
     * ✅ 직렬화할 수 있는 식별자만. 람다를 가진 객체를 넣어도 컴파일은 된다(플러그인이 다형 직렬화기를 붙임) —
     * 백스택을 저장하는 순간 SerializationException 이다(임시 프로브 · 단위 테스트로 확인).
     */
    @Serializable
    data class Detail(val userId: String) : SshRealKey
}

internal fun sshRealLabel(key: NavKey): String = when (key) {
    SshRealKey.Home -> "Home"
    is SshRealKey.Detail -> "Detail(userId=${key.userId})"
    else -> key.toString()
}

/** 화면에 보일 사용자 — 키가 아니라 저장소에 있다 */
internal data class RealUser(val id: String, val name: String, val plan: String)

/** 실제 앱이라면 Koin 으로 주입될 저장소. 키의 id 로 객체를 다시 찾는다 */
internal class RealUserRepository {
    private val users = mapOf(
        "u-100" to RealUser("u-100", "Alice", "Pro"),
        "u-200" to RealUser("u-200", "Bob", "Free")
    )

    fun find(id: String): RealUser? = users[id]
}

/** 엔트리마다 하나 — 인자(userId)는 키에서, 화면이 바꾸는 값(읽음 수)은 SavedStateHandle 에 */
internal class SshDetailViewModel(
    val userId: String,
    private val handle: SavedStateHandle,
    repository: RealUserRepository
) : ViewModel() {
    /** 객체는 키에 넣지 않고 id 로 다시 만든다 — 없는 id 면 null(크래시 대신 '찾을 수 없음') */
    val user: RealUser? = repository.find(userId)

    val reads: StateFlow<Int> = handle.getStateFlow(KEY_READS, 0)

    fun markRead() {
        handle[KEY_READS] = reads.value + 1
    }

    companion object {
        const val KEY_READS = "reads"
    }
}

/** 화면 최상위에서 만든 실물 데모 상태 */
internal class SshRealDemo(
    val backStack: NavBackStack<NavKey>,
    val entries: List<NavEntry<NavKey>>
)

private val SshHomeColor = Color(0xFF00695C)
private val SshDetailColor = Color(0xFF283593)
private val SshGray = Color(0xFF546E7A)

/**
 * 화면 최상위에서 부른다 — 백스택 · 데코레이터(엔트리 저장 상태 · 엔트리 ViewModel 저장소)를 여기서 만들어
 * LazyColumn 카드가 스크롤로 빠져도 SavedStateHandle 값이 남게 한다.
 */
@Composable
internal fun rememberSshRealDemo(): SshRealDemo {
    val repository = remember { RealUserRepository() }
    val backStack = rememberNavBackStack(SshRealKey.Home)
    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = listOf(
            // 엔트리 ViewModel 의 SavedStateHandle 은 이 데코레이터가 주는 엔트리별 저장 상태에 실린다
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<SshRealKey.Home> {
                RealPane("Home", SshHomeColor) {
                    Text(text = "키에는 userId 만 담아 Detail 로 간다", fontSize = 12.sp)
                    RealButton("Alice (u-100) →", SshHomeColor) { backStack.add(SshRealKey.Detail("u-100")) }
                    RealButton("Bob (u-200) →", SshHomeColor) { backStack.add(SshRealKey.Detail("u-200")) }
                    RealButton("없는 사용자 (u-999) →", SshGray) { backStack.add(SshRealKey.Detail("u-999")) }
                }
            }
            entry<SshRealKey.Detail> { key ->
                val vm: SshDetailViewModel = viewModel {
                    SshDetailViewModel(key.userId, createSavedStateHandle(), repository)
                }
                val reads by vm.reads.collectAsState()
                RealPane("Detail", SshDetailColor) {
                    RealInfoLine("키", "userId=${key.userId}")
                    val user = vm.user
                    RealInfoLine(
                        "저장소에서",
                        if (user != null) "${user.name} (${user.plan})" else "찾을 수 없음",
                        valueColor = if (user != null) Color(0xFF212121) else Color(0xFFC62828)
                    )
                    RealInfoLine("읽음 수", "$reads  (SavedStateHandle)")
                    RealButtonRow {
                        RealButton("읽음 +1", SshDetailColor) { vm.markRead() }
                        RealButton("← 뒤로", SshGray) { pop() }
                    }
                }
            }
        }
    )
    return SshRealDemo(backStack, entries)
}

@Composable
internal fun Nav3SavedStateHandleRealCard(demo: SshRealDemo) {
    RealNavCard(
        title = "@Serializable 키 + 엔트리 ViewModel 의 SavedStateHandle",
        subtitle = "위 카드들의 직렬화 · 복원은 문자열로 흉내 낸 것이다. 이 카드는 rememberNavBackStack 과 " +
            "lifecycle-viewmodel-navigation3 로 실제로 저장하고 되돌린다."
    ) {
        RealNavFrame(height = 230.dp) {
            NavDisplay(
                entries = demo.entries,
                onBack = { if (demo.backStack.size > 1) demo.backStack.removeAt(demo.backStack.lastIndex) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        RealInfoLine("백스택", demo.backStack.joinToString(" › ") { sshRealLabel(it) })
        Spacer(modifier = Modifier.height(8.dp))
        RealCaption(
            "• 키에는 id 만 — data class Detail(val userId: String). 프로필 객체는 ViewModel 이 저장소에서 다시 찾는다(없는 id 는 '찾을 수 없음').\n" +
                "• 읽음 수는 그 엔트리 ViewModel 의 SavedStateHandle 에 있다. 뒤로 갔다가 같은 사람을 다시 열면 0 에서 시작한다 — " +
                "Pop 하면 그 엔트리의 저장소가 비워진다.\n" +
                "• 다크 모드 전환(액티비티 재생성)과 프로세스 종료(앱을 내린 뒤 am kill → 최근 앱으로 복귀) 뒤에도 백스택과 읽음 수가 그대로 돌아온다.\n" +
                "• 위 '복합 객체를 키에 담기' 를 실물로 하면 빌드는 된다 — 직렬화 플러그인이 () -> Unit 필드에 다형(polymorphic) 직렬화기를 " +
                "붙인다. 대신 백스택을 저장하는 순간(앱이 백그라운드로 갈 때) 람다 클래스의 직렬화기를 찾지 못해 SerializationException 으로 " +
                "죽는다 — 복원이 아니라 저장에서. 그래서 이 카드의 키는 String id 하나뿐이다."
        )
    }
}
