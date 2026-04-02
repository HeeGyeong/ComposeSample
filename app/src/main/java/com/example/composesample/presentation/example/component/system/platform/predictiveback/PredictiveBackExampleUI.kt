package com.example.composesample.presentation.example.component.system.platform.predictiveback

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PredictiveBackExampleUI(onBackEvent: () -> Unit) {
    var showPredictiveDemo by remember { mutableStateOf(false) }

    if (showPredictiveDemo) {
        PredictiveBackDemoScreen(onExit = { showPredictiveDemo = false })
    } else {
        PredictiveBackMainScreen(
            onBackEvent = onBackEvent,
            onShowDemo = { showPredictiveDemo = true }
        )
    }
}

@Composable
private fun PredictiveBackMainScreen(
    onBackEvent: () -> Unit,
    onShowDemo: () -> Unit
) {
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
                text = "Predictive Back Gesture",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoCard(
                    title = "Predictive Back Gesture란?",
                    description = "Android 14(API 34)+에서 도입된 예측형 뒤로 가기 제스처.\n" +
                            "엣지 스와이프 중 목적지 화면을 미리 보여주어 사용자가 제스처를 확정하거나 취소할 수 있습니다.",
                    bgColor = Color(0xFFE8F5E9)
                )
            }

            item { HorizontalDivider() }

            item {
                SectionHeader("BackHandler")
            }

            item {
                CodeCard(
                    title = "기본 BackHandler — 단순 인터셉트",
                    code = """BackHandler(enabled = true) {
    // 뒤로 가기 이벤트 가로채기
    // 계층에서 가장 안쪽 BackHandler가 우선 처리
    handleBack()
}"""
                )
            }

            item {
                InfoCard(
                    title = "BackHandler 특징",
                    description = "• enabled 파라미터로 활성/비활성 제어\n" +
                            "• 예측형 제스처 진행 상황(progress)은 알 수 없음\n" +
                            "• 모든 API 레벨에서 사용 가능",
                    bgColor = Color(0xFFFFF8E1)
                )
            }

            item { HorizontalDivider() }

            item {
                SectionHeader("PredictiveBackHandler (API 34+)")
            }

            item {
                CodeCard(
                    title = "PredictiveBackHandler 패턴",
                    code = """PredictiveBackHandler(enabled = true) { progress ->
    // progress: Flow<BackEventCompat>
    try {
        progress.collect { backEvent ->
            // 제스처 진행 중: 실시간 애니메이션 업데이트
            // backEvent.progress: 0.0f ~ 1.0f
            // backEvent.swipeEdge: EDGE_LEFT / EDGE_RIGHT
            // backEvent.touchX / touchY: 터치 위치
            scaleValue = 1f - backEvent.progress * 0.1f
        }
        // collect 완료 = 제스처 확정 → 화면 전환
        navigateBack()
    } catch (e: CancellationException) {
        // Flow 취소 = 제스처 취소 → 원래 상태 복원
        scaleValue = 1f
    }
}"""
                )
            }

            item {
                InfoCard(
                    title = "PredictiveBackHandler 특징",
                    description = "• progress: Flow<BackEventCompat> — 제스처 진행값 스트림\n" +
                            "• collect 완료 = 제스처 확정 (화면 전환 실행)\n" +
                            "• CancellationException = 제스처 취소 (화면 유지)\n" +
                            "• BackEventCompat.progress: 0.0(시작) ~ 1.0(완료)\n" +
                            "• BackEventCompat.swipeEdge: 왼쪽/오른쪽 구분 가능",
                    bgColor = Color(0xFFE3F2FD)
                )
            }

            item { HorizontalDivider() }

            item { SectionHeader("비교 요약") }

            item { ComparisonCard() }

            item { HorizontalDivider() }

            item { SectionHeader("데모") }

            item {
                InfoCard(
                    title = "데모 화면 안내",
                    description = "데모 화면에서 왼쪽 엣지를 스와이프하면\n" +
                            "PredictiveBackHandler가 제스처 진행률을 실시간으로 감지하여\n" +
                            "화면 축소 애니메이션으로 시각화합니다.",
                    bgColor = Color(0xFFF3E5F5)
                )
            }

            item {
                Button(
                    onClick = onShowDemo,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("PredictiveBackHandler 데모 화면 열기", color = Color.White)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PredictiveBackDemoScreen(onExit: () -> Unit) {
    var backProgress by remember { mutableFloatStateOf(0f) }
    val scale by animateFloatAsState(
        targetValue = 1f - backProgress * 0.15f,
        label = "predictive_back_scale"
    )

    // PredictiveBackHandler: 엣지 스와이프 제스처 진행 상황을 Flow로 수신
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { backEvent ->
                backProgress = backEvent.progress
            }
            // collect 완료 = 제스처 확정 → 이전 화면으로 복귀
            onExit()
        } catch (e: Exception) {
            // 제스처 취소 → 원래 상태(scale = 1f) 복원
            backProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .background(Color(0xFF1976D2)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "PredictiveBack 데모",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "왼쪽 엣지에서 스와이프하면\n화면이 축소되며 진행률을 표시합니다\n\n손을 떼면 완료, 되돌리면 취소됩니다",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Text(
                text = "진행률: ${(backProgress * 100).toInt()}%",
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("닫기 (버튼으로 이동)", color = Color(0xFF1976D2))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun InfoCard(title: String, description: String, bgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun CodeCard(title: String, code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = Color(0xFF80CBC4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = code,
                color = Color(0xFFCFD8DC),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ComparisonRow("항목", "BackHandler", "PredictiveBackHandler", isHeader = true)
            HorizontalDivider()
            ComparisonRow("제스처 진행률", "❌", "✅ (0~1)")
            ComparisonRow("제스처 취소 감지", "❌", "✅ (CancellationException)")
            ComparisonRow("스와이프 방향", "❌", "✅ (LEFT/RIGHT)")
            ComparisonRow("터치 좌표", "❌", "✅ (touchX/Y)")
            ComparisonRow("실시간 애니메이션", "수동 구현 어려움", "Flow로 자연스럽게")
            ComparisonRow("최소 API 레벨", "모든 버전", "API 34+ 최적 동작")
        }
    }
}

@Composable
private fun ComparisonRow(
    item: String,
    backHandler: String,
    predictive: String,
    isHeader: Boolean = false
) {
    val weight = if (isHeader) FontWeight.Bold else FontWeight.Normal
    val size = if (isHeader) 12.sp else 11.sp
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = item, modifier = Modifier.weight(1.3f), fontWeight = weight, fontSize = size)
        Text(text = backHandler, modifier = Modifier.weight(1f), fontWeight = weight, fontSize = size)
        Text(text = predictive, modifier = Modifier.weight(1f), fontWeight = weight, fontSize = size)
    }
}
