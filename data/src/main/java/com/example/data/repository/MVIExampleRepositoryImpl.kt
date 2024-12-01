package com.example.data.repository

import com.example.domain.repository.MVIExampleRepository

class MVIExampleRepositoryImpl : MVIExampleRepository {
    override fun fetchData(): String {
        return "Fetched Data from API"
    }
} 