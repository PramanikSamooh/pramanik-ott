package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.component.TvVideoCard
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.viewmodel.HomeViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvContinueWatchingScreen(
    homeViewModel: HomeViewModel,
    isHindi: Boolean
) {
    val continueWatching by homeViewModel.continueWatching.collectAsState(initial = emptyList())
    val recentlyWatched by homeViewModel.recentlyWatched.collectAsState(initial = emptyList())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 48.dp, end = 48.dp)
    ) {
        Text(
            text = if (isHindi) "जारी रखें" else "Continue Watching",
            style = PramanikTvTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isHindi) "जहाँ छोड़ा था वहीं से शुरू करें" else "Pick up where you left off",
            style = PramanikTvTheme.typography.bodyLarge,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(20.dp))

        val allItems = continueWatching + recentlyWatched.filter { recent ->
            continueWatching.none { it.videoId == recent.videoId }
        }

        if (allItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "अभी कोई वीडियो नहीं देखी गई" else "No videos watched yet",
                    style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                )
            }
        } else {
            TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allItems, key = { it.videoId }) { entry ->
                    val video = Video(
                        id = entry.videoId,
                        title = entry.title,
                        titleHi = entry.titleHi,
                        thumbnailUrl = entry.thumbnailUrl.ifEmpty { "https://i.ytimg.com/vi/${entry.videoId}/mqdefault.jpg" },
                        thumbnailUrlHQ = "https://i.ytimg.com/vi/${entry.videoId}/hqdefault.jpg",
                        channelName = entry.channelName,
                        durationFormatted = entry.durationFormatted
                    )
                    TvVideoCard(
                        video = video,
                        onClick = {
                            val intent = Intent(context, PlayerActivity::class.java).apply {
                                putExtra("videoId", entry.videoId)
                                putExtra("videoTitle", entry.title)
                                putExtra("videoTitleHi", entry.titleHi)
                                putExtra("videoThumbnail", entry.thumbnailUrl)
                                putExtra("playlistId", entry.playlistId)
                                putExtra("playlistTitle", entry.playlistTitle)
                                putExtra("sectionId", entry.sectionId)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
