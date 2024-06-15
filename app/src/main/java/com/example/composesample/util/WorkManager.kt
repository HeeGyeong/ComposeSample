package com.example.composesample.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent

class BackGroundWorker(
    val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        // 백그라운드에서 수행할 작업을 여기에 작성
        val workManagerData = inputData.getString("workManagerData") ?: ""
        Log.d("doWork", "in doWork() : start $workManagerData")

        for (item in 0..30) {
            Log.d("doWork", "doWork() time : $item")
            delay(1000L)
        }

        Log.d("doWork", "doWork() : fin")

        return Result.success()
    }
}