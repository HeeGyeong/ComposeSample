# Data Module

이 문서는 `data` 모듈에 대한 설명입니다.

이 모듈은 애플리케이션의 데이터 계층을 포함하고 있으며, Clean Architecture 원칙에 따라 설계되었습니다.

## 구조

### 1. API 인터페이스 (API Interfaces)

- **설명**: 외부 API와의 통신을 위한 인터페이스를 정의합니다.

  - `PostApiInterface`: 게시물 데이터를 가져오기 위한 API 인터페이스입니다.
  
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

### 2. 데이터 소스 (Data Sources)

- **설명**: 데이터 소스와의 상호작용을 정의합니다. 이 레이어는 API 호출을 통해 데이터를 가져오는 역할을 합니다.

  - `PostDataSource`: 게시물 데이터를 가져오는 메서드를 정의하는 인터페이스입니다.
  
    ```kotlin
    package com.example.data.repository.dataSource

    import com.example.domain.model.PostData
    import retrofit2.Call

    interface PostDataSource {
        fun getPosts(): Call<List<PostData>>
    }
    ```

  - `PostDataSourceImpl`: `PostDataSource` 인터페이스를 구현한 클래스입니다. API 인터페이스를 통해 게시물 데이터를 가져옵니다.
  
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

### 3. 리포지토리 (Repositories)

- **설명**: 데이터 소스와 도메인 레이어 간의 상호작용을 관리합니다.

  - `PostRepositoryImpl`: 도메인 레이어의 `PostRepository` 인터페이스를 구현한 클래스입니다. 데이터 소스에서 게시물 데이터를 가져오는 기능을 제공합니다.
  
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

## 사용 방법

이 모듈을 사용하려면, 필요한 의존성을 추가하고, 정의된 API 인터페이스 및 리포지토리를 활용하여 데이터 계층을 구현하면 됩니다.

Clean Architecture를 따르므로, 각 레이어 간의 의존성을 최소화하고, 테스트 가능성을 높이는 것이 중요합니다.