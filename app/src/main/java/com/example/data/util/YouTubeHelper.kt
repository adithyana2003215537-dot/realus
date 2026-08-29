package com.example.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class YouTubeMetadata(
  val videoId: String,
  val title: String,
  val authorName: String,
  val thumbnailUrl: String
)

object YouTubeHelper {
  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()

  /**
   * Extracts an 11-character YouTube video ID from various YouTube URL formats or raw ID.
   */
  fun extractVideoId(inputUrl: String): String? {
    val url = inputUrl.trim()
    if (url.isBlank()) return null

    // If it's directly an 11-char ID
    if (url.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
      return url
    }

    // Patterns for standard watch URLs, short URLs, embeds, shorts, music.youtube, etc.
    val patterns = listOf(
      Regex("(?:v=|/v/|embed/|shorts/|youtu\\.be/|watch\\?.*v=)([a-zA-Z0-9_-]{11})"),
      Regex("youtu\\.be/([a-zA-Z0-9_-]{11})"),
      Regex("youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})")
    )

    for (pattern in patterns) {
      val match = pattern.find(url)
      if (match != null && match.groupValues.size >= 2) {
        return match.groupValues[1]
      }
    }

    return null
  }

  /**
   * Validates video ID and fetches title, author, and thumbnail using YouTube's official oEmbed API.
   * Does NOT download or extract copyrighted streams.
   */
  suspend fun fetchMetadata(videoId: String): Result<YouTubeMetadata> = withContext(Dispatchers.IO) {
    try {
      val oEmbedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
      val request = Request.Builder()
        .url(oEmbedUrl)
        .header("User-Agent", "Mozilla/5.0 RealUs-AndroidApp")
        .build()

      val response = client.newCall(request).execute()
      if (!response.isSuccessful) {
        return@withContext Result.failure(
          IllegalArgumentException("Invalid, deleted, or private YouTube video (HTTP ${response.code}).")
        )
      }

      val bodyString = response.body?.string()
      if (bodyString.isNullOrBlank()) {
        return@withContext Result.failure(IllegalArgumentException("Empty response from YouTube oEmbed API."))
      }

      val json = JSONObject(bodyString)
      val title = json.optString("title", "YouTube Video")
      val author = json.optString("author_name", "YouTube Channel")
      val thumbnail = json.optString("thumbnail_url", "https://img.youtube.com/vi/$videoId/hqdefault.jpg")

      Result.success(
        YouTubeMetadata(
          videoId = videoId,
          title = title,
          authorName = author,
          thumbnailUrl = thumbnail
        )
      )
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
