package com.example.composesample.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class BackGroundWorker(
    val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        return try {
            // retry 로직을 위해 try-catch 문을 사용.
            // Result.success 내용 윗 부분을 함수로 빼내어 사용하는 것이 가독성이 좋음.
            // 백그라운드에서 수행할 작업을 여기에 작성
            val workManagerData = inputData.getString("workManagerData") ?: ""
            Log.d("doWork", "start doWork() : $workManagerData")

            var totalProgress = 0

            // Thread Check Log
            Log.d("doWork", "Thread check 1 : ${Thread.currentThread().name}")
            for (item in 0..1) {
                delay(1000L)
                totalProgress++
                setProgressAsync(workDataOf("progress" to totalProgress))
            }

            totalProgress = 10

            withContext(Dispatchers.IO) {
                Log.d("doWork", "Thread check 2 : ${Thread.currentThread().name}")
                for (item in 0..1) {
                    delay(1000L)
                    totalProgress++
                    setProgressAsync(workDataOf("progress" to totalProgress))
                }
                totalProgress = 200

                launch {
                    Log.d("doWork", "Thread check 3 : ${Thread.currentThread().name}")
                    for (item in 0..1) {
                        delay(1000L)
                        totalProgress++
                        setProgressAsync(workDataOf("progress" to totalProgress))
                    }
                    totalProgress = 3000
                }
            }

            withContext(Dispatchers.IO) {
                Log.d("doWork", "Thread check 4 : ${Thread.currentThread().name}")
                for (item in 0..1) {
                    delay(1000L)
                    totalProgress++
                    setProgressAsync(workDataOf("progress" to totalProgress))
                }
                totalProgress = 40000

                launch {
                    Log.d("doWork", "Thread check 5 : ${Thread.currentThread().name}")

                    // 4의 for문
                    for (item in 0..1) {
                        delay(1000L)
                        totalProgress++
                        setProgressAsync(workDataOf("progress" to totalProgress))
                    }
                    totalProgress = 500000
                }

                withContext(Dispatchers.IO) {
                    Log.d("doWork", "Thread check 6 : ${Thread.currentThread().name}")

                    // 5의 for문
                    for (item in 0..1) {
                        delay(1000L)
                        totalProgress++
                        setProgressAsync(workDataOf("progress" to totalProgress))
                    }
                    totalProgress = 6000000

                    launch {
                        Log.d("doWork", "Thread check 7 : ${Thread.currentThread().name}")
                        for (item in 0..1) {
                            delay(1000L)
                            totalProgress++
                            setProgressAsync(workDataOf("progress" to totalProgress))
                        }
                    }
                }
            }

            // withContext(Dispatchers.IO) 안에 있는 launch 와 withContext(Dispatchers.IO)는 병렬로 수행된다.
            Result.success(workDataOf("finalProgress" to totalProgress))
        } catch (e: Exception) {
            Result.retry()
        }
    }
}