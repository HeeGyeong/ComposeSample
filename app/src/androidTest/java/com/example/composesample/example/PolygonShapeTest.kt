package com.example.composesample.example

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerRounding
import androidx.compose.foundation.shape.PolygonShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.example.component.ui.shapes.MorphAvatar
import com.example.composesample.presentation.example.component.ui.shapes.MorphLabState
import com.example.composesample.presentation.example.component.ui.shapes.MorphVariant
import com.example.composesample.presentation.example.component.ui.shapes.RuntimeShaderBorderTile
import com.example.composesample.presentation.example.component.ui.shapes.SharedTiles
import com.example.composesample.presentation.example.component.ui.shapes.StateTile
import com.example.composesample.presentation.example.component.ui.shapes.TransformLabState
import com.example.composesample.presentation.example.component.ui.shapes.TransformReport
import com.example.composesample.presentation.example.component.ui.shapes.ZeroSafeShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * PolygonShape 예제(#148) — 화면의 컴포저블을 그대로 띄워 픽셀과 횟수로 본다.
 * given : 람다 / 값으로 진행률을 주는 아바타, 공유 · copy 별 칸, 크기 0 에서 자라는 칸, 셰이더 테두리
 * when : 진행률 · 각도를 바꾸고, 공유 인스턴스를 클릭처럼 제자리로 바꾸고, 칸을 다시 붙인다
 * then : 람다는 리컴포지션 0 으로 그림이 바뀌고, 제자리 transform 은 다시 붙일 때까지 안 보이다가 공유한 칸만 바뀌고,
 *        감싼 도형은 크기 0 을 지나도 그리고, 감싸지 않으면 던진다
 */
class PolygonShapeTest {

    @get:Rule
    val rule = createComposeRule()

    private fun pixels(tag: String): PixelMap = rule.onNodeWithTag(tag).captureToImage().toPixelMap()

    /** 같은 크기 두 캡처에서 채널 차가 8/255 를 넘는 픽셀 수 */
    private fun differing(a: PixelMap, b: PixelMap): Int {
        var n = 0
        for (y in 0 until min(a.height, b.height)) for (x in 0 until min(a.width, b.width)) {
            val p = a[x, y]
            val q = b[x, y]
            val d = max(max(abs(p.red - q.red), abs(p.green - q.green)), abs(p.blue - q.blue))
            if (d > 8f / 255f) n++
        }
        return n
    }

    private fun countColor(map: PixelMap, match: (Color) -> Boolean): Int {
        var n = 0
        for (y in 0 until map.height) for (x in 0 until map.width) if (match(map[x, y])) n++
        return n
    }

    private fun morphSteps(lab: MorphLabState) {
        for (p in listOf(0.25f, 0.5f, 0.75f, 1f)) {
            rule.runOnIdle { runBlocking { lab.progress.snapTo(p) } }
            rule.waitForIdle()
        }
    }

    @Test
    fun 람다_진행률은_리컴포지션_없이_외곽선만_다시_만든다() {
        val lab = MorphLabState().apply { variant = MorphVariant.LambdaScale }
        rule.setContent { MorphAvatar(lab) }
        rule.waitForIdle()
        val recompositions = lab.recompositions.count
        val calls = lab.counter.calls
        val start = pixels("morphAvatar")

        morphSteps(lab)

        assertEquals("람다로 읽으면 진행률을 바꿔도 리컴포즈하지 않는다", recompositions, lab.recompositions.count)
        val rebuilt = lab.counter.calls - calls
        assertTrue("진행률 4번에 외곽선 $rebuilt 번", rebuilt in 4..8)
        val changed = differing(start, pixels("morphAvatar"))
        assertTrue("작은 쿠키 → 큰 원 픽셀 차 $changed", changed > 1000)
    }

    @Test
    fun 값으로_읽으면_진행률을_바꿀_때마다_리컴포즈한다() {
        val lab = MorphLabState().apply { variant = MorphVariant.ValueNew }
        rule.setContent { MorphAvatar(lab) }
        rule.waitForIdle()
        val recompositions = lab.recompositions.count

        morphSteps(lab)

        assertEquals(recompositions + 4, lab.recompositions.count)
    }

    @Test
    fun 상태를_읽는_transform_은_리컴포지션_없이_돈다() {
        val lab = TransformLabState()
        rule.setContent { StateTile(lab) }
        rule.waitForIdle()
        val recompositions = lab.stateRecompositions.count
        val calls = lab.stateCounter.calls
        val before = pixels("stateTile")

        rule.runOnIdle { runBlocking { lab.angle.snapTo(36f) } }
        rule.waitForIdle()

        assertEquals(recompositions, lab.stateRecompositions.count)
        assertEquals(calls + 1, lab.stateCounter.calls)
        val changed = differing(before, pixels("stateTile"))
        assertTrue("36° 돌린 별 픽셀 차 $changed", changed > 500)
    }

    @Test
    fun 클릭에서_건_transform_은_다시_붙일_때까지_안_보이고_copy_는_따라가지_않는다() {
        val lab = TransformLabState()
        lateinit var scope: CoroutineScope
        rule.setContent {
            scope = rememberCoroutineScope()
            SharedTiles(lab)
        }
        rule.waitForIdle()
        val a0 = pixels("tileA")
        val b0 = pixels("tileB")
        val c0 = pixels("tileC")

        rule.runOnIdle { scope.launch { lab.transformSharedInPlace() } }
        rule.waitForIdle()
        val inPlace = lab.lastReport as TransformReport.InPlace
        assertEquals("제자리 변경 뒤 외곽선을 다시 만든 칸이 없어야 한다", listOf(0, 0, 0), inPlace.rebuilt)
        assertEquals(0, differing(a0, pixels("tileA")))
        assertEquals(0, differing(b0, pixels("tileB")))

        rule.runOnIdle { scope.launch { lab.remount() } }
        rule.waitForIdle()
        val a2 = pixels("tileA")
        val b2 = pixels("tileB")
        assertTrue("다시 붙이면 공유한 A 가 돈다", differing(a0, a2) > 500)
        assertTrue("다시 붙이면 공유한 B 도 돈다", differing(b0, b2) > 500)
        assertEquals("A · B 는 같은 인스턴스라 같은 그림", 0, differing(a2, b2))
        assertEquals("먼저 copy() 한 C 는 따라가지 않는다", 0, differing(c0, pixels("tileC")))
    }

    @Test
    fun ZeroSafeShape_로_감싸면_0_에서_자라는_칸이_그려진다() {
        val star = ZeroSafeShape(PolygonShape.star(5, outerRounding = CornerRounding.dp(2.dp)))
        var height by mutableStateOf(0.dp)
        rule.setContent {
            Column(Modifier.size(300.dp)) {
                Box(Modifier.size(120.dp, 40.dp).background(Color.Blue))
                Box(
                    Modifier
                        .testTag("grow")
                        .size(120.dp, height)
                        .clip(star)
                        .background(Color.Red, star)
                )
            }
        }
        rule.waitForIdle()
        rule.runOnIdle { height = 80.dp }
        rule.waitForIdle()
        val red = countColor(pixels("grow")) { it.red > 0.8f && it.green < 0.2f && it.blue < 0.2f }
        assertTrue("80dp 로 자란 별의 빨간 픽셀 $red", red > 1000)
    }

    @Test
    fun 감싸지_않은_PolygonShape_는_높이_0_칸을_그리면_던진다() {
        val error = runCatching {
            rule.setContent {
                Column(Modifier.size(300.dp)) {
                    Box(Modifier.size(120.dp, 40.dp).background(Color.Blue))
                    Box(Modifier.size(120.dp, 0.dp).background(Color.Red, PolygonShape.star(5)))
                }
            }
            rule.waitForIdle()
        }.exceptionOrNull()
        assertTrue("받은 것: $error", error is IllegalArgumentException)
    }

    @Test
    fun 새_border_는_다각형에_RuntimeShader_테두리를_그린다() {
        assumeTrue("RuntimeShader 는 API 33+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        val star = PolygonShape.star(6, innerRadiusRatio = 0.6f, outerRounding = CornerRounding.dp(4.dp))
        rule.setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) RuntimeShaderBorderTile(star)
        }
        rule.waitForIdle()
        val map = pixels("shaderBorder")
        // 무지개 테두리 — 채도 높은 픽셀의 색상(hue)이 여섯 칸 중 넷 이상에 퍼져 있어야 한다
        val buckets = IntArray(6)
        for (y in 0 until map.height) for (x in 0 until map.width) {
            val c = map[x, y]
            val hi = max(max(c.red, c.green), c.blue)
            val lo = min(min(c.red, c.green), c.blue)
            if (c.alpha < 0.9f || hi - lo < 0.4f) continue
            val hue = when (hi) {
                c.red -> ((c.green - c.blue) / (hi - lo) + 6f) % 6f
                c.green -> (c.blue - c.red) / (hi - lo) + 2f
                else -> (c.red - c.green) / (hi - lo) + 4f
            }
            buckets[hue.toInt().coerceIn(0, 5)]++
        }
        val filled = buckets.count { it >= 20 }
        assertTrue("색상 칸 ${buckets.toList()}", filled >= 4)
    }
}
