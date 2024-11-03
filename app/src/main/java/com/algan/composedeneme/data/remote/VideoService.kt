package com.algan.composedeneme.data.remote

import com.algan.composedeneme.data.model.Video

interface VideoService {
    suspend fun search(searchTerms: String, region: String, maxResults: Int?): List<Video>
}