package com.example.composesample.movie

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.composesample.api.ApiInterface
import com.example.composesample.api.PostApiInterface
import com.example.composesample.model.MovieEntity
import com.example.composesample.model.PostData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 2024.04.10 기준
 *
 * 해당 viewModel에서 사용되는 Sample API는 사용이 불가능 합니다.
 *
 * 해당 예제를 확인이 필요한 경우, 동작 가능한 Sample API와 그에 맞는 DTO로 변경만 해주시면 사용 가능합니다.
 */
class MovieViewModel(
    application: Application,
    private val apiInterface: ApiInterface,
    private val postApiInterface: PostApiInterface,
) : AndroidViewModel(application) {

    private val _data = MutableLiveData<List<MovieEntity>>()
    val data: LiveData<List<MovieEntity>> = _data

    private val _flowData = MutableLiveData<MutableList<MovieEntity>>()
    val flowData: LiveData<MutableList<MovieEntity>> = _flowData

    private val _flowData2 = MutableLiveData<List<MovieEntity>>()
    val flowData2: LiveData<List<MovieEntity>> = _flowData2

    @SuppressLint("CheckResult")
    fun apiFunction(text: String) {
        apiInterface.getSearchMovie(text)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                _data.value = data.movies
            }, {
                Log.d("ComposeLog", "Error !! $it")
            })
    }

    suspend fun apiFlowFunction(text: String) {
        _flowData.value = try {
            apiInterface.getSearchMovieFlow(text).movies as ArrayList<MovieEntity>
        } catch (e: Exception) {
            Log.d("ComposeLog", "Flow Error 1 !! $e")
            null
        }
        _flowData2.value = try {
            apiInterface.getSearchMovieFlow(text).movies
        } catch (e: Exception) {
            Log.d("ComposeLog", "Flow Error 2 !! $e")
            null
        }
    }


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
                        Log.d("asdfasdfasdf", "onResponse : $call : res? $response")
                        _posts.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<List<PostData>>, t: Throwable) {
                    // Handle error
                    Log.d("asdfasdfasdf", "onFailure : $call : t? $t")
                }
            })
        }
    }
}