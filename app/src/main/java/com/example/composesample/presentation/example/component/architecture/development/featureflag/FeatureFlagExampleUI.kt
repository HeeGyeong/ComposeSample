package com.example.composesample.presentation.example.component.architecture.development.featureflag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Type-Safe Feature Flag (Switchboard 유사) 예제
 *
 * 외부 라이브러리(KSP/Firebase) 없이 다음 4가지 핵심 패턴을 자체 구현으로 비교한다.
 *
 * [1] sealed class 기반 type-safe flag 레지스트리
 *  - 문자열 키 대신 [FeatureFlag] sealed class 로 플래그를 정의 → 오타/존재하지 않는 키를 컴파일 타임에 차단.
 *  - Boolean / Enum 두 종류의 타입 안전 플래그를 한 계층으로 표현.
 *
 * [2] StateFlow 기반 reactive 토글
 *  - [FeatureFlagStore] 가 현재 값 맵을 [StateFlow] 로 노출 → 토글 시 구독 중인 모든 UI 가 자동 재구성.
 *
 * [3] Compose 디버그 메뉴 (ModalBottomSheet)
 *  - QA/개발용으로 런타임에 플래그를 강제 오버라이드하는 디버그 시트.
 *
 * [4] Remote Config 시뮬레이션
 *  - 로컬 기본값 → 원격 응답(가정)을 fetch 해 덮어쓰는 흐름을 지연(delay)으로 흉내. (Firebase Remote Config 미사용)
 */

// ====================================================================
// [1] sealed class 기반 type-safe flag 레지스트리
// ====================================================================

/** 플래그가 가질 수 있는 값의 종류 */
sealed interface FlagValue {
    data class Bool(val value: Boolean) : FlagValue
    data class Choice(val value: String) : FlagValue
}

/**
 * 타입 안전 플래그 정의.
 * - 각 플래그는 고유 [key], 사람이 읽는 [title], [default] 값을 가진다.
 * - object 로 선언해 전역 레지스트리([FeatureFlag.all])로 열거 가능.
 */
sealed class FeatureFlag(
    val key: String,
    val title: String,
    val description: String,
    val default: FlagValue
) {
    /** Boolean 플래그 — 신규 디자인 노출 여부 */
    data object NewProfileUi : FeatureFlag(
        key = "new_profile_ui",
        title = "새 프로필 화면",
        description = "리디자인된 프로필 화면을 노출",
        default = FlagValue.Bool(false)
    )

    /** Boolean 플래그 — 결제 베타 기능 */
    data object PaymentBeta : FeatureFlag(
        key = "payment_beta",
        title = "결제 베타",
        description = "신규 결제 플로우(베타) 활성화",
        default = FlagValue.Bool(true)
    )

    /** Enum(Choice) 플래그 — 홈 레이아웃 분기(A/B 테스트) */
    data object HomeLayout : FeatureFlag(
        key = "home_layout",
        title = "홈 레이아웃 (A/B)",
        description = "list / grid / carousel 중 택1",
        default = FlagValue.Choice("list")
    ) {
        val options = listOf("list", "grid", "carousel")
    }

    companion object {
        /** 레지스트리 — 새 플래그 추가 시 여기에 등록 */
        val all: List<FeatureFlag> = listOf(NewProfileUi, PaymentBeta, HomeLayout)
    }
}

// ====================================================================
// [2] StateFlow 기반 reactive store + [4] Remote Config 시뮬레이션
// ====================================================================

/**
 * 플래그 현재 값을 보관하고 변경을 [StateFlow] 로 방출하는 저장소.
 * - 우선순위: 디버그 오버라이드 > 원격 값 > 로컬 기본값.
 */
class FeatureFlagStore {
    // key -> 현재 적용 값
    private val _flags = MutableStateFlow(
        FeatureFlag.all.associate { it.key to it.default }
    )
    val flags: StateFlow<Map<String, FlagValue>> = _flags.asStateFlow()

    // 값의 출처 표시용 (LOCAL / REMOTE / DEBUG)
    private val _sources = MutableStateFlow(
        FeatureFlag.all.associate { it.key to FlagSource.LOCAL }
    )
    val sources: StateFlow<Map<String, FlagSource>> = _sources.asStateFlow()

    /** 타입 안전 조회 — Boolean 플래그 전용 */
    fun isEnabled(flag: FeatureFlag): Boolean =
        (_flags.value[flag.key] as? FlagValue.Bool)?.value ?: false

    /** 타입 안전 조회 — Choice 플래그 전용 */
    fun choiceOf(flag: FeatureFlag): String =
        (_flags.value[flag.key] as? FlagValue.Choice)?.value ?: ""

    /** [3] 디버그 메뉴에서 강제 오버라이드 */
    fun override(flag: FeatureFlag, value: FlagValue) {
        _flags.value = _flags.value.toMutableMap().apply { put(flag.key, value) }
        _sources.value = _sources.value.toMutableMap().apply { put(flag.key, FlagSource.DEBUG) }
    }

    /** 로컬 기본값으로 초기화 */
    fun resetToDefault() {
        _flags.value = FeatureFlag.all.associate { it.key to it.default }
        _sources.value = FeatureFlag.all.associate { it.key to FlagSource.LOCAL }
    }

    /**
     * [4] 원격 응답을 가정한 fetch — 실제로는 네트워크 호출.
     * 디버그 오버라이드된 플래그는 덮어쓰지 않는다(디버그 우선순위 보장).
     */
    suspend fun fetchRemote() {
        delay(800) // 네트워크 지연 흉내
        val remote = mapOf(
            FeatureFlag.NewProfileUi.key to FlagValue.Bool(true),
            FeatureFlag.PaymentBeta.key to FlagValue.Bool(false),
            FeatureFlag.HomeLayout.key to FlagValue.Choice("carousel")
        )
        val curSources = _sources.value
        _flags.value = _flags.value.toMutableMap().apply {
            remote.forEach { (k, v) ->
                if (curSources[k] != FlagSource.DEBUG) put(k, v)
            }
        }
        _sources.value = _sources.value.toMutableMap().apply {
            remote.keys.forEach { k -> if (curSources[k] != FlagSource.DEBUG) put(k, FlagSource.REMOTE) }
        }
    }
}

enum class FlagSource { LOCAL, REMOTE, DEBUG }

// ====================================================================
// UI
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureFlagExampleUI(onBackEvent: () -> Unit) {
    val store = remember { FeatureFlagStore() }
    val flags by store.flags.collectAsState()
    val sources by store.sources.collectAsState()
    val scope = rememberCoroutineScope()

    var showDebugSheet by remember { mutableStateOf(false) }
    var fetching by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                text = "Type-Safe Feature Flag",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 현재 플래그 상태 (type-safe 조회 결과 반영)
            SectionTitle("실제 화면에 반영된 플래그")
            ConsumerPreviewCard(store)

            HorizontalDivider()
            SectionTitle("플래그 레지스트리 (StateFlow 구독)")
            FeatureFlag.all.forEach { flag ->
                FlagStatusRow(
                    flag = flag,
                    value = flags[flag.key],
                    source = sources[flag.key] ?: FlagSource.LOCAL
                )
            }

            HorizontalDivider()
            SectionTitle("Remote Config 시뮬레이션")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !fetching,
                    onClick = {
                        scope.launch {
                            fetching = true
                            store.fetchRemote()
                            fetching = false
                        }
                    }
                ) { Text(if (fetching) "원격 동기화 중..." else "원격 값 fetch") }
                Button(
                    onClick = { store.resetToDefault() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF888888))
                ) { Text("기본값 초기화") }
            }
            Text(
                text = "fetch 는 800ms 지연 후 원격 응답을 덮어쓴다. 단, 디버그(DEBUG)로 오버라이드한 플래그는 보존된다.",
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )

            HorizontalDivider()
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDebugSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A5BD4))
            ) { Text("🛠 디버그 메뉴 열기 (강제 오버라이드)") }

            Spacer(modifier = Modifier.height(8.dp))
            ExplanationCard()
        }
    }

    // [3] 디버그 메뉴 — ModalBottomSheet
    if (showDebugSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDebugSheet = false },
            sheetState = sheetState
        ) {
            DebugFlagSheetContent(store = store, flags = flags)
        }
    }
}

@Composable
private fun ConsumerPreviewCard(store: FeatureFlagStore) {
    // 구독을 위해 collectAsState 로 다시 읽음 → reactive 동작 시연
    val flags by store.flags.collectAsState()

    // type-safe 조회: 잘못된 타입 접근이 컴파일 타임에 막힘
    val profileEnabled = (flags[FeatureFlag.NewProfileUi.key] as? FlagValue.Bool)?.value ?: false
    val paymentBeta = (flags[FeatureFlag.PaymentBeta.key] as? FlagValue.Bool)?.value ?: false
    val homeLayout = (flags[FeatureFlag.HomeLayout.key] as? FlagValue.Choice)?.value ?: "list"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (profileEnabled) "✨ 새 프로필 화면 (리디자인)" else "기존 프로필 화면",
                fontWeight = FontWeight.SemiBold,
                color = if (profileEnabled) Color(0xFF1F5F2A) else Color.DarkGray
            )
            Text(
                text = if (paymentBeta) "💳 결제 베타 플로우 활성" else "결제 정식 플로우",
                color = if (paymentBeta) Color(0xFF1F5F2A) else Color.DarkGray
            )
            Text(text = "홈 레이아웃: $homeLayout", color = Color(0xFF3A5BD4))
        }
    }
}

@Composable
private fun FlagStatusRow(flag: FeatureFlag, value: FlagValue?, source: FlagSource) {
    val valueText = when (value) {
        is FlagValue.Bool -> if (value.value) "ON" else "OFF"
        is FlagValue.Choice -> value.value
        null -> "-"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(text = flag.title, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "key=${flag.key}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SourceBadge(source)
                Spacer(modifier = Modifier.height(0.dp))
                Text(
                    text = "  $valueText",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3A5BD4)
                )
            }
        }
    }
}

@Composable
private fun SourceBadge(source: FlagSource) {
    val (bg, label) = when (source) {
        FlagSource.LOCAL -> Color(0xFFE0E0E0) to "LOCAL"
        FlagSource.REMOTE -> Color(0xFFCDE3FF) to "REMOTE"
        FlagSource.DEBUG -> Color(0xFFFFE0A3) to "DEBUG"
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DebugFlagSheetContent(store: FeatureFlagStore, flags: Map<String, FlagValue>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "🛠 Feature Flag 디버그 메뉴", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "여기서 변경하면 출처가 DEBUG 로 고정되어 원격 fetch 에도 덮어쓰이지 않는다.",
            fontSize = 12.sp,
            color = Color(0xFF555555)
        )
        HorizontalDivider()

        FeatureFlag.all.forEach { flag ->
            when (val current = flags[flag.key]) {
                is FlagValue.Bool -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text(text = flag.title, fontWeight = FontWeight.SemiBold)
                            Text(text = flag.description, fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = current.value,
                            onCheckedChange = { store.override(flag, FlagValue.Bool(it)) }
                        )
                    }
                }

                is FlagValue.Choice -> {
                    val options = (flag as? FeatureFlag.HomeLayout)?.options ?: emptyList()
                    Column {
                        Text(text = flag.title, fontWeight = FontWeight.SemiBold)
                        Text(text = flag.description, fontSize = 12.sp, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            options.forEach { opt ->
                                FilterChip(
                                    selected = current.value == opt,
                                    onClick = { store.override(flag, FlagValue.Choice(opt)) },
                                    label = { Text(opt) }
                                )
                            }
                        }
                    }
                }

                null -> Unit
            }
            HorizontalDivider()
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { store.resetToDefault() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF888888))
        ) { Text("전체 기본값으로 초기화") }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

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
            Text("1) sealed class 레지스트리 — 문자열 키 대신 타입으로 플래그 정의(오타/누락 컴파일 차단)", fontSize = 13.sp)
            Text("2) StateFlow 노출 — 토글 시 collectAsState 구독 UI 가 자동 재구성", fontSize = 13.sp)
            Text("3) ModalBottomSheet 디버그 메뉴 — 런타임 강제 오버라이드(DEBUG 출처 고정)", fontSize = 13.sp)
            Text("4) Remote fetch — 원격 응답으로 덮어쓰되 디버그 오버라이드는 보존(우선순위: DEBUG > REMOTE > LOCAL)", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "참고: Switchboard 라이브러리는 KSP/Firebase 의존성이 필요하다. 본 예제는 동일 패턴을 외부 의존성 없이 직접 구현했다.",
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
