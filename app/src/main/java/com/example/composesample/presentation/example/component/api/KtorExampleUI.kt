package com.example.composesample.presentation.example.component.api

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun KtorExampleUI(
    onBackButtonClick: () -> Unit,
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val apiExampleViewModel = koinViewModel<ApiExampleViewModel>(owner = viewModelStoreOwner)
    val coroutineScope = rememberCoroutineScope()
    val ktorPosts by apiExampleViewModel.ktorPosts.observeAsState(initial = emptyList())
    val isLoading by apiExampleViewModel.isLoading.observeAsState(initial = false)
    val errorMessage by apiExampleViewModel.errorMessage.observeAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            coroutineScope.launch {
                apiExampleViewModel.fetchPostsWithKtor()
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
            .background(color = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .background(color = Color.White)
                        .padding(top = 10.dp, bottom = 10.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            IconButton(
                                onClick = onBackButtonClick
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "")
                            }
                        }

                        Text(
                            text = "Ktor API Example - Start Pull To Refresh",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.h6
                        )

                        errorMessage?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            itemsIndexed(ktorPosts) { index, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "[$index] : ${item.title}",
                        style = MaterialTheme.typography.body1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.LightGray,
            contentColor = if (isLoading) Color.Red else Color.Blue,
        )
    }
} 