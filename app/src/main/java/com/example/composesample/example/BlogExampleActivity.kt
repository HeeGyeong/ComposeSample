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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composesample.example.ui.bottomsheet.BottomSheetUI
import com.example.composesample.example.ui.bottomsheet.CustomBottomSheetUI
import com.example.composesample.example.ui.bottomsheet.ModalBottomSheetUI
import com.example.composesample.example.ui.clickevent.ClickEventUI
import com.example.composesample.example.ui.drawer.ModalDrawerUI
import com.example.composesample.example.ui.drawer.ScaffoldDrawerUI
import com.example.composesample.example.ui.ffmpeg.FfmpegEncodingUI
import com.example.composesample.example.ui.ffmpeg.executeCommand
import com.example.composesample.example.ui.ffmpeg.getRealPathFromURI
import com.example.composesample.example.ui.flexbox.FlexBoxUI
import com.example.composesample.example.ui.lazycolumn.LazyColumnFlingBehaviorExample
import com.example.composesample.example.ui.lazycolumn.LazyColumnIssueUI
import com.example.composesample.example.ui.pager.PullScreenPagerUI
import com.example.composesample.example.ui.recorder.AudioRecorderUI
import com.example.composesample.example.ui.refresh.PullToRefreshUI
import com.example.composesample.example.ui.swipe.SwipeToDismissUI
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
import com.example.composesample.example.util.ConstValue.Companion.ModalDrawExample
import com.example.composesample.example.util.ConstValue.Companion.ScaffoldDrawExample
import com.example.composesample.example.util.ConstValue.Companion.PullScreenPager
import com.example.composesample.example.util.ConstValue.Companion.PullToRefreshExample
import com.example.composesample.example.util.ConstValue.Companion.SwipeToDismissExample
import com.example.composesample.example.util.ConstValue.Companion.TextStyleExample
import com.example.composesample.example.util.ConstValue.Companion.WebViewIssueExample
import com.example.composesample.example.util.ConstValue.Companion.WorkManagerExample
import com.example.composesample.example.util.Toast
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
            val blogExampleViewModel = viewModel<BlogExampleViewModel>()
            blogExampleViewModel.initExampleObject()

            SetSystemUI()
            BlogExampleCase(
                launcher = launcher,
                blogExampleViewModel = blogExampleViewModel
            )

            Toast(stream = blogExampleViewModel.toast)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlogExampleCase(
    launcher: ActivityResultLauncher<String>,
    blogExampleViewModel: BlogExampleViewModel
) {
    val context = LocalContext.current
    val exampleType = remember { mutableStateOf("") }
    val exampleObjectList = blogExampleViewModel.exampleObjectList.collectAsState().value

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

            items(exampleObjectList) { exampleObject ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    ExampleCardSection(
                        context = context,
                        exampleTitle = exampleObject.title,
                        exampleDescription = exampleObject.description,
                        exampleBlogUrl = exampleObject.blogUrl,
                        onButtonClick = {
                            exampleType.value = exampleObject.exampleType
                        },
                        noBlogUrlEvent = {
                            blogExampleViewModel.sendToastMessage("블로그 글이 존재하지 않는 샘플입니다.\n코드로 확인해주세요!")
                        }
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
fun ExampleCardSection(
    context: Context,
    exampleTitle: String,
    exampleDescription: String,
    exampleBlogUrl: String,
    onButtonClick: () -> Unit,
    noBlogUrlEvent: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                            if (exampleBlogUrl.isNotEmpty()) {
                                openWebPage(
                                    context = context,
                                    url = exampleBlogUrl
                                )
                            } else {
                                noBlogUrlEvent.invoke()
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = if (exampleBlogUrl.isNotEmpty()) {
                        Color.White
                    } else {
                        Color.LightGray
                    },
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .align(Alignment.CenterHorizontally),
                            text = "Explain Blog",
                            color = if (exampleBlogUrl.isNotEmpty()) {
                                Color.Black
                            } else {
                                Color.DarkGray
                            },
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

                ScaffoldDrawExample -> {
                    ScaffoldDrawerUI(onBackEvent)
                }

                ModalDrawExample -> {
                    ModalDrawerUI(onBackEvent)
                }

                SwipeToDismissExample -> {
                    SwipeToDismissUI(onBackEvent)
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