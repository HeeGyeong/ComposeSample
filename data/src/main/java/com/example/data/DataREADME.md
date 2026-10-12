# Data Module

This document describes the `data` module.

This module contains the data layer of the application and is designed according to Clean Architecture principles. It depends on `domain` (models and repository interfaces) and never on the presentation layer.

## Structure

### 1. API

- **Description**: Talks to the external API (`ApiClient.NEW_BASE_URL` = jsonplaceholder). Since ARCH-01/02 the calls are `suspend` functions that return domain types — no `retrofit2.Call` reaches the domain.

  - `PostApiInterface`: Retrofit interface for fetching post data.

    ```kotlin
    package com.example.data.api

    import com.example.domain.model.PostData
    import retrofit2.http.GET

    interface PostApiInterface {
        @GET("/posts")
        suspend fun getPosts(): List<PostData>
    }
    ```

  - `HttpClient.getPosts()` (`KtorApiExtensions.kt`): the same call through Ktor.

    ```kotlin
    suspend fun HttpClient.getPosts(): List<PostData> {
        return this.get("/posts").body()
    }
    ```

  Both converters are Gson, which ignores Kotlin defaults and null-safety for missing keys — see the Gson rule in `docs/data/DataRules.md` and the `PostData` entry in `docs/KnownLimitations.md`.

### 2. Data Sources

- **Description**: A data-layer abstraction over the API.

  - `PostDataSource` / `PostDataSourceImpl`

    ```kotlin
    package com.example.data.repository.dataSource

    interface PostDataSource {
        suspend fun getPosts(): List<PostData>
    }

    // com.example.data.repository.dataSourceImpl
    class PostDataSourceImpl(
        private val postApiInterface: PostApiInterface
    ) : PostDataSource {
        override suspend fun getPosts(): List<PostData> {
            return postApiInterface.getPosts()
        }
    }
    ```

### 3. Repositories

- **Description**: Implementations of the domain repository interfaces, plus one data-layer interface.

  - `PostRepositoryImpl`: implements domain `PostRepository` through `PostDataSource`.

    ```kotlin
    class PostRepositoryImpl(
        private val postDataSource: PostDataSource
    ) : PostRepository {
        override suspend fun getPosts(): List<PostData> = postDataSource.getPosts()
    }
    ```

  - `ArticleRepositoryImpl`: implements domain `ArticleRepository` for the Advanced Repository Pattern example — memory → disk → network resolution, with disk and network simulated by in-memory maps and `delay`, and lower-layer hits written back to the upper layers.

  - `MVIExampleRepositoryImpl`: implements domain `MVIExampleRepository` with a fixed string for the MVI example.

  - `UserCacheRepository` / `UserCacheRepositoryImpl`: the DataCache example's repository. Its interface lives here rather than in `domain` because `UserData` is a Room `@Entity`, which the pure-Kotlin domain module cannot reference.

    ```kotlin
    interface UserCacheRepository {
        fun searchUserName(name: String): Flow<List<UserData>>
        suspend fun insertUserData(item: UserData)
        suspend fun updateUserData(item: UserData)
        suspend fun deleteUserData(item: UserData)
        suspend fun allDataDelete()
    }
    ```

### 4. Database (Room)

- **Description**: The DataCache example's local database.

  - `UserData`: table entity

    ```kotlin
    @Entity(tableName = "exampleTable")
    data class UserData(
        @PrimaryKey
        val id: Long?,

        @ColumnInfo(name = "user_name")
        val userName: String
    )
    ```

  - `ExampleDao`: DAO (`RoomDao.kt`)

    ```kotlin
    @Dao
    interface ExampleDao {
        @Query("SELECT * FROM exampleTable WHERE user_name LIKE :searchName || '%' ORDER BY id DESC")
        fun searchData(searchName: String): Flow<List<UserData>>
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertData(item: UserData)
        @Update(onConflict = OnConflictStrategy.REPLACE)
        suspend fun updateData(item: UserData)
        @Delete
        suspend fun deleteData(item: UserData)
        @Query("DELETE FROM exampleTable")
        suspend fun allDataDelete()
    }
    ```

  - `RoomSingleton`: Room database

    ```kotlin
    @Database(entities = [UserData::class], version = 3, exportSchema = false)
    abstract class RoomSingleton : RoomDatabase() {
        abstract fun exampleDao(): ExampleDao
    }
    ```

  The other Room examples (FTS search, indexes, multi-table insert, Paging's RemoteMediator) keep their own databases inside the `app` module next to the example screens.

## Usage

To use this module, add the necessary dependencies and implement the data layer using the defined API interfaces and repositories.

Since it follows Clean Architecture, it is important to minimize dependencies between layers and enhance testability.
