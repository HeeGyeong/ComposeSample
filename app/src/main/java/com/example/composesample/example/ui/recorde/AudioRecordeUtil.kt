package com.example.composesample.example.ui.recorde

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.compose.runtime.MutableState
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun pauseMediaRecorde(
    mediaRecorder: MutableState<MediaRecorder?>,
) {
    mediaRecorder.value?.apply {
        pause()
    }
}

fun resumeMediaRecorde(
    mediaRecorder: MutableState<MediaRecorder?>,
) {
    mediaRecorder.value?.apply {
        resume()
    }
}
fun finishMediaRecorde(
    mediaRecorder: MutableState<MediaRecorder?>,
) {
    mediaRecorder.value?.apply {
        stop()
        reset()
        release()
        mediaRecorder.value = null
    }
}

fun startMediaRecorde(
    context: Context,
    outputFile: MutableState<File?>,
    mediaRecorder: MutableState<MediaRecorder?>,
) {
    try {
        mediaRecorder.value = MediaRecorder()
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
        e.printStackTrace()
    }
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

fun createOutputFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(null)
    return File.createTempFile(
        "AUDIO_${timeStamp}_",
        ".m4a",
        storageDir
    )
}