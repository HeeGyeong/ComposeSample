package com.example.composesample.presentation.example.component.compositionLocal

import androidx.lifecycle.ViewModel
import com.example.domain.useCase.FetchDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CompositionLocalViewModel : ViewModel() {

    private val _compositionLocalTextData = MutableStateFlow("")
    val compositionLocalTextData: StateFlow<String> = _compositionLocalTextData.asStateFlow()

    init {
        _compositionLocalTextData.value = "CompositionLocalViewModel"
    }

    fun changeTextData(text: String) {
        _compositionLocalTextData.value = text
    }
}