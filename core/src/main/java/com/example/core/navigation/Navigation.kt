package com.example.core.navigation

import android.content.Context

interface Navigation {
    fun changeActivity(context: Context, fromActivity: String?, data: Any?)
}