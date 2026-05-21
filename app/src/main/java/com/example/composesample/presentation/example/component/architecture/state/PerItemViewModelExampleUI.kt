package com.example.composesample.presentation.example.component.architecture.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Per-Item ViewModels in Compose
 *
 * LazyColumn 의 각 아이템마다 독립된 ViewModel 스코프를 부여하는 방법을 비교한다.
 *
 * [Section A] 단일 화면 ViewModel 공유
 *  - 모든 아이템이 동일 ViewModel 의 상태를 바라봐 클릭 시 모든 카드가 동시에 변화 → 의도치 않은 결합
 *
 * [Section B] 아이템별 ViewModelStoreOwner per-key
 *  - 키(id)별로 독립된 [ViewModelStore] 를 부여하여 각 아이템이 자기 ViewModel 만 갖는다.
 *  - 아이템이 LazyColumn 에서 떨어져 나갈 때(DisposableEffect onDispose) 해당 Store 를 clear() 해 메모리 누수 방지.
 *
 * Lifecycle 2.11+ 의 신규 per-key API 없이도 [LocalViewModelStoreOwner] 를 CompositionLocalProvider 로
 * 교체하는 패턴으로 동일한 효과를 얻을 수 있다.
 */
@Composable
fun PerItemViewModelExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Per-Item ViewModels in Compose",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            item { SectionTitle("A. 단일 화면 ViewModel (공유 상태 충돌)") }
            item { SharedScopeSection() }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SectionTitle("B. Per-Item ViewModelStoreOwner")
            }
            item { PerItemScopeSection() }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SectionTitle("핵심 패턴 요약")
            }
            item { ExplanationCard() }
        }
    }
}

// ====================================================================
// Section A — 단일 ViewModel 을 모든 아이템이 공유 (의도치 않은 결합)
// ====================================================================

private class SharedItemsViewModel : ViewModel() {
    // 한 ViewModel 에 단일 카운터 → 모든 아이템이 같은 값을 본다
    var counter: MutableIntState = mutableIntStateOf(0)
        private set

    fun increment() {
        counter.intValue = counter.intValue + 1
    }
}

@Composable
private fun SharedScopeSection() {
    val vm: SharedItemsViewModel = viewModel()
    val items = remember { (1..4).map { "Item #$it" } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE9E9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "모든 카드가 같은 ViewModel 을 본다 → 클릭 시 전부 동시에 증가",
                fontSize = 13.sp,
                color = Color(0xFF7A1F1F)
            )
            items.forEach { label ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$label — 카운터: ${vm.counter.intValue}")
                    Button(
                        onClick = { vm.increment() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD43A3A))
                    ) { Text("+1") }
                }
            }
        }
    }
}

// ====================================================================
// Section B — 아이템별 독립 ViewModelStoreOwner
// ====================================================================

private class ItemCounterViewModel : ViewModel() {
    var counter: MutableIntState = mutableIntStateOf(0)
        private set

    // 디버그용: 인스턴스 식별
    val instanceTag: String = "VM@${System.identityHashCode(this).toString(16)}"

    fun increment() {
        counter.intValue = counter.intValue + 1
    }

    override fun onCleared() {
        // clear() 호출이 실제로 발생하는지 시연용 로그 — 운영 코드에서는 불필요
        android.util.Log.d("PerItemVM", "$instanceTag onCleared()")
    }
}

/**
 * 키별로 [ViewModelStore] 를 보관하는 매니저.
 * - 아이템이 LazyColumn 에서 벗어나면 (DisposableEffect.onDispose) 해당 키의 store 를 clear() 한다.
 */
private class PerKeyViewModelStores : ViewModelStoreOwner {
    private val stores = mutableMapOf<String, ViewModelStore>()

    fun getOrCreate(key: String): ViewModelStore =
        stores.getOrPut(key) { ViewModelStore() }

    fun release(key: String) {
        stores.remove(key)?.clear()
    }

    fun clearAll() {
        stores.values.forEach { it.clear() }
        stores.clear()
    }

    // ViewModelStoreOwner 구현은 매니저 자체에는 의미가 없으나 인터페이스 충족용
    override val viewModelStore: ViewModelStore
        get() = throw UnsupportedOperationException("use getOrCreate(key)")
}

@Composable
private fun rememberPerKeyStores(): PerKeyViewModelStores {
    val manager = remember { PerKeyViewModelStores() }
    DisposableEffect(manager) {
        onDispose { manager.clearAll() }
    }
    return manager
}

@Composable
private fun PerItemScopeSection() {
    val stores = rememberPerKeyStores()
    var items by remember { mutableStateOf((1..4).map { "item-$it" }) }
    var nextId by remember { mutableIntStateOf(5) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F4EA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "각 카드가 자기만의 ViewModel 을 갖는다 — 클릭은 해당 카드에만 영향",
                fontSize = 13.sp,
                color = Color(0xFF1F5F2A)
            )
            items.forEach { key ->
                PerItemCard(
                    itemKey = key,
                    storeFor = { stores.getOrCreate(it) },
                    onForgotten = { stores.release(it) },
                    onRemove = { items = items.filterNot { v -> v == key } }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    items = items + "item-${nextId}"
                    nextId += 1
                }) { Text("아이템 추가") }
                Button(
                    onClick = {
                        if (items.isNotEmpty()) items = items.dropLast(1)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF888888))
                ) { Text("마지막 제거") }
            }
        }
    }
}

@Composable
private fun PerItemCard(
    itemKey: String,
    storeFor: (String) -> ViewModelStore,
    onForgotten: (String) -> Unit,
    onRemove: () -> Unit
) {
    // 키별 ViewModelStoreOwner — Composition 에서 사라질 때 onForgotten 으로 정리
    val owner = remember(itemKey) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = storeFor(itemKey)
        }
    }
    DisposableEffect(itemKey) {
        onDispose { onForgotten(itemKey) }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        // viewModel() 은 LocalViewModelStoreOwner 에서 store 를 가져온다 → 각 아이템이 독립
        val vm: ItemCounterViewModel = viewModel(
            modelClass = ItemCounterViewModel::class.java,
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ItemCounterViewModel() as T
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(text = itemKey, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${vm.instanceTag}  /  count=${vm.counter.intValue}",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { vm.increment() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B47))
                ) { Text("+1") }
                Button(
                    onClick = onRemove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAAAAAA))
                ) { Text("x") }
            }
        }
    }
}

// ====================================================================
// 설명 카드
// ====================================================================

@Composable
private fun ExplanationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("핵심 패턴", fontWeight = FontWeight.Bold)
            Text("1) 키별 ViewModelStore 매니저 보유 — 아이템 키 → ViewModelStore 매핑", fontSize = 13.sp)
            Text("2) CompositionLocalProvider(LocalViewModelStoreOwner provides owner) 로 교체", fontSize = 13.sp)
            Text("3) DisposableEffect onDispose 에서 store.clear() — 메모리 누수 방지", fontSize = 13.sp)
            Text("4) viewModel() 호출은 평소처럼 — store 출처만 갈아끼우는 구조", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "주의: Lifecycle 2.11+ 의 신규 ViewModelStoreOwner per-key API 가 정식화되면 직접 매핑 코드 없이 사용 가능. 현재 (2.9.x) 환경에서는 위 패턴으로 동등 효과 구현.",
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
