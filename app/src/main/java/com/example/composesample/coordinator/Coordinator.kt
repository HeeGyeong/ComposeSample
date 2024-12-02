package com.example.composesample.coordinator

import android.content.Context
import android.util.Log
import com.example.core.navigation.NavigationInterface

// NavigationInterfaceImpl
class Coordinator : NavigationInterface {
    override fun changeActivity(
        context: Context,
        fromActivity: String?,
        data: Any?,
    ) {
        Log.d("Coordinator", "NavigationController $fromActivity , data ? $data")
        when (fromActivity) {
            "CoordinatorExampleUI" -> {
                Log.d("Coordinator", "CoordinatorExampleUI Call")
            }

            else -> {
                Log.d("Coordinator", "in else .. what u want ? $fromActivity")
            }
        }
    }
}
