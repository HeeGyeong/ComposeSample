package com.example.composesample.presentation.example.component.system.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.composesample.presentation.MainActivity
import com.example.composesample.presentation.example.exampleObjectList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Streaks Widget Implementation
 */
class StreaksWidget : GlanceAppWidget() {

    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalAnimationApi::class)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var streakData: WeeklyPostingStreak? = null

        val exampleList = exampleObjectList()
        val lastUpdateTitle = exampleList.lastOrNull()?.title ?: "타이틀 정보 없음"
        val lastUpdateDate = exampleList.lastOrNull()?.lastUpdate ?: "업데이트 정보 없음"
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        streakData = WeeklyPostingStreak(
            message = lastUpdateTitle,
            lastUpdate = lastUpdateDate,
            refreshedAt = currentTime
        )

        provideContent {
            GlanceTheme {
                streakData.let { data ->
                    val streakModifier = GlanceModifier.clickable(
                        actionStartActivity<MainActivity>()
                    )
                    StreakContent(modifier = streakModifier, content = data)
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun StreakContent(
    modifier: GlanceModifier = GlanceModifier,
    content: WeeklyPostingStreak
) {
    Box(
        modifier = modifier
            .background(ColorProvider(Color(0xFF6200EE)))
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Streak(content = content)
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun Streak(
    content: WeeklyPostingStreak
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⭐",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 16.sp
            )
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Glance Widget ~",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "${content.refreshedAt}",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                    fontSize = 8.sp
                )
            )
        }

        Button(
            text = "🔄",
            onClick = actionRunCallback<RefreshWidgetAction>(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ColorProvider(Color.White.copy(alpha = 0.2f)),
                contentColor = ColorProvider(Color.White)
            )
        )
    }
}

data class WeeklyPostingStreak(
    val message: String,
    val lastUpdate: String,
    val refreshedAt: String
) 