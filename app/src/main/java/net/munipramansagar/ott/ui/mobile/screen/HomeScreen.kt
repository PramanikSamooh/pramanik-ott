package net.munipramansagar.ott.ui.mobile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import net.munipramansagar.ott.data.model.Announcement
import net.munipramansagar.ott.ui.mobile.component.HeroBanner
import net.munipramansagar.ott.ui.mobile.component.LiveStreamBanner
import net.munipramansagar.ott.ui.mobile.component.ShimmerVideoRow
import net.munipramansagar.ott.ui.mobile.component.VideoCard
import net.munipramansagar.ott.ui.mobile.theme.CardBg
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextMuted
import net.munipramansagar.ott.ui.mobile.theme.TextWhite
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.PathshalaViewModel
import net.munipramansagar.ott.viewmodel.PlaylistWithVideos

@Composable
fun HomeScreen(
    isHindi: Boolean,
    onVideoClick: (String) -> Unit,
    onViewAllClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit = {},
    onPathshalaClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    pathshalaViewModel: PathshalaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val pathshalaState by pathshalaViewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Shimmer hero placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )
                repeat(4) { ShimmerVideoRow() }
            }
        }

        state.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextGray
                    )
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Saffron,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry", fontWeight = FontWeight.SemiBold)
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
                // Live Stream Banner -- shown at top when live
                LiveStreamBanner(
                    isLive = state.liveStatus.isLive,
                    activeStreams = state.liveStatus.activeStreams,
                    onWatchClick = { videoId -> onVideoClick(videoId) }
                )

                // Pathshala Today card
                if (pathshalaState.todaysClasses.isNotEmpty()) {
                    PathshalaTodayCard(
                        todaysClasses = pathshalaState.todaysClasses,
                        isHindi = isHindi,
                        onViewPathshala = onPathshalaClick
                    )
                }

                // Announcements banner
                if (state.announcements.isNotEmpty()) {
                    AnnouncementsBanner(
                        announcements = state.announcements,
                        isHindi = isHindi
                    )
                }

                // Hero Banner -- cinematic edge-to-edge
                if (state.heroBannerVideos.isNotEmpty()) {
                    HeroBanner(
                        videos = state.heroBannerVideos,
                        onVideoClick = { onVideoClick(it.id) }
                    )
                }

                // Section rows -- each section contains playlists as sub-rows
                state.sections.forEach { sectionData ->
                    // Section header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sectionData.section.getLabel(isHindi),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "\u0938\u092D\u0940 \u0926\u0947\u0916\u0947\u0902 >" else "View All >",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Saffron,
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

                // Bottom padding for nav bar
                Spacer(modifier = Modifier.height(80.dp))
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
    Column(modifier = Modifier.padding(top = 12.dp)) {
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextWhite.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "View All >",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Saffron.copy(alpha = 0.8f),
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(announcements, key = { it.id }) { announcement ->
            val cardShape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier
                    .width(280.dp)
                    .height(76.dp)
                    .clip(cardShape)
                    .background(CardBg)
                    .border(1.dp, CardBorder, cardShape)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (announcement.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = announcement.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = announcement.getTitle(isHindi),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
