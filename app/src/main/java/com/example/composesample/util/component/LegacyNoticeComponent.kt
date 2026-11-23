package com.example.composesample.util.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

/**
 * Compose Material 2 를 일부러 보여 주는 예제(M2 API 자체가 주제)에 붙이는 한 줄 안내.
 * Compose 1.13(2026-11) 릴리스에서 M2 라이브러리가 deprecated · 유지보수 종료로 발표됐다 — 컴파일 경고는 아직 없지만
 * 새 코드가 이 화면을 따라 M2 를 쓰지 않도록 화면에서 알린다(DEPRECATION-113-01).
 */
@Composable
fun M2LegacyNotice(modifier: Modifier = Modifier, color: Color = Color(0xFF757575)) {
    Text(
        text = "Material 2 예제 — Compose 1.13 에서 M2 라이브러리 유지보수 종료가 발표됐다. 새 코드는 Material 3 판 예제를 본다.",
        modifier = modifier,
        color = color,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
}
