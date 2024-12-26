package com.example.composesample.coordinator

import android.content.Context
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import com.example.composesample.presentation.MainActivity

/**
 * 해당 Module의 Activity를 호출하는 Initializer
 *
 * 각 나뉘어진 sub Module에 선언되어 사용.
 */
class MainInitializer {
    @OptIn(ExperimentalAnimationApi::class)
    fun startActivity(context: Context, data: Any? = null) {
        if (data == null) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}