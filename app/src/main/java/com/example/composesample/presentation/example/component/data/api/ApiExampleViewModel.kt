package com.example.composesample.presentation.example.component.data.api

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.composesample.util.NetworkStatusLiveData
import com.example.data.api.PostApiInterface
import com.example.data.api.getPosts
import com.example.domain.model.PostData
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ApiExampleViewModel(
    application: Application,
    private val postApiInterface: PostApiInterface,
    private val ktorClient: HttpClient
) : AndroidViewModel(application) {
    // viewModelScope를 사용할 때 예외 처리를 위한 CoroutineExceptionHandler
    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("NetworkLog", "Coroutine Exception Caught: $throwable")
    }

    // Network connect 관련 LiveData
    private val networkStatusLiveData = NetworkStatusLiveData(application)
    fun getNetworkStatus(): LiveData<Boolean> = networkStatusLiveData

    // Api Data
    private val _posts = MutableStateFlow<List<PostData>>(emptyList())
    val posts: StateFlow<List<PostData>> get() = _posts.asStateFlow()

    // Ktor Api Data
    private val _ktorPosts = MutableStateFlow<List<PostData>>(emptyList())
    val ktorPosts: StateFlow<List<PostData>> get() = _ktorPosts.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage.asStateFlow()

    // 기존 Retrofit 호출 함수
    fun fetchPosts() {
        viewModelScope.launch(handler) {
            try {
                _isLoading.value = true
                val result = postApiInterface.getPosts()
                _posts.value = result
                Log.d("NetworkLog", "Api call Comp")
            } catch (e: Exception) {
                Log.e("NetworkLog", "fetchPosts 실패: ${e.message}", e)
                _errorMessage.value = "오류 발생: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Ktor을 사용한 API 호출 함수
    suspend fun fetchPostsWithKtor() {
        if (networkStatusLiveData.value == false) {
            _errorMessage.value = "No network connection"
            return
        }

        _isLoading.value = true
        try {
            Log.d("NetworkLog", "Ktor Api call Start")
            val response: List<PostData> = ktorClient.getPosts()
            _ktorPosts.value = response
            _errorMessage.value = null // Clear any previous error
            Log.d("NetworkLog", "Ktor Api call Success")

            // Post인 경우 Body 값을 넣는 방법.
//            ktorClient.post {
//                setBody(111)
//            }
        } catch (e: IOException) {
            _errorMessage.value = "Network error: ${e.message}"
        } catch (e: HttpException) {
            _errorMessage.value = "HTTP error: ${e.message}"
        } catch (e: Exception) {
            _errorMessage.value = "Unexpected error: ${e.message}"
        } finally {
            _isLoading.value = false
            Log.d("NetworkLog", "Ktor Api call End")
        }
    }
}