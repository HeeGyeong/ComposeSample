package com.example.composesample.example.ui.powersave

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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.example.composesample.example.util.OnLifecycleEvent

private lateinit var powerSaveModeReceiver: BroadcastReceiver

/**
 * 샘플 추가 필요함.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PowerSaveModeExampleUI(
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val isPowerSaveMode = remember { mutableStateOf(false) }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                context.unregisterReceiver(powerSaveModeReceiver)
            }
            else -> { }
        }
    }

    LaunchedEffect(key1 = Unit, block = {
        // 절전모드 체크
        powerSaveModeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // 절전 모드 상태가 변경될 때 호출됩니다.
                if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                    checkPowerSaveMode(
                        context = context!!,
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
            context.registerReceiver(powerSaveModeReceiver, filter, AppCompatActivity.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(powerSaveModeReceiver, filter)
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
    })


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
                            Icon(Icons.Filled.ArrowBack, contentDescription = "")
                        }
                    }
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
    val isPowerSaveMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        powerManager.isPowerSaveMode
    } else {
        false
    }

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
    val isDozeMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager.isDeviceIdleMode
    } else {
        false
    }

    onResultCallBack.invoke(isDozeMode)
}