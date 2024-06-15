package com.example.composesample.presentation.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.example.BlogExampleActivity
import com.example.composesample.util.noRippleClickable
import com.example.composesample.ui.base.SetSystemUI

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                color = Color.White
            ) {
                val isViewLegacyPage = remember { mutableStateOf(false) }

                if (isViewLegacyPage.value) {
                    SetSystemUI()
                    AppbarSample(
                        title = "AppBar Main",
                        isViewLegacyPage = isViewLegacyPage
                    )
                } else {
                    MainActivityScreen(
                        isViewLegacyPage = isViewLegacyPage
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun MainActivityScreen(
    isViewLegacyPage: MutableState<Boolean>
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            stickyHeader {
                MainHeader(
                    title = "Compose Example Project",
                    onBackIconClicked = { },
                    onLeftIconContent = { }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp),
                    text = "Sample code section",
                    style = getTextStyle(20),
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(5.dp))

                Divider(
                    color = Color.Black,
                    thickness = 1.dp,
                )

                Spacer(modifier = Modifier.height(20.dp))

                PageMoveCardViewContent(
                    cardTitle = "go to Compose Function Sample",
                    cardDescription = "Github 및 Blog에서 볼 수 있는 Compose 기능 예제 리스트로 이동합니다.",
                    onClickEvent = {
                        context.startActivity(Intent(context, BlogExampleActivity::class.java))
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                PageMoveCardViewContent(
                    cardTitle = "go to Legacy Compose Sample",
                    cardDescription = "Compose를 맨 처음 사용하면서 작성했던 코드로 이동합니다.\n" +
                            "완전히 기본적인 코드들과 기능들을 구현해두었습니다.\n" +
                            "추후 자주 사용하는 기능들은 개선하여 Function Sample에 추가 될 예정입니다.",
                    onClickEvent = {
                        isViewLegacyPage.value = true
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp),
                    text = "Developer Page section",
                    style = getTextStyle(20),
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(5.dp))

                Divider(
                    color = Color.Black,
                    thickness = 1.dp,
                )

                Spacer(modifier = Modifier.height(20.dp))

                UrlCardViewContent(
                    context = context,
                    lastUpdate = "24년 6월",
                    cardTitle = "Tistory Blog",
                    cardDescription = "개발 경험을 기록하거나 공유하기 위하여 시작한 개발 블로그입니다.",
                    cardUrl = "https://heegs.tistory.com"
                )

                Spacer(modifier = Modifier.height(20.dp))

                UrlCardViewContent(
                    context = context,
                    lastUpdate = "24년 6월",
                    cardTitle = "Github",
                    cardDescription = "공부를 하며 작성한 코드를 나만의 레퍼런스로 만들기 위하여 만든 Github 입니다.\n" +
                            "실무에 적용했던 기능을 추후에도 쉽게 사용하기 위해 SampleCode를 만들어 올리고 있습니다.",
                    cardUrl = "https://github.com/HeeGyeong"
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun UrlCardViewContent(
    context: Context,
    lastUpdate: String,
    cardTitle: String,
    cardDescription: String,
    cardUrl: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp)
            .noRippleClickable {
                // WebPage로 이동.
                openWebPage(
                    context = context,
                    url = cardUrl
                )
            },
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.DarkGray,
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = cardTitle,
                color = Color.White,
                style = getTextStyle(18)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = cardDescription,
                style = getTextStyle(14),
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "마지막 업데이트 : ",
                    color = Color.Gray,
                    style = getTextStyle(14)
                )

                Text(
                    text = lastUpdate,
                    color = Color.White,
                    style = getTextStyle(14)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = cardUrl,
                style = getTextStyle(14),
                color = Color.White,
            )
        }
    }
}

@Composable
fun PageMoveCardViewContent(
    cardTitle: String,
    cardDescription: String,
    onClickEvent: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp)
            .noRippleClickable {
                onClickEvent.invoke()
            },
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.DarkGray,
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = cardTitle,
                color = Color.White,
                style = getTextStyle(18)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = cardDescription,
                style = getTextStyle(14),
                color = Color.White,
            )
        }
    }
}

@Composable
fun MainHeader(
    title: String,
    onBackIconClicked: () -> Unit,
    onLeftIconContent: @Composable () -> Unit = {
        Icon(
            modifier = Modifier
                .size(24.dp, 24.dp)
                .noRippleClickable {
                    onBackIconClicked.invoke()
                },
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = ""
        )
    },
) {
    Box(
        modifier = Modifier
            .background(color = Color.LightGray)
            .padding(top = (4.5).dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onLeftIconContent()

                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = title,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

fun openWebPage(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun getTextStyle(fontSize: Int) = androidx.compose.ui.text.TextStyle(
    fontSize = fontSize.sp,
    letterSpacing = (-0.02).em,
    lineHeight = 1.4.em
)