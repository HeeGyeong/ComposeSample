package com.example.composesample.presentation.example.component.type

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TypeExampleViewModel : ViewModel() {
    // 첫 번째 열 데이터
    private val _a1 = MutableStateFlow("A1 데이터")
    val a1: StateFlow<String> = _a1.asStateFlow()

    private val _a2 = MutableStateFlow("A2 데이터")
    val a2: StateFlow<String> = _a2.asStateFlow()

    // 두 번째 열 데이터
    private val _b1 = MutableStateFlow("B1 데이터")
    val b1: StateFlow<String> = _b1.asStateFlow()

    private val _b2 = MutableStateFlow("B2 데이터")
    val b2: StateFlow<String> = _b2.asStateFlow()

    // 세 번째 열 데이터
    private val _c1 = MutableStateFlow("C1 데이터")
    val c1: StateFlow<String> = _c1.asStateFlow()

    private val _c2 = MutableStateFlow("C2 데이터")
    val c2: StateFlow<String> = _c2.asStateFlow()

    // 네 번째 열 데이터
    private val _d1 = MutableStateFlow("D1 데이터")
    val d1: StateFlow<String> = _d1.asStateFlow()

    private val _d2 = MutableStateFlow("D2 데이터")
    val d2: StateFlow<String> = _d2.asStateFlow()

    // 다섯 번째 열 데이터
    private val _e1 = MutableStateFlow("E1 데이터")
    val e1: StateFlow<String> = _e1.asStateFlow()

    private val _e2 = MutableStateFlow("E2 데이터")
    val e2: StateFlow<String> = _e2.asStateFlow()

    // 데이터 업데이트 함수 (필요시 사용)
    fun updateA1(newValue: String) {
        _a1.value = newValue
    }

    fun updateA2(newValue: String) {
        _a2.value = newValue
    }

    fun updateB1(newValue: String) {
        _b1.value = newValue
    }

    fun updateB2(newValue: String) {
        _b2.value = newValue
    }

    fun updateC1(newValue: String) {
        _c1.value = newValue
    }

    fun updateC2(newValue: String) {
        _c2.value = newValue
    }

    fun updateD1(newValue: String) {
        _d1.value = newValue
    }

    fun updateD2(newValue: String) {
        _d2.value = newValue
    }

    fun updateE1(newValue: String) {
        _e1.value = newValue
    }

    fun updateE2(newValue: String) {
        _e2.value = newValue
    }
} 