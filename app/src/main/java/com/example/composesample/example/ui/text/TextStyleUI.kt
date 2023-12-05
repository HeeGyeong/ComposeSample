package com.example.composesample.example.ui.text

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.composesample.example.util.noRippleClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextStyleUI(onBackButtonClick: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }
        }

        item {
            val visibleFontSizeUI = remember { mutableStateOf(false) }
            val fontSizeUIVisibleText = if (visibleFontSizeUI.value) "감추기" else "보기"

            Text(
                modifier = Modifier
                    .height(30.dp)
                    .noRippleClickable {
                        visibleFontSizeUI.value = !visibleFontSizeUI.value
                    },
                text = "폰트 사이즈 관련 UI $fontSizeUIVisibleText"
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (visibleFontSizeUI.value) {
                CheckFontSizeUI()

                CheckFontSizeUI(14.sp)

                CheckFontSizeUI(1.em)

                CheckFontSizeUI(2.em)

                CheckFontSizeUI(3.em)

                CheckFontSizeUI(4.em)

                CheckFontSizeUI(30.sp)

                val sample = TextUnit.Unspecified.value * 1.em.value
                CheckFontSizeUI((14 * 1.em.value).sp)

                CheckFontSizeUI((14 * 2.em.value).sp)

                CheckFontSizeUI((14 * 3.em.value).sp)

                CheckFontSizeUI(15.sp)

                CheckFontSizeUI(18.em)

                Spacer(modifier = Modifier.height(20.dp))
            }

            val visibleTextSpaceUI = remember { mutableStateOf(false) }
            val textSpaceUIVisibleText = if (visibleTextSpaceUI.value) "감추기" else "보기"

            Text(
                modifier = Modifier
                    .height(30.dp)
                    .noRippleClickable {
                        visibleTextSpaceUI.value = !visibleTextSpaceUI.value
                    },
                text = "텍스트 간격 관련 UI $textSpaceUIVisibleText"
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (visibleTextSpaceUI.value) {
                CheckTextSpaceUI()

                CheckTextSpaceUI(14.sp)

                CheckTextSpaceUI(1.em)

                CheckTextSpaceUI(2.em)

                Text(
                    modifier = Modifier
                        .height(30.dp),
                    text = "실 사용 예시"
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier
                        .wrapContentSize(),
                    text = "이것은 실 사용 예시입니다. 띄어쓰기와 줄바꿈을 수행하는 텍스트는 이렇게 나오게 됩니다.",
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier
                        .wrapContentSize(),
                    text = "이것은 실 사용 예시입니다. 띄어쓰기와 줄바꿈을 수행하는 텍스트는 이렇게 나오게 됩니다.",
                    letterSpacing = -(0.02).em,
                    lineHeight = 1.5.em,
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun CheckTextSpaceUI(fontSize: TextUnit = TextUnit.Unspecified) {
    val text = "01234567890 abcdefghijklnmopqrstuvwxyz 가나다라마바사아자차카타파하"

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        text = "Size : $fontSize",
    )

    Spacer(modifier = Modifier.height(3.dp))
    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text,
        fontSize = 20.sp,
        onTextLayout = {
            Log.d(
                "textStyleUI",
                "fontSize check[$fontSize] 1 : ${it.size} :: ${it.layoutInput.style.fontSize}"
            )
        }
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text,
        fontSize = 20.sp,
        letterSpacing = fontSize,
        onTextLayout = {
            Log.d(
                "textStyleUI",
                "fontSize check[$fontSize] 2 : ${it.size} :: ${it.layoutInput.style.fontSize}"
            )
        }
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text,
        fontSize = 20.sp,
        lineHeight = fontSize,
        onTextLayout = {
            Log.d(
                "textStyleUI",
                "fontSize check[$fontSize] 3 : ${it.size} :: ${it.layoutInput.style.fontSize}"
            )
        }
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text,
        fontSize = 20.sp,
        letterSpacing = fontSize,
        lineHeight = fontSize,
        onTextLayout = {
            Log.d(
                "textStyleUI",
                "fontSize check[$fontSize] 3 : ${it.size} :: ${it.layoutInput.style.fontSize}"
            )
        }
    )

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun CheckFontSizeUI(fontSize: TextUnit = TextUnit.Unspecified) {
    val text1 = "0"
    val text2 = "a"
    val text3 = "ㄱ"

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        text = "Size : $fontSize",
    )
    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text1,
        fontSize = fontSize,
        onTextLayout = {
            Log.d(
                "textStyleUI",
                "fontSize check[$fontSize] 1 : ${it.size} :: ${it.layoutInput.style.fontSize}"
            )
        }
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text2,
        fontSize = fontSize,
        onTextLayout = {
            Log.d(
                "textStyleUI",
                "fontSize check[$fontSize] 2 : ${it.size} :: ${it.layoutInput.style.fontSize}"
            )
        }
    )

    Spacer(modifier = Modifier.height(3.dp))

    Text(
        modifier = Modifier
            .wrapContentSize(),
        text = text3,
        fontSize = fontSize,
        onTextLayout = {
            Log.d(
                "textStyleUI",
                "fontSize check[$fontSize] 3 : ${it.size} :: ${it.layoutInput.style.fontSize}"
            )
        }
    )

    Spacer(modifier = Modifier.height(10.dp))
}