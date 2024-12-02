package com.example.composesample.presentation.example

import android.app.Application
import com.example.core.BaseViewModel
import com.example.core.navigation.Navigation
import com.example.domain.model.ExampleObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update


/**
 * Example 추가 시 Data Update 필요
 */
class BlogExampleViewModel(
    private val navigation: Navigation,
    application: Application
) : BaseViewModel(navigation, application) {
    // toast는 그대로 유지
    val toast = MutableStateFlow("")
    fun sendToastMessage(message: String) {
        toast.update { message }
    }

    private val _exampleObjectList = MutableStateFlow<List<ExampleObject>>(emptyList())
    val exampleObjectList: StateFlow<List<ExampleObject>> = _exampleObjectList.asStateFlow()

    private val _subCategoryList = MutableStateFlow<List<ExampleObject>>(emptyList())
    val subCategoryList: StateFlow<List<ExampleObject>> = _subCategoryList.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _previewExampleData = MutableStateFlow("")
    val previewExampleData: StateFlow<String> = _previewExampleData.asStateFlow()

    // Search results
    val searchExampleList = searchText.combine(exampleObjectList) { query, list ->
        when {
            query.isBlank() -> list
            else -> list.filter { it.title.contains(query, ignoreCase = true) }
        }
    }

    // UI Events
    fun initExampleObject() {
        _exampleObjectList.update { exampleObjectList() }
    }

    fun setSubCategoryList(
        filter: String,
    ) {
        _subCategoryList.update {
            subCategoryList()
                .filter {
                    it.subCategory == filter
                }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun reverseExampleList() {
        _exampleObjectList.update { it.reversed() }
    }

    fun setPreviewExampleData(message: String) {
        _previewExampleData.update { message }
    }
}