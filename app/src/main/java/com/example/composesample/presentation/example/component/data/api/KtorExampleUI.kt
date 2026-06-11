package com.example.composesample.presentation.example.component.data.api

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KtorExampleUI(
    onBackButtonClick: () -> Unit,
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val apiExampleViewModel = koinViewModel<ApiExampleViewModel>(owner = viewModelStoreOwner)
    val coroutineScope = rememberCoroutineScope()
    val ktorPosts by apiExampleViewModel.ktorPosts.collectAsStateWithLifecycle()
    val isLoading by apiExampleViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by apiExampleViewModel.errorMessage.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = {
            coroutineScope.launch {
                apiExampleViewModel.fetchPostsWithKtor()
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isLoading,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.LightGray,
                color = if (isLoading) Color.Red else Color.Blue,
            )
        }
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
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                            }
                        }

                        Text(
                            text = "Ktor API Example - Start Pull To Refresh",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.titleLarge
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
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}