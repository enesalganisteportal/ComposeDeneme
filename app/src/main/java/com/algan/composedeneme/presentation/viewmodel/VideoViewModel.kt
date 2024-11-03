package com.algan.composedeneme.presentation.viewmodel

import android.Manifest
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algan.composedeneme.data.model.Video
import com.algan.composedeneme.data.repository.VideoRepository
import com.algan.composedeneme.presentation.state.VideoState
import com.algan.composedeneme.service.ForegroundService
import com.algan.composedeneme.utils.Constants.DEFAULT_REGION
import com.algan.composedeneme.utils.ToastUtil
import com.arthenica.mobileffmpeg.FFmpeg
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
): ViewModel() {

    var videoState = mutableStateOf<VideoState<List<Video>>>(VideoState.Success(emptyList()))

    fun searchVideos(searchTerms: String, maxResults: Int? = null) {
        viewModelScope.launch {
            videoState.value = VideoState.Loading()

            val result = videoRepository.searchVideos(searchTerms, DEFAULT_REGION, maxResults)
            videoState.value = VideoState.Success(result)
            try {
                val result = videoRepository.searchVideos(searchTerms, DEFAULT_REGION, maxResults)
                videoState.value = VideoState.Success(result)
            } catch (e: Exception) {
                videoState.value = VideoState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun startYouTubeToMP3ConversionService(context: Context, youtubeUrl: String) {
        val intent = Intent(context, ForegroundService::class.java).apply {
            action = ForegroundService.ACTION_CONVERT_YOUTUBE_TO_MP3
            putExtra(ForegroundService.EXTRA_YOUTUBE_URL, youtubeUrl)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android O and above, use startForegroundService
            context.startForegroundService(intent)
        } else {
            // For Android versions below O, use startService
            context.startService(intent)
        }
    }



}