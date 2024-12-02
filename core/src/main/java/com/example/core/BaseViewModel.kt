package com.example.core

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.core.navigation.Navigation

open class BaseViewModel(
    private val navigation: Navigation,
    application: Application
) : AndroidViewModel(application) {

    fun changeToActivity(
        context: Context, fromActivity: String?, data: Any? = null
    ) {
        Log.d(
            "changeToActivity",
            "baseActivity changeToActivity $context -> $fromActivity ::: data ? $data"
        )
        navigation(context, fromActivity, data)
    }
}