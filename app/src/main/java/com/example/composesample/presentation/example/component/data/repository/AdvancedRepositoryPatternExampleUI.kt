package com.example.composesample.presentation.example.component.data.repository

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import com.example.domain.model.ArticleResult
import com.example.domain.model.ArticleSource
import org.koin.androidx.compose.koinViewModel

private val PRESET_IDS = listOf("A1", "A2", "A3")

/**
 * Advanced Repository Pattern 예제
 * - Memory → Disk → Network 3계층 우선순위 해석 + 상위 계층으로의 cache population 을 시연.
 * - 참고 자료(핵심 개념)는 exampleGuide.kt 의 "AdvancedRepositoryPattern" 섹션 참고.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedRepositoryPatternExampleUI(onBackEvent: () -> Unit) {
    val viewModel: AdvancedRepositoryPatternViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf(PRESET_IDS.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Advanced Repository Pattern",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item {
                DemoCard(
                    selectedId = selectedId,
                    onIdSelected = { selectedId = it },
                    uiState = uiState,
                    onFetch = { viewModel.fetchArticle(selectedId) },
                    onForceRefresh = { viewModel.fetchArticle(selectedId, forceRefresh = true) },
                    onInvalidateMemory = viewModel::invalidateMemory,
                    onInvalidateDisk = viewModel::invalidateDisk,
                    onClearLogs = viewModel::clearLogs
                )
            }
            item { GuideCard() }
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "다중 소스 우선순위 해석", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "조회는 Memory → Disk → Network 순으로 우선순위를 매겨 해석한다. " +
                    "가장 가까운(빠른) 계층에 데이터가 있으면 즉시 반환하고, 없으면 다음 계층으로 넘어간다. " +
                    "하위 계층에서 찾은 결과는 상위 계층에도 채워 넣어(cache population) 다음 조회를 가속한다.",
                fontSize = 13.sp,
                color = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Memory(0ms, 인메모리 Map) → Disk(300ms, 시뮬레이션) → Network(900ms, 시뮬레이션)",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF3A5BD4)
            )
        }
    }
}

@Composable
private fun DemoCard(
    selectedId: String,
    onIdSelected: (String) -> Unit,
    uiState: RepositoryPatternUiState,
    onFetch: () -> Unit,
    onForceRefresh: () -> Unit,
    onInvalidateMemory: () -> Unit,
    onInvalidateDisk: () -> Unit,
    onClearLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "조회 대상 선택", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRESET_IDS.forEach { id ->
                    FilterChip(
                        selected = selectedId == id,
                        onClick = { onIdSelected(id) },
                        label = { Text(id) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFCDE3FF))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onFetch,
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A5BD4))
                ) { Text(if (uiState.isLoading) "조회 중..." else "조회") }
                Button(
                    onClick = onForceRefresh,
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("강제 새로고침") }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onInvalidateMemory,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF888888))
                ) { Text("메모리 무효화") }
                Button(
                    onClick = onInvalidateDisk,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF888888))
                ) { Text("디스크 무효화") }
                Button(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF888888))
                ) { Text("로그 지우기") }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ResultBox(uiState.lastResult, uiState.isLoading)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "타임라인 로그", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            TimelineBox(logs = uiState.logs)
        }
    }
}

@Composable
private fun ResultBox(lastResult: ArticleResult?, isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            isLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "해석 중...", fontSize = 12.sp, color = Color(0xFF888888))
            }

            lastResult == null -> Text(
                text = "조회 버튼을 눌러 결과를 확인하세요.",
                fontSize = 12.sp,
                color = Color(0xFF888888)
            )

            else -> Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SourceBadge(lastResult.source)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${lastResult.latencyMs}ms",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF555555)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = lastResult.article.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = lastResult.article.body, fontSize = 12.sp, color = Color(0xFF555555))
            }
        }
    }
}

@Composable
private fun SourceBadge(source: ArticleSource) {
    val (bg, label) = when (source) {
        ArticleSource.MEMORY -> Color(0xFFC8E6C9) to "MEMORY"
        ArticleSource.DISK -> Color(0xFFFFE0A3) to "DISK"
        ArticleSource.NETWORK -> Color(0xFFFFCDD2) to "NETWORK"
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

/** 실시간 타임라인 로그를 보여주는 다크 박스. */
@Composable
private fun TimelineBox(logs: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (logs.isEmpty()) {
            Text(
                text = "조회/무효화 버튼을 눌러 우선순위 해석 흐름을 확인하세요.",
                color = Color(0xFF888888),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        } else {
            Column {
                logs.forEach { line ->
                    Text(
                        text = line,
                        color = lineColor(line),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

private fun lineColor(line: String): Color = when {
    line.contains("MEMORY") -> Color(0xFF81C784)
    line.contains("DISK") -> Color(0xFFFFD54F)
    line.contains("NETWORK") -> Color(0xFFEF9A9A)
    else -> Color(0xFFCFD8DC)
}

@Composable
private fun GuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "구현 노트", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            CodeBlock(
                code = "suspend fun getArticle(id, forceRefresh): ArticleResult {\n" +
                    "    if (!forceRefresh) {\n" +
                    "        memory[id]?.let { return Result(it, MEMORY, 0) }\n" +
                    "        disk[id]?.let { memory[id] = it; return Result(it, DISK, t) }\n" +
                    "    }\n" +
                    "    val fetched = fetchFromNetwork(id)\n" +
                    "    disk[id] = fetched; memory[id] = fetched\n" +
                    "    return Result(fetched, NETWORK, t)\n" +
                    "}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"디스크 무효화\"만 누르면 메모리가 살아있는 동안은 계속 MEMORY 히트가 나온다 — " +
                    "메모리까지 함께 무효화해야 DISK(또는 둘 다 비우면 NETWORK)로 내려가는 걸 확인할 수 있다.",
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ArticleRepository 인터페이스는 domain 모듈에 있다 — ArticleData 가 순수 Kotlin 모델이라 " +
                    "가능한 것으로, UserData(Room @Entity)라 data 레이어에 둬야 했던 UserCacheRepository 예제와 대조된다.",
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(8.dp))
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
