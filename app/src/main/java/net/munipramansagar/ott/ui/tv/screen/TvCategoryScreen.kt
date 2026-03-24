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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    // Load full section data (all playlists, 50 videos each) — not the home page's limited data
    var fullSectionData by remember { mutableStateOf<net.munipramansagar.ott.viewmodel.HomeSectionData?>(null) }
    var isLoadingFull by remember { mutableStateOf(true) }
    LaunchedEffect(sectionId) {
        isLoadingFull = true
        fullSectionData = homeViewModel.getFullSectionData(sectionId)
        isLoadingFull = false
    }

    // Use full data if loaded, otherwise fall back to home page data
    val sectionData = fullSectionData ?: uiState.sections.find { it.section.id == sectionId }
    val title = sectionData?.section?.getLabel(isHindi) ?: sectionId.replaceFirstChar { it.uppercase() }
    val playlists = sectionData?.playlists ?: emptyList()

    // Group playlists: featured, monthly (latest+archive), series
    val monthlyPattern = remember { Regex("^\\d{4}-\\d{2}$") }
    val featured = remember(playlists) { playlists.filter { it.playlist.pinned } }
    val nonFeatured = remember(playlists) { playlists.filter { !it.playlist.pinned } }
    val monthly = remember(nonFeatured) {
        nonFeatured.filter { monthlyPattern.matches(it.playlist.title.trim()) }
            .sortedByDescending { it.playlist.title }
    }
    val series = remember(nonFeatured) {
        nonFeatured.filter { !monthlyPattern.matches(it.playlist.title.trim()) }
            .sortedBy { it.playlist.displayOrder }
    }
    val latest = remember(monthly) { monthly.take(3) }
    val archive = remember(monthly) { monthly.drop(3) }

    val onVideoClick: (Video, String, String, Int, List<Video>) -> Unit = { video, plId, plTitle, plIndex, allVideos ->
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("videoId", video.id)
            putExtra("videoTitle", video.title)
            putExtra("videoTitleHi", video.titleHi)
            putExtra("videoThumbnail", video.thumbnailUrl)
            putExtra("channelName", video.channelName)
            putExtra("durationFormatted", video.durationFormatted)
            putExtra("playlistId", plId)
            putExtra("playlistTitle", plTitle)
            putExtra("sectionId", sectionId)
            putExtra("playlistIndex", plIndex)
            putExtra("nextVideoIds", allVideos.map { it.id }.toTypedArray())
            putExtra("nextVideoTitles", allVideos.map { it.title }.toTypedArray())
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
                    item { TvSectionLabel(if (isHindi) "विशेष" else "FEATURED", Saffron.copy(alpha = 0.7f)) }
                    items(featured.size) { i -> TvPlaylistSection(featured[i], onVideoClick) }
                }

                // Latest monthly
                if (latest.isNotEmpty()) {
                    item { TvSectionLabel(if (isHindi) "नवीनतम" else "LATEST", TextWhite.copy(alpha = 0.6f)) }
                    items(latest.size) { i -> TvPlaylistSection(latest[i], onVideoClick) }
                }

                // Special Series
                if (series.isNotEmpty()) {
                    item { TvSectionLabel(if (isHindi) "विशेष श्रृंखला" else "SPECIAL SERIES", Saffron.copy(alpha = 0.7f)) }
                    items(series.size) { i -> TvPlaylistSection(series[i], onVideoClick) }
                }

                // Monthly Archive
                if (archive.isNotEmpty()) {
                    item { TvSectionLabel(if (isHindi) "मासिक संग्रह" else "MONTHLY ARCHIVE", TextGray.copy(alpha = 0.5f)) }
                    items(archive.size) { i -> TvPlaylistSection(archive[i], onVideoClick) }
                }

                // Fallback
                if (featured.isEmpty() && latest.isEmpty() && series.isEmpty() && archive.isEmpty()) {
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
    onVideoClick: (Video, String, String, Int, List<Video>) -> Unit
) {
    val pl = playlistWithVideos.playlist
    Column(modifier = Modifier.fillMaxWidth()) {
        // Playlist title
        Text(
            text = pl.title,
            style = PramanikTvTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            color = TextWhite,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 4.dp)
        )

        if (pl.videoCount > 0) {
            Text(
                text = "${pl.videoCount} videos",
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
            val videos = playlistWithVideos.videos
            items(videos.size) { index ->
                TvVideoCard(
                    video = videos[index],
                    onClick = { onVideoClick(videos[index], pl.id, pl.title, index, videos) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvSectionLabel(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = PramanikTvTheme.typography.labelMedium.copy(
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp
        ),
        modifier = Modifier.padding(horizontal = 48.dp)
    )
}

