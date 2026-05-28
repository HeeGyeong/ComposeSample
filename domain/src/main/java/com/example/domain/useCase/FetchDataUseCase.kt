package com.example.domain.useCase

import com.example.domain.repository.MVIExampleRepository

class FetchDataUseCase(private val repository: MVIExampleRepository) {
    operator fun invoke(): String {
        return repository.fetchData()
    }
} 