package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.component.TvVideoCard
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.PlaylistWithVideos

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCategoryScreen(
    sectionId: String,
    homeViewModel: HomeViewModel,
    isHindi: Boolean
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Find the section data from home state
    val sectionData = remember(uiState.sections, sectionId) {
        uiState.sections.find { it.section.id == sectionId }
    }
    val title = remember(sectionData, isHindi) {
        sectionData?.section?.getLabel(isHindi) ?: sectionId.replaceFirstChar { it.uppercase() }
    }
    val playlists = remember(sectionData) {
        sectionData?.playlists ?: emptyList()
    }

    // Group playlists: pinned → latest → archive
    val featured = remember(playlists) { playlists.filter { it.playlist.pinned } }
    val nonFeatured = remember(playlists) { playlists.filter { !it.playlist.pinned } }
    val cutoffMs = remember { System.currentTimeMillis() - (35L * 24 * 60 * 60 * 1000) }
    val latest = remember(nonFeatured) {
        nonFeatured.filter { pw ->
            try {
                val pubDate = pw.videos.firstOrNull()?.publishedAt ?: ""
                if (pubDate.length >= 10) {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        .parse(pubDate.take(10))?.time?.let { it > cutoffMs } ?: false
                } else false
            } catch (_: Exception) { false }
        }
    }
    val latestIds = remember(latest) { latest.map { it.playlist.id }.toSet() }
    val archive = remember(nonFeatured, latestIds) {
        nonFeatured.filter { it.playlist.id !in latestIds }
    }

    val onVideoClick: (Video) -> Unit = { video ->
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("videoId", video.id)
            putExtra("videoTitle", video.title)
        }
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp)
    ) {
        // Header with title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = PramanikTvTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(12.dp))

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "कोई प्लेलिस्ट नहीं" else "No content available yet",
                    style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                )
            }
        } else {
            TvLazyColumn(
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Featured section
                if (featured.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isHindi) "विशेष" else "FEATURED",
                            style = PramanikTvTheme.typography.labelMedium.copy(
                                color = Saffron.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                    }
                    items(featured.size) { i -> TvPlaylistSection(featured[i], onVideoClick) }
                }

                // Latest section
                if (latest.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isHindi) "नवीनतम" else "LATEST",
                            style = PramanikTvTheme.typography.labelMedium.copy(
                                color = TextWhite.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                    }
                    items(latest.size) { i -> TvPlaylistSection(latest[i], onVideoClick) }
                }

                // Archive section
                if (archive.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isHindi) "संग्रह" else "ARCHIVE",
                            style = PramanikTvTheme.typography.labelMedium.copy(
                                color = TextGray.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                    }
                    items(archive.size) { i -> TvPlaylistSection(archive[i], onVideoClick) }
                }

                // Fallback if no grouping matched
                if (featured.isEmpty() && latest.isEmpty() && archive.isEmpty()) {
                    items(playlists.size) { i -> TvPlaylistSection(playlists[i], onVideoClick) }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvPlaylistSection(
    playlistWithVideos: PlaylistWithVideos,
    onVideoClick: (Video) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Playlist title
        Text(
            text = playlistWithVideos.playlist.title,
            style = PramanikTvTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            color = TextWhite,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 4.dp)
        )

        if (playlistWithVideos.playlist.videoCount > 0) {
            Text(
                text = "${playlistWithVideos.playlist.videoCount} videos",
                style = PramanikTvTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = TextGray,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Video cards row
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(playlistWithVideos.videos) { video ->
                TvVideoCard(
                    video = video,
                    onClick = { onVideoClick(video) }
                )
            }
        }
    }
}

