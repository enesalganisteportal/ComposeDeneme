package com.algan.composedeneme.data.remote

import java.net.URLEncoder
import com.algan.composedeneme.data.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class VideoServiceImpl : VideoService {

    private val client = OkHttpClient()

    override suspend fun search(searchTerms: String, region: String, maxResults: Int?): List<Video> {
        return withContext(Dispatchers.IO) {
            val encodedSearch = URLEncoder.encode(searchTerms, "UTF-8")
            val url = "https://youtube.com/results?search_query=$encodedSearch&gl=$region"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute().body?.string() ?: ""
            parseHtml(response)
        }
    }

    private fun parseHtml(response: String): List<Video> {
        val results = mutableListOf<Video>()
        if (!response.contains("ytInitialData")) return results

        val startIndex = response.indexOf("ytInitialData") + "ytInitialData".length + 3
        val endIndex = response.indexOf("};", startIndex) + 1
        val jsonStr = response.substring(startIndex, endIndex)
        val data = JSONObject(jsonStr)

        val contentsArray = data.getJSONObject("contents")
            .getJSONObject("twoColumnSearchResultsRenderer")
            .getJSONObject("primaryContents")
            .getJSONObject("sectionListRenderer")
            .getJSONArray("contents")

        for (i in 0 until contentsArray.length()) {
            val contents = contentsArray.getJSONObject(i)
            if (contents.has("itemSectionRenderer")) {
                val section = contents.getJSONObject("itemSectionRenderer")
                    .getJSONArray("contents")

                for (j in 0 until section.length()) {
                    val video = section.getJSONObject(j)
                    if (video.has("videoRenderer")) {
                        val videoData = video.getJSONObject("videoRenderer")
                        videoData.optJSONObject("lengthText")?.optString("simpleText", "0")?.let { duration ->
                            results.add(Video(
                                id = videoData.optString("videoId", ""),
                                thumbnailUrl = videoData.optJSONObject("thumbnail")
                                    ?.getJSONArray("thumbnails")
                                    ?.getJSONObject(0)
                                    ?.optString("url", "") ?: "",
                                title = videoData.optJSONObject("title")
                                    ?.getJSONArray("runs")
                                    ?.getJSONObject(0)
                                    ?.optString("text", "") ?: "",
                                longDescription = videoData.optJSONObject("descriptionSnippet")
                                    ?.getJSONArray("runs")
                                    ?.optJSONObject(0)
                                    ?.optString("text", "") ?: "",
                                channel = videoData.optJSONObject("longBylineText")
                                    ?.getJSONArray("runs")
                                    ?.getJSONObject(0)
                                    ?.optString("text", "") ?: "",
                                duration = duration,
                                views = videoData.optJSONObject("viewCountText")
                                    ?.optString("simpleText", "0") ?: "",
                                publishTime = videoData.optJSONObject("publishedTimeText")
                                    ?.optString("simpleText", "0") ?: "",
                                urlSuffix = videoData.getJSONObject("navigationEndpoint")
                                    .getJSONObject("commandMetadata")
                                    .getJSONObject("webCommandMetadata")
                                    .optString("url", "") ?: ""
                            ))
                        }
                    }
                }
            }
        }
        return results
    }
}
