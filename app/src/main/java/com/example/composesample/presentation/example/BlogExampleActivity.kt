package com.example.composesample.presentation.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.component.animation.AnimationExampleUI
import com.example.composesample.presentation.example.component.api.ApiDisconnectExampleUI
import com.example.composesample.presentation.example.component.api.KtorExampleUI
import com.example.composesample.presentation.example.component.bottomsheet.BottomSheetUI
import com.example.composesample.presentation.example.component.bottomsheet.CustomBottomSheetUI
import com.example.composesample.presentation.example.component.bottomsheet.ModalBottomSheetUI
import com.example.composesample.presentation.example.component.cache.DataCacheExampleUI
import com.example.composesample.presentation.example.component.clickevent.ClickEventUI
import com.example.composesample.presentation.example.component.coordinator.CoordinatorExampleUI
import com.example.composesample.presentation.example.component.coroutine.CoroutineExampleUI
import com.example.composesample.presentation.example.component.cursor.CursorIDEExample
import com.example.composesample.presentation.example.component.drag.DragAndDropExampleUI
import com.example.composesample.presentation.example.component.drawer.ModalDrawerUI
import com.example.composesample.presentation.example.component.drawer.ScaffoldDrawerUI
import com.example.composesample.presentation.example.component.effect.SideEffectExampleUI
import com.example.composesample.presentation.example.component.ffmpeg.FfmpegEncodingUI
import com.example.composesample.presentation.example.component.ffmpeg.executeCommand
import com.example.composesample.presentation.example.component.ffmpeg.getRealPathFromURI
import com.example.composesample.presentation.example.component.flexbox.FlexBoxUI
import com.example.composesample.presentation.example.component.header.StickyHeaderExampleUI
import com.example.composesample.presentation.example.component.intent.PassingIntentDataExample
import com.example.composesample.presentation.example.component.lazycolumn.LazyColumnFlingBehaviorExample
import com.example.composesample.presentation.example.component.lazycolumn.LazyColumnIssueUI
import com.example.composesample.presentation.example.component.lazycolumn.ReverseLazyColumnExampleUI
import com.example.composesample.presentation.example.component.lottie.LottieExampleUI
import com.example.composesample.presentation.example.component.mvi.MVIExampleUI
import com.example.composesample.presentation.example.component.navigation.BottomNavigationActivity
import com.example.composesample.presentation.example.component.pager.PullScreenPagerUI
import com.example.composesample.presentation.example.component.powersave.PowerSaveModeExampleUI
import com.example.composesample.presentation.example.component.recorder.AudioRecorderUI
import com.example.composesample.presentation.example.component.refresh.PullToRefreshUI
import com.example.composesample.presentation.example.component.shimmer.ShimmerExampleUI
import com.example.composesample.presentation.example.component.shimmer.TextShimmerExampleUI
import com.example.composesample.presentation.example.component.sse.SSEExampleUI
import com.example.composesample.presentation.example.component.swipe.SwipeToDismissUI
import com.example.composesample.presentation.example.component.test.UITestExampleUI
import com.example.composesample.presentation.example.component.text.TextStyleUI
import com.example.composesample.presentation.example.component.version.TargetSDK34Example
import com.example.composesample.presentation.example.component.webview.WebViewIssueUI
import com.example.composesample.presentation.example.component.workmanager.WorkManagerUI
import com.example.composesample.presentation.getTextStyle
import com.example.composesample.presentation.legacy.base.SetSystemUI
import com.example.composesample.presentation.openWebPage
import com.example.composesample.util.ConstValue.Companion.AnimationExample
import com.example.composesample.util.ConstValue.Companion.ApiDisconnectExample
import com.example.composesample.util.ConstValue.Companion.AudioRecorderExample
import com.example.composesample.util.ConstValue.Companion.BottomNavigationExample
import com.example.composesample.util.ConstValue.Companion.BottomSheetExample
import com.example.composesample.util.ConstValue.Companion.ClickEventExample
import com.example.composesample.util.ConstValue.Companion.CoordinatorExample
import com.example.composesample.util.ConstValue.Companion.CoroutineExample
import com.example.composesample.util.ConstValue.Companion.CursorIDEExample
import com.example.composesample.util.ConstValue.Companion.CustomBottomSheetExample
import com.example.composesample.util.ConstValue.Companion.DataCacheExample
import com.example.composesample.util.ConstValue.Companion.DragAndDropExample
import com.example.composesample.util.ConstValue.Companion.ExampleType
import com.example.composesample.util.ConstValue.Companion.FfmpegExample
import com.example.composesample.util.ConstValue.Companion.FlexBoxLayoutExample
import com.example.composesample.util.ConstValue.Companion.FlingBehaviorExample
import com.example.composesample.util.ConstValue.Companion.IntentType
import com.example.composesample.util.ConstValue.Companion.KtorExample
import com.example.composesample.util.ConstValue.Companion.LazyColumnExample
import com.example.composesample.util.ConstValue.Companion.LottieExample
import com.example.composesample.util.ConstValue.Companion.MVIExample
import com.example.composesample.util.ConstValue.Companion.ModalBottomSheetExample
import com.example.composesample.util.ConstValue.Companion.ModalDrawExample
import com.example.composesample.util.ConstValue.Companion.PassingIntentDataExample
import com.example.composesample.util.ConstValue.Companion.PowerSaveModeExample
import com.example.composesample.util.ConstValue.Companion.PullScreenPager
import com.example.composesample.util.ConstValue.Companion.PullToRefreshExample
import com.example.composesample.util.ConstValue.Companion.ReverseLazyColumnExample
import com.example.composesample.util.ConstValue.Companion.SSEExample
import com.example.composesample.util.ConstValue.Companion.ScaffoldDrawExample
import com.example.composesample.util.ConstValue.Companion.UIShimmerExample
import com.example.composesample.util.ConstValue.Companion.SideEffectExample
import com.example.composesample.util.ConstValue.Companion.StickyHeaderExample
import com.example.composesample.util.ConstValue.Companion.SwipeToDismissExample
import com.example.composesample.util.ConstValue.Companion.TargetSDK34PermissionExample
import com.example.composesample.util.ConstValue.Companion.TestExample
import com.example.composesample.util.ConstValue.Companion.TextShimmerExample
import com.example.composesample.util.ConstValue.Companion.TextStyleExample
import com.example.composesample.util.ConstValue.Companion.WebViewIssueExample
import com.example.composesample.util.ConstValue.Companion.WorkManagerExample
import com.example.composesample.util.component.Toast
import com.example.composesample.util.noRippleClickable
import com.example.domain.model.ExampleMoveType
import com.example.domain.model.ExampleObject
import org.koin.androidx.compose.koinViewModel

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
            val type = intent.getStringExtra(IntentType) ?: ExampleType
            val blogExampleViewModel: BlogExampleViewModel = koinViewModel()

            blogExampleViewModel.initExampleObject()

            blogExampleViewModel.setStudyType(
                when (type) {
                    ExampleType -> ExampleMoveType.UI
                    else -> ExampleMoveType.EMPTY
                }
            )

            SetSystemUI()
            BlogExampleScreen(
                launcher = launcher,
                blogExampleViewModel = blogExampleViewModel
            )

            Toast(stream = blogExampleViewModel.toast)
        }
    }
}

@Composable
fun BlogExampleScreen(
    launcher: ActivityResultLauncher<String>,
    blogExampleViewModel: BlogExampleViewModel
) {
    val context = LocalContext.current
    val exampleType = remember { mutableStateOf("") }
    val exampleMoveType = remember { mutableStateOf(ExampleMoveType.UI) }
    val studyType = blogExampleViewModel.studyType.collectAsState().value
    val exampleObjectList = blogExampleViewModel.exampleObjectList.collectAsState().value
    val searchText by blogExampleViewModel.searchText.collectAsState()
    val searchExampleList = blogExampleViewModel.searchExampleList.collectAsState(listOf()).value
    val subCategoryList = blogExampleViewModel.subCategoryList.collectAsState(listOf()).value

    LaunchedEffect(Unit) {
        blogExampleViewModel.reverseExampleList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ExampleListContent(
            searchText = searchText,
            subCategoryList = subCategoryList,
            exampleObjectList = exampleObjectList,
            searchExampleList = searchExampleList,
            blogExampleViewModel = blogExampleViewModel,
            exampleType = exampleType,
            exampleMoveType = exampleMoveType,
            context = context
        )

        ExampleCaseUI(
            exampleType = exampleType,
            exampleMoveType = exampleMoveType,
            launcher = launcher
        ) {
            exampleType.value = ""
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExampleListContent(
    searchText: String,
    subCategoryList: List<ExampleObject>,
    exampleObjectList: List<ExampleObject>,
    searchExampleList: List<ExampleObject>,
    blogExampleViewModel: BlogExampleViewModel,
    exampleType: MutableState<String>,
    exampleMoveType: MutableState<ExampleMoveType>,
    context: Context
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        stickyHeader {
            ExampleHeader(
                searchText = searchText,
                onSearchTextChange = blogExampleViewModel::onSearchTextChange,
                onSortClick = blogExampleViewModel::reverseExampleList,
                onBackClick = {
                    if (subCategoryList.isNotEmpty()) {
                        blogExampleViewModel.setSubCategoryList("")
                    } else {
                        (context as? Activity)?.finish()
                    }
                }
            )
        }

        when {
            subCategoryList.isNotEmpty() -> {
                items(subCategoryList) { exampleObject ->
                    ExampleCardSection(
                        context = context,
                        type = exampleObject.exampleType,
                        subCategory = exampleObject.subCategory,
                        exampleLastUpdate = exampleObject.lastUpdate,
                        exampleTitle = exampleObject.title,
                        exampleDescription = exampleObject.description,
                        exampleBlogUrl = exampleObject.blogUrl,
                        onButtonClick = {
                            exampleType.value = exampleObject.exampleType
                            exampleMoveType.value = exampleObject.moveType
                            blogExampleViewModel.setSubCategoryList(exampleObject.subCategory)
                        },
                        noBlogUrlEvent = {
                            blogExampleViewModel.sendToastMessage("블로그 글이 존재하지 않는 예제입니다.\n코드로 확인해주세요!")
                        }
                    )
                }
            }

            searchText.isEmpty() -> {
                items(exampleObjectList) { exampleObject ->
                    ExampleCardSection(
                        context = context,
                        type = exampleObject.exampleType,
                        subCategory = exampleObject.subCategory,
                        exampleLastUpdate = exampleObject.lastUpdate,
                        exampleTitle = exampleObject.title,
                        exampleDescription = exampleObject.description,
                        exampleBlogUrl = exampleObject.blogUrl,
                        onButtonClick = {
                            if (exampleObject.subCategory.isNotEmpty()) {
                                blogExampleViewModel.setSubCategoryList(exampleObject.subCategory)
                            } else {
                                exampleType.value = exampleObject.exampleType
                                exampleMoveType.value = exampleObject.moveType
                            }
                        },
                        noBlogUrlEvent = {
                            blogExampleViewModel.sendToastMessage("블로그 글이 존재하지 않는 예제입니다.\n코드로 확인해주세요!")
                        }
                    )
                }
            }

            searchExampleList.isNotEmpty() -> {
                items(searchExampleList) { exampleObject ->
                    ExampleCardSection(
                        context = context,
                        type = exampleObject.exampleType,
                        subCategory = exampleObject.subCategory,
                        exampleLastUpdate = exampleObject.lastUpdate,
                        exampleTitle = exampleObject.title,
                        exampleDescription = exampleObject.description,
                        exampleBlogUrl = exampleObject.blogUrl,
                        onButtonClick = {
                            if (exampleObject.subCategory.isNotEmpty()) {
                                blogExampleViewModel.setSubCategoryList(exampleObject.subCategory)
                            } else {
                                exampleType.value = exampleObject.exampleType
                                exampleMoveType.value = exampleObject.moveType
                            }
                        },
                        noBlogUrlEvent = {
                            blogExampleViewModel.sendToastMessage("블로그 글이 존재하지 않는 예제입니다.\n코드로 확인해주세요!")
                        }
                    )
                }
            }

            else -> {
                item { NoSearchResultsMessage() }
            }
        }
    }
}

@Composable
private fun ExampleHeader(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSortClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.background(color = Color.LightGray)
    ) {
        MainHeader(
            title = "Compose Function Sample",
            onBackIconClicked = onBackClick
        )

        SearchAndSortSection(
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            onSortClick = onSortClick
        )
    }
}

@Composable
private fun SearchAndSortSection(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSortClick: () -> Unit
) {
    Column(modifier = Modifier.background(color = Color.LightGray)) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.padding(horizontal = 20.dp)) {
            SearchTextField(
                searchText = searchText,
                onSearchTextChange = onSearchTextChange,
                modifier = Modifier.weight(0.7f)
            )

            Spacer(modifier = Modifier.width(20.dp))

            SortButton(
                onClick = onSortClick,
                modifier = Modifier.weight(0.2f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SearchTextField(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier.height(52.dp),
        value = searchText,
        onValueChange = onSearchTextChange,
        placeholder = {
            Text(text = "검색할 제목을 입력해주세요.", color = Color.Black)
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.Transparent,
            cursorColor = Color.Red,
            placeholderColor = Color.LightGray,
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Gray,
            errorBorderColor = Color.Red,
            textColor = Color.Black,
        ),
        singleLine = true,
    )
}

@Composable
private fun SortButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .noRippleClickable {
                onClick.invoke()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "정렬 기준\n반대로 변경",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NoSearchResultsMessage() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "검색 결과와 일치하는 게시글이 없습니다.",
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
    }
}

@Composable
fun ExampleCardSection(
    context: Context,
    type: String,
    subCategory: String,
    exampleLastUpdate: String,
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
            if (exampleLastUpdate.isNotEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Last update : $exampleLastUpdate",
                    color = Color.LightGray,
                    style = getTextStyle(12)
                )

                Spacer(modifier = Modifier.height(2.dp))
            }

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
                            text = if (subCategory.isEmpty()) {
                                "Sample UI"
                            } else {
                                "Sample List"
                            },
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
    exampleMoveType: MutableState<ExampleMoveType>,
    launcher: ActivityResultLauncher<String>,
    onBackEvent: () -> Unit
) {
    val context = LocalContext.current

    when (exampleMoveType.value) {
        ExampleMoveType.UI -> {
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

                        SideEffectExample -> {
                            SideEffectExampleUI(onBackEvent)
                        }

                        DataCacheExample -> {
                            DataCacheExampleUI(onBackEvent)
                        }

                        ApiDisconnectExample -> {
                            ApiDisconnectExampleUI(onBackEvent)
                        }

                        PowerSaveModeExample -> {
                            PowerSaveModeExampleUI(onBackEvent)
                        }

                        DragAndDropExample -> {
                            DragAndDropExampleUI(onBackEvent)
                        }

                        TargetSDK34PermissionExample -> {
                            TargetSDK34Example(onBackEvent)
                        }

                        PassingIntentDataExample -> {
                            PassingIntentDataExample(onBackEvent)
                        }

                        LottieExample -> {
                            LottieExampleUI(onBackEvent)
                        }

                        StickyHeaderExample -> {
                            StickyHeaderExampleUI(onBackEvent)
                        }

                        CursorIDEExample -> {
                            CursorIDEExample(onBackEvent)
                        }

                        KtorExample -> {
                            KtorExampleUI(onBackEvent)
                        }

                        AnimationExample -> {
                            AnimationExampleUI(onBackEvent)
                        }

                        SSEExample -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                SSEExampleUI(onBackEvent)
                            }
                        }

                        MVIExample -> {
                            MVIExampleUI(onBackEvent)
                        }

                        CoordinatorExample -> {
                            CoordinatorExampleUI(onBackEvent)
                        }

                        TestExample -> {
                            UITestExampleUI(onBackEvent)
                        }

                        ReverseLazyColumnExample -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                ReverseLazyColumnExampleUI(onBackEvent)
                            }
                        }

                        CoroutineExample -> {
                            CoroutineExampleUI(onBackEvent)
                        }

                        UIShimmerExample -> {
                            ShimmerExampleUI(onBackEvent)
                        }

                        TextShimmerExample -> {
                            TextShimmerExampleUI(onBackEvent)
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

        ExampleMoveType.ACTIVITY -> {
            // 해당 케이스는 Activity 호출이기 때문에 예외 처리
            when (exampleType.value) {
                BottomNavigationExample -> {
                    val intent =
                        Intent(
                            context,
                            BottomNavigationActivity::class.java
                        ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
                    context.startActivity(intent)
                }

                else -> {

                }
            }

            exampleMoveType.value = ExampleMoveType.EMPTY
        }

        ExampleMoveType.EMPTY -> {
            Unit
        }
    }
}