package com.example.composesample.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.example.ui.clickevent.ClickEventUI
import com.example.composesample.example.ui.ffmpeg.FfmpegEncodingUI
import com.example.composesample.example.ui.ffmpeg.executeCommand
import com.example.composesample.example.ui.ffmpeg.getRealPathFromURI
import com.example.composesample.example.ui.flexbox.FlexBoxUI
import com.example.composesample.example.ui.lazycolumn.LazyColumnIssueUI
import com.example.composesample.example.ui.recorde.AudioRecordeUI
import com.example.composesample.example.ui.text.TextStyleUI
import com.example.composesample.example.ui.webview.WebViewIssueUI
import com.example.composesample.example.util.ConstValue.Companion.AudioRecordeExample
import com.example.composesample.example.util.ConstValue.Companion.ClickEventExample
import com.example.composesample.example.util.ConstValue.Companion.FfmpegExample
import com.example.composesample.example.util.ConstValue.Companion.FlexBoxLayoutExample
import com.example.composesample.example.util.ConstValue.Companion.LazyColumnExample
import com.example.composesample.example.util.ConstValue.Companion.TextStyleExample
import com.example.composesample.example.util.ConstValue.Companion.WebViewIssueExample
import com.example.composesample.ui.base.SetSystemUI

@ExperimentalAnimationApi
class BlogExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contract = ActivityResultContracts.GetContent()

        val callback = ActivityResultCallback<Uri?> { uri ->
            uri?.let {
                val inputVideoPath = getRealPathFromURI(uri, this)
                inputVideoPath?.let {
                    this.executeCommand(inputVideoPath)
                }
            }
        }

        val launcher = registerForActivityResult(contract, callback)

        setContent {
            SetSystemUI()
            BlogExampleCase(
                launcher = launcher
            )
        }
    }
}

@Composable
fun BlogExampleCase(
    launcher: ActivityResultLauncher<String>
) {
    val exampleType = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ExampleButton(
                buttonText = "Lazy Column Keyboard Issue",
                type = LazyColumnExample,
                exampleType = exampleType
            )

            ExampleButton(
                buttonText = "Click Event",
                type = ClickEventExample,
                exampleType = exampleType
            )

            ExampleButton(
                buttonText = "FlexBox Layout Example",
                type = FlexBoxLayoutExample,
                exampleType = exampleType
            )

            ExampleButton(
                buttonText = "Youtube WebView Issue Example",
                type = WebViewIssueExample,
                exampleType = exampleType
            )

            ExampleButton(
                buttonText = "Text Style Example",
                type = TextStyleExample,
                exampleType = exampleType
            )

            ExampleButton(
                buttonText = "Video Encoding Example",
                type = FfmpegExample,
                exampleType = exampleType
            )

            ExampleButton(
                buttonText = "Audio Recorde Example",
                type = AudioRecordeExample,
                exampleType = exampleType
            )
        }

        ExampleCaseUI(
            exampleType = exampleType,
            launcher = launcher,
        ) {
            exampleType.value = ""
        }
    }
}

@Composable
fun ColumnScope.ExampleButton(
    buttonText: String,
    type: String,
    exampleType: MutableState<String>,
    onButtonClick: () -> Unit = {
        exampleType.value = type
    }
) {
    Button(
        modifier = Modifier
            .padding(vertical = 20.dp)
            .align(Alignment.CenterHorizontally),
        onClick = {
            onButtonClick.invoke()

        },
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = buttonText,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExampleCaseUI(
    exampleType: MutableState<String>,
    launcher: ActivityResultLauncher<String>,
    onBackEvent: () -> Unit
) {
    AnimatedVisibility(
        visible = exampleType.value.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)
                .animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { height -> height },
                        animationSpec = tween()
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { height -> height },
                        animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)
                    )
                )
        ) {
            when (exampleType.value) {
                LazyColumnExample -> {
                    LazyColumnIssueUI(onBackEvent)
                }

                ClickEventExample -> {
                    ClickEventUI(onBackEvent)
                }

                FlexBoxLayoutExample -> {
                    FlexBoxUI(onBackEvent)
                }

                WebViewIssueExample -> {
                    WebViewIssueUI(onBackEvent)
                }

                TextStyleExample -> {
                    TextStyleUI(onBackEvent)
                }

                FfmpegExample -> {
                    FfmpegEncodingUI(
                        launcher = launcher,
                        onBackButtonClick = onBackEvent,
                    )
                }

                AudioRecordeExample -> {
                    AudioRecordeUI(onBackEvent)
                }

                else -> {
                    Text(
                        text = "Dummy",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}