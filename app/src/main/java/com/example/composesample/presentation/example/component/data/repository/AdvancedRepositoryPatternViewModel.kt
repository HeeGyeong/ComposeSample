package com.example.composesample.presentation.example.component.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ArticleResult
import com.example.domain.repository.ArticleRepository
import com.example.domain.useCase.GetArticleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RepositoryPatternUiState(
    val isLoading: Boolean = false,
    val lastResult: ArticleResult? = null,
    val logs: List<String> = emptyList()
)

class AdvancedRepositoryPatternViewModel(
    private val getArticleUseCase: GetArticleUseCase,
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepositoryPatternUiState())
    val uiState: StateFlow<RepositoryPatternUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun fetchArticle(id: String, forceRefresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getArticleUseCase(id, forceRefresh)
            val logLine = "[${timeFormat.format(Date())}] id=$id → ${result.source} (${result.latencyMs}ms)" +
                if (forceRefresh) " [강제 새로고침]" else ""
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    lastResult = result,
                    logs = state.logs + logLine
                )
            }
        }
    }

    fun invalidateMemory() {
        articleRepository.invalidateMemory()
        appendSystemLog("메모리 캐시 무효화")
    }

    fun invalidateDisk() {
        articleRepository.invalidateDisk()
        appendSystemLog("디스크 캐시 무효화")
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    private fun appendSystemLog(message: String) {
        val logLine = "[${timeFormat.format(Date())}] $message"
        _uiState.update { it.copy(logs = it.logs + logLine) }
    }
}
