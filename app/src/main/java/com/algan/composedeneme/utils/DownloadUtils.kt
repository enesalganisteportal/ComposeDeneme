package com.algan.composedeneme.utils

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

object DownloadUtils {
    fun downloadYouTubeAudio(context: Context, youtubeLink: String): String {
        val message = mutableStateOf("")
        // Make sure Python is started
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        val python = Python.getInstance()
        val pythonFile = python.getModule("download_youtube")
        // Use the passed context to get the files directory for the output
        val outputDir = context.filesDir.absolutePath
        try {
            val result = pythonFile.callAttr("download_audio", youtubeLink, outputDir)
            message.value = result.toString()
            //Toast.makeText(context, "Audio downloaded to: ${result.toString()}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            //message.value = e.toString()
            message.value = e.toString()
            //throw e // Rethrow the exception if you want to handle it elsewhere (e.g., showing an error message in UI)
        }
        return message.value
    }

}