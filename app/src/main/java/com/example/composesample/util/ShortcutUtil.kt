package com.example.composesample.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager

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