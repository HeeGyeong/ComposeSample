package com.example.composesample.presentation.example.component.system.media.recorder

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.composesample.util.noRippleClickable
import com.example.composesample.util.noRippleSingleClickable
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioRecorderUI(
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val isRecoding = remember { mutableStateOf(false) }
    val mediaRecorder = remember { mutableStateOf<MediaRecorder?>(null) }
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val outputFile = remember { mutableStateOf<File?>(null) }
    val isPlaying = remember { mutableStateOf(false) }
    val isPause = remember { mutableStateOf(false) }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRecoding.value) {
                            "녹음 종료"
                        } else {
                            if (outputFile.value != null) {
                                "저장 된 파일 제거"
                            } else {
                                "녹음 시작"
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // 녹음 중이면 녹음 종료
                    if (isRecoding.value) {
                        Icon(
                            modifier = Modifier
                                .size(36.dp)
                                .noRippleSingleClickable {
                                    isRecoding.value = false
                                    finishMediaRecorder(mediaRecorder)
                                },
                            imageVector = Icons.Filled.Check,
                            contentDescription = ""
                        )
                    }
                    // 눅음 중이 아니면 녹음 시작
                    else {
                        Icon(
                            modifier = Modifier
                                .size(36.dp)
                                .noRippleSingleClickable {
                                    // 이미 저장 된 파일이 있으면 파일 제거
                                    if (outputFile.value != null) {
                                        outputFile.value!!.deleteOnExit()

                                        outputFile.value = null
                                        isRecoding.value = false
                                        isPlaying.value = false

                                        // 녹음 종료
                                        finishMediaRecorder(mediaRecorder)

                                        // 플레이어 종료.
                                        closeMediaPlayer(mediaPlayer)
                                    } else {
                                        // 권한 체크 필요함.
                                        isRecoding.value = true

                                        startMediaRecorder(
                                            context = context,
                                            mediaRecorder = mediaRecorder,
                                            outputFile = outputFile
                                        )
                                    }
                                },
                            imageVector = if (outputFile.value != null) {
                                Icons.Filled.Refresh
                            } else {
                                Icons.Filled.Call
                            },
                            contentDescription = ""
                        )
                    }
                }

                if (isRecoding.value) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPause.value) {
                                "녹음 재개"
                            } else {
                                "녹음 일시 정지"
                            }
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            modifier = Modifier
                                .size(36.dp)
                                .noRippleSingleClickable {
                                    if (isPause.value) {
                                        isPause.value = false
                                        resumeMediaRecorder(
                                            mediaRecorder = mediaRecorder
                                        )
                                    } else {
                                        isPause.value = true
                                        pauseMediaRecorder(
                                            mediaRecorder = mediaRecorder
                                        )
                                    }
                                },
                            imageVector = if (isPause.value) {
                                Icons.Filled.PlayArrow
                            } else {
                                Icons.Filled.Edit
                            },
                            contentDescription = ""
                        )
                    }

                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable {
                            if (isPlaying.value) {
                                isPlaying.value = false

                                closeMediaPlayer(mediaPlayer)
                            } else {
                                if (!isRecoding.value && outputFile.value != null) {
                                    isPlaying.value = true

                                    startMediaPlayer(
                                        context = context,
                                        mediaPlayer = mediaPlayer,
                                        outputFile = outputFile,
                                        isPlaying = isPlaying,
                                    )
                                } else {
                                    if (outputFile.value != null) {
                                        Log.d("MediaPlayerLog", "녹음 중에는 재생할 수 없습니다.")
                                    }
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (!isPlaying.value) {
                            "녹음 된 음성 재생"
                        } else {
                            "재생 종료"
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        modifier = Modifier
                            .size(44.dp),
                        imageVector = if (!isPlaying.value) {
                            Icons.Filled.PlayArrow
                        } else {
                            Icons.Filled.Clear
                        },
                        contentDescription = ""
                    )
                }
            }
        }
    }
}