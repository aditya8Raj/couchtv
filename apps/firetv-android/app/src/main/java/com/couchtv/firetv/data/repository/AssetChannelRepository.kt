package com.couchtv.firetv.data.repository

import android.content.Context
import com.couchtv.firetv.data.model.Channel
import com.couchtv.firetv.data.model.ChannelFeed
import com.couchtv.firetv.data.model.Stream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AssetChannelRepository(
    private val context: Context
) : ChannelRepository {

    override suspend fun getChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val json = context.assets.open("sample_channels.json").bufferedReader().use { it.readText() }
        parseFeed(JSONObject(json)).channels
    }

    private fun parseFeed(root: JSONObject): ChannelFeed {
        val version = root.optInt("version", 1)
        val channels = root.optJSONArray("channels")?.toChannelList() ?: emptyList()
        return ChannelFeed(version = version, channels = channels)
    }

    private fun JSONArray.toChannelList(): List<Channel> {
        val result = mutableListOf<Channel>()
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            result += item.toChannel()
        }
        return result
    }

    private fun JSONObject.toChannel(): Channel {
        return Channel(
            id = optString("id"),
            name = optString("name"),
            country = optString("country"),
            categories = optJSONArray("categories").toStringList(),
            isHindiPriority = optBoolean("is_hindi_priority", false),
            streams = optJSONArray("streams").toStreamList()
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()

        val result = mutableListOf<String>()
        for (index in 0 until length()) {
            val value = optString(index).trim()
            if (value.isNotEmpty()) {
                result += value
            }
        }
        return result
    }

    private fun JSONArray?.toStreamList(): List<Stream> {
        if (this == null) return emptyList()

        val result = mutableListOf<Stream>()
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            result += Stream(
                url = item.optString("url"),
                title = item.optString("title").ifBlank { null },
                label = item.optString("label").ifBlank { null },
                quality = item.optString("quality").ifBlank { null },
                httpReferrer = item.optString("http_referrer").ifBlank { null },
                userAgent = item.optString("user_agent").ifBlank { null }
            )
        }
        return result
    }
}
