package com.example.composesample.presentation.example.component.architecture.state

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Indigo = Color(0xFF283593)
private val Accent = Color(0xFFD84315)
private val Muted = Color(0xFF757575)
private val Mismatch = Color(0xFFC62828)

@Composable
fun DerivedStateCostExampleUI(onBackEvent: () -> Unit) {
    // 기록이 LazyColumn 항목 재생성에도 남도록 화면 루트에 둔다
    val lab = remember { DerivedCostLabState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4FA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Indigo)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = Color.White)
            }
            Text(
                text = "derivedStateOf 의 숨은 비용",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { OverviewCard() }
            item { TapCostLabCard(lab) }
            item { ReadWhatYouUseCard() }
            item { PolicyChainCard() }
            item { EqualsCard() }
            item { MeasurementCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Indigo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("리컴포지션은 건너뛰어도 재계산은 한다", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "derivedStateOf 가 막아 주는 것은 리컴포지션이지 재계산이 아니다. 선택 하나를 화면의 모든 칸이 읽으면 " +
                    "탭 한 번에 이렇게 된다.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "선택이 바뀐다",
                "선택을 읽은 derivedStateOf 가 모두 '바뀌었을 수 있음'이 된다",
                "다시 그리기 직전, 런타임이 칸마다 값을 다시 계산해 지난 값과 비교한다",
                "같으면 그 칸은 건너뛴다 — 다시 그린 칸은 0 이어도 3 의 계산은 칸 수만큼 했다"
            ).forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ==================== 1. Lab ====================

@Composable
private fun TapCostLabCard(lab: DerivedCostLabState) {
    LabCard(
        title = "1. 탭 하나의 비용",
        color = Indigo,
        description = "월간 · 연간 카드를 눌러 선택을 바꾼다. 칸마다 기반 derivedStateOf 1 개(오버라이드 해석)와 그 위 속성 " +
            "4 개(visible · 강조 · 굵기 · fontFamily)가 있다. 스위치는 '고친 코드'를 기준으로 하나씩 옛 동작을 되살리는 킬 스위치다."
    ) {
        ConfigControls(lab)
        Spacer(modifier = Modifier.height(12.dp))
        PaywallPreview(lab)
        TapMeter(lab)
        Spacer(modifier = Modifier.height(12.dp))
        TapResult(lab)
    }
}

@Composable
private fun ConfigControls(lab: DerivedCostLabState) {
    val config = lab.config
    ControlRow("선택과 무관한 칸") {
        listOf(20, 100, 300).forEach { count ->
            LabChip("$count 개", config.plainCount == count) { lab.config = config.copy(plainCount = count) }
        }
    }
    ControlRow("조건 입력") {
        LabChip("람다로 필요할 때만", !config.eagerReads) { lab.config = config.copy(eagerReads = false) }
        LabChip("해석 전에 미리 읽기(옛)", config.eagerReads) { lab.config = config.copy(eagerReads = true) }
    }
    ControlRow("기반 derivedStateOf 정책") {
        LabChip("없음(기본)", !config.basePolicy) { lab.config = config.copy(basePolicy = false) }
        LabChip("structuralEqualityPolicy", config.basePolicy) { lab.config = config.copy(basePolicy = true) }
    }
    ControlRow("폰트 equals") {
        LabChip("있음", config.fontEquals) { lab.config = config.copy(fontEquals = true) }
        LabChip("없음(옛)", !config.fontEquals) { lab.config = config.copy(fontEquals = false) }
    }
}

/** 구성이 바뀌면 칸 상태를 새로 만든다 — 선택 상태는 그대로 */
@Composable
private fun PaywallPreview(lab: DerivedCostLabState) {
    val config = lab.config
    val states = remember(config) {
        paywallParts(config.plainCount).map { part ->
            PaywallPartState(part, lab.selected, config, lab.counters, lab.loader)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFC5CAE9), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlanCard(states[0], "월 9,900원", lab.counters, Modifier.weight(1f)) { lab.select(PaywallPackages.Monthly) }
            PlanCard(states[1], "연 99,000원", lab.counters, Modifier.weight(1f)) { lab.select(PaywallPackages.Annual) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        PartText(states[2], lab.counters, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        // 고정 높이 안에서 스크롤 — 화면 밖 칸도 모두 컴포즈돼 있어 칸 수만큼 센다
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                .verticalScroll(rememberScrollState())
                .padding(6.dp)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (index in SELECTION_DEPENDENT_PARTS until states.size) {
                    val state = states[index]
                    key(state.part.id) { PartText(state, lab.counters, fontSize = 10.sp) }
                }
            }
        }
    }
}

/** 플랜 카드 — 읽기와 리컴포지션 집계는 readPart 로 이 스코프에 */
@Composable
private fun PlanCard(
    state: PaywallPartState,
    price: String,
    counters: DerivedCostCounters,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val reads = readPart(state, counters)
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .border(if (reads.emphasized) 2.5.dp else 1.dp, if (reads.emphasized) Accent else Color(0xFFBDBDBD), shape)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Text(
            text = state.part.label,
            fontSize = 15.sp,
            fontWeight = if (reads.bold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = reads.fontFamily,
            color = if (reads.emphasized) Accent else Color(0xFF424242)
        )
        // 가격은 기본 폰트 — 칸마다 랩 폰트를 쓰는 텍스트를 하나로 둔다. 같은 FontFamily 를 두 텍스트가 쓰면 그 사이에
        // 다른 패밀리 16 개가 해석될 때 캐시(LRU 16)에서 밀려 두 번째도 로드한다(실측 — 4번 카드)
        Text(text = price, fontSize = 11.sp, color = Muted)
    }
}

@Composable
private fun PartText(state: PaywallPartState, counters: DerivedCostCounters, fontSize: TextUnit) {
    val reads = readPart(state, counters)
    if (!reads.visible) return
    Text(
        text = state.part.label,
        fontSize = fontSize,
        fontWeight = if (reads.bold) FontWeight.Bold else FontWeight.Normal,
        fontFamily = reads.fontFamily,
        color = if (reads.emphasized) Accent else Color(0xFF616161)
    )
}

/** 탭이 만든 리컴포지션 · 레이아웃(폰트 로드)은 그 프레임 안에서 끝난다 → 다음 프레임 콜백에서 차이를 기록 */
@Composable
private fun TapMeter(lab: DerivedCostLabState) {
    val seq = lab.tapSeq
    LaunchedEffect(seq) {
        if (seq == 0) return@LaunchedEffect
        withFrameNanos { }
        lab.publish()
    }
}

@Composable
private fun TapResult(lab: DerivedCostLabState) {
    val last = lab.reports.firstOrNull()
    if (last == null) {
        Text("카드를 누르면 그 탭이 부른 일을 측정값과 예상값으로 보여 준다.", fontSize = 12.sp, color = Muted)
        return
    }
    Text(
        text = "탭 #${last.seq}: ${packageName(last.from)} → ${packageName(last.to)}",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Indigo
    )
    Text(text = last.config.shortLabel(), fontSize = 11.sp, color = Muted)
    Spacer(modifier = Modifier.height(6.dp))
    TableRow(listOf("항목", "측정", "예상"), header = true)
    CostRow("오버라이드 해석(기반)", last.measured.resolutions, last.expected.resolutions)
    CostRow("속성 재계산(위층)", last.measured.propertyCalcs, last.expected.propertyCalcs)
    CostRow("칸 리컴포지션", last.measured.recompositions, last.expected.recompositions)
    CostRow("폰트 로드(캐시 미스)", last.measured.fontLoads, last.expected.fontLoads)

    if (lab.reports.size > 1) {
        Spacer(modifier = Modifier.height(10.dp))
        Text("최근 탭(측정 해석 · 속성 · 리컴포지션 · 로드)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Indigo)
        lab.reports.forEach { report ->
            val matches = report.measured == report.expected
            // 구성 한 줄 + 숫자 한 줄
            Text(
                text = "#${report.seq} ${report.config.shortLabel()}\n    →  " +
                    "${report.measured.resolutions} · ${report.measured.propertyCalcs} · " +
                    "${report.measured.recompositions} · ${report.measured.fontLoads}" + if (matches) "" else "  (예상과 다름)",
                modifier = Modifier.padding(vertical = 2.dp),
                fontSize = 11.sp,
                color = if (matches) Color(0xFF424242) else Mismatch,
                lineHeight = 16.sp
            )
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    val total = lab.counters.snapshot()
    Text(
        text = "화면을 연 뒤 누적(구성 변경으로 새로 만든 칸 포함): 해석 ${total.resolutions} · 속성 ${total.propertyCalcs} · " +
            "리컴포지션 ${total.recompositions} · 폰트 로드 ${total.fontLoads}",
        fontSize = 11.sp,
        color = Muted,
        lineHeight = 16.sp
    )
}

private fun packageName(packageId: String) = if (packageId == PaywallPackages.Annual) "연간" else "월간"

@Composable
private fun CostRow(label: String, measured: Int, expected: Int) {
    TableRow(
        cells = listOf(label, "$measured", "$expected"),
        color = if (measured == expected) Color(0xFF212121) else Mismatch
    )
}

// ==================== 2 ~ 5. 설명 ====================

@Composable
private fun ReadWhatYouUseCard() {
    LabCard(
        title = "2. 의존성은 '읽은 것' — 조건 입력을 람다로",
        color = Color(0xFF00695C),
        description = "derivedStateOf 는 계산이 읽을 수 있는 상태가 아니라 실제로 읽은 상태만 의존성으로 모은다. " +
            "DerivedSnapshotState 가 계산을 Snapshot.observe 의 읽기 관찰자 아래에서 돌리며 읽힌 상태를 하나씩 기록한다."
    ) {
        // 폰 폭(약 40 칸)에 맞춘 줄 길이
        CodeBlock(
            "// 옛 코드 — 해석 전에 읽는다\n" +
                "// → 조건 없는 칸도 선택에 의존\n" +
                "val presented by derivedStateOf {\n" +
                "  val selected = isSelected()\n" +
                "  resolve(overrides, selected)\n" +
                "}\n\n" +
                "// 고친 코드 — 검사하는 분기에서만 부른다\n" +
                "val presented by derivedStateOf {\n" +
                "  resolve(overrides, isSelected = {\n" +
                "    selection.id == plan.id\n" +
                "  })\n" +
                "}"
        )
        Spacer(modifier = Modifier.height(10.dp))
        FactRow(
            "건너뛰려면 계산해야 한다",
            "다시 그리기 직전 RecomposeScopeImpl.checkDerivedStateChanged 가 policy ?: structuralEqualityPolicy() 로 " +
                "새 값과 지난 값을 비교한다. 새 값을 알 방법은 계산을 다시 돌리는 것뿐이다."
        )
        FactRow(
            "비용은 인스턴스 수",
            "derivedStateOf 하나를 다섯 스코프가 읽으면 탭당 1 회, 스코프마다 하나씩 만들면 5 회 계산한다 — 캐시는 인스턴스마다 있다(단위 테스트)."
        )
        FactRow(
            "같은 규칙",
            "컴포저블 본문 · snapshotFlow · 레이아웃 · 그리기 블록도 읽은 상태만 구독한다. 쓰는 경로에서만 읽는다."
        )
    }
}

@Composable
private fun PolicyChainCard() {
    LabCard(
        title = "3. 정책 없는 derivedStateOf 의 연쇄",
        color = Color(0xFF6A1B9A),
        description = "정책이 없으면 다시 계산한 값이 같아도 새 레코드를 만든다(DerivedState.kt: policy?.equivalent(…) == true 일 " +
            "때만 기존 레코드를 다시 쓴다). 위층 derivedStateOf 는 의존한 레코드의 동일성으로 유효성을 따지므로 함께 다시 계산된다."
    ) {
        TableRow(listOf("기반 1 + 위층 6, 같은 값", "기반", "위층"), header = true)
        TableRow(listOf("정책 없음(기본)", "1", "6"))
        TableRow(listOf("structuralEqualityPolicy", "1", "0"))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "리컴포지션 판정은 정책이 없어도 구조적 동등성으로 하므로, 정책 없음의 비용은 위층 재계산이지 리컴포지션이 아니다. " +
                "위층 값이 '같다'를 말할 수 없으면(4번) 리컴포지션까지 간다. 1번에서 '미리 읽기'로 두고 정책을 바꿔 보면 속성 재계산이 " +
                "칸 수 × 4 에서 12 로 준다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun EqualsCard() {
    LabCard(
        title = "4. equals 없는 값은 '같다'를 말하지 못한다",
        color = Color(0xFFAD1457),
        description = "원문의 내려받은 폰트(LocalFileFont)는 equals 가 없어 같은 파일이어도 부를 때마다 다른 폰트였다. " +
            "이 화면의 IdentityLabFont 가 그 자리이고, 로더 호출을 센다."
    ) {
        listOf(
            "폰트 속성이 다시 계산되면 새 FontFamily — 지난 값과 같지 않아 칸이 다시 그려진다",
            "텍스트를 다시 배치하며 FontFamilyResolver 의 캐시를 찾는다 — 키(TypefaceRequest, data class)가 family 를 비교해 미스",
            "폰트별 캐시도 Font 의 equals 로 찾는다 — 미스",
            "로딩 전략이 Blocking 이라 그 자리(메인 스레드)에서 TypefaceLoader.loadBlocking 을 부른다"
        ).forEachIndexed { index, step ->
            Text("${index + 1}. $step", fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        FactRow(
            "캐시까지 밀어낸다",
            "FontFamilyResolver 의 두 캐시(요청별 · 폰트별)는 크기 16 의 LRU 다. 한 프레임에 equals 없는 패밀리가 16 개 넘게 해석되면, " +
                "같은 패밀리 인스턴스를 쓰는 두 번째 텍스트도 그 사이 밀려나 다시 로드한다. 다시 측정할 노드는 깊이 → 노드 해시 순서라 " +
                "사이에 몇 개가 끼는지는 실행마다 다르다(실측: 플랜 카드의 이름 · 가격이 한 패밀리를 함께 쓰던 첫 구현에서, 한 실행은 탭당 " +
                "로드가 칸 20 · 100 에서 +1, 300 에서 +2 였고 같은 구성만 따로 연 실행은 +0 — 그래서 가격은 기본 폰트로 뒀다)."
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "조건 입력을 람다로 고친 뒤에는 equals 를 꺼도 탭 비용이 선택 의존 칸 3 개만큼만 는다. 그래도 스냅숏 상태에 담기는 값은 " +
                "equals 가 있어야 한다 — 다른 경로로 다시 계산되는 순간 같은 비용이 돌아온다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun MeasurementCard() {
    LabCard(
        title = "5. 측정 방법",
        color = Color(0xFF37474F),
        description = "원문은 계측 테스트와 킬 스위치로 탭 하나를 쟀다. 이 화면은 같은 생각을 기기와 무관한 횟수로 옮겼다."
    ) {
        FactRow(
            "시간보다 횟수",
            "시간은 기기 · 빌드마다 다르다(원문: 같은 계열 빌드가 Galaxy S23 190 ms, 에뮬레이터 112 ms). waitForIdle 은 프레임 " +
                "단위로 기다려 해상도가 16.7 ms, 바닥이 두 프레임(약 33 ms)이다."
        )
        FactRow(
            "킬 스위치로 A/B",
            "고친 코드를 기준으로 스위치 하나만 옛 동작으로 돌린다. '결함이 있다'와 '그것이 원인이다'는 다른 주장이다 — 원문의 폰트 " +
                "equals 수정은 결함이었지만 체감 지연의 주원인은 아니었다."
        )
        FactRow(
            "측정 시점",
            "탭 직전 카운터를 기준점으로 잡고, 그 탭의 리컴포지션 · 레이아웃이 끝난 다음 프레임 콜백(withFrameNanos)에서 차이를 기록한다."
        )
        FactRow(
            "세는 방법",
            "카운터는 스냅숏 상태가 아닌 평범한 필드다 — 상태로 세면 세는 일이 쓰기가 되어 리컴포지션을 만든다. 리컴포지션은 SideEffect 로 " +
                "세어 반영된 컴포지션만 센다."
        )
        FactRow(
            "이 화면의 실측",
            "Pixel 4 API 30(debug · release) · Pixel Tablet API 35(debug) 에뮬레이터에서 칸 20 · 100 · 300 × 스위치 8 구성 × 두 방향, " +
                "144 탭 모두 측정이 예상과 같았다. 단위 테스트는 같은 칸 상태를 실제 컴포지션으로 돌려 8 구성을 확인한다."
        )
    }
}

@Composable
private fun SummaryCard() {
    LabCard(title = "정리", color = Indigo, description = "여러 칸이 함께 읽는 상태마다 물을 것: 누가 읽고, 모두 읽어야 하는가?") {
        listOf(
            "derivedStateOf 는 리컴포지션을 막아 줄 뿐, 의존성이 바뀌면 재계산은 매번 한다",
            "비용은 '선택을 읽은 derivedStateOf 인스턴스 수'에 비례한다 — 다시 그린 칸 수가 아니다",
            "조건 입력은 람다로 넘기고 그 조건을 검사하는 분기에서만 부른다",
            "derivedStateOf 위에 derivedStateOf 를 쌓으면 아래층에 structuralEqualityPolicy 를 고려한다",
            "상태에 담기는 값은 equals 를 갖춘다 — 없으면 매번 '바뀜'으로 보여 리컴포지션과 캐시 미스가 따라온다",
            "고친 것은 스위치로 되돌릴 수 있게 두고 같은 기기에서 A/B 로 잰다"
        ).forEach { line ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("•", fontSize = 12.sp, color = Indigo)
                Spacer(modifier = Modifier.width(6.dp))
                Text(line, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 18.sp)
            }
        }
    }
}

// ==================== 공통 ====================

@Composable
private fun LabCard(
    title: String,
    color: Color,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Muted, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ControlRow(label: String, chips: @Composable () -> Unit) {
    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { chips() }
}

@Composable
private fun LabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC5CAE9))
    )
}

@Composable
private fun TableRow(cells: List<String>, header: Boolean = false, color: Color = Color(0xFF212121)) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = cell,
                modifier = Modifier.weight(if (index == 0) 2f else 1f),
                fontSize = 12.sp,
                fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
                color = if (header) Indigo else color
            )
        }
    }
    HorizontalDivider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FactRow(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Text(text = body, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 18.sp)
    }
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
            .padding(10.dp),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFECEFF1),
        lineHeight = 16.sp
    )
}
