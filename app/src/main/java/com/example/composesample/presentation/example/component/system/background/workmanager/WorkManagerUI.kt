package com.example.composesample.presentation.example.component.system.background.workmanager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.composesample.util.BackGroundWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkManagerUI(
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleContext = LocalLifecycleOwner.current
    val map = mapOf("workManagerData" to "워커에서 사용할 데이터 입니다.")
    val data = Data.Builder().putAll(map).build()
    val workerRequestId = remember { mutableStateOf<UUID?>(null) }

    val uniqueWorkTag = "unique_work_tag"
    val uniqueWorkRequest = OneTimeWorkRequest.Builder(BackGroundWorker::class.java)
        .setInputData(data)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            WorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .addTag(uniqueWorkTag)
        .build()

    // 반복해서 작업을 수행하는 WorkRequest
    val periodicWorkRequest =
        PeriodicWorkRequestBuilder<BackGroundWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

    val uniqueReplaceWorkTag = "unique_replace_work_tag"
    val uniqueReplaceWorkRequest = OneTimeWorkRequest.Builder(BackGroundWorker::class.java)
        .setInputData(data)
        .setInitialDelay(2, TimeUnit.SECONDS)
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            WorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .addTag(uniqueReplaceWorkTag)
        .build()

    var workerResponseText by remember { mutableStateOf("Stop Worker") }

    WorkManager.getInstance(context).getWorkInfoByIdLiveData(uniqueReplaceWorkRequest.id)
        .observe(lifecycleContext) { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt("progress", 0)

                        val progressText = when {
                            progress < 10 -> "Normal : $progress"
                            progress < 200 -> "first in IO : $progress"
                            progress < 3000 -> "first in IO in launch : $progress"
                            progress < 40000 -> "second in IO : $progress"
                            progress < 500000 -> "second in IO in launch : $progress"
                            progress < 6000000 -> "second in IO in IO : $progress"
                            else -> "second in IO in IO in launch : $progress"
                        }

                        workerResponseText = "Progress: $progressText"
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        val finalProgress = workInfo.outputData.getInt("finalProgress", 0)
                        workerResponseText = "Work completed. Final progress: $finalProgress"
                    }

                    WorkInfo.State.FAILED -> {
                        workerResponseText = "Work failed"
                    }

                    else -> {
                        // 다른 상태 처리
                        workerResponseText = "else  case"
                    }
                }
            }
        }

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
            Button(onClick = {
                // 매번 생성.
                val workRequest = OneTimeWorkRequestBuilder<BackGroundWorker>()
                    .setInputData(data)
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }) {
                Text(text = "Call WorkManager")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                WorkManager.getInstance(context).enqueue(uniqueWorkRequest)
                workerRequestId.value = uniqueWorkRequest.id
            }) {
                Text(text = "Call Unique WorkManager")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                workerRequestId.value?.let { WorkManager.getInstance(context).cancelWorkById(it) }
//                WorkManager.getInstance(context).cancelAllWorkByTag(uniqueReplaceWorkTag)
            }) {
                Text(text = "Stop Unique WorkManager")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "uniqueWorkNameSample",
                    ExistingWorkPolicy.REPLACE,
                    uniqueReplaceWorkRequest
                )
//                WorkManager.getInstance(context).cancelUniqueWork("uniqueWorkNameSample")
            }) {
                Text(text = "Call Unique Replace WorkManager")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = workerResponseText,
                fontSize = 16.sp,
                color = Color.White
            )

            Button(onClick = {
                WorkManager.getInstance(context)
                    .beginWith(uniqueReplaceWorkRequest)
                    .then(uniqueReplaceWorkRequest)
                    .enqueue()
            }) {
                Text(text = "The operation has not been confirmed. This is just sample code.")
            }
        }
    }
}