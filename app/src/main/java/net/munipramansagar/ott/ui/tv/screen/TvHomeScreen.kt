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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
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
import net.munipramansagar.ott.ui.tv.component.TvHeroBanner
import net.munipramansagar.ott.ui.tv.component.TvVideoCard
import net.munipramansagar.ott.ui.tv.component.TvVideoCardShimmer
import net.munipramansagar.ott.ui.tv.component.shimmerBrush
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.SaffronLight
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.viewmodel.HomeViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen(
    homeViewModel: HomeViewModel,
    isHindi: Boolean,
    onCategoryClick: (String) -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val onVideoClick: (Video) -> Unit = { video ->
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("videoId", video.id)
            putExtra("videoTitle", video.title)
        }
        context.startActivity(intent)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            TvHomeShimmer()
        } else if (uiState.error != null) {
            TvErrorState(
                message = uiState.error ?: "Something went wrong",
                onRetry = { homeViewModel.refresh() }
            )
        } else {
            TvLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp)
            ) {
                // Hero banner – full width
                if (uiState.heroBannerVideos.isNotEmpty()) {
                    item {
                        TvHeroBanner(
                            videos = uiState.heroBannerVideos,
                            onPlayClick = onVideoClick
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Category rows
                items(uiState.rows.size) { index ->
                    val row = uiState.rows[index]
                    TvCategoryRow(
                        title = if (isHindi) row.labelHi else row.label,
                        videos = row.videos,
                        onVideoClick = onVideoClick,
                        onViewAllClick = { onCategoryClick(row.categorySlug) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvCategoryRow(
    title: String,
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Row header: title + "View All >"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = PramanikTvTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = TextWhite
            )

            // "View All" link
            var viewAllFocused by remember { mutableStateOf(false) }
            Text(
                text = "View All \u203A",
                style = PramanikTvTheme.typography.labelLarge.copy(
                    color = if (viewAllFocused) SaffronLight else Saffron,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .onFocusChanged { viewAllFocused = it.isFocused }
                    .focusable()
                    .clickable { onViewAllClick() }
                    .background(
                        if (viewAllFocused) Saffron.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Video cards row
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(videos) { video ->
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
private fun TvHomeShimmer() {
    TvLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        // Banner shimmer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(shimmerBrush())
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Row shimmers
        items(3) {
            Column(modifier = Modifier.padding(horizontal = 48.dp)) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(20.dp)
                        .background(shimmerBrush(), RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    repeat(5) {
                        TvVideoCardShimmer()
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
            )
            Spacer(modifier = Modifier.height(20.dp))
            androidx.tv.material3.Button(
                onClick = onRetry,
                colors = androidx.tv.material3.ButtonDefaults.colors(
                    containerColor = Saffron,
                    contentColor = TextWhite,
                    focusedContainerColor = SaffronLight,
                    focusedContentColor = TextWhite
                ),
                shape = androidx.tv.material3.ButtonDefaults.shape(
                    shape = PramanikTvTheme.shapes.button
                )
            ) {
                Text(
                    "Retry",
                    style = PramanikTvTheme.typography.labelLarge
                )
            }
        }
    }
}
