package com.example.composesample.presentation.example.component.data.paging

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PagingExampleUI(
    onBackButtonClick: () -> Unit,
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val pagingViewModel = koinViewModel<PagingViewModel>(owner = viewModelStoreOwner)
    val pagingItems = pagingViewModel.pagingDataFlow.collectAsLazyPagingItems()
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            pagingItems.refresh()
            refreshing = false
        }
    )

    LaunchedEffect(key1 = pagingItems.itemCount) {
        Log.d("PagingLog", "itemCount: ${pagingItems.itemCount}")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            stickyHeader {
                MainHeader(
                    title = "Paging Example",
                    onBackIconClicked = onBackButtonClick
                )
            }

            items(
                count = pagingItems.itemCount,
                key = { index -> pagingItems[index]?.hashCode() ?: index }
            ) { index ->
                pagingItems[index]?.let { item ->
                    Text(text = "Item: $item", fontSize = 40.sp)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            when (pagingItems.loadState.append) {
                is LoadState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is LoadState.Error -> {
                    item {
                        Text(
                            text = "에러 발생",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { pagingItems.retry() }
                        )
                    }
                }

                else -> Unit
            }
        }

        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}