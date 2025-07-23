package com.example.composesample.presentation.example.component.architecture.state

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

/**
 * SnapshotFlow vs collectAsState Example
 *
 * 주요 차이점과 올바른 사용법:
 * 1. collectAsState: Flow → State 변환, UI 업데이트용
 * 2. snapshotFlow: Compose state → Flow 변환, side-effect용
 * 3. collectAsStateWithLifecycle: 라이프사이클 인식 버전
 */

@Composable
fun SnapshotFlowExampleUI(
    onBackEvent: () -> Unit,
    viewModel: SnapshotFlowExampleViewModel = koinViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "SnapshotFlow vs collectAsState",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                UsageGuideCard()
            }

            item {
                CollectAsStateExampleCard(viewModel)
            }

            item {
                CollectAsStateWithLifecycleCard(viewModel)
            }

            item {
                SnapshotFlowExampleCard(viewModel)
            }

            item {
                WrongUsageExampleCard(viewModel)
            }

            item {
                LogMessagesCard(viewModel)
            }
        }
    }
}

@Composable
private fun UsageGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📚 사용 가이드",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "✅ collectAsState 올바른 사용:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF388E3C)
            )
            Text(
                text = "• ViewModel → UI 데이터 표시\n• 저빈도 업데이트 데이터",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "✅ snapshotFlow 올바른 사용:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF388E3C)
            )
            Text(
                text = "• Compose state → side-effect\n• 스크롤/제스처 추적\n• 로깅, 분석",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "❌ 피해야 할 사용:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD32F2F)
            )
            Text(
                text = "• snapshotFlow + collectAsState 조합\n• 고빈도 데이터를 collectAsState로\n• UI 업데이트에 snapshotFlow 사용",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun CollectAsStateExampleCard(viewModel: SnapshotFlowExampleViewModel) {
    // ✅ 올바른 사용: ViewModel → UI 데이터 바인딩
    val userInputCount by viewModel.userInputCount.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF1F8E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✅ collectAsState 올바른 사용",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ViewModel 데이터를 UI에 표시",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "사용자 입력 횟수: $userInputCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = { viewModel.incrementUserInput() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                ) {
                    Text("입력", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CollectAsStateWithLifecycleCard(viewModel: SnapshotFlowExampleViewModel) {
    // ✅ 더 나은 방법: 라이프사이클 인식 버전
    val animationProgress by viewModel.animationProgress.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // ViewModel에 라이프사이클 설정
    LaunchedEffect(Unit) {
        viewModel.setLifecycleOwner(lifecycleOwner)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✅ collectAsStateWithLifecycle",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "라이프사이클 인식하여 자원 절약",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 진행도 표시
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(animationProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "애니메이션 진행도",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "백그라운드에서 자동 일시정지",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = animationProgress,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun SnapshotFlowExampleCard(viewModel: SnapshotFlowExampleViewModel) {
    val lazyListState = rememberLazyListState()
    var scrollPosition by remember { mutableStateOf(0) }

    // ✅ snapshotFlow 올바른 사용: side-effect용
    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                scrollPosition = index
                viewModel.addLogMessage("스크롤 위치 변경: $index")
            }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✅ snapshotFlow 올바른 사용",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "스크롤 위치 추적 (side-effect)",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "현재 스크롤 위치: $scrollPosition",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 스크롤 가능한 리스트
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                items(50) { index ->
                    Text(
                        text = "항목 $index",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WrongUsageExampleCard(viewModel: SnapshotFlowExampleViewModel) {
    // ❌ 잘못된 사용: 고빈도 데이터를 collectAsState로
    // 이것은 매 100ms마다 recomposition을 일으킴!
    val highFrequencyData by viewModel.highFrequencyData.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "❌ collectAsState 잘못된 사용",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "고빈도 업데이트 데이터 (성능 문제 발생)",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "고빈도 카운터: $highFrequencyData",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD32F2F)
            )

            Text(
                text = "⚠️ 매 100ms마다 recomposition 발생!",
                fontSize = 12.sp,
                color = Color(0xFFD32F2F),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun LogMessagesCard(viewModel: SnapshotFlowExampleViewModel) {
    val logMessages by viewModel.logMessages.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Side-effect 로그",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { viewModel.clearLogs() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Clear", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val recentMessages = logMessages.takeLast(20)
                items(recentMessages.size) { index ->
                    Text(
                        text = recentMessages[index],
                        fontSize = 10.sp,
                        color = Color.Green,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SnapshotFlowExamplePreview() {
    SnapshotFlowExampleUI(
        onBackEvent = {}
    )
} 