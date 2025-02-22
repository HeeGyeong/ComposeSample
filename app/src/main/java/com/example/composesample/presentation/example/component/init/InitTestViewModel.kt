package com.example.composesample.presentation.example.component.init

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InitTestViewModel : ViewModel() {
    private val _isLaunchedEffectLoading = MutableStateFlow(false)
    val isLaunchedEffectLoading = _isLaunchedEffectLoading.asStateFlow()

    private val _isViewModelInitLoading = MutableStateFlow(false)
    val isViewModelInitLoading = _isViewModelInitLoading.asStateFlow()

    fun changeLaunchedEffectLoading() {
        _isLaunchedEffectLoading.value = _isLaunchedEffectLoading.value.not()
    }

    private fun changeViewModelInitLoading() {
        println("viewModel flag before: ${isViewModelInitLoading.value}")
        _isViewModelInitLoading.value = _isViewModelInitLoading.value.not()
        println("viewModel flag after: ${isViewModelInitLoading.value}")
    }

    init {
        viewModelScope.launch {
            changeViewModelInitLoading()

            delay(3000L)

            changeViewModelInitLoading()
        }
    }
}