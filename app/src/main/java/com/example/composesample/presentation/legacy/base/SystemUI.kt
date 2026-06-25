package com.example.composesample.presentation.legacy.base

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

/**
 * 상태바/내비게이션 바 색상 지정 (legacy 데모용).
 *
 * 지원 종료된 accompanist-systemuicontroller 를 제거하고 플랫폼 Window API 로 대체했다.
 * window.statusBarColor / navigationBarColor 직접 지정은 API 35(에지투에지 강제)에서
 * deprecated 이지만, 이 legacy 데모는 "검정 상태바 + 노랑 내비바" 시연이 목적이라
 * 동작을 그대로 보존한다. (에지투에지 전환은 시연 의도를 바꾸므로 적용하지 않음)
 */
@Suppress("DEPRECATION")
@Composable
fun SetSystemUI() {
    val view = LocalView.current
    if (view.isInEditMode) return

    val window = view.context.findActivity()?.window ?: return
    // Top System UI
    window.statusBarColor = Color.Black.toArgb()
    // Bottom System UI
    window.navigationBarColor = Color.Yellow.toArgb()
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
