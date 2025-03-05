package com.example.composesample.presentation.example.component.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class PagingViewModel : ViewModel() {
    private val config = PagingConfig(
        pageSize = 20,              // 한 페이지당 로드할 아이템 수
        prefetchDistance = 17,       // 미리 로드할 임계값 (기본값: pageSize)
        enablePlaceholders = false,  // 로드되지 않은 아이템을 placeholder로 표시할지
        initialLoadSize = 20,       // 첫 로드시 가져올 아이템 수 (기본값: pageSize * 3)
    )

    val pagingDataFlow: Flow<PagingData<String>> = Pager(
        config = config,
        pagingSourceFactory = { PagingDataSourceExample() } // 데이터 로드 처리
    )
        .flow // Pager를 Flow<PagingData>로 변환
        .cachedIn(viewModelScope) // 페이징 데이터를 ViewModel 스코프에서 캐싱
}