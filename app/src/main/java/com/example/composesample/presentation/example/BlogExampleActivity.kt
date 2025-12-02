package com.example.composesample.presentation.example

// UI Components imports
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import com.example.composesample.presentation.example.component.architecture.development.compose17.FocusRestorerExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.GraphicsLayerExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.LookaheadScopeExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.PathGraphicsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.TextOverflowExampleUI
import com.example.composesample.presentation.example.component.architecture.development.coordinator.CoordinatorExampleUI
import com.example.composesample.presentation.example.component.architecture.development.cursor.CursorIDEExample
import com.example.composesample.presentation.example.component.architecture.development.init.InitTestExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.UITestExampleUI
import com.example.composesample.presentation.example.component.architecture.development.type.TypeExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.CompositionLocalExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.coroutine.CoroutineExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.effect.SideEffectExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.mvi.MVIExampleUI
import com.example.composesample.presentation.example.component.data.api.ApiDisconnectExampleUI
import com.example.composesample.presentation.example.component.data.api.KtorExampleUI
import com.example.composesample.presentation.example.component.data.cache.DataCacheExampleUI
import com.example.composesample.presentation.example.component.data.paging.PagingExampleUI
import com.example.composesample.presentation.example.component.data.sse.SSEExampleUI
import com.example.composesample.presentation.example.component.interaction.clickevent.ClickEventUI
import com.example.composesample.presentation.example.component.interaction.drag.DragAndDropExampleUI
import com.example.composesample.presentation.example.component.interaction.refresh.PullToRefreshUI
import com.example.composesample.presentation.example.component.interaction.swipe.SwipeToDismissUI
import com.example.composesample.presentation.example.component.navigation.BottomNavigationActivity
import com.example.composesample.presentation.example.component.system.background.workmanager.WorkManagerUI
import com.example.composesample.presentation.example.component.system.media.recorder.AudioRecorderUI
import com.example.composesample.presentation.example.component.system.platform.file.SafFileSelectionUI
import com.example.composesample.presentation.example.component.system.platform.intent.PassingIntentDataExample
import com.example.composesample.presentation.example.component.system.platform.language.LanguageSettingExampleUI
import com.example.composesample.presentation.example.component.system.platform.language.LocalLanguageChangeExampleUI
import com.example.composesample.presentation.example.component.system.platform.powersave.PowerSaveModeExampleUI
import com.example.composesample.presentation.example.component.system.platform.shortcut.ShortcutExampleUI
import com.example.composesample.presentation.example.component.system.platform.version.TargetSDK34Example
import com.example.composesample.presentation.example.component.system.platform.webview.WebViewIssueUI
import com.example.composesample.presentation.example.component.ui.layout.animation.AnimationExampleUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.BottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.CustomBottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.ModalBottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.drawer.ModalDrawerUI
import com.example.composesample.presentation.example.component.ui.layout.drawer.ScaffoldDrawerUI
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FlexBoxUI
import com.example.composesample.presentation.example.component.ui.layout.header.StickyHeaderExampleUI
import com.example.composesample.presentation.example.component.ui.layout.lazycolumn.LazyColumnFlingBehaviorExample
import com.example.composesample.presentation.example.component.ui.layout.lazycolumn.LazyColumnIssueUI
import com.example.composesample.presentation.example.component.ui.layout.lazycolumn.ReverseLazyColumnExampleUI
import com.example.composesample.presentation.example.component.ui.layout.pager.PullScreenPagerUI
import com.example.composesample.presentation.example.component.ui.media.lottie.LottieExampleUI
import com.example.composesample.presentation.example.component.ui.media.shimmer.ShimmerExampleUI
import com.example.composesample.presentation.example.component.ui.media.shimmer.TextShimmerExampleUI
import com.example.composesample.presentation.example.component.ui.scroll.NestedScrollingExampleUI
import com.example.composesample.presentation.example.component.architecture.state.SnapshotFlowExampleUI
import com.example.composesample.presentation.example.component.system.ui.widget.GlanceWidgetExampleUI
import com.example.composesample.presentation.example.component.ui.graphics.NewShadowApiExampleUI
import com.example.composesample.presentation.example.component.ui.navigation.NestedRoutesNav3ExampleUI
import com.example.composesample.presentation.example.component.ui.notification.SnapNotifyExampleUI
import com.example.composesample.presentation.example.component.ui.shapes.CardCornersExampleUI
import com.example.composesample.presentation.example.component.architecture.lifecycle.AutoCloseableExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.StaticDynamicCompositionLocalExampleUI
import com.example.composesample.presentation.example.component.architecture.development.performance.InlineValueClassExampleUI
import com.example.composesample.presentation.example.component.architecture.development.language.SealedClassInterfaceExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.CoroutinesInternalsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.WithContextExampleUI
import com.example.composesample.presentation.example.component.architecture.development.flow.FlatMapExampleUI
import com.example.composesample.presentation.example.component.architecture.navigation.Navigation3ExampleUI
import com.example.composesample.presentation.example.component.architecture.modularization.ModularizationExampleUI
import com.example.composesample.presentation.example.component.ui.text.AutoSizingTextExampleUI
import com.example.composesample.presentation.example.component.ui.text.TextStyleUI
import com.example.composesample.presentation.getTextStyle
import com.example.composesample.presentation.openWebPage
import com.example.composesample.util.ConstValue.Companion.AnimationExample
import com.example.composesample.util.ConstValue.Companion.ApiDisconnectExample
import com.example.composesample.util.ConstValue.Companion.AutoSizingTextExample
import com.example.composesample.util.ConstValue.Companion.AudioRecorderExample
import com.example.composesample.util.ConstValue.Companion.BottomNavigationExample
import com.example.composesample.util.ConstValue.Companion.BottomSheetExample
import com.example.composesample.util.ConstValue.Companion.ClickEventExample
import com.example.composesample.util.ConstValue.Companion.CompositionLocalExample
import com.example.composesample.util.ConstValue.Companion.CoordinatorExample
import com.example.composesample.util.ConstValue.Companion.CoroutineExample
import com.example.composesample.util.ConstValue.Companion.CursorIDEExample
import com.example.composesample.util.ConstValue.Companion.CustomBottomSheetExample
import com.example.composesample.util.ConstValue.Companion.DataCacheExample
import com.example.composesample.util.ConstValue.Companion.DragAndDropExample
import com.example.composesample.util.ConstValue.Companion.ExampleType
import com.example.composesample.util.ConstValue.Companion.FlexBoxLayoutExample
import com.example.composesample.util.ConstValue.Companion.FlingBehaviorExample
import com.example.composesample.util.ConstValue.Companion.FocusRestorerExample
import com.example.composesample.util.ConstValue.Companion.GlanceWidgetExample
import com.example.composesample.util.ConstValue.Companion.GraphicsLayerExample
import com.example.composesample.util.ConstValue.Companion.InitTestExample
import com.example.composesample.util.ConstValue.Companion.IntentType
import com.example.composesample.util.ConstValue.Companion.KtorExample
import com.example.composesample.util.ConstValue.Companion.LanguageSettingExample
import com.example.composesample.util.ConstValue.Companion.LazyColumnExample
import com.example.composesample.util.ConstValue.Companion.LocalLanguageChangeExample
import com.example.composesample.util.ConstValue.Companion.LookaheadScopeExample
import com.example.composesample.util.ConstValue.Companion.LottieExample
import com.example.composesample.util.ConstValue.Companion.MVIExample
import com.example.composesample.util.ConstValue.Companion.ModalBottomSheetExample
import com.example.composesample.util.ConstValue.Companion.ModalDrawExample
import com.example.composesample.util.ConstValue.Companion.NestedRoutesNav3Example
import com.example.composesample.util.ConstValue.Companion.NestedScrollingExample
import com.example.composesample.util.ConstValue.Companion.NewShadowApiExample
import com.example.composesample.util.ConstValue.Companion.SnapNotifyExample
import com.example.composesample.util.ConstValue.Companion.CardCornersExample
import com.example.composesample.util.ConstValue.Companion.AutoCloseableExample
import com.example.composesample.util.ConstValue.Companion.StaticDynamicCompositionLocalExample
import com.example.composesample.util.ConstValue.Companion.InlineValueClassExample
import com.example.composesample.util.ConstValue.Companion.SealedClassInterfaceExample
import com.example.composesample.util.ConstValue.Companion.CoroutinesInternalsExample
import com.example.composesample.util.ConstValue.Companion.FlatMapExample
import com.example.composesample.util.ConstValue.Companion.Navigation3Example
import com.example.composesample.util.ConstValue.Companion.ModularizationExample
import com.example.composesample.util.ConstValue.Companion.WithContextExample
import com.example.composesample.util.ConstValue.Companion.PagingExample
import com.example.composesample.util.ConstValue.Companion.PassingIntentDataExample
import com.example.composesample.util.ConstValue.Companion.PathGraphicsExample
import com.example.composesample.util.ConstValue.Companion.PowerSaveModeExample
import com.example.composesample.util.ConstValue.Companion.PullScreenPager
import com.example.composesample.util.ConstValue.Companion.PullToRefreshExample
import com.example.composesample.util.ConstValue.Companion.ReverseLazyColumnExample
import com.example.composesample.util.ConstValue.Companion.SSEExample
import com.example.composesample.util.ConstValue.Companion.SafFileExample
import com.example.composesample.util.ConstValue.Companion.ScaffoldDrawExample
import com.example.composesample.util.ConstValue.Companion.ShortCutKey
import com.example.composesample.util.ConstValue.Companion.ShortCutTypeDynamic
import com.example.composesample.util.ConstValue.Companion.ShortCutTypePin
import com.example.composesample.util.ConstValue.Companion.ShortCutTypeXML
import com.example.composesample.util.ConstValue.Companion.ShortcutExample
import com.example.composesample.util.ConstValue.Companion.SideEffectExample
import com.example.composesample.util.ConstValue.Companion.SnapshotFlowExample
import com.example.composesample.util.ConstValue.Companion.StickyHeaderExample
import com.example.composesample.util.ConstValue.Companion.SwipeToDismissExample
import com.example.composesample.util.ConstValue.Companion.TargetSDK34PermissionExample
import com.example.composesample.util.ConstValue.Companion.TestExample
import com.example.composesample.util.ConstValue.Companion.TextOverflowExample
import com.example.composesample.util.ConstValue.Companion.TextShimmerExample
import com.example.composesample.util.ConstValue.Companion.TextStyleExample
import com.example.composesample.util.ConstValue.Companion.TypeExample
import com.example.composesample.util.ConstValue.Companion.UIShimmerExample
import com.example.composesample.util.ConstValue.Companion.WebViewIssueExample
import com.example.composesample.util.ConstValue.Companion.WorkManagerExample
import com.example.composesample.util.component.Toast
import com.example.composesample.util.noRippleClickable
import com.example.domain.model.ExampleMoveType
import com.example.domain.model.ExampleObject
import org.koin.androidx.compose.koinViewModel

@ExperimentalAnimationApi
class BlogExampleActivity : ComponentActivity() {
    @SuppressLint("ContextCastToActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current as BlogExampleActivity
            val type = intent.getStringExtra(IntentType) ?: ExampleType
            val blogExampleViewModel: BlogExampleViewModel = koinViewModel()

            blogExampleViewModel.initExampleObject()

            val enterShortcutCase = when {
                context.intent.getStringExtra(ShortCutKey) == ShortCutTypeXML -> 1
                context.intent.getStringExtra(ShortCutKey) == ShortCutTypeDynamic -> 2
                context.intent.getStringExtra(ShortCutKey) == ShortCutTypePin -> 3
                else -> 0
            }

            blogExampleViewModel.setStudyType(
                when (type) {
                    ExampleType -> ExampleMoveType.UI
                    else -> ExampleMoveType.EMPTY
                }
            )

            // targetSDK 35 example case
//            Scaffold(
//                containerColor = Color.LightGray
//            ) { paddingValues ->
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues)
//                        .background(color = Color.White)
//                ) {
//                    BlogExampleScreen(
//                        launcher = launcher,
//                        blogExampleViewModel = blogExampleViewModel
//                    )
//
//                    Toast(stream = blogExampleViewModel.toast)
//                }
//            }


            LaunchedEffect(enterShortcutCase) {
                when (enterShortcutCase) {
                    1 -> {
                        blogExampleViewModel.sendToastMessage("Enter Shortcut case : XML")
                    }

                    2 -> {
                        blogExampleViewModel.sendToastMessage("Enter Shortcut case : Dynamic")
                    }

                    3 -> {
                        blogExampleViewModel.sendToastMessage("Enter Shortcut case : Pin")
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Yellow)
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .background(color = Color.White)
            ) {
                BlogExampleScreen(
                    blogExampleViewModel = blogExampleViewModel
                )

                Toast(stream = blogExampleViewModel.toast)
            }
        }
    }
}

@Composable
fun BlogExampleScreen(
    blogExampleViewModel: BlogExampleViewModel
) {
    val context = LocalContext.current
    val exampleType = remember { mutableStateOf("") }
    val exampleMoveType = remember { mutableStateOf(ExampleMoveType.UI) }
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
                        .then(
                            if (exampleBlogUrl.isEmpty()) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.weight(1f)
                            }
                        )
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

                if (exampleBlogUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(20.dp))

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
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExampleCaseUI(
    exampleType: MutableState<String>,
    exampleMoveType: MutableState<ExampleMoveType>,
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

                        CompositionLocalExample -> {
                            CompositionLocalExampleUI(onBackEvent)
                        }

                        InitTestExample -> {
                            InitTestExampleUI(onBackEvent)
                        }

                        ShortcutExample -> {
                            ShortcutExampleUI(onBackEvent)
                        }

                        PagingExample -> {
                            PagingExampleUI(onBackEvent)
                        }

                        TypeExample -> {
                            TypeExampleUI(onBackEvent)
                        }

                        SafFileExample -> {
                            SafFileSelectionUI(onBackButtonClick = onBackEvent)
                        }

                        LanguageSettingExample -> {
                            LanguageSettingExampleUI(onBackEvent)
                        }

                        LocalLanguageChangeExample -> {
                            LocalLanguageChangeExampleUI(onBackEvent)
                        }

                        TextOverflowExample -> {
                            TextOverflowExampleUI(onBackEvent)
                        }

                        GraphicsLayerExample -> {
                            GraphicsLayerExampleUI(onBackEvent)
                        }

                        LookaheadScopeExample -> {
                            LookaheadScopeExampleUI(onBackEvent)
                        }

                        FocusRestorerExample -> {
                            FocusRestorerExampleUI(onBackEvent)
                        }

                        PathGraphicsExample -> {
                            PathGraphicsExampleUI(onBackEvent)
                        }

                        NestedScrollingExample -> {
                            NestedScrollingExampleUI(onBackEvent)
                        }

                        SnapshotFlowExample -> {
                            SnapshotFlowExampleUI(onBackEvent)
                        }

                        GlanceWidgetExample -> {
                            GlanceWidgetExampleUI(onBackEvent)
                        }

                        AutoSizingTextExample -> {
                            AutoSizingTextExampleUI(onBackEvent)
                        }

                        NestedRoutesNav3Example -> {
                            NestedRoutesNav3ExampleUI(onBackEvent)
                        }

                        NewShadowApiExample -> {
                            NewShadowApiExampleUI(onBackEvent)
                        }

                        SnapNotifyExample -> {
                            SnapNotifyExampleUI(onBackEvent)
                        }

                        CardCornersExample -> {
                            CardCornersExampleUI(onBackEvent)
                        }

                        AutoCloseableExample -> {
                            AutoCloseableExampleUI(onBackEvent)
                        }

                        StaticDynamicCompositionLocalExample -> {
                            StaticDynamicCompositionLocalExampleUI(onBackEvent)
                        }

                        InlineValueClassExample -> {
                            InlineValueClassExampleUI(onBackEvent)
                        }

                        SealedClassInterfaceExample -> {
                            SealedClassInterfaceExampleUI(onBackEvent)
                        }

                        CoroutinesInternalsExample -> {
                            CoroutinesInternalsExampleUI(onBackEvent)
                        }

                        FlatMapExample -> {
                            FlatMapExampleUI(onBackEvent)
                        }

                        Navigation3Example -> {
                            Navigation3ExampleUI(onBackEvent)
                        }

                        ModularizationExample -> {
                            ModularizationExampleUI(onBackEvent)
                        }

                        WithContextExample -> {
                            WithContextExampleUI(onBackEvent)
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