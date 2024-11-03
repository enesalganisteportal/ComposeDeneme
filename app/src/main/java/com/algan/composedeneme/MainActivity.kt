package com.algan.composedeneme

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.algan.composedeneme.presentation.ui.VideoListScreen
import com.arthenica.mobileffmpeg.FFmpeg
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoListScreen()
        }
    }
}


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



suspend fun convertMp4ToMp3(context: Context, inputPath: String, outputPath: String) {
    val command = arrayOf("-i", inputPath, "-q:a", "0", "-map", "a", outputPath)
    FFmpeg.execute(command)
}

suspend fun convertAudioToMp3(context: Context, inputPath: String, outputPath: String) {
    val command = arrayOf(
        "-y",
        "-i", inputPath,
        "-vn",
        "-ar", "44100",
        "-ac", "2",
        "-b:a", "192k",
        outputPath
    )
    FFmpeg.execute(command)
}

suspend fun mergeVideoAndAudio(context: Context, videoPath: String, audioPath: String, outputPath: String) {
    val command = arrayOf(
        "-i", videoPath,
        "-i", audioPath,
        "-c:v", "copy",
        "-c:a", "aac",
        outputPath
    )
    FFmpeg.execute(command)
}


fun uriToPath(uri: Uri, context: Context): String {
    val contentResolver = context.contentResolver
    val fileDescriptor = contentResolver.openFileDescriptor(uri, "r", null) ?: return ""

    //val fileName = getFileNameFromUri(uri, context).substringBeforeLast(".")
    // Create a file in your app's private storage directory with a .mp4 extension
    val destinationFile = File(context.filesDir, "selectedFile.mp4")

    FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
        FileOutputStream(destinationFile).use { outputStream ->
            // Copy the contents from the input stream to the output stream
            inputStream.copyTo(outputStream)
        }
    }
    // Return the path to the newly created file in your app's private storage
    return destinationFile.absolutePath
}


fun saveToDownloads(
    context: Context,
    outputPath: String,
    fileName: String,
    fileExtension: String,
    folderName: String = "Converted Files"
) {
    val resolver = context.contentResolver
    val outputFileName = "$fileName.$fileExtension"
    val mimeType = when (fileExtension.lowercase()) {
        "mp3" -> "audio/mpeg"
        "mp4" -> "video/mp4"
        else -> "application/octet-stream"
    }

    val file = File(outputPath)

   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android Q and above: Use MediaStore to save in Downloads directory
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}${File.separator}$folderName"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        val outputUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        try {
            outputUri?.let { uri ->
                resolver.openOutputStream(uri).use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }
                // Cleanup after saving
                deleteTemporaryFiles(context, outputPath, uriToPath(uri, context))
            } ?: false
        } catch (e: IOException) {
            Log.e("SaveToDownloads", "Error occurred: ${e.message}")
            false
        }
    } else {
        // For older versions: Use direct file saving to Downloads folder
        val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = File(downloadsPath, folderName).apply { mkdirs() }
        val targetFile = File(targetDir, outputFileName)

        try {
            FileInputStream(file).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            // Cleanup after saving
            deleteTemporaryFiles(context, outputPath, targetFile.absolutePath)
            true
        } catch (e: IOException) {
            Log.e("SaveToDownloads", "Error occurred: ${e.message}")
            false
        }
    }
}



// New function to delete temporary files
fun deleteTemporaryFiles(context: Context, outputPath: String, inputPath: String) {

    val outputMp3File = File(outputPath)
    //Log.e("DeleteFile", outputMp3File.toString())
    if (outputMp3File.exists()) {
        outputMp3File.delete()
    }

    //("deleteUriPath", inputPath)
    val selectedMp4File = File(inputPath)
    //Log.e("DeleteFile", selectedMp4File.toString())
    if (selectedMp4File.exists()) {
        selectedMp4File.delete()
    }
}
// Use the getFileNameFromUri function you have to get the original file name.


fun getFileNameFromUri(uri: Uri, context: Context): String {
    var fileName = "Unknown"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }
    return fileName.substringBeforeLast(".")
}


fun showDownloadNotification(context: Context, outputFileName: String) {
    val notificationId = 1 // Identifier for the notification
    val channelId = "download_channel" // Identifier for the notification channel

    // Intent to open the system's Downloads app
    val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)

    // PendingIntent that will start the intent when the notification is tapped
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Build the notification
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.stat_sys_download_done) // System icon for download done
        .setContentTitle("Conversion Completed")
        .setContentText(outputFileName)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    // Display the notification
    // Display the notification
    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context, // Use context instead of this
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, handle accordingly
            return@with
        }
        notify(notificationId, notification)
    }

}
