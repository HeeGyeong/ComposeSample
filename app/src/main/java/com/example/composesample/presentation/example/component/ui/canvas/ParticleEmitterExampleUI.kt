package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// 단일 파티클 — 위치/속도/수명/크기/색상
private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,         // 0..1 (1=full life)
    val maxLife: Float,      // 초기 수명(초)
    val size: Float,
    val color: Color,
)

private enum class EmitMode { FIREWORK, STARDUST }

@Composable
fun ParticleEmitterExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Particle Emitter",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { CanvasParticleCard() }
            item { LayoutVsCanvasCard() }
            item { PhysicsExplanationCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "물리 기반 파티클 시스템 (순수 Compose)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "외부 라이브러리 없이 Canvas + withFrameNanos 기반의 파티클 시스템 구현. " +
                        "탭한 위치에서 두 가지 트리거 효과를 발사합니다: " +
                        "\n• 폭죽(Firework): 360° 방사형 폭발 + 중력 + 페이드아웃" +
                        "\n• 별가루(Stardust): 위쪽으로 흩날리는 부드러운 트레일",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun CanvasParticleCard() {
    val particles = remember { mutableStateListOf<Particle>() }
    var mode by remember { mutableStateOf(EmitMode.FIREWORK) }

    // 게임 루프 — withFrameNanos로 실제 경과 시간 사용
    LaunchedEffect(Unit) {
        var lastNs = 0L
        while (true) {
            withFrameNanos { ns ->
                val dt = if (lastNs == 0L) 0f else ((ns - lastNs) / 1_000_000_000f)
                lastNs = ns
                if (dt > 0f) updateParticles(particles, dt)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Canvas 기반 파티클 (탭으로 발사)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "단일 Canvas에서 모든 파티클을 그립니다. drawCircle 호출만으로 끝나 " +
                        "수백 개 파티클도 합성/측정 비용 없이 처리.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeButton("폭죽", mode == EmitMode.FIREWORK) { mode = EmitMode.FIREWORK }
                ModeButton("별가루", mode == EmitMode.STARDUST) { mode = EmitMode.STARDUST }
                Button(
                    onClick = { particles.clear() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) { Text(text = "Clear", color = Color.White, fontSize = 12.sp) }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF101820), RoundedCornerShape(12.dp))
                    .pointerInput(mode) {
                        // tap 위치에서 모드별 파티클 emit
                        awaitEachTap { offset ->
                            when (mode) {
                                EmitMode.FIREWORK -> emitFirework(particles, offset)
                                EmitMode.STARDUST -> emitStardust(particles, offset)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawParticles(particles)
                }

                Text(
                    text = "TAP 또는 드래그",
                    color = Color(0xFF7A8590),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
                Text(
                    text = "particles=${particles.size}",
                    color = Color(0xFF7A8590),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF1976D2) else Color(0xFFBBDEFB)
        )
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF1976D2),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LayoutVsCanvasCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. Canvas vs Layout 기반 렌더링",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Canvas (선택): 모든 파티클을 단일 DrawScope에서 drawCircle 반복 호출. " +
                        "측정/배치/Compose 트리 비용 0. 수백~수천 파티클까지 부드럽게 동작.\n\n" +
                        "• Layout (Box + offset Modifier): 각 파티클이 별도 Composable. " +
                        "디버그/접근성/개별 입력 처리에 유리하지만 100개만 넘어도 리컴포지션 비용이 급증. " +
                        "단순 시각 효과에는 부적합.\n\n" +
                        "본 데모는 Canvas 방식만 라이브로 제공하고, Layout 방식은 개념 설명에 그칩니다 — " +
                        "프로덕션에서는 수치적으로 Canvas 방식이 거의 항상 정답입니다.",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PhysicsExplanationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. 물리 모델",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• 시간 기반 적분: withFrameNanos로 dt(초)를 측정하여 v += g·dt, p += v·dt 적용. " +
                        "프레임 레이트 변동에도 동일한 체감 속도.\n" +
                        "• 중력: 하강 가속도 GRAVITY (px/s²) — 폭죽은 강하게, 별가루는 약하게.\n" +
                        "• 공기 저항(drag): v *= (1 - DRAG·dt) — 별가루의 부드러운 감쇠 표현.\n" +
                        "• 수명: life -= dt/maxLife. life ≤ 0이면 리스트에서 제거.\n" +
                        "• 알파 페이드: drawCircle alpha = life — 끝부분으로 갈수록 자연스럽게 사라짐.",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
        }
    }
}

// ============== 물리 / 렌더링 헬퍼 ==============

private const val GRAVITY = 900f       // px/s² (폭죽용)
private const val GRAVITY_LIGHT = 250f // px/s² (별가루용)
private const val DRAG = 1.4f          // 별가루 감속 계수

private fun updateParticles(particles: SnapshotStateList<Particle>, dt: Float) {
    val iter = particles.listIterator()
    while (iter.hasNext()) {
        val p = iter.next()
        // 색상 채도가 낮으면(별가루 색상군) 가벼운 중력 + drag 적용
        val isLight = p.color.alpha < 1f || p.maxLife > 1.4f
        val g = if (isLight) GRAVITY_LIGHT else GRAVITY
        val drag = if (isLight) DRAG else 0f

        p.vy += g * dt
        if (drag > 0f) {
            val factor = (1f - drag * dt).coerceAtLeast(0f)
            p.vx *= factor
            p.vy *= factor
        }
        p.x += p.vx * dt
        p.y += p.vy * dt
        p.life -= dt / p.maxLife
        if (p.life <= 0f) iter.remove()
    }
}

private fun DrawScope.drawParticles(particles: List<Particle>) {
    for (p in particles) {
        drawCircle(
            color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
            radius = p.size,
            center = Offset(p.x, p.y)
        )
    }
}

// 360° 방사형 폭발 — 60개 파티클
private fun emitFirework(particles: SnapshotStateList<Particle>, origin: Offset) {
    val palette = listOf(
        Color(0xFFFF5252),
        Color(0xFFFFD740),
        Color(0xFF69F0AE),
        Color(0xFF40C4FF),
        Color(0xFFFF80AB),
    )
    repeat(60) {
        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val speed = 220f + Random.nextFloat() * 380f
        particles.add(
            Particle(
                x = origin.x,
                y = origin.y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                life = 1f,
                maxLife = 0.8f + Random.nextFloat() * 0.5f,
                size = 3f + Random.nextFloat() * 3f,
                color = palette[Random.nextInt(palette.size)]
            )
        )
    }
}

// 위로 흩어지는 별가루 — 30개 파티클, 부드럽게 사라짐
private fun emitStardust(particles: SnapshotStateList<Particle>, origin: Offset) {
    val palette = listOf(
        Color(0xCCFFFFFF),
        Color(0xCCFFE082),
        Color(0xCCB39DDB),
        Color(0xCC81D4FA),
    )
    repeat(30) {
        val angle = -Math.PI.toFloat() / 2f + (Random.nextFloat() - 0.5f) * 1.4f
        val speed = 80f + Random.nextFloat() * 220f
        particles.add(
            Particle(
                x = origin.x + (Random.nextFloat() - 0.5f) * 20f,
                y = origin.y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                life = 1f,
                maxLife = 1.6f + Random.nextFloat() * 0.8f,
                size = 2f + Random.nextFloat() * 2.5f,
                color = palette[Random.nextInt(palette.size)]
            )
        )
    }
}

// 탭 위치 콜백 — pointerInput에서 사용
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.awaitEachTap(
    onTap: (Offset) -> Unit
) {
    detectTapGestures(onPress = { offset -> onTap(offset) })
}
