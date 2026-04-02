package com.example.composesample.presentation.example.component.architecture.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ComposeSnapshotExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4FF))
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1565C0))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
            Text(
                text = "Compose Snapshot System",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // 개요 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Compose Snapshot 시스템",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "State<T>의 내부 동작 원리. 모든 mutableStateOf, remember는 Snapshot 시스템 위에서 동작합니다.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                // derivedStateOf 예제
                DerivedStateSection()
            }

            item {
                // 개념 카드: Snapshot이란?
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📸 Snapshot이란?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Snapshot은 State 값의 특정 시점 복사본입니다. " +
                                    "Compose 런타임은 각 Recomposition을 격리된 Snapshot 내에서 실행하여 " +
                                    "상태 변경이 완료될 때까지 다른 스냅샷에 보이지 않게 합니다.",
                            fontSize = 13.sp,
                            color = Color(0xFF444444),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        SnapshotConceptItem(
                            title = "GlobalSnapshot",
                            description = "앱 전체의 최신 상태를 나타내는 루트 스냅샷. mutableStateOf 값이 최종 반영되는 곳"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SnapshotConceptItem(
                            title = "MutableSnapshot",
                            description = "격리된 쓰기 가능 스냅샷. 변경 내용을 apply() 전까지 외부에서 볼 수 없음"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SnapshotConceptItem(
                            title = "withMutableSnapshot { }",
                            description = "블록 내 상태 변경을 원자적으로 처리. 블록 종료 시 GlobalSnapshot에 자동 apply"
                        )
                    }
                }
            }

            item {
                // derivedStateOf 개념 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚡ derivedStateOf",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "하나 이상의 State에서 파생된 값. 의존 State가 변경될 때만 재계산하여 불필요한 Recomposition 방지.",
                            fontSize = 13.sp,
                            color = Color(0xFF444444),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E))
                        ) {
                            Text(
                                text = "val isOverThreshold by remember {\n" +
                                        "    derivedStateOf { counter > 5 }\n" +
                                        "}",
                                fontSize = 12.sp,
                                color = Color(0xFF90CAF9),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✅ counter가 1→2→3 변해도 isOverThreshold는 false 유지 → Recomposition 없음\n" +
                                    "✅ counter가 5→6 변할 때만 true로 바뀌며 Recomposition 발생",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                // snapshotFlow 비교 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔄 snapshotFlow vs derivedStateOf",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ComparisonRow("snapshotFlow", "State → Flow 변환. 코루틴에서 State 변화 관찰")
                        Spacer(modifier = Modifier.height(4.dp))
                        ComparisonRow("derivedStateOf", "State → 파생 State. Composition 내 계산 최적화")
                        Spacer(modifier = Modifier.height(4.dp))
                        ComparisonRow("remember { }", "Composition 범위 내 값 캐싱. 재계산 없음")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DerivedStateSection() {
    var counter by remember { mutableIntStateOf(0) }
    // derivedStateOf: counter > 5 일 때만 재계산
    val isOverThreshold by remember { derivedStateOf { counter > 5 } }
    val statusText by remember { derivedStateOf { if (counter > 5) "임계값 초과!" else "임계값 이하" } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverThreshold) Color(0xFFE8F5E9) else Color(0xFFFFF9C4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🧪 derivedStateOf 인터랙티브 예제",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "카운터: $counter",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOverThreshold) Color(0xFF2E7D32) else Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                fontSize = 14.sp,
                color = if (isOverThreshold) Color(0xFF2E7D32) else Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { counter++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("+ 증가")
                }
                Button(
                    onClick = { counter = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text("리셋")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "counter가 5를 넘을 때만 derivedStateOf 재계산 → Recomposition 최소화",
                fontSize = 11.sp,
                color = Color(0xFF777777),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SnapshotConceptItem(title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
        Text(
            text = "  $description",
            fontSize = 12.sp,
            color = Color(0xFF555555),
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun ComparisonRow(name: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = "• $name",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1),
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = ": $desc",
            fontSize = 13.sp,
            color = Color(0xFF444444),
            lineHeight = 18.sp
        )
    }
}
