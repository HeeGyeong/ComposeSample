package com.example.composesample.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * parseBoldMarkup() 의 **강조** 변환 검증 (boldMarkup() 은 이 결과로 AnnotatedString 을 만든다)
 * given : 테스트를 위한 사전 준비를 설정하는 단계
 * when : 실제로 테스트하고자 하는 동작을 실행하는 단계
 * then : 실행 결과를 검증하는 단계
 */
class TextMarkupUtilTest {

    @Test
    fun `별표 짝은 지워지고 그 사이 글자만 굵게 표시된다`() {
        // Given
        val source = "앞 **강조 하나** 중간 **둘** 끝"

        // When
        val result = parseBoldMarkup(source)

        // Then
        assertEquals("앞 강조 하나 중간 둘 끝", result.plainText)
        assertEquals(listOf("강조 하나", "둘"), result.boldRanges.map { result.plainText.substring(it.first, it.last + 1) })
    }

    @Test
    fun `별표가 없으면 굵게 구간 없이 그대로다`() {
        // Given & When
        val result = parseBoldMarkup("그냥 문장")

        // Then
        assertEquals("그냥 문장", result.plainText)
        assertTrue(result.boldRanges.isEmpty())
    }

    @Test
    fun `짝이 맞지 않는 마지막 별표는 글자 그대로 남는다`() {
        // Given & When
        val result = parseBoldMarkup("**완성** 그리고 **미완")

        // Then
        assertEquals("완성 그리고 **미완", result.plainText)
        assertEquals(listOf(0..1), result.boldRanges)
    }
}
