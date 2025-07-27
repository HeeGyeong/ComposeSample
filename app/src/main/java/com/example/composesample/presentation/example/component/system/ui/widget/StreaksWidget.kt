package com.example.composesample.presentation.example.component.system.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import com.example.composesample.presentation.MainActivity
import com.example.composesample.presentation.example.exampleObjectList

/**
 * Glance를 사용한 실제 위젯 구현체
 */
class StreaksWidget : GlanceAppWidget() {

    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalAnimationApi::class)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 실제 데이터를 가져오는 로직 (여기서는 예시 데이터 사용)
        var streakData: WeeklyPostingStreak? = null

        // ExampleObjectList에서 마지막 업데이트 날짜 가져오기
        val exampleList = exampleObjectList()
        val lastUpdateTitle = exampleList.lastOrNull()?.title ?: "타이틀 정보 없음"
        val lastUpdateDate = exampleList.lastOrNull()?.lastUpdate ?: "업데이트 정보 없음"

        streakData = WeeklyPostingStreak(
            message = lastUpdateTitle,
            lastUpdate = lastUpdateDate
        )

        provideContent {
            GlanceTheme {
                streakData.let { data ->
                    // 위젯 클릭 이벤트
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
        Streak(
            modifier = GlanceModifier.wrapContentSize(),
            content = content
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun Streak(
    modifier: GlanceModifier = GlanceModifier,
    content: WeeklyPostingStreak
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = content.message,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Text(
            text = "마지막 업데이트: ${content.lastUpdate}",
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

/**
 * 예시 데이터 클래스
 */
data class WeeklyPostingStreak(
    val message: String,
    val lastUpdate: String
) 