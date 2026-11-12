package com.example.composesample.presentation.example.component.ui.layout.insets

// ==================== 요청 · 플래그 ====================

/**
 * 바 위에 그려지는 아이콘·글자의 색.
 * - [Dark] = 어두운 아이콘 → 밝은 배경용. 플랫폼 플래그로는 `isAppearanceLight* = true`
 * - [Light] = 밝은 아이콘 → 어두운 배경용. `isAppearanceLight* = false`
 * - [Unspecified] = 이 바는 지정하지 않음 — 아래 요청(없으면 원래 값)을 그대로 둔다
 */
enum class BarContent(val label: String) {
    Unspecified("지정 없음"),
    Light("밝은 아이콘"),
    Dark("어두운 아이콘")
}

/** 한 화면이 선언한 요청. 바마다 따로 지정한다. */
data class BarRequest(
    val statusBar: BarContent = BarContent.Unspecified,
    val navigationBar: BarContent = BarContent.Unspecified
) {
    /** [base] 위에 이 요청을 덮은 결과 — 지정하지 않은 바는 [base] 그대로 */
    fun applyTo(base: BarFlags): BarFlags = BarFlags(
        lightStatus = statusBar.toFlagOr(base.lightStatus),
        lightNavigation = navigationBar.toFlagOr(base.lightNavigation)
    )
}

private fun BarContent.toFlagOr(fallback: Boolean): Boolean = when (this) {
    BarContent.Dark -> true
    BarContent.Light -> false
    BarContent.Unspecified -> fallback
}

/** 창의 실제 플래그(`WindowInsetsControllerCompat.isAppearanceLight*`). true = 어두운 아이콘 */
data class BarFlags(val lightStatus: Boolean, val lightNavigation: Boolean)

fun Boolean.iconLabel(): String = if (this) "어두운 아이콘" else "밝은 아이콘"

/** 창 하나의 플래그를 읽고 쓰는 곳 — 기기에서는 WindowInsetsControllerCompat, 테스트에서는 가짜 창 */
interface BarWindow {
    var flags: BarFlags
}

// ==================== ⓐ 저장·복원 ====================

/**
 * 순진한 선언 — 들어올 때 지금 값을 저장하고 요청을 적용, 나갈 때 저장해 둔 값으로 되돌린다.
 *
 * 들어오고 나가는 순서가 "나가는 화면 먼저"일 때만 맞다. 전환 애니메이션은 두 화면을 함께 컴포지션에 두므로
 * 들어오는 화면이 먼저 적용하고 나가는 화면이 나중에 되돌린다 — 그 되돌림이 새 화면의 요청을 덮고,
 * 새 화면은 '옛 화면이 적용한 값'을 원래 값으로 저장해 둔다.
 */
class SaveRestoreDeclaration(
    private val window: BarWindow,
    private val request: BarRequest
) {
    private var saved: BarFlags? = null

    /** 저장한 값과 적용한 값 */
    fun enter(): Pair<BarFlags, BarFlags> {
        val before = window.flags
        saved = before
        val applied = request.applyTo(before)
        window.flags = applied
        return before to applied
    }

    /** 되돌린 값(들어온 적이 없으면 null) */
    fun leave(): BarFlags? {
        val restore = saved ?: return null
        window.flags = restore
        saved = null
        return restore
    }
}

// ==================== ⓑ 요청 스택 ====================

/**
 * 창 하나의 요청 목록. 들어온 순서를 지키고, 바마다 **가장 최근에 그 바를 지정한 요청**이 이긴다.
 * 어느 순서로 빠지든 남은 요청으로 다시 계산하고, 다 빠지면 첫 요청 직전에 읽어 둔 원래 값으로 돌아간다.
 *
 * 원문 라이브러리(composablehorizons/systembars `WindowSystemBars`)와 같은 규칙이다 — 이미 있는 요청의 값이
 * 바뀌어도 순서는 그대로다(다시 그리기만으로 '가장 최근'이 바뀌지 않게).
 */
class BarRequestStack(private val window: BarWindow) {
    /** 첫 요청 직전의 값 — 스택이 비면 여기로 돌아간다 */
    val original: BarFlags = window.flags

    private val requests = LinkedHashMap<Any, BarRequest>()

    val isEmpty: Boolean get() = requests.isEmpty()

    /** 들어온 순서대로 (키, 요청) */
    val entries: List<Pair<Any, BarRequest>> get() = requests.map { it.key to it.value }

    /** 새 키는 맨 위에 쌓고, 있는 키는 자리를 지킨 채 값만 바꾼다 */
    fun put(key: Any, request: BarRequest): BarFlags {
        requests[key] = request
        return apply()
    }

    fun remove(key: Any): BarFlags {
        requests.remove(key)
        return apply()
    }

    /** 바마다 가장 최근의 지정값, 없으면 원래 값 */
    fun resolve(): BarFlags {
        val ordered = requests.values.reversed()
        val status = ordered.firstOrNull { it.statusBar != BarContent.Unspecified }?.statusBar
        val navigation = ordered.firstOrNull { it.navigationBar != BarContent.Unspecified }?.navigationBar
        return BarRequest(
            statusBar = status ?: BarContent.Unspecified,
            navigationBar = navigation ?: BarContent.Unspecified
        ).applyTo(original)
    }

    private fun apply(): BarFlags {
        val resolved = resolve()
        window.flags = resolved
        return resolved
    }
}

/**
 * 창마다 스택 하나. 첫 요청이 올 때 만들고(그 순간의 값이 원래 값), 마지막 요청이 빠지면 지운다.
 * 키는 창 객체 — 액티비티 창과 다이얼로그 창은 서로 다른 스택을 갖는다.
 */
class BarRequestRegistry {
    private val stacks = LinkedHashMap<Any, BarRequestStack>()

    fun stackOf(windowKey: Any): BarRequestStack? = stacks[windowKey]

    fun put(windowKey: Any, window: BarWindow, requestKey: Any, request: BarRequest): BarFlags =
        stacks.getOrPut(windowKey) { BarRequestStack(window) }.put(requestKey, request)

    fun remove(windowKey: Any, requestKey: Any): BarFlags? {
        val stack = stacks[windowKey] ?: return null
        val restored = stack.remove(requestKey)
        if (stack.isEmpty) stacks.remove(windowKey)
        return restored
    }
}

// ==================== 무대(시나리오) ====================

/** 무대에 올라가는 화면. [request] 가 그 화면의 선언이다 */
enum class StagePage(val label: String, val request: BarRequest) {
    Home("홈", BarRequest()),
    DarkHeader("어두운 헤더 화면", BarRequest(statusBar = BarContent.Light)),
    LightPhoto("밝은 사진 화면", BarRequest(statusBar = BarContent.Dark));

    /** 홈처럼 아무 바도 지정하지 않은 화면은 선언하지 않는다 — 저장·복원도 하지 않는다 */
    val declares: Boolean get() = request != BarRequest()
}

/** 선언 방식 */
enum class DeclarationMode(val label: String) {
    SaveRestore("저장·복원"),
    Stack("요청 스택")
}

/** 무대의 화면 교체 방식 */
enum class TransitionMode(val label: String) {
    Immediate("즉시 교체"),
    Animated("AnimatedContent"),
    NavDisplay("NavDisplay")
}

/** 자동 대조 시나리오 — 홈에서 출발해 [steps] 를 차례로 지나 홈으로 돌아온다 */
enum class StageScenario(val label: String, val steps: List<StagePage>) {
    DarkThenLight("① 홈 → 어두운 헤더 → 밝은 사진 → 홈", listOf(StagePage.DarkHeader, StagePage.LightPhoto, StagePage.Home)),
    LightThenDark("② 홈 → 밝은 사진 → 어두운 헤더 → 홈", listOf(StagePage.LightPhoto, StagePage.DarkHeader, StagePage.Home))
}

/** 화면에 보이는 것이 [visible] 일 때 바가 가져야 할 값 — 그 화면의 요청, 지정 없는 바는 원래 값 */
fun expectedFlags(original: BarFlags, visible: StagePage): BarFlags = visible.request.applyTo(original)

/** 화면이 바뀔 때 두 선언이 도는 순서 */
enum class SwapOrder {
    /** 나가는 화면의 onDispose 가 먼저 — 한 컴포지션 안에서 교체될 때 */
    LeaveFirst,

    /** 들어오는 화면의 효과가 먼저 — 전환 애니메이션 동안 두 화면이 함께 있을 때 */
    EnterFirst
}

/**
 * 가짜 창 위에서 시나리오를 돌려 단계마다 (보인 화면, 기대값, 실제값) 을 낸다.
 * 기기의 자동 대조가 이 예측과 같은지 본다 — 순서([order])만 실제 전환 방식에서 오는 값이다.
 */
fun simulateScenario(
    scenario: StageScenario,
    mode: DeclarationMode,
    order: SwapOrder,
    original: BarFlags
): List<Triple<StagePage, BarFlags, BarFlags>> {
    val window = object : BarWindow {
        override var flags: BarFlags = original
    }
    val registry = BarRequestRegistry()
    val live = HashMap<StagePage, SaveRestoreDeclaration>()

    fun enter(page: StagePage) {
        if (!page.declares) return
        when (mode) {
            DeclarationMode.SaveRestore ->
                live[page] = SaveRestoreDeclaration(window, page.request).also { it.enter() }
            DeclarationMode.Stack -> registry.put(window, window, page, page.request)
        }
    }

    fun leave(page: StagePage) {
        if (!page.declares) return
        when (mode) {
            DeclarationMode.SaveRestore -> live.remove(page)?.leave()
            DeclarationMode.Stack -> registry.remove(window, page)
        }
    }

    var current = StagePage.Home
    enter(current)
    return scenario.steps.map { next ->
        when (order) {
            SwapOrder.LeaveFirst -> { leave(current); enter(next) }
            SwapOrder.EnterFirst -> { enter(next); leave(current) }
        }
        current = next
        Triple(next, expectedFlags(original, next), window.flags)
    }
}
