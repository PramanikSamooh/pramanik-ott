package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import net.munipramansagar.ott.ui.tv.theme.GlassBorder
import net.munipramansagar.ott.ui.tv.theme.GlassCard
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
        sectionData?.section?.getLabel(isHindi) ?: sectionId
    }
    val playlists = remember(sectionData) {
        sectionData?.playlists ?: emptyList()
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

        // Filter chips row (placeholder for future)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(label = "All", isActive = true)
            FilterChip(label = "Latest", isActive = false)
            FilterChip(label = "Popular", isActive = false)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isHindi) "कोई प्लेलिस्ट नहीं" else "No playlists available",
                        style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                    )
                }
            }
        } else {
            // Show playlists with their videos
            TvLazyColumn(
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(playlists.size) { index ->
                    val playlistWithVideos = playlists[index]
                    TvPlaylistSection(
                        playlistWithVideos = playlistWithVideos,
                        onVideoClick = onVideoClick
                    )
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FilterChip(
    label: String,
    isActive: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }

    val bgColor = when {
        isActive -> Saffron.copy(alpha = 0.2f)
        isFocused -> GlassCard
        else -> GlassCard.copy(alpha = 0.5f)
    }
    val borderColor = when {
        isActive -> Saffron.copy(alpha = 0.5f)
        isFocused -> GlassBorder.copy(alpha = 0.3f)
        else -> GlassBorder
    }
    val textColor = when {
        isActive -> Saffron
        isFocused -> TextWhite
        else -> TextGray
    }

    Text(
        text = label,
        style = PramanikTvTheme.typography.labelMedium.copy(
            color = textColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        ),
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .background(bgColor, PramanikTvTheme.shapes.pill)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    )
}
