package com.example.composesample.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.util.Log
import android.widget.Toast

// ShortcutManager 는 API 25(N_MR1)+, requestPinShortcut 은 API 26(O)+ 부터 지원.
// minSdk 24 환경에서 직접 호출하면 NPE 가 발생하므로 함수 내부에서 가드한다.
fun createDynamicShortcut(context: Context, intent: Intent) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
        Toast.makeText(context, "Dynamic Shortcut 은 Android 7.1(API 25)+ 에서 동작합니다.", Toast.LENGTH_SHORT).show()
        return
    }
    val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return

    val dynamicShortCut = ShortcutInfo.Builder(context, "dynamic_shortcut_id")
        .setShortLabel("dynamicShortCut")
        .setLongLabel("dynamicShortCut Long Label")
        .setIntent(intent)
        .build()

    shortcutManager.addDynamicShortcuts(listOf(dynamicShortCut))
}

fun createPinShortcut(context: Context, intent: Intent) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        Toast.makeText(context, "Pin Shortcut 은 Android 8.0(API 26)+ 에서 동작합니다.", Toast.LENGTH_SHORT).show()
        return
    }
    val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return

    val shortCutCount = shortcutManager.pinnedShortcuts.size
    Log.d("ShortcutLog", "shortCutCount ? $shortCutCount")

    var isExist = false
    if (shortCutCount > 0) {
        for (index in 0 until shortCutCount) {
            Log.d("ShortcutLog", "id ? ${shortcutManager.pinnedShortcuts[index].id}")
            if (shortcutManager.pinnedShortcuts[index].id == "pin_shortcut_id") {
                isExist = true
            }
        }
    }

    // 중복된 shortcut을 방지하기 위함.
    if (isExist) {
        return
    }

    val pinShortCut = ShortcutInfo.Builder(context, "pin_shortcut_id")
        .setShortLabel("pinShortCut")
        .setLongLabel("pinShortCut Long Label")
        .setIntent(intent)
        .build()

    // 핀 숏컷 요청
    val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortCut)
    val successCallback = PendingIntent.getBroadcast(
        context, 0,
        pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE
    )

    shortcutManager.requestPinShortcut(pinShortCut, successCallback.intentSender)
}
