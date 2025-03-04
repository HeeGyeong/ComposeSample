package com.example.composesample.presentation.example.component.paging

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagingExampleUI(
    onBackButtonClick: () -> Unit,
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val pagingViewModel = koinViewModel<PagingViewModel>(owner = viewModelStoreOwner)
    val pagingItems = pagingViewModel.pagingDataFlow.collectAsLazyPagingItems()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
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
                    Text(text = "Item: $item")
                }
            }
        }
    }
}