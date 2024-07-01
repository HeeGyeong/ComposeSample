package com.example.composesample.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
        // 백그라운드에서 수행할 작업을 여기에 작성
        val workManagerData = inputData.getString("workManagerData") ?: ""
        Log.d("doWork", "in doWork() : start $workManagerData")


        // Thread Check Log
        Log.d("doWork", "Thread check 1 : ${Thread.currentThread().name}")
        for (item in 0..1) {
            Log.d("doWork", "1 :: doWork() time : $item")
            delay(1000L)
        }
        Log.d("doWork", "1 :: doWork() normal : fin")

        withContext(Dispatchers.IO) {
            Log.d("doWork", "Thread check 2 : ${Thread.currentThread().name}")
            for (item in 0..1) {
                Log.d("doWork", "2 :: doWork() withContext time : $item")
                delay(1000L)
            }
            Log.d("doWork", "2 :: doWork() withContext : fin")

            launch {
                Log.d("doWork", "Thread check 3 : ${Thread.currentThread().name}")
                for (item in 0..1) {
                    Log.d("doWork", "3 :: doWork() launch time : $item")
                    delay(1000L)
                }
                Log.d("doWork", "3 :: doWork() launch : fin")
            }
        }

        withContext(Dispatchers.IO) {
            Log.d("doWork", "Thread check 4 : ${Thread.currentThread().name}")
            for (item in 0..1) {
                Log.d("doWork", "4 :: doWork() withContext time : $item")
                delay(1000L)
            }
            Log.d("doWork", "4 :: doWork() withContext : fin")

            launch {
                Log.d("doWork", "Thread check 5 : ${Thread.currentThread().name}")
                for (item in 0..1) {
                    Log.d("doWork", "5 :: doWork() launch time : $item")
                    delay(1000L)
                }
                Log.d("doWork", "5 :: doWork() launch : fin")
            }

            withContext(Dispatchers.IO) {
                Log.d("doWork", "Thread check 6 : ${Thread.currentThread().name}")
                for (item in 0..1) {
                    Log.d("doWork", "6 :: doWork() withContext time : $item")
                    delay(1000L)
                }
                Log.d("doWork", "6 :: doWork() withContext : fin")

                launch {
                    Log.d("doWork", "Thread check 7 : ${Thread.currentThread().name}")
                    for (item in 0..1) {
                        Log.d("doWork", "7 :: doWork() launch time : $item")
                        delay(1000L)
                    }
                    Log.d("doWork", "7 :: doWork() launch : fin")
                }
            }
        }

        Log.d("doWork", "doWork() : fin")
        return Result.success()
    }
}