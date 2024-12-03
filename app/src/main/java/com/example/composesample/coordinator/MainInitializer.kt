package com.example.composesample.coordinator

import android.content.Context
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import com.example.composesample.presentation.MainActivity

class MainInitializer {
    @OptIn(ExperimentalAnimationApi::class)
    fun startActivity(context: Context, data: Any? = null) {
        if (data == null) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}