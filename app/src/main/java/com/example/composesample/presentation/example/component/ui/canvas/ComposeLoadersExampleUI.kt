package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ComposeLoadersExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        MainHeader(
            title = "Compose Loaders",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LoaderSection(
                    title = "Rose Curve (r = cos(kθ), k=3)",
                    formula = "r = cos(3θ),  θ ∈ [0, π]",
                    description = "극좌표 방정식으로 만드는 장미 곡선. k가 홀수이면 k개 꽃잎, k가 짝수이면 2k개 꽃잎이 생깁니다."
                ) {
                    RoseCurveLoader(color = Color(0xFFFF6B9D))
                }
            }
            item {
                LoaderSection(
                    title = "Lissajous (a=3, b=2)",
                    formula = "x = sin(3t + δ),  y = sin(2t)",
                    description = "두 수직 조화 진동의 합성 곡선. 위상(δ) 변화에 따라 매듭 모양이 연속적으로 변형됩니다."
                ) {
                    LissajousLoader(color = Color(0xFF00D4FF))
                }
            }
            item {
                LoaderSection(
                    title = "Lemniscate of Bernoulli (∞)",
                    formula = "x = cos(t)/(1+sin²t),  y = sin(t)cos(t)/(1+sin²t)",
                    description = "두 초점으로부터의 거리 곱이 일정한 점의 궤적. 무한대 기호(∞) 모양의 곡선입니다."
                ) {
                    LemniscateLoader(color = Color(0xFF00FF88))
                }
            }
            item {
                LoaderSection(
                    title = "Spirograph (Hypotrochoid, R=5, r=3, d=4)",
                    formula = "x = (R-r)cos(t) + d·cos((R-r)/r·t)",
                    description = "큰 원 내부에서 굴러가는 작은 원 위의 점이 그리는 곡선. R=5, r=3일 때 6π 주기로 패턴이 완성됩니다."
                ) {
                    SpirographLoader(color = Color(0xFFFFAA00))
                }
            }
            item {
                LoaderSection(
                    title = "Cardioid",
                    formula = "r = a(1 - cos θ),  θ ∈ [0, 2π]",
                    description = "반지름이 같은 두 원이 서로 접하며 굴러갈 때 한 점이 그리는 하트 모양 곡선입니다."
                ) {
                    CardioidLoader(color = Color(0xFFFF4444))
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ── 공통 섹션 카드 ──
@Composable
private fun LoaderSection(
    title: String,
    formula: String,
    description: String,
    loader: @Composable () -> Unit
) {
    Card(
        backgroundColor = Color(0xFF161B22),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color(0xFFE8E8E8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formula,
                color = Color(0xFF888888),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                loader()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                color = Color(0xFF666666),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ── 공통 트레일 애니메이션 헬퍼 ──
// 사전 계산된 곡선 점 목록을 받아 헤드 인덱스 기준으로 꼬리를 그린다.
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrail(
    points: List<Offset>,
    headIdx: Int,
    trailLen: Int,
    color: Color,
    scale: Float,
    maxDotRadius: Float = 5f
) {
    val cx = size.width / 2
    val cy = size.height / 2
    for (i in 0 until trailLen) {
        val idx = ((headIdx - i + points.size) % points.size)
        val pt = points[idx]
        val alpha = (1f - i.toFloat() / trailLen).coerceIn(0f, 1f)
        val radius = if (i == 0) maxDotRadius else maxDotRadius * 0.5f * alpha + 0.5f
        drawCircle(
            color = color.copy(alpha = alpha * 0.9f),
            radius = radius,
            center = Offset(cx + pt.x * scale, cy + pt.y * scale)
        )
    }
}

// ── Rose Curve: r = cos(kθ), k=3 ──
@Composable
private fun RoseCurveLoader(color: Color) {
    val k = 3f
    val steps = 600
    // r = cos(kθ)를 직교 좌표로 변환 (θ: 0 ~ π, 3개 꽃잎)
    val points = remember {
        List(steps) { i ->
            val theta = (i.toFloat() / steps) * PI.toFloat()
            val r = cos(k * theta)
            Offset(r * cos(theta), r * sin(theta))
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "rose")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rose_p"
    )
    Canvas(modifier = Modifier.size(160.dp)) {
        val headIdx = (progress * points.size).toInt().coerceIn(0, points.size - 1)
        drawTrail(points, headIdx, points.size / 3, color, size.width * 0.44f)
    }
}

// ── Lissajous: x = sin(at + δ), y = sin(bt) ──
@Composable
private fun LissajousLoader(color: Color) {
    val a = 3f
    val b = 2f
    val steps = 500
    val infiniteTransition = rememberInfiniteTransition(label = "lissajous")
    // 위상(δ)이 0 → 2π 로 변하며 도형 형태가 연속적으로 변형됨
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lissajous_phase"
    )
    Canvas(modifier = Modifier.size(160.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val scale = size.width * 0.42f
        val trailLen = steps / 3
        // 현재 위상에서 곡선의 꼬리 부분만 그린다
        for (i in 0 until trailLen) {
            val idx = steps - 1 - i
            if (idx < 0) break
            val t = (idx.toFloat() / steps) * (2 * PI).toFloat()
            val x = sin(a * t + phase)
            val y = sin(b * t)
            val alpha = (1f - i.toFloat() / trailLen).coerceIn(0f, 1f)
            val radius = if (i == 0) 5f else 2.5f * alpha + 0.5f
            drawCircle(
                color = color.copy(alpha = alpha * 0.9f),
                radius = radius,
                center = Offset(cx + x * scale, cy + y * scale)
            )
        }
    }
}

// ── Lemniscate of Bernoulli: (x²+y²)² = a²(x²-y²) ──
@Composable
private fun LemniscateLoader(color: Color) {
    val steps = 600
    // 매개변수 방정식: x = cos(t)/(1+sin²t), y = sin(t)cos(t)/(1+sin²t)
    val points = remember {
        List(steps) { i ->
            val t = (i.toFloat() / steps) * (2 * PI).toFloat()
            val denom = 1f + sin(t) * sin(t)
            Offset(cos(t) / denom, sin(t) * cos(t) / denom)
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "lemniscate")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lemniscate_p"
    )
    Canvas(modifier = Modifier.size(160.dp)) {
        val headIdx = (progress * points.size).toInt().coerceIn(0, points.size - 1)
        drawTrail(points, headIdx, points.size / 3, color, size.width * 0.85f)
    }
}

// ── Spirograph (Hypotrochoid): R=5, r=3, d=4 ──
@Composable
private fun SpirographLoader(color: Color) {
    val R = 5f
    val r = 3f
    val d = 4f
    val steps = 900
    // t: 0 ~ 6π (LCM(R,r)/r = 5개 사이클에서 패턴 완성, 실용적으로 6π 사용)
    val points = remember {
        List(steps) { i ->
            val t = (i.toFloat() / steps) * 6f * PI.toFloat()
            val x = (R - r) * cos(t) + d * cos((R - r) / r * t)
            val y = (R - r) * sin(t) - d * sin((R - r) / r * t)
            Offset(x, y)
        }
    }
    // 스케일 정규화 (최대 반지름 ≈ R-r+d = 6)
    val maxRadius = R - r + d
    val infiniteTransition = rememberInfiniteTransition(label = "spirograph")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spirograph_p"
    )
    Canvas(modifier = Modifier.size(160.dp)) {
        val headIdx = (progress * points.size).toInt().coerceIn(0, points.size - 1)
        drawTrail(points, headIdx, points.size / 4, color, size.width * 0.42f / maxRadius)
    }
}

// ── Cardioid: r = a(1 - cosθ) ──
@Composable
private fun CardioidLoader(color: Color) {
    val steps = 600
    // r = 1 - cosθ 를 직교 좌표로 변환 후 중심 보정 (-0.5, 0)
    val points = remember {
        List(steps) { i ->
            val theta = (i.toFloat() / steps) * (2 * PI).toFloat()
            val r = 1f - cos(theta)
            Offset(r * cos(theta) - 0.5f, r * sin(theta))
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "cardioid")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cardioid_p"
    )
    Canvas(modifier = Modifier.size(160.dp)) {
        val headIdx = (progress * points.size).toInt().coerceIn(0, points.size - 1)
        drawTrail(points, headIdx, points.size / 3, color, size.width * 0.26f)
    }
}
