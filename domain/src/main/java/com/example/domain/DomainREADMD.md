# Domain Module

This document describes the `domain` module.

This module contains the domain logic of the application and is designed according to Clean Architecture principles.

## Structure

### 1. Model

- **Description**: Defines the data structures used in the application.

  - `MovieEntity`: A data class that holds movie information.
  
    ```kotlin
    package com.example.domain.model

    data class MovieEntity(
        val actor: String,
        val director: String,
        val image: String,
        val link: String,
        val pubDate: String,
        val subtitle: String,
        val title: String,
        val userRating: String
    )
    ```

  - `ExampleObject`: A data class that defines an example object.
  
    ```kotlin
    package com.example.domain.model

    data class ExampleObject(
        val subCategory: String = "",
        val moveType: ExampleMoveType = ExampleMoveType.UI,
        val title: String,
        val description: String,
        val blogUrl: String,
        val exampleType: String,
    )

    enum class ExampleMoveType {
        UI,
        ACTIVITY,
        EMPTY,
    }
    ```

  - `MovieResponse`: A data class that contains a list of movies.
  
    ```kotlin
    package com.example.domain.model

    import com.google.gson.annotations.SerializedName

    data class MovieResponse(
        @SerializedName("display")
        val display: Int,

        @SerializedName("items")
        val movies: List<MovieEntity>,

        @SerializedName("lastBuildDate")
        val lastBuildDate: String,

        @SerializedName("start")
        val start: Int,

        @SerializedName("total")
        val total: Int
    )
    ```

  - `PostData`: A data class that holds post information.
  
    ```kotlin
    package com.example.domain.model

    import com.google.gson.annotations.SerializedName

    data class PostData(
        @SerializedName("userId") val userId: Int,
        @SerializedName("id") val id: Int,
        @SerializedName("title") val title: String,
        @SerializedName("body") val body: String
    )
    ```

### 2. Repository

- **Description**: Defines interactions with data sources. This layer provides abstraction for the data sources (e.g., API, database).

  - `PostRepository`: An interface that defines methods for fetching post data.
  
    ```kotlin
    package com.example.domain.repository

    import com.example.domain.model.PostData
    import retrofit2.Call

    interface PostRepository {
        fun getPosts(): Call<List<PostData>>
    }
    ```

### 3. Use Cases

- **Description**: Defines use cases that handle domain logic. This layer implements business rules and interacts with the repository to fetch and process data.

  - `GetPostUseCase`: A use case for fetching post data.
  
    ```kotlin
    package com.example.domain.useCase

    import com.example.domain.repository.PostRepository

    class GetPostUseCase(
        private val postRepository: PostRepository
    ) {
        operator fun invoke() = postRepository.getPosts()
    }
    ```

## Usage

To use this module, add the necessary dependencies and implement business logic using the defined data models and repositories.

Since it follows Clean Architecture, it is important to minimize dependencies between layers and enhance testability.