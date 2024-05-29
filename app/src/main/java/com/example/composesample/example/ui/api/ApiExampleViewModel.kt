package com.example.composesample.example.ui.api

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.composesample.api.PostApiInterface
import com.example.composesample.model.PostData
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiExampleViewModel(
    application: Application,
    private val postApiInterface: PostApiInterface,
) : AndroidViewModel(application) {

    // 해당 fetch Data 관련해서 Example로 옮겨서 작업하기.
    private val _posts = MutableLiveData<List<PostData>>()
    val posts: LiveData<List<PostData>> get() = _posts

    fun fetchPosts() {
        viewModelScope.launch {
            postApiInterface.getPosts().enqueue(object : Callback<List<PostData>> {
                override fun onResponse(
                    call: Call<List<PostData>>,
                    response: Response<List<PostData>>
                ) {
                    if (response.isSuccessful) {
                        _posts.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<List<PostData>>, t: Throwable) {
                    // Handle error
                    Log.d("postApiInterface", "onFailure : $call : t? $t")
                }
            })
        }
    }
}