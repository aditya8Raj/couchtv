package com.couchtv.firetv.data.repository

import com.couchtv.firetv.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class RemoteChannelRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val feedUrls: List<String> = DEFAULT_FEED_URLS
) : ChannelRepository {

    override suspend fun getChannels(): List<Channel> = withContext(Dispatchers.IO) {
        var lastError: Exception? = null

        for (url in feedUrls) {
            try {
                val channels = fetchChannels(url)
                if (channels.isNotEmpty()) {
                    return@withContext channels
                }
            } catch (error: Exception) {
                lastError = error
            }
        }

        throw IOException(
            "Unable to load remote feed from configured endpoints",
            lastError
        )
    }

    private fun fetchChannels(url: String): List<Channel> {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Remote feed request to $url failed with HTTP ${response.code}")
            }

            val body = response.body?.string()
                ?: throw IOException("Remote feed response body from $url is empty")

            ChannelJsonParser.parseFeed(body).channels
        }
    }

    companion object {
        val DEFAULT_FEED_URLS = listOf(
            "https://aditya8raj.github.io/couchtv/channels.json",
            "https://raw.githubusercontent.com/aditya8Raj/couchtv/gh-pages/channels.json",
            "https://raw.githubusercontent.com/aditya8Raj/couchtv/main/pipeline/dist/channels.json"
        )
    }
}
