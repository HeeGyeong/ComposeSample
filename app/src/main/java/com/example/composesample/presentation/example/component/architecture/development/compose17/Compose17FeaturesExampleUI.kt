package com.example.composesample.presentation.example.component.architecture.development.compose17

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Compose17FeatureItem(
    val title: String,
    val description: String,
    val keywords: List<String>
)

private val compose17Features = listOf(
    Compose17FeatureItem(
        title = "TextOverflow (Start / Middle Ellipsis)",
        description = "텍스트 생략 위치를 End(기본)뿐만 아니라 Start(앞 생략), Middle(중간 생략)로 지정할 수 있습니다.",
        keywords = listOf("TextOverflow.StartEllipsis", "TextOverflow.MiddleEllipsis")
    ),
    Compose17FeatureItem(
        title = "GraphicsLayer (BlendMode / ColorFilter)",
        description = "Modifier.graphicsLayer에서 BlendMode와 ColorFilter를 적용해 GPU 수준의 색상 블렌딩과 필터를 처리합니다.",
        keywords = listOf("BlendMode.Screen", "ColorFilter.tint", "compositeOver")
    ),
    Compose17FeatureItem(
        title = "LookaheadScope (자동 레이아웃 애니메이션)",
        description = "LookaheadScope 내부에서 Modifier.animateBounds()를 사용하면 레이아웃 이동/크기 변화가 자동으로 애니메이션 처리됩니다.",
        keywords = listOf("LookaheadScope", "animateBounds", "자동 애니메이션")
    ),
    Compose17FeatureItem(
        title = "FocusRestorer (포커스 복원)",
        description = "Modifier.focusRestorer()로 LazyRow 등에서 포커스가 사라졌다 돌아올 때 마지막 포커스 위치를 자동 복원합니다.",
        keywords = listOf("focusRestorer", "FocusRequester", "TV/폴더블 접근성")
    ),
    Compose17FeatureItem(
        title = "PathGraphics (Path.reverse / contains)",
        description = "Path.reverse()로 채우기 방향을 반전(도넛 구멍 효과)하고, Path.contains(offset)로 히트 테스트를 수행합니다.",
        keywords = listOf("Path.reverse", "Path.contains", "Canvas 도형")
    )
)

@Composable
fun Compose17FeaturesExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EE))
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
                text = "Compose 1.7.6 새로운 기능들",
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
                // 소개 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Compose 1.7.6",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "5가지 새로운 UI 기능을 소개합니다.\n각 항목을 메인 리스트에서 개별 예제로 확인하세요.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            items(compose17Features.mapIndexed { i, it -> i to it }) { (index, feature) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 번호 배지
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
                            ) {
                                Text(
                                    text = " ${index + 1} ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = feature.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = feature.description,
                            fontSize = 13.sp,
                            color = Color(0xFF555555),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 키워드 태그
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            feature.keywords.forEach { keyword ->
                                Card(
                                    shape = RoundedCornerShape(4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                                ) {
                                    Text(
                                        text = keyword,
                                        fontSize = 11.sp,
                                        color = Color(0xFF6200EE),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
