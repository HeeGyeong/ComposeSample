package com.example.composesample.presentation.example.component.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

class PagingDataSourceExample : PagingSource<Int, String>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        val page = params.key ?: 0
        return try {
            val data = List(20) { "Item ${page * 20 + it}" }
            LoadResult.Page(
                data = data,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (data.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // override 필수
    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition
    }
}
