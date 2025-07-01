package com.example.composesample.presentation.example.component.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay

class PagingDataSource : PagingSource<Int, String>() {
    // 실제 데이터 로드 로직
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        val page = params.key ?: 0 // 현재 페이지 번호
        return try {
            if (page != 0) {
                delay(2000) // 데이터 로딩 UI를 표시하기 위해 임의로 추가된 딜레이
            }

            val data = List(20) { "Item ${page * 20 + it}" } // 더미 데이터
            LoadResult.Page(
                data = data, // 실제 로드된 데이터 리스트
                prevKey = if (page == 0) null else page - 1, // 이전 페이지 번호
                nextKey = if (data.isEmpty()) null else page + 1 // 다음 페이지 번호
            ) // 해당 결과로 인해 자동으로 데이터가 append 된다.
        } catch (e: Exception) {
            // 예외 발생 시 에러 반환
            LoadResult.Error(e)
        }
    }

    // override 필수
    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
