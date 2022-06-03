package com.example.composesample.cal

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalViewModel: ViewModel() {

    private val _counter = MutableStateFlow(0)
    val counter = _counter.asStateFlow()


    fun addCounter() {
        _counter.value += 1
    }

    fun minusCounter() {
        _counter.value -= 1
    }

    fun multiCounter() {
        _counter.value *= 2
    }

    fun divCounter() {
        _counter.value /= 2
    }
}