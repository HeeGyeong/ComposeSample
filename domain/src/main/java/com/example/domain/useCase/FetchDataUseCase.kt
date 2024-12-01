package com.example.domain.useCase

import com.example.domain.repository.MVIExampleRepository

class FetchDataUseCase(private val repository: MVIExampleRepository) {
    fun execute(): String {
        return repository.fetchData()
    }
} 