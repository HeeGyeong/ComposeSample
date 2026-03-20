package com.example.composesample.presentation.example.component.system.platform.webview

import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WebViewIssueUI(onBackButtonClick: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }
        }

        val youtubeSampleUrl = "https://www.youtube.com/watch?v=f7ghF08rCTQ"
        repeat(3) {
            item {
                WebViewYoutubePlayer(getEmbedYoutubeUrl(youtubeSampleUrl))

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

    }
}

@Composable
fun WebViewYoutubePlayer(youtubeUrl: String) {
    val context = LocalContext.current
    val webChromeClient = remember { YoutubeWebChromeClient(context) }
    val webViewState = rememberWebViewState(url = youtubeUrl)
    val webViewClient = AccompanistWebViewClient()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = (screenWidth * 9 / 16).dp

    WebView(
        modifier = Modifier
            .height(screenHeight)
            .background(color = Color.Transparent)
            .fillMaxWidth(),
        state = webViewState,
        client = webViewClient,
        chromeClient = webChromeClient,
        onCreated = { webView ->
            with(webView) {
                settings.run {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    javaScriptCanOpenWindowsAutomatically = false
                    mediaPlaybackRequiresUserGesture = false
                }

                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                setBackgroundColor(0)
            }
        }
    )
}

class YoutubeWebChromeClient(
    private val context: Context,
) : AccompanistWebChromeClient() {
    private var fullScreenView: View? = null
    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (fullScreenView != null) { // fullScreen이 떠있으면
            callback?.onCustomViewHidden() // 종료 시킨다.
            return
        }

        fullScreenView = view
        // 뷰 추가
        windowManager.addView(fullScreenView, WindowManager.LayoutParams())
    }

    override fun onHideCustomView() {
        super.onHideCustomView()
        // 뷰 제거
        windowManager.removeView(fullScreenView)
        fullScreenView = null
    }
}

fun getEmbedYoutubeUrl(youtubeUrl: String): String {
    val youTubeTypeURL = "www.youtube.com"
    val youTubeTypeURL2 = "youtu.be"
    val youTubeLoadUrl = "https://www.youtube.com/embed/"

    return youTubeLoadUrl +
            if (youtubeUrl.contains(youTubeTypeURL)) {
                youtubeUrl.split("watch?v=")[1]
            } else if (youtubeUrl.contains(youTubeTypeURL2)) {
                youtubeUrl.split("youtu.be/")[1]
            } else {
                ""
            }
}