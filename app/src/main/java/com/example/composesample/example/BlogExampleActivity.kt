package com.example.composesample.example

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.example.ui.bottomsheet.BottomSheetUI
import com.example.composesample.example.ui.bottomsheet.CustomBottomSheetUI
import com.example.composesample.example.ui.bottomsheet.ModalBottomSheetUI
import com.example.composesample.example.ui.clickevent.ClickEventUI
import com.example.composesample.example.ui.ffmpeg.FfmpegEncodingUI
import com.example.composesample.example.ui.ffmpeg.executeCommand
import com.example.composesample.example.ui.ffmpeg.getRealPathFromURI
import com.example.composesample.example.ui.flexbox.FlexBoxUI
import com.example.composesample.example.ui.lazycolumn.LazyColumnFlingBehaviorExample
import com.example.composesample.example.ui.lazycolumn.LazyColumnIssueUI
import com.example.composesample.example.ui.pager.PullScreenPagerUI
import com.example.composesample.example.ui.recorder.AudioRecorderUI
import com.example.composesample.example.ui.refresh.PullToRefreshUI
import com.example.composesample.example.ui.text.TextStyleUI
import com.example.composesample.example.ui.webview.WebViewIssueUI
import com.example.composesample.example.ui.workmanager.WorkManagerUI
import com.example.composesample.example.util.ConstValue.Companion.AudioRecorderExample
import com.example.composesample.example.util.ConstValue.Companion.BottomSheetExample
import com.example.composesample.example.util.ConstValue.Companion.ClickEventExample
import com.example.composesample.example.util.ConstValue.Companion.CustomBottomSheetExample
import com.example.composesample.example.util.ConstValue.Companion.FfmpegExample
import com.example.composesample.example.util.ConstValue.Companion.FlexBoxLayoutExample
import com.example.composesample.example.util.ConstValue.Companion.FlingBehaviorExample
import com.example.composesample.example.util.ConstValue.Companion.LazyColumnExample
import com.example.composesample.example.util.ConstValue.Companion.ModalBottomSheetExample
import com.example.composesample.example.util.ConstValue.Companion.PullScreenPager
import com.example.composesample.example.util.ConstValue.Companion.PullToRefreshExample
import com.example.composesample.example.util.ConstValue.Companion.TextStyleExample
import com.example.composesample.example.util.ConstValue.Companion.WebViewIssueExample
import com.example.composesample.example.util.ConstValue.Companion.WorkManagerExample
import com.example.composesample.example.util.noRippleClickable
import com.example.composesample.main.MainHeader
import com.example.composesample.main.getTextStyle
import com.example.composesample.main.openWebPage
import com.example.composesample.ui.base.SetSystemUI

@ExperimentalAnimationApi
class BlogExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contract = ActivityResultContracts.GetContent()

        val callback = ActivityResultCallback<Uri?> { uri ->
            uri?.let {
                val inputVideoPath = getRealPathFromURI(uri, this)
                inputVideoPath?.let {
                    this.executeCommand(inputVideoPath)
                }
            }
        }

        val launcher = registerForActivityResult(contract, callback)

        setContent {
            SetSystemUI()
            BlogExampleCase(
                launcher = launcher
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlogExampleCase(
    launcher: ActivityResultLauncher<String>
) {
    val context = LocalContext.current
    val exampleType = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            stickyHeader {
                MainHeader(
                    title = "Compose Function Sample",
                    onBackIconClicked = {
                        (context as Activity).finish()
                    }
                )
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {

                    /*ExampleCardSection(
                        context = context,
                        exampleTitle = "Lazy Column Keyboard Issue",
                        exampleDescription = "키보드 이슈 확인 중",
                        exampleBlogUrl = "https://heegs.tistory.com/142",
                        onButtonClick = {
                            exampleType.value = LazyColumnExample
                        }
                    )*/

                    // Button UI 변경 및 Button 아래 해당 Blog, Github Code에 관련한 Link 추가.
                    ExampleButton(
                        buttonText = "Lazy Column Keyboard Issue",
                        type = LazyColumnExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Click Event",
                        type = ClickEventExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "FlexBox Layout Example",
                        type = FlexBoxLayoutExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Youtube WebView Issue Example",
                        type = WebViewIssueExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Text Style Example",
                        type = TextStyleExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Video Encoding Example",
                        type = FfmpegExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Audio Recorder Example",
                        type = AudioRecorderExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Work Manager Example",
                        type = WorkManagerExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Pull to Refresh example",
                        type = PullToRefreshExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Pull screen pager example",
                        type = PullScreenPager,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "LazyColumn FlingBehavior Example",
                        type = FlingBehaviorExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Bottom Sheet Example",
                        type = BottomSheetExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Modal Bottom Sheet Example",
                        type = ModalBottomSheetExample,
                        exampleType = exampleType
                    )

                    ExampleButton(
                        buttonText = "Custom Bottom Sheet Example",
                        type = CustomBottomSheetExample,
                        exampleType = exampleType
                    )
                }
            }
        }

        ExampleCaseUI(
            exampleType = exampleType,
            launcher = launcher,
        ) {
            exampleType.value = ""
        }
    }
}

@Composable
fun ColumnScope.ExampleButton(
    buttonText: String,
    type: String,
    exampleType: MutableState<String>,
    onButtonClick: () -> Unit = {
        exampleType.value = type
    }
) {
    Button(
        modifier = Modifier
            .padding(vertical = 20.dp)
            .align(Alignment.CenterHorizontally),
        onClick = {
            onButtonClick.invoke()

        },
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = buttonText,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
        )
    }
}

/**
 * Example 관련 Sample 버튼 및 관련하여 정리한 Blog URL을 입력하기 위한 CardView
 *
 * 일괄적으로 블로그 글 찾아서 변경 할 예정.
 */
@Composable
fun ExampleCardSection(
    context: Context,
    exampleTitle: String,
    exampleDescription: String,
    exampleBlogUrl: String,
    onButtonClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.DarkGray,
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = exampleTitle,
                color = Color.White,
                style = getTextStyle(18)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = exampleDescription,
                style = getTextStyle(14),
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 10.dp)
            ) {
                // Move Sample Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .noRippleClickable {
                            onButtonClick.invoke()
                        },
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color.White,
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .align(Alignment.CenterHorizontally),
                            text = "Sample UI",
                            color = Color.Black,
                            style = getTextStyle(18)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Move Blog Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .noRippleClickable {
                            openWebPage(
                                context = context,
                                url = exampleBlogUrl
                            )
                        },
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color.White,
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .align(Alignment.CenterHorizontally),
                            text = "Explain Blog",
                            color = Color.Black,
                            style = getTextStyle(18)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExampleCaseUI(
    exampleType: MutableState<String>,
    launcher: ActivityResultLauncher<String>,
    onBackEvent: () -> Unit
) {
    AnimatedVisibility(
        visible = exampleType.value.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)
                .animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { height -> height },
                        animationSpec = tween()
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { height -> height },
                        animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)
                    )
                )
        ) {
            when (exampleType.value) {
                LazyColumnExample -> {
                    LazyColumnIssueUI(onBackEvent)
                }

                ClickEventExample -> {
                    ClickEventUI(onBackEvent)
                }

                FlexBoxLayoutExample -> {
                    FlexBoxUI(onBackEvent)
                }

                WebViewIssueExample -> {
                    WebViewIssueUI(onBackEvent)
                }

                TextStyleExample -> {
                    TextStyleUI(onBackEvent)
                }

                FfmpegExample -> {
                    FfmpegEncodingUI(
                        launcher = launcher,
                        onBackButtonClick = onBackEvent,
                    )
                }

                AudioRecorderExample -> {
                    AudioRecorderUI(onBackEvent)
                }

                WorkManagerExample -> {
                    WorkManagerUI(onBackEvent)
                }

                PullToRefreshExample -> {
                    PullToRefreshUI(onBackEvent)
                }

                PullScreenPager -> {
                    PullScreenPagerUI(onBackEvent)
                }

                FlingBehaviorExample -> {
                    LazyColumnFlingBehaviorExample(onBackEvent)
                }

                BottomSheetExample -> {
                    BottomSheetUI(onBackEvent)
                }

                ModalBottomSheetExample -> {
                    ModalBottomSheetUI(onBackEvent)
                }

                CustomBottomSheetExample -> {
                    CustomBottomSheetUI(onBackEvent)
                }

                else -> {
                    Text(
                        text = "Dummy",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}