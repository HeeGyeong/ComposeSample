package com.example.composesample.presentation.example.component.ui.media.shimmer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// SkeletonContainer가 한 번만 소유하는 로딩 상태 + 공유 shimmer 진행도 — 하위 Modifier.autoSkeleton()이 파라미터 전달 없이 이걸 자동으로 읽는다
private class SkeletonScope(val loading: Boolean, val phase: State<Float>)

private val LocalSkeletonScope = compositionLocalOf<SkeletonScope?> { null }

@Composable
fun AutoSkeletonModifierExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "자동 스켈레톤 로딩 감지 Modifier",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { ManualVsAutoDemoCard() }
            item { TechniqueCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "자동 스켈레톤 감지란",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "이 프로젝트의 기존 Shimmer 예제(ShimmerExampleUI/TextShimmerExampleUI)는 요소마다 " +
                        "if(isLoading) 분기로 실제 콘텐츠와 크기를 맞춘 스켈레톤 트리를 별도로 그려야 합니다. " +
                        "이 예제는 로딩 상태를 CompositionLocal로 한 번만 전파하고, 각 요소는 Modifier 하나만 붙이면 " +
                        "자신의 측정된 크기를 그대로 재사용해 스켈레톤을 그리는 방식을 다룹니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val steps = listOf(
                Triple("① 상태 소유", "SkeletonContainer", "로딩 상태 + 공유 shimmer 애니메이션을 한 곳에서 소유"),
                Triple("② 자동 전파", "CompositionLocalProvider", "하위 요소는 파라미터 없이 CompositionLocal에서 loading을 읽음"),
                Triple("③ 측정 재사용", "drawWithContent", "이미 측정된 콘텐츠 크기 그대로 shimmer 사각형을 그림"),
                Triple("④ 독립 크로스페이드", "graphicsLayer(alpha)", "drawWithContent가 바깥쪽이라 콘텐츠·shimmer 알파가 서로 독립적으로 페이드")
            )
            steps.forEach { (label, api, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.24f))
                    Text(text = api, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2), modifier = Modifier.weight(0.34f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.42f))
                }
            }
        }
    }
}

@Composable
private fun ManualVsAutoDemoCard() {
    var loading by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "데모 — 수동 분기 vs 자동 감지",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "같은 로딩 상태 하나로 두 방식을 동시에 토글합니다. 아래 두 번째 카드는 실제 텍스트 길이에 맞춰 " +
                        "스켈레톤 너비가 자동으로 달라지는 것에 주목하세요 — 수동 방식처럼 fillMaxWidth(0.6f) 같은 비율을 " +
                        "눈대중으로 지정할 필요가 없습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "로딩 상태", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Switch(checked = loading, onCheckedChange = { loading = it })
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "수동 방식 — if(isLoading) 분기로 별도 스켈레톤 트리를 직접 구성",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575)
            )
            ManualSkeletonProfileRow(loading = loading)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "자동 방식 — 실제 콘텐츠에 Modifier.autoSkeleton()만 붙임(isLoading 파라미터 전달 없음)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575)
            )
            SkeletonContainer(loading = loading) {
                AutoSkeletonProfileRow()
            }
        }
    }
}

@Composable
private fun ManualSkeletonProfileRow(loading: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "K", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
            } else {
                Text(text = "김코틀린", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Compose 개발자", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun AutoSkeletonProfileRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .autoSkeleton(shape = CircleShape)
                .background(Color(0xFF1976D2)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "K", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "김코틀린",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.autoSkeleton()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Compose 개발자",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.autoSkeleton()
            )
        }
    }
}

// 로딩 상태 + 단일 rememberInfiniteTransition을 CompositionLocal로 전파 — 하위 autoSkeleton()들이 애니메이션을 각자 만들지 않고 공유
@Composable
private fun SkeletonContainer(
    loading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "autoSkeletonShimmer")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoSkeletonPhase"
    )

    CompositionLocalProvider(LocalSkeletonScope provides SkeletonScope(loading, phase)) {
        Box(modifier) { content() }
    }
}

// CompositionLocal에서 로딩 상태를 자동으로 읽어, 이 요소가 이미 측정된 크기 그대로 shimmer를 그린다 — 스코프가 없으면 아무 것도 하지 않고 원본 그대로 반환
private fun Modifier.autoSkeleton(shape: Shape = RoundedCornerShape(6.dp)): Modifier = composed {
    val scope = LocalSkeletonScope.current ?: return@composed this
    val contentAlpha = remember { Animatable(if (scope.loading) 0f else 1f) }

    LaunchedEffect(scope.loading) {
        contentAlpha.animateTo(
            targetValue = if (scope.loading) 0f else 1f,
            animationSpec = tween(durationMillis = 350)
        )
    }

    this
        .clip(shape)
        .drawWithContent {
            // 콘텐츠(그래픽스레이어로 감싸여 있어 알파가 독립적으로 적용됨)를 먼저 그리고,
            drawContent()
            // 그 위에 shimmer 오버레이를 별도로 그린다 — 아래 graphicsLayer의 alpha 영향을 받지 않는다
            val shimmerAlpha = 1f - contentAlpha.value
            if (shimmerAlpha > 0f) {
                val travel = size.width + size.height
                val offset = scope.phase.value * travel
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.LightGray.copy(alpha = 0.25f),
                            Color.LightGray.copy(alpha = 0.85f),
                            Color.LightGray.copy(alpha = 0.25f)
                        ),
                        start = Offset(offset - size.width, 0f),
                        end = Offset(offset, size.height)
                    ),
                    alpha = shimmerAlpha
                )
            }
        }
        .graphicsLayer { alpha = contentAlpha.value }
}

@Composable
private fun TechniqueCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 — drawWithContent가 바깥쪽, graphicsLayer가 안쪽",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Modifier 체인은 왼쪽이 바깥쪽입니다. drawWithContent 안에서 drawContent()를 호출하면 그 안쪽의 " +
                        "graphicsLayer(alpha)가 적용된 실제 콘텐츠가 그려지고, 그 다음 줄에서 그리는 shimmer 사각형은 " +
                        "그 alpha 밖에서 그려지므로 두 알파가 서로 독립적으로 페이드됩니다. 순서를 바꾸면 shimmer까지 " +
                        "콘텐츠와 함께 페이드되어 크로스페이드가 깨집니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                code = "this\n" +
                        "    .clip(shape)\n" +
                        "    .drawWithContent {\n" +
                        "        drawContent()               // graphicsLayer가 적용된 실제 콘텐츠\n" +
                        "        drawRect(shimmerBrush, alpha = 1f - contentAlpha.value)\n" +
                        "    }\n" +
                        "    .graphicsLayer { alpha = contentAlpha.value }"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "기존 ShimmerItem()/ShimmerTestItem()과의 차이: 기존 예제는 실제 콘텐츠와 별개로 크기를 맞춘 " +
                        "Spacer 스켈레톤 트리를 직접 그리고 shimmerAnimation 값을 각 요소에 파라미터로 전달합니다. " +
                        "이 예제는 실제 콘텐츠(Text/Box)를 그대로 두고 Modifier 하나만 얹어, Compose가 이미 계산해 둔 " +
                        "그 요소의 측정 크기를 shimmer 사각형 크기로 재사용합니다 — 텍스트 길이가 바뀌어도 스켈레톤 폭을 " +
                        "따로 맞출 필요가 없습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "CompositionLocal로 로딩 상태 + 공유 shimmer 애니메이션을 한 곳(SkeletonContainer)에서만 소유하고 전파한다",
                "Modifier.autoSkeleton()은 CompositionLocal에서 loading을 자동으로 읽어, 요소마다 isLoading 파라미터를 넘길 필요가 없다",
                "shimmer는 별도 스켈레톤 트리를 만들지 않고, Compose가 이미 측정해 둔 이 요소 자신의 크기를 그대로 재사용해 그린다",
                "drawWithContent(바깥) 안에서 drawContent() 이후에 shimmer를 그리고, graphicsLayer(alpha)는 그 안쪽에 둬야 콘텐츠·shimmer 알파가 독립적으로 크로스페이드된다",
                "스코프(SkeletonContainer) 밖에서 쓰면 autoSkeleton()은 아무 것도 하지 않고 원본 Modifier를 그대로 반환해 비용이 0이다"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}
