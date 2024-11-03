package com.algan.composedeneme.data.repository

import com.algan.composedeneme.data.model.Video
import com.algan.composedeneme.data.remote.VideoService

class VideoRepository(private val videoService: VideoService) {
    suspend fun searchVideos(searchTerms: String, region: String, maxResults: Int?): List<Video> {
        return videoService.search(searchTerms, region, maxResults)
    }
}