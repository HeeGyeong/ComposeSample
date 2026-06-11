package com.example.composesample.presentation.example.component.interaction.refresh

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshUI(
    onBackButtonClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val refreshViewModel = viewModel<RefreshViewModel>()
    val refreshState by refreshViewModel.refreshState
    val pullToRefreshState = rememberPullToRefreshState()

    // M3 에서는 onRefresh 를 상태가 아닌 PullToRefreshBox 에 직접 전달한다.
    // 아래 람다는 코루틴으로 직접 로딩 상태를 제어하는 대체 onRefresh 예시.
    val onRefreshSame: () -> Unit = {
        coroutineScope.launch {
            refreshViewModel.changeRefreshState(true)
            delay(1000L)
            refreshViewModel.changeRefreshState(false)
            refreshViewModel.updateItem()
        }
    }

    PullToRefreshBox(
        isRefreshing = refreshState.isLoading,
        onRefresh = refreshViewModel::addRefreshItem,
//        onRefresh = onRefreshSame,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = refreshState.isLoading,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.LightGray,
                color = if (refreshState.isLoading) Color.Red else Color.Blue,
            )
        }
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
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
    }
}