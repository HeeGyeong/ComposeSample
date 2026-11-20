package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * #117 실물 카드들이 같이 쓰는 모양 — 같은 화면의 개념 시뮬레이션 카드와 구분되게 초록 테두리 + "✅ 실물" 머리말.
 * 실물 카드는 androidx.navigation3 1.2.2 · lifecycle-viewmodel-navigation3 2.11.1 을 실제로 부른다.
 */
internal val RealGreen = Color(0xFF2E7D32)

@Composable
internal fun RealNavCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, RealGreen, RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "✅ 실물 · $title", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = RealGreen)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF616161), lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/** NavDisplay 를 담는 고정 높이 틀 — LazyColumn 아이템 안이라 높이를 정해 줘야 한다 */
@Composable
internal fun RealNavFrame(height: Dp = 240.dp, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFB0BEC5), RoundedCornerShape(8.dp)),
        content = content
    )
}

/** NavDisplay 안 화면 하나 */
@Composable
internal fun RealPane(title: String, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        content()
    }
}

@Composable
internal fun RealButton(text: String, color: Color = RealGreen, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
internal fun RealButtonRow(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { content() }
}

/** 이름 · 값 한 줄 — 값은 고정폭 글꼴 */
@Composable
internal fun RealInfoLine(label: String, value: String, valueColor: Color = Color(0xFF212121)) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF616161), modifier = Modifier.width(88.dp))
        Text(text = value, fontSize = 12.sp, color = valueColor, fontFamily = FontFamily.Monospace)
    }
}

@Composable
internal fun RealCaption(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 15.sp)
}
