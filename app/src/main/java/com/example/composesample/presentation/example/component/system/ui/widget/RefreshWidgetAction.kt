package com.example.composesample.presentation.example.component.system.ui.widget

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** UPDATE 를 누른 시각 — 위젯마다의 Glance 상태(Preferences DataStore)에 남는다 */
val RefreshedAtKey = longPreferencesKey("refreshed_at")

/** 마지막으로 누른 시각과 이번에 그리기 시작한 시각 중 늦은 쪽 */
fun refreshedAtText(pressedAt: Long?, renderedAt: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(maxOf(pressedAt ?: 0L, renderedAt)))

/**
 * 위젯 새로고침을 위한 Action Callback — 누른 위젯만 다시 그린다
 */
class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 시각은 provideGlance 가 아니라 상태로 넘긴다 — 세션이 살아 있으면 provideGlance 는 다시 불리지 않고 상태 변경만 다시 그린다
        updateAppWidgetState(context, glanceId) { prefs -> prefs[RefreshedAtKey] = System.currentTimeMillis() }
        // glanceId 만으로는 어느 위젯인지 모른다 — 위젯 종류별로 등록된 id 에서 찾는다
        val manager = GlanceAppWidgetManager(context)
        listOf(MediumWidget(), LargeWidget(), StreaksWidget())
            .firstOrNull { widget -> glanceId in manager.getGlanceIds(widget.javaClass) }
            ?.update(context, glanceId)
    }
}
