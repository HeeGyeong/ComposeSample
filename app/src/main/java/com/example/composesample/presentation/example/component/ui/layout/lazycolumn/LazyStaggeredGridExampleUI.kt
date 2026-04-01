package com.example.composesample.presentation.example.component.ui.layout.lazycolumn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 그리드 아이템 데이터 모델
private data class StaggeredItem(
    val id: Int,
    val title: String,
    val height: Dp,          // 각 아이템의 고유 높이 (폭포수 효과 핵심)
    val color: Color,
    val tag: String,         // 필터링용 태그
    val isFullSpan: Boolean = false  // 헤더/배너용 전체 너비 아이템
)

// 샘플 데이터 — 다양한 높이로 폭포수 레이아웃 연출
private val sampleItems = listOf(
    StaggeredItem(1, "Compose 기초", 120.dp, Color(0xFF6200EE), "UI"),
    StaggeredItem(2, "LazyColumn 고급 패턴과 활용 방법", 180.dp, Color(0xFF03DAC6), "Layout"),
    StaggeredItem(3, "State 관리", 100.dp, Color(0xFFFF6D00), "State"),
    StaggeredItem(4, "Navigation 3.0", 160.dp, Color(0xFF1565C0), "Navigation"),
    StaggeredItem(5, "ViewModel + Flow 실전 패턴", 200.dp, Color(0xFF2E7D32), "Architecture"),
    StaggeredItem(6, "Canvas API", 140.dp, Color(0xFFC62828), "UI"),
    StaggeredItem(7, "애니메이션", 110.dp, Color(0xFF6A1B9A), "Animation"),
    StaggeredItem(8, "Material 3 컴포넌트 전체 목록 및 사용법", 220.dp, Color(0xFF00695C), "UI"),
    StaggeredItem(9, "Koin DI", 130.dp, Color(0xFFAD1457), "Architecture"),
    StaggeredItem(10, "Paging 3", 150.dp, Color(0xFF0277BD), "Data"),
    StaggeredItem(11, "Room DB", 120.dp, Color(0xFF558B2F), "Data"),
    StaggeredItem(12, "Coroutine Flow", 170.dp, Color(0xFF4527A0), "State"),
    StaggeredItem(13, "SharedElement 전환", 190.dp, Color(0xFFBF360C), "Animation"),
    StaggeredItem(14, "Accessibility", 140.dp, Color(0xFF37474F), "UI"),
    StaggeredItem(15, "Testing", 160.dp, Color(0xFF1B5E20), "Architecture"),
)

private val filterTags = listOf("전체", "UI", "Layout", "State", "Navigation", "Architecture", "Animation", "Data")

@Composable
fun LazyStaggeredGridExampleUI(onBackEvent: () -> Unit) {
    var selectedTag by remember { mutableStateOf("전체") }

    // 선택된 태그에 따라 필터링된 아이템
    val filteredItems = remember(selectedTag) {
        if (selectedTag == "전체") sampleItems
        else sampleItems.filter { it.tag == selectedTag }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF1A1A2E))
    ) {
        // 상단 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
            Text(
                text = "LazyStaggeredGrid",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyVerticalStaggeredGrid(
            // 고정 열 수 방식 (적응형: StaggeredGridCells.Adaptive(minSize))
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            // ── 섹션 1: 설명 헤더 (전체 너비 스팬) ──────────────────────────
            item(span = StaggeredGridItemSpan.FullLine) {
                SectionCard(
                    title = "1. LazyVerticalStaggeredGrid 기본",
                    description = "각 아이템이 서로 다른 높이를 가져 자연스러운 폭포수(Waterfall) 레이아웃을 형성합니다.\n" +
                            "StaggeredGridCells.Fixed(2): 열 수 고정\n" +
                            "StaggeredGridCells.Adaptive(140.dp): 최소 너비 기준 자동 열 수"
                )
            }

            // ── 섹션 2: 필터 칩 (전체 너비 스팬) ────────────────────────────
            item(span = StaggeredGridItemSpan.FullLine) {
                FilterChipRow(
                    tags = filterTags,
                    selectedTag = selectedTag,
                    onTagSelected = { selectedTag = it }
                )
            }

            // ── 섹션 3: 필터링된 아이템 목록 ─────────────────────────────────
            items(
                items = filteredItems,
                key = { it.id }  // 재정렬 애니메이션을 위한 key 지정
            ) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.85f),
                    exit = fadeOut(tween(200)) + scaleOut(tween(200))
                ) {
                    StaggeredGridCard(item = item)
                }
            }

            // ── 섹션 4: 코드 설명 카드 (전체 너비 스팬) ──────────────────────
            item(span = StaggeredGridItemSpan.FullLine) {
                SectionCard(
                    title = "2. StaggeredGridItemSpan",
                    description = "item(span = StaggeredGridItemSpan.FullLine): 헤더/배너처럼 전체 너비 점유\n" +
                            "items(key = { it.id }): 필터링/정렬 시 부드러운 재배치 애니메이션"
                )
            }

            // ── 섹션 5: Adaptive 방식 설명 카드 (전체 너비 스팬) ─────────────
            item(span = StaggeredGridItemSpan.FullLine) {
                SectionCard(
                    title = "3. 폼팩터 대응 팁",
                    description = "StaggeredGridCells.Adaptive(minSize = 140.dp)를 사용하면\n" +
                            "폰에서는 2열, 태블릿에서는 3~4열로 자동 조정됩니다.\n" +
                            "WindowSizeClass와 조합하면 반응형 레이아웃 완성."
                )
            }
        }
    }
}

// 설명 섹션 카드 (전체 너비)
@Composable
private fun SectionCard(
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF16213E)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = Color(0xFF7B8CDE),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// 필터 칩 가로 스크롤 행
@Composable
private fun FilterChipRow(
    tags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tags.forEach { tag ->
            FilterChip(
                selected = tag == selectedTag,
                onClick = { onTagSelected(tag) },
                label = {
                    Text(
                        text = tag,
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6200EE),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF16213E),
                    labelColor = Color(0xFFB0BEC5)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = tag == selectedTag,
                    borderColor = Color(0xFF3A3A5C),
                    selectedBorderColor = Color(0xFF6200EE)
                )
            )
        }
    }
}

// 개별 폭포수 아이템 카드
@Composable
private fun StaggeredGridCard(item: StaggeredItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(item.height),  // 각 아이템마다 다른 높이 — 폭포수 핵심
        shape = RoundedCornerShape(12.dp),
        color = item.color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 색상 인디케이터
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.7f))
            )

            Column {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                // 태그 배지
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = item.color.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = item.tag,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
