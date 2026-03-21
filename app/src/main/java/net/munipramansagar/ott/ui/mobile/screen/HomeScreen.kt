package net.munipramansagar.ott.ui.mobile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import net.munipramansagar.ott.data.model.Announcement
import net.munipramansagar.ott.ui.mobile.component.HeroBanner
import net.munipramansagar.ott.ui.mobile.component.ShimmerVideoRow
import net.munipramansagar.ott.ui.mobile.component.VideoCard
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.PlaylistWithVideos

@Composable
fun HomeScreen(
    isHindi: Boolean,
    onVideoClick: (String) -> Unit,
    onViewAllClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                repeat(4) { ShimmerVideoRow() }
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { viewModel.refresh() }) {
                        Text("Retry", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Announcements banner
                if (state.announcements.isNotEmpty()) {
                    AnnouncementsBanner(
                        announcements = state.announcements,
                        isHindi = isHindi
                    )
                }

                // Hero Banner
                if (state.heroBannerVideos.isNotEmpty()) {
                    HeroBanner(
                        videos = state.heroBannerVideos,
                        onVideoClick = { onVideoClick(it.id) }
                    )
                }

                // Section rows — each section contains playlists as sub-rows
                state.sections.forEach { sectionData ->
                    // Section header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sectionData.section.getLabel(isHindi),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isHindi) "सभी देखें" else "View All",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                onViewAllClick(sectionData.section.id)
                            }
                        )
                    }

                    // Playlist sub-rows within this section
                    sectionData.playlists.forEach { playlistWithVideos ->
                        PlaylistRow(
                            playlistWithVideos = playlistWithVideos,
                            onVideoClick = onVideoClick,
                            onPlaylistClick = onPlaylistClick
                        )
                    }
                }

                // Bottom padding
                Box(modifier = Modifier.padding(bottom = 80.dp))
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlistWithVideos: PlaylistWithVideos,
    onVideoClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        // Playlist header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = playlistWithVideos.playlist.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onPlaylistClick(playlistWithVideos.playlist.id)
                }
            )
        }

        // Horizontal scroll of videos
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

@Composable
private fun AnnouncementsBanner(
    announcements: List<Announcement>,
    isHindi: Boolean
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(announcements, key = { it.id }) { announcement ->
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (announcement.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = announcement.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = announcement.getTitle(isHindi),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
