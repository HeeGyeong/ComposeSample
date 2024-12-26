package com.example.composesample.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.example.BlogExampleActivity
import com.example.composesample.presentation.legacy.LegacyActivity

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                color = Color.White
            ) {
                MainActivityScreen()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainActivityScreen() {
    val context = LocalContext.current

    MainScreenContent(
        onExampleCodeClick = {
            context.startActivity(Intent(context, BlogExampleActivity::class.java))
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