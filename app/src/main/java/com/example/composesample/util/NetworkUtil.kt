package com.example.composesample.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NetworkUtil(private val context: Context) {
    fun isNetworkConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val isActiveNetwork = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                isActiveNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                isActiveNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }
}

class NetworkInterceptor(private val networkUtil: NetworkUtil) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkUtil.isNetworkConnected()) {
            throw IOException("Network connection is lost")
        }
        return chain.proceed(chain.request())
    }
}

class NetworkStatusLiveData(context: Context) : LiveData<Boolean>() {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            postValue(true)
        }

        override fun onLost(network: android.net.Network) {
            postValue(false)
        }
    }

    override fun onActive() {
        super.onActive()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        postValue(isNetworkAvailable())
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}