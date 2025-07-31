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
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.composesample.presentation.MainActivity
import com.example.composesample.presentation.example.exampleObjectList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 3x2+ 크기 전용 큰 위젯
 */
class LargeWidget : GlanceAppWidget() {

    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val exampleList = exampleObjectList()
        val lastUpdateTitle = exampleList.lastOrNull()?.title ?: "타이틀 정보 없음"
        val lastUpdateDate = exampleList.lastOrNull()?.lastUpdate ?: "업데이트 정보 없음"
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val streakData = WeeklyPostingStreak(
            message = lastUpdateTitle,
            lastUpdate = lastUpdateDate,
            refreshedAt = currentTime
        )

        provideContent {
            GlanceTheme {
                val clickModifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
                LargeWidgetContent(modifier = clickModifier, content = streakData)
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun LargeWidgetContent(
    modifier: GlanceModifier = GlanceModifier,
    content: WeeklyPostingStreak
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorProvider(Color.Blue))
            .padding(16.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "🖥️ LARGE DASHBOARD",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Text(
                    text = "3x2+ 크기의 위젯",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(6.dp))
                
                Text(
                    text = "업데이트: ${content.lastUpdate}",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                        fontSize = 11.sp
                    )
                )
                
                Text(
                    text = "새로고침: ${content.refreshedAt}",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                        fontSize = 10.sp
                    )
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACTIVE",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Button(
                    text = "🔄 SYNC",
                    onClick = actionRunCallback<RefreshWidgetAction>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(Color.White.copy(alpha = 0.3f)),
                        contentColor = ColorProvider(Color.White)
                    )
                )
            }
        }
    }
} 