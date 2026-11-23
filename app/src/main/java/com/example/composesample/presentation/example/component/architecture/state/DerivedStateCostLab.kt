package com.example.composesample.presentation.example.component.architecture.state

import android.content.Context
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.computedStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

// ==================== derivedStateOf 의 숨은 비용(#176) ====================
// 원문(RevenueCat 페이월)의 구조를 줄인 모형: 칸마다 상태 보관자가 있고, 오버라이드를 해석하는 기반 derivedStateOf 1 개
// 위에 화면이 읽는 속성 derivedStateOf 4 개가 쌓인다. 탭 하나가 어떤 계산을 몇 번 부르는지 센다.

/** 플랜(패키지) 식별자 */
object PaywallPackages {
    const val Monthly = "monthly"
    const val Annual = "annual"
}

/** 오버라이드 조건 — 원문 SDK 의 ComponentOverride.Condition 중 선택에 관한 두 가지 */
enum class PartCondition {
    /** 이 칸이 속한 패키지가 선택됨(플랜 카드 강조) */
    Selected,

    /** 선택된 패키지가 연간(칸 위치와 무관) */
    AnnualSelected
}

/** 페이월의 한 칸 — overrides 가 비면 선택과 무관한 평범한 텍스트 */
data class PaywallPart(
    val id: Int,
    val label: String,
    val packageId: String? = null,
    val overrides: List<PartCondition> = emptyList()
)

/** 내려받은 폰트 명세(원문 FontSpec.Downloaded 자리) — 이름이 같으면 같은 명세 */
data class LabFontSpec(val name: String)

/** 오버라이드를 합친 결과(원문 presentedPartial 자리). data class 라 내용이 같으면 equals 가 true */
data class PresentedLook(
    val visible: Boolean,
    val emphasized: Boolean,
    val bold: Boolean,
    val font: LabFontSpec
)

/** 선택에 의존하는 칸 수 — 플랜 카드 2 + 연간 안내 1 */
const val SELECTION_DEPENDENT_PARTS = 3

/** 칸마다 기반 위에 쌓은 속성 derivedStateOf 수 — visible · 강조 · 굵기 · fontFamily */
const val DERIVED_PROPERTIES_PER_PART = 4

/** 플랜 카드 2 · 연간 안내 1 · 선택과 무관한 텍스트 [plainCount] 개 */
fun paywallParts(plainCount: Int): List<PaywallPart> = buildList {
    add(PaywallPart(0, "월간", PaywallPackages.Monthly, listOf(PartCondition.Selected)))
    add(PaywallPart(1, "연간", PaywallPackages.Annual, listOf(PartCondition.Selected)))
    add(PaywallPart(2, "연간이면 2개월 무료", overrides = listOf(PartCondition.AnnualSelected)))
    repeat(plainCount) { add(PaywallPart(SELECTION_DEPENDENT_PARTS + it, "t${it + 1}")) }
}

/**
 * 오버라이드 해석 — 조건 입력을 값이 아니라 람다로 받고, 그 조건을 검사하는 분기에서만 부른다(원문 #4383 앞 절반).
 * derivedStateOf 는 계산이 '실제로 읽은' 상태만 의존성으로 모으므로, 부르지 않은 람다가 읽을 상태는 의존성에 들어가지 않는다.
 */
fun resolveLook(
    part: PaywallPart,
    font: LabFontSpec,
    isSelected: () -> Boolean,
    annualSelected: () -> Boolean
): PresentedLook {
    var emphasized = false
    var bold = false
    part.overrides.forEach { condition ->
        when (condition) {
            PartCondition.Selected -> if (isSelected()) {
                emphasized = true
                bold = true
            }

            PartCondition.AnnualSelected -> if (annualSelected()) emphasized = true
        }
    }
    return PresentedLook(visible = true, emphasized = emphasized, bold = bold, font = font)
}

/** 칸 상태를 만드는 API — Compose 1.13 의 computedStateOf 는 결과를 캐시하지 않는다(#185) */
enum class StateApi(val label: String) {
    Derived("derivedStateOf"),
    Computed("computedStateOf")
}

/**
 * 킬 스위치 — 원문처럼 '고친 코드'를 기준으로 두고 스위치 하나씩 옛 동작을 되살린다.
 * @param eagerReads true = 옛 코드: 해석 전에 조건 입력을 전부 읽는다(조건 없는 칸도 선택에 의존)
 * @param basePolicy true = 기반 derivedStateOf 에 structuralEqualityPolicy(원문 SDK 는 정책 없음).
 *   computedStateOf 에는 '정책 없음' 이 없다(기본 structuralEqualityPolicy) — Computed 에서는 효과가 없다
 * @param fontEquals false = 옛 코드: equals 없는 폰트(원문 #4245 이전 LocalFileFont)
 * @param stateApi 기반 · 속성 다섯 개를 만드는 API
 */
data class DerivedCostConfig(
    val plainCount: Int = 100,
    val eagerReads: Boolean = false,
    val basePolicy: Boolean = false,
    val fontEquals: Boolean = true,
    val stateApi: StateApi = StateApi.Derived
)

/** 탭 하나가 만든 일 */
data class TapCost(
    val resolutions: Int,
    val propertyCalcs: Int,
    val recompositions: Int,
    val fontLoads: Int
) {
    operator fun minus(other: TapCost) = TapCost(
        resolutions - other.resolutions,
        propertyCalcs - other.propertyCalcs,
        recompositions - other.recompositions,
        fontLoads - other.fontLoads
    )
}

/**
 * 카운터 — 스냅숏 상태가 아닌 평범한 필드다. 세는 일 자체가 상태 쓰기가 되어 리컴포지션을 만들면 안 되기 때문.
 * 모두 메인 스레드(컴포지션 · 레이아웃)에서만 증가한다.
 */
class DerivedCostCounters {
    /** 기반 derivedStateOf(오버라이드 해석) 계산 횟수 */
    var resolutions = 0

    /** 그 위 속성 derivedStateOf 계산 횟수 */
    var propertyCalcs = 0

    /** 칸 스코프 리컴포지션 — SideEffect 로 세므로 반영된 컴포지션만 */
    var recompositions = 0

    /** TypefaceLoader.loadBlocking 호출 = FontFamilyResolver 캐시 미스 */
    var fontLoads = 0

    fun snapshot() = TapCost(resolutions, propertyCalcs, recompositions, fontLoads)
}

/**
 * 탭 하나의 예상 비용 — DerivedState.kt · RecomposeScopeImpl.kt(runtime 1.12.1) 규칙으로 계산한다.
 * - 선택을 읽은 칸만 기반을 다시 계산한다(미리 읽기면 모든 칸, 람다면 선택 의존 칸 3).
 * - 결과가 바뀐 칸(플랜 카드 2 · 연간 안내 1)은 정책과 무관하게 새 레코드 → 속성 4 개 모두 재계산 → 리컴포지션.
 * - 결과가 같은 칸은 정책이 없으면 그래도 새 레코드 → 속성 4 개 재계산, 정책이 있으면 기존 레코드 재사용 → 0.
 * - 다시 계산한 속성이 이전 값과 같으면 리컴포지션을 건너뛴다. equals 없는 폰트는 절대 같지 않아 리컴포지션 + 캐시 미스.
 */
fun expectedTapCost(config: DerivedCostConfig, selectionChanged: Boolean): TapCost {
    if (!selectionChanged) return TapCost(0, 0, 0, 0)
    val unchangedReaders = if (config.eagerReads) config.plainCount else 0
    val propertyRecalcsOfUnchanged = if (config.basePolicy) 0 else unchangedReaders
    val recompositions = SELECTION_DEPENDENT_PARTS + if (config.fontEquals) 0 else propertyRecalcsOfUnchanged
    return TapCost(
        resolutions = SELECTION_DEPENDENT_PARTS + unchangedReaders,
        propertyCalcs = (SELECTION_DEPENDENT_PARTS + propertyRecalcsOfUnchanged) * DERIVED_PROPERTIES_PER_PART,
        recompositions = recompositions,
        fontLoads = if (config.fontEquals) 0 else recompositions
    )
}

/**
 * computedStateOf 로 만든 칸의 탭 비용 — 기반 해석 · 속성 계산은 하한이다(#185, runtime 1.13.0).
 * 정확한 수는 다시 그릴 칸이 바뀐 속성 넷 가운데 무엇을 먼저 검사하느냐(스코프에 기록된 순서 — 해시 순)에 따라 달라진다.
 * - 기반 = 속성: 속성 계산이 기반을 읽을 때마다 기반도 다시 계산한다(캐시 없음 — 스코프에는 바깥 속성만 기록)
 * - 결과가 바뀐 칸: '다르다' 가 처음 나올 때까지 검사(≥ 1) + 다시 그리며 속성 4 개를 읽는다
 * - 결과가 같은 칸(미리 읽기): 속성마다 검사 1 + 건너뛴 스코프의 재읽기 1 = 8
 * - 정책 스위치는 효과가 없다(computedStateOf 에는 '정책 없음' 이 없다). equals 없는 폰트는 계산마다 새 값이라
 *   검사하면 늘 '다르다' → 미리 읽기면 선택을 읽은 칸이 모두 다시 그려진다
 */
fun computedTapLowerBound(config: DerivedCostConfig, selectionChanged: Boolean): TapCost {
    if (!selectionChanged) return TapCost(0, 0, 0, 0)
    val unchangedReaders = if (config.eagerReads) config.plainCount else 0
    val recompositions = SELECTION_DEPENDENT_PARTS + if (config.fontEquals) 0 else unchangedReaders
    val skipped = if (config.fontEquals) unchangedReaders else 0
    val calcs = recompositions * (1 + DERIVED_PROPERTIES_PER_PART) + skipped * DERIVED_PROPERTIES_PER_PART * 2
    return TapCost(
        resolutions = calcs,
        propertyCalcs = calcs,
        recompositions = recompositions,
        fontLoads = if (config.fontEquals) 0 else recompositions
    )
}

/** 구성에 맞는 예상 — derivedStateOf 는 정확한 수, computedStateOf 는 하한 */
fun expectedFor(config: DerivedCostConfig, selectionChanged: Boolean): TapCost =
    if (config.stateApi == StateApi.Computed) computedTapLowerBound(config, selectionChanged) else expectedTapCost(config, selectionChanged)

/**
 * 측정이 예상과 맞나 — computedStateOf 는 리컴포지션 · 폰트 로드가 같고, 기반 = 속성이며, 둘 다 하한 이상이어야 한다.
 * [ignoreFontLoads] 는 레이아웃이 없는 JVM 컴포지션용(폰트 로드는 레이아웃에서 일어난다)
 */
fun tapMatches(config: DerivedCostConfig, measured: TapCost, expected: TapCost, ignoreFontLoads: Boolean = false): Boolean {
    val fonts = ignoreFontLoads || measured.fontLoads == expected.fontLoads
    if (config.stateApi == StateApi.Derived) return fonts && measured.copy(fontLoads = 0) == expected.copy(fontLoads = 0)
    return fonts &&
        measured.recompositions == expected.recompositions &&
        measured.resolutions == measured.propertyCalcs &&
        measured.propertyCalcs >= expected.propertyCalcs
}

// ==================== 폰트: equals 유무 ====================

/** 세는 TypefaceLoader — 호출 1 회 = FontFamilyResolver 캐시 미스 1 회. 파일 대신 시스템 serif 를 돌려준다 */
class CountingTypefaceLoader(private val onLoad: () -> Unit) : AndroidFont.TypefaceLoader {
    override fun loadBlocking(context: Context, font: AndroidFont): Typeface {
        onLoad()
        return Typeface.SERIF
    }

    override suspend fun awaitLoad(context: Context, font: AndroidFont): Typeface = loadBlocking(context, font)
}

/** 원문 #4245 이전 LocalFileFont — equals 를 재정의하지 않아 같은 명세여도 서로 다른 폰트(동일성 비교) */
class IdentityLabFont(
    val spec: LabFontSpec,
    loader: AndroidFont.TypefaceLoader
) : AndroidFont(FontLoadingStrategy.Blocking, loader, FontVariation.Settings()) {
    override val weight: FontWeight = FontWeight.Normal
    override val style: FontStyle = FontStyle.Normal
}

/** #4245 이후 — 같은 명세 · 같은 로더면 같은 폰트 */
class EqualLabFont(
    val spec: LabFontSpec,
    loader: AndroidFont.TypefaceLoader
) : AndroidFont(FontLoadingStrategy.Blocking, loader, FontVariation.Settings()) {
    override val weight: FontWeight = FontWeight.Normal
    override val style: FontStyle = FontStyle.Normal

    override fun equals(other: Any?): Boolean =
        this === other || (other is EqualLabFont && spec == other.spec && typefaceLoader == other.typefaceLoader)

    override fun hashCode(): Int = 31 * spec.hashCode() + typefaceLoader.hashCode()
}

/** 명세 → FontFamily. 원문 FontSpec.Downloaded.resolve() 처럼 부를 때마다 새 폰트 인스턴스를 만든다 */
fun LabFontSpec.resolve(withEquals: Boolean, loader: AndroidFont.TypefaceLoader): FontFamily =
    FontFamily(if (withEquals) EqualLabFont(this, loader) else IdentityLabFont(this, loader))

// ==================== 칸 상태 보관자 ====================

/**
 * 칸 하나의 상태 보관자(원문 TextComponentState 축소판) — 기반 1(오버라이드 해석) + 그 위 속성 4.
 * 기반은 private 이고 컴포지션은 속성만 읽는다.
 */
class PaywallPartState(
    val part: PaywallPart,
    selected: State<String>,
    config: DerivedCostConfig,
    counters: DerivedCostCounters,
    loader: AndroidFont.TypefaceLoader,
    font: LabFontSpec = LabFontSpec("lab-serif")
) {
    /** 설정한 API 로 상태를 만든다 — policy 가 null 이면 derivedStateOf 는 정책 없음, computedStateOf 는 기본 정책 */
    private fun <T> derive(config: DerivedCostConfig, policy: SnapshotMutationPolicy<T>?, calculation: () -> T): State<T> =
        when {
            config.stateApi == StateApi.Computed ->
                if (policy != null) computedStateOf(policy, calculation) else computedStateOf(calculation = calculation)
            policy != null -> derivedStateOf(policy, calculation)
            else -> derivedStateOf(calculation)
        }

    private val presented: State<PresentedLook> = run {
        val calculation: () -> PresentedLook = {
            counters.resolutions++
            val isSelected = { selected.value == part.packageId }
            val annualSelected = { selected.value == PaywallPackages.Annual }
            if (config.eagerReads) {
                // 킬 스위치: 옛 코드처럼 해석 전에 입력을 모두 읽는다 → 조건 없는 칸도 선택이 의존성에 들어간다
                isSelected()
                annualSelected()
            }
            resolveLook(part, font, isSelected, annualSelected)
        }
        derive(config, if (config.basePolicy) structuralEqualityPolicy() else null, calculation)
    }

    val visible: State<Boolean> = derive(config, null) { counters.propertyCalcs++; presented.value.visible }
    val emphasized: State<Boolean> = derive(config, null) { counters.propertyCalcs++; presented.value.emphasized }
    val bold: State<Boolean> = derive(config, null) { counters.propertyCalcs++; presented.value.bold }
    val fontFamily: State<FontFamily> = derive(config, null) {
        counters.propertyCalcs++
        presented.value.font.resolve(config.fontEquals, loader)
    }
}

/** 칸 스코프가 읽는 네 값 */
data class PartReads(
    val visible: Boolean,
    val emphasized: Boolean,
    val bold: Boolean,
    val fontFamily: FontFamily
)

/**
 * 칸 스코프에서 네 속성을 읽고 리컴포지션을 센다. 값을 돌려주는 컴포저블은 자기 재시작 스코프가 없어
 * 읽기가 부른 쪽 스코프에 기록된다 — 화면과 단위 테스트가 같은 읽기 모양을 쓰도록 여기에 둔다.
 */
@Composable
fun readPart(state: PaywallPartState, counters: DerivedCostCounters): PartReads {
    SideEffect { counters.recompositions++ }
    return PartReads(
        visible = state.visible.value,
        emphasized = state.emphasized.value,
        bold = state.bold.value,
        fontFamily = state.fontFamily.value
    )
}

// ==================== 화면 상태 ====================

/** 측정한 탭 하나 */
data class TapReport(
    val seq: Int,
    val config: DerivedCostConfig,
    val from: String,
    val to: String,
    val measured: TapCost,
    val expected: TapCost
) {
    val matches: Boolean get() = tapMatches(config, measured, expected)
}

/** 구성 짧은 이름 — 기록 표의 한 칸 */
fun DerivedCostConfig.shortLabel(): String = buildString {
    if (stateApi == StateApi.Computed) append("computed · ")
    append("N=").append(plainCount)
    append(if (eagerReads) " · 미리 읽기" else " · 람다")
    append(if (basePolicy) " · 정책" else " · 정책 없음")
    append(if (fontEquals) " · equals" else " · equals 없음")
}

/**
 * Lab 화면 상태 — 탭 직전 카운터를 기준점으로 잡고, 그 탭의 리컴포지션 · 레이아웃이 끝난 다음 프레임에 [publish] 로 차이를 기록한다.
 */
class DerivedCostLabState {
    var config by mutableStateOf(DerivedCostConfig())
    val selected = mutableStateOf(PaywallPackages.Monthly)
    val counters = DerivedCostCounters()
    val loader = CountingTypefaceLoader { counters.fontLoads++ }
    val reports = mutableStateListOf<TapReport>()

    /** 탭마다 1 증가 — 측정 효과가 이 값을 키로 다시 시작한다 */
    var tapSeq by mutableIntStateOf(0)
        private set

    private var baseline = counters.snapshot()
    private var pending: TapReport? = null

    fun select(packageId: String) {
        val from = selected.value
        baseline = counters.snapshot()
        pending = TapReport(
            seq = tapSeq + 1,
            config = config,
            from = from,
            to = packageId,
            measured = TapCost(0, 0, 0, 0),
            expected = expectedFor(config, from != packageId)
        )
        selected.value = packageId
        tapSeq++
    }

    fun publish() {
        val report = pending ?: return
        pending = null
        reports.add(0, report.copy(measured = counters.snapshot() - baseline))
        while (reports.size > MAX_REPORTS) reports.removeAt(reports.lastIndex)
    }

    private companion object {
        const val MAX_REPORTS = 6
    }
}
