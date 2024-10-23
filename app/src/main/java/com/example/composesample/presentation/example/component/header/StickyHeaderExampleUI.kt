package com.example.composesample.presentation.example.component.header


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.composesample.presentation.example.component.preview.ViewModelPreview4


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StickyHeaderExampleUI(
    onBackButtonClick: () -> Unit
) {
    val listState = rememberLazyListState()
    // scrollBehavior를 사용할 때 scroll 관련 api에 따라서 topBar가 다르게 동작한다.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
//    TopAppBarDefaults.pinnedScrollBehavior()
//    TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // LargeTopBar를 사용하기 때문에 해당 변수는 사용하지 않아도 된다.
    // 하지만, Header가 변경되는 타이밍에 따라 처리해야하는 것이 있다면 해당 변수를 사용한다.
//    val isCollapsed: Boolean by remember {
//        derivedStateOf {
//            scrollBehavior.state.collapsedFraction == 1f
//        }
//    }

    val overlapHeightPx = with(LocalDensity.current) {
        EXPANDED_TOP_BAR_HEIGHT.toPx() - COLLAPSED_TOP_BAR_HEIGHT.toPx()
    }
    val isCollapsed: Boolean by remember {
        derivedStateOf {
            val isFirstItemHidden =
                listState.firstVisibleItemScrollOffset > overlapHeightPx
            isFirstItemHidden || listState.firstVisibleItemIndex > 0
        }
    }

    Scaffold(
        // LargeTopBar 사용할때만 nestedScroll 설정. LargeTopBar는 scroll을 탐지하여 영역을 변경함.
        // LargeTopBar가 없을 때 nestedScroll 사용 시, 스크롤이 안된다.
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CollapsedHeaderUI(
                modifier = Modifier.zIndex(2f),
                isCollapsed = isCollapsed,
                onBackButtonClick = {
                    onBackButtonClick.invoke()
                }
            )

            // 가장 기본적인 TopBar
//            TopAppBar(title = { Text("내 앱") }, scrollBehavior = scrollBehavior)

            // 크기가 동적으로 변경되는 TopBar
//            LargeTopBar(
//                scrollBehavior = scrollBehavior,
//                onBackButtonClick = {
//                    onBackButtonClick.invoke()
//                }
//            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)
                .padding(padding)
        ) {
            stickyHeader {
                ExpandedHeaderUI(
                    onBackButtonClick = {
                        onBackButtonClick.invoke()
                    }
                )
            }

            item {
                ExpandedHeaderUI(
                    onBackButtonClick = {
                        onBackButtonClick.invoke()
                    }
                )
            }

            item {
                ViewModelPreview4()
            }

            item {
                repeat(30) { index ->
                    Text(text = "Text View : $index", fontSize = 30.sp)
                }
            }
        }
    }
}

@Composable
fun StickyHeaderComponent(
    onBackButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
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


val COLLAPSED_TOP_BAR_HEIGHT = 56.dp
val EXPANDED_TOP_BAR_HEIGHT = 100.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LargeTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBackButtonClick: () -> Unit,
) = LargeTopAppBar(
    title = {
        StickyHeaderComponent(
            onBackButtonClick = onBackButtonClick
        )
    },
    colors = TopAppBarDefaults.mediumTopAppBarColors(
        containerColor = Color.DarkGray, // Expanded
        scrolledContainerColor = Color.DarkGray, // Collapsed
        titleContentColor = Color.LightGray
    ),
    scrollBehavior = scrollBehavior,
)

@Composable
private fun CollapsedHeaderUI(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean,
    onBackButtonClick: () -> Unit,
) {
    val color: Color by animateColorAsState(
        if (isCollapsed) {
            Color.Green
        } else {
            Color.DarkGray
        }, label = "collapsed color"
    )

    Box(
        modifier = modifier
            .background(color)
            .fillMaxWidth()
            .height(COLLAPSED_TOP_BAR_HEIGHT),
        contentAlignment = Alignment.BottomStart
    ) {
        AnimatedVisibility(
            visible = isCollapsed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            StickyHeaderComponent(
                onBackButtonClick = onBackButtonClick
            )
        }
    }
}

@Composable
private fun ExpandedHeaderUI(
    onBackButtonClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(color = Color.DarkGray)
            .fillMaxWidth()
            .height(EXPANDED_TOP_BAR_HEIGHT),
        contentAlignment = Alignment.BottomStart
    ) {
        StickyHeaderComponent(
            onBackButtonClick = onBackButtonClick
        )
    }
}