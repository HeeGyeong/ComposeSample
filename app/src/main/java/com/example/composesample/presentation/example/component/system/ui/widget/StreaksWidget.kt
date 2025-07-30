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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.LocalSize
import androidx.glance.layout.Row
import androidx.compose.ui.unit.DpSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.width
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
    val size = LocalSize.current
    
    Box(
        modifier = modifier
            .background(ColorProvider(Color(0xFF6200EE)))
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 큰 위젯 (3x2 이상) - 가로가 넓고 세로도 충분할 때
            size.width >= 320.dp && size.height >= 120.dp -> {
                LargeWidgetLayout(content = content)
            }
            // 중간 위젯 (2x2) - 정사각형에 가까운 형태
            size.width >= 180.dp && size.height >= 120.dp -> {
                MediumWidgetLayout(content = content)
            }
            // 작은 위젯 (2x1) - 가로로 긴 형태
            else -> {
                SmallWidgetLayout(content = content)
            }
        }
    }
}

// 큰 위젯용 레이아웃 (3x2 이상)
@SuppressLint("RestrictedApi")
@Composable
private fun LargeWidgetLayout(
    content: WeeklyPostingStreak
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 아이콘과 제목
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "⭐ Glance Widget",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Text(
                text = content.message,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = "업데이트: ${content.lastUpdate}",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                    fontSize = 10.sp
                )
            )
            
            Text(
                text = "새로고침: ${content.refreshedAt}",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                    fontSize = 8.sp
                )
            )
        }
        
        // 오른쪽: 새로고침 버튼
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
}

// 중간 위젯용 레이아웃 (2x2)
@SuppressLint("RestrictedApi")
@Composable
private fun MediumWidgetLayout(
    content: WeeklyPostingStreak
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⭐",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 20.sp
            )
        )
        
        Spacer(modifier = GlanceModifier.height(6.dp))
        
        Text(
            text = "Glance Widget",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = "업데이트: ${content.lastUpdate}",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        )
        
        Text(
            text = "새로고침: ${content.refreshedAt}",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                fontSize = 8.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Button(
            text = "🔄 새로고침",
            onClick = actionRunCallback<RefreshWidgetAction>(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ColorProvider(Color.White.copy(alpha = 0.2f)),
                contentColor = ColorProvider(Color.White)
            )
        )
    }
}

// 작은 위젯용 레이아웃 (2x1)
@SuppressLint("RestrictedApi")
@Composable
private fun SmallWidgetLayout(
    content: WeeklyPostingStreak
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘
        Text(
            text = "⭐",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 16.sp
            )
        )
        
        Spacer(modifier = GlanceModifier.width(8.dp))
        
        // 텍스트 정보
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Glance Widget",
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
        
        // 새로고침 버튼
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