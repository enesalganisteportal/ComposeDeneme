package com.algan.composedeneme.data.model

sealed class DownloadState {
    object Idle : DownloadState()
    object Loading : DownloadState()
    data class Success(val fileName: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}
