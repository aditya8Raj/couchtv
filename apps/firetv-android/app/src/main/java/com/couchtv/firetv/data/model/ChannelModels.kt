package com.couchtv.firetv.data.model

data class ChannelFeed(
    val version: Int,
    val channels: List<Channel>
)

data class Channel(
    val id: String,
    val name: String,
    val country: String?,
    val categories: List<String>,
    val isHindiPriority: Boolean,
    val streams: List<Stream>
)

data class Stream(
    val url: String,
    val title: String?,
    val label: String?,
    val quality: String?,
    val httpReferrer: String?,
    val userAgent: String?
)
