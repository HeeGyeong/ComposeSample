package com.example.core.navigation

import android.content.Context

open class Navigation(private val navigationInterface: NavigationInterface) {
    operator fun invoke(
        context: Context,
        fromActivity: String?,
        data: Any?
    ) = navigationInterface.changeActivity(context, fromActivity, data)
}
