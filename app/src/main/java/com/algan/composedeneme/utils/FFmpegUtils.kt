package com.algan.composedeneme.utils

import com.arthenica.mobileffmpeg.FFmpeg

object FFmpegUtils {
    fun convertAudioToMp3(inputPath: String, outputPath: String) {
        val command = arrayOf("-y", "-i", inputPath, "-vn", "-ar", "44100", "-ac", "2", "-b:a", "192k", outputPath)
        FFmpeg.execute(command)
    }
}