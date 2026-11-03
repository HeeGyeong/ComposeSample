package com.example.composesample.example

import androidx.compose.ui.geometry.Offset
import com.example.composesample.presentation.example.component.ui.shader.FIGURE_EIGHT_FRAMES
import com.example.composesample.presentation.example.component.ui.shader.FILTER_SAMPLES
import com.example.composesample.presentation.example.component.ui.shader.FilterScenario
import com.example.composesample.presentation.example.component.ui.shader.FilterSetting
import com.example.composesample.presentation.example.component.ui.shader.MIN_VISIBLE_CHANGE
import com.example.composesample.presentation.example.component.ui.shader.TILT_GAIN
import com.example.composesample.presentation.example.component.ui.shader.TiltFilter
import com.example.composesample.presentation.example.component.ui.shader.figureEightTilt
import com.example.composesample.presentation.example.component.ui.shader.filterInput
import com.example.composesample.presentation.example.component.ui.shader.runFilterCase
import com.example.composesample.presentation.example.component.ui.shader.toScreenFrame
import com.example.composesample.presentation.example.component.ui.shader.upVectorFromAccelerometer
import com.example.composesample.presentation.example.component.ui.shader.upVectorFromRotationVector
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 스크래치 아트 예제 — 화면과 같은 센서 계산 · 필터 함수를 JVM 에서 돌려 판정을 고정한다.
 * 그리기 횟수 · createShader 횟수는 기기에서만 잰다(android.graphics 가 필요하다).
 */
class ScratchFoilLabTest {

    private val eps = 1e-4f

    /** x 축으로 θ 만큼 돌린 자세의 사원수(x, y, z, w). */
    private fun rotationAboutX(theta: Double) =
        floatArrayOf(sin(theta / 2).toFloat(), 0f, 0f, cos(theta / 2).toFloat())

    @Test
    fun `회전 없는 사원수의 위쪽은 화면 밖 방향이다`() {
        assertArrayEquals(floatArrayOf(0f, 0f, 1f), upVectorFromRotationVector(floatArrayOf(0f, 0f, 0f, 1f)), eps)
    }

    @Test
    fun `회전 벡터와 가속도계는 같은 자세에서 같은 위쪽을 낸다`() {
        val theta = Math.toRadians(35.0)
        val fromRotation = upVectorFromRotationVector(rotationAboutX(theta))
        // 같은 자세에서 가속도계가 재는 값 = 9.81 × 위쪽
        val fromAccel = upVectorFromAccelerometer(
            floatArrayOf(0f, (9.81 * sin(theta)).toFloat(), (9.81 * cos(theta)).toFloat())
        )
        assertArrayEquals(fromAccel, fromRotation, eps)
    }

    @Test
    fun `w 가 없는 회전 벡터는 단위 사원수 조건으로 채운다`() {
        val q = rotationAboutX(Math.toRadians(20.0))
        assertArrayEquals(upVectorFromRotationVector(q), upVectorFromRotationVector(q.copyOf(3)), eps)
    }

    @Test
    fun `어느 방향으로 세워 들어도 위쪽은 화면 위쪽이다`() {
        // ROTATION_90 = 기기를 반시계로 90° 돌림 → 기기 오른쪽 변(+x)이 위로 간다(가속도계 x = +g)
        assertArrayEquals(floatArrayOf(0f, 1f, 0f), toScreenFrame(floatArrayOf(1f, 0f, 0f), 1), eps)
        // ROTATION_270 = 시계로 90° → 기기 왼쪽 변(-x)이 위
        assertArrayEquals(floatArrayOf(0f, 1f, 0f), toScreenFrame(floatArrayOf(-1f, 0f, 0f), 3), eps)
        // ROTATION_180 = 거꾸로 → 기기 아래 변(-y)이 위
        assertArrayEquals(floatArrayOf(0f, 1f, 0f), toScreenFrame(floatArrayOf(0f, -1f, 0f), 2), eps)
        assertArrayEquals(floatArrayOf(0f, 1f, 0f), toScreenFrame(floatArrayOf(0f, 1f, 0f), 0), eps)
    }

    @Test
    fun `가로로 돌린 화면의 오른쪽은 기기의 아래 변 쪽이다`() {
        // ROTATION_90 에서 기기 윗변(+y)은 왼쪽으로 간다 → 화면 x 는 기기 -y
        assertArrayEquals(floatArrayOf(-1f, 0f, 0f), toScreenFrame(floatArrayOf(0f, 1f, 0f), 1), eps)
        assertArrayEquals(floatArrayOf(1f, 0f, 0f), toScreenFrame(floatArrayOf(0f, 1f, 0f), 3), eps)
        // 화면 밖(z)은 회전과 무관
        assertEquals(0.9f, toScreenFrame(floatArrayOf(0.1f, 0.2f, 0.9f), 1)[2], eps)
    }

    @Test
    fun `첫 값은 중립이라 내보내지 않는다`() {
        val filter = TiltFilter()
        assertNull(filter.offer(floatArrayOf(0.3f, 0.4f, 0.866f)))
        assertEquals(1, filter.received)
        assertEquals(0, filter.emitted)
    }

    @Test
    fun `누운 기기에서 기울이면 화면 x 로 움직인다`() {
        val filter = TiltFilter(alpha = 1f, minVisibleChange = 0f)
        filter.offer(floatArrayOf(0f, 0f, 1f))
        val angle = Math.toRadians(10.0)
        val out = filter.offer(floatArrayOf(sin(angle).toFloat(), 0f, cos(angle).toFloat()))!!
        assertEquals(TILT_GAIN * sin(angle).toFloat(), out.x, eps)
        assertEquals(0f, out.y, eps)
    }

    @Test
    fun `세워 든 기기의 앞뒤 기울기도 1차로 반응한다`() {
        // 위쪽 = 화면 y 인 자세에서 앞뒤로 10° — y 성분만 보면 1 - cos10° = 0.015 밖에 안 바뀐다
        val filter = TiltFilter(alpha = 1f, minVisibleChange = 0f)
        filter.offer(floatArrayOf(0f, 1f, 0f))
        val angle = Math.toRadians(10.0)
        val out = filter.offer(floatArrayOf(0f, cos(angle).toFloat(), sin(angle).toFloat()))!!
        assertEquals(0f, out.x, eps)
        assertEquals(TILT_GAIN * sin(angle).toFloat(), abs(out.y), eps)
        assertTrue("y 성분만 쓰면 ${1 - cos(angle)} 뿐", abs(out.y) > 10 * (1 - cos(angle)))
    }

    @Test
    fun `기울기는 -1 에서 1 로 자른다`() {
        val filter = TiltFilter(alpha = 1f, minVisibleChange = 0f)
        filter.offer(floatArrayOf(0f, 0f, 1f))
        val out = filter.offer(floatArrayOf(1f, 0f, 0f))!!
        assertEquals(1f, out.x, eps)
    }

    @Test
    fun `recalibrate 뒤의 첫 값이 새 중립이 되고 그 자세에서 기울인 만큼만 움직인다`() {
        val filter = TiltFilter(alpha = 1f)
        val at30 = floatArrayOf(sin(Math.toRadians(30.0)).toFloat(), 0f, cos(Math.toRadians(30.0)).toFloat())
        val at40 = floatArrayOf(sin(Math.toRadians(40.0)).toFloat(), 0f, cos(Math.toRadians(40.0)).toFloat())
        filter.offer(floatArrayOf(0f, 0f, 1f))
        filter.offer(at30)
        filter.recalibrate()

        assertNull(filter.offer(at30))
        val out = filter.offer(at40)!!
        assertEquals(TILT_GAIN * sin(Math.toRadians(10.0)).toFloat(), out.x, eps)
    }

    @Test
    fun `탁자 위 잡음은 작은 변화 무시가 거의 다 버린다`() {
        val raw = runFilterCase(FilterScenario.TABLE, FilterSetting.RAW)
        val lowPass = runFilterCase(FilterScenario.TABLE, FilterSetting.LOW_PASS)
        val both = runFilterCase(FilterScenario.TABLE, FilterSetting.LOW_PASS_AND_THRESHOLD)

        assertEquals(FILTER_SAMPLES, raw.received)
        // 저역 통과만으로는 값이 조금씩 계속 바뀌어 거의 매번 내보낸다
        assertTrue("raw ${raw.emitted}", raw.emitted >= FILTER_SAMPLES - 1)
        assertTrue("lowPass ${lowPass.emitted}", lowPass.emitted >= FILTER_SAMPLES - 1)
        // 첫 값(중립)에도 잡음이 섞여 있어 한두 번은 문턱을 넘는다
        assertTrue("both ${both.emitted}", both.emitted <= 2)
    }

    @Test
    fun `천천히 기울이면 따라가고 멈추면 곧 조용해진다`() {
        val lowPass = runFilterCase(FilterScenario.SLOW_TILT, FilterSetting.LOW_PASS)
        val both = runFilterCase(FilterScenario.SLOW_TILT, FilterSetting.LOW_PASS_AND_THRESHOLD)
        val target = TILT_GAIN * sin(Math.toRadians(20.0)).toFloat()

        assertTrue("움직이는 동안은 따라간다 ${both.emitted}", both.emitted > 20)
        assertEquals(target, both.last.x, 2 * MIN_VISIBLE_CHANGE + 0.01f)
        assertTrue(
            "멈춘 동안: 저역 통과만 ${lowPass.emittedWhileStill} · 작은 변화 무시 ${both.emittedWhileStill}",
            both.emittedWhileStill * 4 < lowPass.emittedWhileStill
        )
    }

    @Test
    fun `고정 입력은 같은 시드로 늘 같다`() {
        val a = filterInput(FilterScenario.SLOW_TILT)
        val b = filterInput(FilterScenario.SLOW_TILT)
        assertEquals(FILTER_SAMPLES, a.size)
        a.indices.forEach { assertArrayEquals(a[it], b[it], 0f) }
    }

    @Test
    fun `8자 궤적은 제자리에서 시작해 제자리로 돌아오고 프레임마다 값이 다르다`() {
        val points = (0..FIGURE_EIGHT_FRAMES).map { figureEightTilt(it / FIGURE_EIGHT_FRAMES.toFloat()) }
        assertEquals(0f, points.first().getDistance(), eps)
        assertEquals(0f, points.last().getDistance(), eps)
        assertEquals(FIGURE_EIGHT_FRAMES, points.drop(1).distinct().size)
        assertTrue(points.all { abs(it.x) <= 1f && abs(it.y) <= 1f })
    }
}
