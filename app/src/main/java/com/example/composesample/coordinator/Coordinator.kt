package com.example.composesample.coordinator

import android.content.Context
import android.util.Log
import com.example.coordinator.coordinator.CoordinatorInitializer
import com.example.core.navigation.NavigationInterface

/**
 * NavigationInterfaceImpl
 *
 * 실제 sub module 간 activity 이동을 담당하는 클래스
 */
class Coordinator : NavigationInterface {
    override fun changeActivity(
        context: Context,
        fromActivity: String?,
        data: Any?,
    ) {
        Log.d("Coordinator", "NavigationController $fromActivity , data ? $data")
        when (fromActivity) {
            "CoordinatorModuleUI" -> {
                Log.d("Coordinator", "Move Coordinator Sample Activity")
                CoordinatorInitializer().startActivity(context, data)
            }

            "MainModuleUI" -> {
                Log.d("Coordinator", "Move Main Module Activity")
                MainInitializer().startActivity(context, data)
            }

            else -> {
                Log.d("Coordinator", "in else .. what u want ? $fromActivity")
            }
        }
    }
}
