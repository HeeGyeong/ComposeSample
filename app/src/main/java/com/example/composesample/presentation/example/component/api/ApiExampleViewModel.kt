package com.example.composesample.presentation.example.component.api

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.composesample.util.NetworkStatusLiveData
import com.example.data.api.PostApiInterface
import com.example.data.api.getPosts
import com.example.domain.model.PostData
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
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
    private val _posts = MutableLiveData<List<PostData>>()
    val posts: LiveData<List<PostData>> get() = _posts

    // Ktor Api Data
    private val _ktorPosts = MutableLiveData<List<PostData>>()
    val ktorPosts: LiveData<List<PostData>> get() = _ktorPosts

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // 기존 Retrofit 호출 함수
    fun fetchPosts() {
        viewModelScope.launch(handler) {
            postApiInterface.getPosts().enqueue(object : Callback<List<PostData>> {
                override fun onResponse(
                    call: Call<List<PostData>>,
                    response: Response<List<PostData>>
                ) {
                    Log.d("NetworkLog", "Api call Comp")
                    if (response.isSuccessful) {
                        _posts.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<List<PostData>>, t: Throwable) {
                    // Handle error
                    Log.d("NetworkLog", "onFailure : $call : t? $t")
                }
            })
        }
    }

    // Ktor을 사용한 API 호출 함수
    suspend fun fetchPostsWithKtor() {
        if (getNetworkStatus().value == false) {
            _errorMessage.postValue("No network connection")
            return
        }

        _isLoading.postValue(true)
        try {
            Log.d("NetworkLog", "Ktor Api call Start")
            val response: List<PostData> = ktorClient.getPosts()
            _ktorPosts.postValue(response)
            _errorMessage.postValue(null) // Clear any previous error
            Log.d("NetworkLog", "Ktor Api call Success")

            // Post인 경우 Body 값을 넣는 방법.
//            ktorClient.post {
//                setBody(111)
//            }
        } catch (e: IOException) {
            _errorMessage.postValue("Network error: ${e.message}")
        } catch (e: HttpException) {
            _errorMessage.postValue("HTTP error: ${e.message}")
        } catch (e: Exception) {
            _errorMessage.postValue("Unexpected error: ${e.message}")
        } finally {
            _isLoading.postValue(false)
            Log.d("NetworkLog", "Ktor Api call End")
        }
    }
}