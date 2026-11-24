package com.example.composesample.example

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.composesample.presentation.example.component.ui.shapes.CountingShape
import com.example.composesample.presentation.example.component.ui.shapes.FractionBase
import com.example.composesample.presentation.example.component.ui.shapes.GalleryKind
import com.example.composesample.presentation.example.component.ui.shapes.MorphRun
import com.example.composesample.presentation.example.component.ui.shapes.MorphVariant
import com.example.composesample.presentation.example.component.ui.shapes.OutlineCounter
import com.example.composesample.presentation.example.component.ui.shapes.RoundingChoice
import com.example.composesample.presentation.example.component.ui.shapes.ZeroCase
import com.example.composesample.presentation.example.component.ui.shapes.ZeroSafeShape
import com.example.composesample.presentation.example.component.ui.shapes.fixed
import com.example.composesample.presentation.example.component.ui.shapes.formatDp
import com.example.composesample.presentation.example.component.ui.shapes.formatMicros
import com.example.composesample.presentation.example.component.ui.shapes.fractionBaseDp
import com.example.composesample.presentation.example.component.ui.shapes.galleryShape
import com.example.composesample.presentation.example.component.ui.shapes.pixelDiff
import com.example.composesample.presentation.example.component.ui.shapes.probeZero
import com.example.composesample.presentation.example.component.ui.shapes.ratioLabel
import com.example.composesample.presentation.example.component.ui.shapes.requestedCornerRadiusDp
import com.example.composesample.presentation.example.component.ui.shapes.smoothingChoices
import com.example.composesample.presentation.example.component.ui.shapes.vertexChoices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PolygonShape 예제(#148)의 계산 · 판정 · 크기 0 감시.
 * 외곽선을 실제로 그리는 동작(리컴포지션 0 · 제자리 transform · 셰이더 테두리)은 계측 테스트(PolygonShapeTest)가 본다.
 */
class PolygonShapeLabTest {

    private val density = Density(2f)

    @Test
    fun fraction_의_기준_길이는_도형마다_다르다() {
        // 128dp 정사각형 칸, fraction(0.25f)
        assertEquals(16f, requestedCornerRadiusDp(GalleryKind.Regular, RoundingChoice.Fraction, 128f, 128f))
        assertEquals(16f, requestedCornerRadiusDp(GalleryKind.Star, RoundingChoice.Fraction, 128f, 128f))
        assertEquals(16f, requestedCornerRadiusDp(GalleryKind.PillStar, RoundingChoice.Fraction, 128f, 128f))
        assertEquals(32f, requestedCornerRadiusDp(GalleryKind.Rectangle, RoundingChoice.Fraction, 128f, 128f))
        assertEquals(32f, requestedCornerRadiusDp(GalleryKind.Custom, RoundingChoice.Fraction, 128f, 128f))
        // 56dp 칸이면 같은 fraction 이 비례해서 줄어든다
        assertEquals(7f, requestedCornerRadiusDp(GalleryKind.Regular, RoundingChoice.Fraction, 56f, 56f))
        assertEquals(14f, requestedCornerRadiusDp(GalleryKind.Rectangle, RoundingChoice.Fraction, 56f, 56f))
        // 둥글기 인자가 없는 도형
        assertNull(requestedCornerRadiusDp(GalleryKind.Pill, RoundingChoice.Fraction, 128f, 128f))
        assertNull(requestedCornerRadiusDp(GalleryKind.Circle, RoundingChoice.Dp, 128f, 128f))
    }

    @Test
    fun dp_둥글기는_칸_크기와_무관하다() {
        for (size in listOf(56f, 128f)) {
            assertEquals(8f, requestedCornerRadiusDp(GalleryKind.Star, RoundingChoice.Dp, size, size))
            assertEquals(0f, requestedCornerRadiusDp(GalleryKind.Star, RoundingChoice.None, size, size))
        }
    }

    @Test
    fun 필스타의_기준은_가로로_긴_칸에서_반지름과_달라진다() {
        // 200 × 100: 짧은 변 ÷ 2 = 50 을 안쪽으로 줄이면 150 × 50 → 짧은 쪽 50 (정사각형이 아니면 Radius 와 우연히 같을 뿐)
        assertEquals(50f, fractionBaseDp(FractionBase.PillStarRadius, 200f, 100f))
        // 100 × 300: 50 을 줄이면 50 × 250 → 50
        assertEquals(50f, fractionBaseDp(FractionBase.PillStarRadius, 100f, 300f))
        assertEquals(64f, fractionBaseDp(FractionBase.PillStarRadius, 128f, 128f))
        assertEquals(64f, fractionBaseDp(FractionBase.Radius, 128f, 128f))
    }

    @Test
    fun 갤러리_도형은_모든_선택_조합에서_만들어진다() {
        // 팩토리의 require(꼭짓점 수 · smoothing 범위 · innerRadiusRatio) 를 화면의 선택지가 어기지 않는지
        var made = 0
        for (kind in GalleryKind.entries) for (v in vertexChoices) for (r in RoundingChoice.entries) for (s in smoothingChoices) {
            galleryShape(kind, v, r, s)
            made++
        }
        assertEquals(GalleryKind.entries.size * vertexChoices.size * RoundingChoice.entries.size * smoothingChoices.size, made)
    }

    @Test
    fun 모핑_판정_값으로는_프레임마다_리컴포즈_람다는_0() {
        assertTrue(MorphRun(MorphVariant.ValueNew, 2, 52, 50, 50, 0L).matchesExpectation)
        assertFalse("값으로 읽는데 리컴포지션이 없으면 다르다", MorphRun(MorphVariant.ValueNew, 2, 52, 0, 50, 0L).matchesExpectation)
        assertTrue(MorphRun(MorphVariant.LambdaScale, 2, 54, 0, 52, 0L).matchesExpectation)
        assertTrue(MorphRun(MorphVariant.LambdaLayout, 2, 54, 0, 97, 0L).matchesExpectation)
        assertFalse("람다인데 한 번이라도 리컴포즈하면 다르다", MorphRun(MorphVariant.LambdaScale, 2, 54, 1, 52, 0L).matchesExpectation)
        assertFalse("외곽선을 거의 안 만들면 그림이 안 바뀐 것", MorphRun(MorphVariant.LambdaScale, 2, 54, 0, 3, 0L).matchesExpectation)
    }

    @Test
    fun 모핑_시간은_1회와_프레임당을_따로_계산한다() {
        // ② 처럼 프레임당 외곽선이 둘이면 프레임당 시간이 1회의 두 배 가까이
        val run = MorphRun(MorphVariant.LambdaLayout, 2, 50, 0, 100, 50_000_000L)
        assertEquals(500.0, run.microsPerOutline, 0.001)
        assertEquals(1000.0, run.microsPerFrame, 0.001)
        assertEquals(0.0, MorphRun(MorphVariant.LambdaScale, 2, 0, 0, 0, 0L).microsPerFrame, 0.0)
    }

    @Test
    fun pixelDiff_는_채널_차가_있는_픽셀만_세고_가장_큰_차를_적는다() {
        val a = intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFF102030.toInt(), 0x00000000)
        val b = intArrayOf(0xFF000000.toInt(), 0xFFFFFFC0.toInt(), 0xFF112030.toInt(), 0x00000000)
        val diff = pixelDiff(a, b)
        assertEquals(4, diff.total)
        assertEquals(2, diff.differing)
        assertEquals(0x3F, diff.maxChannel)
    }

    @Test
    fun 숫자_표기는_로캘과_무관하게_반올림한다() {
        assertEquals("1.3", fixed(1.25, 1))
        assertEquals("0.0", fixed(-0.04, 1))
        assertEquals("0.28", fixed(0.284, 2))
        assertEquals("76", fixed(75.6, 0))
        assertEquals("-2.5", fixed(-2.5, 1))
        assertEquals("486 µs", formatMicros(486.4))
        assertEquals("13.2 µs", formatMicros(13.24))
        assertEquals("0.28 µs", formatMicros(0.284))
        assertEquals("37배", ratioLabel(486.0, 13.2))
        assertEquals("-", ratioLabel(1.0, 0.0))
        assertEquals("7dp", formatDp(7f))
        assertEquals("17.5dp", formatDp(17.5f))
    }

    @Test
    fun ZeroSafeShape_는_크기_0_에서_감싼_도형을_부르지_않고_빈_사각형을_준다() {
        var calls = 0
        val fake = object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                calls++
                return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
            }
        }
        val safe = ZeroSafeShape(fake)
        for (size in listOf(Size(0f, 0f), Size(240f, 0f), Size(0f, 30f))) {
            val outline = safe.createOutline(size, LayoutDirection.Ltr, density)
            assertTrue(outline is Outline.Rectangle)
            assertEquals(0f, outline.bounds.width * outline.bounds.height, 0f)
        }
        assertEquals(0, calls)
        safe.createOutline(Size(240f, 1f), LayoutDirection.Ltr, density)
        assertEquals(1, calls)
        // 같은 도형을 감싸면 같은 Shape 로 본다(Background 의 외곽선 캐시가 유지된다)
        assertEquals(ZeroSafeShape(fake), safe)
        assertEquals(ZeroSafeShape(fake).hashCode(), safe.hashCode())
    }

    /**
     * 감시 테스트 — Compose 1.13.0 의 PolygonShape 계열은 크기 0 에서 IllegalArgumentException 을 던진다.
     * 예외는 Path 를 만들기 전(graphics-shapes 의 RoundedPolygon 검사)에서 나므로 JVM 에서도 재현된다.
     * 라이브러리를 올려 이 테스트가 깨지면 4번 카드 · exampleGuide · 정리 문구를 고친다.
     */
    @Test
    fun PolygonShape_계열만_0x0_에서_던진다() {
        for (case in ZeroCase.entries) {
            val cell = probeZero(case, Size(0f, 0f), density)
            if (case.polygon) {
                assertFalse("${case.label} 이 0×0 에서 그렸다 — 라이브러리가 고쳐졌는지 확인", cell.ok)
                assertTrue("${case.label}: ${cell.error}", cell.error is IllegalArgumentException)
            } else {
                assertTrue("${case.label}: ${cell.error}", cell.ok)
            }
        }
    }

    @Test
    fun CountingShape_는_횟수와_외곽선을_만든_때의_값을_적는다() {
        val counter = OutlineCounter()
        var degrees = 0f
        val fake = object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
                Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }
        val counting = CountingShape(fake, counter) { degrees }
        counting.createOutline(Size(10f, 10f), LayoutDirection.Ltr, density)
        degrees = 36f
        // 값만 바꾸고 외곽선을 안 만들면 기록은 그대로다
        assertEquals(0f, counter.lastTag)
        counting.createOutline(Size(10f, 10f), LayoutDirection.Ltr, density)
        assertEquals(2, counter.calls)
        assertEquals(36f, counter.lastTag)
        counter.reset()
        assertEquals(0, counter.calls)
        assertEquals(0L, counter.nanos)
    }
}
