package com.example.coordinator.coordinator

import android.content.Context
import android.content.Intent
import com.example.coordinator.CoordinatorActivity

class CoordinatorInitializer {
    fun startActivity(context: Context, data: Any? = null) {
        if (data == null) {
            context.startActivity(Intent(context, CoordinatorActivity::class.java))
        }
    }
}

