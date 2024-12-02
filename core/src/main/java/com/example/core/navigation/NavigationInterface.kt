package com.example.core.navigation

import android.content.Context

interface NavigationInterface {
    fun changeActivity(context: Context, fromActivity: String?, data: Any?)
}