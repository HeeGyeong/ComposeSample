package com.example.core.navigation

import android.content.Context

open class NavigationImpl(private val navigation: Navigation) {
    operator fun invoke(
        context: Context,
        fromActivity: String?,
        data: Any?
    ) = navigation.changeActivity(context, fromActivity, data)
}
