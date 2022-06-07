package com.example.composesample.scope.sub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LaunchedEffectViewModel(application: Application) : AndroidViewModel(application) {

    private val _isGo = MutableLiveData(false)
    val isGo: LiveData<Boolean> = _isGo

    fun onChangeText(changeText: String) {
        _isGo.value = changeText == "go"
    }
}