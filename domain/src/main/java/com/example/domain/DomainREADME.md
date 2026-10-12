# Domain Module

This document describes the `domain` module.

This module contains the domain logic of the application and is designed according to Clean Architecture principles.

> **Pure Kotlin (JVM) module.** As of the ARCH-04 refactoring, this module applies the `kotlin("jvm")` plugin only and has **no Android, Retrofit, or Gson dependencies**. Models are plain Kotlin data classes, repository interfaces expose `suspend` functions returning domain types (not `retrofit2.Call`), and JSON keys are matched by property name so no `@SerializedName` annotation is required.

## Structure

### 1. Model

- **Description**: Defines the data structures used in the application. UI/app-only models (e.g. `ExampleObject`, `ExampleMoveType`) live in the `app` module's `presentation.example.model` package, not here (ARCH-05).

  - `ArticleData` / `ArticleSource` / `ArticleResult`: Pure models for the Advanced Repository Pattern example, defined in `ArticleData.kt`. Because they are not Room `@Entity` classes, the repository interface that returns them can live in this module.

    ```kotlin
    package com.example.domain.model

    data class ArticleData(
        val id: String,
        val title: String,
        val body: String
    )

    /** 데이터가 어느 계층에서 해석되었는지 (우선순위: MEMORY > DISK > NETWORK) */
    enum class ArticleSource { MEMORY, DISK, NETWORK }

    /** 조회 결과 + 출처 + 소요 시간(ms) */
    data class ArticleResult(
        val article: ArticleData,
        val source: ArticleSource,
        val latencyMs: Long
    )
    ```

  - `PostData`: A data class that holds post information. Defined in `PostResponse.kt`. Property names match the JSON keys, so no Gson annotation is needed. Retrofit parses straight into it, so none of its non-null fields is protected against a missing key — kept as is and recorded in `KnownLimitations.md` (see the Gson rule in `DataRules.md`).

    ```kotlin
    package com.example.domain.model

    // domain 모듈은 순수 Kotlin 유지. 프로퍼티명이 JSON 키와 일치하므로 Gson 어노테이션 불필요.
    data class PostData(
        val userId: Int,
        val id: Int,
        val title: String,
        val body: String
    )
    ```

### 2. Repository

- **Description**: Defines interactions with data sources. This layer provides abstraction for the data sources (e.g., API, database). Interfaces expose `suspend` functions returning domain types — no framework types such as `retrofit2.Call` leak into the domain layer.

  - `PostRepository`: An interface that defines methods for fetching post data.

    ```kotlin
    package com.example.domain.repository

    import com.example.domain.model.PostData

    interface PostRepository {
        suspend fun getPosts(): List<PostData>
    }
    ```

  - `ArticleRepository`: Resolves an article through memory → disk → network (Advanced Repository Pattern example).

    ```kotlin
    package com.example.domain.repository

    import com.example.domain.model.ArticleResult

    interface ArticleRepository {
        suspend fun getArticle(id: String, forceRefresh: Boolean = false): ArticleResult
        fun invalidateMemory()
        fun invalidateDisk()
    }
    ```

  - `MVIExampleRepository`: An interface used by the MVI example to fetch a simple value.

    ```kotlin
    package com.example.domain.repository

    interface MVIExampleRepository {
        fun fetchData(): String
    }
    ```

### 3. Use Cases

- **Description**: Defines use cases that handle domain logic. This layer implements business rules and interacts with the repository to fetch and process data. Use cases are invoked via `operator fun invoke()` (CONV-04).

  - `GetPostUseCase`: A use case for fetching post data.

    ```kotlin
    package com.example.domain.useCase

    import com.example.domain.repository.PostRepository

    class GetPostUseCase(
        private val postRepository: PostRepository
    ) {
        suspend operator fun invoke() = postRepository.getPosts()
    }
    ```

  - `GetArticleUseCase`: A use case for the Advanced Repository Pattern example.

    ```kotlin
    package com.example.domain.useCase

    import com.example.domain.repository.ArticleRepository

    class GetArticleUseCase(
        private val articleRepository: ArticleRepository
    ) {
        suspend operator fun invoke(id: String, forceRefresh: Boolean = false) =
            articleRepository.getArticle(id, forceRefresh)
    }
    ```

  - `FetchDataUseCase`: A use case used by the MVI example.

    ```kotlin
    package com.example.domain.useCase

    import com.example.domain.repository.MVIExampleRepository

    class FetchDataUseCase(
        private val repository: MVIExampleRepository
    ) {
        operator fun invoke(): String = repository.fetchData()
    }
    ```

## Usage

To use this module, add the necessary dependencies and implement business logic using the defined data models and repositories.

Since it follows Clean Architecture, it is important to minimize dependencies between layers and enhance testability. Keeping this module as pure Kotlin guarantees that no Android/framework dependency leaks into the core business logic.
