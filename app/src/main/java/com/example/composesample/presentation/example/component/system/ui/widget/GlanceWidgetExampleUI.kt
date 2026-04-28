package com.example.composesample.presentation.example.component.system.ui.widget

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * Glance Widget Example
 */
@Composable
fun GlanceWidgetExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Glance Widget Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GlanceOverviewCard() }
            item { WidgetHierarchyCard() }
            item { WidgetSizesCard() }
        }
    }
}

@Composable
private fun GlanceOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📱 Glance란?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Glance는 Jetpack Compose를 사용하여 Android 위젯을 더 쉽게 만들 수 있게 해주는 라이브러리입니다.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureChip("RemoteViews 대체", Color(0xFF4CAF50))
                FeatureChip("Compose UI", Color(0xFF2196F3))
                FeatureChip("간편한 구조", Color(0xFFFF9800))
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String, color: Color) {
    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WidgetHierarchyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🏗️ 위젯 구조",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            HierarchyItem("1️⃣ App Manifest", "위젯 선언 및 설정 (AndroidManifest.xml)")
            HierarchyItem("2️⃣ GlanceAppWidgetReceiver", "위젯 라이프사이클 관리 (Receiver)")
            HierarchyItem("3️⃣ GlanceAppWidget", "위젯의 UI 구성 (StreaksWidget)")
            HierarchyItem("4️⃣ Glance UI", "실제 Composable UI (StreakContent)")
        }
    }
}

@Composable
private fun HierarchyItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    Color(0xFF7B1FA2),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun WidgetSizesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📏 다양한 위젯 크기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SizeItem("2x1", "작은 위젯 (기본)", "아이콘 + 제목 + 새로고침 버튼")
            SizeItem("2x2", "중간 위젯", "세로 배치, 아이콘 + 상세 정보 + 버튼")
            SizeItem("3x2+", "큰 위젯", "좌우 분할, 풍부한 정보 표시")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "💡 기본 2x1 크기로 시작하여 원하는 크기로 조정 가능!",
                fontSize = 12.sp,
                color = Color(0xFF795548),
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "📱 위젯을 길게 눌러서 테두리 핸들로 크기 조정",
                fontSize = 12.sp,
                color = Color(0xFF795548),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SizeItem(size: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier,
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE65100).copy(alpha = 0.1f)
        ) {
            Text(
                text = size,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 10.sp,
                color = Color(0xFFE65100),
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GlanceWidgetExamplePreview() {
    GlanceWidgetExampleUI(
        onBackEvent = {}
    )
} 