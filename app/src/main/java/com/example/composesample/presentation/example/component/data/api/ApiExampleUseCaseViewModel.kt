package com.example.composesample.presentation.example.component.data.api

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
            try {
                val result = getPostUseCase.invoke()
                _posts.postValue(result)
                Log.d("NetworkLog", "Api call Comp")
            } catch (e: Exception) {
                Log.e("NetworkLog", "fetchPosts 실패: ${e.message}", e)
            }
        }
    }
}