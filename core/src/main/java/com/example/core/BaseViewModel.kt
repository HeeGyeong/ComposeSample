package com.example.core

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.core.navigation.NavigationImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

class BaseViewModel(
    private val navigationImpl: NavigationImpl,
    application: Application
) : AndroidViewModel(application) {

    fun changeToActivity(
        context: Context, fromActivity: String?, data: Any? = null
    ) {
        Log.d(
            "changeToActivity",
            "baseActivity changeToActivity $context -> $fromActivity ::: data ? $data"
        )
        navigationImpl(context, fromActivity, data)
    }
}