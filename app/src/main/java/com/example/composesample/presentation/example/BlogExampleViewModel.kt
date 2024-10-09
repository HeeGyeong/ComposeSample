package com.example.composesample.presentation.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update


/**
 * Example 추가 시 Data Update 필요
 */
class BlogExampleViewModel(application: Application) : AndroidViewModel(application) {
    val toast = MutableStateFlow("")
    fun sendToastMessage(message: String) {
        toast.update { message }
    }

    val exampleObjectList = MutableStateFlow(listOf<ExampleObject>())
    val subCategoryList = MutableStateFlow(listOf<ExampleObject>())

    fun initExampleObject() {
        exampleObjectList.update { exampleObjectList() }
    }

    fun setSubCategoryList(
        filter: String,
    ) {
        subCategoryList.update {
            subCategoryList()
                .filter {
                    it.subCategory == filter
                }
        }
    }

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    val searchExampleList = searchText
        .combine(exampleObjectList) { query, list ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.title.contains(query, ignoreCase = true)
                }
            }
        }

    fun reverseExampleList() {
        exampleObjectList.update { ArrayList(exampleObjectList.value.reversed()) }
    }

    val previewExampleData = MutableStateFlow("")
    fun setPreviewExampleData(message: String) {
        previewExampleData.update { message }
    }
}