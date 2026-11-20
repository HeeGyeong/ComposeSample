package com.example.composesample.example

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import com.example.composesample.presentation.example.component.architecture.navigation.BasicRealKey
import com.example.composesample.presentation.example.component.architecture.navigation.RealUserRepository
import com.example.composesample.presentation.example.component.architecture.navigation.ScopeCounterViewModel
import com.example.composesample.presentation.example.component.architecture.navigation.ScopeRealKey
import com.example.composesample.presentation.example.component.architecture.navigation.ScopeRealTracker
import com.example.composesample.presentation.example.component.architecture.navigation.SshDetailViewModel
import com.example.composesample.presentation.example.component.architecture.navigation.SshRealKey
import com.example.composesample.presentation.example.component.architecture.navigation.basicRealLabel
import com.example.composesample.presentation.example.component.architecture.navigation.scopeRealLabel
import com.example.composesample.presentation.example.component.architecture.navigation.sshRealLabel
import com.example.composesample.presentation.example.component.ui.navigation.RealTab
import com.example.composesample.presentation.example.component.ui.navigation.TabRealKey
import com.example.composesample.presentation.example.component.ui.navigation.tabRealLabel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Nav3 실물 카드(#117)의 키 · 기록 · ViewModel 검증.
 * given : 네 카드의 @Serializable 키, 엔트리 ViewModel 과 그 저장소
 * when : 키를 직렬화해 되돌리고, 저장소를 비우고, SavedStateHandle 에 남은 값으로 ViewModel 을 다시 만든다
 * then : 키는 같은 값으로 돌아오고, 비운 저장소의 ViewModel 만 onCleared 를 남기고, 읽음 수는 이어진다
 */
class Nav3RealCardsTest {

    private val json = Json

    private fun <T> roundTrip(serializer: KSerializer<T>, value: T): T =
        json.decodeFromString(serializer, json.encodeToString(serializer, value))

    @Test
    fun `네 카드의 키는 직렬화했다 되돌려도 같은 값 — rememberNavBackStack 이 저장 · 복원하는 모양`() {
        listOf(BasicRealKey.Home, BasicRealKey.Profile("user_123"), BasicRealKey.Settings, BasicRealKey.Detail(7))
            .forEach { assertEquals(it, roundTrip(BasicRealKey.serializer(), it)) }
        listOf(ScopeRealKey.Home, ScopeRealKey.Detail(3))
            .forEach { assertEquals(it, roundTrip(ScopeRealKey.serializer(), it)) }
        listOf(SshRealKey.Home, SshRealKey.Detail("u-100"))
            .forEach { assertEquals(it, roundTrip(SshRealKey.serializer(), it)) }
        RealTab.entries.forEach { tab ->
            assertEquals(TabRealKey.Root(tab), roundTrip(TabRealKey.serializer(), TabRealKey.Root(tab)))
            assertEquals(TabRealKey.Item(tab, 2), roundTrip(TabRealKey.serializer(), TabRealKey.Item(tab, 2)))
        }
    }

    @Test
    fun `탭 키는 탭마다 달라 contentKey 가 겹치지 않는다`() {
        val roots = RealTab.entries.map { TabRealKey.Root(it).toString() }
        assertEquals(roots.size, roots.toSet().size)
        assertTrue(TabRealKey.Item(RealTab.Home, 1).toString() != TabRealKey.Item(RealTab.Plans, 1).toString())
    }

    @Test
    fun `백스택 줄에 쓰는 이름`() {
        assertEquals("Profile(user_123)", basicRealLabel(BasicRealKey.Profile("user_123")))
        assertEquals("Detail(#7)", basicRealLabel(BasicRealKey.Detail(7)))
        assertEquals("Detail 2", scopeRealLabel(ScopeRealKey.Detail(2)))
        assertEquals("Detail(userId=u-999)", sshRealLabel(SshRealKey.Detail("u-999")))
        assertEquals("Plans/3", tabRealLabel(TabRealKey.Item(RealTab.Plans, 3)))
        assertEquals("User", tabRealLabel(TabRealKey.Root(RealTab.User)))
    }

    @Test
    fun `저장소를 비우면 그 ViewModel 만 onCleared 를 남긴다 — 최근이 위, 6줄까지`() {
        val tracker = ScopeRealTracker()
        val kept = ownerWith(tracker, "Home")
        val popped = ownerWith(tracker, "Detail 1")
        val keptVm = kept.second
        val poppedVm = popped.second
        assertEquals(1, keptVm.id)
        assertEquals(2, poppedVm.id)

        popped.first.viewModelStore.clear()
        assertEquals(listOf("ViewModel #2 onCleared (처음 만든 화면: Detail 1)"), tracker.log.toList())

        // 같은 저장소에서 다시 꺼내면 같은 인스턴스 — 데코레이터 없이 함께 쓰는 경우
        assertEquals(keptVm, provider(kept.first, tracker, "Detail 9")[ScopeCounterViewModel::class])
        assertEquals("Home", keptVm.createdFor)

        repeat(7) { ownerWith(tracker, "x$it").first.viewModelStore.clear() }
        assertEquals(ScopeRealTracker.MAX_LOG, tracker.log.size)
        // 다시 꺼낸 것은 새 번호를 받지 않는다 — #3 ~ #9 가 새로 생겨 마지막이 #9
        assertTrue(tracker.log.first(), tracker.log.first().startsWith("ViewModel #9 "))
    }

    @Test
    fun `SavedStateHandle 에 남은 읽음 수로 ViewModel 을 다시 만들면 이어서 센다 — 프로세스 종료 뒤 복원 모양`() {
        val repository = RealUserRepository()
        val handle = SavedStateHandle(mapOf(SshDetailViewModel.KEY_READS to 2))
        val vm = SshDetailViewModel("u-100", handle, repository)
        assertEquals(2, vm.reads.value)
        vm.markRead()
        assertEquals(3, vm.reads.value)
        assertEquals(3, handle.get<Int>(SshDetailViewModel.KEY_READS))
        assertEquals("Alice", vm.user?.name)

        // 처음 여는 엔트리는 0, 없는 id 는 크래시 대신 null
        val fresh = SshDetailViewModel("u-999", SavedStateHandle(), repository)
        assertEquals(0, fresh.reads.value)
        assertNull(fresh.user)
    }

    @Test
    fun `람다를 가진 키도 컴파일은 되지만 저장(직렬화)에서 SerializationException`() {
        val key = LambdaKey("Alice") {}
        try {
            json.encodeToString(LambdaKey.serializer(), key)
            fail("람다 필드가 직렬화되면 안 된다")
        } catch (e: SerializationException) {
            assertTrue(e.message.orEmpty(), e.message.orEmpty().contains("Function0"))
        }
    }

    private fun provider(owner: ViewModelStoreOwner, tracker: ScopeRealTracker, name: String) =
        ViewModelProvider.create(owner, viewModelFactory { initializer { ScopeCounterViewModel(tracker, name) } })

    private fun ownerWith(tracker: ScopeRealTracker, name: String): Pair<ViewModelStoreOwner, ScopeCounterViewModel> {
        val owner = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        return owner to provider(owner, tracker, name)[ScopeCounterViewModel::class]
    }
}

/** 시뮬레이션 카드의 '복합 객체를 키에 담기' 를 실물 키로 — 테스트에만 둔다 */
@Serializable
data class LambdaKey(val name: String, val onRefresh: () -> Unit) : NavKey
