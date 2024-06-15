package com.example.composesample.example.ui.api

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.data.api.PostApiInterface
import com.example.composesample.example.util.NetworkStatusLiveData
import com.example.domain.model.PostData
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiExampleViewModel(
    application: Application,
    private val postApiInterface: PostApiInterface,
) : AndroidViewModel(application) {
    // Network connect 관련 LiveData
    private val networkStatusLiveData = NetworkStatusLiveData(application)
    fun getNetworkStatus(): LiveData<Boolean> = networkStatusLiveData

    // Api Data
    private val _posts = MutableLiveData<List<PostData>>()
    val posts: LiveData<List<PostData>> get() = _posts

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
}