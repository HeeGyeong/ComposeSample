package com.example.composesample.presentation.example.component.ui.graphics

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/**
 * graphics 예제들이 함께 쓰는 화면 픽셀 읽기(Gooey 텍스트 모핑 · drawMesh 리본).
 * PixelCopy 로 창에 실제로 그려진 최종 픽셀(블러 · 색 필터 · 메시 · 합성이 모두 끝난 것)을 읽는다.
 */

// debug 는 HotSwan 때문에 minSdk 26 이라 ObsoleteSdkInt 로 보이지만 release(minSdk 24)에는 필요한 검사다
@SuppressLint("ObsoleteSdkInt")
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
internal fun pixelCopyAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

internal class CapturedPixels(val width: Int, val height: Int, val pixels: IntArray)

/** boundsInWindow 는 창 기준이라 액티비티 창의 PixelCopy 좌표와 같다 */
@SuppressLint("ObsoleteSdkInt") // debug 는 HotSwan 때문에 minSdk 26 — release(minSdk 24)에는 필요한 표시
@RequiresApi(Build.VERSION_CODES.O)
internal object WindowCapture {
    suspend fun read(window: Window, bounds: Rect): CapturedPixels? {
        val rect = android.graphics.Rect(
            bounds.left.roundToInt(),
            bounds.top.roundToInt(),
            bounds.right.roundToInt(),
            bounds.bottom.roundToInt()
        )
        if (rect.width() <= 0 || rect.height() <= 0) return null
        return suspendCancellableCoroutine { continuation ->
            val bitmap = createBitmap(rect.width(), rect.height())
            PixelCopy.request(
                window, rect, bitmap,
                { result ->
                    if (result != PixelCopy.SUCCESS) {
                        bitmap.recycle()
                        continuation.resume(null)
                        return@request
                    }
                    val pixels = IntArray(bitmap.width * bitmap.height)
                    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                    val captured = CapturedPixels(bitmap.width, bitmap.height, pixels)
                    bitmap.recycle()
                    continuation.resume(captured)
                },
                Handler(Looper.getMainLooper())
            )
        }
    }

    /** 읽은 사각형을 잉크 색 기준으로 분석(픽셀 수가 많아 기본 디스패처에서) */
    suspend fun analyze(window: Window, bounds: Rect, inkArgb: Int): InkStats? {
        val captured = read(window, bounds) ?: return null
        return withContext(Dispatchers.Default) {
            InkAnalysis.analyze(captured.pixels, captured.width, captured.height, inkArgb)
        }
    }
}

/** 측정할 레이어의 창 안 위치 — LazyColumn 뷰포트에 잘리지 않고 다 보일 때만 잰다 */
internal class MeasuredBounds {
    var bounds: Rect = Rect.Zero
    var size: IntSize = IntSize.Zero

    val fullyVisible: Boolean
        get() = size.width > 0 && bounds.width >= size.width - 1 && bounds.height >= size.height - 1
}

internal fun Modifier.measured(target: MeasuredBounds): Modifier = this.onGloballyPositioned {
    target.bounds = it.boundsInWindow()
    target.size = it.size
}
