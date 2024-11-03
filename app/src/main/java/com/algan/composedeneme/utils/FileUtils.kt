package com.algan.composedeneme.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.algan.composedeneme.deleteTemporaryFiles
import com.algan.composedeneme.uriToPath
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


object FileUtils {
    fun getMp3OutputPath(inputPath: String, context: Context): String {
        val fileNameWithoutExtension = File(inputPath).nameWithoutExtension.replace("_audio", "")
        return "${context.filesDir.absolutePath}/$fileNameWithoutExtension.mp3"
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

    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }

    fun getFileName(path: String): String {
        return File(path).name
    }
}