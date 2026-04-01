package com.example.composesample.presentation.example.component.architecture.pattern.remember

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun RememberPatternsExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 헤더
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
                text = "Remember Patterns",
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
            item { RememberSaveableSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { RememberUpdatedStateSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { DerivedStateOfSection() }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 1: rememberSaveable
// ─────────────────────────────────────────────────────────

@Composable
private fun RememberSaveableSection() {
    // remember: 리컴포지션 생존 O, 화면 회전 생존 X
    var rememberCount by remember { mutableIntStateOf(0) }
    // rememberSaveable: 리컴포지션 생존 O, 화면 회전 생존 O (Bundle에 저장)
    var saveableCount by rememberSaveable { mutableIntStateOf(0) }

    SectionCard(title = "1. rememberSaveable") {
        Text(
            text = "화면을 회전하면 remember 카운터는 0으로 초기화되지만\nrememberSaveable 카운터는 값을 유지합니다.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // remember 카운터
        CounterRow(
            label = "remember",
            count = rememberCount,
            labelColor = Color(0xFFE53935),
            onIncrement = { rememberCount++ },
            onReset = { rememberCount = 0 }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // rememberSaveable 카운터
        CounterRow(
            label = "rememberSaveable",
            count = saveableCount,
            labelColor = Color(0xFF1E88E5),
            onIncrement = { saveableCount++ },
            onReset = { saveableCount = 0 }
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoBox(
            text = "rememberSaveable은 내부적으로 savedInstanceState를 활용합니다.\n" +
                    "Bundle에 저장 가능한 타입(Int, String 등)은 자동 직렬화되며,\n" +
                    "커스텀 타입은 Saver를 직접 구현해야 합니다."
        )
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 2: rememberUpdatedState
// ─────────────────────────────────────────────────────────

@Composable
private fun RememberUpdatedStateSection() {
    var message by remember { mutableStateOf("초기 메시지") }
    var logWithoutUpdated by remember { mutableStateOf("대기 중...") }
    var logWithUpdated by remember { mutableStateOf("대기 중...") }
    var triggerCount by remember { mutableIntStateOf(0) }

    /**
     * ❌ 문제 패턴: LaunchedEffect 안에서 message를 직접 캡처
     * key=Unit → 최초 1회만 실행. 이후 message가 바뀌어도 이펙트 안의 값은 업데이트되지 않음.
     */
    val capturedMessage = message // 캡처 시점의 값
    LaunchedEffect(triggerCount) {
        delay(2000)
        logWithoutUpdated = "2초 후 캡처된 값: \"$capturedMessage\""
    }

    /**
     * ✅ 올바른 패턴: rememberUpdatedState로 감싸면
     * LaunchedEffect가 재시작되지 않아도 항상 최신 message를 참조
     */
    val updatedMessage by rememberUpdatedState(message)
    LaunchedEffect(triggerCount) {
        delay(2000)
        logWithUpdated = "2초 후 최신 값: \"$updatedMessage\""
    }

    SectionCard(title = "2. rememberUpdatedState") {
        Text(
            text = "LaunchedEffect 실행 중에 외부 값이 바뀌면 어떻게 될까요?\n" +
                    "아래에서 메시지를 바꾸고 '이펙트 트리거' 버튼을 누른 뒤,\n" +
                    "2초 안에 메시지를 다시 변경해보세요.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { message = "변경된 메시지 A" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) { Text("메시지 A로 변경", fontSize = 12.sp) }
            Button(
                onClick = { message = "변경된 메시지 B" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) { Text("메시지 B로 변경", fontSize = 12.sp) }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { triggerCount++ },
            modifier = Modifier.fillMaxWidth()
        ) { Text("이펙트 트리거 (2초 후 결과 표시)") }

        Spacer(modifier = Modifier.height(8.dp))

        Text("현재 메시지: \"$message\"", fontSize = 13.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        ResultBox(label = "❌ 일반 캡처", result = logWithoutUpdated, color = Color(0xFFFFEBEE))
        Spacer(modifier = Modifier.height(4.dp))
        ResultBox(label = "✅ rememberUpdatedState", result = logWithUpdated, color = Color(0xFFE3F2FD))

        Spacer(modifier = Modifier.height(12.dp))

        InfoBox(
            text = "rememberUpdatedState는 내부적으로 SideEffect로 값을 업데이트합니다.\n" +
                    "타이머, 애니메이션, 콜백 등 오래 실행되는 이펙트에서 유용합니다."
        )
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 3: derivedStateOf
// ─────────────────────────────────────────────────────────

@Composable
private fun DerivedStateOfSection() {
    var count by remember { mutableIntStateOf(0) }

    /**
     * ✅ 올바른 패턴: remember { derivedStateOf { } }
     * count가 바뀌어도 isEven의 결과가 실제로 달라질 때만 리컴포지션 발생
     */
    val isEven by remember { derivedStateOf { count % 2 == 0 } }

    /**
     * ✅ 올바른 패턴: 조건 계산 최적화
     * count가 0~4 구간에서 변할 때 buttonEnabled 값이 바뀌지 않으면 리컴포지션 안 함
     */
    val buttonEnabled by remember { derivedStateOf { count >= 5 } }

    /**
     * ❌ 잘못된 패턴 (주석): remember(count) { derivedStateOf { } }
     * key로 count를 넘기면 count가 바뀔 때마다 derivedStateOf 블록 자체가 재생성됨.
     * derivedStateOf의 스마트 재계산 효과가 사라지고, 오히려 일반 remember(count)와 동일해짐.
     */

    SectionCard(title = "3. derivedStateOf") {
        Text(
            text = "다른 State에서 계산된 값은 derivedStateOf로 감싸세요.\n" +
                    "결과가 실제로 달라질 때만 리컴포지션이 발생합니다.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("현재 카운트: $count", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        // isEven: count가 홀수↔짝수 경계를 넘을 때만 리컴포지션
        Text(
            text = "짝수 여부 (isEven): $isEven",
            fontSize = 14.sp,
            color = if (isEven) Color(0xFF1E88E5) else Color(0xFFE53935)
        )
        Spacer(modifier = Modifier.height(4.dp))

        // buttonEnabled: count < 5 구간에서는 아무리 변해도 리컴포지션 없음
        Text(
            text = "버튼 활성화 (count >= 5): $buttonEnabled",
            fontSize = 14.sp,
            color = if (buttonEnabled) Color(0xFF43A047) else Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { count++ }) { Text("+1") }
            Button(onClick = { count-- }) { Text("-1") }
            Button(
                onClick = { count = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) { Text("리셋") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* count >= 5 일 때만 활성화 */ },
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxWidth()
        ) { Text("5 이상일 때만 활성화되는 버튼") }

        Spacer(modifier = Modifier.height(12.dp))

        InfoBox(
            text = "❌ 잘못된 패턴:\n" +
                    "  remember(count) { derivedStateOf { count % 2 == 0 } }\n" +
                    "  → key가 바뀔 때마다 블록이 재생성되어 최적화 효과 없음\n\n" +
                    "✅ 올바른 패턴:\n" +
                    "  remember { derivedStateOf { count % 2 == 0 } }\n" +
                    "  → count 변화를 내부에서 감지, 결과가 바뀔 때만 리컴포지션"
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
private fun CounterRow(
    label: String,
    count: Int,
    labelColor: Color,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label: $count",
            fontSize = 14.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(
                onClick = onIncrement,
                colors = ButtonDefaults.buttonColors(containerColor = labelColor)
            ) { Text("+1", fontSize = 12.sp) }
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) { Text("리셋", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun ResultBox(label: String, result: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Text(result, fontSize = 13.sp, color = Color.DarkGray)
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
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF5D4037),
            lineHeight = 18.sp
        )
    }
}
