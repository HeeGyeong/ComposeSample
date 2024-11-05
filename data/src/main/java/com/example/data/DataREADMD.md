# Data Module

This document describes the `data` module.

This module contains the data layer of the application and is designed according to Clean Architecture principles.

## Structure

### 1. API Interfaces

- **Description**: Defines interfaces for communication with external APIs.

  - `PostApiInterface`: API interface for fetching post data.
  
    ```kotlin
    package com.example.data.api

    import com.example.domain.model.PostData
    import retrofit2.Call
    import retrofit2.http.GET

    interface PostApiInterface {
        @GET("/posts")
        fun getPosts(): Call<List<PostData>>
    }
    ```

### 2. Data Sources

- **Description**: Defines interactions with data sources. This layer is responsible for fetching data through API calls.

  - `PostDataSource`: Interface defining methods for fetching post data.
  
    ```kotlin
    package com.example.data.repository.dataSource

    import com.example.domain.model.PostData
    import retrofit2.Call

    interface PostDataSource {
        fun getPosts(): Call<List<PostData>>
    }
    ```

  - `PostDataSourceImpl`: Class implementing the `PostDataSource` interface. It fetches post data through the API interface.
  
    ```kotlin
    package com.example.data.repository.dataSourceImpl

    import com.example.data.api.PostApiInterface
    import com.example.data.repository.dataSource.PostDataSource
    import com.example.domain.model.PostData
    import retrofit2.Call

    class PostDataSourceImpl(
        private val postApiInterface: PostApiInterface
    ) : PostDataSource {
        override fun getPosts(): Call<List<PostData>> {
            return postApiInterface.getPosts()
        }
    }
    ```

### 3. Repositories

- **Description**: Manages interactions between data sources and the domain layer.

  - `PostRepositoryImpl`: Class implementing the `PostRepository` interface from the domain layer. It provides functionality to fetch post data from the data source.
  
    ```kotlin
    package com.example.data.repository

    import com.example.data.repository.dataSource.PostDataSource
    import com.example.domain.model.PostData
    import com.example.domain.repository.PostRepository
    import retrofit2.Call

    class PostRepositoryImpl(
        private val postDataSource: PostDataSource
    ) : PostRepository {
        override fun getPosts(): Call<List<PostData>> = postDataSource.getPosts()
    }
    ```

## Usage

To use this module, add the necessary dependencies and implement the data layer using the defined API interfaces and repositories.

Since it follows Clean Architecture, it is important to minimize dependencies between layers and enhance testability.