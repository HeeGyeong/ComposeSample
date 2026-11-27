package com.example.composesample.presentation.example.component.architecture.pattern.appstate

import androidx.appstate.datastore.PersistToDataStore
import androidx.appstate.datastore.syncToDataStore
import androidx.appstate.statestore.StateStore
import androidx.appstate.statestore.StateStoreKey
import androidx.appstate.transform.transform
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.full.allSupertypes
import kotlin.time.TimeSource
import kotlin.time.measureTime

/*
 * AppState(#163) — androidx.appstate statestore · statestore-datastore · transform 1.0.0-alpha02.
 * 화면(AppStateExampleUI)과 JVM 단위 테스트(AppStateLabTest)가 같은 시나리오 함수를 부른다 — 화면 없이 도는 Compose runtime 이라 JVM 에서도 그대로 돈다.
 */

// ==================== 키 ====================

/** 방문 수 — object 키(한 개뿐이라 어디서 읽어도 같은 칸) */
object VisitCountKey : StateStoreKey<Int>(0)

/** 같은 뜻의 class 키 — StateStoreKey 는 data class 가 아니라 open class 라 인스턴스마다 다른 키가 된다(함정) */
class VisitCountClassKey : StateStoreKey<Int>(0)

/** 로그인한 사용자 — 바뀌면 장바구니가 지워지도록 다른 키의 autoClearKey 로 쓴다 */
object UserIdKey : StateStoreKey<String?>(null)

/** 사용자 장바구니 — UserIdKey 값이 바뀌면 저장소에서 빠진다 */
object CartKey : StateStoreKey<List<String>>(emptyList(), autoClearKey = UserIdKey)

/** 구독 등급 */
enum class Tier { Free, Premium }

object TierKey : StateStoreKey<Tier>(Tier.Free)

/** DataStore 로 남기는 값 — 값 타입은 @Serializable 이어야 한다 */
@Serializable
data class Profile(val name: String, val premium: Boolean)

object ThemeKey : StateStoreKey<String>("system"), PersistToDataStore
object ProfileKey : StateStoreKey<Profile?>(null), PersistToDataStore
object SavedCartKey : StateStoreKey<List<String>>(emptyList(), autoClearKey = UserIdKey), PersistToDataStore

/** 조건이 될 때까지 기다린다(타임아웃이면 null) — 리스너 · 헤드리스 컴포지션은 Default 스레드에서 비동기로 돈다 */
private suspend fun awaitUntil(timeoutMillis: Long, condition: () -> Boolean): Long? {
    val mark = TimeSource.Monotonic.markNow()
    return withTimeoutOrNull(timeoutMillis) {
        while (!condition()) delay(5)
        mark.elapsedNow().inWholeMilliseconds
    }
}

private fun runningRecomposers(): Int = Recomposer.runningRecomposers.value.size

// ==================== ① 키 동일성 ====================

data class KeyIdentityResult(val objectRead: Int, val classRead: Int, val keyCount: Int)

/** object 키는 다시 읽으면 3, class 키는 새 인스턴스로 읽으면 기본값 0 — 저장소에는 키가 3개(object 1 + class 인스턴스 2) */
fun runKeyIdentity(): KeyIdentityResult {
    val store = StateStore()
    store.setState(VisitCountKey, 3)
    store.setState(VisitCountClassKey(), 3)
    return KeyIdentityResult(
        objectRead = store.getState(VisitCountKey).value,
        classRead = store.getState(VisitCountClassKey()).value,
        keyCount = store.keys.size
    )
}

// ==================== ② 자동 정리(autoClearKey) ====================

enum class AutoClearCase(val label: String) {
    Plain("로그인(alice) → 담기 → 사용자 bob"),
    ReadFirst("로그인 → 화면이 먼저 getState → 담기 → bob"),
    Guest("손님(null)일 때 담기 → 로그인 alice"),
    SameUser("로그인 alice → 담기 → 다시 alice")
}

data class AutoClearResult(
    val case: AutoClearCase,
    val cleared: Boolean,
    val clearedAfterMillis: Long?,
    val cartAfter: List<String>,
    /** 시나리오 동안 늘어난 Recomposer 수 — 정리되지 않은 키는 감시용 헤드리스 컴포지션이 남는다 */
    val leftoverRecomposers: Int
)

/** 첫 setState 때 감시가 걸리고(그때의 사용자 값을 기억), 사용자 값이 그와 달라지면 키를 지운다 */
suspend fun runAutoClear(case: AutoClearCase, waitMillis: Long = 1_000): AutoClearResult {
    val before = runningRecomposers()
    val store = StateStore()
    if (case != AutoClearCase.Guest) store.setState(UserIdKey, "alice")
    if (case == AutoClearCase.ReadFirst) store.getState(CartKey) // 화면이 장바구니를 먼저 그렸다
    store.setState(CartKey, listOf("사과", "배"))
    store.setState(UserIdKey, if (case == AutoClearCase.Plain || case == AutoClearCase.ReadFirst) "bob" else "alice")
    val clearedAfter = awaitUntil(waitMillis) { CartKey !in store.keys }
    delay(50) // 정리 뒤 감시 scope 가 취소되는 시간
    return AutoClearResult(
        case = case,
        cleared = clearedAfter != null,
        clearedAfterMillis = clearedAfter,
        cartAfter = store.getState(CartKey).value,
        leftoverRecomposers = runningRecomposers() - before
    )
}

// ==================== ③ transform 분기 — 떠난 분기는 수집도 멈춘다 ====================

data class TierStep(
    val action: String,
    val transformValue: String,
    val flowValue: String,
    /** 화질 흐름을 수집 중인 수 — transform 판 · Flow 판 따로 */
    val transformCollectors: Int,
    val flowCollectors: Int,
    /** 이 단계에서 transform 블록이 다시 돈 횟수 */
    val transformRuns: Int,
    val transformMillis: Long?,
    val flowMillis: Long?
)

data class TierBranchResult(val steps: List<TierStep>, val transformThread: String)

private fun policyOf(tier: Tier, quality: String?): String =
    if (tier == Tier.Premium) "광고 없음 · $quality" else "광고 + 저화질"

/**
 * 같은 정책을 transform(헤드리스 컴포지션 — when 분기 안에서 collectAsState)과 Flow(flatMapLatest + stateIn)로 만들어
 * 등급 · 화질을 바꿔 가며 값 · 수집자 수 · 다시 돈 횟수를 잰다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun runTierBranch(): TierBranchResult {
    val store = StateStore()
    val qualityForTransform = MutableStateFlow("HD")
    val qualityForFlow = MutableStateFlow("HD")
    val runs = AtomicInteger()
    var thread = ""
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    try {
        val transformed = transform(initialValue = "준비 전", scope = scope) {
            runs.incrementAndGet()
            thread = Thread.currentThread().name
            when (val tier = store.getState(TierKey).value) {
                Tier.Free -> policyOf(tier, null)
                Tier.Premium -> {
                    // 이 분기가 컴포지션에 있는 동안만 수집한다 — Free 로 바뀌면 분기가 빠지며 수집이 취소된다
                    val quality by qualityForTransform.collectAsState()
                    policyOf(tier, quality)
                }
            }
        }
        val flowed = snapshotFlow { store.getState(TierKey).value }
            .flatMapLatest { tier ->
                if (tier == Tier.Premium) qualityForFlow.map { policyOf(tier, it) } else flowOf(policyOf(tier, null))
            }
            .stateIn(scope, SharingStarted.Eagerly, "준비 전")

        val steps = mutableListOf<TierStep>()
        suspend fun step(action: String, expected: String, change: () -> Unit) {
            val runsBefore = runs.get()
            change()
            // 둘을 동시에 기다려 각자 도착한 시간을 잰다(앞의 것을 기다리는 동안 뒤의 것이 끝나 0ms 로 나오지 않게)
            val (t, f) = coroutineScope {
                val tWait = async { awaitUntil(1_000) { transformed.value == expected } }
                val fWait = async { awaitUntil(1_000) { flowed.value == expected } }
                tWait.await() to fWait.await()
            }
            delay(60) // 취소된 수집이 subscriptionCount 에 반영될 시간
            steps += TierStep(
                action = action,
                transformValue = transformed.value,
                flowValue = flowed.value,
                transformCollectors = qualityForTransform.subscriptionCount.value,
                flowCollectors = qualityForFlow.subscriptionCount.value,
                transformRuns = runs.get() - runsBefore,
                transformMillis = t,
                flowMillis = f
            )
        }
        step("시작(Free)", policyOf(Tier.Free, null)) {}
        step("Premium 으로", policyOf(Tier.Premium, "HD")) { store.setState(TierKey, Tier.Premium) }
        step("화질 4K", policyOf(Tier.Premium, "4K")) {
            qualityForTransform.value = "4K"
            qualityForFlow.value = "4K"
        }
        step("Free 로", policyOf(Tier.Free, null)) { store.setState(TierKey, Tier.Free) }
        step("Free 인 채 화질 SD", policyOf(Tier.Free, null)) {
            qualityForTransform.value = "SD"
            qualityForFlow.value = "SD"
        }
        return TierBranchResult(steps, thread)
    } finally {
        scope.coroutineContext.job.cancelAndJoin()
    }
}

// ==================== ④ DataStore 동기화 ====================

data class PersistStep(val label: String, val detail: String)

data class PersistResult(
    val steps: List<PersistStep>,
    val fileAfterFirstRun: String,
    val keyNames: List<String>,
    /** alpha02 가 값 타입을 찾는 kotlin-reflect allSupertypes 를 이 실행에서 처음 부른 시간 — 프로세스에서 처음이면 리플렉션 초기화가 든다 */
    val reflectMillis: Double
)

private fun readJson(file: File): String = if (file.exists()) file.readText() else "(파일 없음)"

/** 실행 한 번 = StateStore 하나 + syncToDataStore 하나. 같은 파일에 DataStore 를 둘 열 수 없어 끝날 때 scope 를 닫고 기다린다 */
private class Launch(val store: StateStore, private val scope: CoroutineScope) {
    suspend fun close() = scope.coroutineContext.job.cancelAndJoin()
}

private fun launchSynced(file: File): Launch {
    val store = StateStore()
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    scope.launch { store.syncToDataStore(file.absolutePath, scope) }
    return Launch(store, scope)
}

/**
 * 같은 파일로 세 번 '앱을 켠다'.
 * 1회차: 테마 · 프로필 · 장바구니를 저장한 뒤 로그아웃(프로필 null) · 사용자 변경(장바구니 자동 정리)
 * 2회차: 키를 읽어 복원 — 지운 줄 안 프로필 · 장바구니가 되살아나는지
 * 3회차: 복원 전에 쓴 값(테마 light)이 복원에 덮이는지
 */
suspend fun runPersistence(dir: File): PersistResult {
    val file = File(dir, "appstate_demo.json")
    file.delete()
    val steps = mutableListOf<PersistStep>()
    // 라이브러리(StateStoreKey.getValueType)와 같은 호출 — 이게 처음이면 아래 첫 저장 시간에서 리플렉션 초기화가 빠진다
    val reflect = TimeSource.Monotonic.measureTime { ProfileKey::class.allSupertypes }

    // 1회차
    val first = launchSynced(file)
    first.store.setState(ThemeKey, "dark")
    first.store.setState(ProfileKey, Profile("민지", premium = true))
    first.store.setState(UserIdKey, "alice")
    first.store.setState(SavedCartKey, listOf("사과"))
    val saved = awaitUntil(3_000) { readJson(file).let { "ThemeKey" in it && "ProfileKey" in it && "SavedCartKey" in it } }
    steps += PersistStep("1회차 저장", "세 키가 파일에 쓰이기까지 ${saved ?: "3,000+"}ms")
    first.store.setState(ProfileKey, null) // 로그아웃
    first.store.setState(UserIdKey, "bob") // 장바구니 자동 정리
    val cleared = awaitUntil(1_000) { SavedCartKey !in first.store.keys }
    delay(500) // 쓰기가 있다면 끝날 시간
    val fileAfterFirst = readJson(file)
    steps += PersistStep(
        "1회차 로그아웃 · 사용자 변경",
        "저장소: 프로필 null · 장바구니 ${if (cleared != null) "정리됨(${cleared}ms)" else "그대로"} → 파일: 프로필 ${if ("민지" in fileAfterFirst) "민지 그대로" else "지워짐"} · 장바구니 ${if ("사과" in fileAfterFirst) "사과 그대로" else "지워짐"}"
    )
    first.close()

    // 2회차 — 키가 저장소에 들어와야(읽혀야) 복원된다
    val second = launchSynced(file)
    val themeState = second.store.getState(ThemeKey)
    val profileState = second.store.getState(ProfileKey)
    val cartState = second.store.getState(SavedCartKey)
    val firstSeen = themeState.value
    val restored = awaitUntil(3_000) { themeState.value == "dark" && profileState.value != null && cartState.value.isNotEmpty() }
    steps += PersistStep(
        "2회차 복원",
        "처음 읽은 테마 '$firstSeen'(기본값) → ${restored ?: "3,000+"}ms 뒤 '${themeState.value}' · 프로필 ${profileState.value?.name ?: "null"} · 장바구니 ${cartState.value}"
    )
    second.store.setState(UserIdKey, "carol")
    val reCleared = awaitUntil(800) { SavedCartKey !in second.store.keys }
    steps += PersistStep(
        "2회차 사용자 변경",
        "복원된 장바구니는 ${if (reCleared != null) "다시 정리됨" else "그대로(자동 정리 감시 없음 — getState 가 먼저 키를 넣었다)"}"
    )
    second.close()

    // 3회차 — 복원 전에 쓴 값
    val third = launchSynced(file)
    third.store.setState(ThemeKey, "light")
    delay(1_000)
    steps += PersistStep(
        "3회차 복원 전 쓰기",
        "앱이 쓴 'light' → 1초 뒤 저장소 '${third.store.getState(ThemeKey).value}' · 파일 ${if ("light" in readJson(file)) "light" else "dark 그대로"}"
    )
    third.close()

    val keyNames = Regex("\"([\\w.]+Key)\"").findAll(fileAfterFirst).map { it.groupValues[1] }.toList()
    return PersistResult(steps, fileAfterFirst, keyNames, reflect.inWholeMicroseconds / 1000.0)
}
