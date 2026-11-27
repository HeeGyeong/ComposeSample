package com.example.composesample.presentation.example.component.architecture.pattern.appstate

import androidx.appstate.statestore.StateStore
import androidx.appstate.transform.transform
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/*
 * AppState(#163) — androidx.appstate 1.0.0-alpha02. Compose runtime 을 UI 밖 상태 계층으로 쓰는 StateStore · transform 과
 * DataStore 동기화를 시나리오 함수(AppStateLab.kt)로 재고, 위쪽 라이브 카드에서는 화면 둘이 한 StateStore 를 나눠 쓰는 모양을 보인다.
 */

private val Accent = Color(0xFF00695C)
private val Body = Color(0xFF37474F)
private val Muted = Color(0xFF607D8B)
private val Warn = Color(0xFFC62828)

@Composable
fun AppStateExampleUI(onBackEvent: () -> Unit) {
    // 앱 계층 상태 — 실제 앱이면 Application · DI 싱글턴이 쥔다(StateStore 는 자동 싱글턴이 아니다)
    val store = remember { StateStore() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5F4)),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Header(onBackEvent) }
        item { VersionCard() }
        item { LiveStoreCard(store) }
        item { KeyIdentityCard() }
        item { AutoClearCard() }
        item { TierBranchCard() }
        item { PersistenceCard() }
        item { TestingCard() }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun Header(onBackEvent: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackEvent) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Accent)
        }
        Column {
            Text("AppState — StateStore · Transform", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Body)
            Text("androidx.appstate 1.0.0-alpha02 · UI 밖에서 도는 Compose runtime", fontSize = 12.sp, color = Muted)
        }
    }
}

/** 잰 기기 — 카드의 '잰 것' 문구 */
private const val MEASURED_ON = "Pixel 8 API 36 에뮬 debug · JVM 단위 테스트 같은 결과"

@Composable
private fun VersionCard() {
    LabCard(title = "왜 alpha02 인가") {
        Note(
            "alpha03(2026-11-18)의 transform 은 compose runtime-saveable 1.14.0-alpha01 · savedstate 1.6.0-alpha05 를 요구한다. 넣어 보니 앱 전체 해석이 " +
                "Compose runtime 묶음 9좌표 1.13.0 → 1.14.0-alpha01 · savedstate 1.5.0 → 1.6.0-alpha06 · collection 1.6.0 → 1.7.0 으로 끌어올려졌다. " +
                "이 예제는 그 대신 끌어올림이 없는 alpha02 를 쓴다(대신 kotlin-reflect 2.3.20 이 새로 들어온다). " +
                "alpha03 에서 바뀐 점 — PersistToDataStore<T> 가 이름 · KSerializer 를 직접 줘 리플렉션 제거, transform 안 rememberSaveable 용 SavedStateContainer, RunnableTransformScope.run — 은 가이드에 적었다."
        )
    }
}

// ==================== 라이브 — 화면 둘이 한 StateStore 를 나눠 쓴다 ====================

@Composable
private fun LiveStoreCard(store: StateStore) {
    // transform(컴포저블 오버로드) — 이 화면의 코루틴 scope 에 묶인 헤드리스 컴포지션이 등급을 읽어 정책을 만든다
    val policy by transform(initialValue = "준비 전") {
        when (val tier = store.getState(TierKey).value) {
            Tier.Free -> "광고 + 저화질 (${tier.name})"
            Tier.Premium -> "광고 없음 · 고화질 (${tier.name})"
        }
    }
    LabCard(title = "라이브 — 두 화면이 한 StateStore 를 읽는다") {
        Note("위 줄은 '설정 화면', 아래 줄은 '플레이어 화면' 이라고 생각한다. 둘은 서로를 모르고 같은 store 와 키만 안다.")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { store.setState(TierKey, Tier.Free) }) { Text("Free", fontSize = 12.sp) }
            OutlinedButton(onClick = { store.setState(TierKey, Tier.Premium) }) { Text("Premium", fontSize = 12.sp) }
            OutlinedButton(onClick = { store.updateState(VisitCountKey) { it + 1 } }) { Text("방문 +1", fontSize = 12.sp) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { store.setState(UserIdKey, "alice") }) { Text("alice", fontSize = 12.sp) }
            OutlinedButton(onClick = { store.setState(UserIdKey, "bob") }) { Text("bob", fontSize = 12.sp) }
            OutlinedButton(onClick = { store.updateState(CartKey) { it + "상품${it.size + 1}" } }) { Text("장바구니 +1", fontSize = 12.sp) }
        }
        Mono(
            "플레이어: ${policy}\n" +
                "방문 ${store.getState(VisitCountKey).value} · 사용자 ${store.getState(UserIdKey).value ?: "손님"}\n" +
                "장바구니 ${store.getState(CartKey).value}\n" +
                "store.keys ${store.keys.map { it::class.simpleName }}"
        )
        Note(
            "장바구니는 autoClearKey = UserIdKey — 사용자를 바꾸면 빠진다. 단 이 카드는 장바구니를 늘 읽고(getState) 있어서, " +
                "'장바구니 +1' 이 첫 setState 가 아니게 되면 감시가 안 걸린다(아래 ② 의 ReadFirst)."
        )
    }
}

// ==================== ① 키 동일성 ====================

@Composable
private fun KeyIdentityCard() {
    var result by remember { mutableStateOf<KeyIdentityResult?>(null) }
    LabCard(title = "① 키는 object 로 — StateStoreKey 는 data class 가 아니다") {
        Code(
            "// 한 개\n" +
                "object VisitCountKey : StateStoreKey<Int>(0)\n" +
                "// 인스턴스마다 다른 키\n" +
                "class VisitCountClassKey : StateStoreKey<Int>(0)\n\n" +
                "store.setState(VisitCountKey, 3)\n" +
                "store.setState(VisitCountClassKey(), 3)\n" +
                "store.getState(VisitCountClassKey()).value // ?"
        )
        RunButton("실행", tag = "runKeyIdentity") { result = runKeyIdentity() }
        result?.let {
            Mono("object 키 다시 읽기 ${it.objectRead} · class 키 새 인스턴스로 읽기 ${it.classRead} · store.keys ${it.keyCount}개")
        }
        Facts(
            "object 키 3 · class 키 새 인스턴스 0 · keys 3 — StateStore 는 키를 SnapshotStateMap 의 키로 쓰고, StateStoreKey 는 equals 를 고치지 않은 open class 다. " +
                "같은 칸을 원하면 object(또는 data object)로, class 키라면 그 인스턴스 하나를 들고 다녀야 한다."
        )
    }
}

// ==================== ② 자동 정리 ====================

@Composable
private fun AutoClearCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<AutoClearResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }
    LabCard(title = "② autoClearKey — 사용자가 바뀌면 장바구니 지우기") {
        Code(
            "object CartKey : StateStoreKey<List<String>>(\n" +
                "    emptyList(), autoClearKey = UserIdKey)\n" +
                "// 첫 setState 때 감시를 걸고 그때의 UserIdKey 값을 기억한다"
        )
        RunButton(if (running) "실행 중…" else "네 경우 실행", tag = "runAutoClear", enabled = !running) {
            running = true
            scope.launch {
                results = AutoClearCase.entries.map { runAutoClear(it) }
                running = false
            }
        }
        results.forEach { r ->
            Mono(
                "${r.case.name}: ${r.case.label}\n  → ${if (r.cleared) "지워짐(${r.clearedAfterMillis}ms)" else "남음 ${r.cartAfter}"} · 남은 Recomposer +${r.leftoverRecomposers}",
                color = if (r.case == AutoClearCase.ReadFirst || r.case == AutoClearCase.SameUser) Warn else Body
            )
        }
        Facts(
            "Plain 8ms(JVM 3ms) 뒤 지워짐. ReadFirst 는 안 지워진다 — getState 가 기본값으로 키를 먼저 넣으면 setState 의 '처음 넣을 때만 감시' 조건(!contains)이 거짓이 된다. " +
                "위 라이브 카드가 바로 이 상태라 사용자를 바꿔도 장바구니가 남는다.",
            "Guest(null → alice)는 5ms 뒤 지워진다 — 감시는 첫 setState 때의 값과 비교하므로 손님 장바구니를 로그인 뒤로 넘기려면 이 방식은 안 맞다.",
            "SameUser 는 남고 Recomposer 하나가 남는다 — 감시는 Dispatchers.Unconfined · SupervisorJob scope 의 헤드리스 컴포지션이라, 정리되기 전까지 store 와 함께 " +
                "Recomposer.runningRecomposers 에 계속 남는다. 키마다 하나씩이다."
        )
    }
}

// ==================== ③ transform 분기 ====================

@Composable
private fun TierBranchCard() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<TierBranchResult?>(null) }
    var running by remember { mutableStateOf(false) }
    LabCard(title = "③ transform — 떠난 분기는 수집도 멈춘다 (Flow 판과 나란히)") {
        Code(
            "// 헤드리스 컴포지션\n" +
                "transform(\"준비 전\", scope) {\n" +
                "  when (store.getState(TierKey).value) {\n" +
                "    Free -> \"광고 + 저화질\"\n" +
                "    Premium -> {\n" +
                "      // 이 분기에 있을 때만 수집\n" +
                "      val q by quality.collectAsState()\n" +
                "      \"광고 없음 · \$q\" } } }\n\n" +
                "snapshotFlow { store.getState(TierKey).value }\n" +
                "  .flatMapLatest { if (it == Premium) quality.map { … } else flowOf(…) }\n" +
                "  .stateIn(scope, Eagerly, \"준비 전\")"
        )
        RunButton(if (running) "실행 중…" else "실행", tag = "runTierBranch", enabled = !running) {
            running = true
            scope.launch {
                result = runTierBranch()
                running = false
            }
        }
        result?.let { r ->
            r.steps.forEach { s ->
                Mono(
                    "${s.action}\n  transform '${s.transformValue}' 수집 ${s.transformCollectors} · 다시 돎 ${s.transformRuns} · ${s.transformMillis ?: "-"}ms\n" +
                        "  Flow      '${s.flowValue}' 수집 ${s.flowCollectors} · ${s.flowMillis ?: "-"}ms"
                )
            }
            Note("transform 블록이 돈 스레드: ${r.transformThread}")
        }
        Facts(
            "값과 수집자 수는 두 판이 같다(0 → 1 → 1 → 0 → 0). Free 로 가면 Premium 분기가 컴포지션을 떠나며 collectAsState 의 수집이 취소된다 — Flow 판의 flatMapLatest 와 같은 효과를 when 분기로 쓴다.",
            "Free 인 채 화질이 바뀌어도 transform 블록은 다시 돌지 않는다(읽지 않는 값). 블록은 기본 dispatcher 인 Dispatchers.Default 워커에서 돈다.",
            "도착 시간은 세 번 재서 둘 다 0~41ms 로 흔들렸다(5ms 간격 확인) — 어느 쪽이 빠르다고 말할 수 없다."
        )
    }
}

// ==================== ④ DataStore 동기화 ====================

@Composable
private fun PersistenceCard() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var result by remember { mutableStateOf<PersistResult?>(null) }
    var running by remember { mutableStateOf(false) }
    LabCard(title = "④ syncToDataStore — 같은 파일로 앱을 세 번 켠다") {
        Code(
            "object ThemeKey : StateStoreKey<String>(\"system\"), PersistToDataStore\n" +
                "object ProfileKey : StateStoreKey<Profile?>(null), PersistToDataStore\n" +
                "object SavedCartKey : StateStoreKey<List<String>>(emptyList(),\n" +
                "    autoClearKey = UserIdKey), PersistToDataStore\n\n" +
                "scope.launch { store.syncToDataStore(file.absolutePath, scope) }"
        )
        RunButton(if (running) "실행 중… (약 5초)" else "실행", tag = "runPersistence", enabled = !running) {
            running = true
            scope.launch {
                result = runPersistence(context.filesDir)
                running = false
            }
        }
        result?.let { r ->
            Mono("kotlin-reflect allSupertypes 첫 호출 ${"%.1f".format(java.util.Locale.ROOT, r.reflectMillis)}ms (앱을 새로 켠 뒤 처음 누르면 초기화 비용 포함)")
            r.steps.forEach { Mono("${it.label}\n  ${it.detail}") }
            Note("파일 안 키 이름(class qualifiedName): ${r.keyNames.joinToString { it.substringAfterLast('.') }} — 전체 이름 ${r.keyNames.firstOrNull() ?: ""}")
            Mono(r.fileAfterFirstRun.replace(Regex("com\\.example\\.[\\w.]+\\.(\\w+Key)"), "…$1"))
        }
        Facts(
            "로그아웃(프로필 null)은 파일에 안 쓰인다 — syncToDataStore 는 value != null 일 때만 쓴다. 자동 정리로 빠진 장바구니도 파일에 남는다(keys 순회에서 빠질 뿐 지우지 않는다). 2회차에 둘 다 되살아났다.",
            "복원은 키가 저장소에 들어온 뒤(누가 getState 한 뒤)에야 한다 — 처음엔 기본값 'system' 이 보이고 5ms 뒤 'dark'. 아무도 읽지 않은 키는 복원되지 않는다.",
            "복원된 장바구니는 getState 가 먼저 넣은 키라 자동 정리 감시가 없다 — 2회차에 사용자를 바꿔도 남았다.",
            "3회차: 복원이 끝나기 전에 쓴 'light' 를 복원이 'dark' 로 덮었다. 켜자마자 쓰는 값은 잃을 수 있다.",
            "저장 이름은 키 class 의 qualifiedName — 키 이름 · 패키지를 바꾸거나 R8 이 이름을 바꾸면 옛 값을 못 찾는다(소스 — 이 앱 release 는 축소가 꺼져 있다).",
            "alpha02 는 값 타입을 kotlin-reflect(allSupertypes)로 찾는다 — 새로 켠 앱의 첫 호출 91.6ms, 그 뒤 첫 저장 44ms(리플렉션이 첫 저장에 섞이면 510ms)."
        )
    }
}

// ==================== ⑤ 테스트 ====================

@Composable
private fun TestingCard() {
    LabCard(title = "⑤ transform-testing — 프레임을 손으로 민다") {
        Code(
            "@Test fun policy() = transformTest {\n" +
                "  val scope = TransformScope()\n" +
                "  val policy = scope.launchTransform(\"\") { … }\n" +
                "  backgroundScope.launch(testDispatcher) { scope.run() }\n" +
                "  advance()  // 대기 중인 재구성 한 번\n" +
                "  store.setState(TierKey, Premium)\n" +
                "  advance()\n" +
                "  assertEquals(\"광고 없음 · HD\", policy.value) }"
        )
        Note("AppStateLabTest 가 이 모양 그대로 돈다(JVM 단위 테스트 — 기기 불필요).")
        Facts(
            "JVM 단위 테스트에서는 android.os.Trace 를 목으로 막아야 한다 — 안 막으면 transform · 자동 정리 감시 · transformTest 의 컴포지션이 'Method beginSection in android.os.Trace not mocked' 로 죽는다. " +
                "예외는 라이브러리 scope 안에서 stderr 로만 나가 테스트에는 '안 지워짐 · 값이 그대로' 로만 보였다."
        )
    }
}

// ==================== 공용 ====================

@Composable
private fun LabCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Accent)
            content()
        }
    }
}

@Composable
private fun Facts(vararg facts: String) {
    Text("잰 것 ($MEASURED_ON)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Body)
    facts.forEach { Note("• $it") }
}

@Composable
private fun RunButton(label: String, tag: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.testTag(tag)) {
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun Code(code: String) {
    Text(
        code,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF263238),
        lineHeight = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECEFF1), RoundedCornerShape(8.dp))
            .padding(8.dp)
    )
}

@Composable
private fun Mono(text: String, color: Color = Body) {
    Text(text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = color, lineHeight = 15.sp)
}

@Composable
private fun Note(text: String) {
    Row {
        Spacer(Modifier.width(2.dp))
        Text(text, fontSize = 11.sp, color = Muted, lineHeight = 16.sp)
    }
}
