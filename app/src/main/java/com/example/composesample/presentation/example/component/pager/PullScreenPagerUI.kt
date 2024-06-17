package com.example.composesample.presentation.example.component.pager

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PullScreenPagerUI(
    onBackButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                IconButton(
                    onClick = {
                        onBackButtonClick.invoke()
                    }
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "")
                }
            }

            HorizontalTextPager()
//            DifferentScreenPager()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalTextPager() {
    val textList = listOf(
        "index : 1",
        "index : 2",
        "index : 3",
        "index : 4",
        "index : 5",
    )

    val pagerState = rememberPagerState(pageCount = {
        textList.size
    })
    var scrollLastPage by remember { mutableStateOf(false) }
    var scrollFirstPage by remember { mutableStateOf(false) }

    // index : last -> first
    LaunchedEffect(key1 = pagerState.isScrollInProgress) {
        Log.d(
            "isScrollInProgress",
            "pagerState.currentPageOffsetFraction : ${pagerState.currentPageOffsetFraction}"
        )
        if (pagerState.currentPage == textList.size - 1
            && pagerState.isScrollInProgress
            && pagerState.currentPageOffsetFraction == 0f
        ) {
            scrollLastPage = true
        }

        if (scrollLastPage) {
            pagerState.animateScrollToPage(0)
            scrollLastPage = false
        }
    }

    // index : first -> last
    LaunchedEffect(key1 = pagerState.isScrollInProgress) {
        if (pagerState.currentPage == 0
            && pagerState.isScrollInProgress
            && pagerState.currentPageOffsetFraction == 0f
        ) {
            scrollFirstPage = true
        }

        if (scrollFirstPage) {
            pagerState.animateScrollToPage(textList.size - 1)
            scrollFirstPage = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState
        ) {
            TextPager(text = textList[pagerState.currentPage])
        }

        PagerDotUI(
            itemSize = textList.size,
            pagerState = pagerState
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DifferentScreenPager() {
    val exampleScreenList = listOf(
        DisplayItem { FirstScreen() },
        DisplayItem { SecondScreen() },
        DisplayItem { ThirdScreen() },
        DisplayItem { LastScreen() }
    )


    val pagerState = rememberPagerState(pageCount = {
        exampleScreenList.size
    })

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState
        ) {
            exampleScreenList[pagerState.currentPage].display()
        }

        PagerDotUI(
            itemSize = exampleScreenList.size,
            pagerState = pagerState
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxScope.PagerDotUI(
    itemSize: Int,
    pagerState: PagerState,
) {
    Row(
        Modifier
            .height(48.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(itemSize) { index ->
            val color =
                if (pagerState.currentPage == index) Color.DarkGray else Color.LightGray
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(10.dp)
            )
        }
    }
}