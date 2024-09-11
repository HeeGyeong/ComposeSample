package com.example.composesample.presentation.example.component.lottie

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter.State
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.request.onAnimationEnd
import coil.request.onAnimationStart
import coil.request.repeatCount
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.composesample.R
import com.example.composesample.util.noRippleClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LottieExampleUI(
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var isReplay by remember { mutableStateOf(false) }

    // Lottie Value
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_sample))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = 1,
        speed = 1f,
        restartOnPlay = true
    )

    // Gif Value -> 내부 함수만으로는 클릭 시 재생이 안된다.
    var replayKey by remember { mutableStateOf(false) }
    val imageRequest = remember(replayKey) {
        ImageRequest.Builder(context)
            .data(R.drawable.gif_lottie_sample)
            .repeatCount(0) // GIF를 한 번만 재생하도록 설정
            .crossfade(true)
            .decoderFactory(
                if (SDK_INT >= 28) {
                    ImageDecoderDecoder.Factory()
                } else {
                    GifDecoder.Factory()
                }
            )
            .onAnimationStart {
                Log.d("CoilTransform", "onAnimationStart")
            }
            .onAnimationEnd {
                Log.d("CoilTransform", "onAnimationEnd")
            }
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        transform = { data ->
            if (data is State.Success) {
                Log.d("CoilTransform", "result : ${data.result.request}")
            }
            data
        }
    )

    val painter2 = rememberAsyncImagePainter(
        model = imageRequest,
        transform = { data ->
            if (data is State.Success) {
                Log.d("CoilTransform", "result : ${data.result.request}")
            }
            data
        }
    )

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

        item {
            Column {
                LottieAnimation(
                    modifier = Modifier.size(100.dp),
                    composition = composition,
                    iterations = Int.MAX_VALUE
                )

                Spacer(modifier = Modifier.height(20.dp))

                LottieAnimation(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            if (progress == 1.0f) {
                                isReplay = true
                            }
                            isPlaying = true
                        },
                    composition = composition,
                    progress = {
                        if (progress == 1.0f) {
                            isPlaying = false
                        }

                        // lottie가 끝났을 때, 한번 더 클릭하면 progress가 1.0에서 0.0으로 초기화 되는 동작을 수행한다.
                        if (isReplay) {
                            isPlaying = true
                            isReplay = false
                        }
                        progress
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (replayKey) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .noRippleClickable {
                                replayKey = !replayKey
                            },
                    )
                } else {
                    Image(
                        painter = painter2,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .noRippleClickable {
                                replayKey = !replayKey
                            },
                    )
                }
            }
        }
    }
}