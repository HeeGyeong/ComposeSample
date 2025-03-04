package com.example.composesample.presentation.example.component.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class PagingViewModel : ViewModel() {
    val pagingDataFlow: Flow<PagingData<String>> = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { PagingDataSourceExample() }
    ).flow.cachedIn(viewModelScope)
}