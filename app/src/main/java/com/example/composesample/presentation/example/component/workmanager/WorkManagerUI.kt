package com.example.composesample.presentation.example.component.workmanager

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.composesample.util.BackGroundWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkManagerUI(
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val map = mapOf("workManagerData" to "workManagerData")
    val data = Data.Builder().putAll(map).build()
    val workerRequestId = remember { mutableStateOf<UUID?>(null) }

    val uniqueWorkTag = "unique_work_tag"
    val uniqueWorkRequest = OneTimeWorkRequest.Builder(BackGroundWorker::class.java)
        .setInputData(data)
        .addTag(uniqueWorkTag)
        .build()

    val uniqueReplaceWorkTag = "unique_replace_work_tag"
    val uniqueReplaceWorkRequest = OneTimeWorkRequest.Builder(BackGroundWorker::class.java)
        .setInputData(data)
        .setInitialDelay(5, TimeUnit.SECONDS)
        .addTag(uniqueReplaceWorkTag)
        .build()

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
            }) {
                Text(text = "Stop Unique WorkManager")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                WorkManager.getInstance(context).enqueueUniqueWork(
                    uniqueReplaceWorkTag,
                    ExistingWorkPolicy.REPLACE,
                    uniqueReplaceWorkRequest
                )
            }) {
                Text(text = "Call Unique Replace WorkManager")
            }
        }
    }
}