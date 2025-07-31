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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
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
 * 2x2 크기 전용 중간 위젯
 */
class MediumWidget : GlanceAppWidget() {

    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val exampleList = exampleObjectList()
        val lastUpdateDate = exampleList.lastOrNull()?.lastUpdate ?: "업데이트 정보 없음"
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val streakData = WeeklyPostingStreak(
            message = "Medium Widget",
            lastUpdate = lastUpdateDate,
            refreshedAt = currentTime
        )

        provideContent {
            GlanceTheme {
                val clickModifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
                MediumWidgetContent(modifier = clickModifier, content = streakData)
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun MediumWidgetContent(
    modifier: GlanceModifier = GlanceModifier,
    content: WeeklyPostingStreak
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorProvider(Color.Green))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🖼️",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 24.sp
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Text(
                text = "MEDIUM 2x2",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = GlanceModifier.height(6.dp))

            Text(
                text = "업데이트: ${content.lastUpdate}",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            )
            
            Text(
                text = "새로고침: ${content.refreshedAt}",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Button(
                text = "🔄 UPDATE",
                onClick = actionRunCallback<RefreshWidgetAction>(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(Color.White.copy(alpha = 0.3f)),
                    contentColor = ColorProvider(Color.White)
                )
            )
        }
    }
} 