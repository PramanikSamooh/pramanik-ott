package net.munipramansagar.ott.ui.mobile.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import net.munipramansagar.ott.ui.mobile.component.VideoCard
import net.munipramansagar.ott.viewmodel.CategoryViewModel
import net.munipramansagar.ott.viewmodel.PlaylistWithVideos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    isHindi: Boolean,
    onVideoClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = state.section?.getLabel(isHindi) ?: if (isHindi) "प्लेलिस्ट" else "Playlists",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onBack) {
                            Text("Go Back", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            else -> {
                // Show playlists with preview videos
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.playlists) { playlistWithVideos ->
                        PlaylistSection(
                            playlistWithVideos = playlistWithVideos,
                            onVideoClick = onVideoClick,
                            onPlaylistClick = onPlaylistClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistSection(
    playlistWithVideos: PlaylistWithVideos,
    onVideoClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    Column {
        // Playlist header card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onPlaylistClick(playlistWithVideos.playlist.id) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (playlistWithVideos.playlist.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = playlistWithVideos.playlist.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .height(64.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.padding(end = 12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlistWithVideos.playlist.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (playlistWithVideos.playlist.videoCount > 0) {
                        Text(
                            text = "${playlistWithVideos.playlist.videoCount} videos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preview videos horizontal scroll
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(playlistWithVideos.videos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    onClick = { onVideoClick(video.id) }
                )
            }
        }
    }
}
