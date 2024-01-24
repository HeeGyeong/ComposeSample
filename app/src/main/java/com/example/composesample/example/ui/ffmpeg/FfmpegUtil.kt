package com.example.composesample.example.ui.ffmpeg

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

/**
 * FFmpeg에 Command Line을 입력하는 부분
 */
@SuppressLint("SimpleDateFormat")
fun Context.executeCommand(inputFilePath: String) {
    val suffixData = SimpleDateFormat("HH-mm-ss").format(Date())
    val outputFile = File(getExternalFilesDir(null), "compose-sample-output-$suffixData.mp4")
    val outPutFilePath = outputFile.absolutePath

    val inputFfmpegCommand = "-y -i $inputFilePath -r 20 $outPutFilePath"

    FFmpegKit.executeAsync(inputFfmpegCommand) { session ->
        if (ReturnCode.isSuccess(session.returnCode)) {
            sharedLowQualityVideo(outputFile)
        }
    }
}

/**
 * Video File을 공유하는 Intent
 */
fun Context.sharedLowQualityVideo(file: File) {
    val videoUri = FileProvider.getUriForFile(this, "com.example.composesample", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, videoUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(shareIntent)
}

/**
 * 화질을 변경한 video file Path를 구하는 로직
 */
fun getRealPathFromURI(uri: Uri, context: Context): String? {
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

    } catch (e: Exception) {
        Log.e("Exception", e.message!!)
    }

    return file.path
}