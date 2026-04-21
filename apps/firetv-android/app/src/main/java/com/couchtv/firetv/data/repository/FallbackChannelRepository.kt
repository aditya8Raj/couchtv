package com.couchtv.firetv.data.repository

import com.couchtv.firetv.data.model.Channel

class FallbackChannelRepository(
    private val primary: ChannelRepository,
    private val fallback: ChannelRepository
) : ChannelRepository {

    override suspend fun getChannels(): List<Channel> {
        return try {
            val channels = primary.getChannels()
            if (channels.isNotEmpty()) channels else fallback.getChannels()
        } catch (_: Exception) {
            fallback.getChannels()
        }
    }
}
