package com.example.composesample.presentation.example.component.api

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.composesample.util.NetworkStatusLiveData
import com.example.domain.model.PostData
import com.example.domain.useCase.GetPostUseCase
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ApiExampleViewModel를 그대로 사용하였습니다.
 *
 * fetchPost에서 api를 호출하는 부분을 useCase를 사용하도록 변경한 것 외에 모두 동일합니다.
 */
class ApiExampleUseCaseViewModel(
    application: Application,
    private val getPostUseCase: GetPostUseCase
) : AndroidViewModel(application) {
    // Network connect 관련 LiveData
    private val networkStatusLiveData = NetworkStatusLiveData(application)
    fun getNetworkStatus(): LiveData<Boolean> = networkStatusLiveData

    // Api Data
    private val _posts = MutableLiveData<List<PostData>>()
    val posts: LiveData<List<PostData>> get() = _posts

    fun fetchPosts() {
        viewModelScope.launch {
            getPostUseCase.invoke().enqueue(
                object : Callback<List<PostData>> {
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
                }
            )
        }
    }
}