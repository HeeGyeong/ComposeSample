package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedContentExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                text = "AnimatedContent 심화",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { TabSwitchSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { CounterSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { StateTransitionSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { TransitionSpecGallery() }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 1: 탭 전환 (슬라이드 방향 자동 전환)
// ─────────────────────────────────────────────────────────

@Composable
private fun TabSwitchSection() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("홈", "검색", "프로필")

    SectionCard(title = "1. 탭 전환 — 방향 인식 슬라이드") {
        Text(
            text = "탭 인덱스 증가 시 왼쪽→오른쪽, 감소 시 오른쪽→왼쪽으로 슬라이드합니다.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                // 인덱스 증가: 왼쪽에서 진입 / 오른쪽으로 퇴장
                // 인덱스 감소: 오른쪽에서 진입 / 왼쪽으로 퇴장
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "tab_transition"
        ) { tab ->
            TabContent(tab = tab, tabs = tabs)
        }
    }
}

@Composable
private fun TabContent(tab: Int, tabs: List<String>) {
    val colors = listOf(Color(0xFFE3F2FD), Color(0xFFE8F5E9), Color(0xFFFCE4EC))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(colors[tab], RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${tabs[tab]} 화면 콘텐츠",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 2: 숫자 카운터 (위아래 슬라이드)
// ─────────────────────────────────────────────────────────

@Composable
private fun CounterSection() {
    var count by remember { mutableIntStateOf(0) }

    SectionCard(title = "2. 숫자 카운터 — 증감 방향 슬라이드") {
        Text(
            text = "+버튼: 위에서 아래로 진입 / -버튼: 아래에서 위로 진입",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { count-- },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) { Text("-", fontSize = 20.sp) }

            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = count,
                    transitionSpec = {
                        // 증가 시: 아래에서 진입, 위로 퇴장
                        // 감소 시: 위에서 진입, 아래로 퇴장
                        if (targetState > initialState) {
                            slideInVertically { it } + fadeIn() togetherWith
                                    slideOutVertically { -it } + fadeOut()
                        } else {
                            slideInVertically { -it } + fadeIn() togetherWith
                                    slideOutVertically { it } + fadeOut()
                        }
                    },
                    label = "counter_transition"
                ) { value ->
                    Text(
                        text = "$value",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = { count++ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) { Text("+", fontSize = 20.sp) }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 3: 상태 전환 UI (로딩 → 성공 → 에러)
// ─────────────────────────────────────────────────────────

private enum class UiState { IDLE, LOADING, SUCCESS, ERROR }

@Composable
private fun StateTransitionSection() {
    var uiState by remember { mutableStateOf(UiState.IDLE) }
    val scope = rememberCoroutineScope()

    SectionCard(title = "3. 상태 전환 — Scale + Fade") {
        Text(
            text = "로딩 → 성공/에러 상태 전환 시 scale + fade 애니메이션으로 자연스럽게 전환합니다.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 상태 전환 UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    scaleIn(initialScale = 0.85f) + fadeIn(tween(300)) togetherWith
                            scaleOut(targetScale = 0.85f) + fadeOut(tween(200))
                },
                label = "state_transition"
            ) { state ->
                when (state) {
                    UiState.IDLE -> IdleContent()
                    UiState.LOADING -> LoadingContent()
                    UiState.SUCCESS -> SuccessContent()
                    UiState.ERROR -> ErrorContent()
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        uiState = UiState.LOADING
                        delay(1500)
                        uiState = UiState.SUCCESS
                    }
                },
                enabled = uiState == UiState.IDLE || uiState == UiState.ERROR || uiState == UiState.SUCCESS,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) { Text("성공 시뮬레이션", fontSize = 12.sp) }

            Button(
                onClick = {
                    scope.launch {
                        uiState = UiState.LOADING
                        delay(1500)
                        uiState = UiState.ERROR
                    }
                },
                enabled = uiState == UiState.IDLE || uiState == UiState.ERROR || uiState == UiState.SUCCESS,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) { Text("에러 시뮬레이션", fontSize = 12.sp) }

            Button(
                onClick = { uiState = UiState.IDLE },
                enabled = uiState != UiState.IDLE && uiState != UiState.LOADING,
                modifier = Modifier.weight(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) { Text("리셋", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun IdleContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🚀", fontSize = 40.sp)
        Text("버튼을 눌러 시작하세요", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun LoadingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("처리 중...", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun SuccessContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFF43A047), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", fontSize = 28.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("성공!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
    }
}

@Composable
private fun ErrorContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFE53935), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✕", fontSize = 28.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("오류 발생", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 4: transitionSpec 갤러리
// ─────────────────────────────────────────────────────────

@Composable
private fun TransitionSpecGallery() {
    data class TransitionItem(val label: String, val color: Color)

    val items = listOf(
        TransitionItem("A", Color(0xFF5C6BC0)),
        TransitionItem("B", Color(0xFF26A69A)),
        TransitionItem("C", Color(0xFFF57C00)),
        TransitionItem("D", Color(0xFFE91E63)),
    )

    var currentIndex by remember { mutableIntStateOf(0) }

    // 선택된 transitionSpec 종류
    var specIndex by remember { mutableIntStateOf(0) }
    val specLabels = listOf("Fade", "Slide H", "Slide V", "Scale", "Expand")

    SectionCard(title = "4. transitionSpec 갤러리") {
        Text(
            text = "전환 효과를 선택하고 Next 버튼으로 콘텐츠를 전환해보세요.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 효과 선택 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            specLabels.forEachIndexed { idx, label ->
                Button(
                    onClick = { specIndex = idx },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (specIndex == idx) MaterialTheme.colorScheme.primary
                        else Color.LightGray
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                ) { Text(label, fontSize = 10.sp) }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 전환 데모 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    when (specIndex) {
                        0 -> fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                        1 -> slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        2 -> slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                        3 -> scaleIn(initialScale = 0.5f) + fadeIn() togetherWith
                                scaleOut(targetScale = 1.5f) + fadeOut()
                        else -> expandVertically() + fadeIn() togetherWith
                                fadeOut() using SizeTransform(clip = false)
                    }
                },
                label = "gallery_transition"
            ) { idx ->
                val item = items[idx]
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(item.color, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.label, fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { currentIndex = (currentIndex + 1) % items.size },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Next →") }

        Spacer(modifier = Modifier.height(8.dp))

        InfoBox(
            "togetherWith: 진입(enter)과 퇴장(exit) 애니메이션을 하나의 ContentTransform으로 결합\n" +
                    "SizeTransform: 콘텐츠 크기가 달라질 때 크기 변화에 애니메이션 적용\n" +
                    "label 파라미터: Android Studio 애니메이션 프리뷰에서 식별자로 사용"
        )
    }
}

// ─────────────────────────────────────────────────────────
// 공통 컴포넌트
// ─────────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun InfoBox(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color(0xFF5D4037), lineHeight = 18.sp)
    }
}
