package com.example.composesample.presentation.example.component.api

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.data.api.PostApiInterface
import com.example.composesample.util.NetworkStatusLiveData
import com.example.domain.model.PostData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiExampleViewModel(
    application: Application,
    private val postApiInterface: PostApiInterface,
    private val ktorClient: HttpClient
) : AndroidViewModel(application) {
    // Network connect 관련 LiveData
    private val networkStatusLiveData = NetworkStatusLiveData(application)
    fun getNetworkStatus(): LiveData<Boolean> = networkStatusLiveData

    // Api Data
    private val _posts = MutableLiveData<List<PostData>>()
    val posts: LiveData<List<PostData>> get() = _posts

    // Ktor Api Data
    private val _ktorPosts = MutableLiveData<List<PostData>>()
    val ktorPosts: LiveData<List<PostData>> get() = _ktorPosts

    // 기존 Retrofit 호출 함수
    fun fetchPosts() {
        viewModelScope.launch {
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
        try {
            val response: List<PostData> = ktorClient.get("https://jsonplaceholder.typicode.com/posts").body()
            _ktorPosts.postValue(response)
            Log.d("NetworkLog", "Ktor Api call Success")
        } catch (e: Exception) {
            Log.e("NetworkLog", "Ktor Api call Failed: ${e.message}")
        }
    }
}