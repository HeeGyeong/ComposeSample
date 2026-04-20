package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * Nav3 ViewModel Scope 예제
 *
 * Navigation 3 에서 ViewModel 스코프 동작이 Navigation 2 와 어떻게 달라지는지,
 * 이전 동작을 복원하려면 어떻게 해야 하는지 3가지 섹션으로 시뮬레이션합니다.
 *
 * 1) Nav2 Auto-Scope: 각 NavBackStackEntry 가 ViewModelStoreOwner → 화면 pop 시 자동 clear
 * 2) Nav3 Default: List<NavKey> 직접 관리 → 자동 ViewModelStoreOwner 없음 → pop 해도 ViewModel 유지
 * 3) Nav3 + Restore Pattern: NavKey 별 ViewModelStore 를 직접 관리 → Nav2 와 동일 동작 복원
 */
@Composable
fun Nav3ViewModelScopeExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Nav3 ViewModel Scope",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item { Nav2AutoScopeCard() }
            item { Nav3DefaultCard() }
            item { Nav3RestoreScopeCard() }
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFEDE7F6),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🧭 ViewModel 스코프 동작 변화",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5E35B1)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Nav2는 NavBackStackEntry 자체가 ViewModelStoreOwner 였습니다. " +
                        "Nav3는 백스택이 List<NavKey> 라는 순수 상태 — 기본적으로는 자동 스코프가 없습니다. " +
                        "아래 3가지 시나리오로 차이를 확인합니다.",
                fontSize = 12.sp,
                color = Color(0xFF4527A0),
                lineHeight = 16.sp
            )
        }
    }
}

// 간단한 카운터 ViewModel 모델 (실제 ViewModel 대신 시뮬레이션)
private class CounterViewModel(val screenName: String) {
    var count: Int = 0
        private set

    fun increment() {
        count++
    }
}

@Composable
private fun Nav2AutoScopeCard() {
    // Nav2 시뮬레이션: backStack 의 각 엔트리가 자체 ViewModel 을 소유하고, pop 시 자동 clear
    var backStack by remember { mutableStateOf(listOf("Home")) }
    val storeByEntry = remember { mutableStateMapOf<String, CounterViewModel>() }

    // 현재 화면에 대한 VM 보장
    val currentKey = backStack.last() + "#${backStack.size}"
    val currentVm = storeByEntry.getOrPut(currentKey) { CounterViewModel(backStack.last()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "✅ Nav2 Auto-Scope",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "NavBackStackEntry == ViewModelStoreOwner → pop 시 자동으로 VM 제거",
                fontSize = 11.sp,
                color = Color(0xFF558B2F)
            )
            Spacer(Modifier.height(12.dp))

            BackStackBox(backStack, Color(0xFF2E7D32))

            Spacer(Modifier.height(8.dp))

            CounterBox(
                label = "현재 화면: ${currentVm.screenName} (key=$currentKey)",
                count = currentVm.count,
                color = Color(0xFF2E7D32),
                onIncrement = { currentVm.increment() }
            )

            Spacer(Modifier.height(8.dp))

            StoreInspector(
                storeKeys = storeByEntry.keys.toList(),
                color = Color(0xFF2E7D32)
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        backStack = backStack + "Detail"
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Push", fontSize = 12.sp, color = Color.White)
                }
                Button(
                    onClick = {
                        if (backStack.size > 1) {
                            // Nav2 동작: pop 되는 엔트리의 ViewModel 자동 clear
                            val poppedKey = backStack.last() + "#${backStack.size}"
                            storeByEntry.remove(poppedKey)
                            backStack = backStack.dropLast(1)
                        }
                    },
                    enabled = backStack.size > 1,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF7043)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pop (clear)", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun Nav3DefaultCard() {
    // Nav3 기본 동작: 백스택은 List<NavKey>, ViewModel 은 상위 Composable 범위에 1회 생성 후 공유
    var backStack by remember { mutableStateOf(listOf("Home")) }
    // 핵심 차이: VM 을 엔트리 키가 아니라 "화면 이름"으로만 저장 → pop 해도 제거되지 않음
    val sharedStore = remember { mutableStateMapOf<String, CounterViewModel>() }

    val currentName = backStack.last()
    val currentVm = sharedStore.getOrPut(currentName) { CounterViewModel(currentName) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "⚠ Nav3 Default (스코프 없음)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "List<NavKey> 만 관리 → 상위 범위에 ViewModel 이 남음 → pop 해도 상태 유지",
                fontSize = 11.sp,
                color = Color(0xFFB71C1C)
            )
            Spacer(Modifier.height(12.dp))

            BackStackBox(backStack, Color(0xFFC62828))

            Spacer(Modifier.height(8.dp))

            CounterBox(
                label = "현재 화면: ${currentVm.screenName}",
                count = currentVm.count,
                color = Color(0xFFC62828),
                onIncrement = { currentVm.increment() }
            )

            Spacer(Modifier.height(8.dp))

            StoreInspector(
                storeKeys = sharedStore.keys.toList(),
                color = Color(0xFFC62828)
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        backStack = backStack + "Detail"
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Push", fontSize = 12.sp, color = Color.White)
                }
                Button(
                    onClick = {
                        if (backStack.size > 1) {
                            // Nav3 기본: pop 해도 ViewModel 을 정리하지 않음 (잠재적 메모리 누수)
                            backStack = backStack.dropLast(1)
                        }
                    },
                    enabled = backStack.size > 1,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF7043)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pop (유지)", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            HintBox(
                "💡 Detail 에서 카운터를 올리고 Pop → 다시 Push 하면 이전 카운트가 그대로 남아있음",
                Color(0xFFC62828)
            )
        }
    }
}

@Composable
private fun Nav3RestoreScopeCard() {
    // Nav3 라이프사이클 확장 패턴: NavKey 별 VM 매핑을 유지하고, 백스택에서 사라진 키의 VM 만 제거
    var backStack by remember { mutableStateOf(listOf("Home")) }
    val scopedStore = remember { mutableStateMapOf<String, CounterViewModel>() }

    val currentKey = backStack.last() + "#${backStack.size}"
    val currentVm = scopedStore.getOrPut(currentKey) { CounterViewModel(backStack.last()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🔧 Nav3 + Restore Pattern",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "NavKey 별 ViewModelStore Map 을 직접 유지 → 백스택 diff 로 사라진 키만 정리",
                fontSize = 11.sp,
                color = Color(0xFF0D47A1)
            )
            Spacer(Modifier.height(12.dp))

            BackStackBox(backStack, Color(0xFF1565C0))

            Spacer(Modifier.height(8.dp))

            CounterBox(
                label = "현재 화면: ${currentVm.screenName} (key=$currentKey)",
                count = currentVm.count,
                color = Color(0xFF1565C0),
                onIncrement = { currentVm.increment() }
            )

            Spacer(Modifier.height(8.dp))

            StoreInspector(
                storeKeys = scopedStore.keys.toList(),
                color = Color(0xFF1565C0)
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        backStack = backStack + "Detail"
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Push", fontSize = 12.sp, color = Color.White)
                }
                Button(
                    onClick = {
                        if (backStack.size > 1) {
                            val newBackStack = backStack.dropLast(1)
                            // 복원 패턴: 살아있는 키 집합과의 diff 로 사라진 VM 만 제거
                            val liveKeys = newBackStack.mapIndexed { idx, name -> "$name#${idx + 1}" }.toSet()
                            scopedStore.keys
                                .filterNot { it in liveKeys }
                                .forEach { scopedStore.remove(it) }
                            backStack = newBackStack
                        }
                    },
                    enabled = backStack.size > 1,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF7043)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pop (diff clear)", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            HintBox(
                "💡 실제 Nav3 에서는 rememberSaveableStateHolder + DisposableEffect 로 동일 로직을 구현합니다",
                Color(0xFF1565C0)
            )
        }
    }
}

@Composable
private fun BackStackBox(backStack: List<String>, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Text(
            text = "BackStack: ${backStack.joinToString(" → ")}",
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun CounterBox(
    label: String,
    count: Int,
    color: Color,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = Color(0xFF444444))
            Text(
                "count = $count",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
        Button(
            onClick = onIncrement,
            colors = ButtonDefaults.buttonColors(backgroundColor = color),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("+1", fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
private fun StoreInspector(storeKeys: List<String>, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "ViewModelStore 내용 (${storeKeys.size}개)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (storeKeys.isEmpty()) {
                Text("(비어있음)", fontSize = 10.sp, color = Color.Gray)
            } else {
                storeKeys.forEach { key ->
                    Text(
                        "• $key",
                        fontSize = 10.sp,
                        color = Color(0xFF555555),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun HintBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(text, fontSize = 10.sp, color = color, lineHeight = 14.sp)
    }
}
