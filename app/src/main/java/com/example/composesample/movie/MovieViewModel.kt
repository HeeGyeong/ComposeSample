package com.example.composesample.movie

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.composesample.api.ApiInterface
import com.example.composesample.model.MovieEntity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MovieViewModel(application: Application, private val apiInterface: ApiInterface) :
    AndroidViewModel(application) {

    private val _data = MutableLiveData<List<MovieEntity>>()
    val data: LiveData<List<MovieEntity>> = _data

    private val _flowData = MutableLiveData<MutableList<MovieEntity>>()
    val flowData: LiveData<MutableList<MovieEntity>> = _flowData

    private val _flowData2 = MutableLiveData<List<MovieEntity>>()
    val flowData2: LiveData<List<MovieEntity>> = _flowData2

    fun apiFunction(text: String) {
        val disposable = apiInterface.getSearchMovie(text)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                _data.value = data.movies
            }, {
                Log.d("ComposeLog", "Error !! $it")
            })
    }

    suspend fun apiFlowFunction(text: String) {
        _flowData.value = apiInterface.getSearchMovieFlow(text).movies as ArrayList<MovieEntity>
        _flowData2.value = apiInterface.getSearchMovieFlow(text).movies
    }
}