package com.couchtv.firetv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.couchtv.firetv.data.model.Channel
import com.couchtv.firetv.data.repository.AssetChannelRepository
import com.couchtv.firetv.data.repository.FallbackChannelRepository
import com.couchtv.firetv.data.repository.RemoteChannelRepository
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CouchTvApp()
        }
    }
}

@Composable
private fun CouchTvApp() {
    val context = LocalContext.current
    val repository = remember(context.applicationContext) {
        val fallback = AssetChannelRepository(context.applicationContext)
        val remote = RemoteChannelRepository()
        FallbackChannelRepository(primary = remote, fallback = fallback)
    }
    var state by remember { mutableStateOf<HomeState>(HomeState.Loading) }
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }

    LaunchedEffect(repository) {
        state = try {
            HomeState.Ready(repository.getChannels())
        } catch (error: Exception) {
            HomeState.Error(error.message ?: "Failed to load channels")
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val current = state) {
                HomeState.Loading -> LoadingView()
                is HomeState.Error -> ErrorView(current.message)
                is HomeState.Ready -> {
                    val channel = selectedChannel
                    if (channel == null) {
                        ChannelListView(
                            channels = current.channels,
                            onChannelSelected = { selectedChannel = it }
                        )
                    } else {
                        PlaybackView(
                            channel = channel,
                            onBack = { selectedChannel = null }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "CouchTV", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Loading curated channels...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "CouchTV", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Could not load channels", style = MaterialTheme.typography.bodyLarge)
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ChannelListView(channels: List<Channel>, onChannelSelected: (Channel) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(text = "CouchTV", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Curated feed loaded: ${channels.size} channels",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Source: remote JSON feed with local fallback",
            style = MaterialTheme.typography.bodyMedium
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(channels, key = { it.id }) { channel ->
                ChannelRow(channel = channel, onClick = { onChannelSelected(channel) })
            }
        }
    }
}

@Composable
private fun ChannelRow(channel: Channel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = channel.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        val badge = if (channel.isHindiPriority) "Hindi" else "General"
        Text(text = badge, style = MaterialTheme.typography.bodyMedium)
    }

    val categoryLabel = if (channel.categories.isEmpty()) "Uncategorized" else channel.categories.joinToString()
    Text(text = categoryLabel, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun PlaybackView(channel: Channel, onBack: () -> Unit) {
    val context = LocalContext.current
    val streamUrl = channel.streams.firstOrNull()?.url.orEmpty()

    if (streamUrl.isBlank()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(text = channel.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "No playable stream available for this channel.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text(text = "Back to Channels")
            }
        }
        return
    }

    val exoPlayer = remember(streamUrl) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(streamUrl))
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = channel.name, style = MaterialTheme.typography.headlineSmall)
                Text(text = "Playing live stream", style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = onBack) {
                Text(text = "Back")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = true
                    player = exoPlayer
                }
            },
            update = { it.player = exoPlayer }
        )
    }
}

private sealed interface HomeState {
    data object Loading : HomeState
    data class Ready(val channels: List<Channel>) : HomeState
    data class Error(val message: String) : HomeState
}
