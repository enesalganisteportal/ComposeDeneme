package com.algan.composedeneme.presentation.state


sealed class VideoState<T> {
    class Loading<T> : VideoState<T>()  // Make Loading generic
    data class Success<T>(val data: T) : VideoState<T>()
    data class Error<T>(val message: String) : VideoState<T>()  // Make Error generic
}