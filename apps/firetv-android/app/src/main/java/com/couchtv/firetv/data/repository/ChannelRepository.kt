package com.couchtv.firetv.data.repository

import com.couchtv.firetv.data.model.Channel

interface ChannelRepository {
    suspend fun getChannels(): List<Channel>
}
