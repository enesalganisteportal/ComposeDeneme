package com.algan.composedeneme.utils

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

object DownloadUtils {
    fun downloadYouTubeAudio2(context: Context, youtubeLink: String): String {
        val message = mutableStateOf("")
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        val python = Python.getInstance()
        val pythonFile = python.getModule("download_youtube")
        val outputDir = context.filesDir.absolutePath
        try {
            val result = pythonFile.callAttr("download_audio", youtubeLink, outputDir)
            message.value = result.toString()
        } catch (e: Exception) {
            message.value = e.toString()
        }
        return message.value
    }

    fun downloadYouTubeAudio(context: Context, youtubeLink: String, progressCallback: (Float) -> Unit) :String{
        val message = mutableStateOf("")
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        val python = Python.getInstance()
        val pythonFile = python.getModule("download_youtube")
        val outputDir = context.filesDir.absolutePath

        val progressCallback: (Float) -> Unit = { progress ->
            progressCallback.invoke(progress)
        }

        try {
            val result = pythonFile.callAttr("download_audio", youtubeLink, outputDir, progressCallback)
            message.value = result.toString()
        } catch (e: Exception) {
            message.value = e.toString()
        }
        return message.value
    }

}