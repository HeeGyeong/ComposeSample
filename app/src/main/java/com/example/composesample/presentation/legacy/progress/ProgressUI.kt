package com.example.composesample.presentation.legacy.progress

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.R

@ExperimentalAnimationApi
@Composable
fun Progress() {
    val isVisible = remember { mutableStateOf(false) }
    val isEnable = remember { mutableStateOf(true) }
    val result = remember { mutableStateOf("") }
    val waitText = stringResource(id = R.string.progress_wait)

    val handler = Handler(Looper.getMainLooper())

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                isVisible.value = true
                isEnable.value = false
                result.value = waitText

                Thread {
                    Thread.sleep(3 * 1000)

                    handler.post {
                        isVisible.value = false
                        result.value = "Task finished."
                        isEnable.value = true
                    }
                }.start()
            },
            enabled = isEnable.value
        ) {
            Text(text = "Do Task")
        }

        AnimatedVisibility(isVisible.value) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                CircularProgressIndicator()
                CircularProgressIndicator(
                    color = Color(0xFFE30022)
                )
            }
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))
        Text(
            text = result.value,
            fontSize = 20.sp
        )


        val isLineEnable = remember { mutableStateOf(true) }
        val resultLine = remember { mutableStateOf("") }
        val progressLine = remember { mutableStateOf(0.0f) }

        Button(
            onClick = {
                isLineEnable.value = false
                resultLine.value = "Task running...."

                Thread {
                    while (progressLine.value < 1) {
                        Thread.sleep(1 * 100)
                        progressLine.value = progressLine.value + 0.01F

                        handler.post {
                            val finished = (progressLine.value * 100).toInt()
                            resultLine.value = "Done $finished of 100 tasks"
                        }
                    }
                }.start()
            },
            enabled = isLineEnable.value
        ) {
            Text(text = "Run Tasks")
        }

        LinearProgressIndicator(
            progress = progressLine.value
        )

        Text(
            text = resultLine.value,
            fontSize = 20.sp
        )

        val isEnableCircle = remember { mutableStateOf(true) }
        val resultCircle = remember { mutableStateOf("") }
        val progressCircle = remember { mutableStateOf(0.0F) }

        Button(
            onClick = {
                progressCircle.value = 0.0F
                isEnableCircle.value = false
                resultCircle.value = "Task running...."

                Thread {
                    while (progressCircle.value < 1) {
                        Thread.sleep(1 * 100)
                        progressCircle.value = progressCircle.value + 0.01F

                        if (progressCircle.value >= 1F) {
                            isEnableCircle.value = true
                        }
                        val finished = (progressCircle.value * 100).toInt()
                        resultCircle.value = "Done $finished of 100 tasks"
                    }
                }.start()
            },
            enabled = isEnableCircle.value
        ) {
            Text(text = "Run Tasks")
        }

        CircularProgressIndicator(
            progress = progressCircle.value,
            Modifier
                .rotate(-90F)
                .size(200.dp),
            color = Color(0xFF7BB661),
            strokeWidth = 12.dp
        )

        Text(
            text = resultCircle.value,
            fontSize = 20.sp
        )
    }
}

@ExperimentalAnimationApi
@Preview("progress")
@Composable
fun ProgressPreview() {
    Progress()
}