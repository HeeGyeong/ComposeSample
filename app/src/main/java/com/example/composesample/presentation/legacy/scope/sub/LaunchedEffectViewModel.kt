package com.example.composesample.presentation.legacy.scope.sub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LaunchedEffectViewModel(application: Application) : AndroidViewModel(application) {

    private val _isGo = MutableStateFlow(false)
    val isGo: StateFlow<Boolean> = _isGo.asStateFlow()

    fun onChangeText(changeText: String) {
        _isGo.value = changeText == "go"
    }
}
