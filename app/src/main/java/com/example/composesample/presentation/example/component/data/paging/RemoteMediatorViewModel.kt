package com.example.composesample.presentation.example.component.data.paging

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.room.withTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoteMediatorViewModel(application: Application) : ViewModel() {

    private val db = ArticleDatabase.getInstance(application)
    private val api = FakeArticleApi()

    private val _eventLog = MutableStateFlow<List<String>>(emptyList())
    val eventLog: StateFlow<List<String>> = _eventLog.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // DB 에 실제로 캐시된 행 수 — 화면이 보고 있는 것이 네트워크가 아니라 DB 임을 드러낸다
    val cachedCount: StateFlow<Int> = db.articleDao().countFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // 값이 바뀔 때마다 Pager 를 새로 만든다 → RemoteMediator.initialize() 가 다시 호출된다
    private val pagerEpoch = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<ArticleEntity>> = pagerEpoch
        .flatMapLatest { createPagerFlow() }
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalPagingApi::class)
    private fun createPagerFlow(): Flow<PagingData<ArticleEntity>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            // 기본값(pageSize * 3)을 쓰면 첫 로드만 3페이지를 요청해 네트워크 페이지와
            // DB 페이지의 대응이 흐려진다. 시연에서는 1:1 로 맞춰야 로그가 읽힌다.
            initialLoadSize = PAGE_SIZE,
            enablePlaceholders = false
        ),
        remoteMediator = ArticleRemoteMediator(db = db, api = api, log = ::appendLog),
        // PagingSource 는 DB 만 읽는다. 네트워크는 RemoteMediator 쪽에만 있다.
        pagingSourceFactory = { db.articleDao().pagingSource() }
    ).flow

    fun toggleOffline() {
        val next = !_isOffline.value
        _isOffline.value = next
        api.isOffline = next
        appendLog(if (next) "── 네트워크 OFF ──" else "── 네트워크 ON ──")
    }

    /** 화면 재진입과 같은 효과 — Pager 를 새로 만들어 initialize() 분기를 다시 관찰한다 */
    fun recreatePager() {
        appendLog("── Pager 재생성 (initialize() 재호출) ──")
        pagerEpoch.update { it + 1 }
    }

    fun clearCache() {
        viewModelScope.launch {
            db.withTransaction {
                db.articleDao().clearAll()
                db.remoteKeyDao().clearAll()
            }
            appendLog("캐시(DB) 전체 삭제 — 다음 initialize() 는 LAUNCH_INITIAL_REFRESH")
        }
    }

    fun clearLog() {
        _eventLog.value = emptyList()
    }

    private fun appendLog(message: String) {
        _eventLog.update { current -> (current + message).takeLast(MAX_LOG) }
    }

    companion object {
        const val PAGE_SIZE = 10
        private const val MAX_LOG = 40
    }
}
