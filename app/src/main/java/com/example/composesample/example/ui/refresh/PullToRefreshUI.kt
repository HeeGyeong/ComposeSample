package com.example.composesample.example.ui.refresh

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PullToRefreshUI(
    onBackButtonClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val refreshViewModel = viewModel<RefreshViewModel>()
    val refreshState by refreshViewModel.refreshState
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshState.isLoading,
        onRefresh = refreshViewModel::addRefreshItem
    )

    val pullRefreshStateSame = rememberPullRefreshState(
        refreshing = refreshState.isLoading,
        onRefresh = {
            coroutineScope.launch {
                refreshViewModel.changeRefreshState(true)
                delay(1000L)
                refreshViewModel.changeRefreshState(false)
                refreshViewModel.updateItem()
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
//            .pullRefresh(pullRefreshStateSame)
            .background(color = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .background(color = Color.DarkGray)
        ) {
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

            items(refreshState.refreshItemList) {
                Column {
                    Text(text = it.index)
                    Text(text = it.randomIntData.toString())
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        PullRefreshIndicator(
            refreshing = refreshViewModel.refreshState.value.isLoading,
            state = pullRefreshState,
//            state = pullRefreshStateSame,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.LightGray,
            contentColor = if (refreshViewModel.refreshState.value.isLoading) Color.Red else Color.Blue,
        )
    }
}