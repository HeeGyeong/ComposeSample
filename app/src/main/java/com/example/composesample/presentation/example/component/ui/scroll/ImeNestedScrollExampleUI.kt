package com.example.composesample.presentation.example.component.ui.scroll

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * IME Interactive Control Example
 *
 * - Modifier.imeNestedScroll(): 리스트 스크롤 제스처의 남는 delta를 키보드 표시/숨김 애니메이션으로 이어받아
 *   드래그하는 손가락을 따라 키보드가 함께 움직이도록 만든다 (API 30+, 그 이하는 no-op).
 * - WindowInsets.Companion.imeAnimationSource / imeAnimationTarget: 키보드 애니메이션의 시작/도착 인셋 값.
 *   현재값(WindowInsets.ime)이 그 사이 어디쯤인지 계산하면 실제 키보드 애니메이션 진행률을 그대로 커스텀 UI에 동기화할 수 있다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImeNestedScrollExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "IME Interactive Control Example",
            onBackIconClicked = onBackEvent
        )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ImeSdkWarningCard()
        }

        ImeAnimationProgressCard()

        Box(
            modifier = Modifier
                .weight(1f)
                .imeNestedScroll()
        ) {
            val messages = remember {
                (1..30).map { index -> "메시지 $index — 맨 위까지 스크롤한 뒤 계속 아래로 드래그해서 키보드를 딸려 내려보세요" }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(messages) { message ->
                    ImeChatMessageBubble(message)
                }
            }
        }

        ImeMessageInputBar()
    }
}

@Composable
private fun ImeSdkWarningCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "⚠️ imeNestedScroll()은 API 30(R) 미만에서 no-op입니다. 이 기기(API ${Build.VERSION.SDK_INT})에서는 " +
                "리스트를 드래그해도 키보드가 함께 움직이지 않고, 일반 스크롤로만 동작합니다.",
            fontSize = 11.sp,
            color = Color(0xFFE65100),
            modifier = Modifier.padding(12.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImeAnimationProgressCard() {
    val density = LocalDensity.current
    val sourceBottomPx = WindowInsets.imeAnimationSource.getBottom(density)
    val targetBottomPx = WindowInsets.imeAnimationTarget.getBottom(density)
    val currentBottomPx = WindowInsets.ime.getBottom(density)

    val range = (targetBottomPx - sourceBottomPx).toFloat()
    val progress = when {
        range != 0f -> ((currentBottomPx - sourceBottomPx) / range).coerceIn(0f, 1f)
        currentBottomPx > 0 -> 1f
        else -> 0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "imeAnimationSource / imeAnimationTarget 실시간 동기화",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF3700B3)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "키보드를 열고 닫으면 progress가 실제 IME 애니메이션 진행 속도 그대로 움직입니다",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ImeInsetValueColumn(label = "source", valuePx = sourceBottomPx, color = Color(0xFFE53E3E))
                ImeInsetValueColumn(label = "current(ime)", valuePx = currentBottomPx, color = Color(0xFF38A169))
                ImeInsetValueColumn(label = "target", valuePx = targetBottomPx, color = Color(0xFF3182CE))
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "progress = ${(progress * 100).toInt()}%  (source → target 사이 현재 위치)",
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "커스텀 UI를 애니메이션에 직접 동기화한 예 (progress만큼 위로 딸려 올라옴)",
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(y = (-(progress * 24)).dp)
                        .background(Color(0xFF6200EE), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "동기화된 배지", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ImeInsetValueColumn(label: String, valuePx: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
        Text(text = "${valuePx}px", fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ImeChatMessageBubble(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFF1F1F1), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = message, fontSize = 13.sp, color = Color(0xFF333333))
        }
    }
}

@Composable
private fun ImeMessageInputBar() {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text(text = "메시지를 입력하세요", fontSize = 13.sp) },
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { text = "" },
            modifier = Modifier.background(Color(0xFF6200EE), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "전송",
                tint = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImeNestedScrollExampleUIPreview() {
    ImeNestedScrollExampleUI(onBackEvent = {})
}
