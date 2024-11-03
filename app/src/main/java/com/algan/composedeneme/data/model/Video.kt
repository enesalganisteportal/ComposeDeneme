package com.algan.composedeneme.data.model

data class Video(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val longDescription: String?,
    val channel: String,
    val duration: String,
    val views: String,
    val publishTime: String,
    val urlSuffix: String
)