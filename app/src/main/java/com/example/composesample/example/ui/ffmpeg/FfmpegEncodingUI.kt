package com.example.composesample.example.ui.ffmpeg

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 전반적인 코드 및 주석 추가 수정 필요함.
 *
 * 코드가 아직 정리가 되어있지 않으며, 단순 기능만 동작하도록 구현되어있는 상태의 예제.
 *
 * 24.01.23
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FfmpegEncodingUI(
    launcher: ActivityResultLauncher<String>,
    onBackButtonClick: () -> Unit
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
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

        item {
            Button(onClick = {
                launcher.launch("video/*")
            }) {
                Text(
                    text = "Video Encoding",
                )
            }
        }
    }
}

fun Context.sharedLowQualityVideo(file: File) {
    val gifUri = FileProvider.getUriForFile(this, "com.example.composesample", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, gifUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(shareIntent)
}

fun Context.executeCommand(inputFilePath: String) {
    val suffixData = getCurrentTime()
    val outputFile = File(getExternalFilesDir(null), "output-filename-$suffixData.mp4")
    val outPutFilePath = outputFile.absolutePath

    val inputFfmpegCommand = "-y -i $inputFilePath -r 20 $outPutFilePath"

    FFmpegKit.executeAsync(inputFfmpegCommand) { session ->
        if (ReturnCode.isSuccess(session.returnCode)) {
            sharedLowQualityVideo(outputFile)
        }
    }
}

private fun getCurrentTime(): String? {
    val currentDate = Date()
    val dateFormat = SimpleDateFormat("HH-mm-ss")
    return dateFormat.format(currentDate)
}

fun getRealPathFromURI(uri: Uri, context: Context): String? {
    val returnCursor = context.contentResolver.query(uri, null, null, null, null)
    returnCursor!!.moveToFirst()

    val file = File(context.filesDir, "shared_temp_file")

    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        var read = 0
        val maxBufferSize = 1 * 1024 * 1024
        val bytesAvailable = inputStream?.available() ?: 0
        val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
        val buffers = ByteArray(bufferSize)
        while (inputStream?.read(buffers).also {
                if (it != null) {
                    read = it
                }
            } != -1) {
            outputStream.write(buffers, 0, read)
        }
        inputStream?.close()
        outputStream.close()

    } catch (e: java.lang.Exception) {
        Log.e("Exception", e.message!!)
    }

    returnCursor.close()
    return file.path
}