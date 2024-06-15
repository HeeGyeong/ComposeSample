package com.example.composesample.presentation.hash

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.composesample.R


enum class TagType {
    Normal,
    HashTag,
    Mention,
}

@Composable
fun HashTagUI() {
    val string = "#질문 #테스트 @Heegs 해시태그가 제대로 되고 있나요. #궁금 @Others 응답해주세요." +
            "Test Text #@#@#@#@#@#@#@ #.@. #@. @#. #.@ @.# :: #..@., #@.. @#,. #..@ @.,# !@123 !#123 !@:{] !#[];:" +
            ">>>> @3$3 #3$# @$55 #%44 >>>>> #가다_다라 @마바_사아"

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HashtagsMentionsTextView(string) {
            println(it)
        }
    }
}

@Composable
fun HashtagsMentionsTextView(
    text: String,
    onClick: (String) -> Unit
) {

    val normalStyle = SpanStyle(color = colorResource(id = R.color.black))
    val hashTagStyle = SpanStyle(color = colorResource(id = R.color.purple_700))
    val mentionStyle = SpanStyle(color = colorResource(id = R.color.teal_700))

    // HashTag, Mention 을 찾을 정규식.
    val tags = Regex("((?=[^\\w!])[#@][\\u4e00-\\u9fa5\\w]+)")

    val subStringList = remember {
        var lastIndex = 0
        val annotatedStringList = mutableStateListOf<AnnotatedString.Range<String>>()

        // findAll을 사용하여 InputData(text)에서 hashTag에 작성한 정규식에 해당하는 경우를 반환합니다.
        for (textIndex in tags.findAll(text)) {

            val start = textIndex.range.first
            val end = textIndex.range.last + 1
            val string = text.substring(start, end)

            Log.d("hashTags", "start : $start , end : $end , string : $string")
            Log.d("hashTags", "lastIndex ? $lastIndex > $end")

            // 정규식에 걸리는 두 개의 단어 사이에 추가적인 Text가 있다면 일반 Text로 추가.
            if (start > lastIndex) {
                annotatedStringList.add(
                    AnnotatedString.Range(
                        item = text.substring(lastIndex, start),
                        start = lastIndex,
                        end = start,
                        tag = TagType.Normal.name
                    )
                )
            }

            if (string.contains("@")) {
                annotatedStringList.add(
                    AnnotatedString.Range(
                        item = string,
                        start = start,
                        end = end,
                        tag = TagType.Mention.name
                    )
                )
            } else {
                annotatedStringList.add(
                    AnnotatedString.Range(
                        item = string,
                        start = start,
                        end = end,
                        tag = TagType.HashTag.name
                    )
                )
            }

            lastIndex = end
        }

        // 마지막 정규식 데이터 다음에 발생하는 Text들은 일반 Text로 설정.
        if (lastIndex < text.length) {
            annotatedStringList.add(
                AnnotatedString.Range(
                    item = text.substring(lastIndex, text.length),
                    start = lastIndex,
                    end = text.length,
                    tag = TagType.Normal.name
                )
            )
        }

        // 위에서 나눈 StringList를 반환.
        annotatedStringList
    }

    // 구분한 subStringList의 Tag에 따라 TextStyle 설정을 해주는 부분.
    val annotatedString = buildAnnotatedString {
        subStringList.forEach {
            when (it.tag) {
                TagType.HashTag.name -> {
                    pushStringAnnotation(tag = it.tag, annotation = it.item)
                    withStyle(style = hashTagStyle) { append(it.item) }
                    /*
                    하나의 스타일을 변경 후 pop을 하지 않으면,
                    이후에 별도로 추가할 Style에 대하여 정상 동작을 하지 않습니다.
                     */
                    pop()
                }
                TagType.Mention.name -> {
                    pushStringAnnotation(tag = it.tag, annotation = it.item)
                    withStyle(style = mentionStyle) { append(it.item) }
                    pop()
                }
                TagType.Normal.name -> {
                    withStyle(style = normalStyle) { append(it.item) }
                }
            }
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.body2,
        onClick = { position ->
            val annotatedStringRange =
                subStringList.first { it.start < position && position < it.end }
            if (annotatedStringRange.tag == TagType.HashTag.name) {
                onClick(annotatedStringRange.item + " ${TagType.HashTag.name}")
            } else if (annotatedStringRange.tag == TagType.Mention.name) {
                onClick(annotatedStringRange.item + " ${TagType.Mention.name}")
            }
        }
    )
}