package com.example.composesample.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.util.Log

@SuppressLint("NewApi")
fun createDynamicShortcut(context: Context, intent: Intent) {
    val shortcutManager = context.getSystemService(ShortcutManager::class.java)

    val dynamicShortCut = ShortcutInfo.Builder(context, "dynamic_shortcut_id")
        .setShortLabel("dynamicShortCut")
        .setLongLabel("dynamicShortCut Long Label")
        .setIntent(intent)
        .build()

    shortcutManager.addDynamicShortcuts(listOf(dynamicShortCut))
}

@SuppressLint("NewApi")
fun createPinShortcut(context: Context, intent: Intent) {
    val shortcutManager = context.getSystemService(ShortcutManager::class.java)

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