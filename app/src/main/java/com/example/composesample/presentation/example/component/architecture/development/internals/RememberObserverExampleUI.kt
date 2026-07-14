package com.example.composesample.presentation.example.component.architecture.development.internals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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

// 컴포지션 진입/이탈에 반응하는 RememberObserver 구현체 — onRemembered/onForgotten 이벤트를 콜백으로 전달한다.
// onAbandoned 는 이 객체가 onRemembered 없이 컴포지션 자체가 커밋 실패해 폐기될 때만 호출되어 실사용 데모로 재현하기 어렵다(카드에서 설명으로 대체).
private class LifecycleTracker(
    private val label: String,
    private val onEvent: (String) -> Unit
) : RememberObserver {
    override fun onRemembered() {
        onEvent("🟢 [$label] onRemembered — 컴포지션에 진입")
    }

    override fun onForgotten() {
        onEvent("🔴 [$label] onForgotten — 컴포지션에서 제거됨")
    }

    override fun onAbandoned() {
        onEvent("⚠️ [$label] onAbandoned — 커밋되지 못하고 폐기됨")
    }
}

// 리컴포지션 횟수를 세는 plain holder(HowComposeWorksExampleUI 와 동일 패턴) — snapshot state 가 아니므로 읽어도 무효화를 일으키지 않는다.
private class RecompositionCounter {
    var value = 0
}

@Composable
fun RememberObserverExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "RememberObserver / Composition Lifecycle",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { LifecycleDemoCard() }
            item { DisposableEffectComparisonCard() }
            item { PracticalUseCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RememberObserver 란",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "remember { } 로 기억시킨 객체가 3가지 콜백(onRemembered/onForgotten/onAbandoned)을 구현하면, " +
                        "Compose 런타임이 그 객체의 '컴포지션 멤버십'이 바뀔 때마다 자동으로 호출해 줍니다. " +
                        "DisposableEffect 가 key 변경마다 반응하는 부수 효과 블록인 것과 달리, RememberObserver 는 " +
                        "리컴포지션 자체가 아니라 '이 객체가 지금 컴포지션 트리에 있는가'만 정확히 추적합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "interface RememberObserver {\n" +
                        "    fun onRemembered()   // 컴포지션에 커밋되어 진입할 때\n" +
                        "    fun onForgotten()     // 컴포지션에서 완전히 제거될 때\n" +
                        "    fun onAbandoned()     // 커밋 전 컴포지션이 폐기될 때(예외/취소)\n" +
                        "}\n\n" +
                        "val holder = remember {\n" +
                        "    object : RememberObserver {\n" +
                        "        override fun onRemembered() { /* 리소스 획득 */ }\n" +
                        "        override fun onForgotten() { /* 리소스 해제 */ }\n" +
                        "        override fun onAbandoned() { /* 실패 시 정리 */ }\n" +
                        "    }\n" +
                        "}",
                borderColor = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val callbacks = listOf(
                Triple("onRemembered", "진입", "커밋된 컴포지션에 처음 등장(최초 remember 또는 재진입)"),
                Triple("onForgotten", "이탈", "컴포지션에서 완전히 사라짐(조건부 제거, 스코프 종료 등)"),
                Triple("onAbandoned", "폐기", "onRemembered 없이 컴포지션 자체가 커밋되지 못하고 버려짐")
            )
            callbacks.forEach { (name, phase, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = name, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, color = Color(0xFF1976D2), modifier = Modifier.weight(0.3f))
                    Text(text = phase, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.14f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.56f))
                }
            }
        }
    }
}

@Composable
private fun LifecycleDemoCard() {
    var mounted by remember { mutableStateOf(true) }
    var recomposeTrigger by remember { mutableIntStateOf(0) }
    val events = remember { mutableStateListOf<String>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실동작 — 컴포지션 진입/이탈 vs 리컴포지션",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"컴포지션에서 제거\"는 자식을 실제로 트리에서 뺐다가 다시 넣어 onRemembered/onForgotten 을 유발합니다. " +
                        "\"리컴포지션만 유발\"은 자식을 트리에 그대로 둔 채 body 만 재실행시켜, remember 된 객체가 재생성되지 않고 " +
                        "그대로 살아남는지 비교합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { mounted = !mounted },
                    colors = ButtonDefaults.buttonColors(containerColor = if (mounted) Color(0xFFD32F2F) else Color(0xFF388E3C))
                ) {
                    Text(text = if (mounted) "컴포지션에서 제거" else "컴포지션에 추가", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { recomposeTrigger++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    enabled = mounted
                ) {
                    Text(text = "리컴포지션만 유발", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (mounted) {
                TrackedChild(
                    recomposeTrigger = recomposeTrigger,
                    onEvent = { events.add(0, it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(text = "이벤트 로그 (최신순)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (events.isEmpty()) {
                    Text(text = "(아직 이벤트 없음 — 버튼을 눌러보세요)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF9E9E9E))
                } else {
                    events.take(6).forEach { log ->
                        Text(text = log, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFB3E5FC), lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackedChild(recomposeTrigger: Int, onEvent: (String) -> Unit) {
    // remember 에 key 를 주지 않았으므로, 이 Composable 이 컴포지션에 남아있는 한 recomposeTrigger 가 바뀌어 리컴포즈돼도
    // LifecycleTracker 인스턴스는 재생성되지 않는다 — onRemembered/onForgotten 은 진입/이탈에만 반응한다.
    remember { LifecycleTracker(label = "Child", onEvent = onEvent) }
    val composition = remember { RecompositionCounter() }
    composition.value++

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1976D2).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Child (recomposeTrigger=$recomposeTrigger)", fontSize = 12.sp, color = Color(0xFF1976D2))
        Text(
            text = "이 카드 컴포지션 ${composition.value}회",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF1976D2)
        )
    }
}

@Composable
private fun DisposableEffectComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "DisposableEffect 와 무엇이 다른가",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "DisposableEffect(key) 는 key 가 바뀔 때마다 이전 효과를 onDispose 로 정리하고 새로 실행됩니다 — " +
                        "컴포지션에 계속 남아있어도 key 변경만으로 재실행됩니다. RememberObserver 는 remember 되는 " +
                        "'값 객체' 자신이 생명주기를 구현하므로, 그 객체가 컴포지션에 남아있는 한 어떤 파라미터가 바뀌어도 " +
                        "onRemembered/onForgotten 이 다시 불리지 않고, onAbandoned 로 커밋 실패까지 구분해 줍니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// DisposableEffect: 부수 효과를 별도 블록으로 선언, key 변경 시마다 재실행\n" +
                        "DisposableEffect(someKey) {\n" +
                        "    val resource = acquire()\n" +
                        "    onDispose { resource.release() }\n" +
                        "}\n\n" +
                        "// RememberObserver: remember 되는 값 객체 자신이 생명주기를 구현\n" +
                        "val holder = remember {\n" +
                        "    object : RememberObserver {\n" +
                        "        val resource = acquire()\n" +
                        "        override fun onRemembered() {}\n" +
                        "        override fun onForgotten() { resource.release() }\n" +
                        "        override fun onAbandoned() { resource.release() }\n" +
                        "    }\n" +
                        "}",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val rows = listOf(
                Triple("반응 대상", "key 파라미터 변경 시마다", "객체의 컴포지션 진입/이탈에만"),
                Triple("실패 처리", "onDispose 하나로 통합", "onAbandoned 로 커밋 실패를 별도 구분"),
                Triple("선언 위치", "함수 본문의 부수 효과 블록", "remember 되는 값 객체 자신이 구현")
            )
            rows.forEach { (aspect, disposableEffect, rememberObserver) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(Color(0xFFEDE7F6), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = aspect, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5E35B1), modifier = Modifier.weight(0.22f))
                    Text(text = disposableEffect, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.39f))
                    Text(text = rememberObserver, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.39f))
                }
            }
        }
    }
}

@Composable
private fun PracticalUseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실전 활용 — rememberCoroutineScope 의 내부 원리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "rememberCoroutineScope() 가 반환하는 CoroutineScope 는 Compose 런타임 내부에서 RememberObserver 를 " +
                        "구현한 객체에 감싸여 있어, onForgotten 시점에 scope.cancel() 이 자동 호출됩니다. Composable 을 벗어나면 " +
                        "그 안에서 launch 한 코루틴이 자동으로 취소되는 이유가 바로 이 패턴입니다(아래는 개념 재현이며 실제 " +
                        "런타임 소스를 그대로 옮긴 것은 아닙니다).",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// Compose 런타임 내부에서 rememberCoroutineScope() 가 유사하게 동작합니다(개념 재현)\n" +
                        "@Composable\n" +
                        "fun rememberMyCoroutineScope(): CoroutineScope {\n" +
                        "    val holder = remember {\n" +
                        "        object : RememberObserver {\n" +
                        "            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)\n" +
                        "            override fun onRemembered() {}\n" +
                        "            override fun onForgotten() { scope.cancel() }\n" +
                        "            override fun onAbandoned() { scope.cancel() }\n" +
                        "        }\n" +
                        "    }\n" +
                        "    return holder.scope\n" +
                        "}",
                borderColor = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "같은 원리가 유용한 다른 상황: 리스너 등록/해제나 노출(impression) 로깅처럼 '진입 1회 / 이탈 1회'가 " +
                        "정확히 보장돼야 하는 리소스. 일반 Composable 본문 코드는 리컴포지션마다 재실행되므로 직접 로깅하면 " +
                        "중복이 발생하지만, RememberObserver 는 객체가 remember 슬롯에 실제로 남아있는 동안에는 다시 불리지 않습니다.",
                fontSize = 11.sp,
                color = Color(0xFF424242),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "RememberObserver 는 remember 된 객체가 컴포지션에 진입(onRemembered)·이탈(onForgotten)·폐기(onAbandoned)될 때 호출되는 3가지 콜백을 제공한다",
                "리컴포지션(body 재실행) 자체는 이 콜백을 다시 유발하지 않는다 — 오직 컴포지션 트리에서 실제로 빠졌다가 다시 들어올 때만 반응한다",
                "DisposableEffect 는 key 변경마다 반응하는 부수 효과 블록인 반면, RememberObserver 는 값 객체 자신의 생명주기에 묶인다",
                "onAbandoned 는 onRemembered 없이 컴포지션이 커밋 실패로 폐기될 때만 호출되어, onDispose 하나로 뭉뚱그려지는 DisposableEffect 와 구분된다",
                "rememberCoroutineScope() 등 Compose 런타임 내부에서도 같은 패턴으로 리소스 해제를 컴포지션 멤버십에 정확히 묶는 데 쓰인다"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}
