package com.example.composesample.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.example.composesample.presentation.example.BlogExampleActivity
import com.example.composesample.presentation.example.component.system.ui.widget.StreaksWidget
import com.example.composesample.presentation.example.component.system.ui.widget.SmallWidget
import com.example.composesample.presentation.example.component.system.ui.widget.MediumWidget
import com.example.composesample.presentation.example.component.system.ui.widget.LargeWidget
import com.example.composesample.presentation.legacy.LegacyActivity
import com.example.composesample.util.ConstValue.ExampleType
import com.example.composesample.util.ConstValue.IntentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateWidget()

        setContent {
            Surface(
                color = Color.White
            ) {
                // windowInsetsPadding이 systemBars를 처리하므로 content padding 불필요
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.LightGray)
                        .windowInsetsPadding(WindowInsets.systemBars),
                ) { _ ->
                    MainActivityScreen()
                }
            }
        }
    }

    private fun updateWidget() {
        // Activity 수명주기에 종속되도록 lifecycleScope 사용 (생명주기 외 실행 방지)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                StreaksWidget().updateAll(this@MainActivity)
                SmallWidget().updateAll(this@MainActivity)
                MediumWidget().updateAll(this@MainActivity)
                LargeWidget().updateAll(this@MainActivity)
            } catch (e: Exception) {
                Log.e("MainActivity", "위젯 업데이트 실패: ${e.message}", e)
            }
        }
    }
}

/**
 * Feature 단위로 이동할 수 있는 Main 화면
 *
 * presentation Layer 아래에 있는 2개의 package는 각각의 feature 단위다.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainActivityScreen() {
    val context = LocalContext.current

    MainScreenContent(
        onExampleCodeClick = {
            val intent = Intent(context, BlogExampleActivity::class.java).apply {
                putExtra(IntentType, ExampleType)
            }
            context.startActivity(intent)
        },
        onLegacyCodeClick = {
            context.startActivity(Intent(context, LegacyActivity::class.java))
        },
        onWebViewClick = { url ->
            openWebPage(context, url)
        }
    )
}

fun openWebPage(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

fun getTextStyle(fontSize: Int) = androidx.compose.ui.text.TextStyle(
    fontSize = fontSize.sp,
    letterSpacing = (-0.02).em,
    lineHeight = 1.4.em
)