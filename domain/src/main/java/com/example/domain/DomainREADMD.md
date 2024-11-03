# Domain Module

이 문서는 `domain` 모듈에 대한 설명입니다.

이 모듈은 애플리케이션의 도메인 로직을 포함하고 있으며, Clean Architecture 원칙에 따라 설계되었습니다.

## 구조

### 1. 모델 (Model)

- **설명**: 애플리케이션에서 사용하는 데이터 구조를 정의합니다.

  - `MovieEntity`: 영화 정보를 담고 있는 데이터 클래스입니다.
  
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

  - `ExampleObject`: 예제 객체를 정의하는 데이터 클래스입니다.
  
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

  - `MovieResponse`: 영화 목록을 포함하는 응답 데이터 클래스입니다.
  
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

  - `PostData`: 포스트 정보를 담고 있는 데이터 클래스입니다.
  
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

### 2. 레포지토리 (Repository)

- **설명**: 데이터 소스와의 상호작용을 정의합니다. 이 레이어는 데이터의 출처(예: API, 데이터베이스 등)에 대한 추상화를 제공합니다.

  - `PostRepository`: 포스트 데이터를 가져오는 메서드를 정의하는 인터페이스입니다.
  
    ```kotlin
    package com.example.domain.repository

    import com.example.domain.model.PostData
    import retrofit2.Call

    interface PostRepository {
        fun getPosts(): Call<List<PostData>>
    }
    ```

### 3. 유스케이스 (Use Cases)

- **설명**: 도메인 로직을 처리하는 유스케이스를 정의하는 레이어입니다. 이 레이어는 비즈니스 규칙을 구현하며, 레포지토리와 상호작용하여 데이터를 가져오고 가공합니다.

  - `GetPostUseCase`: 포스트 데이터를 가져오는 유스케이스입니다.
  
    ```kotlin
    package com.example.domain.useCase

    import com.example.domain.repository.PostRepository

    class GetPostUseCase(
        private val postRepository: PostRepository
    ) {
        operator fun invoke() = postRepository.getPosts()
    }
    ```

## 사용 방법

이 모듈을 사용하려면, 필요한 의존성을 추가하고, 정의된 데이터 모델 및 레포지토리를 활용하여 비즈니스 로직을 구현하면 됩니다.

Clean Architecture를 따르므로, 각 레이어 간의 의존성을 최소화하고, 테스트 가능성을 높이는 것이 중요합니다.