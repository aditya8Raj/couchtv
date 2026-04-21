package com.couchtv.firetv.data.repository

import android.content.Context
import com.couchtv.firetv.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetChannelRepository(
    private val context: Context
) : ChannelRepository {

    override suspend fun getChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val json = context.assets.open("sample_channels.json").bufferedReader().use { it.readText() }
        ChannelJsonParser.parseFeed(json).channels
    }
}
