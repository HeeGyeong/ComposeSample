package com.example.composesample.data.repository

interface MVIExampleRepository {
    fun fetchData(): String
}

class MVIExampleRepositoryImpl : MVIExampleRepository {
    override fun fetchData(): String {
        // Simulate API call
        return "Fetched Data from API"
    }
} 