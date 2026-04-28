package com.example.composesample.presentation.example.component.architecture.development.test

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composesample.presentation.MainHeader

@Composable
fun TurbineFlowTestExampleUI(onBackEvent: () -> Unit) {
    val viewModel: TurbineFlowTestViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        MainHeader(title = "Coroutine Flow Testing", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Section 1: 기초 개념 ──────────────────────────────────────────
            item {
                SectionTitle("1. 기초 개념: Turbine 전에 먼저 알아야 할 것")
            }
            item {
                ConceptCard(
                    title = "runTest { }",
                    description = "가상 시간(virtual time)을 제어하는 테스트 스코프. delay()가 실제로 기다리지 않고 즉시 건너뜁니다.",
                    color = Color(0xFF4A90D9)
                )
            }
            item {
                ConceptCard(
                    title = "runCurrent()",
                    description = "현재 큐에 대기 중인 코루틴만 실행. delay() 이전까지만 진행되어 Loading 상태를 검증할 때 사용합니다.",
                    color = Color(0xFF7B68EE)
                )
            }
            item {
                ConceptCard(
                    title = "advanceUntilIdle()",
                    description = "모든 보류 중인 코루틴이 완료될 때까지 실행. delay() 포함 전체 플로우를 완료한 최종 상태를 검증합니다.",
                    color = Color(0xFF50C878)
                )
            }
            item {
                ConceptCard(
                    title = "StandardTestDispatcher vs UnconfinedTestDispatcher",
                    description = "Standard: 명시적 진행 제어 필요 (runCurrent/advanceUntilIdle)\nUnconfined: 즉시 실행, 초기 상태 검증에 편리하지만 타이밍 제어 불가",
                    color = Color(0xFFFF8C00)
                )
            }

            // ── Section 2: ViewModel 상태 시뮬레이션 ────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item {
                SectionTitle("2. 테스트 대상 ViewModel 상태 흐름")
            }
            item {
                StateSimulationCard(
                    uiState = uiState,
                    onFetch = { shouldFail -> viewModel.fetchData(shouldFail) },
                    onReset = { viewModel.reset() }
                )
            }

            // ── Section 3: ❌ 안티패턴 ───────────────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item {
                SectionTitle("3. ❌ Turbine 과명세화 안티패턴")
            }
            item {
                AntiPatternCard()
            }

            // ── Section 4: ✅ 올바른 패턴 ────────────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item {
                SectionTitle("4. ✅ 올바른 패턴: 상태별 분리 테스트")
            }
            item {
                CorrectPatternCard()
            }

            // ── Section 5: ✅ Turbine이 빛나는 곳 ────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item {
                SectionTitle("5. ✅ Turbine이 적합한 경우: SharedFlow 이벤트")
            }
            item {
                TurbineGoodUseCard()
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ─── 섹션 타이틀 ──────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

// ─── 개념 카드 ────────────────────────────────────────────────────────────────

@Composable
private fun ConceptCard(title: String, description: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(6.dp))
            Text(text = description, color = Color(0xFFCCCCCC), fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

// ─── 상태 시뮬레이션 카드 ──────────────────────────────────────────────────────

@Composable
private fun StateSimulationCard(
    uiState: FlowTestUiState,
    onFetch: (Boolean) -> Unit,
    onReset: () -> Unit
) {
    var shouldFail by remember { mutableStateOf(false) }

    val (stateLabel, stateColor) = when (uiState) {
        is FlowTestUiState.Empty   -> "Empty" to Color(0xFF888888)
        is FlowTestUiState.Loading -> "Loading..." to Color(0xFFFFD700)
        is FlowTestUiState.Success -> "Success ✓" to Color(0xFF50C878)
        is FlowTestUiState.Error   -> "Error ✗" to Color(0xFFFF6B6B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 현재 상태 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "현재 상태:", color = Color(0xFFAAAAAA), fontSize = 13.sp)
                Text(
                    text = stateLabel,
                    color = stateColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 성공 시 아이템 표시
            if (uiState is FlowTestUiState.Success) {
                Spacer(Modifier.height(8.dp))
                uiState.items.forEach { item ->
                    Text(
                        text = "  • $item",
                        color = Color(0xFF50C878),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            // 에러 시 메시지 표시
            if (uiState is FlowTestUiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(text = uiState.message, color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF444444))
            Spacer(Modifier.height(12.dp))

            // 실패 시뮬레이션 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "실패 시뮬레이션", color = Color(0xFFAAAAAA), fontSize = 13.sp)
                Switch(
                    checked = shouldFail,
                    onCheckedChange = { shouldFail = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF6B6B),
                        checkedTrackColor = Color(0xFFFF6B6B).copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onFetch(shouldFail) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90D9)),
                    enabled = uiState !is FlowTestUiState.Loading
                ) {
                    Text(text = "fetchData()", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))
                ) {
                    Text(text = "reset()", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // 상태 흐름 시각화
            Text(text = "상태 흐름:", color = Color(0xFF888888), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "Empty" to (uiState is FlowTestUiState.Empty),
                    "Loading" to (uiState is FlowTestUiState.Loading),
                    "Success" to (uiState is FlowTestUiState.Success),
                    "Error" to (uiState is FlowTestUiState.Error)
                ).forEach { (label, isActive) ->
                    Text(
                        text = label,
                        color = if (isActive) stateColor else Color(0xFF555555),
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (isActive) stateColor else Color(0xFF444444),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

// ─── 안티패턴 카드 ────────────────────────────────────────────────────────────

@Composable
private fun AntiPatternCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1A1A)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "awaitItem() 체이닝으로 인한 과명세화",
                color = Color(0xFFFF6B6B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            CodeSnippet(
                code = """viewModel.uiState.test {
    awaitItem() // Empty
    viewModel.fetchData()
    awaitItem() // Loading  ← 문제!
    awaitItem() // Success
}"""
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "⚠ Loading 상태를 생략하도록 코드를 변경하면\n→ 올바른 결정임에도 이 테스트는 실패합니다.",
                color = Color(0xFFFFAA44),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "⚠ StateFlow는 최신 값만 버퍼링하므로\n→ 빠른 연속 업데이트 시 중간 상태를 놓칠 수 있습니다.",
                color = Color(0xFFFFAA44),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ─── 올바른 패턴 카드 ─────────────────────────────────────────────────────────

@Composable
private fun CorrectPatternCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2D1A)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "상태별 독립 테스트 + runCurrent()",
                color = Color(0xFF50C878),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            CodeSnippet(
                code = """// ① 초기 상태 검증
@Test
fun `초기 상태는 Empty`() = runTest {
    assertEquals(Empty, viewModel.uiState.value)
}

// ② Loading 상태 검증
@Test
fun `fetch 직후 Loading 상태`() = runTest {
    viewModel.fetchData()
    runCurrent()  // delay() 이전까지만 실행
    assertEquals(Loading, viewModel.uiState.value)
}

// ③ 최종 상태 검증
@Test
fun `fetch 완료 후 Success 상태`() = runTest {
    viewModel.fetchData()
    advanceUntilIdle()  // 전체 완료
    assertTrue(viewModel.uiState.value is Success)
}"""
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "✓ 각 테스트가 독립적이므로 Loading 생략 변경에 영향 없음\n✓ 구현 세부사항이 아닌 상태 자체를 검증",
                color = Color(0xFF88CC88),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ─── Turbine 적합한 사용 카드 ─────────────────────────────────────────────────

@Composable
private fun TurbineGoodUseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SharedFlow 이벤트 스트림 검증",
                color = Color(0xFF7B9FFF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            CodeSnippet(
                code = """// SharedFlow는 replaying이 없어 .value로 검증 불가
// → Turbine의 awaitItem()이 적합한 영역
@Test
fun `fetchData 성공 시 ShowToast 이벤트 발행`() = runTest {
    viewModel.events.test {
        viewModel.fetchData(shouldFail = false)
        advanceUntilIdle()
        val event = awaitItem()
        assertTrue(event is ShowToast)
    }
}"""
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "✓ SharedFlow는 최신값 보존이 없어 .value 검증 불가\n✓ 이벤트 순서가 중요한 경우 Turbine이 적합\n✓ 단방향 이벤트(Toast, Navigation)에 활용",
                color = Color(0xFF8888CC),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ─── 코드 스니펫 표시 ─────────────────────────────────────────────────────────

@Composable
private fun CodeSnippet(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFFE0E0E0),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 17.sp
        )
    }
}
