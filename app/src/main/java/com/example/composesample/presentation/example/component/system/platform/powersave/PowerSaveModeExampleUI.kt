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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.BackGroundWorker

@Composable
fun PowerSaveModeExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        MainHeader(
            title = "PowerSave Mode Example",
            onBackIconClicked = onBackEvent
        )

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF37474F),
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("절전 모드", fontSize = 12.sp) }
            )
            // 절전 모드는 막는 층 하나일 뿐이다 — 버킷·Doze·사용자 제한·강제 중지까지 앱이 직접 읽어 판정한다
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("백그라운드 제한 진단", fontSize = 12.sp) }
            )
        }

        when (selectedTab) {
            0 -> PowerSaveModeTab()
            1 -> BackgroundRestrictionTab()
        }
    }
}

@Composable
private fun PowerSaveModeTab() {
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