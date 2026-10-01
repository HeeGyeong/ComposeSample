package com.example.composesample.presentation.example.component.architecture.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composesample.presentation.MainHeader

/**
 * Per-Item ViewModels in Compose
 *
 * LazyColumn 의 각 아이템마다 독립된 ViewModel 스코프를 부여하는 방법을 비교한다.
 *
 * [A] 단일 화면 ViewModel 공유 — 모든 아이템이 같은 상태를 봐서 클릭 하나에 전부 바뀐다
 * [B] 수작업 per-key Store — 키별 ViewModelStore 를 직접 들고 CompositionLocalProvider 로 갈아끼운다
 * [C] lifecycle 2.11 공식 API — rememberViewModelStoreProvider() + rememberViewModelStoreOwner(key, provider)
 *
 * 두 구현의 차이(실측)는 "언제 ViewModel 이 비워지는가"에서 난다 — 화면의 '살아 있는 ViewModel' 목록으로 직접 본다.
 * 참고 자료·측정 결과는 같은 폴더의 exampleGuide.kt.
 */
@Composable
fun PerItemViewModelExampleUI(onBackEvent: () -> Unit) {
    // Provider·수작업 매니저는 LazyColumn 밖(화면 수준)에 둔다. 섹션이 들어 있는 lazy 항목 안에 두면 그 항목이 스크롤로
    // 화면 밖에 나갈 때 함께 컴포지션을 떠나, 공식 API 도 clearAllKeys() 로 전부 비워진다(실측).
    // 키를 주지 않으면 이 호출 위치의 composite key hash 로 부모(액티비티) Store 안의 상태를 찾는다.
    val provider: ViewModelStoreProvider = rememberViewModelStoreProvider()
    val manualStores = remember { PerKeyViewModelStores() }
    DisposableEffect(manualStores) { onDispose { manualStores.clearAll() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
    ) {
        MainHeader(
            title = "Per-Item ViewModels",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item { SectionTitle("A. 단일 화면 ViewModel (공유 상태 충돌)") }
            item { SharedScopeSection() }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SectionTitle("B. 수작업 per-key Store")
            }
            item { ManualScopeSection(manualStores) }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SectionTitle("C. 공식 API (lifecycle 2.11)")
            }
            item { OfficialScopeSection(provider) }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SectionTitle("살아 있는 ViewModel")
            }
            item { LiveViewModelsCard() }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SectionTitle("핵심 패턴과 함정")
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
    // private 클래스라 기본 viewModel() 의 리플렉션 생성(NewInstanceFactory)이 IllegalAccessException 으로 실패한다
    // (release 는 화면 진입 즉시 크래시, debug 는 live edit 이 오류를 삼켜 이 영역이 비고 이후 클릭이 반영되지 않았다).
    // 초기화 람다로 생성자를 직접 호출한다 — 아래 ItemCounterViewModel 도 같은 방식.
    val vm: SharedItemsViewModel = viewModel { SharedItemsViewModel() }
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
// 아이템 ViewModel + 살아 있는 인스턴스 기록
// ====================================================================

/**
 * 프로세스 전체에서 살아 있는 아이템 ViewModel 목록. 액티비티가 재생성돼도 남아 있어서,
 * 구성 변경 전후로 어느 인스턴스가 살아남았는지 볼 수 있다. 생성은 SideEffect 에서 등록하고(컴포지션 중 쓰기 회피),
 * onCleared 에서 지운다.
 */
private object LiveItemViewModels {
    val tags = mutableStateMapOf<String, String>()
    private var seq = 0

    fun nextId(): Int = ++seq
}

private class ItemCounterViewModel(label: String) : ViewModel() {
    var counter: MutableIntState = mutableIntStateOf(0)
        private set

    /** 인스턴스 식별 — 같은 아이템이라도 새로 만들어지면 번호가 바뀐다 */
    val instanceTag: String = "$label·VM#${LiveItemViewModels.nextId()}"

    fun increment() {
        counter.intValue = counter.intValue + 1
    }

    override fun onCleared() {
        LiveItemViewModels.tags.remove(instanceTag)
    }
}

/** 아이템 키로 만든 owner 를 끼워 넣고, 그 안에서 평소처럼 viewModel() 을 부른다 */
@Composable
private fun ItemCounterRow(owner: ViewModelStoreOwner, label: String, onRemove: () -> Unit) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        val vm: ItemCounterViewModel = viewModel { ItemCounterViewModel(label) }
        SideEffect { LiveItemViewModels.tags[vm.instanceTag] = label }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(text = label, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${vm.instanceTag.substringAfter('·')}  /  count=${vm.counter.intValue}",
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
// Section B — 수작업 per-key Store
// ====================================================================

/**
 * 키별로 [ViewModelStore] 를 보관하는 매니저. remember 로 들고 있어서 컴포지션과 수명이 같다 —
 * 아이템이 컴포지션에서 빠지면(onDispose) 그 키의 store 를 clear() 하고, 화면 전체가 떠나면 전부 비운다.
 * 구성 변경으로 컴포지션이 다시 만들어질 때도 똑같이 비워진다(그래서 카운터가 초기화된다).
 */
private class PerKeyViewModelStores {
    private val stores = mutableMapOf<String, ViewModelStore>()

    fun getOrCreate(key: String): ViewModelStore = stores.getOrPut(key) { ViewModelStore() }

    fun release(key: String) {
        stores.remove(key)?.clear()
    }

    fun clearAll() {
        stores.values.forEach { it.clear() }
        stores.clear()
    }
}

@Composable
private fun ManualScopeSection(stores: PerKeyViewModelStores) {
    var items by rememberSaveable { mutableStateOf(listOf("m1", "m2", "m3")) }
    var nextId by rememberSaveable { mutableIntStateOf(4) }
    var expanded by rememberSaveable { mutableStateOf(true) }

    ScopeCard(
        description = "키별 ViewModelStore 를 remember 로 들고, 아이템이 컴포지션에서 빠지면 비운다.",
        containerColor = Color(0xFFFFF4E0),
        expanded = expanded,
        onToggleExpanded = { expanded = !expanded },
        onAdd = {
            items = items + "m$nextId"
            nextId += 1
        }
    ) {
        if (expanded) {
            items.forEach { itemKey ->
                // key() 가 없으면 슬롯이 위치로 매겨져, 중간 아이템을 지울 때 그 뒤 아이템들의 store 까지 비워진다(실측)
                key(itemKey) {
                    val owner = remember(itemKey) {
                        object : ViewModelStoreOwner {
                            override val viewModelStore: ViewModelStore = stores.getOrCreate(itemKey)
                        }
                    }
                    DisposableEffect(itemKey) { onDispose { stores.release(itemKey) } }
                    ItemCounterRow(owner = owner, label = "수작업 $itemKey", onRemove = { items = items - itemKey })
                }
            }
        }
    }
}

// ====================================================================
// Section C — lifecycle 2.11 공식 API
// ====================================================================

/** provider 는 부모(액티비티)의 ViewModelStore 안에 상태를 두므로 구성 변경을 넘어 살아남는다 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfficialScopeSection(provider: ViewModelStoreProvider) {
    var items by rememberSaveable { mutableStateOf(listOf("o1", "o2", "o3")) }
    var nextId by rememberSaveable { mutableIntStateOf(4) }
    var expanded by rememberSaveable { mutableStateOf(true) }
    var clearOnRemove by rememberSaveable { mutableStateOf(true) }

    ScopeCard(
        description = "rememberViewModelStoreProvider() + rememberViewModelStoreOwner(key, provider). " +
            "아이템을 지울 때 clearKey(key) 를 직접 불러야 비워진다.",
        containerColor = Color(0xFFE7F4EA),
        expanded = expanded,
        onToggleExpanded = { expanded = !expanded },
        onAdd = {
            items = items + "o$nextId"
            nextId += 1
        },
        extraControls = {
            FlowRow {
                FilterChip(
                    selected = clearOnRemove,
                    onClick = { clearOnRemove = !clearOnRemove },
                    label = { Text("삭제할 때 clearKey 호출", fontSize = 12.sp) }
                )
            }
        }
    ) {
        if (expanded) {
            items.forEach { itemKey ->
                key(itemKey) {
                    val owner = rememberViewModelStoreOwner(itemKey, provider)
                    ItemCounterRow(
                        owner = owner,
                        label = "공식 $itemKey",
                        onRemove = {
                            items = items - itemKey
                            // owner 는 토큰만 닫고 clearKey 를 부르지 않는다 — 데이터에서 지운 시점을 아는 쪽이 직접 비운다
                            if (clearOnRemove) provider.clearKey(itemKey)
                        }
                    )
                }
            }
        }
    }
}

// ====================================================================
// 공통 카드 · 살아 있는 ViewModel
// ====================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScopeCard(
    description: String,
    containerColor: Color,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onAdd: () -> Unit,
    extraControls: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = description, fontSize = 13.sp, color = Color(0xFF333333))
            content()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdd) { Text("아이템 추가") }
                Button(
                    onClick = onToggleExpanded,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF888888))
                ) { Text(if (expanded) "접기(컴포지션에서 빼기)" else "펼치기") }
            }
            extraControls()
        }
    }
}

@Composable
private fun LiveViewModelsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "onCleared() 가 불리기 전까지의 인스턴스. 아이템을 지우고·접고·다크 모드나 글꼴 크기를 바꿔(액티비티 재생성) 보세요.",
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )
            val live = LiveItemViewModels.tags.keys.sortedBy { it.substringAfter('#').toIntOrNull() ?: 0 }
            if (live.isEmpty()) {
                Text("없음", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            } else {
                live.forEach { tag ->
                    Text(tag, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF263238))
                }
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
            Text("실측(Galaxy A72) — 언제 비워지나", fontWeight = FontWeight.Bold)
            Text("• 액티비티 재생성: 수작업은 전부 새 인스턴스, 공식 API 는 그대로(부모 Store 에 상태를 둔다)", fontSize = 13.sp)
            Text("• 아이템 삭제: 수작업은 즉시 onCleared, 공식 API 는 clearKey 를 부르지 않으면 남는다(화면을 떠날 때 clearAllKeys 로 정리)", fontSize = 13.sp)
            Text("• 접기(컴포지션에서 빼기)·스크롤로 화면 밖에 나감: 수작업은 비워지고 다시 그리면 새로 만든다, 공식 API 는 유지", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("함정", fontWeight = FontWeight.Bold)
            Text(
                "• forEach 에 key() 가 없으면 슬롯이 위치로 매겨져, 중간 아이템을 지울 때 그 뒤 아이템들의 store 까지 비워진다(이 예제의 이전 구현에서 실측)",
                fontSize = 13.sp
            )
            Text(
                "• 키 없는 rememberViewModelStoreProvider() 는 호출 위치의 composite key hash 로 상태를 찾는다. 형제 Provider 가 여럿이거나 " +
                    "조건에 따라 위치가 바뀌면 key 를 직접 준다",
                fontSize = 13.sp
            )
            Text(
                "• Provider 는 아이템을 담은 LazyColumn·Pager 밖에 둔다 — 스크롤로 사라지는 항목 안에 두면 그 항목이 빠질 때 " +
                    "clearAllKeys() 로 전부 비워지고, 돌아오면 새로 만든다(실측)",
                fontSize = 13.sp
            )
            Text(
                "• 공식 owner 는 토큰만 닫는다 — '데이터에서 지웠다'는 사실은 컴포지션이 모르므로 clearKey 는 지우는 쪽 책임이다",
                fontSize = 13.sp
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
