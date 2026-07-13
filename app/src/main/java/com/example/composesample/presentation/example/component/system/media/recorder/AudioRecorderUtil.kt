package com.example.composesample.presentation.example.component.system.media.recorder

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun pauseMediaRecorder(
    mediaRecorder: MutableState<MediaRecorder?>,
) {
    mediaRecorder.value?.apply {
        pause()
    }
}

fun resumeMediaRecorder(
    mediaRecorder: MutableState<MediaRecorder?>,
) {
    mediaRecorder.value?.apply {
        resume()
    }
}
fun finishMediaRecorder(
    mediaRecorder: MutableState<MediaRecorder?>,
) {
    mediaRecorder.value?.apply {
        stop()
        reset()
        release()
        mediaRecorder.value = null
    }
}

fun startMediaRecorder(
    context: Context,
    outputFile: MutableState<File?>,
    mediaRecorder: MutableState<MediaRecorder?>,
) {
    try {
        // API 31(S)+ 부터 context-aware constructor 권장. 이하 단말은 deprecated no-arg 사용.
        mediaRecorder.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        outputFile.value = createOutputFile(context)

        mediaRecorder.value?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.value?.absolutePath)
            prepare()
            start()
        }
    } catch (e: IOException) {
        Log.e("AudioRecorder", "녹음 시작 실패: ${e.message}", e)
    }
}

fun createOutputFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(null)
    return File.createTempFile(
        "AUDIO_${timeStamp}_",
        ".m4a",
        storageDir
    )
}

fun closeMediaPlayer(
    mediaPlayer: MutableState<MediaPlayer?>,
) {
    if (mediaPlayer.value != null) {
        mediaPlayer.value!!.release()
        mediaPlayer.value = null
    }
}

fun startMediaPlayer(
    context: Context,
    mediaPlayer: MutableState<MediaPlayer?>,
    outputFile: MutableState<File?>,
    isPlaying: MutableState<Boolean>,
) {
    mediaPlayer.value = MediaPlayer
        .create(context, Uri.parse(outputFile.value?.absolutePath))
        .apply {
            setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            )
            start()
        }

    mediaPlayer.value?.setOnCompletionListener {
        isPlaying.value = false
    }
}
