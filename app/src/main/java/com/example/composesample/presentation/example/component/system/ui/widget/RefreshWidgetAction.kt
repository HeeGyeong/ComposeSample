package com.example.composesample.presentation.example.component.system.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll

/**
 * 위젯 새로고침을 위한 Action Callback
 */
class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 위젯을 새로고침 (현재 데이터로 업데이트)
        StreaksWidget().updateAll(context)
    }
} 