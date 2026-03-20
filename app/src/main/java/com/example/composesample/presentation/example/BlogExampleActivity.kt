package com.example.composesample.presentation.example

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import com.example.composesample.presentation.getTextStyle
import com.example.composesample.presentation.openWebPage
import com.example.composesample.util.ConstValue.ExampleType
import com.example.composesample.util.ConstValue.IntentType
import com.example.composesample.util.ConstValue.ShortCutKey
import com.example.composesample.util.ConstValue.ShortCutTypeDynamic
import com.example.composesample.util.ConstValue.ShortCutTypePin
import com.example.composesample.util.ConstValue.ShortCutTypeXML
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
