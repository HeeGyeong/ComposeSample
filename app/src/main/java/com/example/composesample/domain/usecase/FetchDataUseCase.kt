package com.example.composesample.domain.usecase

import com.example.composesample.data.repository.MVIExampleRepository

class FetchDataUseCase(private val repository: MVIExampleRepository) {
    fun execute(): String {
        return repository.fetchData()
    }
} 