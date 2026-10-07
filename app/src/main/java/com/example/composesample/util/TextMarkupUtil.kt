package com.example.composesample.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * 예제 설명 문자열의 `**강조**` 를 굵은 글씨로 바꾼다.
 * Text 는 마크다운을 해석하지 않으므로, 이걸 거치지 않으면 별표가 화면에 그대로 찍힌다.
 * 짝이 맞지 않는 마지막 `**` 는 글자 그대로 둔다.
 */
fun String.boldMarkup(): AnnotatedString {
    val parsed = parseBoldMarkup(this)
    if (parsed.boldRanges.isEmpty()) return AnnotatedString(parsed.plainText)
    return buildAnnotatedString {
        var cursor = 0
        parsed.boldRanges.forEach { range ->
            append(parsed.plainText.substring(cursor, range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(parsed.plainText.substring(range.first, range.last + 1))
            }
            cursor = range.last + 1
        }
        append(parsed.plainText.substring(cursor))
    }
}

/** 별표를 걷어낸 본문과, 그 본문 기준 굵게 표시할 구간 */
internal data class BoldMarkup(val plainText: String, val boldRanges: List<IntRange>)

internal fun parseBoldMarkup(source: String): BoldMarkup {
    val plain = StringBuilder()
    val ranges = mutableListOf<IntRange>()
    var index = 0
    while (index < source.length) {
        val start = source.indexOf("**", index)
        val end = if (start >= 0) source.indexOf("**", start + 2) else -1
        if (start < 0 || end < 0) {
            plain.append(source, index, source.length)
            break
        }
        plain.append(source, index, start)
        val boldStart = plain.length
        plain.append(source, start + 2, end)
        if (plain.length > boldStart) ranges += boldStart until plain.length
        index = end + 2
    }
    return BoldMarkup(plain.toString(), ranges)
}
