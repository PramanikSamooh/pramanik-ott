package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.data.model.Category
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.component.TvVideoCard
import net.munipramansagar.ott.ui.tv.theme.DarkBackground
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.viewmodel.HomeViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCategoryScreen(
    categorySlug: String,
    homeViewModel: HomeViewModel,
    isHindi: Boolean
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val category = remember(categorySlug) { Category.fromSlug(categorySlug) }
    val title = remember(category, isHindi) {
        category?.getLabel(isHindi) ?: categorySlug
    }
    val videos = remember(uiState.rows, categorySlug) {
        uiState.rows.find { it.categorySlug == categorySlug }?.videos ?: emptyList()
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
            .background(DarkBackground)
            .padding(top = 32.dp)
    ) {
        // Title
        Text(
            text = title,
            style = PramanikTvTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No videos available",
                    style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                )
            }
        } else {
            TvLazyVerticalGrid(
                columns = TvGridCells.Adaptive(260.dp),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(videos) { video ->
                    TvVideoCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        cardWidth = 260
                    )
                }
            }
        }
    }
}
