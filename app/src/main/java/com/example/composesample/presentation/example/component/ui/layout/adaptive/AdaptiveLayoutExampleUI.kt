package com.example.composesample.presentation.example.component.ui.layout.adaptive

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity

/**
 * AdaptiveLayout Example 참고 자료
 * - 공식 문서: https://developer.android.com/develop/ui/compose/layouts/adaptive
 * - WindowSizeClass API: https://developer.android.com/reference/kotlin/androidx/compose/material3/windowsizeclass/WindowSizeClass
 */

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AdaptiveLayoutExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    // Activity에서 WindowSizeClass 계산
    val windowSizeClass = calculateWindowSizeClass(context as ComponentActivity)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        item {
            // 상단 뒤로가기 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackEvent.invoke() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
                Text(
                    text = "Adaptive Layout (WindowSizeClass)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            // 현재 WindowSizeClass 정보 표시
            WindowSizeInfoCard(windowSizeClass = windowSizeClass)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // 화면 크기에 따라 레이아웃 분기
            when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Compact -> {
                    // 폰 세로 모드: 단일 열 레이아웃
                    CompactLayout()
                }
                WindowWidthSizeClass.Medium -> {
                    // 태블릿 세로 / 폴더블 펼침: 2열 레이아웃
                    MediumLayout()
                }
                WindowWidthSizeClass.Expanded -> {
                    // 태블릿 가로 / 데스크탑: 사이드바 + 콘텐츠 레이아웃
                    ExpandedLayout()
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // WindowHeightSizeClass 예시
            HeightSizeClassSection(windowSizeClass = windowSizeClass)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // WindowSizeClass 설명 카드
            ExplanationCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** 현재 WindowSizeClass 정보를 카드로 표시 */
@Composable
private fun WindowSizeInfoCard(windowSizeClass: WindowSizeClass) {
    val widthLabel = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> "Compact (< 600dp) — 폰 세로"
        WindowWidthSizeClass.Medium -> "Medium (600–840dp) — 태블릿 세로 / 폴더블"
        WindowWidthSizeClass.Expanded -> "Expanded (> 840dp) — 태블릿 가로"
        else -> "Unknown"
    }
    val heightLabel = when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact -> "Compact (< 480dp) — 폰 가로"
        WindowHeightSizeClass.Medium -> "Medium (480–900dp) — 일반"
        WindowHeightSizeClass.Expanded -> "Expanded (> 900dp) — 태블릿"
        else -> "Unknown"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "현재 WindowSizeClass",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Width : $widthLabel", color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Height: $heightLabel", color = Color.White, fontSize = 13.sp)
        }
    }
}

/** Compact 레이아웃 — 단일 열 */
@Composable
private fun CompactLayout() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Compact 레이아웃 (단일 열)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        ContentCard(title = "항목 1", color = Color(0xFFE3F2FD))
        ContentCard(title = "항목 2", color = Color(0xFFE8F5E9))
        ContentCard(title = "항목 3", color = Color(0xFFFFF9C4))
    }
}

/** Medium 레이아웃 — 2열 */
@Composable
private fun MediumLayout() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Medium 레이아웃 (2열)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ContentCard(title = "항목 1", color = Color(0xFFE3F2FD), modifier = Modifier.weight(1f))
            ContentCard(title = "항목 2", color = Color(0xFFE8F5E9), modifier = Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ContentCard(title = "항목 3", color = Color(0xFFFFF9C4), modifier = Modifier.weight(1f))
            ContentCard(title = "항목 4", color = Color(0xFFFCE4EC), modifier = Modifier.weight(1f))
        }
    }
}

/** Expanded 레이아웃 — 사이드바 + 콘텐츠 */
@Composable
private fun ExpandedLayout() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Expanded 레이아웃 (사이드바 + 콘텐츠)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 사이드바 (30%)
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxSize()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "사이드바\n내비게이션",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            // 메인 콘텐츠 (70%)
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxSize()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "메인 콘텐츠 영역\n(70% 너비)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/** WindowHeightSizeClass 섹션 */
@Composable
private fun HeightSizeClassSection(windowSizeClass: WindowSizeClass) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WindowHeightSizeClass 활용",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 높이가 Compact(폰 가로)이면 BottomBar 대신 NavRail 권장
            val heightNote = when (windowSizeClass.heightSizeClass) {
                WindowHeightSizeClass.Compact ->
                    "현재 Compact — 폰 가로 모드. BottomBar 대신 NavigationRail 권장."
                WindowHeightSizeClass.Medium ->
                    "현재 Medium — 일반 폰·태블릿. BottomBar 또는 NavigationRail 선택 가능."
                WindowHeightSizeClass.Expanded ->
                    "현재 Expanded — 대형 태블릿. NavigationRail 또는 NavigationDrawer 권장."
                else -> "Unknown"
            }
            Text(text = heightNote, fontSize = 13.sp, color = Color(0xFF4A148C))
        }
    }
}

/** 설명 카드 */
@Composable
private fun ExplanationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WindowSizeClass 핵심 개념",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "Compact: < 600dp — 일반 스마트폰",
                "Medium: 600–840dp — 태블릿 세로, 폴더블 펼침",
                "Expanded: > 840dp — 태블릿 가로, 대형 화면",
                "calculateWindowSizeClass()로 Activity에서 계산",
                "FlexBox와 달리 기기 클래스 기반 레이아웃 분기",
                "폰·태블릿·폴더블 모두 대응하는 Adaptive UI 구현"
            ).forEach { line ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "• $line", fontSize = 13.sp)
            }
        }
    }
}

/** 재사용 콘텐츠 카드 */
@Composable
private fun ContentCard(
    title: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(color, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
