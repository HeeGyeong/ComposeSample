package com.example.composesample.presentation.example.component.architecture.development.rebound

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// ===================== 데이터 모델 =====================

data class ReboundRole(
    val name: String,
    val budget: Int,
    val description: String,
    val color: Color
)

private val reboundRoles = listOf(
    ReboundRole("Screen", 3, "페이지/화면 수준 Composable", Color(0xFF5C6BC0)),
    ReboundRole("Container", 10, "Layout·Card 등 컨테이너", Color(0xFF26A69A)),
    ReboundRole("Leaf", 5, "Text·Icon 등 리프 노드", Color(0xFF66BB6A)),
    ReboundRole("Interactive", 30, "Button·TextField 등 상호작용", Color(0xFFFF7043)),
    ReboundRole("ListItem", 60, "목록의 각 아이템", Color(0xFFAB47BC)),
    ReboundRole("Animated", 120, "애니메이션 처리 Composable", Color(0xFFFFA726)),
)

// $changed 비트마스크 상태 정의
data class ChangedBitState(
    val bits: String,
    val label: String,
    val meaning: String,
    val color: Color,
    val example: String
)

private val changedBitStates = listOf(
    ChangedBitState("00", "UNCERTAIN", "컴파일러가 변경 여부를 알 수 없음", Color(0xFF9E9E9E), "동적으로 결정되는 값"),
    ChangedBitState("01", "SAME", "이전 컴포지션과 동일한 값", Color(0xFF43A047), "stable한 파라미터"),
    ChangedBitState("10", "DIFFERENT", "값이 변경됨 → 리컴포지션 발생", Color(0xFFE53935), "매번 새로 생성된 listOf()"),
    ChangedBitState("11", "STATIC", "절대 변경되지 않는 정적 값", Color(0xFF1565C0), "컴파일 타임 상수"),
)

// ===================== 메인 화면 =====================

@Composable
fun ReboundExampleUI(onBackEvent: () -> Unit) {
    var triggerCount by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { ReboundHeader(onBackEvent = onBackEvent) }

        item { ReboundDescription() }

        // 섹션 1: 역할별 예산
        item {
            SectionTitle("1. 역할별 리컴포지션 예산 (회/초)")
        }
        items(reboundRoles) { role ->
            ReboundRoleCard(role = role)
        }

        // 섹션 2: 동적 컨텍스트 배수
        item {
            SectionTitle("2. 동적 컨텍스트 배수 (Context Multiplier)")
        }
        item {
            DynamicContextMultiplierSection()
        }

        // 섹션 3: 리컴포지션 카운터 데모
        item {
            SectionTitle("3. 리컴포지션 카운터 데모")
        }
        item {
            ReboundDemoSection(
                triggerCount = triggerCount,
                onTrigger = { triggerCount++ }
            )
        }

        // 섹션 4: $changed 비트마스크
        item {
            SectionTitle("4. \$changed 비트마스크")
        }
        item {
            ChangedBitmaskSection()
        }

        // 섹션 5: 실제 버그 시뮬레이션
        item {
            SectionTitle("5. 실제 버그 시뮬레이션 (Before / After)")
        }
        item {
            BugSimulationSection()
        }
    }
}

// ===================== 공통 컴포넌트 =====================

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun ReboundHeader(onBackEvent: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackEvent) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기"
            )
        }
        Text(
            text = "Rebound - 리컴포지션 모니터링",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ReboundDescription() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Rebound란?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Composable의 역할에 따라 리컴포지션 예산을 할당하는 컨텍스트 인식 모니터링 도구입니다.\n\n" +
                        "기존 도구(Layout Inspector)가 단순히 횟수만 표시하는 것과 달리, " +
                        "각 Composable의 역할에 맞는 기준으로 과도한 리컴포지션 여부를 판단합니다.\n\n" +
                        "Kotlin 컴파일러 플러그인으로 동작하며 디버그 빌드에서만 활성화됩니다.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ===================== 섹션 1: 역할별 예산 카드 =====================

@Composable
private fun ReboundRoleCard(role: ReboundRole) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(role.color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .border(1.dp, role.color.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${role.budget}",
                        color = role.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "/s",
                        color = role.color.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = role.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = role.description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// ===================== 섹션 2: 동적 컨텍스트 배수 =====================

@Composable
private fun DynamicContextMultiplierSection() {
    var isScrolling by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }
    var isUserInput by remember { mutableStateOf(false) }

    // 컨텍스트 배수 계산 (중복 적용)
    val multiplier = 1.0f *
            (if (isScrolling) 2.0f else 1.0f) *
            (if (isAnimating) 1.5f else 1.0f) *
            (if (isUserInput) 1.5f else 1.0f)

    // Container 역할 기준 (기본 예산: 10/s)
    val baseRole = reboundRoles.first { it.name == "Container" }
    val effectiveBudget = (baseRole.budget * multiplier).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "앱 상태에 따라 예산이 자동으로 늘어납니다.\n스크롤/애니메이션/입력 중에는 더 많은 리컴포지션이 허용됩니다.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 스위치 목록
            ContextToggleRow(
                label = "스크롤 중",
                multiplierText = "× 2.0",
                checked = isScrolling,
                onCheckedChange = { isScrolling = it }
            )
            ContextToggleRow(
                label = "애니메이션 중",
                multiplierText = "× 1.5",
                checked = isAnimating,
                onCheckedChange = { isAnimating = it }
            )
            ContextToggleRow(
                label = "사용자 입력 중",
                multiplierText = "× 1.5",
                checked = isUserInput,
                onCheckedChange = { isUserInput = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 계산 결과
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Container 역할 기준 유효 예산",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${baseRole.budget}",
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "  ×  ${String.format(Locale.US, "%.2f", multiplier)}  =  ",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = "$effectiveBudget /s",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (multiplier > 1f) Color(0xFF1565C0) else Color(0xFF26A69A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextToggleRow(
    label: String,
    multiplierText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = multiplierText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(end = 8.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// ===================== 섹션 3: 리컴포지션 카운터 데모 =====================

@Composable
private fun ReboundDemoSection(
    triggerCount: Int,
    onTrigger: () -> Unit
) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    SideEffect { recomposeCount++ }

    val containerBudget = reboundRoles.first { it.name == "Container" }.budget
    val ratio = (recomposeCount.toFloat() / containerBudget).coerceIn(0f, 1f)
    val statusColor = when {
        ratio > 0.8f -> Color(0xFFE53935)
        ratio > 0.5f -> Color(0xFFFFA726)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "이 컴포넌트의 리컴포지션 횟수", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$recomposeCount",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = "회",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = statusColor,
                trackColor = Color(0xFFEEEEEE)
            )
            Text(
                text = "Container 역할 예산 기준: ${containerBudget}회/초",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onTrigger,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("상태 변경으로 리컴포지션 유발 (${triggerCount}회 트리거)")
            }
        }
    }
}

// ===================== 섹션 4: $changed 비트마스크 =====================

@Composable
private fun ChangedBitmaskSection() {
    var selectedIndex by remember { mutableIntStateOf(2) } // 기본값: DIFFERENT

    val selected = changedBitStates[selectedIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compose 컴파일러는 모든 @Composable 함수에 \$changed 파라미터를 자동 주입합니다.\n" +
                        "각 파라미터의 상태를 2비트로 인코딩하여 불필요한 리컴포지션을 건너뜁니다.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4가지 상태 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                changedBitStates.forEachIndexed { index, state ->
                    Button(
                        onClick = { selectedIndex = index },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedIndex == index) state.color
                            else Color(0xFFEEEEEE),
                            contentColor = if (selectedIndex == index) Color.White
                            else Color.DarkGray
                        )
                    ) {
                        Text(
                            text = state.bits,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 선택된 상태 상세
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(selected.color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .border(1.dp, selected.color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selected.bits,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = selected.color
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selected.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = selected.color
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = selected.meaning, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "예) ${selected.example}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 위반 로그 시뮬레이션 (DIFFERENT 선택 시)
            if (selectedIndex == 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Rebound 위반 로그 (시뮬레이션)",
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "BUDGET VIOLATION: ProfileHeader\n" +
                                    "  rate=11/s  exceeds LEAF budget=5/s\n" +
                                    "  -> params: avatarUrl=DIFFERENT\n" +
                                    "             displayName=DIFFERENT\n" +
                                    "  -> forced: 0 | param-driven: 11\n" +
                                    "  -> interaction: IDLE",
                            fontSize = 11.sp,
                            color = Color(0xFFEF5350),
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

// ===================== 섹션 5: 실제 버그 시뮬레이션 =====================

@Composable
private fun BugSimulationSection() {
    var showFixed by remember { mutableStateOf(false) }
    var triggerBefore by remember { mutableIntStateOf(0) }
    var triggerAfter by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "블로그 실제 사례: TravelGuideCard",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "데이터 레이어 헬퍼가 매 호출마다 listOf(...)를 새로 생성하면\n" +
                        "파라미터가 항상 DIFFERENT로 인식되어 Skip이 불가능합니다.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Before / After 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Before (버그)", "After (수정)").forEachIndexed { index, label ->
                    val isSelected = showFixed == (index == 1)
                    val tabColor = if (index == 0) Color(0xFFE53935) else Color(0xFF43A047)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) Color.White else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { showFixed = (index == 1) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) tabColor else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!showFixed) {
                BugBeforeCard(trigger = triggerBefore, onTrigger = { triggerBefore++ })
            } else {
                BugAfterCard(trigger = triggerAfter, onTrigger = { triggerAfter++ })
            }
        }
    }
}

@Composable
private fun BugBeforeCard(trigger: Int, onTrigger: () -> Unit) {
    // 매번 새로운 listOf() 생성 → destinations 파라미터가 항상 DIFFERENT
    val destinations = listOf("Paris", "Tokyo", "New York")
    var skipCount by remember { mutableIntStateOf(0) }
    var recomposeCount by remember { mutableIntStateOf(0) }

    SideEffect {
        recomposeCount++
        // destinations는 항상 새 객체 → skip 불가
    }

    BugDemoContent(
        title = "문제 코드",
        codeSnippet = "// 매번 새로운 객체 생성\nval destinations = listOf(...)",
        destinations = destinations,
        recomposeCount = recomposeCount,
        skipCount = skipCount,
        skipRate = 0,
        statusColor = Color(0xFFE53935),
        bitState = "10 (DIFFERENT)",
        bitColor = Color(0xFFE53935),
        trigger = trigger,
        onTrigger = onTrigger
    )
}

@Composable
private fun BugAfterCard(trigger: Int, onTrigger: () -> Unit) {
    // remember{}로 감싸기 → destinations 파라미터 안정적
    val destinations = remember { listOf("Paris", "Tokyo", "New York") }
    var recomposeCount by remember { mutableIntStateOf(0) }
    var skipCount by remember { mutableIntStateOf(0) }

    SideEffect {
        recomposeCount++
        if (recomposeCount > 1) skipCount++
    }

    val skipRate = if (recomposeCount > 1) (skipCount * 100 / (recomposeCount - 1)) else 0

    BugDemoContent(
        title = "수정 코드",
        codeSnippet = "// remember{}로 안정화\nval destinations = remember { listOf(...) }",
        destinations = destinations,
        recomposeCount = recomposeCount,
        skipCount = skipCount,
        skipRate = skipRate,
        statusColor = Color(0xFF43A047),
        bitState = "01 (SAME)",
        bitColor = Color(0xFF43A047),
        trigger = trigger,
        onTrigger = onTrigger
    )
}

@Composable
private fun BugDemoContent(
    title: String,
    codeSnippet: String,
    destinations: List<String>,
    recomposeCount: Int,
    skipCount: Int,
    skipRate: Int,
    statusColor: Color,
    bitState: String,
    bitColor: Color,
    trigger: Int,
    onTrigger: () -> Unit
) {
    Column {
        // 코드 스니펫
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = codeSnippet,
                fontSize = 12.sp,
                color = Color(0xFF80CBC4),
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // destinations 파라미터 상태 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricChip(
                label = "\$changed",
                value = bitState,
                valueColor = bitColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 통계
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricChip(
                label = "리컴포지션",
                value = "${recomposeCount}회",
                valueColor = statusColor,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = "Skip 비율",
                value = "$skipRate%",
                valueColor = statusColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onTrigger,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = statusColor)
        ) {
            Text("리컴포지션 유발 (${trigger}회)")
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

// ===================== Preview =====================

@Preview(showBackground = true)
@Composable
fun ReboundExampleUIPreview() {
    ReboundExampleUI(onBackEvent = {})
}
