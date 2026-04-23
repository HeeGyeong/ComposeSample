package com.example.composesample.presentation.example.component.system.platform.powersave

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.composesample.util.BackGroundWorker

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PowerSaveModeExampleUI(
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val isPowerSaveMode = remember { mutableStateOf(false) }

    val uniqueWorkTag = "unique_work_tag"
    val uniqueWorkRequest = OneTimeWorkRequest.Builder(BackGroundWorker::class.java)
        .addTag(uniqueWorkTag)
        .build()

    // register/unregister를 Composable의 enter/exit 생명주기와 일치시킨다.
    // OnLifecycleEvent(ON_DESTROY) 방식은 Activity 생존 중 화면 이탈 시
    // observer가 먼저 제거되어 ON_DESTROY가 발화되지 않고 receiver가 누수된다.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                    checkPowerSaveMode(
                        context = ctx!!,
                        onResultCallBack = {
                            Log.d("PowerSaveModeLog", "PowerSave Mode is Changed => $it")
                            isPowerSaveMode.value = it
                        }
                    )
                }
            }
        }

        val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, AppCompatActivity.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        checkPowerSaveMode(
            context = context,
            onResultCallBack = {
                Log.d("PowerSaveModeLog", "First PowerSave Mode : $it")
                isPowerSaveMode.value = it
            }
        )

        // Doze 모드는 절전 모드가 아니다.
        checkDozeMode(
            context = context,
            onResultCallBack = {
                Log.d("PowerSaveModeLog", "check Doze Mode : $it")
            }
        )

        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = {
                                onBackButtonClick.invoke()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Button(onClick = {
                    startBatterySaverSetting(context)
                }) { Text(text = "Change BatterySaverOption") }

                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = "Now PowerSaveMode : ${isPowerSaveMode.value}"
                )


                Button(
                    modifier = Modifier.padding(top = 10.dp),
                    onClick = {
                        if (isPowerSaveMode.value) {
                            WorkManager.getInstance(context).enqueue(uniqueWorkRequest)
                        } else {
                            startBatterySaverSetting(context)
                        }
                    }) {
                    Text(text = "Call Unique WorkManager")
                }
            }
        }
    }
}

// 절전모드 세팅 화면으로 이동
fun startBatterySaverSetting(
    context: Context
) {
    val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
    (context as Activity).startActivity(intent)
}

// 절전 모드 ResultCallback
fun checkPowerSaveMode(
    context: Context,
    onResultCallBack: (Boolean) -> Unit,
) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isPowerSaveMode = powerManager.isPowerSaveMode

    if (isPowerSaveMode) {
        Log.d("checkPowerSaveMode", "Power Save Mode is ON")
    } else {
        Log.d("checkPowerSaveMode", "Power Save Mode is OFF")
    }

    onResultCallBack.invoke(isPowerSaveMode)
}

fun checkDozeMode(
    context: Context,
    onResultCallBack: (Boolean) -> Unit,
) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isDozeMode = powerManager.isDeviceIdleMode

    onResultCallBack.invoke(isDozeMode)
}